package de.routineheld.app.ui.weekplan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.routineheld.app.data.model.TimeOfDay
import de.routineheld.app.ui.theme.TimeOfDayColors

@Composable
fun TimeOfDayHeader(
    timeOfDay: TimeOfDay,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TimeOfDayColors.getBackgroundColor(timeOfDay))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = timeOfDay.labelDE,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}
