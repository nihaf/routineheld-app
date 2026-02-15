package de.routineheld.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import kotlinx.coroutines.flow.Flow

@Dao
interface WeekPlanDao {

    @Query("SELECT * FROM week_plans ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<WeekPlanEntity>>

    @Transaction
    @Query("SELECT * FROM week_plans WHERE id = :id")
    fun getWeekPlanWithSlots(id: Long): Flow<WeekPlanWithSlots?>

    @Transaction
    @Query("SELECT * FROM week_plans WHERE id = :id")
    suspend fun getWeekPlanWithSlotsOnce(id: Long): WeekPlanWithSlots?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weekPlan: WeekPlanEntity): Long

    @Update
    suspend fun update(weekPlan: WeekPlanEntity)

    @Delete
    suspend fun delete(weekPlan: WeekPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: WeekPlanSlotEntity): Long

    @Update
    suspend fun updateSlot(slot: WeekPlanSlotEntity)

    @Query("DELETE FROM week_plan_slots WHERE id = :slotId")
    suspend fun deleteSlot(slotId: Long)

    @Query("DELETE FROM week_plan_slots WHERE weekPlanId = :weekPlanId")
    suspend fun deleteAllSlots(weekPlanId: Long)
}
