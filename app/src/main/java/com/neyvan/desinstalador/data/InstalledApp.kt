package com.neyvan.desinstalador.data

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
    val sizeBytes: Long = 0L,
    val formattedSize: String = "",
    private val packageManager: PackageManager? = null,
    private var cachedIcon: Drawable? = null
) {
    fun getIcon(pm: PackageManager): Drawable {
        cachedIcon?.let { return it }
        val loaded = try {
            pm.getApplicationIcon(packageName)
        } catch (_: Throwable) {
            pm.defaultActivityIcon
        }
        cachedIcon = loaded
        return loaded
    }
}
