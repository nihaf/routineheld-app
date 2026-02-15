package de.routineheld.app.ui.weekplan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.routineheld.app.ui.components.EmptyState

@Composable
fun WeekPlanScreen(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.CalendarMonth,
        title = "Wochenplan",
        subtitle = "Inhalt folgt in Phase 5",
        modifier = modifier
    )
}
