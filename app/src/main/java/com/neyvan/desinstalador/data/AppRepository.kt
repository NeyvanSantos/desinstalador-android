package com.neyvan.desinstalador.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.format.Formatter
import java.io.File

class AppRepository(private val context: Context) {

    fun loadInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val ownPackage = context.packageName

        // getInstalledApplications sem GET_META_DATA é instantâneo
        return packageManager.getInstalledApplications(0)
            .asSequence()
            .filter { it.packageName != ownPackage }
            .map { info ->
                val sizeBytes = calculateFastAppSize(info)
                val formattedSize = if (sizeBytes > 0) {
                    Formatter.formatFileSize(context, sizeBytes)
                } else {
                    ""
                }

                val isSystemApp = info.flags and ApplicationInfo.FLAG_SYSTEM != 0
                val isUpdatedSystemApp = info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                val label = try {
                    info.loadLabel(packageManager).toString()
                } catch (_: Throwable) {
                    info.packageName
                }

                InstalledApp(
                    packageName = info.packageName,
                    label = label,
                    category = classifyInstalledApp(
                        packageName = info.packageName,
                        isSystemApp = isSystemApp,
                        isUpdatedSystemApp = isUpdatedSystemApp
                    ),
                    sizeBytes = sizeBytes,
                    formattedSize = formattedSize
                )
            }
            .toList()
    }

    fun isPackageInstalled(packageName: String): PackagePresence =
        try {
            context.packageManager.getApplicationInfo(packageName, 0)
            PackagePresence.INSTALLED
        } catch (_: PackageManager.NameNotFoundException) {
            PackagePresence.NOT_INSTALLED
        } catch (error: Exception) {
            PackagePresence.Unknown(error)
        }

    private fun calculateFastAppSize(info: ApplicationInfo): Long {
        return try {
            var total = 0L
            val sourceDir = info.sourceDir
            if (sourceDir != null) {
                val file = File(sourceDir)
                if (file.exists()) {
                    total += file.length()
                }
            }
            val splitDirs = info.splitSourceDirs
            if (splitDirs != null) {
                for (split in splitDirs) {
                    val file = File(split)
                    if (file.exists()) {
                        total += file.length()
                    }
                }
            }
            total
        } catch (_: Throwable) {
            0L
        }
    }
}

sealed interface PackagePresence {
    data object INSTALLED : PackagePresence
    data object NOT_INSTALLED : PackagePresence
    data class Unknown(val cause: Exception) : PackagePresence
}
