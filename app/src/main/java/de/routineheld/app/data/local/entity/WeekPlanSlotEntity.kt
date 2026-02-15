package de.routineheld.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "week_plan_slots",
    foreignKeys = [
        ForeignKey(
            entity = WeekPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["weekPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoutinePlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["routinePlanId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("weekPlanId"),
        Index("routinePlanId"),
        Index("activityId"),
        Index(value = ["weekPlanId", "dayOfWeek", "timeOfDay"], unique = true)
    ]
)
data class WeekPlanSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekPlanId: Long,
    val dayOfWeek: Int,
    val timeOfDay: Int,
    val routinePlanId: Long? = null,
    val activityId: Long? = null
)
