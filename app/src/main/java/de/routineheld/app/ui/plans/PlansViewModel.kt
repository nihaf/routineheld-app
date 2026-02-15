package de.routineheld.app.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.repository.ActivityRepository
import de.routineheld.app.data.repository.RoutinePlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val allPlans: StateFlow<List<RoutinePlanWithEntries>> =
        planRepository.getAllPlansWithEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPlans: StateFlow<List<RoutinePlanWithEntries>> = combine(
        allPlans,
        searchQuery
    ) { plans, query ->
        if (query.isBlank()) plans
        else plans.filter { it.plan.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(PlansUiState())
    val uiState = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createPlan(name: String, slotCount: Int, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val planId = planRepository.create(name, slotCount, isTemplate = false)
            onCreated(planId)
        }
    }

    fun showDeleteDialog(plan: RoutinePlanEntity) {
        _uiState.update { it.copy(
            deleteCandidate = plan,
            showDeleteDialog = true
        )}
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.value.deleteCandidate?.let { plan ->
                planRepository.delete(plan)
            }
            dismissDeleteDialog()
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(
            deleteCandidate = null,
            showDeleteDialog = false
        )}
    }

    fun duplicatePlan(planId: Long, onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val newPlanId = planRepository.duplicatePlan(planId)
            if (newPlanId > 0) onDuplicated(newPlanId)
        }
    }
}

data class PlansUiState(
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteCandidate: RoutinePlanEntity? = null
)
