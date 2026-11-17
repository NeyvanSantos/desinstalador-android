package com.neyvan.desinstalador.ui

import com.neyvan.desinstalador.data.InstalledApp
import com.neyvan.desinstalador.data.InstalledAppCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UninstallQueueStateTest {

    @Test
    fun `queue starts with unique packages and reports progress`() {
        val queue = UninstallQueueState.start(
            listOf("com.example.first", "com.example.second", "com.example.first")
        )

        assertEquals(listOf("com.example.first", "com.example.second"), queue.remainingPackages)
        assertEquals(2, queue.totalCount)
        assertEquals(0f, queue.progress, 0f)

        val next = queue.completeCurrent()
        assertEquals("com.example.second", next.currentPackage)
        assertEquals(1, next.completedCount)
        assertEquals(0.5f, next.progress, 0f)
        assertTrue(next.isRunning)
    }

    @Test
    fun `queue rejects malformed package names`() {
        assertThrows(IllegalArgumentException::class.java) {
            UninstallQueueState.start(listOf("not a package"))
        }
    }

    @Test
    fun `uncertain result pauses until retry or skip decision`() {
        val queue = UninstallQueueState.start(listOf("com.example.first", "com.example.second"))
        val paused = queue.pause(UninstallQueueIssue.UNKNOWN)

        assertTrue(paused.isPaused)
        assertFalse(paused.isRunning)
        assertEquals("com.example.first", paused.currentPackage)
        assertEquals("com.example.first", paused.resume().currentPackage)
        assertEquals("com.example.second", paused.skipCurrent().currentPackage)
        assertEquals(1, paused.skipCurrent().skippedCount)
    }

    @Test
    fun `process recovery is paused and cancellation clears only queue state`() {
        val recovered = UninstallQueueState.start(listOf("com.example.first"))
            .recoverAfterProcessDeath()
        val selected = setOf("com.example.first")

        assertTrue(recovered.requiresRecovery)
        assertFalse(recovered.isRunning)
        assertEquals(UninstallQueueState.EMPTY, recovered.cancel())
        assertEquals(setOf("com.example.first"), selected)
    }

    @Test
    fun `result is based on actual package presence rather than activity result alone`() {
        assertEquals(UninstallResult.REMOVED, classifyUninstallResult(false, activityResultOk = false))
        assertEquals(UninstallResult.NOT_CONFIRMED, classifyUninstallResult(true, activityResultOk = true))
        assertEquals(UninstallResult.CANCELLED, classifyUninstallResult(true, activityResultOk = false))
        assertEquals(UninstallResult.UNKNOWN, classifyUninstallResult(null, activityResultOk = true))
    }

    @Test
    fun `selection only accepts selectable visible packages and drops missing packages`() {
        val selectable = setOf("com.example.first", "com.example.second")
        val selected = togglePackageSelection(emptySet(), "com.example.first", selectable)

        assertEquals(setOf("com.example.first"), selected)
        assertEquals(
            setOf("com.example.first", "com.example.second"),
            toggleVisiblePackageSelection(selected, setOf("com.example.second"), selectable)
        )
        assertEquals(selected, togglePackageSelection(selected, "com.android.settings", selectable))
        assertEquals(setOf("com.example.first"), retainInstalledSelection(selected, selectable))
    }

    @Test
    fun `app search and ordering respect system visibility and size`() {
        val apps = listOf(
            InstalledApp("com.example.zulu", "Zulu", InstalledAppCategory.USER_APP, sizeBytes = 20),
            InstalledApp("com.example.alpha", "Alpha", InstalledAppCategory.USER_APP, sizeBytes = 50),
            InstalledApp("com.example.system", "System Tool", InstalledAppCategory.SYSTEM_APP, sizeBytes = 10)
        )

        val defaultState = UninstallerUiState(apps = apps, isLoading = false)
        assertEquals(listOf("com.example.alpha", "com.example.zulu"), defaultState.visibleApps.map { it.packageName })
        assertEquals(
            listOf("com.example.zulu"),
            defaultState.copy(searchQuery = "zulu").visibleApps.map { it.packageName }
        )
        assertEquals(
            listOf("com.example.system", "com.example.zulu", "com.example.alpha"),
            defaultState.copy(showSystemApps = true, sortOrder = AppSortOrder.SIZE_ASC)
                .visibleApps.map { it.packageName }
        )
    }
}
