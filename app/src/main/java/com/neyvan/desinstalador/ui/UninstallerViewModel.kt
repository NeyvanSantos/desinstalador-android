package com.neyvan.desinstalador.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neyvan.desinstalador.accessibility.AccessibilityConsentStore
import com.neyvan.desinstalador.accessibility.AccessibilityHelper
import com.neyvan.desinstalador.accessibility.AutoUninstallAccessibilityService
import com.neyvan.desinstalador.accessibility.UninstallDialogMatcher
import com.neyvan.desinstalador.accessibility.canAutomateUninstallForApp
import com.neyvan.desinstalador.accessibility.isAutomationAvailable
import com.neyvan.desinstalador.data.AppRepository
import com.neyvan.desinstalador.data.InstalledApp
import com.neyvan.desinstalador.data.PackagePresence
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class AppSortOrder {
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}

data class PendingUninstallLaunch(
    val queueOperationId: String,
    val operationId: String,
    val packageName: String,
    val dispatched: Boolean = false
)

data class UninstallerUiState(
    val apps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val showSystemApps: Boolean = false,
    val sortOrder: AppSortOrder = AppSortOrder.NAME_ASC,
    val isLoading: Boolean = true,
    val accessibilityReady: Boolean = false,
    val accessibilityConsentGranted: Boolean = false,
    val queue: UninstallQueueState = UninstallQueueState.EMPTY,
    val pendingUninstallLaunch: PendingUninstallLaunch? = null,
    val isQueueOperationPending: Boolean = false,
    val lastMessage: String? = null
) {
    val isUninstalling: Boolean
        get() = queue.isRunning || isQueueOperationPending || pendingUninstallLaunch != null

    val isAutoModeActive: Boolean
        get() = accessibilityReady

    val visibleApps: List<InstalledApp>
        get() {
            val query = searchQuery.trim().lowercase()
            val filtered = apps.filter { app ->
                (showSystemApps || !app.isSystem) &&
                    (query.isEmpty() ||
                        app.label.lowercase().contains(query) ||
                        app.packageName.lowercase().contains(query))
            }

            return when (sortOrder) {
                AppSortOrder.NAME_ASC -> filtered.sortedBy { it.label.lowercase() }
                AppSortOrder.NAME_DESC -> filtered.sortedByDescending { it.label.lowercase() }
                AppSortOrder.SIZE_DESC -> filtered.sortedWith(
                    compareByDescending<InstalledApp> { it.sizeBytes }
                        .thenBy { it.label.lowercase() }
                )
                AppSortOrder.SIZE_ASC -> filtered.sortedWith(
                    compareBy<InstalledApp> { it.sizeBytes }
                        .thenBy { it.label.lowercase() }
                )
            }
        }
}

class UninstallerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)
    private val queueStore = UninstallQueueStore(application)
    private val accessibilityConsentStore = AccessibilityConsentStore(application)
    private val queueOperations = UninstallQueueOperationCoordinator()

    private val _state = MutableStateFlow(UninstallerUiState())
    val state: StateFlow<UninstallerUiState> = _state.asStateFlow()
    private var queueOperationJob: Job? = null

    init {
        viewModelScope.launch {
            AutoUninstallAccessibilityService.isRunning.collect {
                checkAccessibilityStatus()
            }
        }
        checkAccessibilityStatus()
        loadInitialState()
    }

    fun checkAccessibilityStatus() {
        val serviceEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(getApplication())
        val consentGranted = accessibilityConsentStore.hasConsent()
        _state.update {
            it.copy(
                accessibilityReady = isAutomationAvailable(serviceEnabled, consentGranted),
                accessibilityConsentGranted = consentGranted
            )
        }
    }

    fun acceptAccessibilityDisclosure() {
        accessibilityConsentStore.accept()
        val serviceEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(getApplication())
        _state.update {
            it.copy(
                accessibilityConsentGranted = true,
                accessibilityReady = isAutomationAvailable(serviceEnabled, userConsented = true)
            )
        }
    }

    fun declineAccessibilityDisclosure() {
        accessibilityConsentStore.revoke()
        AutoUninstallAccessibilityService.disarmAutomation()
        _state.update {
            it.copy(accessibilityConsentGranted = false, accessibilityReady = false)
        }
    }

    fun onAccessibilitySettingsUnavailable() {
        _state.update {
            it.copy(lastMessage = "Não foi possível abrir as configurações de acessibilidade deste dispositivo.")
        }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.apps.isEmpty()) }
            val apps = withContext(Dispatchers.IO) {
                repository.loadInstalledApps()
            }
            var selectionChanged = false
            _state.update { current ->
                val selectable = apps.filterNot(InstalledApp::isProtected)
                    .mapTo(mutableSetOf(), InstalledApp::packageName)
                val retainedSelection = retainInstalledSelection(current.selectedPackages, selectable)
                selectionChanged = retainedSelection != current.selectedPackages
                current.copy(
                    apps = apps,
                    selectedPackages = retainedSelection,
                    isLoading = false
                )
            }
            if (selectionChanged && !_state.value.isQueueOperationPending) persistSelection()
        }
    }

    fun onSortOrderChange(order: AppSortOrder) {
        _state.update { it.copy(sortOrder = order) }
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onToggleSystemApps() {
        val current = _state.value
        if (current.isQueueOperationPending || current.queue.isRunning || current.queue.isPaused) return
        _state.update { it.copy(showSystemApps = !it.showSystemApps) }
    }

    fun onToggleApp(packageName: String) {
        var selectionChanged = false
        _state.update { current ->
            if (current.isQueueOperationPending || current.queue.isRunning || current.queue.isPaused) {
                return@update current
            }
            val selectable = current.apps.filterNot(InstalledApp::isProtected)
                .mapTo(mutableSetOf(), InstalledApp::packageName)
            val selection = togglePackageSelection(
                selectedPackages = current.selectedPackages,
                packageName = packageName,
                selectablePackages = selectable
            )
            selectionChanged = selection != current.selectedPackages
            current.copy(selectedPackages = selection)
        }
        if (selectionChanged) persistSelection()
    }

    fun onSelectAllVisible() {
        var selectionChanged = false
        _state.update { current ->
            if (current.isQueueOperationPending || current.queue.isRunning || current.queue.isPaused) {
                return@update current
            }
            val visible = current.visibleApps.mapTo(mutableSetOf(), InstalledApp::packageName)
            val selectable = current.apps.filterNot(InstalledApp::isProtected)
                .mapTo(mutableSetOf(), InstalledApp::packageName)
            val selection = toggleVisiblePackageSelection(
                selectedPackages = current.selectedPackages,
                visiblePackages = visible,
                selectablePackages = selectable
            )
            selectionChanged = selection != current.selectedPackages
            current.copy(selectedPackages = selection)
        }
        if (selectionChanged) persistSelection()
    }

    fun onClearSelection() {
        val current = _state.value
        if (current.isQueueOperationPending || current.queue.isRunning || current.queue.isPaused) return
        if (current.selectedPackages.isEmpty()) return
        _state.update { it.copy(selectedPackages = emptySet()) }
        persistSelection()
    }

    fun startUninstallQueue() {
        val current = _state.value
        if (current.pendingUninstallLaunch != null) {
            _state.update {
                it.copy(lastMessage = "Aguarde o retorno do instalador antes de iniciar outra fila.")
            }
            return
        }
        if (current.isQueueOperationPending || current.queue.isRunning || current.queue.isPaused) return
        val queuePackages = current.apps
            .filter { it.packageName in current.selectedPackages && !it.isProtected }
            .map(InstalledApp::packageName)
        if (queuePackages.isEmpty()) {
            _state.update {
                it.copy(lastMessage = "Selecione pelo menos um aplicativo que possa ser processado.")
            }
            return
        }

        val queue = UninstallQueueState.start(queuePackages)
        val token = queueOperations.beginQueue(requireNotNull(queue.operationId)) ?: return
        val selected = current.selectedPackages intersect queuePackages.toSet()
        _state.update {
            it.copy(
                queue = queue,
                selectedPackages = selected,
                pendingUninstallLaunch = null,
                isQueueOperationPending = true,
                lastMessage = null
            )
        }
        launchQueueOperation {
            persistTransition(token, queue, selected, launchCurrent = true)
        }
    }

    fun canLaunch(pending: PendingUninstallLaunch): Boolean =
        queueOperations.isQueueCurrent(pending.queueOperationId) &&
            _state.value.pendingUninstallLaunch == pending &&
            !pending.dispatched

    fun onIntentLaunchDispatched(pending: PendingUninstallLaunch) {
        queueOperations.updateIfQueueCurrent(pending.queueOperationId) {
            _state.update { current ->
                if (current.pendingUninstallLaunch == pending) {
                    current.copy(pendingUninstallLaunch = pending.copy(dispatched = true))
                } else {
                    current
                }
            }
        }
    }

    fun onUninstallLaunchFailed(pending: PendingUninstallLaunch) {
        val token = queueOperations.beginOperation(pending.queueOperationId) ?: return
        var accepted = false
        queueOperations.commitIfCurrent(token) {
            val current = _state.value
            if (current.pendingUninstallLaunch == pending &&
                current.queue.currentPackage == pending.packageName
            ) {
                accepted = true
                AutoUninstallAccessibilityService.disarmAutomation(pending.operationId)
                _state.update {
                    it.copy(
                        pendingUninstallLaunch = null,
                        isQueueOperationPending = true
                    )
                }
            }
        }
        if (!accepted) {
            queueOperations.finishOperation(token)
            return
        }
        launchQueueOperation {
            val current = _state.value
            persistTransition(
                token = token,
                queue = current.queue.pause(UninstallQueueIssue.NOT_CONFIRMED),
                selectedPackages = current.selectedPackages,
                launchCurrent = false
            )
        }
    }

    fun onIntentUninstallFinished(resultOk: Boolean) {
        val current = _state.value
        val pending = current.pendingUninstallLaunch ?: return
        if (current.queue.operationId != pending.queueOperationId) {
            _state.update {
                if (it.pendingUninstallLaunch == pending) {
                    it.copy(pendingUninstallLaunch = null)
                } else {
                    it
                }
            }
            return
        }
        if (current.queue.currentPackage != pending.packageName) {
            return
        }
        val token = queueOperations.beginOperation(pending.queueOperationId) ?: return
        var accepted = false
        queueOperations.commitIfCurrent(token) {
            val latest = _state.value
            if (latest.pendingUninstallLaunch == pending &&
                latest.queue.operationId == pending.queueOperationId &&
                latest.queue.currentPackage == pending.packageName
            ) {
                accepted = true
                AutoUninstallAccessibilityService.disarmAutomation(pending.operationId)
                _state.update {
                    it.copy(
                        pendingUninstallLaunch = null,
                        isQueueOperationPending = true
                    )
                }
            }
        }
        if (!accepted) {
            queueOperations.finishOperation(token)
            return
        }

        launchQueueOperation {
            val stillInstalled = when (awaitPackagePresence(pending.packageName)) {
                PackagePresence.NOT_INSTALLED -> false
                PackagePresence.INSTALLED -> true
                is PackagePresence.Unknown -> null
            }
            if (!queueOperations.isCurrent(token)) return@launchQueueOperation
            val latest = _state.value
            if (latest.queue.operationId != pending.queueOperationId ||
                latest.queue.currentPackage != pending.packageName
            ) {
                queueOperations.finishOperation(token)
                return@launchQueueOperation
            }
            when (classifyUninstallResult(stillInstalled, resultOk)) {
                UninstallResult.REMOVED -> {
                    val advanced = latest.queue.completeCurrent()
                    val selected = latest.selectedPackages - pending.packageName
                    val message = if (advanced.remainingPackages.isEmpty()) {
                        queueFinishedMessage(advanced)
                    } else {
                        null
                    }
                    persistTransition(
                        token = token,
                        queue = advanced,
                        selectedPackages = selected,
                        launchCurrent = advanced.isRunning,
                        message = message,
                        refreshAfterSave = advanced.remainingPackages.isEmpty()
                    )
                }
                UninstallResult.CANCELLED -> persistTransition(
                    token,
                    latest.queue.pause(UninstallQueueIssue.CANCELLED),
                    latest.selectedPackages,
                    launchCurrent = false
                )
                UninstallResult.NOT_CONFIRMED -> persistTransition(
                    token,
                    latest.queue.pause(UninstallQueueIssue.NOT_CONFIRMED),
                    latest.selectedPackages,
                    launchCurrent = false
                )
                UninstallResult.UNKNOWN -> persistTransition(
                    token,
                    latest.queue.pause(UninstallQueueIssue.UNKNOWN),
                    latest.selectedPackages,
                    launchCurrent = false
                )
            }
        }
    }

    fun retryCurrentPackage() {
        val current = _state.value
        if (current.isQueueOperationPending ||
            !current.queue.isPaused ||
            current.queue.requiresRecovery ||
            current.queue.currentPackage == null
        ) {
            return
        }
        val token = beginQueueAction(current.queue) ?: return
        if (current.apps.any { it.packageName == current.queue.currentPackage && it.isProtected }) {
            launchQueueOperation {
                persistTransition(
                    token,
                    current.queue.pause(UninstallQueueIssue.PROTECTED),
                    current.selectedPackages,
                    launchCurrent = false
                )
            }
            return
        }
        launchQueueOperation {
            persistTransition(token, current.queue.resume(), current.selectedPackages, launchCurrent = true)
        }
    }

    fun skipCurrentPackage() {
        val current = _state.value
        if (current.isQueueOperationPending || !current.queue.isPaused || current.queue.requiresRecovery) return
        val token = beginQueueAction(current.queue) ?: return
        val advanced = current.queue.skipCurrent()
        val message = if (advanced.remainingPackages.isEmpty()) queueFinishedMessage(advanced) else null
        launchQueueOperation {
            persistTransition(
                token,
                advanced,
                current.selectedPackages,
                launchCurrent = advanced.isRunning,
                message = message
            )
        }
    }

    fun resumeRecoveredQueue() {
        val current = _state.value
        val packageName = current.queue.currentPackage ?: return
        if (current.isQueueOperationPending || !current.queue.requiresRecovery || current.isLoading) return
        val token = beginQueueAction(current.queue) ?: return
        launchQueueOperation {
            when (withContext(Dispatchers.IO) { repository.isPackageInstalled(packageName) }) {
                PackagePresence.NOT_INSTALLED -> {
                    if (!queueOperations.isCurrent(token)) return@launchQueueOperation
                    val latest = _state.value
                    if (latest.queue.operationId != token.queueId ||
                        latest.queue.currentPackage != packageName
                    ) {
                        queueOperations.finishOperation(token)
                        return@launchQueueOperation
                    }
                    val advanced = latest.queue.completeCurrent()
                    val selected = latest.selectedPackages - packageName
                    val message = if (advanced.remainingPackages.isEmpty()) queueFinishedMessage(advanced) else null
                    persistTransition(
                        token,
                        advanced,
                        selected,
                        launchCurrent = advanced.isRunning,
                        message = message,
                        refreshAfterSave = advanced.remainingPackages.isEmpty()
                    )
                }
                PackagePresence.INSTALLED -> {
                    if (!queueOperations.isCurrent(token)) return@launchQueueOperation
                    val latest = _state.value
                    if (latest.queue.operationId == token.queueId &&
                        latest.queue.currentPackage == packageName &&
                        latest.queue.requiresRecovery
                    ) {
                        val protected = latest.apps.any {
                            it.packageName == packageName && it.isProtected
                        }
                        val resumed = if (protected) {
                            latest.queue.pause(UninstallQueueIssue.PROTECTED)
                        } else {
                            latest.queue.resume()
                        }
                        persistTransition(
                            token,
                            resumed,
                            latest.selectedPackages,
                            launchCurrent = resumed.isRunning
                        )
                    } else {
                        queueOperations.finishOperation(token)
                    }
                }
                is PackagePresence.Unknown -> {
                    if (!queueOperations.isCurrent(token)) return@launchQueueOperation
                    val latest = _state.value
                    if (latest.queue.operationId == token.queueId &&
                        latest.queue.currentPackage == packageName
                    ) {
                        persistTransition(
                            token,
                            latest.queue.pause(UninstallQueueIssue.UNKNOWN),
                            latest.selectedPackages,
                            launchCurrent = false
                        )
                    } else {
                        queueOperations.finishOperation(token)
                    }
                }
            }
        }
    }

    fun cancelQueue() {
        val current = _state.value
        val queueId = current.queue.operationId ?: return
        if (current.queue.remainingPackages.isEmpty()) return
        val selected = current.selectedPackages
        val cancellation = queueOperations.cancelQueue(queueId) {
            queueOperationJob?.cancel()
            AutoUninstallAccessibilityService.disarmAutomation()
            _state.update {
                it.copy(
                    queue = UninstallQueueState.EMPTY,
                    pendingUninstallLaunch = it.pendingUninstallLaunch?.takeIf { pending ->
                        pending.dispatched
                    },
                    isQueueOperationPending = true,
                    lastMessage = null
                )
            }
        } ?: return

        queueOperationJob = viewModelScope.launch {
            val result = queueOperations.persistCancellation(cancellation) {
                persistQueue(UninstallQueueState.EMPTY, selected)
            }
            if (result is QueuePersistenceResult.Stale) return@launch
            val saved = (result as QueuePersistenceResult.Completed).success
            queueOperations.commitIfCancellationCurrent(cancellation) {
                _state.update {
                    it.copy(
                        isQueueOperationPending = false,
                        lastMessage = if (saved) {
                            "Fila cancelada. Os aplicativos ainda não processados continuam selecionados."
                        } else {
                            "A fila foi desarmada, mas não foi possível salvar o cancelamento. Verifique a recuperação antes de continuar."
                        }
                    )
                }
            }
            queueOperations.finishCancellation(cancellation)
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(lastMessage = null) }
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val (apps, saved) = withContext(Dispatchers.IO) {
                repository.loadInstalledApps() to queueStore.load()
            }
            AutoUninstallAccessibilityService.disarmAutomation()
            val restoredQueue = saved?.queue ?: UninstallQueueState.EMPTY
            restoredQueue.operationId?.let(queueOperations::restoreQueue)
            _state.update { current ->
                val selectable = apps.filterNot(InstalledApp::isProtected)
                    .mapTo(mutableSetOf(), InstalledApp::packageName)
                val queue = restoredQueue.currentPackage
                    ?.takeIf { packageName -> apps.any { it.packageName == packageName && it.isProtected } }
                    ?.let { restoredQueue.pause(UninstallQueueIssue.PROTECTED) }
                    ?: restoredQueue
                current.copy(
                    apps = apps,
                    selectedPackages = retainInstalledSelection(
                        saved?.selectedPackages ?: current.selectedPackages,
                        selectable
                    ),
                    queue = queue,
                    isLoading = false,
                    lastMessage = if (saved?.wasCorrupted == true) {
                        "Os dados salvos da fila estavam inválidos. A fila foi descartada com segurança; confira a seleção."
                    } else {
                        current.lastMessage
                    }
                )
            }
        }
    }

    private fun beginQueueAction(queue: UninstallQueueState): QueueOperationToken? {
        val queueId = queue.operationId ?: return null
        val token = queueOperations.beginOperation(queueId) ?: return null
        _state.update { current ->
            if (current.queue.operationId == queueId) {
                current.copy(isQueueOperationPending = true, lastMessage = null)
            } else {
                current
            }
        }
        return token
    }

    private fun launchQueueOperation(block: suspend () -> Unit) {
        queueOperationJob = viewModelScope.launch { block() }
    }

    private suspend fun persistTransition(
        token: QueueOperationToken,
        queue: UninstallQueueState,
        selectedPackages: Set<String>,
        launchCurrent: Boolean,
        message: String? = null,
        refreshAfterSave: Boolean = false
    ) {
        val current = _state.value
        val targetPackage = queue.currentPackage.takeIf { launchCurrent && queue.isRunning }
        val protectedTarget = targetPackage?.let { packageName ->
            current.apps.any { it.packageName == packageName && it.isProtected }
        } == true
        val effectiveQueue = if (protectedTarget) {
            queue.pause(UninstallQueueIssue.PROTECTED)
        } else {
            queue
        }
        val pendingLaunch = effectiveQueue.currentPackage
            .takeIf { launchCurrent && effectiveQueue.isRunning }
            ?.let { packageName ->
                PendingUninstallLaunch(
                    queueOperationId = token.queueId,
                    operationId = token.operationId,
                    packageName = packageName
                )
            }

        when (val result = queueOperations.persist(token) {
            persistQueue(effectiveQueue, selectedPackages)
        }) {
            QueuePersistenceResult.Stale -> return
            is QueuePersistenceResult.Completed -> {
                if (!result.success) {
                    queueOperations.commitIfCurrent(token) {
                        AutoUninstallAccessibilityService.disarmAutomation()
                        val retainedQueue = _state.value.queue.takeIf {
                            it.remainingPackages.isNotEmpty()
                        } ?: effectiveQueue
                        _state.update {
                            it.copy(
                                queue = retainedQueue.pause(UninstallQueueIssue.UNKNOWN),
                                pendingUninstallLaunch = null,
                                isQueueOperationPending = false,
                                lastMessage = "Não foi possível gravar o estado local. A fila foi pausada e nenhuma nova desinstalação foi iniciada."
                            )
                        }
                    }
                    queueOperations.finishOperation(token)
                    return
                }
            }
        }

        val committed = queueOperations.commitIfCurrent(token) {
            val automationWarning = pendingLaunch?.let {
                armAutomationFor(it.packageName, it.operationId)
            }
            _state.update { latest ->
                if (latest.queue.operationId != token.queueId) {
                    latest
                } else {
                    latest.copy(
                        queue = effectiveQueue,
                        selectedPackages = selectedPackages,
                        pendingUninstallLaunch = pendingLaunch,
                        isQueueOperationPending = false,
                        lastMessage = if (protectedTarget) null else message ?: automationWarning
                    )
                }
            }
        }
        if (!committed) return
        if (effectiveQueue.remainingPackages.isEmpty()) {
            queueOperations.finishQueue(token)
        } else {
            queueOperations.finishOperation(token)
        }
        if (refreshAfterSave) refreshApps()
    }

    private suspend fun persistQueue(
        queue: UninstallQueueState,
        selectedPackages: Set<String>
    ): Boolean = try {
        withContext(Dispatchers.IO) {
            queueStore.save(queue, selectedPackages)
        }
    } catch (error: SecurityException) {
        Log.e(TAG, "Could not persist uninstall queue", error)
        false
    } catch (error: IllegalStateException) {
        Log.e(TAG, "Could not persist uninstall queue", error)
        false
    }

    private fun persistSelection() {
        viewModelScope.launch {
            val saved = queueOperations.serializeSelectionWrite {
                withContext(Dispatchers.IO) {
                    queueStore.saveSelection(_state.value.selectedPackages)
                }
            }
            if (!saved) {
                _state.update {
                    it.copy(lastMessage = "Não foi possível salvar a seleção neste dispositivo.")
                }
            }
        }
    }

    private fun armAutomationFor(packageName: String, operationId: String): String? {
        val current = _state.value
        if (!current.accessibilityReady) return null
        val app = current.apps.firstOrNull { it.packageName == packageName } ?: return null
        val otherLabels = current.apps.asSequence()
            .filter { it.packageName != packageName }
            .map(InstalledApp::label)
            .toList()
        val hasAmbiguousLabel = UninstallDialogMatcher.hasAmbiguousAppLabel(app.label, otherLabels)
        if (!canAutomateUninstallForApp(app.isSystem, hasAmbiguousLabel)) {
            if (app.isSystem) return "Apps do sistema exigem confirmação manual."
            return "Outro aplicativo tem o mesmo nome; confirme este diálogo manualmente."
        }
        return if (
            AutoUninstallAccessibilityService.armAutomation(
                operationId = operationId,
                expectedPackageName = packageName,
                expectedAppLabel = app.label
            )
        ) {
            null
        } else {
            "A confirmação automática foi desarmada; confirme este diálogo manualmente."
        }
    }

    private suspend fun awaitPackagePresence(packageName: String): PackagePresence {
        repeat(PACKAGE_CHECK_ATTEMPTS) { attempt ->
            val presence = withContext(Dispatchers.IO) {
                repository.isPackageInstalled(packageName)
            }
            when (presence) {
                PackagePresence.NOT_INSTALLED -> return presence
                is PackagePresence.Unknown -> return presence
                PackagePresence.INSTALLED -> if (attempt < PACKAGE_CHECK_ATTEMPTS - 1) {
                    delay(PACKAGE_CHECK_INTERVAL_MS)
                }
            }
        }
        return PackagePresence.INSTALLED
    }

    private fun queueFinishedMessage(queue: UninstallQueueState): String =
        "Fila finalizada: ${queue.completedCount} removido(s), ${queue.skippedCount} ignorado(s)."

    override fun onCleared() {
        queueOperationJob?.cancel(CancellationException("ViewModel cleared"))
        AutoUninstallAccessibilityService.disarmAutomation()
        super.onCleared()
    }

    private companion object {
        const val PACKAGE_CHECK_ATTEMPTS = 20
        const val PACKAGE_CHECK_INTERVAL_MS = 250L
        const val TAG = "UninstallerViewModel"
    }
}
