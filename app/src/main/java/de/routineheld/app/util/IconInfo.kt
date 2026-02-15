package de.routineheld.app.util

data class IconInfo(
    val ref: String,           // Drawable resource name, e.g. "ic_wake_up"
    val nameDE: String,        // German display name
    val category: IconCategory
)

enum class IconCategory(val labelDE: String) {
    MORNING("Morgenroutine"),           // 8 icons
    MEALS("Mahlzeiten"),                // 7 icons
    ACTIVITIES("Aktivitäten & Spiel"),  // 12 icons
    CHORES("Pflichten & Lernen"),       // 8 icons
    PLACES("Orte & Wege"),              // 8 icons
    EVENING("Abendroutine")             // 7 icons
}
