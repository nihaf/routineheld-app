package de.routineheld.app.ui.plans.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.repository.ActivityRepository
import de.routineheld.app.data.repository.RoutinePlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val planId: Long = checkNotNull(savedStateHandle.get<Long>("planId")) {
        "PlanEditor requires planId"
    }

    val planWithEntries: StateFlow<RoutinePlanWithEntries?> =
        planRepository.getPlanWithEntries(planId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allActivities: StateFlow<List<ActivityEntity>> =
        activityRepository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(PlanEditorUiState())
    val uiState = _uiState.asStateFlow()

    fun showActivityPicker(forPosition: Int) {
        _uiState.update { it.copy(
            activeSlotPosition = forPosition,
            showActivityPicker = true
        )}
    }

    fun hideActivityPicker() {
        _uiState.update { it.copy(
            activeSlotPosition = null,
            showActivityPicker = false
        )}
    }

    fun assignActivityToSlot(position: Int, activity: ActivityEntity) {
        viewModelScope.launch {
            planRepository.addEntry(planId, activity.id, position)
            hideActivityPicker()
        }
    }

    fun removeEntry(entryId: Long) {
        viewModelScope.launch {
            planRepository.removeEntryById(entryId, planId)
        }
    }

    fun moveEntryUp(position: Int) {
        viewModelScope.launch {
            val entries = planWithEntries.value?.entries ?: return@launch
            val filledPositions = entries.sortedBy { it.position }.map { it.position }
            val currentIndex = filledPositions.indexOf(position)
            if (currentIndex > 0) {
                planRepository.reorderEntries(planId, currentIndex, currentIndex - 1)
            }
        }
    }

    fun moveEntryDown(position: Int) {
        viewModelScope.launch {
            val entries = planWithEntries.value?.entries ?: return@launch
            val filledPositions = entries.sortedBy { it.position }.map { it.position }
            val currentIndex = filledPositions.indexOf(position)
            if (currentIndex >= 0 && currentIndex < filledPositions.size - 1) {
                planRepository.reorderEntries(planId, currentIndex, currentIndex + 1)
            }
        }
    }

    fun showRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = true) }
    }

    fun hideRenameDialog() {
        _uiState.update { it.copy(showRenameDialog = false) }
    }

    fun renamePlan(newName: String) {
        viewModelScope.launch {
            planRepository.updatePlanName(planId, newName)
            hideRenameDialog()
        }
    }

    fun addSlot() {
        viewModelScope.launch {
            val currentPlan = planWithEntries.value?.plan ?: return@launch
            if (currentPlan.slotCount < 12) {
                planRepository.updateSlotCount(planId, currentPlan.slotCount + 1)
            }
        }
    }

    fun removeSlot() {
        viewModelScope.launch {
            val currentPlan = planWithEntries.value?.plan ?: return@launch
            if (currentPlan.slotCount > 3) {
                val entries = planWithEntries.value?.entries ?: emptyList()
                val lastPosition = currentPlan.slotCount - 1
                val hasEntryInLastSlot = entries.any { it.position == lastPosition }
                if (!hasEntryInLastSlot) {
                    planRepository.updateSlotCount(planId, currentPlan.slotCount - 1)
                }
            }
        }
    }
}

data class PlanEditorUiState(
    val activeSlotPosition: Int? = null,
    val showActivityPicker: Boolean = false,
    val showRenameDialog: Boolean = false
)
