package com.neyvan.desinstalador.ui

import android.content.Context
import java.util.UUID

internal data class PersistedUninstallQueue(
    val queue: UninstallQueueState,
    val selectedPackages: Set<String>,
    val wasCorrupted: Boolean = false
)

internal class UninstallQueueStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun load(): PersistedUninstallQueue? {
        val decoded = decodeUninstallQueuePreferences(preferences.all)
        val result = decoded.persisted ?: return null
        if (decoded.wasCorrupted) {
            val editor = preferences.edit()
            if (result.queue.remainingPackages.isEmpty()) {
                removeQueueValues(editor)
            }
            editor.putStringSet(KEY_SELECTED_PACKAGES, result.selectedPackages)
                .commit()
        }
        return result
    }

    fun save(queue: UninstallQueueState, selectedPackages: Set<String>): Boolean {
        if (selectedPackages.any { !isValidPackageName(it) }) return false
        val editor = preferences.edit()
        if (queue.remainingPackages.isEmpty()) {
            removeQueueValues(editor)
            return editor.putStringSet(KEY_SELECTED_PACKAGES, selectedPackages.toSet()).commit()
        }
        if (queue.operationId?.let(::isValidQueueOperationId) != true ||
            queue.remainingPackages.any { PACKAGE_SEPARATOR in it || !isValidPackageName(it) } ||
            queue.remainingPackages.distinct().size != queue.remainingPackages.size ||
            queue.totalCount != queue.completedCount + queue.skippedCount + queue.remainingPackages.size ||
            queue.completedCount < 0 ||
            queue.skippedCount < 0
        ) {
            return false
        }

        return editor
            .putString(KEY_PACKAGES, queue.remainingPackages.joinToString(PACKAGE_SEPARATOR))
            .putInt(KEY_TOTAL_COUNT, queue.totalCount)
            .putInt(KEY_COMPLETED_COUNT, queue.completedCount)
            .putInt(KEY_SKIPPED_COUNT, queue.skippedCount)
            .putString(KEY_OPERATION_ID, queue.operationId)
            .putStringSet(KEY_SELECTED_PACKAGES, selectedPackages.toSet())
            .commit()
    }

    fun saveSelection(selectedPackages: Set<String>): Boolean {
        if (selectedPackages.any { !isValidPackageName(it) }) return false
        return preferences.edit()
            .putStringSet(KEY_SELECTED_PACKAGES, selectedPackages.toSet())
            .commit()
    }

    private fun removeQueueValues(editor: android.content.SharedPreferences.Editor) {
        editor.remove(KEY_PACKAGES)
            .remove(KEY_TOTAL_COUNT)
            .remove(KEY_COMPLETED_COUNT)
            .remove(KEY_SKIPPED_COUNT)
            .remove(KEY_OPERATION_ID)
    }

    companion object {
        const val PREFERENCES_NAME = "uninstall_queue"
        const val KEY_PACKAGES = "remaining_packages"
        const val KEY_TOTAL_COUNT = "total_count"
        const val KEY_COMPLETED_COUNT = "completed_count"
        const val KEY_SKIPPED_COUNT = "skipped_count"
        const val KEY_SELECTED_PACKAGES = "selected_packages"
        const val KEY_OPERATION_ID = "operation_id"
        const val PACKAGE_SEPARATOR = "\n"
    }
}

internal data class DecodedUninstallQueue(
    val persisted: PersistedUninstallQueue?,
    val wasCorrupted: Boolean
)

internal fun decodeUninstallQueuePreferences(values: Map<String, *>): DecodedUninstallQueue {
    var corrupted = false
    val selectedValue = values[UninstallQueueStore.KEY_SELECTED_PACKAGES]
    val selectedPackages = when (selectedValue) {
        null -> emptySet()
        is Set<*> -> selectedValue.mapNotNull { value ->
            (value as? String)?.takeIf(::isValidPackageName).also {
                if (it == null) corrupted = true
            }
        }.toSet()
        else -> {
            corrupted = true
            emptySet()
        }
    }

    val rawPackages = values[UninstallQueueStore.KEY_PACKAGES]
    if (rawPackages == null) {
        val persisted = if (selectedPackages.isEmpty() && !corrupted) {
            null
        } else {
            PersistedUninstallQueue(UninstallQueueState.EMPTY, selectedPackages, corrupted)
        }
        return DecodedUninstallQueue(persisted, corrupted)
    }
    if (rawPackages !is String) {
        return DecodedUninstallQueue(
            PersistedUninstallQueue(UninstallQueueState.EMPTY, selectedPackages, true),
            true
        )
    }

    val packages = rawPackages.split(UninstallQueueStore.PACKAGE_SEPARATOR)
    val malformed = packages.isEmpty() ||
        packages.any { !isValidPackageName(it) } ||
        packages.distinct().size != packages.size
    if (malformed) {
        return DecodedUninstallQueue(
            PersistedUninstallQueue(UninstallQueueState.EMPTY, selectedPackages, true),
            true
        )
    }

    fun integerValue(key: String, fallback: Int): Int? {
        val value = values[key] ?: return fallback
        return (value as? Int).also { if (it == null) corrupted = true }
    }

    val totalCount = integerValue(UninstallQueueStore.KEY_TOTAL_COUNT, packages.size)
    val completedCount = integerValue(UninstallQueueStore.KEY_COMPLETED_COUNT, 0)
    val skippedCount = integerValue(UninstallQueueStore.KEY_SKIPPED_COUNT, 0)
    val storedOperationId = values[UninstallQueueStore.KEY_OPERATION_ID]
    val operationId = when (storedOperationId) {
        null -> UUID.randomUUID().toString()
        is String -> storedOperationId.takeIf(::isValidQueueOperationId)
            ?: run {
                corrupted = true
                null
            }
        else -> {
            corrupted = true
            null
        }
    }
    val countsValid = totalCount != null &&
        completedCount != null &&
        skippedCount != null &&
        completedCount >= 0 &&
        skippedCount >= 0 &&
        totalCount == completedCount + skippedCount + packages.size
    if (!countsValid || operationId == null) corrupted = true
    if (corrupted) {
        return DecodedUninstallQueue(
            PersistedUninstallQueue(UninstallQueueState.EMPTY, selectedPackages, true),
            true
        )
    }

    val queue = UninstallQueueState(
        remainingPackages = packages,
        totalCount = requireNotNull(totalCount),
        completedCount = requireNotNull(completedCount),
        skippedCount = requireNotNull(skippedCount),
        operationId = operationId
    ).recoverAfterProcessDeath()
    val persisted = PersistedUninstallQueue(queue, selectedPackages)
    return DecodedUninstallQueue(persisted, false)
}
