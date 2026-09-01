package com.carjam.rebootcenter

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Shizuku UserService.
 *
 * Shizuku UserServices are not normal Android Services. The class itself
 * implements IBinder so Shizuku can instantiate it in the user-service process.
 */
class RebootUserService : Binder() {

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (code == IBinder.FIRST_CALL_TRANSACTION) {
            val command = data.readString().orEmpty()
            val result = runCommand(command)
            reply?.writeString(result)
            return true
        }
        return super.onTransact(code, data, reply, flags)
    }

    private fun runCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }.trim()
            val error = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }.trim()
            val exitCode = process.waitFor()

            when {
                error.isNotBlank() -> "Failed (exit $exitCode): $error"
                output.isNotBlank() -> output
                exitCode == 0 -> "Command sent successfully."
                else -> "Command failed with exit code $exitCode."
            }
        } catch (e: Exception) {
            "Failed: ${e.message ?: "unknown error"}"
        }
    }
}
