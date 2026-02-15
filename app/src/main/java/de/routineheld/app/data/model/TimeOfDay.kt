package de.routineheld.app.data.model

enum class TimeOfDay(val index: Int, val labelDE: String) {
    MORNING(0, "Morgens"),
    AFTERNOON(1, "Mittags"),
    EVENING(2, "Abends");

    companion object {
        fun fromIndex(index: Int) = entries.first { it.index == index }
    }
}
