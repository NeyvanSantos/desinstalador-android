package com.neyvan.desinstalador.accessibility

import android.content.Context
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings

object AccessibilityHelper {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expectedServiceName = "${context.packageName}/${AutoUninstallAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val enabledInSettings = enabledServices.split(':')
            .any { it.equals(expectedServiceName, ignoreCase = true) }
        val serviceRunning = AutoUninstallAccessibilityService.isServiceRunning()
        return enabledInSettings && serviceRunning
    }

    fun openAccessibilitySettings(context: Context): Boolean {
        try {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
            return true
        } catch (_: ActivityNotFoundException) {
            return false
        } catch (_: SecurityException) {
            return false
        }
    }

}
