package com.neyvan.desinstalador.ui

import java.util.UUID

enum class UninstallQueueIssue {
    CANCELLED,
    NOT_CONFIRMED,
    UNKNOWN,
    PROTECTED
}

internal enum class UninstallResult {
    REMOVED,
    CANCELLED,
    NOT_CONFIRMED,
    UNKNOWN
}

internal fun classifyUninstallResult(
    packageStillInstalled: Boolean?,
    activityResultOk: Boolean
): UninstallResult = when (packageStillInstalled) {
    false -> UninstallResult.REMOVED
    null -> UninstallResult.UNKNOWN
    true -> if (activityResultOk) UninstallResult.NOT_CONFIRMED else UninstallResult.CANCELLED
}

internal fun isValidPackageName(packageName: String): Boolean =
    PACKAGE_NAME_PATTERN.matches(packageName)

data class UninstallQueueState(
    val remainingPackages: List<String> = emptyList(),
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val skippedCount: Int = 0,
    val operationId: String? = null,
    val isRunning: Boolean = false,
    val isPaused: Boolean = false,
    val requiresRecovery: Boolean = false,
    val issue: UninstallQueueIssue? = null
) {
    val currentPackage: String?
        get() = remainingPackages.firstOrNull()

    val remainingCount: Int
        get() = remainingPackages.size

    val progress: Float
        get() = if (totalCount == 0) 0f else (completedCount.toFloat() / totalCount).coerceIn(0f, 1f)

    fun completeCurrent(): UninstallQueueState {
        if (remainingPackages.isEmpty()) return this
        return copy(
            remainingPackages = remainingPackages.drop(1),
            completedCount = completedCount + 1,
            isRunning = remainingPackages.size > 1,
            isPaused = false,
            requiresRecovery = false,
            issue = null
        )
    }

    fun skipCurrent(): UninstallQueueState {
        if (remainingPackages.isEmpty()) return this
        return copy(
            remainingPackages = remainingPackages.drop(1),
            skippedCount = skippedCount + 1,
            isRunning = remainingPackages.size > 1,
            isPaused = false,
            requiresRecovery = false,
            issue = null
        )
    }

    fun pause(issue: UninstallQueueIssue): UninstallQueueState =
        copy(isRunning = false, isPaused = true, requiresRecovery = false, issue = issue)

    fun recoverAfterProcessDeath(): UninstallQueueState =
        copy(isRunning = false, isPaused = true, requiresRecovery = true, issue = null)

    fun resume(): UninstallQueueState =
        copy(isRunning = true, isPaused = false, requiresRecovery = false, issue = null)

    fun cancel(): UninstallQueueState = EMPTY

    companion object {
        val EMPTY = UninstallQueueState()

        fun start(
            packages: List<String>,
            operationId: String = UUID.randomUUID().toString()
        ): UninstallQueueState {
            require(packages.none(String::isBlank)) { "Queue package names must not be blank" }
            require(packages.all(::isValidPackageName)) { "Queue contains an invalid package name" }
            require(isValidQueueOperationId(operationId)) { "Queue operation ID is invalid" }
            val distinctPackages = packages.distinct()
            if (distinctPackages.isEmpty()) return EMPTY
            return UninstallQueueState(
                remainingPackages = distinctPackages,
                totalCount = distinctPackages.size,
                operationId = operationId,
                isRunning = true
            )
        }
    }
}

internal fun isValidQueueOperationId(operationId: String): Boolean =
    QUEUE_OPERATION_ID_PATTERN.matches(operationId)

internal fun togglePackageSelection(
    selectedPackages: Set<String>,
    packageName: String,
    selectablePackages: Set<String>
): Set<String> {
    if (packageName !in selectablePackages) return selectedPackages
    return if (packageName in selectedPackages) selectedPackages - packageName else selectedPackages + packageName
}

internal fun toggleVisiblePackageSelection(
    selectedPackages: Set<String>,
    visiblePackages: Set<String>,
    selectablePackages: Set<String>
): Set<String> {
    val selectableVisible = visiblePackages intersect selectablePackages
    if (selectableVisible.isEmpty()) return selectedPackages
    return if (selectableVisible.all { it in selectedPackages }) {
        selectedPackages - selectableVisible
    } else {
        selectedPackages + selectableVisible
    }
}

internal fun retainInstalledSelection(
    selectedPackages: Set<String>,
    installedPackages: Set<String>
): Set<String> = selectedPackages intersect installedPackages

private val PACKAGE_NAME_PATTERN = Regex("(?:[A-Za-z0-9_]+\\.)+[A-Za-z0-9_]+")
private val QUEUE_OPERATION_ID_PATTERN =
    Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
