package de.routineheld.app.util.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Renders an HTML string to a PDF file using Android's WebView for HTML layout
 * and PdfDocument/Canvas for PDF output.
 *
 * The WebView is rendered off-screen (software layer) at 2x the A4 CSS-pixel size,
 * then the resulting bitmap is written into a PdfDocument page at standard A4 dimensions.
 *
 * WebView operations must run on the main thread; the caller's (background) thread
 * blocks on a CountDownLatch until the PDF is written.
 *
 * Must be called from a background thread.
 */
class HtmlPdfRenderer(private val context: Context) {

    fun render(htmlContent: String, outputFile: File, landscape: Boolean = false) {
        check(Looper.myLooper() != Looper.getMainLooper()) {
            "HtmlPdfRenderer.render() must be called from a background thread"
        }

        // A4 in CSS px at 96dpi: portrait 794×1123, landscape 1123×794
        // Render at 2x for higher quality (each CSS px = 2 physical pixels)
        val cssW = if (landscape) 1123 else 794
        val cssH = if (landscape) 794 else 1123
        val physW = cssW * 2
        val physH = cssH * 2

        // A4 in PDF points (72dpi): portrait 595×842, landscape 842×595
        val pdfW = if (landscape) 842 else 595
        val pdfH = if (landscape) 595 else 842

        val latch = CountDownLatch(1)
        val error = AtomicReference<Throwable>()

        Handler(Looper.getMainLooper()).post {
            val webView = WebView(context)
            // Software rendering required for off-screen view.draw()
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            webView.settings.defaultTextEncodingName = "UTF-8"

            // Pre-layout so the WebView knows its size before content loads
            val wSpec = View.MeasureSpec.makeMeasureSpec(physW, View.MeasureSpec.EXACTLY)
            val hSpec = View.MeasureSpec.makeMeasureSpec(physH, View.MeasureSpec.EXACTLY)
            webView.measure(wSpec, hSpec)
            webView.layout(0, 0, physW, physH)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    try {
                        // Re-apply layout to catch any reflows after content load
                        view.measure(wSpec, hSpec)
                        view.layout(0, 0, physW, physH)

                        val bitmap = Bitmap.createBitmap(physW, physH, Bitmap.Config.ARGB_8888)
                        val bitmapCanvas = Canvas(bitmap)
                        bitmapCanvas.drawColor(Color.WHITE)
                        view.draw(bitmapCanvas)

                        val document = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(pdfW, pdfH, 1).create()
                        val page = document.startPage(pageInfo)
                        page.canvas.drawBitmap(
                            bitmap,
                            Rect(0, 0, physW, physH),
                            RectF(0f, 0f, pdfW.toFloat(), pdfH.toFloat()),
                            null
                        )
                        document.finishPage(page)
                        FileOutputStream(outputFile).use { document.writeTo(it) }
                        document.close()
                        bitmap.recycle()

                        latch.countDown()
                    } catch (e: Exception) {
                        error.set(e)
                        latch.countDown()
                    }
                }
            }

            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        }

        if (!latch.await(30, TimeUnit.SECONDS)) {
            throw Exception("PDF rendering timed out after 30 seconds")
        }
        error.get()?.let { throw Exception(it) }
    }
}
