package de.routineheld.app.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen {

    @Serializable
    data object Activities : Screen

    @Serializable
    data object Plans : Screen

    @Serializable
    data object WeekPlan : Screen

    @Serializable
    data class PlanEditor(val planId: Long? = null) : Screen

    @Serializable
    data object Settings : Screen
}
