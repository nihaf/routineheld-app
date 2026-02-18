package de.routineheld.app.util.pdf

import android.content.Context
import de.routineheld.app.data.local.entity.ActivityEntity
import de.routineheld.app.data.local.relation.RoutinePlanWithEntries
import java.io.File

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
        val html = TagesplanHtmlBuilder(context).build(plan, activities, config.childName)
        val file = File(context.cacheDir, "routineheld_${plan.plan.name.sanitize()}.pdf")
        HtmlPdfRenderer(context).render(html, file)
        return file
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
