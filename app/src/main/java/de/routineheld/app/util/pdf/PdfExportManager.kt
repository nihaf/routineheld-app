package de.routineheld.app.util.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfExportManager @Inject constructor(
    private val context: Context,
    private val routinePlanPdfGenerator: RoutinePlanPdfGenerator,
    private val weekPlanPdfGenerator: WeekPlanPdfGenerator
) {
    sealed class ExportResult {
        data class Success(val file: File, val uri: Uri) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    suspend fun exportPlan(
        plan: RoutinePlanWithEntries,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val config = RoutinePlanPdfGenerator.PdfConfig(childName = childName)
                val tempFile = routinePlanPdfGenerator.generate(plan, activities, config)

                copyToDownloads(tempFile, plan.plan.name)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                ExportResult.Success(tempFile, uri)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Unbekannter Fehler beim PDF-Export")
            }
        }
    }

    private fun copyToDownloads(sourceFile: File, planName: String) {
        val fileName = "RoutineHeld_${planName.sanitize()}_${System.currentTimeMillis()}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI, values
            ) ?: throw IOException("Konnte Download-Eintrag nicht erstellen")

            context.contentResolver.openOutputStream(uri)?.use { out ->
                sourceFile.inputStream().use { it.copyTo(out) }
            }
        }
    }

    fun sharePdf(uri: Uri, planName: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Ablaufplan: $planName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Plan teilen").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    suspend fun exportWeekPlan(
        weekPlan: WeekPlanWithSlots,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): ExportResult {
        return withContext(Dispatchers.IO) {
            try {
                val tempFile = weekPlanPdfGenerator.generate(weekPlan, plans, activities, childName)

                copyToDownloads(tempFile, weekPlan.weekPlan.name)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )

                ExportResult.Success(tempFile, uri)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Unbekannter Fehler beim PDF-Export")
            }
        }
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
