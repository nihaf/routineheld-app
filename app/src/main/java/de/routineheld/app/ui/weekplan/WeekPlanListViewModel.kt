package de.routineheld.app.ui.weekplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.repository.WeekPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeekPlanListViewModel @Inject constructor(
    private val weekPlanRepository: WeekPlanRepository
) : ViewModel() {

    val weekPlans: StateFlow<List<WeekPlanEntity>> =
        weekPlanRepository.getAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(WeekPlanListUiState())
    val uiState = _uiState.asStateFlow()

    fun showCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = true) }
    }

    fun dismissCreateDialog() {
        _uiState.update { it.copy(showCreateDialog = false) }
    }

    fun createWeekPlan(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val weekPlanId = weekPlanRepository.create(name)
            dismissCreateDialog()
            onCreated(weekPlanId)
        }
    }

    fun showDeleteDialog(weekPlan: WeekPlanEntity) {
        _uiState.update {
            it.copy(
                deleteCandidate = weekPlan,
                showDeleteDialog = true
            )
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.value.deleteCandidate?.let { weekPlan ->
                weekPlanRepository.delete(weekPlan)
            }
            dismissDeleteDialog()
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                deleteCandidate = null,
                showDeleteDialog = false
            )
        }
    }
}

data class WeekPlanListUiState(
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteCandidate: WeekPlanEntity? = null
)
