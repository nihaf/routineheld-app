package de.routineheld.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.routineheld.app.ui.activities.ActivitiesScreen
import de.routineheld.app.ui.plans.PlansScreen
import de.routineheld.app.ui.weekplan.WeekPlanScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute: Screen? = when (navBackStackEntry?.destination?.route) {
        Screen.Activities::class.qualifiedName -> Screen.Activities
        Screen.Plans::class.qualifiedName -> Screen.Plans
        Screen.WeekPlan::class.qualifiedName -> Screen.WeekPlan
        else -> null
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { screen ->
                    navController.navigate(screen) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Activities,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Activities> {
                ActivitiesScreen()
            }
            composable<Screen.Plans> {
                PlansScreen()
            }
            composable<Screen.WeekPlan> {
                WeekPlanScreen()
            }
        }
    }
}
