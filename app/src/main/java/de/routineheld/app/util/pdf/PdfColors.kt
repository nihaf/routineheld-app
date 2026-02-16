package de.routineheld.app.util.pdf

import androidx.core.graphics.toColorInt

object PdfColors {
    // Base colors (from theme)
    const val PAGE_BACKGROUND = "#F2EEEB"
    const val CARD_BACKGROUND = "#FFFBF8"
    const val CARD_BORDER = "#E8D4C0"
    const val TITLE_TEXT = "#442B1A"
    const val NUMBER_CIRCLE = "#E8A87C"
    const val NUMBER_TEXT = "#FFFFFF"
    const val ACTIVITY_NAME = "#5C3D2E"
    const val FOOTER_TEXT = "#A89080"

    // Activity colors (mapped from ActivityColors in theme)
    val activityColorHexes = listOf(
        "#F2D8A7", // Honig
        "#D9E8DD", // Salbei
        "#D4E6F0", // Himmel
        "#F2BEA0", // Pfirsich
        "#E8D4E8", // Lavendel
        "#F5E6A3", // Sonnengelb
        "#D4ECEC", // Mint
        "#F0D4D4"  // Rosa
    )

    fun applyOpacity(hexColor: String, opacity: Float): Int {
        val color = hexColor.toColorInt()
        val alpha = (opacity * 255).toInt()
        return android.graphics.Color.argb(
            alpha,
            android.graphics.Color.red(color),
            android.graphics.Color.green(color),
            android.graphics.Color.blue(color)
        )
    }

    fun darken(hexColor: String, factor: Float): Int {
        val color = hexColor.toColorInt()
        val r = (android.graphics.Color.red(color) * (1 - factor)).toInt()
        val g = (android.graphics.Color.green(color) * (1 - factor)).toInt()
        val b = (android.graphics.Color.blue(color) * (1 - factor)).toInt()
        return android.graphics.Color.rgb(r, g, b)
    }
}
