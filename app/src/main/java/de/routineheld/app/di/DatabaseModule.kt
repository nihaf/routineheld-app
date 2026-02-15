package de.routineheld.app.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.routineheld.app.data.local.AppDatabase
import de.routineheld.app.data.local.dao.ActivityDao
import de.routineheld.app.data.local.dao.RoutinePlanDao
import de.routineheld.app.data.local.dao.WeekPlanDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "routineheld.db"
        ).build()
    }

    @Provides
    fun provideActivityDao(db: AppDatabase): ActivityDao = db.activityDao()

    @Provides
    fun provideRoutinePlanDao(db: AppDatabase): RoutinePlanDao = db.routinePlanDao()

    @Provides
    fun provideWeekPlanDao(db: AppDatabase): WeekPlanDao = db.weekPlanDao()
}
