package com.neyvan.desinstalador.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AutoUninstallAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isRunning.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Princípio Fail-Closed: se a automação não estiver ativada ou tiver expirado, aborta imediatamente
        if (!isAutomationActive()) return
        if (event == null) return

        // Validação estrita de segurança: só processa janelas de instaladores oficiais verificados
        val eventPackage = event.packageName?.toString() ?: ""
        if (eventPackage.isNotEmpty() && eventPackage !in ALLOWED_PACKAGE_INSTALLERS) {
            return
        }

        val root = rootInActiveWindow ?: return
        val rootPackage = root.packageName?.toString() ?: ""
        if (rootPackage.isNotEmpty() && rootPackage !in ALLOWED_PACKAGE_INSTALLERS) {
            return
        }

        val clicked = clickConfirmButton(root)
        if (clicked) {
            // Renova o tempo de tolerância para o próximo app da fila
            armAutomation()
        }
    }

    private fun clickConfirmButton(root: AccessibilityNodeInfo): Boolean {
        // Tenta achar botões pelo id padrão do Android para diálogos de confirmação
        val androidButtonIds = listOf(
            "android:id/button1", // Botão positivo (OK / Desinstalar)
            "com.android.packageinstaller:id/ok_button",
            "com.google.android.packageinstaller:id/ok_button",
            "com.miui.packageinstaller:id/ok_button"
        )

        for (resId in androidButtonIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(resId)
            for (node in nodes) {
                if (node.isClickable && node.isEnabled) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }

        // Tenta buscar por textos comuns em vários idiomas/formatos
        val targetTexts = listOf(
            "OK", "Ok", "ok",
            "Desinstalar", "DESINSTALAR",
            "Uninstall", "UNINSTALL"
        )

        for (text in targetTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(text)
            for (node in nodes) {
                if (node.isClickable && node.isEnabled) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
                // Se o próprio texto não for clicável, tenta o pai dele (caso seja um botão container)
                val parent = node.parent
                if (parent != null) {
                    if (parent.isClickable && parent.isEnabled) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                }
            }
        }

        return false
    }

    override fun onInterrupt() {
        disarmAutomation()
        _isRunning.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        disarmAutomation()
        if (instance == this) {
            instance = null
            _isRunning.value = false
        }
    }

    companion object {
        // Timeout de segurança: a automação expira após 15 segundos sem nova ação para evitar execuções acidentais
        private const val DEFAULT_TIMEOUT_MS = 15_000L
        private var automationDeadlineMs: Long = 0L

        private val ALLOWED_PACKAGE_INSTALLERS = setOf(
            "com.google.android.packageinstaller",
            "com.android.packageinstaller",
            "com.miui.packageinstaller",
            "com.samsung.android.packageinstaller"
        )

        private var instance: AutoUninstallAccessibilityService? = null

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun isServiceRunning(): Boolean = instance != null

        /**
         * Arma a automação com uma janela de tempo de expiração segura (Princípio Fail-Closed).
         */
        @Synchronized
        fun armAutomation(timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
            automationDeadlineMs = SystemClock.elapsedRealtime() + timeoutMs
        }

        /**
         * Desarma imediatamente a automação, impedindo cliques posteriores.
         */
        @Synchronized
        fun disarmAutomation() {
            automationDeadlineMs = 0L
        }

        /**
         * Verifica se a automação está ativamente armada e dentro do período de validade.
         */
        @Synchronized
        fun isAutomationActive(): Boolean {
            val now = SystemClock.elapsedRealtime()
            val active = now <= automationDeadlineMs
            if (!active && automationDeadlineMs != 0L) {
                automationDeadlineMs = 0L
            }
            return active
        }
    }
}
