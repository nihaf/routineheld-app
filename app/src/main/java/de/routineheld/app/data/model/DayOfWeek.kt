package de.routineheld.app.data.model

enum class DayOfWeek(val index: Int, val labelDE: String, val shortDE: String) {
    MONDAY(1, "Montag", "Mo"),
    TUESDAY(2, "Dienstag", "Di"),
    WEDNESDAY(3, "Mittwoch", "Mi"),
    THURSDAY(4, "Donnerstag", "Do"),
    FRIDAY(5, "Freitag", "Fr"),
    SATURDAY(6, "Samstag", "Sa"),
    SUNDAY(7, "Sonntag", "So");

    companion object {
        fun fromIndex(index: Int) = entries.first { it.index == index }
    }
}
