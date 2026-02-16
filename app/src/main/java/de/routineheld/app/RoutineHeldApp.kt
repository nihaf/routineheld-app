package de.routineheld.app

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.HiltAndroidApp
import de.routineheld.app.data.repository.SeedDataManager
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class RoutineHeldApp : Application() {

    @Inject
    lateinit var seedDataManager: SeedDataManager

    override fun onCreate() {
        super.onCreate()

        // Seed default activities on first launch
        ProcessLifecycleOwner.get().lifecycleScope.launch {
            seedDataManager.seedDefaultActivitiesIfNeeded()
        }
    }
}
