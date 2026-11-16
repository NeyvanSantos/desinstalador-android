package com.neyvan.desinstalador.accessibility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings

object AccessibilityHelper {
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        if (AutoUninstallAccessibilityService.isServiceRunning()) return true

        val expectedServiceName = "${context.packageName}/${AutoUninstallAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabledServices.split(':').any { it.equals(expectedServiceName, ignoreCase = true) }
    }

    fun openAccessibilitySettings(context: Context) {
        val serviceComponent = "${context.packageName}/${AutoUninstallAccessibilityService::class.java.canonicalName}"

        // Tenta abrir direto com os argumentos padrão do Android/AOSP e MIUI
        val intents = listOf(
            // 1. Tenta ir direto para os detalhes do serviço de acessibilidade se a ROM suportar
            Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            },
            // 2. Tenta a tela de Acessibilidade com destaque/foco no serviço específico
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(":settings:fragment_args_key", serviceComponent)
                putExtra(":settings:show_fragment_args", Bundle().apply {
                    putString(":settings:fragment_args_key", serviceComponent)
                })
            },
            // 3. Fallback clássico para a tela geral de Acessibilidade
            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )

        for (intent in intents) {
            try {
                if (intent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(intent)
                    return
                }
            } catch (_: Throwable) {
                // Tenta a próxima opção
            }
        }

        try {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (_: Throwable) {
        }
    }
}
