package com.neyvan.desinstalador.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.neyvan.desinstalador.accessibility.AccessibilityHelper
import com.neyvan.desinstalador.accessibility.AutoUninstallAccessibilityService
import com.neyvan.desinstalador.data.AppRepository
import com.neyvan.desinstalador.data.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppSortOrder {
    NAME_ASC,
    NAME_DESC,
    SIZE_DESC,
    SIZE_ASC
}

data class UninstallerUiState(
    val apps: List<InstalledApp> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val searchQuery: String = "",
    val showSystemApps: Boolean = false,
    val sortOrder: AppSortOrder = AppSortOrder.NAME_ASC,
    val isLoading: Boolean = true,
    val isUninstalling: Boolean = false,
    val accessibilityReady: Boolean = false,
    val intentUninstallPackage: String? = null,
    val intentQueue: List<String> = emptyList(),
    val lastMessage: String? = null
) {
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

    private val _state = MutableStateFlow(UninstallerUiState())
    val state: StateFlow<UninstallerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            AutoUninstallAccessibilityService.isRunning.collect {
                checkAccessibilityStatus()
            }
        }
        checkAccessibilityStatus()
        refreshApps()
    }

    fun checkAccessibilityStatus() {
        val isEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(getApplication())
        _state.update { it.copy(accessibilityReady = isEnabled) }
    }

    fun refreshApps() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = it.apps.isEmpty()) }
            val apps = withContext(Dispatchers.IO) {
                repository.loadInstalledApps()
            }
            _state.update { current ->
                val stillInstalled = apps.map { it.packageName }.toSet()
                current.copy(
                    apps = apps,
                    selectedPackages = current.selectedPackages.filter { it in stillInstalled }.toSet(),
                    isLoading = false
                )
            }
        }
    }

    fun onSortOrderChange(order: AppSortOrder) {
        _state.update { it.copy(sortOrder = order) }
    }

    fun onSearchChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onToggleSystemApps() {
        _state.update { it.copy(showSystemApps = !it.showSystemApps) }
    }

    fun onToggleApp(packageName: String) {
        _state.update { current ->
            val selected = current.selectedPackages.toMutableSet()
            if (!selected.add(packageName)) {
                selected.remove(packageName)
            }
            current.copy(selectedPackages = selected)
        }
    }

    fun onSelectAllVisible() {
        _state.update { current ->
            val visible = current.visibleApps.map { it.packageName }.toSet()
            val allSelected = visible.isNotEmpty() && visible.all { it in current.selectedPackages }
            current.copy(
                selectedPackages = if (allSelected) {
                    current.selectedPackages - visible
                } else {
                    current.selectedPackages + visible
                }
            )
        }
    }

    fun onClearSelection() {
        _state.update { it.copy(selectedPackages = emptySet()) }
    }

    fun startUninstallQueue() {
        val queue = _state.value.selectedPackages.toList()
        if (queue.isEmpty() || _state.value.isUninstalling) return

        AutoUninstallAccessibilityService.armAutomation()
        _state.update {
            it.copy(
                isUninstalling = true,
                intentQueue = queue.drop(1),
                intentUninstallPackage = queue.first(),
                lastMessage = null
            )
        }
    }

    fun onIntentUninstallFinished(success: Boolean) {
        val finishedPackage = _state.value.intentUninstallPackage ?: return
        _state.update { current ->
            val remainingQueue = current.intentQueue
            val next = remainingQueue.firstOrNull()
            val selected = current.selectedPackages.toMutableSet()
            if (success) {
                selected.remove(finishedPackage)
            }

            if (next == null) {
                AutoUninstallAccessibilityService.disarmAutomation()
            } else {
                AutoUninstallAccessibilityService.armAutomation()
            }

            current.copy(
                selectedPackages = selected,
                intentQueue = remainingQueue.drop(1),
                intentUninstallPackage = next,
                isUninstalling = next != null,
                lastMessage = if (!success && next == null) {
                    "Desinstalação interrompida. Os apps restantes continuam selecionados."
                } else if (next == null) {
                    "Desinstalação concluída!"
                } else {
                    current.lastMessage
                }
            )
        }
        refreshApps()
    }

    fun consumeMessage() {
        _state.update { it.copy(lastMessage = null) }
    }

    override fun onCleared() {
        AutoUninstallAccessibilityService.disarmAutomation()
        super.onCleared()
    }
}
