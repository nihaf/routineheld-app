package de.routineheld.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Screen
)

val bottomNavItems = listOf(
    BottomNavItem("Aktivitäten", Icons.Outlined.Category, Screen.Activities),
    BottomNavItem("Abläufe", Icons.Outlined.ViewList, Screen.Plans),
    BottomNavItem("Wochenplan", Icons.Outlined.CalendarMonth, Screen.WeekPlan)
)

@Composable
fun BottomNavBar(
    currentRoute: Screen?,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}
