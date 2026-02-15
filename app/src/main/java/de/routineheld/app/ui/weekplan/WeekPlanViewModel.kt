package de.routineheld.app.ui.weekplan

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.repository.WeekPlanRepository
import javax.inject.Inject

@HiltViewModel
class WeekPlanViewModel @Inject constructor(
    private val repository: WeekPlanRepository
) : ViewModel()
