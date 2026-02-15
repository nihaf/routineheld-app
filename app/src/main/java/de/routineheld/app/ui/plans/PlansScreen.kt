package de.routineheld.app.ui.plans

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansScreen(
    modifier: Modifier = Modifier,
    viewModel: PlansViewModel = hiltViewModel(),
    onNavigateToEditor: (Long) -> Unit = {}
) {
    val plans by viewModel.filteredPlans.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ablaufpläne") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, "Neuen Plan erstellen")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                placeholder = { Text("Pläne durchsuchen...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )

            if (plans.isEmpty() && searchQuery.isBlank()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.ViewList,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Noch keine Ablaufpläne", style = MaterialTheme.typography.titleLarge)
                        Text("Erstelle deinen ersten Ablaufplan", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                // Plans list
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(plans, key = { it.plan.id }) { planWithEntries ->
                        PlanCard(
                            planWithEntries = planWithEntries,
                            onClick = { onNavigateToEditor(planWithEntries.plan.id) },
                            onDelete = { viewModel.showDeleteDialog(planWithEntries.plan) },
                            onDuplicate = {
                                viewModel.duplicatePlan(planWithEntries.plan.id, onNavigateToEditor)
                            },
                            onExport = { viewModel.exportPlanAsPdf(planWithEntries.plan.id) }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showCreateDialog) {
        CreatePlanDialog(
            onDismiss = viewModel::dismissCreateDialog,
            onCreate = { name, slotCount ->
                viewModel.createPlan(name, slotCount) { planId ->
                    viewModel.dismissCreateDialog()
                    onNavigateToEditor(planId)
                }
            }
        )
    }

    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text("Plan löschen?") },
            text = { Text("${uiState.deleteCandidate?.name} wirklich löschen?") },
            confirmButton = {
                Button(onClick = viewModel::confirmDelete) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) { Text("Abbrechen") }
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
