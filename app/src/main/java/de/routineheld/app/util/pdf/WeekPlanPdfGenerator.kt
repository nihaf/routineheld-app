package de.routineheld.app.util.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import de.routineheld.app.data.model.DayOfWeek
import de.routineheld.app.data.model.TimeOfDay
import de.routineheld.app.util.IconRegistry
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.toColorInt

class WeekPlanPdfGenerator(
    private val context: Context
) {
    // Page dimensions (A4 Landscape)
    private val pageWidth = 842f
    private val pageHeight = 595f
    private val margin = 30f

    // Table dimensions
    private val titleHeight = 60f
    private val footerHeight = 20f
    private val headerColWidth = 70f
    private val headerRowHeight = 32f
    private val availableHeight = pageHeight - 2 * margin - titleHeight - footerHeight
    private val dataRowHeight = (availableHeight - headerRowHeight) / 3
    private val dayColWidth = (pageWidth - 2 * margin - headerColWidth) / 7

    fun generate(
        weekPlan: WeekPlanWithSlots,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawBackground(canvas)
        drawTitle(canvas, weekPlan.weekPlan.name, childName)
        drawTable(canvas, weekPlan.slots, plans, activities)
        drawFooter(canvas)

        document.finishPage(page)

        val file = File(context.cacheDir, "routineheld_week_${weekPlan.weekPlan.name.sanitize()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(PdfColors.PAGE_BACKGROUND.toColorInt())
    }

    private fun drawTitle(canvas: Canvas, weekPlanName: String, childName: String?) {
        val paint = Paint().apply {
            color = PdfColors.TITLE_TEXT.toColorInt()
            textSize = 24f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("WOCHENPLAN", pageWidth / 2, 50f, paint)

        if (!childName.isNullOrBlank()) {
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("~ für $childName ~", pageWidth / 2, 70f, paint)
        } else {
            paint.textSize = 14f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("~ $weekPlanName ~", pageWidth / 2, 70f, paint)
        }
    }

    private fun drawTable(
        canvas: Canvas,
        slots: List<WeekPlanSlotEntity>,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>
    ) {
        val slotMap = slots.associateBy { Pair(it.dayOfWeek, it.timeOfDay) }
        val tableLeft = margin
        val tableTop = margin + titleHeight

        // Draw day headers
        DayOfWeek.entries.forEach { day ->
            val x = tableLeft + headerColWidth + (day.index - 1) * dayColWidth
            drawDayHeader(canvas, day.shortDE, x, tableTop, dayColWidth, headerRowHeight)
        }

        // Draw time rows
        TimeOfDay.entries.forEach { time ->
            val y = tableTop + headerRowHeight + time.index * dataRowHeight

            // Time header (left column)
            drawTimeHeader(canvas, time, tableLeft, y, headerColWidth, dataRowHeight)

            // 7 cells for each day
            DayOfWeek.entries.forEach { day ->
                val x = tableLeft + headerColWidth + (day.index - 1) * dayColWidth
                val slot = slotMap[Pair(day.index, time.index)]
                drawTableCell(canvas, slot, plans, activities, x, y, dayColWidth, dataRowHeight)
            }
        }

        // Draw table borders
        drawTableBorders(canvas, tableLeft, tableTop, headerColWidth, dayColWidth, headerRowHeight, dataRowHeight)
    }

    private fun drawDayHeader(
        canvas: Canvas,
        day: String,
        x: Float, y: Float,
        width: Float, height: Float
    ) {
        // Background
        val bgPaint = Paint().apply {
            color = Color.parseColor("#E5DDD5")
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        // Text
        val textPaint = Paint().apply {
            color = PdfColors.TITLE_TEXT.toColorInt()
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(day, x + width / 2, y + height / 2 + 4f, textPaint)
    }

    private fun drawTimeHeader(
        canvas: Canvas,
        time: TimeOfDay,
        x: Float, y: Float,
        width: Float, height: Float
    ) {
        // Colored background
        val bgColor = when (time) {
            TimeOfDay.MORNING -> Color.parseColor("#FFF8E1")
            TimeOfDay.AFTERNOON -> Color.parseColor("#E8F5E9")
            TimeOfDay.EVENING -> Color.parseColor("#E3F2FD")
        }
        val bgPaint = Paint().apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        // Text (vertically centered)
        val textPaint = Paint().apply {
            color = Color.parseColor("#442B1A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(time.labelDE, x + width / 2, y + height / 2 + 3f, textPaint)
    }

    private fun drawTableCell(
        canvas: Canvas,
        slot: WeekPlanSlotEntity?,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        x: Float, y: Float,
        width: Float, height: Float
    ) {
        val padding = 6f

        // Empty cell
        if (slot == null || (slot.routinePlanId == null && slot.activityId == null)) {
            val emptyPaint = Paint().apply {
                color = Color.parseColor("#D0C8C0")
                textSize = 14f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("—", x + width / 2, y + height / 2 + 5f, emptyPaint)
            return
        }

        // Plan assigned
        slot.routinePlanId?.let { planId ->
            val plan = plans[planId] ?: return@let
            val planActivities = plan.entries
                .sortedBy { it.position }
                .mapNotNull { activities[it.activityId] }

            // Draw icons (max 4)
            val iconSize = 20
            val iconsToShow = planActivities.take(4)
            val totalIconWidth = iconsToShow.size * (iconSize + 2)
            var iconX = x + (width - totalIconWidth) / 2

            iconsToShow.forEach { activity ->
                drawIcon(canvas, activity.iconRef, iconX, y + padding, iconSize)
                iconX += iconSize + 2
            }

            // "+X" badge if more than 4
            if (planActivities.size > 4) {
                val morePaint = Paint().apply {
                    color = Color.parseColor("#8B7B6B")
                    textSize = 8f
                    isAntiAlias = true
                }
                canvas.drawText("+${planActivities.size - 4}", iconX, y + padding + iconSize - 2, morePaint)
            }

            // Plan name
            val namePaint = TextPaint().apply {
                color = PdfColors.ACTIVITY_NAME.toColorInt()
                textSize = 9f
                isAntiAlias = true
            }
            val nameLayout = StaticLayout.Builder
                .obtain(plan.plan.name, 0, plan.plan.name.length, namePaint, (width - 2 * padding).toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()

            canvas.save()
            canvas.translate(x + padding, y + padding + iconSize + 6)
            nameLayout.draw(canvas)
            canvas.restore()
        }

        // Single activity assigned
        slot.activityId?.let { activityId ->
            val activity = activities[activityId] ?: return@let

            // Icon centered
            val iconSize = 28
            val iconX = x + (width - iconSize) / 2
            drawIcon(canvas, activity.iconRef, iconX, y + padding + 10, iconSize)

            // Name
            val namePaint = TextPaint().apply {
                color = PdfColors.ACTIVITY_NAME.toColorInt()
                textSize = 9f
                isAntiAlias = true
            }
            val nameLayout = StaticLayout.Builder
                .obtain(activity.name, 0, activity.name.length, namePaint, (width - 2 * padding).toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setMaxLines(2)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()

            canvas.save()
            canvas.translate(x + padding, y + padding + iconSize + 20)
            nameLayout.draw(canvas)
            canvas.restore()
        }
    }

    private fun drawTableBorders(
        canvas: Canvas,
        tableLeft: Float,
        tableTop: Float,
        headerColWidth: Float,
        dayColWidth: Float,
        headerRowHeight: Float,
        dataRowHeight: Float
    ) {
        val borderPaint = Paint().apply {
            color = Color.parseColor("#D0C5B4")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val tableWidth = headerColWidth + 7 * dayColWidth
        val tableHeight = headerRowHeight + 3 * dataRowHeight

        // Outer border
        canvas.drawRect(tableLeft, tableTop, tableLeft + tableWidth, tableTop + tableHeight, borderPaint)

        // Vertical lines
        // Header column separator
        canvas.drawLine(
            tableLeft + headerColWidth, tableTop,
            tableLeft + headerColWidth, tableTop + tableHeight,
            borderPaint
        )
        // Day column separators
        for (i in 1..6) {
            val x = tableLeft + headerColWidth + i * dayColWidth
            canvas.drawLine(x, tableTop, x, tableTop + tableHeight, borderPaint)
        }

        // Horizontal lines
        // Header row separator
        canvas.drawLine(
            tableLeft, tableTop + headerRowHeight,
            tableLeft + tableWidth, tableTop + headerRowHeight,
            borderPaint
        )
        // Data row separators
        for (i in 1..2) {
            val y = tableTop + headerRowHeight + i * dataRowHeight
            canvas.drawLine(tableLeft, y, tableLeft + tableWidth, y, borderPaint)
        }
    }

    private fun drawIcon(canvas: Canvas, iconRef: String, x: Float, y: Float, size: Int) {
        val resId = IconRegistry.getDrawableRes(context, iconRef)
        if (resId == 0) return

        val drawable = ContextCompat.getDrawable(context, resId) ?: return
        drawable.setBounds(x.toInt(), y.toInt(), (x + size).toInt(), (y + size).toInt())
        drawable.draw(canvas)
    }

    private fun drawFooter(canvas: Canvas) {
        val paint = Paint().apply {
            color = PdfColors.FOOTER_TEXT.toColorInt()
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("RoutineHeld", pageWidth / 2, pageHeight - 10f, paint)
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
