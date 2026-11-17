package com.neyvan.desinstalador.data

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val category: InstalledAppCategory,
    val sizeBytes: Long = 0L,
    val formattedSize: String = "",
    private val packageManager: PackageManager? = null,
    private var cachedIcon: Drawable? = null
) {
    val isSystem: Boolean
        get() = category != InstalledAppCategory.USER_APP

    val isProtected: Boolean
        get() = category == InstalledAppCategory.CRITICAL_SYSTEM_APP

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
