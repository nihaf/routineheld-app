package de.routineheld.app.ui.activities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.routineheld.app.ui.components.EmptyState

@Composable
fun ActivitiesScreen(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.Category,
        title = "Aktivitäten",
        subtitle = "Inhalt folgt in Phase 2",
        modifier = modifier
    )
}
