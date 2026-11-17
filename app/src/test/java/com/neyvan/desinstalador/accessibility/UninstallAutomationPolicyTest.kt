package com.neyvan.desinstalador.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UninstallAutomationPolicyTest {

    @Test
    fun `dialog identifies the selected app without accepting a package prefix`() {
        assertTrue(
            UninstallDialogMatcher.isUninstallConfirmation(
                listOf("Do you want to uninstall Notes?")
            )
        )
        assertTrue(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = listOf("Do you want to uninstall Notes?", "Cancel", "Uninstall")
            )
        )
        assertTrue(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = listOf("Package: com.example.notes")
            )
        )
        assertFalse(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = listOf(
                    "com.example.notesplus",
                    "Do you want to uninstall NotesPlus?",
                    "Uninstall"
                )
            )
        )
        assertFalse(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = listOf("Do you want to uninstall Calendar?", "Cancel", "Uninstall")
            )
        )
    }

    @Test
    fun `generic text is not an app identity or an uninstall confirmation`() {
        assertFalse(UninstallDialogMatcher.isUninstallConfirmation(listOf("Uninstall")))
        assertFalse(UninstallDialogMatcher.isUninstallConfirmation(listOf("App info")))
        assertFalse(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = listOf("Uninstall", "Cancel", "App info")
            )
        )
    }

    @Test
    fun `invisible nodes and nodes from another window are excluded`() {
        val visibleTexts = relevantVisibleTexts(
            listOf(
                AccessibilityTextEvidence("com.example.notes", false, 1),
                AccessibilityTextEvidence("com.example.notesplus", true, 2),
                AccessibilityTextEvidence("Do you want to uninstall this app?", true, 1)
            ),
            expectedWindowId = 1
        )

        assertEquals(listOf("Do you want to uninstall this app?"), visibleTexts)
        assertFalse(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.example.notes",
                expectedAppLabel = "Notes",
                visibleTexts = visibleTexts
            )
        )
    }

    @Test
    fun `ambiguous app labels disable automation`() {
        assertTrue(
            UninstallDialogMatcher.hasAmbiguousAppLabel(
                expectedAppLabel = "Calculator",
                otherAppLabels = listOf("  Cálculator  ")
            )
        )
        assertFalse(
            UninstallDialogMatcher.hasAmbiguousAppLabel(
                expectedAppLabel = "Calculator",
                otherAppLabels = listOf("Calendar")
            )
        )
        assertTrue(
            UninstallDialogMatcher.hasAmbiguousAppLabel(
                expectedAppLabel = "Notes",
                otherAppLabels = listOf("Notes Plus")
            )
        )
    }

    @Test
    fun `only exact uninstall actions are positive`() {
        assertTrue(UninstallDialogMatcher.isPositiveAction("Desinstalar", "com.android:id/action"))
        assertTrue(UninstallDialogMatcher.isPositiveAction("Uninstall app", null))
        assertFalse(UninstallDialogMatcher.isPositiveAction("Uninstall updates", null))
        assertFalse(UninstallDialogMatcher.isUninstallConfirmation(listOf("Uninstall updates")))
        assertFalse(UninstallDialogMatcher.isPositiveAction(null, "android:id/button1"))
        assertFalse(UninstallDialogMatcher.isPositiveAction("Cancelar", "android:id/button1"))
        assertFalse(UninstallDialogMatcher.isPositiveAction("Uninstall", "android:id/button2"))
        assertFalse(UninstallDialogMatcher.isPositiveAction("Continue", null))
        assertTrue(UninstallDialogMatcher.isPositiveAction("OK", "android:id/button1"))
        assertFalse(UninstallDialogMatcher.isPositiveAction("OK", "android:id/button2"))
        assertFalse(UninstallDialogMatcher.isPositiveAction("OK", null))
        assertFalse(
            UninstallDialogMatcher.isPositiveAction(
                "Uninstall updates",
                null,
                isVerifiedTargetUninstallDialog = true
            )
        )
        assertFalse(
            UninstallDialogMatcher.isPositiveAction(
                "OK",
                "android:id/button2",
                isVerifiedTargetUninstallDialog = true
            )
        )
        assertFalse(
            UninstallDialogMatcher.isPositiveAction(
                "OK",
                null,
                isVerifiedTargetUninstallDialog = false
            )
        )
    }

    @Test
    fun `generic OK is positive only in a verified target uninstall dialog`() {
        val dialogTexts = listOf(
            "DesinstalaJá teste temporário",
            "Do you want to uninstall this app?",
            "Cancel",
            "OK"
        )
        val target = UninstallAutomationTarget(
            operationId = "op-1",
            packageName = "com.neyvan.desinstalaja.validationtarget",
            appLabel = "DesinstalaJá teste temporário"
        )

        assertTrue(UninstallDialogMatcher.isUninstallConfirmation(dialogTexts))
        assertTrue(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = target.packageName,
                expectedAppLabel = target.appLabel,
                visibleTexts = dialogTexts
            )
        )
        assertTrue(
            UninstallDialogMatcher.isPositiveAction(
                "OK",
                null,
                isVerifiedTargetUninstallDialog =
                    UninstallDialogMatcher.isUninstallConfirmation(dialogTexts) &&
                        UninstallDialogMatcher.matchesExpectedApp(
                            expectedPackageName = target.packageName,
                            expectedAppLabel = target.appLabel,
                            visibleTexts = dialogTexts
                        )
            )
        )
        assertFalse(UninstallDialogMatcher.isUninstallConfirmation(listOf("OK")))
        assertFalse(
            UninstallDialogMatcher.matchesExpectedApp(
                expectedPackageName = "com.neyvan.desinstalaja.validationtarget",
                expectedAppLabel = target.appLabel,
                visibleTexts = listOf("OK")
            )
        )

        val gate = UninstallAutomationGate()
        var clicked = 0
        assertTrue(gate.arm(target, nowUptimeMs = 100L, timeoutMs = 1_000L))
        assertTrue(
            gate.consumeVerifiedObservation(
                target = target,
                observation = InstallerDialogObservation(
                    installerPackageName = "com.android.packageinstaller",
                    windowPackageName = "com.android.packageinstaller",
                    rootVisible = true,
                    eventWindowId = 1,
                    rootWindowId = 1,
                    visibleTexts = dialogTexts,
                    hasPositiveAction = true
                ),
                nowUptimeMs = 200L,
                eventTimeUptimeMs = 200L
            ) {
                clicked++
            }
        )
        assertEquals(1, clicked)
    }

    @Test
    fun `only known Android installers are allowed`() {
        assertTrue(InstallerPackages.isAllowed("com.android.packageinstaller"))
        assertTrue(InstallerPackages.isAllowed("com.samsung.android.packageinstaller"))
        assertFalse(InstallerPackages.isAllowed("com.example.fakeinstaller"))
        assertFalse(InstallerPackages.isAllowed("com.android.settings"))
    }

    @Test
    fun `automation requires both system service and explicit app consent`() {
        assertFalse(isAutomationAvailable(serviceEnabled = true, userConsented = false))
        assertFalse(isAutomationAvailable(serviceEnabled = false, userConsented = true))
        assertTrue(isAutomationAvailable(serviceEnabled = true, userConsented = true))
    }

    @Test
    fun `only user apps with unambiguous labels can be automated`() {
        assertTrue(canAutomateUninstallForApp(isSystemApp = false, hasAmbiguousAppLabel = false))
        assertFalse(canAutomateUninstallForApp(isSystemApp = true, hasAmbiguousAppLabel = false))
        assertFalse(canAutomateUninstallForApp(isSystemApp = false, hasAmbiguousAppLabel = true))
    }

    @Test
    fun `gate is single use and bound to the operation, package and event time`() {
        val gate = UninstallAutomationGate()
        val target = UninstallAutomationTarget("op-1", "com.example.notes", "Notes")
        var clicked = 0

        assertTrue(gate.arm(target, nowUptimeMs = 100L, timeoutMs = 1_000L))
        assertFalse(
            gate.consumeAndRun("op-old", target.packageName, 500L, 500L) { clicked++ }
        )
        assertFalse(
            gate.consumeAndRun(target.operationId, target.packageName, 500L, 99L) { clicked++ }
        )
        assertTrue(
            gate.consumeAndRun(target.operationId, target.packageName, 500L, 500L) { clicked++ }
        )
        assertFalse(
            gate.consumeAndRun(target.operationId, target.packageName, 501L, 501L) { clicked++ }
        )
        assertEquals(1, clicked)
    }

    @Test
    fun `transient installer observation keeps gate armed until target dialog is verified`() {
        val gate = UninstallAutomationGate()
        val target = UninstallAutomationTarget("op-1", "com.example.notes", "Notes")
        var clicked = 0

        assertTrue(gate.arm(target, nowUptimeMs = 100L, timeoutMs = 1_000L))

        val transientObservation = InstallerDialogObservation(
            installerPackageName = "com.android.packageinstaller",
            windowPackageName = "com.android.packageinstaller",
            rootVisible = true,
            eventWindowId = 1,
            rootWindowId = 1,
            visibleTexts = listOf("Package installer"),
            hasPositiveAction = false
        )
        assertFalse(
            gate.consumeVerifiedObservation(
                target,
                transientObservation,
                nowUptimeMs = 200L,
                eventTimeUptimeMs = 200L
            ) { clicked++ }
        )
        assertEquals(target, gate.activeTarget(200L))
        assertEquals(0, clicked)

        val verifiedObservation = transientObservation.copy(
            visibleTexts = listOf(
                "Do you want to uninstall Notes?",
                "Notes",
                "Cancel",
                "Uninstall"
            ),
            hasPositiveAction = true
        )
        assertTrue(
            gate.consumeVerifiedObservation(
                target,
                verifiedObservation,
                nowUptimeMs = 300L,
                eventTimeUptimeMs = 300L
            ) { clicked++ }
        )
        assertNull(gate.activeTarget(300L))
        assertEquals(1, clicked)
    }

    @Test
    fun `stale disarm cannot cancel a newer operation and expired gate is unavailable`() {
        val gate = UninstallAutomationGate()
        val oldTarget = UninstallAutomationTarget("op-old", "com.example.old", "Old")
        val newTarget = UninstallAutomationTarget("op-new", "com.example.new", "New")

        assertTrue(gate.arm(oldTarget, nowUptimeMs = 100L, timeoutMs = 1_000L))
        assertTrue(gate.arm(newTarget, nowUptimeMs = 200L, timeoutMs = 1_000L))
        gate.disarm(oldTarget.operationId)
        assertEquals(newTarget, gate.activeTarget(500L))
        assertNull(gate.activeTarget(1_200L))
    }

    @Test
    fun `service interruption keeps a bound instance available until destruction`() {
        val availability = AccessibilityServiceAvailability<Any>()
        val oldInstance = Any()
        val currentInstance = Any()

        availability.connected(oldInstance)
        availability.connected(currentInstance)
        availability.interrupted(oldInstance)
        assertTrue(availability.isRunning())

        availability.interrupted(currentInstance)
        assertTrue(availability.isRunning())
        availability.destroyed(currentInstance)
        assertFalse(availability.isRunning())
    }
}
