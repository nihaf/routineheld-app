package de.routineheld.app.data.repository

import de.routineheld.app.data.local.dao.WeekPlanDao
import de.routineheld.app.data.local.entity.WeekPlanEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeekPlanRepository @Inject constructor(
    private val weekPlanDao: WeekPlanDao
) {
    fun getAll(): Flow<List<WeekPlanEntity>> = weekPlanDao.getAll()

    fun getWeekPlanWithSlots(id: Long): Flow<WeekPlanWithSlots?> =
        weekPlanDao.getWeekPlanWithSlots(id)

    suspend fun getWeekPlanWithSlotsOnce(id: Long): WeekPlanWithSlots? =
        weekPlanDao.getWeekPlanWithSlotsOnce(id)

    suspend fun create(name: String): Long {
        return weekPlanDao.insert(WeekPlanEntity(name = name))
    }

    suspend fun insert(weekPlan: WeekPlanEntity): Long = weekPlanDao.insert(weekPlan)

    suspend fun update(weekPlan: WeekPlanEntity) = weekPlanDao.update(weekPlan)

    suspend fun delete(weekPlan: WeekPlanEntity) = weekPlanDao.delete(weekPlan)

    suspend fun upsertSlot(slot: WeekPlanSlotEntity): Long = weekPlanDao.insertSlot(slot)

    suspend fun updateSlot(slot: WeekPlanSlotEntity) = weekPlanDao.updateSlot(slot)

    suspend fun deleteSlot(slotId: Long) = weekPlanDao.deleteSlot(slotId)

    suspend fun deleteAllSlots(weekPlanId: Long) = weekPlanDao.deleteAllSlots(weekPlanId)
}
