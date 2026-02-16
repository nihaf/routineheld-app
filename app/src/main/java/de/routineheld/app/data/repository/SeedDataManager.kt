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
            // Morgenroutine
            ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up", isUserCreated = false),
            ActivityEntity(name = "Auf Toilette gehen", iconRef = "ic_toilet", isUserCreated = false),
            ActivityEntity(name = "Zähne putzen", iconRef = "ic_brush_teeth", isUserCreated = false),
            ActivityEntity(name = "Gesicht waschen", iconRef = "ic_wash_face", isUserCreated = false),
            ActivityEntity(name = "Haare kämmen", iconRef = "ic_comb_hair", isUserCreated = false),
            ActivityEntity(name = "Anziehen", iconRef = "ic_get_dressed", isUserCreated = false),
            ActivityEntity(name = "Schuhe anziehen", iconRef = "ic_shoes", isUserCreated = false),
            ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast", isUserCreated = false),
            ActivityEntity(name = "Trinken", iconRef = "ic_drink", isUserCreated = false),
            ActivityEntity(name = "Bett machen", iconRef = "ic_make_bed", isUserCreated = false),
            ActivityEntity(name = "Rucksack packen", iconRef = "ic_pack_bag", isUserCreated = false),

            // Tagesablauf
            ActivityEntity(name = "Kindergarten", iconRef = "ic_kindergarten", isUserCreated = false),
            ActivityEntity(name = "Schule", iconRef = "ic_school", isUserCreated = false),
            ActivityEntity(name = "Hausaufgaben", iconRef = "ic_homework", isUserCreated = false),
            ActivityEntity(name = "Lernen", iconRef = "ic_study", isUserCreated = false),
            ActivityEntity(name = "Mittagessen", iconRef = "ic_lunch", isUserCreated = false),
            ActivityEntity(name = "Snack", iconRef = "ic_snack", isUserCreated = false),
            ActivityEntity(name = "Abendessen", iconRef = "ic_dinner", isUserCreated = false),
            ActivityEntity(name = "Pause machen", iconRef = "ic_rest", isUserCreated = false),

            // Körperpflege
            ActivityEntity(name = "Baden", iconRef = "ic_bath", isUserCreated = false),
            ActivityEntity(name = "Duschen", iconRef = "ic_shower", isUserCreated = false),
            ActivityEntity(name = "Hände waschen", iconRef = "ic_wash_hands", isUserCreated = false),

            // Abendablauf
            ActivityEntity(name = "Schlafanzug anziehen", iconRef = "ic_pajamas", isUserCreated = false),
            ActivityEntity(name = "Buch lesen", iconRef = "ic_read_book", isUserCreated = false),
            ActivityEntity(name = "Kuscheln", iconRef = "ic_cuddle", isUserCreated = false),
            ActivityEntity(name = "Gute Nacht sagen", iconRef = "ic_good_night", isUserCreated = false),
            ActivityEntity(name = "Ins Bett gehen", iconRef = "ic_bedtime", isUserCreated = false),

            // Sport & Bewegung
            ActivityEntity(name = "Draußen spielen", iconRef = "ic_play_outside", isUserCreated = false),
            ActivityEntity(name = "Drinnen spielen", iconRef = "ic_play_inside", isUserCreated = false),
            ActivityEntity(name = "Spielplatz", iconRef = "ic_playground", isUserCreated = false),
            ActivityEntity(name = "Ball spielen", iconRef = "ic_ball", isUserCreated = false),
            ActivityEntity(name = "Fußball", iconRef = "ic_soccer", isUserCreated = false),
            ActivityEntity(name = "Fahrrad fahren", iconRef = "ic_bicycle", isUserCreated = false),
            ActivityEntity(name = "Schwimmen", iconRef = "ic_swimming", isUserCreated = false),
            ActivityEntity(name = "Gymnastik", iconRef = "ic_gymnastics", isUserCreated = false),
            ActivityEntity(name = "Yoga", iconRef = "ic_yoga", isUserCreated = false),
            ActivityEntity(name = "Meditation", iconRef = "ic_meditation", isUserCreated = false),
            ActivityEntity(name = "Spazieren gehen", iconRef = "ic_walking", isUserCreated = false),
            ActivityEntity(name = "Joggen", iconRef = "ic_jogging", isUserCreated = false),
            ActivityEntity(name = "Tanzen", iconRef = "ic_dancing", isUserCreated = false),
            ActivityEntity(name = "Rollschuhe fahren", iconRef = "ic_roller_skate", isUserCreated = false),
            ActivityEntity(name = "Skateboard fahren", iconRef = "ic_skateboard", isUserCreated = false),
            ActivityEntity(name = "Klettern", iconRef = "ic_climbing", isUserCreated = false),
            ActivityEntity(name = "Trampolin springen", iconRef = "ic_trampoline", isUserCreated = false),
            ActivityEntity(name = "Dehnen", iconRef = "ic_stretching", isUserCreated = false),

            // Kreativität & Hobbys
            ActivityEntity(name = "Malen", iconRef = "ic_paint", isUserCreated = false),
            ActivityEntity(name = "Basteln", iconRef = "ic_craft", isUserCreated = false),
            ActivityEntity(name = "Musik hören", iconRef = "ic_listen_music", isUserCreated = false),
            ActivityEntity(name = "Klavier üben", iconRef = "ic_piano", isUserCreated = false),
            ActivityEntity(name = "Gitarre üben", iconRef = "ic_guitar", isUserCreated = false),
            ActivityEntity(name = "Singen", iconRef = "ic_singing", isUserCreated = false),
            ActivityEntity(name = "Backen", iconRef = "ic_baking", isUserCreated = false),
            ActivityEntity(name = "Kochen helfen", iconRef = "ic_cooking", isUserCreated = false),

            // Spiele & Unterhaltung
            ActivityEntity(name = "Bildschirmzeit", iconRef = "ic_screen_time", isUserCreated = false),
            ActivityEntity(name = "Fernsehen", iconRef = "ic_watch_tv", isUserCreated = false),
            ActivityEntity(name = "Computer", iconRef = "ic_computer", isUserCreated = false),
            ActivityEntity(name = "Tablet", iconRef = "ic_tablet", isUserCreated = false),
            ActivityEntity(name = "Videospiele", iconRef = "ic_video_game", isUserCreated = false),
            ActivityEntity(name = "Puzzle", iconRef = "ic_puzzle", isUserCreated = false),
            ActivityEntity(name = "Lego bauen", iconRef = "ic_lego", isUserCreated = false),

            // Haushaltstätigkeiten
            ActivityEntity(name = "Aufräumen", iconRef = "ic_tidy_up", isUserCreated = false),
            ActivityEntity(name = "Tisch decken", iconRef = "ic_set_table", isUserCreated = false),
            ActivityEntity(name = "Tisch abräumen", iconRef = "ic_clear_table", isUserCreated = false),
            ActivityEntity(name = "Geschirr spülen", iconRef = "ic_wash_dishes", isUserCreated = false),
            ActivityEntity(name = "Spülmaschine", iconRef = "ic_dishwasher", isUserCreated = false),
            ActivityEntity(name = "Müll rausbringen", iconRef = "ic_take_trash", isUserCreated = false),
            ActivityEntity(name = "Wäsche aufhängen", iconRef = "ic_hang_laundry", isUserCreated = false),
            ActivityEntity(name = "Wäsche zusammenlegen", iconRef = "ic_fold_laundry", isUserCreated = false),
            ActivityEntity(name = "Staubsaugen", iconRef = "ic_vacuum", isUserCreated = false),
            ActivityEntity(name = "Pflanzen gießen", iconRef = "ic_water_plants", isUserCreated = false),
            ActivityEntity(name = "Garten arbeiten", iconRef = "ic_gardening", isUserCreated = false),
            ActivityEntity(name = "Haustier füttern", iconRef = "ic_feed_pet", isUserCreated = false),
            ActivityEntity(name = "Hund spazieren führen", iconRef = "ic_walk_dog", isUserCreated = false),

            // Soziales & Ausflüge
            ActivityEntity(name = "Freunde treffen", iconRef = "ic_meet_friends", isUserCreated = false),
            ActivityEntity(name = "Großeltern besuchen", iconRef = "ic_grandparents", isUserCreated = false),
            ActivityEntity(name = "Einkaufen", iconRef = "ic_shopping", isUserCreated = false),
            ActivityEntity(name = "Ausflug", iconRef = "ic_trip", isUserCreated = false),
            ActivityEntity(name = "Zoo besuchen", iconRef = "ic_zoo", isUserCreated = false),
            ActivityEntity(name = "Wald", iconRef = "ic_forest", isUserCreated = false),
            ActivityEntity(name = "Arzt", iconRef = "ic_doctor", isUserCreated = false),
        )

        activityRepository.insertAll(defaultActivities)
        preferencesDataStore.setHasSeededActivities(true)
    }
}
