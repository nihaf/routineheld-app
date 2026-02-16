package de.routineheld.app.ui.weekplan.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import de.routineheld.app.data.repository.ActivityRepository
import de.routineheld.app.data.repository.RoutinePlanRepository
import de.routineheld.app.data.repository.WeekPlanRepository
import de.routineheld.app.util.pdf.PdfExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeekPlanEditorViewModel @Inject constructor(
    private val weekPlanRepository: WeekPlanRepository,
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository,
    private val pdfExportManager: PdfExportManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val weekPlanId: Long = checkNotNull(savedStateHandle.get<Long>("weekPlanId")) {
        "weekPlanId required"
    }

    // Main data sources
    val weekPlanWithSlots: StateFlow<WeekPlanWithSlots?> =
        weekPlanRepository.getWeekPlanWithSlots(weekPlanId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availablePlans: StateFlow<List<RoutinePlanWithEntries>> =
        planRepository.getAllPlansWithEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActivities: StateFlow<List<ActivityEntity>> =
        activityRepository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived state - slot map for O(1) lookup
    val slotMap: StateFlow<Map<Pair<Int, Int>, WeekPlanSlotEntity>> =
        weekPlanWithSlots.map { weekPlan ->
            weekPlan?.slots?.associateBy { Pair(it.dayOfWeek, it.timeOfDay) } ?: emptyMap()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Derived state - plan lookup map
    val planLookup: StateFlow<Map<Long, RoutinePlanWithEntries>> =
        availablePlans.map { plans ->
            plans.associateBy { it.plan.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Derived state - activity lookup map
    val activityLookup: StateFlow<Map<Long, ActivityEntity>> =
        allActivities.map { activities ->
            activities.associateBy { it.id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // UI state
    private val _uiState = MutableStateFlow(WeekPlanEditorUiState())
    val uiState = _uiState.asStateFlow()

    fun showAssignSheet(dayOfWeek: Int, timeOfDay: Int) {
        _uiState.update {
            it.copy(
                showAssignSheet = true,
                selectedDay = dayOfWeek,
                selectedTime = timeOfDay
            )
        }
    }

    fun dismissAssignSheet() {
        _uiState.update {
            it.copy(
                showAssignSheet = false,
                selectedDay = null,
                selectedTime = null
            )
        }
    }

    fun assignPlanToSlot(dayOfWeek: Int, timeOfDay: Int, planId: Long) {
        viewModelScope.launch {
            weekPlanRepository.upsertSlot(
                WeekPlanSlotEntity(
                    weekPlanId = weekPlanId,
                    dayOfWeek = dayOfWeek,
                    timeOfDay = timeOfDay,
                    routinePlanId = planId,
                    activityId = null
                )
            )
            dismissAssignSheet()
        }
    }

    fun assignActivityToSlot(dayOfWeek: Int, timeOfDay: Int, activityId: Long) {
        viewModelScope.launch {
            weekPlanRepository.upsertSlot(
                WeekPlanSlotEntity(
                    weekPlanId = weekPlanId,
                    dayOfWeek = dayOfWeek,
                    timeOfDay = timeOfDay,
                    routinePlanId = null,
                    activityId = activityId
                )
            )
            dismissAssignSheet()
        }
    }

    fun showSlotMenu(dayOfWeek: Int, timeOfDay: Int) {
        _uiState.update {
            it.copy(
                showSlotMenu = true,
                menuSlotDay = dayOfWeek,
                menuSlotTime = timeOfDay
            )
        }
    }

    fun dismissSlotMenu() {
        _uiState.update {
            it.copy(
                showSlotMenu = false,
                menuSlotDay = null,
                menuSlotTime = null
            )
        }
    }

    fun clearSlot(dayOfWeek: Int, timeOfDay: Int) {
        viewModelScope.launch {
            val slot = slotMap.value[Pair(dayOfWeek, timeOfDay)]
            if (slot != null) {
                weekPlanRepository.deleteSlot(slot.id)
            }
            dismissSlotMenu()
        }
    }

    fun copySlotToWholeWeek(sourceDay: Int, timeOfDay: Int) {
        viewModelScope.launch {
            val sourceSlot = slotMap.value[Pair(sourceDay, timeOfDay)]
            if (sourceSlot != null) {
                // Copy to all 7 days (1-7)
                for (day in 1..7) {
                    if (day != sourceDay) {
                        weekPlanRepository.upsertSlot(
                            WeekPlanSlotEntity(
                                weekPlanId = weekPlanId,
                                dayOfWeek = day,
                                timeOfDay = timeOfDay,
                                routinePlanId = sourceSlot.routinePlanId,
                                activityId = sourceSlot.activityId
                            )
                        )
                    }
                }
            }
            dismissSlotMenu()
        }
    }

    fun exportWeekPlanAsPdf() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null) }

            val weekPlan = weekPlanWithSlots.value
            if (weekPlan == null) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        exportError = "Wochenplan nicht gefunden"
                    )
                }
                return@launch
            }

            // Gather all referenced routine plans
            val planIds = weekPlan.slots.mapNotNull { it.routinePlanId }.distinct()
            val plans = planRepository.getAllPlansWithEntries().first()
                .filter { it.plan.id in planIds }
                .associateBy { it.plan.id }

            // Gather all referenced activities (from slots + from plan entries)
            val activityIds = mutableSetOf<Long>()
            weekPlan.slots.mapNotNull { it.activityId }.let { activityIds.addAll(it) }
            plans.values.flatMap { it.entries.map { e -> e.activityId } }
                .let { activityIds.addAll(it) }

            val activities = allActivities.value
                .filter { it.id in activityIds }
                .associateBy { it.id }

            val result = pdfExportManager.exportWeekPlan(weekPlan, plans, activities, childName = null)

            when (result) {
                is PdfExportManager.ExportResult.Success -> {
                    _uiState.update { it.copy(isExporting = false) }
                    pdfExportManager.sharePdf(result.uri, weekPlan.weekPlan.name)
                }
                is PdfExportManager.ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportError = result.message
                        )
                    }
                }
            }
        }
    }

    fun dismissExportError() {
        _uiState.update { it.copy(exportError = null) }
    }
}

data class WeekPlanEditorUiState(
    val showAssignSheet: Boolean = false,
    val selectedDay: Int? = null,
    val selectedTime: Int? = null,
    val showSlotMenu: Boolean = false,
    val menuSlotDay: Int? = null,
    val menuSlotTime: Int? = null,
    val isExporting: Boolean = false,
    val exportError: String? = null
)
