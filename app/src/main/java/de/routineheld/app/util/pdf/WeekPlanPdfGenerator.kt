package de.routineheld.app.util.pdf

import android.content.Context
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import de.routineheld.app.data.local.relation.WeekPlanWithSlots
import java.io.File

class WeekPlanPdfGenerator(
    private val context: Context
) {
    fun generate(
        weekPlan: WeekPlanWithSlots,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): File {
        val html = WochenplanHtmlBuilder(context).build(weekPlan, plans, activities, childName)
        val file = File(context.cacheDir, "routineheld_week_${weekPlan.weekPlan.name.sanitize()}.pdf")
        HtmlPdfRenderer(context).render(html, file, landscape = true)
        return file
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
