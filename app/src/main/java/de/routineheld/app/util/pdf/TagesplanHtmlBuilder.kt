package de.routineheld.app.util.pdf

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.content.ContextCompat
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.util.IconRegistry
import java.io.ByteArrayOutputStream

class TagesplanHtmlBuilder(private val context: Context) {

    private val titleColors = listOf(
        "#E07850", "#5BAB8A", "#E8A040", "#7BAFD4", "#D4789C",
        "#9BB864", "#C8A0D8", "#E8C84C", "#6BAFB8"
    )

    fun build(
        plan: RoutinePlanWithEntries,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): String {
        val filledActivities = plan.entries
            .sortedBy { it.position }
            .mapNotNull { entry -> activities[entry.activityId] }

        val columns = if (filledActivities.size <= 9) 3 else 4
        val titleLetters = colorfulTitle(plan.plan.name)
        val subtitle = if (!childName.isNullOrBlank()) "~ für $childName ~" else "~ ${escapeHtml(plan.plan.name)} ~"
        val rowsHtml = buildRows(filledActivities, columns)

        return loadTemplate("tagesplan_template.html")
            .replace("{{PLAN_TITLE_LETTERS}}", titleLetters)
            .replace("{{SUBTITLE}}", subtitle)
            .replace("{{ROWS_HTML}}", rowsHtml)
    }

    private fun buildRows(activities: List<ActivityEntity>, columns: Int): String {
        val sb = StringBuilder()
        activities.chunked(columns).forEachIndexed { rowIndex, rowActivities ->
            sb.append("<tr>\n")
            rowActivities.forEachIndexed { colIndex, activity ->
                val number = rowIndex * columns + colIndex + 1
                sb.append(buildCard(number, activity))
            }
            // Pad last row with empty cells
            repeat(columns - rowActivities.size) {
                sb.append("<td></td>\n")
            }
            sb.append("</tr>\n")
        }
        return sb.toString()
    }

    private fun buildCard(number: Int, activity: ActivityEntity): String {
        val accentColor = if (activity.color != null && activity.color in 0..7) {
            PdfColors.activityColorHexes[activity.color!!]
        } else {
            "#F2D8A7"
        }
        // 200px bitmap displayed at 70px CSS → ~205 DPI effective in final PDF
        val base64 = drawableToBase64(activity.iconRef, 200)
        val iconHtml = if (base64.isNotEmpty()) {
            """<img class="card-icon" src="data:image/png;base64,$base64" alt="${escapeHtml(activity.name)}"/>"""
        } else ""

        return """<td class="card">
<span class="accent-bar" style="background:$accentColor;"></span>
$iconHtml
<span class="number-circle">$number</span>
<div class="card-name">${escapeHtml(activity.name)}</div>
</td>
"""
    }

    private fun colorfulTitle(text: String): String =
        text.uppercase().mapIndexed { i, c ->
            "<span style=\"color:${titleColors[i % titleColors.size]}\">$c</span>"
        }.joinToString("")

    private fun drawableToBase64(iconRef: String, sizePx: Int = 300): String {
        val resId = IconRegistry.getDrawableRes(context, iconRef)
        if (resId == 0) return ""
        val drawable = ContextCompat.getDrawable(context, resId) ?: return ""
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, sizePx, sizePx)
        drawable.draw(android.graphics.Canvas(bmp))
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
        bmp.recycle()
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    }

    private fun escapeHtml(text: String): String =
        text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")

    private fun loadTemplate(name: String): String =
        context.assets.open("pdf/$name").use { it.readBytes().toString(Charsets.UTF_8) }
}
