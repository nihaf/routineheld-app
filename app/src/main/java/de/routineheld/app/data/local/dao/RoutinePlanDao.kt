package de.routineheld.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntryEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutinePlanDao {

    @Query("SELECT * FROM routine_plans ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<RoutinePlanEntity>>

    @Query("SELECT * FROM routine_plans WHERE isTemplate = 1")
    fun getTemplates(): Flow<List<RoutinePlanEntity>>

    @Query("SELECT * FROM routine_plans WHERE isTemplate = 0 ORDER BY updatedAt DESC")
    fun getUserPlans(): Flow<List<RoutinePlanEntity>>

    @Transaction
    @Query("SELECT * FROM routine_plans WHERE id = :id")
    fun getPlanWithEntries(id: Long): Flow<RoutinePlanWithEntries?>

    @Transaction
    @Query("SELECT * FROM routine_plans WHERE id = :id")
    suspend fun getPlanWithEntriesOnce(id: Long): RoutinePlanWithEntries?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: RoutinePlanEntity): Long

    @Update
    suspend fun update(plan: RoutinePlanEntity)

    @Delete
    suspend fun delete(plan: RoutinePlanEntity)

    @Query("DELETE FROM routine_plans WHERE id = :id")
    suspend fun deleteById(id: Long)

    // Entries

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: RoutinePlanEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<RoutinePlanEntryEntity>)

    @Delete
    suspend fun deleteEntry(entry: RoutinePlanEntryEntity)

    @Query("DELETE FROM routine_plan_entries WHERE planId = :planId")
    suspend fun deleteAllEntries(planId: Long)

    @Query("UPDATE routine_plan_entries SET position = :newPosition WHERE id = :entryId")
    suspend fun updateEntryPosition(entryId: Long, newPosition: Int)

    @Query("SELECT * FROM routine_plan_entries WHERE planId = :planId ORDER BY position ASC")
    suspend fun getEntriesForPlan(planId: Long): List<RoutinePlanEntryEntity>

    @Query("DELETE FROM routine_plan_entries WHERE id = :entryId")
    suspend fun deleteEntryById(entryId: Long)

    @Transaction
    @Query("SELECT * FROM routine_plans WHERE isTemplate = 0 ORDER BY updatedAt DESC")
    fun getAllPlansWithEntries(): Flow<List<RoutinePlanWithEntries>>

    @Transaction
    suspend fun reorderEntries(planId: Long, fromPos: Int, toPos: Int) {
        val entries = getEntriesForPlan(planId)
        deleteAllEntries(planId)

        val mutableList = entries.toMutableList()
        val movedItem = mutableList.removeAt(fromPos)
        mutableList.add(toPos, movedItem)

        val updatedEntries = mutableList.mapIndexed { index, entry ->
            entry.copy(position = index)
        }
        insertEntries(updatedEntries)
    }
}
