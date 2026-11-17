package com.neyvan.desinstalador.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.neyvan.desinstalador.accessibility.AccessibilityHelper
import com.neyvan.desinstalador.data.InstalledApp
import com.neyvan.desinstalador.ui.theme.OutlineBorder
import com.neyvan.desinstalador.ui.theme.PanelElevated

@Composable
fun UninstallerRoute(viewModel: UninstallerViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    val uninstallLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.onIntentUninstallFinished(result.resultCode == android.app.Activity.RESULT_OK)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (!state.isUninstalling) {
            viewModel.refreshApps()
            viewModel.checkAccessibilityStatus()
        }
    }

    LaunchedEffect(state.intentUninstallPackage) {
        val packageName = state.intentUninstallPackage ?: return@LaunchedEffect
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            putExtra(Intent.EXTRA_RETURN_RESULT, true)
        }
        uninstallLauncher.launch(intent)
    }

    LaunchedEffect(state.lastMessage) {
        val message = state.lastMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.consumeMessage()
    }

    if (showConfirmDialog) {
        val count = state.selectedPackages.size
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Desinstalar $count app${if (count == 1) "" else "s"}?") },
            text = {
                Text(
                    if (state.accessibilityReady) {
                        "O app vai confirmar automaticamente cada desinstalação na tela com segurança."
                    } else {
                        "O modo automático está desligado. O Android vai pedir confirmação em cada app."
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.startUninstallQueue()
                    }
                ) {
                    Text("Desinstalar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    UninstallerScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onSearchChange = viewModel::onSearchChange,
        onToggleSystemApps = viewModel::onToggleSystemApps,
        onSortOrderChange = viewModel::onSortOrderChange,
        onToggleApp = viewModel::onToggleApp,
        onSelectAllVisible = viewModel::onSelectAllVisible,
        onClearSelection = viewModel::onClearSelection,
        onEnableFastMode = {
            AccessibilityHelper.openAccessibilitySettings(context)
        },
        onUninstallClick = { showConfirmDialog = true }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UninstallerScreen(
    state: UninstallerUiState,
    snackbarHostState: SnackbarHostState,
    onSearchChange: (String) -> Unit,
    onToggleSystemApps: () -> Unit,
    onSortOrderChange: (AppSortOrder) -> Unit,
    onToggleApp: (String) -> Unit,
    onSelectAllVisible: () -> Unit,
    onClearSelection: () -> Unit,
    onEnableFastMode: () -> Unit,
    onUninstallClick: () -> Unit
) {
    val visible = state.visibleApps
    val selectedCount = state.selectedPackages.size
    val allVisibleSelected = visible.isNotEmpty() && visible.all { it.packageName in state.selectedPackages }
    val filterScrollState = rememberScrollState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = selectedCount > 0,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                UninstallBar(
                    count = selectedCount,
                    uninstalling = state.isUninstalling,
                    onClear = onClearSelection,
                    onUninstall = onUninstallClick
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "DesinstalaJá",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Selecione múltiplos apps para remoção rápida e em lote",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Banner de Status da Automação
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                SilentStatusBanner(
                    isAuto = state.isAutoModeActive,
                    onEnable = onEnableFastMode
                )
            }

            // Campo de Busca com altura e área de toque acessível (56dp)
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = {
                        Text(
                            "Buscar por nome ou pacote...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Limpar busca",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Barra de Filtros com Scroll Horizontal (não quebra em telas menores)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(filterScrollState)
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.showSystemApps,
                    onClick = { if (state.showSystemApps) onToggleSystemApps() },
                    label = { Text("Instalados") },
                    colors = filterColors()
                )
                FilterChip(
                    selected = state.showSystemApps,
                    onClick = { if (!state.showSystemApps) onToggleSystemApps() },
                    label = { Text("Sistema") },
                    colors = filterColors()
                )
                FilterChip(
                    selected = state.sortOrder == AppSortOrder.NAME_ASC || state.sortOrder == AppSortOrder.NAME_DESC,
                    onClick = {
                        val next = if (state.sortOrder == AppSortOrder.NAME_ASC) {
                            AppSortOrder.NAME_DESC
                        } else {
                            AppSortOrder.NAME_ASC
                        }
                        onSortOrderChange(next)
                    },
                    leadingIcon = {
                        Icon(
                            if (state.sortOrder == AppSortOrder.NAME_DESC) Icons.Outlined.ArrowDownward else Icons.Outlined.SortByAlpha,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(if (state.sortOrder == AppSortOrder.NAME_DESC) "Z → A" else "A → Z")
                    },
                    colors = filterColors()
                )
                FilterChip(
                    selected = state.sortOrder == AppSortOrder.SIZE_DESC || state.sortOrder == AppSortOrder.SIZE_ASC,
                    onClick = {
                        val next = if (state.sortOrder == AppSortOrder.SIZE_DESC) {
                            AppSortOrder.SIZE_ASC
                        } else {
                            AppSortOrder.SIZE_DESC
                        }
                        onSortOrderChange(next)
                    },
                    leadingIcon = {
                        Icon(
                            if (state.sortOrder == AppSortOrder.SIZE_ASC) Icons.Outlined.ArrowUpward else Icons.Outlined.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(if (state.sortOrder == AppSortOrder.SIZE_ASC) "Menor tamanho" else "Maior tamanho")
                    },
                    colors = filterColors()
                )
            }

            // Contador de apps e Ação Selecionar Tudo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${visible.size} aplicativos encontrados",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(Modifier.weight(1f))
                TextButton(
                    onClick = onSelectAllVisible,
                    enabled = visible.isNotEmpty(),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = if (allVisibleSelected) "Desmarcar todos" else "Selecionar todos",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Lista de Apps
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (visible.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Nenhum app encontrado.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.packageName }) { app ->
                        AppRow(
                            app = app,
                            selected = app.packageName in state.selectedPackages,
                            onToggle = { onToggleApp(app.packageName) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SilentStatusBanner(isAuto: Boolean, onEnable: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (isAuto) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !isAuto, onClick = onEnable)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isAuto) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surface
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Bolt,
                contentDescription = null,
                tint = if (isAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (isAuto) "Desinstalação automática ativa" else "Desinstalação automática desligada",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isAuto) {
                    "Os apps selecionados serão confirmados e removidos automaticamente."
                } else {
                    "Toque aqui para ativar o serviço nas configurações do celular."
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun filterColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
)

@Composable
private fun AppRow(
    app: InstalledApp,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) PanelElevated else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        Spacer(Modifier.width(8.dp))
        AppIcon(
            app = app,
            packageManager = context.packageManager
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (app.formattedSize.isNotEmpty()) {
                    Text(
                        text = app.formattedSize,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                if (app.isSystem) {
                    Text(
                        text = "Sistema",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Text(
                    text = app.packageName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
            }
        }
    }
}

@Composable
private fun AppIcon(app: InstalledApp, packageManager: android.content.pm.PackageManager) {
    val bitmap = remember(app.packageName) {
        app.getIcon(packageManager).toBitmap()
    }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, OutlineBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    )
}

@Composable
private fun UninstallBar(
    count: Int,
    uninstalling: Boolean,
    onClear: () -> Unit,
    onUninstall: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(PanelElevated)
            .border(1.dp, OutlineBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Button(
            onClick = onUninstall,
            enabled = !uninstalling,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            if (uninstalling) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.onError,
                    strokeWidth = 2.5.dp
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Desinstalando aplicativos...",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Desinstalar $count ${if (count == 1) "aplicativo" else "aplicativos"}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        TextButton(
            onClick = onClear,
            enabled = !uninstalling,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(44.dp)
        ) {
            Text(
                "Limpar seleção",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
