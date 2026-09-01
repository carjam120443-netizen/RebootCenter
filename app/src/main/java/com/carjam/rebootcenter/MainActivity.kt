package com.carjam.rebootcenter

import android.os.Bundle
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RebootCenterScreen()
                }
            }
        }
    }
}

data class RebootAction(val icon: String, val title: String, val description: String)

@Composable
private fun RebootCenterScreen() {
    var message by remember { mutableStateOf("Ready") }
    val shizukuRunning = remember { runCatching { Shizuku.pingBinder() }.getOrDefault(false) }
    val actions = listOf(
        RebootAction("🔄", "Restart", "Restart Android normally"),
        RebootAction("⚡", "Bootloader", "Reboot to bootloader when supported"),
        RebootAction("🛠", "Recovery", "Reboot to recovery when supported"),
        RebootAction("🔌", "Power off", "Power the device down when supported")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("⚡ RebootCenter", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(6.dp))
            Text("Modern reboot controls with optional Shizuku support")
            Spacer(Modifier.height(14.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (shizukuRunning) "🟢 Shizuku connected" else "🟠 Shizuku unavailable")
                    Spacer(Modifier.height(4.dp))
                    Text(if (shizukuRunning) "Privileged operations can be enabled in later builds." else "Start Shizuku and grant permission to enable advanced controls.")
                }
            }

            Spacer(Modifier.height(8.dp))
            Text("Actions", style = MaterialTheme.typography.titleLarge)
        }

        items(actions) { action ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${action.icon}  ${action.title}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(action.description)
                    }
                    Button(onClick = { message = "${action.title}: implementation coming next" }) {
                        Text("Run")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("Status: $message")
            Spacer(Modifier.height(12.dp))
            Text("RebootCenter never performs destructive data wipes from the reboot menu.")
        }
    }
}
