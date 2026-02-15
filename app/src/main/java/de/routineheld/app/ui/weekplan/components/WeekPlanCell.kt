package de.routineheld.app.ui.weekplan.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.ui.theme.ActivityColors
import de.routineheld.app.util.IconRegistry

@Composable
fun WeekPlanCell(
    slot: WeekPlanSlotEntity?,
    plan: RoutinePlanWithEntries?,
    activities: Map<Long, ActivityEntity>,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Empty cell
    if (slot == null || (slot.routinePlanId == null && slot.activityId == null)) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(2.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp)
                )
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(8.dp)
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Aktivität zuweisen",
                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
        return
    }

    // Plan assigned
    if (slot.routinePlanId != null && plan != null) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .padding(2.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongPress
                ),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Icons row (max 4)
                val planActivities = plan.entries
                    .sortedBy { it.position }
                    .mapNotNull { activities[it.activityId] }
                val iconsToShow = planActivities.take(4)

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    iconsToShow.forEach { activity ->
                        val iconRes = IconRegistry.getDrawableRes(context, activity.iconRef)
                        if (iconRes != 0) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = activity.name,
                                modifier = Modifier.size(18.dp).padding(horizontal = 1.dp),
                                tint = Color.Unspecified
                            )
                        }
                    }
                    if (planActivities.size > 4) {
                        Text(
                            text = "+${planActivities.size - 4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }

                // Plan name
                Text(
                    text = plan.plan.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        return
    }

    // Single activity assigned
    if (slot.activityId != null) {
        val activity = activities[slot.activityId]
        if (activity != null) {
            val backgroundColor = activity.color?.let {
                if (it in ActivityColors.indices) {
                    ActivityColors[it].copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            } ?: MaterialTheme.colorScheme.surface

            Surface(
                modifier = modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongPress
                    ),
                shape = RoundedCornerShape(8.dp),
                color = backgroundColor,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Icon
                    val iconRes = IconRegistry.getDrawableRes(context, activity.iconRef)
                    if (iconRes != 0) {
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = activity.name,
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                    }

                    // Activity name
                    Text(
                        text = activity.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
