package de.routineheld.app.ui.activities

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.ui.components.EmptyState
import de.routineheld.app.util.IconRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitiesScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivitiesViewModel = hiltViewModel()
) {
    val activities by viewModel.activities.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Aktivitäten") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Neue Aktivität")
            }
        }
    ) { paddingValues ->
        if (activities.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Category,
                title = "Noch keine Aktivitäten",
                subtitle = "Tippe auf +, um deine erste Aktivität zu erstellen",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities, key = { it.id }) { activity ->
                    ActivityCard(
                        activity = activity,
                        onClick = { viewModel.showEditDialog(activity) },
                        onLongClick = { viewModel.showDeleteDialog(activity) }
                    )
                }
            }
        }
    }

    // Dialogs
    if (uiState.showCreateEditDialog) {
        CreateEditActivityDialog(
            name = uiState.formName,
            iconRef = uiState.formIconRef,
            color = uiState.formColor,
            isValid = uiState.isFormValid,
            isEditMode = uiState.editingActivity != null,
            onNameChange = viewModel::updateFormName,
            onIconChange = viewModel::updateFormIcon,
            onColorChange = viewModel::updateFormColor,
            onSave = viewModel::saveActivity,
            onDismiss = viewModel::dismissCreateEditDialog
        )
    }

    if (uiState.showDeleteDialog) {
        DeleteConfirmationDialog(
            activityName = uiState.deleteCandidate?.name ?: "",
            usageCount = uiState.deleteUsageCount,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDeleteDialog
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ActivityCard(
    activity: ActivityEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    val iconRes = remember(activity.iconRef) {
        IconRegistry.getDrawableRes(context, activity.iconRef)
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .aspectRatio(0.9f)  // Slightly taller than square
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with optional colored background
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .then(
                        if (activity.color != null) {
                            Modifier
                                .background(
                                    color = Color(activity.color),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 3.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .padding(10.dp)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (iconRes != 0) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = activity.name,
                        modifier = Modifier.size(if (activity.color != null) 44.dp else 48.dp),
                        tint = Color.Unspecified
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Activity name
            Text(
                text = activity.name,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    activityName: String,
    usageCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Outlined.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text("Aktivität löschen?")
        },
        text = {
            if (usageCount > 0) {
                Text(
                    "'$activityName' wird in $usageCount Ablaufplänen verwendet. " +
                            "Beim Löschen wird sie auch aus diesen Plänen entfernt."
                )
            } else {
                Text("Möchtest du '$activityName' wirklich löschen?")
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(if (usageCount > 0) "Trotzdem löschen" else "Löschen")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
