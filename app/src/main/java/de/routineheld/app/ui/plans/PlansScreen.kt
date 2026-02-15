package de.routineheld.app.ui.plans

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.routineheld.app.ui.components.EmptyState

@Composable
fun PlansScreen(
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Outlined.ViewList,
        title = "Abläufe",
        subtitle = "Inhalt folgt in Phase 3",
        modifier = modifier
    )
}
