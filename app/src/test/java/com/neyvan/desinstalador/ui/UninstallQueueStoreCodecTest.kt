package com.neyvan.desinstalador.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UninstallQueueStoreCodecTest {

    @Test
    fun `valid queue is restored paused and selection survives without an active queue`() {
        val operationId = "42a3c6b7-8772-4d40-98da-3f2a5847432d"
        val values = mapOf(
            UninstallQueueStore.KEY_PACKAGES to "com.example.first\ncom.example.second",
            UninstallQueueStore.KEY_TOTAL_COUNT to 3,
            UninstallQueueStore.KEY_COMPLETED_COUNT to 1,
            UninstallQueueStore.KEY_SKIPPED_COUNT to 0,
            UninstallQueueStore.KEY_OPERATION_ID to operationId,
            UninstallQueueStore.KEY_SELECTED_PACKAGES to setOf("com.example.second")
        )

        val restored = requireNotNull(decodeUninstallQueuePreferences(values).persisted)
        assertEquals(listOf("com.example.first", "com.example.second"), restored.queue.remainingPackages)
        assertEquals(operationId, restored.queue.operationId)
        assertTrue(restored.queue.requiresRecovery)
        assertEquals(setOf("com.example.second"), restored.selectedPackages)

        val selectionOnly = requireNotNull(
            decodeUninstallQueuePreferences(
                mapOf(UninstallQueueStore.KEY_SELECTED_PACKAGES to setOf("com.example.second"))
            ).persisted
        )
        assertEquals(UninstallQueueState.EMPTY, selectionOnly.queue)
        assertEquals(setOf("com.example.second"), selectionOnly.selectedPackages)
    }

    @Test
    fun `corrupt queue values are discarded without losing valid selection`() {
        val decoded = decodeUninstallQueuePreferences(
            mapOf(
                UninstallQueueStore.KEY_PACKAGES to "com.example.notes\nnot a package",
                UninstallQueueStore.KEY_TOTAL_COUNT to "invalid",
                UninstallQueueStore.KEY_SELECTED_PACKAGES to setOf("com.example.notes")
            )
        )

        assertTrue(decoded.wasCorrupted)
        assertEquals(UninstallQueueState.EMPTY, requireNotNull(decoded.persisted).queue)
        assertEquals(setOf("com.example.notes"), requireNotNull(decoded.persisted).selectedPackages)
    }

    @Test
    fun `legacy queue without operation id gets a new recovery id`() {
        val decoded = decodeUninstallQueuePreferences(
            mapOf(
                UninstallQueueStore.KEY_PACKAGES to "com.example.notes",
                UninstallQueueStore.KEY_TOTAL_COUNT to 1
            )
        )

        val queue = requireNotNull(decoded.persisted).queue
        assertFalse(decoded.wasCorrupted)
        assertTrue(isValidQueueOperationId(requireNotNull(queue.operationId)))
        assertTrue(queue.requiresRecovery)
    }
}
