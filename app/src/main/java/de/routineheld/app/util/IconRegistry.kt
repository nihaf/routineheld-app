package de.routineheld.app.util

import android.content.Context
import androidx.annotation.DrawableRes

object IconRegistry {
    val allIcons: List<IconInfo> = listOf(
        // MORNING (8 icons)
        IconInfo("ic_wake_up", "Aufwachen", IconCategory.MORNING),
        IconInfo("ic_make_bed", "Bett machen", IconCategory.MORNING),
        IconInfo("ic_toilet", "Auf Toilette gehen", IconCategory.MORNING),
        IconInfo("ic_wash_face", "Waschen", IconCategory.MORNING),
        IconInfo("ic_brush_teeth", "Zähne putzen", IconCategory.MORNING),
        IconInfo("ic_comb_hair", "Haare kämmen", IconCategory.MORNING),
        IconInfo("ic_get_dressed", "Anziehen", IconCategory.MORNING),
        IconInfo("ic_shoes", "Schuhe anziehen", IconCategory.MORNING),

        // MEALS (7 icons)
        IconInfo("ic_breakfast", "Frühstücken", IconCategory.MEALS),
        IconInfo("ic_lunch", "Mittagessen", IconCategory.MEALS),
        IconInfo("ic_dinner", "Abendessen", IconCategory.MEALS),
        IconInfo("ic_snack", "Snack", IconCategory.MEALS),
        IconInfo("ic_drink", "Trinken", IconCategory.MEALS),
        IconInfo("ic_set_table", "Tisch decken", IconCategory.MEALS),
        IconInfo("ic_clear_table", "Tisch abräumen", IconCategory.MEALS),

        // ACTIVITIES (12 icons)
        IconInfo("ic_play_outside", "Draußen spielen", IconCategory.ACTIVITIES),
        IconInfo("ic_play_inside", "Drinnen spielen", IconCategory.ACTIVITIES),
        IconInfo("ic_paint", "Malen", IconCategory.ACTIVITIES),
        IconInfo("ic_craft", "Basteln", IconCategory.ACTIVITIES),
        IconInfo("ic_read_book", "Buch lesen", IconCategory.ACTIVITIES),
        IconInfo("ic_listen_music", "Musik hören", IconCategory.ACTIVITIES),
        IconInfo("ic_ball", "Ball spielen", IconCategory.ACTIVITIES),
        IconInfo("ic_soccer", "Fußball", IconCategory.ACTIVITIES),
        IconInfo("ic_gymnastics", "Turnen", IconCategory.ACTIVITIES),
        IconInfo("ic_swimming", "Schwimmen", IconCategory.ACTIVITIES),
        IconInfo("ic_bicycle", "Fahrrad fahren", IconCategory.ACTIVITIES),
        IconInfo("ic_playground", "Spielplatz", IconCategory.ACTIVITIES),

        // CHORES (8 icons)
        IconInfo("ic_tidy_up", "Aufräumen", IconCategory.CHORES),
        IconInfo("ic_study", "Lernen", IconCategory.CHORES),
        IconInfo("ic_homework", "Hausaufgaben", IconCategory.CHORES),
        IconInfo("ic_shopping", "Einkaufen", IconCategory.CHORES),
        IconInfo("ic_wash_hands", "Hände waschen", IconCategory.CHORES),
        IconInfo("ic_walk_dog", "Gassi gehen", IconCategory.CHORES),
        IconInfo("ic_water_plants", "Blumen gießen", IconCategory.CHORES),
        IconInfo("ic_take_trash", "Müll rausbringen", IconCategory.CHORES),

        // PLACES (8 icons)
        IconInfo("ic_kindergarten", "Kindergarten", IconCategory.PLACES),
        IconInfo("ic_school", "Schule", IconCategory.PLACES),
        IconInfo("ic_grandparents", "Oma/Opa besuchen", IconCategory.PLACES),
        IconInfo("ic_meet_friends", "Freunde treffen", IconCategory.PLACES),
        IconInfo("ic_doctor", "Arzt", IconCategory.PLACES),
        IconInfo("ic_trip", "Ausflug", IconCategory.PLACES),
        IconInfo("ic_forest", "Wald", IconCategory.PLACES),
        IconInfo("ic_zoo", "Zoo", IconCategory.PLACES),

        // EVENING (7 icons)
        IconInfo("ic_bath", "Baden", IconCategory.EVENING),
        IconInfo("ic_shower", "Duschen", IconCategory.EVENING),
        IconInfo("ic_pajamas", "Schlafanzug anziehen", IconCategory.EVENING),
        IconInfo("ic_cuddle", "Kuscheln", IconCategory.EVENING),
        IconInfo("ic_good_night", "Gute Nacht sagen", IconCategory.EVENING),
        IconInfo("ic_watch_tv", "Fernsehen", IconCategory.EVENING),
        IconInfo("ic_screen_time", "Medienzeit", IconCategory.EVENING),
    )

    /**
     * Find an icon by its drawable reference name
     */
    fun getByRef(ref: String): IconInfo? = allIcons.find { it.ref == ref }

    /**
     * Get all icons in a specific category
     */
    fun getByCategory(category: IconCategory): List<IconInfo> =
        allIcons.filter { it.category == category }

    /**
     * Search icons by German name (case-insensitive)
     */
    fun search(query: String): List<IconInfo> =
        allIcons.filter { it.nameDE.contains(query, ignoreCase = true) }

    /**
     * Get the drawable resource ID for an icon reference.
     *
     * Note: Uses getIdentifier() for dynamic resource loading based on database-stored
     * icon references. While this is discouraged by lint (slower than direct R.drawable
     * references), it's necessary here because:
     * - The app has 48 different activity icons
     * - Icon references are stored as strings in the database
     * - Icons must be loaded dynamically at runtime
     *
     * The lint warnings about "unused resources" for these drawables are false positives
     * since they ARE used, just not through direct references that lint can detect.
     */
    @DrawableRes
    @Suppress("DiscouragedApi")
    fun getDrawableRes(context: Context, ref: String): Int {
        return context.resources.getIdentifier(ref, "drawable", context.packageName)
    }
}
