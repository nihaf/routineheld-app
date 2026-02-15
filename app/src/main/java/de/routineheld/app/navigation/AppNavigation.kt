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
import androidx.navigation.toRoute
import de.routineheld.app.ui.activities.ActivitiesScreen
import de.routineheld.app.ui.plans.PlansScreen
import de.routineheld.app.ui.plans.editor.PlanEditorScreen
import de.routineheld.app.ui.weekplan.WeekPlanScreen
import de.routineheld.app.ui.weekplan.editor.WeekPlanEditorScreen

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
                PlansScreen(
                    onNavigateToEditor = { planId ->
                        navController.navigate(Screen.PlanEditor(planId = planId))
                    }
                )
            }
            composable<Screen.PlanEditor> { backStackEntry ->
                val planEditor: Screen.PlanEditor = backStackEntry.toRoute()
                PlanEditorScreen(
                    planId = planEditor.planId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable<Screen.WeekPlan> {
                WeekPlanScreen(
                    onNavigateToEditor = { weekPlanId ->
                        navController.navigate(Screen.WeekPlanEditor(weekPlanId = weekPlanId))
                    }
                )
            }
            composable<Screen.WeekPlanEditor> { backStackEntry ->
                val weekPlanEditor: Screen.WeekPlanEditor = backStackEntry.toRoute()
                WeekPlanEditorScreen(
                    weekPlanId = weekPlanEditor.weekPlanId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
