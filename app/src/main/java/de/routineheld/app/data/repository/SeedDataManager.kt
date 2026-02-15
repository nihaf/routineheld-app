package de.routineheld.app.data.repository

import de.routineheld.app.data.local.datastore.AppPreferencesDataStore
import de.routineheld.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeedDataManager @Inject constructor(
    private val activityRepository: ActivityRepository,
    private val preferencesDataStore: AppPreferencesDataStore
) {
    suspend fun seedDefaultActivitiesIfNeeded() {
        val hasSeeded = preferencesDataStore.hasSeededActivities.first()
        if (hasSeeded) return

        val defaultActivities = listOf(
            ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up", isUserCreated = false),
            ActivityEntity(name = "Zähne putzen", iconRef = "ic_brush_teeth", isUserCreated = false),
            ActivityEntity(name = "Anziehen", iconRef = "ic_get_dressed", isUserCreated = false),
            ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast", isUserCreated = false),
            ActivityEntity(name = "Kindergarten", iconRef = "ic_kindergarten", isUserCreated = false),
            ActivityEntity(name = "Mittagessen", iconRef = "ic_lunch", isUserCreated = false),
            ActivityEntity(name = "Draußen spielen", iconRef = "ic_play_outside", isUserCreated = false),
            ActivityEntity(name = "Abendessen", iconRef = "ic_dinner", isUserCreated = false),
            ActivityEntity(name = "Baden", iconRef = "ic_bath", isUserCreated = false),
            ActivityEntity(name = "Schlafanzug anziehen", iconRef = "ic_pajamas", isUserCreated = false),
            ActivityEntity(name = "Buch lesen", iconRef = "ic_read_book", isUserCreated = false),
            ActivityEntity(name = "Gute Nacht sagen", iconRef = "ic_good_night", isUserCreated = false),
        )

        activityRepository.insertAll(defaultActivities)
        preferencesDataStore.setHasSeededActivities(true)
    }
}
