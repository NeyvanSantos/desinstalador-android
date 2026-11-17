package com.neyvan.desinstalador.accessibility

import java.text.Normalizer

data class UninstallAutomationTarget(
    val operationId: String,
    val packageName: String,
    val appLabel: String
)

internal data class InstallerDialogObservation(
    val installerPackageName: String,
    val windowPackageName: String?,
    val rootVisible: Boolean,
    val eventWindowId: Int,
    val rootWindowId: Int?,
    val visibleTexts: Collection<String>,
    val hasPositiveAction: Boolean
) {
    fun verifies(target: UninstallAutomationTarget): Boolean =
        InstallerPackages.isAllowed(installerPackageName) &&
            windowPackageName == installerPackageName &&
            rootVisible &&
            rootWindowId == eventWindowId &&
            UninstallDialogMatcher.isUninstallConfirmation(visibleTexts) &&
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = target.packageName,
                expectedAppLabel = target.appLabel,
                visibleTexts = visibleTexts
            ) &&
            hasPositiveAction
}

internal object InstallerPackages {
    val allowed = setOf(
        "com.google.android.packageinstaller",
        "com.android.packageinstaller",
        "com.miui.packageinstaller",
        "com.samsung.android.packageinstaller"
    )

    fun isAllowed(packageName: String): Boolean = packageName in allowed
}

internal fun isAutomationAvailable(serviceEnabled: Boolean, userConsented: Boolean): Boolean =
    serviceEnabled && userConsented

internal fun canAutomateUninstallForApp(
    isSystemApp: Boolean,
    hasAmbiguousAppLabel: Boolean
): Boolean = !isSystemApp && !hasAmbiguousAppLabel

internal class UninstallAutomationGate {
    private var target: UninstallAutomationTarget? = null
    private var armedAtUptimeMs = 0L
    private var deadlineUptimeMs = 0L

    @Synchronized
    fun arm(target: UninstallAutomationTarget, nowUptimeMs: Long, timeoutMs: Long): Boolean {
        if (target.operationId.isBlank() ||
            !PACKAGE_NAME_PATTERN.matches(target.packageName) ||
            target.appLabel.isBlank() ||
            timeoutMs !in 1..MAX_TIMEOUT_MS
        ) {
            disarm()
            return false
        }
        this.target = target
        armedAtUptimeMs = nowUptimeMs
        deadlineUptimeMs = nowUptimeMs + timeoutMs
        return true
    }

    @Synchronized
    fun activeTarget(nowUptimeMs: Long): UninstallAutomationTarget? {
        if (target == null) return null
        if (nowUptimeMs >= deadlineUptimeMs) {
            disarm()
            return null
        }
        return target
    }

    @Synchronized
    fun isEventAfterArm(operationId: String, eventTimeUptimeMs: Long): Boolean =
        target?.let { it.operationId == operationId && eventTimeUptimeMs >= armedAtUptimeMs } == true

    @Synchronized
    fun consumeAndRun(
        operationId: String,
        packageName: String,
        nowUptimeMs: Long,
        eventTimeUptimeMs: Long,
        action: () -> Unit
    ): Boolean {
        val active = activeTarget(nowUptimeMs) ?: return false
        if (active.operationId != operationId) return false
        if (eventTimeUptimeMs < armedAtUptimeMs) return false
        if (active.packageName != packageName) {
            disarm(operationId)
            return false
        }
        disarm(operationId)
        action()
        return true
    }

    @Synchronized
    fun consumeVerifiedObservation(
        target: UninstallAutomationTarget,
        observation: InstallerDialogObservation,
        nowUptimeMs: Long,
        eventTimeUptimeMs: Long,
        action: () -> Unit
    ): Boolean {
        val active = activeTarget(nowUptimeMs) ?: return false
        if (active != target || eventTimeUptimeMs < armedAtUptimeMs) return false
        if (!observation.verifies(active)) return false

        disarm(active.operationId)
        action()
        return true
    }

    @Synchronized
    fun isActive(nowUptimeMs: Long): Boolean = activeTarget(nowUptimeMs) != null

    @Synchronized
    fun disarm(operationId: String? = null) {
        if (operationId != null && target?.operationId != operationId) return
        target = null
        armedAtUptimeMs = 0L
        deadlineUptimeMs = 0L
    }

