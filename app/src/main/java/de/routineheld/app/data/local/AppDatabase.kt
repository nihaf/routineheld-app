package de.routineheld.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import de.routineheld.app.data.local.dao.ActivityDao
import de.routineheld.app.data.local.dao.RoutinePlanDao
import de.routineheld.app.data.local.dao.WeekPlanDao
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntryEntity
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity

@Database(
    entities = [
        ActivityEntity::class,
        RoutinePlanEntity::class,
        RoutinePlanEntryEntity::class,
        WeekPlanEntity::class,
        WeekPlanSlotEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun routinePlanDao(): RoutinePlanDao
    abstract fun weekPlanDao(): WeekPlanDao
}
