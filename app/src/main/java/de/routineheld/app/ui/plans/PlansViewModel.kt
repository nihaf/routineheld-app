package de.routineheld.app.ui.plans

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.repository.ActivityRepository
import de.routineheld.app.data.repository.RoutinePlanRepository
import de.routineheld.app.util.pdf.PdfExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository,
    private val pdfExportManager: PdfExportManager
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

    fun exportPlanAsPdf(planId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportError = null, exportedFileUri = null) }

            val plan = allPlans.value.find { it.plan.id == planId }
            if (plan == null) {
                _uiState.update { it.copy(isExporting = false, exportError = "Plan nicht gefunden") }
                return@launch
            }

            val allActivitiesList = activityRepository.getAllActivities().first()

            val activityMap = plan.entries
                .mapNotNull { entry ->
                    allActivitiesList.find { it.id == entry.activityId }
                }
                .associateBy { it.id }

            val result = pdfExportManager.exportPlan(plan, activityMap, childName = null)

            when (result) {
                is PdfExportManager.ExportResult.Success -> {
                    _uiState.update { it.copy(
                        isExporting = false,
                        exportedFileUri = result.uri
                    )}
                    pdfExportManager.sharePdf(result.uri, plan.plan.name)
                }
                is PdfExportManager.ExportResult.Error -> {
                    _uiState.update { it.copy(
                        isExporting = false,
                        exportError = result.message
                    )}
                }
            }
        }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(exportedFileUri = null, exportError = null) }
    }
}

data class PlansUiState(
    val showCreateDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val deleteCandidate: RoutinePlanEntity? = null,
    val isExporting: Boolean = false,
    val exportedFileUri: Uri? = null,
    val exportError: String? = null
)