    private companion object {
        const val MAX_TIMEOUT_MS = 60_000L
        val PACKAGE_NAME_PATTERN = Regex("(?:[A-Za-z0-9_]+\\.)+[A-Za-z0-9_]+")
    }
}

internal object UninstallDialogMatcher {
    fun isUninstallConfirmation(visibleTexts: Collection<String>): Boolean =
        visibleTexts.asSequence()
            .map(::normalize)
            .any { text -> CONFIRMATION_PATTERNS.any { it.containsMatchIn(text) } }

    fun hasAmbiguousAppLabel(expectedAppLabel: String, otherAppLabels: Collection<String>): Boolean {
        val expected = normalize(expectedAppLabel)
        if (expected.isBlank()) return false
        val expectedPattern = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(expected)}(?![\\p{L}\\p{N}])")
        return otherAppLabels.any { otherLabel ->
            val other = normalize(otherLabel)
            other == expected || expectedPattern.containsMatchIn(other)
        }
    }

    fun matchesExpectedApp(
        expectedPackageName: String,
        expectedAppLabel: String,
        visibleTexts: Collection<String>
    ): Boolean {
        if (expectedPackageName.isBlank() || expectedAppLabel.isBlank()) return false

        val texts = visibleTexts.map(::normalize)
            .filter { it.isNotBlank() && !isActionText(it) }
        val packagePattern = Regex(
            "(?<![A-Za-z0-9_.])${Regex.escape(expectedPackageName)}(?![A-Za-z0-9_.])"
        )
        if (visibleTexts.any(packagePattern::containsMatchIn)) return true

        val appLabel = normalize(expectedAppLabel)
        if (texts.any { it == appLabel }) return true
        val labelPattern = Regex("(?<![\\p{L}\\p{N}])${Regex.escape(appLabel)}(?![\\p{L}\\p{N}])")
        return texts.any { text ->
            isUninstallConfirmation(listOf(text)) && labelPattern.containsMatchIn(text)
        }
    }

    fun isPositiveAction(
        buttonText: String?,
        viewId: String?,
        isVerifiedTargetUninstallDialog: Boolean = false
    ): Boolean {
        val normalizedId = viewId.orEmpty().substringAfterLast('/')
        if (normalizedId.equals("button2", ignoreCase = true)) return false

        val text = normalize(buttonText.orEmpty())
        if (isNegativeActionText(text)) return false
        return text in POSITIVE_ACTIONS ||
            (text == "ok" &&
                (normalizedId.equals("button1", ignoreCase = true) ||
                    isVerifiedTargetUninstallDialog))
    }

    private fun isActionText(text: String): Boolean =
        isPositiveAction(text, null) || isNegativeActionText(text)

    private fun isNegativeActionText(text: String): Boolean =
        text == "cancelar" ||
            text.startsWith("cancelar ") ||
            text == "cancel" ||
            text.startsWith("cancel ") ||
            text == "manter" ||
            text.startsWith("manter ") ||
            text == "keep" ||
            text.startsWith("keep ") ||
            text == "nao" ||
            text.startsWith("nao ") ||
            text == "no" ||
            text.startsWith("no ")

    private fun normalize(value: String): String =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace(ACCENT_MARKS, "")
            .lowercase()
            .replace(WHITESPACE, " ")
            .trim()

    private val ACCENT_MARKS = Regex("\\p{Mn}+")
    private val WHITESPACE = Regex("\\s+")
    private val CONFIRMATION_PATTERNS = listOf(
        Regex("\\b(?:do you want to|are you sure you want to) uninstall\\b"),
        Regex("\\buninstall this (?:app|application)\\b"),
        Regex("\\b(?:deseja|quer|tem certeza que deseja) desinstalar\\b"),
        Regex("\\bdesinstalar este (?:app|aplicativo)\\b")
    )
    private val POSITIVE_ACTIONS = setOf(
        "uninstall",
        "uninstall app",
        "uninstall application",
        "desinstalar",
        "desinstalar app",
        "desinstalar aplicativo",
        "remover",
        "remover app",
        "remover aplicativo"
    )
}
