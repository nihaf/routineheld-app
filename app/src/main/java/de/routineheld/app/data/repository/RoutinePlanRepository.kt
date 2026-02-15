package de.routineheld.app.data.repository

import de.routineheld.app.data.local.dao.RoutinePlanDao
import de.routineheld.app.data.local.entity.RoutinePlanEntity
import de.routineheld.app.data.local.entity.RoutinePlanEntryEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutinePlanRepository @Inject constructor(
    private val planDao: RoutinePlanDao
) {
    fun getAll(): Flow<List<RoutinePlanEntity>> = planDao.getAll()

    fun getTemplates(): Flow<List<RoutinePlanEntity>> = planDao.getTemplates()

    fun getUserPlans(): Flow<List<RoutinePlanEntity>> = planDao.getUserPlans()

    fun getPlanWithEntries(id: Long): Flow<RoutinePlanWithEntries?> =
        planDao.getPlanWithEntries(id)

    suspend fun getPlanWithEntriesOnce(id: Long): RoutinePlanWithEntries? =
        planDao.getPlanWithEntriesOnce(id)

    suspend fun create(name: String, slotCount: Int, isTemplate: Boolean = false): Long {
        return planDao.insert(
            RoutinePlanEntity(name = name, slotCount = slotCount, isTemplate = isTemplate)
        )
    }

    suspend fun insert(plan: RoutinePlanEntity): Long = planDao.insert(plan)

    suspend fun update(plan: RoutinePlanEntity) = planDao.update(plan)

    suspend fun delete(plan: RoutinePlanEntity) = planDao.delete(plan)

    suspend fun touchPlan(planId: Long) {
        val plan = planDao.getPlanWithEntriesOnce(planId) ?: return
        planDao.update(plan.plan.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun addEntry(planId: Long, activityId: Long, position: Int): Long {
        return planDao.insertEntry(
            RoutinePlanEntryEntity(planId = planId, activityId = activityId, position = position)
        )
    }

    suspend fun insertEntries(entries: List<RoutinePlanEntryEntity>) =
        planDao.insertEntries(entries)

    suspend fun removeEntry(entry: RoutinePlanEntryEntity) = planDao.deleteEntry(entry)

    suspend fun deleteAllEntries(planId: Long) = planDao.deleteAllEntries(planId)

    suspend fun updateEntryPosition(entryId: Long, newPosition: Int) =
        planDao.updateEntryPosition(entryId, newPosition)

    suspend fun getEntriesForPlan(planId: Long): List<RoutinePlanEntryEntity> =
        planDao.getEntriesForPlan(planId)
}
