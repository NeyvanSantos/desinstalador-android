package com.neyvan.desinstalador.ui

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

internal data class QueueOperationToken(
    val queueId: String,
    val operationId: String
)

internal data class QueueCancellationToken(val operationId: String)

internal sealed interface QueuePersistenceResult {
    data object Stale : QueuePersistenceResult
    data class Completed(val success: Boolean) : QueuePersistenceResult
}

internal class UninstallQueueOperationCoordinator {
    private val lock = Any()
    private val persistenceMutex = Mutex()

    private var activeQueueId: String? = null
    private var activeOperationId: String? = null
    private var cancellationOperationId: String? = null

    fun beginQueue(queueId: String, operationId: String = UUID.randomUUID().toString()): QueueOperationToken? =
        synchronized(lock) {
            if (activeQueueId != null || cancellationOperationId != null) return null
            QueueOperationToken(queueId, operationId).also {
                activeQueueId = queueId
                activeOperationId = operationId
            }
        }

    fun restoreQueue(queueId: String): Boolean = synchronized(lock) {
        if (activeQueueId != null || cancellationOperationId != null) return false
        activeQueueId = queueId
        true
    }

    fun beginOperation(queueId: String, operationId: String = UUID.randomUUID().toString()): QueueOperationToken? =
        synchronized(lock) {
            if (activeQueueId != queueId ||
                activeOperationId != null ||
                cancellationOperationId != null
            ) {
                return null
            }
            QueueOperationToken(queueId, operationId).also {
                activeOperationId = operationId
            }
        }

    fun isQueueCurrent(queueId: String): Boolean = synchronized(lock) {
        activeQueueId == queueId && cancellationOperationId == null
    }

    fun updateIfQueueCurrent(queueId: String, action: () -> Unit): Boolean =
        synchronized(lock) {
            if (activeQueueId != queueId || cancellationOperationId != null) return false
            action()
            true
        }

    fun isCurrent(token: QueueOperationToken): Boolean = synchronized(lock) {
        isCurrentLocked(token)
    }

    fun commitIfCurrent(token: QueueOperationToken, action: () -> Unit): Boolean =
        synchronized(lock) {
            if (!isCurrentLocked(token)) return false
            action()
            true
        }

    fun finishOperation(token: QueueOperationToken) {
        synchronized(lock) {
            if (isCurrentLocked(token)) activeOperationId = null
        }
    }

    fun finishQueue(token: QueueOperationToken) {
        synchronized(lock) {
            if (activeQueueId == token.queueId &&
                activeOperationId == token.operationId &&
                cancellationOperationId == null
            ) {
                activeQueueId = null
                activeOperationId = null
            }
        }
    }

    fun cancelQueue(
        queueId: String,
        beforeInvalidationCompletes: () -> Unit
    ): QueueCancellationToken? = synchronized(lock) {
        if (activeQueueId != queueId || cancellationOperationId != null) return null
        activeQueueId = null
        activeOperationId = null
        val operationId = UUID.randomUUID().toString()
        cancellationOperationId = operationId
        beforeInvalidationCompletes()
        QueueCancellationToken(operationId)
    }

    suspend fun persist(
        token: QueueOperationToken,
        write: suspend () -> Boolean
    ): QueuePersistenceResult = persistenceMutex.withLock {
        if (!isCurrent(token)) return@withLock QueuePersistenceResult.Stale
        val success = write()
        if (!isCurrent(token)) {
            QueuePersistenceResult.Stale
        } else {
            QueuePersistenceResult.Completed(success)
        }
    }

    suspend fun persistCancellation(
        token: QueueCancellationToken,
        write: suspend () -> Boolean
    ): QueuePersistenceResult = persistenceMutex.withLock {
        if (!isCancellationCurrent(token)) return@withLock QueuePersistenceResult.Stale
        val success = write()
        if (!isCancellationCurrent(token)) {
            QueuePersistenceResult.Stale
        } else {
            QueuePersistenceResult.Completed(success)
        }
    }

    suspend fun serializeSelectionWrite(write: suspend () -> Boolean): Boolean =
        persistenceMutex.withLock { write() }

    fun isCancellationCurrent(token: QueueCancellationToken): Boolean = synchronized(lock) {
        cancellationOperationId == token.operationId
    }

    fun commitIfCancellationCurrent(
        token: QueueCancellationToken,
        action: () -> Unit
    ): Boolean = synchronized(lock) {
        if (cancellationOperationId != token.operationId) return false
        action()
        true
    }

    fun finishCancellation(token: QueueCancellationToken) {
        synchronized(lock) {
            if (cancellationOperationId == token.operationId) cancellationOperationId = null
        }
    }

    private fun isCurrentLocked(token: QueueOperationToken): Boolean =
        activeQueueId == token.queueId &&
            activeOperationId == token.operationId &&
            cancellationOperationId == null
}
