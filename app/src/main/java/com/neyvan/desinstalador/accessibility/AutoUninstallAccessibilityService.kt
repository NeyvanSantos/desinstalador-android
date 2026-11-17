package com.neyvan.desinstalador.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.StateFlow
import java.util.ArrayDeque

class AutoUninstallAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        disarmAutomation()
        serviceAvailability.connected(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val now = SystemClock.uptimeMillis()
        if (event == null) {
            disarmAutomation()
            return
        }
        val target = automationGate.activeTarget(now) ?: return
        if (!automationGate.isEventAfterArm(target.operationId, event.eventTime)) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }

        val eventPackage = event.packageName?.toString().orEmpty()
        if (!InstallerPackages.isAllowed(eventPackage)) {
            if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                disarmAutomation(target.operationId)
            }
            return
        }

        val root = rootInActiveWindow
        val rootPackage = root?.packageName?.toString()
        val activeRoot = root?.takeIf {
            rootPackage == eventPackage &&
                InstallerPackages.isAllowed(eventPackage) &&
                it.windowId == event.windowId &&
                it.isVisibleToUser
        }
        val relevantNodes = activeRoot
            ?.let(::collectNodes)
            .orEmpty()
            .filter { node -> node.isVisibleToUser && node.windowId == activeRoot?.windowId }
        val evidence = relevantNodes.flatMap { node ->
            listOfNotNull(node.text?.toString(), node.contentDescription?.toString())
                .map { text ->
                    AccessibilityTextEvidence(
                        text = text,
                        isVisibleToUser = node.isVisibleToUser,
                        windowId = node.windowId
                    )
                }
        }
        val visibleTexts = relevantVisibleTexts(evidence, activeRoot?.windowId ?: event.windowId)
        val uninstallTextMatches = UninstallDialogMatcher.isUninstallConfirmation(visibleTexts)
        val targetMatches = UninstallDialogMatcher.matchesExpectedApp(
            target.packageName,
            target.appLabel,
            visibleTexts
        )
        val actionNodes = relevantNodes.filter { node ->
            UninstallDialogMatcher.isPositiveAction(
                buttonText = node.text?.toString() ?: node.contentDescription?.toString(),
                viewId = node.viewIdResourceName,
                isVerifiedTargetUninstallDialog = uninstallTextMatches && targetMatches
            )
        }
        val button = actionNodes.firstOrNull { node -> node.isEnabled && node.isClickable }

        automationGate.consumeVerifiedObservation(
            target = target,
            observation = InstallerDialogObservation(
                installerPackageName = eventPackage,
                windowPackageName = rootPackage,
                rootVisible = activeRoot?.isVisibleToUser == true,
                eventWindowId = event.windowId,
                rootWindowId = activeRoot?.windowId,
                visibleTexts = visibleTexts,
                hasPositiveAction = button != null
            ),
            nowUptimeMs = now,
            eventTimeUptimeMs = event.eventTime
        ) {
            button?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun collectNodes(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        pending.add(root)

        while (pending.isNotEmpty() && nodes.size < MAX_ACCESSIBILITY_NODES) {
            val node = pending.removeFirst()
            nodes += node
            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(pending::addLast)
            }
        }
        return nodes
    }

    override fun onInterrupt() {
        disarmAutomation()
        serviceAvailability.interrupted(this)
    }

    override fun onDestroy() {
        disarmAutomation()
        serviceAvailability.destroyed(this)
        super.onDestroy()
    }

    companion object {
        private const val DEFAULT_TIMEOUT_MS = 15_000L
        private const val MAX_ACCESSIBILITY_NODES = 200
        private val automationGate = UninstallAutomationGate()
        private val serviceAvailability = AccessibilityServiceAvailability<AutoUninstallAccessibilityService>()
        val isRunning: StateFlow<Boolean> = serviceAvailability.isRunning

        fun isServiceRunning(): Boolean = serviceAvailability.isRunning()

        fun armAutomation(
            operationId: String,
            expectedPackageName: String,
            expectedAppLabel: String,
            timeoutMs: Long = DEFAULT_TIMEOUT_MS
        ): Boolean = automationGate.arm(
                target = UninstallAutomationTarget(operationId, expectedPackageName, expectedAppLabel),
                nowUptimeMs = SystemClock.uptimeMillis(),
                timeoutMs = timeoutMs
            )

        fun disarmAutomation(operationId: String? = null) {
            automationGate.disarm(operationId)
        }

        fun isAutomationActive(): Boolean =
            automationGate.isActive(SystemClock.uptimeMillis())
    }
}
