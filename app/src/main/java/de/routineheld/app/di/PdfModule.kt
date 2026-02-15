package de.routineheld.app.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.routineheld.app.util.pdf.PdfExportManager
import de.routineheld.app.util.pdf.RoutinePlanPdfGenerator
import de.routineheld.app.util.pdf.WeekPlanPdfGenerator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PdfModule {

    @Provides
    @Singleton
    fun provideRoutinePlanPdfGenerator(
        @ApplicationContext context: Context
    ): RoutinePlanPdfGenerator {
        return RoutinePlanPdfGenerator(context)
    }

    @Provides
    @Singleton
    fun provideWeekPlanPdfGenerator(
        @ApplicationContext context: Context
    ): WeekPlanPdfGenerator {
        return WeekPlanPdfGenerator(context)
    }

    @Provides
    @Singleton
    fun providePdfExportManager(
        @ApplicationContext context: Context,
        routinePlanPdfGenerator: RoutinePlanPdfGenerator,
        weekPlanPdfGenerator: WeekPlanPdfGenerator
    ): PdfExportManager {
        return PdfExportManager(context, routinePlanPdfGenerator, weekPlanPdfGenerator)
    }
}
