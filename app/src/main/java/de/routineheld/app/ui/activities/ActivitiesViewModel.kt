package de.routineheld.app.ui.activities

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.routineheld.app.data.repository.ActivityRepository
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel()
