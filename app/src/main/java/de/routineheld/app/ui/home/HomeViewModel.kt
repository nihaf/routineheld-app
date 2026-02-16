package de.routineheld.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.repository.RoutinePlanRepository
import de.routineheld.app.data.repository.WeekPlanRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val planRepository: RoutinePlanRepository,
    private val weekPlanRepository: WeekPlanRepository
) : ViewModel() {

    fun createPlan(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val planId = planRepository.create(name = "Neuer Ablauf", slotCount = 5)
            onCreated(planId)
        }
    }

    fun createWeekPlan(onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val weekPlanId = weekPlanRepository.create(name = "Neuer Wochenplan")
            onCreated(weekPlanId)
        }
    }
}
