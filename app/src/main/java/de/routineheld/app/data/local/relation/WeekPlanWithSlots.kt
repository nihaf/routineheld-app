package de.routineheld.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity

data class WeekPlanWithSlots(
    @Embedded val weekPlan: WeekPlanEntity,
    @Relation(
        entity = WeekPlanSlotEntity::class,
        parentColumn = "id",
        entityColumn = "weekPlanId"
    )
    val slots: List<WeekPlanSlotEntity>
)
