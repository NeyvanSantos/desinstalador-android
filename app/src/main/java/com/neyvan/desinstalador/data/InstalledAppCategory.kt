package com.neyvan.desinstalador.data

enum class InstalledAppCategory {
    USER_APP,
    SYSTEM_APP,
    UPDATED_SYSTEM_APP,
    CRITICAL_SYSTEM_APP
}

object CriticalSystemPackages {
    private val operatingSystem = setOf(
        "android",
        "com.android.systemui",
        "com.android.settings",
        "com.android.permissioncontroller",
        "com.google.android.permissioncontroller",
        "com.android.phone",
        "com.android.server.telecom",
        "com.android.keyguard",
        "com.android.providers.settings",
        "com.android.providers.telephony",
        "com.android.emergency",
        "com.android.cellbroadcastreceiver"
    )

    private val packageInstallers = setOf(
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.miui.packageinstaller",
        "com.samsung.android.packageinstaller"
    )

    private val platformServices = setOf(
        "com.google.android.gms",
        "com.google.android.gsf",
        "com.android.vending",
        "com.google.android.webview",
        "com.android.webview"
    )

    private val launchersAndSecurity = setOf(
        "com.miui.home",
        "com.miui.securitycenter",
        "com.google.android.apps.nexuslauncher",
        "com.android.launcher3",
        "com.sec.android.app.launcher"
    )

    val all: Set<String> = operatingSystem + packageInstallers + platformServices + launchersAndSecurity

    fun contains(packageName: String): Boolean = packageName in all
}

fun classifyInstalledApp(
    packageName: String,
    isSystemApp: Boolean,
    isUpdatedSystemApp: Boolean
): InstalledAppCategory = when {
    CriticalSystemPackages.contains(packageName) -> InstalledAppCategory.CRITICAL_SYSTEM_APP
    isUpdatedSystemApp -> InstalledAppCategory.UPDATED_SYSTEM_APP
    isSystemApp -> InstalledAppCategory.SYSTEM_APP
    else -> InstalledAppCategory.USER_APP
}
