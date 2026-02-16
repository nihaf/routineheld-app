package de.routineheld.app.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey

object PreferencesKeys {
    val HAS_SEEDED_ACTIVITIES = booleanPreferencesKey("has_seeded_activities")
}
