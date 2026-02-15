package de.routineheld.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntryEntity

data class RoutinePlanWithEntries(
    @Embedded val plan: RoutinePlanEntity,
    @Relation(
        entity = RoutinePlanEntryEntity::class,
        parentColumn = "id",
        entityColumn = "planId"
    )
    val entries: List<RoutinePlanEntryEntity>
)
