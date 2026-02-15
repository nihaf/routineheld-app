package de.routineheld.app.ui.theme

import androidx.compose.ui.graphics.Color
import de.routineheld.app.data.model.TimeOfDay

object TimeOfDayColors {
    val morningBackground = Color(0xFFFFF8E1)
    val morningAccent = Color(0xFFF9A825)
    val afternoonBackground = Color(0xFFE8F5E9)
    val afternoonAccent = Color(0xFF66BB6A)
    val eveningBackground = Color(0xFFE3F2FD)
    val eveningAccent = Color(0xFF42A5F5)

    fun getBackgroundColor(timeOfDay: TimeOfDay): Color = when (timeOfDay) {
        TimeOfDay.MORNING -> morningBackground
        TimeOfDay.AFTERNOON -> afternoonBackground
        TimeOfDay.EVENING -> eveningBackground
    }
}
