package de.routineheld.app.util.pdf

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import androidx.core.content.ContextCompat
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.entity.WeekPlanSlotEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import de.routineheld.app.data.model.DayOfWeek
import de.routineheld.app.data.model.TimeOfDay
import de.routineheld.app.util.IconRegistry
import java.io.ByteArrayOutputStream

class WochenplanHtmlBuilder(private val context: Context) {

    private val titleColors = listOf(
        "#E07850", "#5BAB8A", "#E8A040", "#7BAFD4", "#D4789C",
        "#9BB864", "#C8A0D8", "#E8C84C", "#6BAFB8"
    )

    fun build(
        weekPlan: WeekPlanWithSlots,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): String {
        val titleLetters = colorfulTitle("WOCHENPLAN")
        val subtitle = if (!childName.isNullOrBlank()) "~ für $childName ~" else "~ ${escapeHtml(weekPlan.weekPlan.name)} ~"
        val slotMap = weekPlan.slots.associateBy { Pair(it.dayOfWeek, it.timeOfDay) }
        val tableRowsHtml = buildTableRows(slotMap, plans, activities)

        return loadTemplate("wochenplan_template.html")
            .replace("{{TITLE_LETTERS}}", titleLetters)
            .replace("{{SUBTITLE}}", subtitle)
            .replace("{{TABLE_ROWS}}", tableRowsHtml)
    }

    private fun buildTableRows(
        slotMap: Map<Pair<Int, Int>, WeekPlanSlotEntity>,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>
    ): String {
        val sb = StringBuilder()
        TimeOfDay.entries.forEach { time ->
            val timeClass = when (time) {
                TimeOfDay.MORNING -> "morning"
                TimeOfDay.AFTERNOON -> "afternoon"
                TimeOfDay.EVENING -> "evening"
            }
            val timeIcon = when (time) {
                TimeOfDay.MORNING -> "&#9728;"   // ☀
                TimeOfDay.AFTERNOON -> "&#9729;" // ☁
                TimeOfDay.EVENING -> "&#9790;"   // ☾
            }
            sb.append("<tr>\n")
            sb.append("""<td class="time-header $timeClass"><span class="time-icon">$timeIcon</span><span class="time-label">${escapeHtml(time.labelDE)}</span></td>""")
            sb.append("\n")
            DayOfWeek.entries.forEach { day ->
                val slot = slotMap[Pair(day.index, time.index)]
                sb.append(buildWeekCell(slot, plans, activities, timeClass))
                sb.append("\n")
            }
            sb.append("</tr>\n")
        }
        return sb.toString()
    }

    private fun buildWeekCell(
        slot: WeekPlanSlotEntity?,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        timeClass: String
    ): String {
        if (slot == null || (slot.routinePlanId == null && slot.activityId == null)) {
            return """<td class="cell $timeClass"><span class="empty">—</span></td>"""
        }

        val routinePlanId = slot.routinePlanId
        if (routinePlanId != null) {
            val plan = plans[routinePlanId]
            if (plan != null) {
                val planActivities = plan.entries
                    .sortedBy { it.position }
                    .mapNotNull { activities[it.activityId] }

                val iconsHtml = buildString {
                    planActivities.take(4).forEach { activity ->
                        // 90px bitmap displayed at 29px CSS → ~223 DPI effective in final PDF
                        val base64 = drawableToBase64(activity.iconRef, 90)
                        if (base64.isNotEmpty()) {
                            append("""<img src="data:image/png;base64,$base64" width="29" height="29" alt="${escapeHtml(activity.name)}"/>""")
                        }
                    }
                    if (planActivities.size > 4) {
                        append("""<span style="font-size:7pt;">+${planActivities.size - 4}</span>""")
                    }
                }

                return """<td class="cell $timeClass"><div class="mini-icons">$iconsHtml</div><span class="plan-name">${escapeHtml(plan.plan.name)}</span></td>"""
            }
        }

        val activityId = slot.activityId
        if (activityId != null) {
            val activity = activities[activityId]
            if (activity != null) {
                // 120px bitmap displayed at 40px CSS → ~216 DPI effective in final PDF
                val base64 = drawableToBase64(activity.iconRef, 120)
                val iconHtml = if (base64.isNotEmpty()) {
                    """<img class="big-icon" src="data:image/png;base64,$base64" width="40" height="40" alt="${escapeHtml(activity.name)}"/>"""
                } else ""
                return """<td class="cell $timeClass">$iconHtml<span class="act-name">${escapeHtml(activity.name)}</span></td>"""
            }
        }

        return """<td class="cell $timeClass"><span class="empty">—</span></td>"""
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
