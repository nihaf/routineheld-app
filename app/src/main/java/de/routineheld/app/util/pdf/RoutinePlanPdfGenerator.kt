package de.routineheld.app.util.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.ContextCompat
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.util.IconRegistry
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withSave

class RoutinePlanPdfGenerator(
    private val context: Context
) {
    data class PdfConfig(
        val childName: String? = null,
        val showFooter: Boolean = true
    )

    fun generate(
        plan: RoutinePlanWithEntries,
        activities: Map<Long, ActivityEntity>,
        config: PdfConfig = PdfConfig()
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawBackground(canvas)
        drawTitle(canvas, plan.plan.name, config.childName)
        drawActivityCards(canvas, plan, activities)
        if (config.showFooter) {
            drawFooter(canvas)
        }

        document.finishPage(page)

        val file = File(context.cacheDir, "routineheld_${plan.plan.name.sanitize()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(PdfColors.PAGE_BACKGROUND.toColorInt())
    }

    private fun drawTitle(canvas: Canvas, planName: String, childName: String?) {
        val paint = Paint().apply {
            color = PdfColors.TITLE_TEXT.toColorInt()
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(planName, 595f / 2, 70f, paint)

        if (!childName.isNullOrBlank()) {
            paint.textSize = 16f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("für $childName", 595f / 2, 92f, paint)
        }
    }

    private fun drawActivityCards(
        canvas: Canvas,
        plan: RoutinePlanWithEntries,
        activities: Map<Long, ActivityEntity>
    ) {
        val filledEntries = plan.entries
            .sortedBy { it.position }
            .mapNotNull { entry -> activities[entry.activityId]?.let { entry to it } }

        if (filledEntries.isEmpty()) return

        val columns = if (filledEntries.size <= 9) 3 else 4
        val startY = 120f
        val marginH = 40f
        val spacing = 12f
        val usableWidth = 595f - 2 * marginH
        val cardWidth = (usableWidth - (columns - 1) * spacing) / columns
        val cardHeight = cardWidth * 1.15f

        filledEntries.forEachIndexed { index, (entry, activity) ->
            val col = index % columns
            val row = index / columns
            val x = marginH + col * (cardWidth + spacing)
            val y = startY + row * (cardHeight + spacing)

            drawSingleCard(canvas, x, y, cardWidth, cardHeight, index + 1, activity)
        }
    }

    private fun drawSingleCard(
        canvas: Canvas,
        x: Float, y: Float,
        width: Float, height: Float,
        number: Int,
        activity: ActivityEntity
    ) {
        val rect = RectF(x, y, x + width, y + height)

        // Background (with activity color)
        val bgPaint = Paint().apply {
            color = if (activity.color != null && activity.color in 0..7) {
                Color.parseColor(PdfColors.activityColorHexes[activity.color!!])
            } else {
                Color.parseColor(PdfColors.CARD_BACKGROUND)
            }
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 8f, 8f, bgPaint)

        // Border (darker shade of activity color)
        val borderPaint = Paint().apply {
            color = if (activity.color != null && activity.color in 0..7) {
                PdfColors.darken(PdfColors.activityColorHexes[activity.color!!], 0.15f)
            } else {
                Color.parseColor(PdfColors.CARD_BORDER)
            }
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

        // Icon
        val iconSize = (width * 0.55f).toInt()
        val iconX = x + (width - iconSize) / 2
        val iconY = y + 12f
        drawIcon(canvas, activity.iconRef, iconX, iconY, iconSize)

        // Number in circle
        val circleRadius = 11f
        val circleY = iconY + iconSize + 16f
        val circlePaint = Paint().apply {
            color = PdfColors.NUMBER_CIRCLE.toColorInt()
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x + width / 2, circleY, circleRadius, circlePaint)

        val numPaint = Paint().apply {
            color = PdfColors.NUMBER_TEXT.toColorInt()
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("$number", x + width / 2, circleY + 5f, numPaint)

        // Activity name
        val namePaint = Paint().apply {
            color = PdfColors.ACTIVITY_NAME.toColorInt()
            textSize = if (width > 140) 12f else 10f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val nameY = circleY + circleRadius + 14f
        drawTextWrapped(canvas, activity.name, x + width / 2, nameY, width - 8f, namePaint, 2)
    }

    private fun drawIcon(canvas: Canvas, iconRef: String, x: Float, y: Float, size: Int) {
        val resId = IconRegistry.getDrawableRes(context, iconRef)
        if (resId == 0) return

        val drawable = ContextCompat.getDrawable(context, resId) ?: return
        drawable.setBounds(x.toInt(), y.toInt(), (x + size).toInt(), (y + size).toInt())
        drawable.draw(canvas)
    }

    private fun drawTextWrapped(
        canvas: Canvas,
        text: String,
        centerX: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        maxLines: Int
    ) {
        val textPaint = TextPaint(paint)
        val layoutWidth = maxWidth.toInt()

        val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, layoutWidth)
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .setIncludePad(false)
            .build()

        canvas.withSave {
            // Position the layout so that its center aligns with centerX
            val leftPosition = centerX - layoutWidth / 2f
            translate(leftPosition, startY)
            layout.draw(this)
        }
    }

    private fun drawFooter(canvas: Canvas) {
        val paint = Paint().apply {
            color = PdfColors.FOOTER_TEXT.toColorInt()
            textSize = 8f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("RoutineHeld", 595f / 2, 832f, paint)
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
