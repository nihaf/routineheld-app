package de.routineheld.app.ui.weekplan.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.routineheld.app.data.model.DayOfWeek
import de.routineheld.app.data.model.TimeOfDay
import de.routineheld.app.ui.weekplan.components.WeekPlanMatrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPlanEditorScreen(
    weekPlanId: Long,
    onNavigateBack: () -> Unit,
    viewModel: WeekPlanEditorViewModel = hiltViewModel()
) {
    val weekPlanWithSlots by viewModel.weekPlanWithSlots.collectAsStateWithLifecycle()
    val slotMap by viewModel.slotMap.collectAsStateWithLifecycle()
    val planLookup by viewModel.planLookup.collectAsStateWithLifecycle()
    val activityLookup by viewModel.activityLookup.collectAsStateWithLifecycle()
    val availablePlans by viewModel.availablePlans.collectAsStateWithLifecycle()
    val allActivities by viewModel.allActivities.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(weekPlanWithSlots?.weekPlan?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::exportWeekPlanAsPdf) {
                        Icon(Icons.Default.Share, "Als PDF exportieren")
                    }
                }
            )
        }
    ) { padding ->
        if (weekPlanWithSlots == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            WeekPlanMatrix(
                slotMap = slotMap,
                planLookup = planLookup,
                activityLookup = activityLookup,
                onSlotClick = viewModel::showAssignSheet,
                onSlotLongPress = viewModel::showSlotMenu,
                modifier = Modifier.padding(padding)
            )
        }
    }

    // Assignment sheet
    if (uiState.showAssignSheet && uiState.selectedDay != null && uiState.selectedTime != null) {
        SlotAssignSheet(
            day = DayOfWeek.fromIndex(uiState.selectedDay),
            time = TimeOfDay.fromIndex(uiState.selectedTime),
            currentSlot = slotMap[Pair(uiState.selectedDay, uiState.selectedTime)],
            availablePlans = availablePlans,
            allActivities = allActivities,
            onAssignPlan = { planId ->
                viewModel.assignPlanToSlot(uiState.selectedDay, uiState.selectedTime, planId)
            },
            onAssignActivity = { activityId ->
                viewModel.assignActivityToSlot(uiState.selectedDay, uiState.selectedTime, activityId)
            },
            onDismiss = viewModel::dismissAssignSheet
        )
    }

    // Slot menu (long press)
    if (uiState.showSlotMenu && uiState.menuSlotDay != null && uiState.menuSlotTime != null) {
        var showMenu by remember { mutableStateOf(true) }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = {
                showMenu = false
                viewModel.dismissSlotMenu()
            }
        ) {
            DropdownMenuItem(
                text = { Text("Ändern") },
                onClick = {
                    showMenu = false
                    viewModel.dismissSlotMenu()
                    viewModel.showAssignSheet(uiState.menuSlotDay, uiState.menuSlotTime)
                }
            )
            DropdownMenuItem(
                text = { Text("Auf ganze Woche kopieren") },
                onClick = {
                    showMenu = false
                    viewModel.copySlotToWholeWeek(uiState.menuSlotDay, uiState.menuSlotTime)
                }
            )
            DropdownMenuItem(
                text = { Text("Entfernen") },
                onClick = {
                    showMenu = false
                    viewModel.clearSlot(uiState.menuSlotDay, uiState.menuSlotTime)
                }
            )
        }
    }

    // Export progress dialog
    if (uiState.isExporting) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("PDF wird erstellt...") },
            text = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = {}
        )
    }

    // Export error dialog
    if (uiState.exportError != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissExportError,
            title = { Text("Fehler beim Export") },
            text = { Text(uiState.exportError) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissExportError) {
                    Text("OK")
                }
            }
        )
    }
}
