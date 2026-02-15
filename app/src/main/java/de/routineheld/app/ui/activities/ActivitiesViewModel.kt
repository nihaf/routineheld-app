package de.routineheld.app.ui.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    // Activities list from database (Flow)
    val activities: StateFlow<List<ActivityEntity>> =
        repository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for dialogs and forms
    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    // Create/Edit Dialog Actions
    fun showCreateDialog() {
        _uiState.update {
            it.copy(
                showCreateEditDialog = true,
                editingActivity = null,
                formName = "",
                formIconRef = null,
                formColor = null,
                isFormValid = false
            )
        }
    }

    fun showEditDialog(activity: ActivityEntity) {
        _uiState.update {
            it.copy(
                showCreateEditDialog = true,
                editingActivity = activity,
                formName = activity.name,
                formIconRef = activity.iconRef,
                formColor = activity.color,
                isFormValid = true  // Existing activity is valid
            )
        }
    }

    fun dismissCreateEditDialog() {
        _uiState.update {
            it.copy(
                showCreateEditDialog = false,
                editingActivity = null,
                formName = "",
                formIconRef = null,
                formColor = null,
                isFormValid = false
            )
        }
    }

    // Form Field Updates
    fun updateFormName(name: String) {
        val trimmedName = name.take(30)  // Enforce 30 char limit
        _uiState.update {
            it.copy(
                formName = trimmedName,
                isFormValid = validateForm(trimmedName, it.formIconRef)
            )
        }
    }

    fun updateFormIcon(iconRef: String) {
        _uiState.update {
            it.copy(
                formIconRef = iconRef,
                isFormValid = validateForm(it.formName, iconRef)
            )
        }
    }

    fun updateFormColor(color: Int?) {
        _uiState.update { it.copy(formColor = color) }
    }

    private fun validateForm(name: String, iconRef: String?): Boolean {
        return name.trim().isNotBlank() && iconRef != null
    }

    // Save Action (Create or Update)
    fun saveActivity() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            if (state.editingActivity != null) {
                // Update existing
                repository.update(
                    state.editingActivity.copy(
                        name = state.formName.trim(),
                        iconRef = state.formIconRef!!,
                        color = state.formColor
                    )
                )
            } else {
                // Create new
                repository.create(
                    name = state.formName.trim(),
                    iconRef = state.formIconRef!!,
                    color = state.formColor
                )
            }
            dismissCreateEditDialog()
        }
    }

    // Delete Actions
    fun showDeleteDialog(activity: ActivityEntity) {
        viewModelScope.launch {
            val usageCount = repository.getUsageCount(activity.id)
            _uiState.update {
                it.copy(
                    deleteCandidate = activity,
                    deleteUsageCount = usageCount,
                    showDeleteDialog = true
                )
            }
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.value.deleteCandidate?.let { activity ->
                repository.delete(activity.id)
            }
            dismissDeleteDialog()
        }
    }

    fun dismissDeleteDialog() {
        _uiState.update {
            it.copy(
                deleteCandidate = null,
                deleteUsageCount = 0,
                showDeleteDialog = false
            )
        }
    }
}

data class ActivitiesUiState(
    // Create/Edit Dialog
    val showCreateEditDialog: Boolean = false,
    val editingActivity: ActivityEntity? = null,  // null = create, non-null = edit

    // Form State
    val formName: String = "",
    val formIconRef: String? = null,
    val formColor: Int? = null,
    val isFormValid: Boolean = false,

    // Delete Confirmation
    val showDeleteDialog: Boolean = false,
    val deleteCandidate: ActivityEntity? = null,
    val deleteUsageCount: Int = 0
)
