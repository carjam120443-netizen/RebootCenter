package com.carjam.rebootcenter

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

private const val SHIZUKU_PERMISSION_CODE = 100
private const val EXECUTE_TRANSACTION = IBinder.FIRST_CALL_TRANSACTION

class MainActivity : ComponentActivity() {
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, _ ->
        if (requestCode == SHIZUKU_PERMISSION_CODE) recreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Shizuku.addRequestPermissionResultListener(permissionListener)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { RebootCenterScreen() }
            }
        }
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        ShizukuCommandRunner.unbind()
        super.onDestroy()
    }
}

data class RebootAction(val icon: String, val title: String, val command: String, val description: String)

private fun hasShizukuPermission(): Boolean = runCatching {
    Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
}.getOrDefault(false)

private fun isShizukuRunning(): Boolean = runCatching {
    Shizuku.pingBinder()
}.getOrDefault(false)

private fun requestShizukuPermission() {
    runCatching { Shizuku.requestPermission(SHIZUKU_PERMISSION_CODE) }
}

private object ShizukuCommandRunner {
    private var service: IBinder? = null
    private var connection: ServiceConnection? = null
    private val waitLock = Object()

    fun bind() {
        if (!hasShizukuPermission() || service != null || connection != null) return

        val args = Shizuku.UserServiceArgs(
            ComponentName("com.carjam.rebootcenter", RebootUserService::class.java.name)
        )
            .daemon(false)
            .debuggable(true)
            .version(1)
            .tag("reboot-command-service")

        val newConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                synchronized(waitLock) {
                    service = binder
                    waitLock.notifyAll()
                }
            }

            override fun onServiceDisconnected(name: ComponentName) {
                synchronized(waitLock) {
                    service = null
                    connection = null
                    waitLock.notifyAll()
                }
            }
        }

        connection = newConnection
        runCatching {
            Shizuku.bindUserService(args, newConnection)
        }.onFailure {
            synchronized(waitLock) {
                service = null
                connection = null
                waitLock.notifyAll()
            }
        }
    }

    fun run(command: String, onResult: (String) -> Unit) {
        Thread {
            bind()

            val deadline = System.currentTimeMillis() + 5000
            synchronized(waitLock) {
                while (service == null && connection != null && System.currentTimeMillis() < deadline) {
                    runCatching { waitLock.wait(250) }
                }
            }

            val binder = service
            if (binder == null) {
                onResult("Failed: Shizuku UserService could not connect. Check that Shizuku permission is granted, then try again.")
                return@Thread
            }

            runCatching {
                val data = Parcel.obtain()
                val reply = Parcel.obtain()
                try {
                    data.writeString(command)
                    binder.transact(EXECUTE_TRANSACTION, data, reply, 0)
                    reply.readString() ?: "Command completed."
                } finally {
                    data.recycle()
                    reply.recycle()
                }
            }.onSuccess(onResult)
                .onFailure { onResult("Failed: ${it.message ?: "unknown error"}") }
        }.start()
    }

    fun unbind() {
        val currentConnection = connection ?: return
        runCatching {
            val args = Shizuku.UserServiceArgs(
                ComponentName("com.carjam.rebootcenter", RebootUserService::class.java.name)
            )
                .daemon(false)
                .version(1)
                .tag("reboot-command-service")
            Shizuku.unbindUserService(args, currentConnection, true)
        }
        service = null
        connection = null
    }
}

@Composable
private fun RebootCenterScreen() {
    var message by remember { mutableStateOf("Ready") }
    var permissionState by remember { mutableStateOf(hasShizukuPermission()) }
    var shizukuRunning by remember { mutableStateOf(isShizukuRunning()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            shizukuRunning = isShizukuRunning()
            permissionState = hasShizukuPermission()
            delay(1000)
        }
    }

    val actions = listOf(
        RebootAction("🔄", "Restart", "reboot", "Restart Android normally"),
        RebootAction("⚡", "Bootloader", "reboot bootloader", "Reboot to bootloader / fastboot when supported"),
        RebootAction("🛠", "Recovery", "reboot recovery", "Reboot to recovery when supported"),
        RebootAction("🔌", "Power off", "reboot -p", "Power the device down when supported")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("⚡ RebootCenter", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text("Shizuku-powered reboot controls")
            Spacer(Modifier.height(14.dp))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (shizukuRunning) "🟢 Shizuku connected" else "🔴 Shizuku not running")
                    Spacer(Modifier.height(4.dp))
                    Text(if (permissionState) "✅ RebootCenter has Shizuku permission" else "🔐 Permission is required for reboot actions")
                    Spacer(Modifier.height(10.dp))
                    if (shizukuRunning && !permissionState) {
                        Button(onClick = { requestShizukuPermission() }) {
                            Text("Grant Shizuku Permission")
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Actions", style = MaterialTheme.typography.titleLarge)
        }

        items(actions) { action ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${action.icon}  ${action.title}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(action.description)
                    }
                    Button(
                        enabled = permissionState,
                        onClick = {
                            message = "Connecting to Shizuku UserService…"
                            ShizukuCommandRunner.run(action.command) { result ->
                                scope.launch(Dispatchers.Main) {
                                    message = "${action.title}: $result"
                                    permissionState = hasShizukuPermission()
                                }
                            }
                        }
                    ) { Text("Run") }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("Status: $message")
            Spacer(Modifier.height(12.dp))
            Text("⚠️ RebootCenter does not unlock bootloaders, wipe data, or flash partitions.")
        }
    }
}
