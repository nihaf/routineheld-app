package de.routineheld.app.data.repository

import de.routineheld.app.data.local.dao.ActivityDao
import de.routineheld.app.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAll()

    fun getDefaultActivities(): Flow<List<ActivityEntity>> = activityDao.getDefaultActivities()

    suspend fun getById(id: Long): ActivityEntity? = activityDao.getById(id)

    suspend fun getUsageCount(activityId: Long): Int = activityDao.getUsageCount(activityId)

    suspend fun create(name: String, iconRef: String, color: Int? = null): Long {
        return activityDao.insert(
            ActivityEntity(name = name, iconRef = iconRef, color = color)
        )
    }

    suspend fun insert(activity: ActivityEntity): Long = activityDao.insert(activity)

    suspend fun insertAll(activities: List<ActivityEntity>): List<Long> =
        activityDao.insertAll(activities)

    suspend fun update(activity: ActivityEntity) = activityDao.update(activity)

    suspend fun delete(id: Long) = activityDao.deleteById(id)
}
