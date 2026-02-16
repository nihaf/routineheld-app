package de.routineheld.app.ui.weekplan.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.model.DayOfWeek
import de.routineheld.app.data.model.TimeOfDay
import de.routineheld.app.ui.theme.ActivityColors
import de.routineheld.app.util.IconRegistry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlotAssignSheet(
    day: DayOfWeek,
    time: TimeOfDay,
    currentSlot: WeekPlanSlotEntity?,
    availablePlans: List<RoutinePlanWithEntries>,
    allActivities: List<ActivityEntity>,
    onAssignPlan: (Long) -> Unit,
    onAssignActivity: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var assignmentMode by remember {
        mutableStateOf(
            if (currentSlot?.routinePlanId != null) "plan" else "activity"
        )
    }
    var selectedPlanId by remember { mutableStateOf(currentSlot?.routinePlanId) }
    var selectedActivityId by remember { mutableStateOf(currentSlot?.activityId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Title
            Text(
                text = "${day.labelDE} · ${time.labelDE}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mode selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { assignmentMode = "plan" }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = assignmentMode == "plan",
                    onClick = { assignmentMode = "plan" }
                )
                Text(
                    text = "Ablaufplan zuweisen",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Plan dropdown
            if (assignmentMode == "plan") {
                var expanded by remember { mutableStateOf(false) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 40.dp, bottom = 16.dp)
                ) {
                    TextField(
                        value = availablePlans.find { it.plan.id == selectedPlanId }?.plan?.name ?: "Plan auswählen",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availablePlans.forEach { plan ->
                            DropdownMenuItem(
                                text = { Text(plan.plan.name) },
                                onClick = {
                                    selectedPlanId = plan.plan.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { assignmentMode = "activity" }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = assignmentMode == "activity",
                    onClick = { assignmentMode = "activity" }
                )
                Text(
                    text = "Einzelne Aktivität",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Activity grid
            if (assignmentMode == "activity") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 40.dp, top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(allActivities, key = { it.id }) { activity ->
                        ActivityPickerItem(
                            activity = activity,
                            isSelected = activity.id == selectedActivityId,
                            onClick = { selectedActivityId = activity.id }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Abbrechen")
                }

                Button(
                    onClick = {
                        when (assignmentMode) {
                            "plan" -> selectedPlanId?.let { onAssignPlan(it) }
                            "activity" -> selectedActivityId?.let { onAssignActivity(it) }
                        }
                    },
                    enabled = (assignmentMode == "plan" && selectedPlanId != null) ||
                            (assignmentMode == "activity" && selectedActivityId != null)
                ) {
                    Text("Zuweisen")
                }
            }
        }
    }
}

@Composable
private fun ActivityPickerItem(
    activity: ActivityEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val iconRes = remember(activity.iconRef) {
        IconRegistry.getDrawableRes(context, activity.iconRef)
    }

    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        activity.color?.let {
            if (it in ActivityColors.indices) {
                ActivityColors[it].copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        } ?: MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (iconRes != 0) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = activity.name,
                        modifier = Modifier.size(32.dp),
                        tint = Color.Unspecified
                    )
                }

                Text(
                    text = activity.name,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
