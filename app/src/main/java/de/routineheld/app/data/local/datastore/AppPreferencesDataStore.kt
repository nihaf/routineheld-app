package de.routineheld.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferencesDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val hasSeededActivities: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.HAS_SEEDED_ACTIVITIES] ?: false
        }

    suspend fun setHasSeededActivities(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAS_SEEDED_ACTIVITIES] = value
        }
    }
}
