package com.aman.shiii_android.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aman.shiii_android.data.local.AuthPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    @Inject
    lateinit var authPreferences: AuthPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i(TAG, "Device boot completed, checking for active user")
            val user = authPreferences.getUser()
            if (user != null) {
                Log.i(TAG, "Active user ${user.username} found, restarting ShiiiSyncForegroundService")
                ShiiiSyncForegroundService.startService(context)
            }
        }
    }
}
