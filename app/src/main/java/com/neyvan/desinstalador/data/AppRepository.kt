package com.neyvan.desinstalador.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.text.format.Formatter
import java.io.File

class AppRepository(private val context: Context) {

    // Lista de proteção de integridade do sistema contra exclusão acidental ou sabotagem (Hardening de Segurança)
    private val criticalSystemPackages = setOf(
        // Core OS & Telecom
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.keyguard",

        // Package Installers oficiais
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.miui.packageinstaller",
        "com.samsung.android.packageinstaller",

        // Google Core Framework & Play Services
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.vending",

        // WebViews essenciais
        "com.google.android.webview",
        "com.android.webview",

        // Launchers principais e módulos de segurança de fabricantes
        "com.miui.home",
        "com.miui.securitycenter",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.sec.android.app.launcher"
    )

    fun loadInstalledApps(): List<InstalledApp> {
        val packageManager = context.packageManager
        val ownPackage = context.packageName

        // getInstalledApplications sem GET_META_DATA é instantâneo
        return packageManager.getInstalledApplications(0)
            .asSequence()
            .filter { it.packageName != ownPackage && it.packageName !in criticalSystemPackages }
            .map { info ->
                val sizeBytes = calculateFastAppSize(info)
                val formattedSize = if (sizeBytes > 0) {
                    Formatter.formatFileSize(context, sizeBytes)
                } else {
                    ""
                }

                val label = try {
                    info.loadLabel(packageManager).toString()
                } catch (_: Throwable) {
                    info.packageName
                }

                InstalledApp(
                    packageName = info.packageName,
                    label = label,
                    isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0 &&
                        info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP == 0,
                    sizeBytes = sizeBytes,
                    formattedSize = formattedSize
                )
            }
            .toList()
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
