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

    fun getAllPlansWithEntries(): Flow<List<RoutinePlanWithEntries>> =
        planDao.getAllPlansWithEntries()

    suspend fun reorderEntries(planId: Long, fromPos: Int, toPos: Int) {
        planDao.reorderEntries(planId, fromPos, toPos)
        touchPlan(planId)
    }

    suspend fun removeEntryById(entryId: Long, planId: Long) {
        planDao.deleteEntryById(entryId)
        touchPlan(planId)
    }

    suspend fun duplicatePlan(planId: Long): Long {
        val original = planDao.getPlanWithEntriesOnce(planId) ?: return -1
        val newPlan = original.plan.copy(
            id = 0,
            name = "${original.plan.name} (Kopie)",
            isTemplate = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val newPlanId = planDao.insert(newPlan)
        val newEntries = original.entries.map {
            it.copy(id = 0, planId = newPlanId)
        }
        planDao.insertEntries(newEntries)
        return newPlanId
    }

    suspend fun updatePlanName(planId: Long, newName: String) {
        val plan = planDao.getPlanWithEntriesOnce(planId)?.plan ?: return
        planDao.update(plan.copy(
            name = newName,
            updatedAt = System.currentTimeMillis()
        ))
    }

    suspend fun updateSlotCount(planId: Long, newSlotCount: Int) {
        val plan = planDao.getPlanWithEntriesOnce(planId)?.plan ?: return
        planDao.update(plan.copy(
            slotCount = newSlotCount,
            updatedAt = System.currentTimeMillis()
        ))
    }
}
