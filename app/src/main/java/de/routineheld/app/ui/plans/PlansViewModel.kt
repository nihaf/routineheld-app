package de.routineheld.app.ui.plans

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.repository.RoutinePlanRepository
import javax.inject.Inject

@HiltViewModel
class PlansViewModel @Inject constructor(
    private val repository: RoutinePlanRepository
) : ViewModel()
