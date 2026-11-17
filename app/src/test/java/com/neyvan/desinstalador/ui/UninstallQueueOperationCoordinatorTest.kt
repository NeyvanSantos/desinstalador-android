package com.neyvan.desinstalador.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UninstallQueueOperationCoordinatorTest {

    @Test
    fun `cancellation invalidates an in flight write and clears it before new operations`() = runBlocking {
        val coordinator = UninstallQueueOperationCoordinator()
        val token = requireNotNull(coordinator.beginQueue("queue-1", "start-1"))
        val writeStarted = CompletableDeferred<Unit>()
        val allowOldWrite = CompletableDeferred<Unit>()
        var persistedQueue: String? = "previous"
        var published = false

        val oldWrite = async(Dispatchers.Default) {
            coordinator.persist(token) {
                writeStarted.complete(Unit)
                allowOldWrite.await()
                persistedQueue = token.queueId
                true
            }
        }
        writeStarted.await()
        val cancellation = requireNotNull(coordinator.cancelQueue(token.queueId) {})
        val cancelWrite = async(Dispatchers.Default) {
            coordinator.persistCancellation(cancellation) {
                persistedQueue = null
                true
            }
        }

        assertNull(coordinator.beginQueue("queue-2", "start-2"))
        allowOldWrite.complete(Unit)
        assertEquals(QueuePersistenceResult.Stale, oldWrite.await())
        assertEquals(QueuePersistenceResult.Completed(true), cancelWrite.await())
        assertNull(persistedQueue)
        assertFalse(coordinator.commitIfCurrent(token) { published = true })
        assertFalse(published)

        coordinator.finishCancellation(cancellation)
        assertTrue(coordinator.beginQueue("queue-2", "start-2") != null)
    }

    @Test
    fun `double start and concurrent retry skip or recovery allow only one operation`() = runBlocking {
        val coordinator = UninstallQueueOperationCoordinator()
        val first = requireNotNull(coordinator.beginQueue("queue-1", "start-1"))
        assertNull(coordinator.beginQueue("queue-2", "start-2"))
        coordinator.finishOperation(first)

        val actions = listOf(
            async(Dispatchers.Default) { coordinator.beginOperation("queue-1", "retry") },
            async(Dispatchers.Default) { coordinator.beginOperation("queue-1", "skip") }
        ).awaitAll()
        assertEquals(1, actions.count { it != null })

        val activeAction = requireNotNull(actions.firstOrNull { it != null })
        coordinator.finishOperation(activeAction)
        val recoveries = listOf(
            async(Dispatchers.Default) { coordinator.beginOperation("queue-1", "recover-1") },
            async(Dispatchers.Default) { coordinator.beginOperation("queue-1", "recover-2") }
        ).awaitAll()
        assertEquals(1, recoveries.count { it != null })
    }

    @Test
    fun `canceled operation cannot rearm automation`() {
        val coordinator = UninstallQueueOperationCoordinator()
        val token = requireNotNull(coordinator.beginQueue("queue-1", "operation-1"))
        var armed = false

        coordinator.cancelQueue(token.queueId) {}
        assertFalse(coordinator.commitIfCurrent(token) { armed = true })
        assertFalse(armed)
    }
}
