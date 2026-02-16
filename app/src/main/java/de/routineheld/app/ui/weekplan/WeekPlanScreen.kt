package de.routineheld.app.ui.weekplan

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.routineheld.app.ui.components.EmptyState
import de.routineheld.app.ui.weekplan.components.CreateWeekPlanDialog
import de.routineheld.app.ui.weekplan.components.WeekPlanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeekPlanScreen(
    modifier: Modifier = Modifier,
    viewModel: WeekPlanListViewModel = hiltViewModel(),
    onNavigateToEditor: (Long) -> Unit
) {
    val weekPlans by viewModel.weekPlans.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Wochenpläne") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showCreateDialog
            ) {
                Icon(Icons.Default.Add, "Neuen Wochenplan erstellen")
            }
        }
    ) { padding ->
        when {
            weekPlans.isEmpty() -> {
                EmptyState(
                    icon = Icons.Outlined.CalendarMonth,
                    title = "Noch keine Wochenpläne",
                    subtitle = "Erstelle deinen ersten Wochenplan",
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(padding)
                ) {
                    items(weekPlans, key = { it.id }) { weekPlan ->
                        WeekPlanCard(
                            weekPlan = weekPlan,
                            onClick = { onNavigateToEditor(weekPlan.id) },
                            onDelete = { viewModel.showDeleteDialog(weekPlan) }
                        )
                    }
                }
            }
        }
    }

    // Create dialog
    if (uiState.showCreateDialog) {
        CreateWeekPlanDialog(
            onDismiss = viewModel::dismissCreateDialog,
            onCreate = { name ->
                viewModel.createWeekPlan(name) { weekPlanId ->
                    onNavigateToEditor(weekPlanId)
                }
            }
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteDialog && uiState.deleteCandidate != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Wochenplan löschen?") },
            text = { Text("Möchtest du \"${uiState.deleteCandidate!!.name}\" wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
