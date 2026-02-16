package de.routineheld.app.ui.weekplan.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.model.DayOfWeek
import de.routineheld.app.data.model.TimeOfDay

@Composable
fun WeekPlanMatrix(
    slotMap: Map<Pair<Int, Int>, WeekPlanSlotEntity>,
    planLookup: Map<Long, RoutinePlanWithEntries>,
    activityLookup: Map<Long, ActivityEntity>,
    onSlotClick: (dayOfWeek: Int, timeOfDay: Int) -> Unit,
    onSlotLongPress: (dayOfWeek: Int, timeOfDay: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        // Fixed column: Time-of-day headers
        Column(modifier = Modifier.width(80.dp)) {
            // Empty corner space
            Spacer(modifier = Modifier.height(48.dp))

            TimeOfDay.entries.forEach { time ->
                TimeOfDayHeader(
                    timeOfDay = time,
                    modifier = Modifier.height(120.dp)
                )
            }
        }

        // Scrollable columns: Days
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        ) {
            DayOfWeek.entries.forEach { day ->
                Column(modifier = Modifier.width(110.dp)) {
                    // Day header
                    DayHeader(
                        day = day,
                        modifier = Modifier.height(48.dp)
                    )

                    // 3 cells per day (morning, afternoon, evening)
                    TimeOfDay.entries.forEach { time ->
                        val key = Pair(day.index, time.index)
                        val slot = slotMap[key]
                        val plan = slot?.routinePlanId?.let { planLookup[it] }

                        WeekPlanCell(
                            slot = slot,
                            plan = plan,
                            activities = activityLookup,
                            onClick = { onSlotClick(day.index, time.index) },
                            onLongPress = { onSlotLongPress(day.index, time.index) },
                            modifier = Modifier.height(120.dp)
                        )
                    }
                }
            }
        }
    }
}
