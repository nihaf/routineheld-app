package de.routineheld.app.ui.plans.editor

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlanEditorScreen(
    planId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: PlanEditorViewModel = hiltViewModel()
) {
    val planWithEntries by viewModel.planWithEntries.collectAsStateWithLifecycle()
    val allActivities by viewModel.allActivities.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val plan = planWithEntries?.plan
    val entries = planWithEntries?.entries ?: emptyList()

    val entriesByPosition = remember(entries) {
        entries.associateBy { it.position }
    }

    val activitiesById = remember(allActivities) {
        allActivities.associateBy { it.id }
    }

    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(plan?.name ?: "Laden...") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, "Menü")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Als PDF exportieren") },
                            onClick = { showMenu = false; viewModel.exportAsPdf() }
                        )
                        DropdownMenuItem(
                            text = { Text("Plan umbenennen") },
                            onClick = { showMenu = false; viewModel.showRenameDialog() }
                        )
                        DropdownMenuItem(
                            text = { Text("Platz hinzufügen") },
                            onClick = { showMenu = false; viewModel.addSlot() },
                            enabled = (plan?.slotCount ?: 0) < 12
                        )
                        DropdownMenuItem(
                            text = { Text("Platz entfernen") },
                            onClick = { showMenu = false; viewModel.removeSlot() },
                            enabled = (plan?.slotCount ?: 0) > 3
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (plan == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = plan.slotCount,
                    key = { position ->
                        entriesByPosition[position]?.id ?: "empty_$position"
                    }
                ) { position ->
                    val entry = entriesByPosition[position]
                    val activity = entry?.let { activitiesById[it.activityId] }

                    PlanSlot(
                        position = position,
                        activity = activity,
                        entry = entry,
                        onEmptySlotClick = { viewModel.showActivityPicker(position) },
                        onRemoveEntry = { entry?.let { viewModel.removeEntry(it.id) } },
                        onMoveUp = { viewModel.moveEntryUp(position) },
                        onMoveDown = { viewModel.moveEntryDown(position) },
                        modifier = Modifier.animateItem(
                            fadeInSpec = null, fadeOutSpec = null, placementSpec = spring(
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold
                            )
                        )
                    )
                }
            }
        }
    }

    if (uiState.showActivityPicker && uiState.activeSlotPosition != null) {
        ActivityPickerSheet(
            activities = allActivities,
            usedActivityIds = entries.map { it.activityId }.toSet(),
            onActivitySelected = { activity ->
                viewModel.assignActivityToSlot(uiState.activeSlotPosition!!, activity)
            },
            onDismiss = viewModel::hideActivityPicker
        )
    }

    if (uiState.showRenameDialog) {
        var newName by remember { mutableStateOf(plan?.name ?: "") }
        AlertDialog(
            onDismissRequest = viewModel::hideRenameDialog,
            title = { Text("Plan umbenennen") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { if (it.length <= 40) newName = it },
                    label = { Text("Name") },
                    supportingText = { Text("${newName.length}/40") }
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.renamePlan(newName) },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Speichern")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideRenameDialog) {
                    Text("Abbrechen")
                }
            }
        )
    }

    if (uiState.isExporting) {
        AlertDialog(
            onDismissRequest = { },
            confirmButton = { },
            icon = { CircularProgressIndicator() },
            text = { Text("PDF wird erstellt…") }
        )
    }

    uiState.exportError?.let { error ->
        AlertDialog(
            onDismissRequest = viewModel::dismissExportDialog,
            title = { Text("Fehler") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = viewModel::dismissExportDialog) {
                    Text("OK")
                }
            }
        )
    }
}
