# Phase 4: Erster Export — PDF-Export für Ablaufpläne

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-07 (PDF-Export Ablaufplan) |
| Abhängigkeiten | Phase 1 (Architektur), Phase 2 (Aktivitäten, Icons), Phase 3 (Ablaufpläne) |
| Ergebnis | Druckfertiger PDF-Export für Ablaufpläne mit Share-Funktion |
| Geschätzte Story Points | 8 |

## Voraussetzungen

Folgende Komponenten müssen existieren:

- Vollständige Ablaufpläne mit zugewiesenen Aktivitäten (Phase 3)
- Icon-Assets als Vector Drawables (Phase 2)
- `IconRegistry` zur Auflösung von `iconRef` zu Drawable-Ressourcen
- `ActivityColors`-Liste aus dem Theme
- Einstellungen: Kindname (wird in Phase 6 implementiert, hier optional vorbereiten)

---

## AP-07: PDF-Export (Ablaufplan)

### 7.1 Design-Spezifikation des PDFs

Das generierte PDF soll aussehen wie die Routinekarten-Produkte auf Etsy (vgl. Referenzbilder im Projekt). Es ist kein technisches Dokument, sondern ein kindgerecht gestaltetes Blatt zum Ausdrucken und Aufhängen.

**Seitenformat:** A4 Hochformat (210 × 297 mm = 595 × 842 Punkte bei 72 dpi)

**Layout-Aufbau:**

```
┌─────────────────────────────────────────┐
│          ═══════════════════            │
│              TAGESPLAN                   │  Titel (Plan-Name)
│           ~ für [Kindname] ~             │  Optional: Kindname aus Einstellungen
│          ═══════════════════            │
│                                         │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │         │  │         │  │         │ │
│  │  Icon   │  │  Icon   │  │  Icon   │ │  3-Spalten-Grid
│  │         │  │         │  │         │ │
│  │  ─ 1 ─  │  │  ─ 2 ─  │  │  ─ 3 ─  │ │  Nummerierung
│  │  Name   │  │  Name   │  │  Name   │ │  Aktivitätsname
│  └─────────┘  └─────────┘  └─────────┘ │
│                                         │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │         │  │         │  │         │ │
│  │  Icon   │  │  Icon   │  │  Icon   │ │
│  │         │  │         │  │         │ │
│  │  ─ 4 ─  │  │  ─ 5 ─  │  │  ─ 6 ─  │ │
│  │  Name   │  │  Name   │  │  Name   │ │
│  └─────────┘  └─────────┘  └─────────┘ │
│                                         │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐ │
│  │         │  │         │  │         │ │
│  │  Icon   │  │  Icon   │  │  Icon   │ │
│  │         │  │         │  │         │ │
│  │  ─ 7 ─  │  │  ─ 8 ─  │  │  ─ 9 ─  │ │
│  │  Name   │  │  Name   │  │  Name   │ │
│  └─────────┘  └─────────┘  └─────────┘ │
│                                         │
│               RoutineHeld               │  Dezenter Footer
└─────────────────────────────────────────┘
```

### 7.2 Maße und Abstände (in PDF-Punkten, 1pt = 1/72 Zoll)

Alle Maße sind absolut in Punkten definiert, unabhängig von der Geräte-DPI:

```
Seitenbreite:        595 pt
Seitenhöhe:          842 pt
Seitenränder:        40 pt (links, rechts, oben, unten)
Nutzbare Breite:     515 pt (595 - 2×40)
Nutzbare Höhe:       762 pt (842 - 2×40)

Titel-Bereich:       80 pt Höhe
  Plan-Name:         Schriftgröße 28pt, Bold, zentriert
  Kindname:          Schriftgröße 16pt, Regular, zentriert, 8pt unter Plan-Name

Karten-Grid:
  Spalten:           3 (bei ≤ 9 Aktivitäten) oder 4 (bei 10-12 Aktivitäten)
  Spaltenabstand:    12 pt
  Zeilenabstand:     12 pt
  Kartenbreite:      (Nutzbare Breite - (Spalten-1) × Spaltenabstand) / Spalten
                     3 Spalten: (515 - 24) / 3 ≈ 163 pt
                     4 Spalten: (515 - 36) / 4 ≈ 119 pt
  Kartenhöhe:        Kartenbreite × 1.15 (leicht hochformatig)
  Kartenradius:      8 pt (abgerundete Ecken)

Icon in Karte:
  Größe:             Kartenbreite × 0.55
  Position:          Zentriert, 12pt vom oberen Kartenrand

Nummer:
  Schriftgröße:      14pt, Bold
  Position:          Zentriert, unter dem Icon, 6pt Abstand

Aktivitätsname:
  Schriftgröße:      12pt (3 Spalten) oder 10pt (4 Spalten), Regular
  Position:          Zentriert, unter der Nummer, 4pt Abstand
  Max. Zeilen:       2 (mit Ellipse bei Überlauf)

Footer:
  Text:              "RoutineHeld" in 8pt, Light, zentriert, 10pt vom unteren Seitenrand
```

### 7.3 Farbschema

Das PDF verwendet das aktive Farbthema der App. Für den MVP gibt es nur das „Honig"-Theme:

```
Seitenhintergrund:     #F2EEEB (warmes Off-White)
Karten-Hintergrund:    #FFFBF8 (helles Weiß)
Karten-Rahmen:         #E8D4C0 (sanftes Beige), 1pt Stärke
Titel-Farbe:           #442B1A (warmes Dunkelbraun)
Nummer-Kreis:          #E8A87C (Primary/Pfirsich), Textfarbe #FFFFFF
Name-Farbe:            #5C3D2E (Dunkelbraun)
Footer-Farbe:          #A89080 (gedämpftes Braun)

Falls die Aktivität eine eigene Farbe hat:
  Karten-Hintergrund wird mit der ActivityColor eingefärbt (30% Opacity)
  Karten-Rahmen wird in der ActivityColor gezeichnet (80% Opacity)
```

### 7.4 Seitenumbruch-Logik

```
3 Spalten:
  ≤ 9 Aktivitäten:  1 Seite (3 Reihen)
  10-12 Aktivitäten: 1 Seite (4 Reihen, Karten werden leicht kleiner)

4 Spalten:
  ≤ 12 Aktivitäten: 1 Seite (3 Reihen)
```

Da der MVP auf maximal 12 Slots beschränkt ist, wird in der Regel nur eine Seite benötigt. Die Logik sollte dennoch auf mehrere Seiten vorbereitet sein (für spätere Erweiterung): Berechne, wie viele Reihen auf eine Seite passen, und starte eine neue Seite wenn nötig.

### 7.5 Technische Implementierung

**Datei:** `util/pdf/RoutinePlanPdfGenerator.kt`

Verwende die Android-native `PdfDocument`-API. Diese API arbeitet mit einem `Canvas`-Objekt, auf das direkt gezeichnet wird.

```kotlin
class RoutinePlanPdfGenerator(
    private val context: Context
) {
    data class PdfConfig(
        val childName: String? = null,
        val showFooter: Boolean = true
    )

    fun generate(
        plan: RoutinePlanWithEntries,
        activities: Map<Long, ActivityEntity>,  // activityId -> Entity
        config: PdfConfig = PdfConfig()
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawBackground(canvas)
        drawTitle(canvas, plan.plan.name, config.childName)
        drawActivityCards(canvas, plan, activities)
        drawFooter(canvas)

        document.finishPage(page)

        val file = File(context.cacheDir, "routineheld_${plan.plan.name.sanitize()}.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        return file
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#F2EEEB"))
    }

    private fun drawTitle(canvas: Canvas, planName: String, childName: String?) {
        val paint = Paint().apply {
            color = Color.parseColor("#442B1A")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
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

        val columns = if (filledEntries.size <= 9) 3 else 4
        val startY = 120f // Unter dem Titel
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
        // 1. Karten-Hintergrund (abgerundetes Rechteck)
        val rect = RectF(x, y, x + width, y + height)
        val bgPaint = Paint().apply {
            color = Color.parseColor("#FFFBF8")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        // Falls Aktivität eine Farbe hat: Hintergrund einfärben
        // activity.color?.let { colorIndex -> ... }
        canvas.drawRoundRect(rect, 8f, 8f, bgPaint)

        // 2. Karten-Rahmen
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E8D4C0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

        // 3. Icon zeichnen (Vector Drawable -> Bitmap -> Canvas)
        val iconSize = (width * 0.55f).toInt()
        val iconX = x + (width - iconSize) / 2
        val iconY = y + 12f
        drawIcon(canvas, activity.iconRef, iconX, iconY, iconSize)

        // 4. Nummer in farbigem Kreis
        val circleRadius = 11f
        val circleY = iconY + iconSize + 16f
        val circlePaint = Paint().apply {
            color = Color.parseColor("#E8A87C")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(x + width / 2, circleY, circleRadius, circlePaint)
        val numPaint = Paint().apply {
            color = Color.WHITE
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("$number", x + width / 2, circleY + 5f, numPaint)

        // 5. Aktivitätsname
        val namePaint = Paint().apply {
            color = Color.parseColor("#5C3D2E")
            textSize = if (width > 140) 12f else 10f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val nameY = circleY + circleRadius + 14f
        drawTextWrapped(canvas, activity.name, x + width / 2, nameY, width - 8f, namePaint, 2)
    }

    private fun drawIcon(canvas: Canvas, iconRef: String, x: Float, y: Float, size: Int) {
        val resId = IconRegistry.getDrawableRes(context, iconRef)
        if (resId == 0) return // Fallback: nichts zeichnen oder Platzhalter

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
        // Implementiere mehrzeiligen Text mit StaticLayout oder manueller Wortumbruch-Logik
        val layout = StaticLayout.Builder.obtain(text, 0, text.length, TextPaint(paint), maxWidth.toInt())
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()

        canvas.save()
        canvas.translate(centerX - maxWidth / 2, startY)
        layout.draw(canvas)
        canvas.restore()
    }

    private fun String.sanitize(): String =
        this.replace(Regex("[^a-zA-Z0-9äöüÄÖÜß_-]"), "_").take(50)
}
```

### 7.6 PDF-Export-Flow

**Datei:** `util/pdf/PdfExportManager.kt`

Der Export-Flow besteht aus drei Schritten:

```kotlin
class PdfExportManager @Inject constructor(
    private val context: Context,
    private val pdfGenerator: RoutinePlanPdfGenerator
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
                // 1. PDF generieren
                val config = RoutinePlanPdfGenerator.PdfConfig(childName = childName)
                val tempFile = pdfGenerator.generate(plan, activities, config)

                // 2. In öffentlichen Downloads-Ordner kopieren
                val publicFile = copyToDownloads(tempFile, plan.plan.name)

                // 3. Content-URI über FileProvider erstellen (für Share)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    publicFile
                )

                ExportResult.Success(publicFile, uri)
            } catch (e: Exception) {
                ExportResult.Error(e.message ?: "Unbekannter Fehler beim PDF-Export")
            }
        }
    }

    private fun copyToDownloads(sourceFile: File, planName: String): File {
        val fileName = "RoutineHeld_${planName.sanitize()}_${System.currentTimeMillis()}.pdf"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Scoped Storage: Verwende MediaStore
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

        return sourceFile // Rückgabe der Temp-Datei für FileProvider
    }
}
```

### 7.7 FileProvider-Konfiguration

Erstelle die FileProvider-Konfiguration für den Share-Intent:

**Datei:** `res/xml/file_paths.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <cache-path name="pdf_exports" path="." />
</paths>
```

**In AndroidManifest.xml:**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### 7.8 Share-Intent

Nach erfolgreichem Export wird ein Share-Intent angeboten:

```kotlin
fun sharePdf(context: Context, uri: Uri, planName: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "Ablaufplan: $planName")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Plan teilen"))
}
```

Der Share-Dialog bietet automatisch folgende Optionen an: Drucken, WhatsApp, E-Mail, Google Drive, Dateien-App etc.

### 7.9 Integration in den Plan-Editor und die Übersicht

Beide Screens (PlanEditorScreen und PlansScreen) rufen den Export über das ViewModel aus:

```kotlin
// Im PlanEditorViewModel oder PlansViewModel
fun exportAsPdf(planId: Long) {
    viewModelScope.launch {
        _uiState.update { it.copy(isExporting = true) }

        val plan = planRepository.getPlanWithEntriesOnce(planId)
        if (plan == null) {
            _uiState.update { it.copy(isExporting = false, exportError = "Plan nicht gefunden") }
            return@launch
        }

        val activityMap = plan.entries
            .mapNotNull { entry -> activityRepository.getById(entry.activityId) }
            .associateBy { it.id }

        val result = pdfExportManager.exportPlan(plan, activityMap, childName = null)

        when (result) {
            is PdfExportManager.ExportResult.Success -> {
                _uiState.update {
                    it.copy(isExporting = false, exportedFileUri = result.uri)
                }
            }
            is PdfExportManager.ExportResult.Error -> {
                _uiState.update {
                    it.copy(isExporting = false, exportError = result.message)
                }
            }
        }
    }
}
```

### 7.10 Ladeindikator

Während der PDF-Generierung (typischerweise < 1 Sekunde) zeige eine Ladeanimation:

```kotlin
if (uiState.isExporting) {
    AlertDialog(
        onDismissRequest = { },  // Nicht abbrechen während Export
        confirmButton = { },
        icon = { CircularProgressIndicator() },
        text = { Text("PDF wird erstellt…") }
    )
}
```

---

## Abnahmekriterien Phase 4

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Der Export erzeugt ein valides PDF-Dokument (öffnet sich in PDF-Viewer ohne Fehler) | Manueller Test |
| 2 | Das PDF ist im A4-Hochformat (595 × 842 pt) | PDF-Metadaten prüfen |
| 3 | Der Plan-Titel wird zentriert oben angezeigt | Visueller Test |
| 4 | Alle zugewiesenen Aktivitäten werden als nummerierte Karten mit Icon und Name dargestellt | Visueller Test |
| 5 | Icons sind scharf und erkennbar (nicht pixelig oder abgeschnitten) | Visueller Test auf verschiedenen Geräten |
| 6 | Das Layout passt sich der Aktivitätenanzahl an (3 oder 4 Spalten) | Test mit 6, 9, 10, 12 Aktivitäten |
| 7 | Das Farbschema entspricht dem „Honig"-Theme | Visueller Test |
| 8 | Nach dem Export wird ein Share-Dialog angeboten | Manueller Test |
| 9 | Das PDF wird im Downloads-Ordner gespeichert | Dateisystem-Prüfung |
| 10 | Der Export dauert maximal 3 Sekunden | Zeitmessung |
| 11 | Ein Ladeindikator wird während des Exports angezeigt | Manueller Test |
| 12 | Der Export-Button ist sowohl im Plan-Editor als auch in der Plan-Übersicht erreichbar | Manueller Test |
| 13 | Leere Slots (ohne Aktivität) werden im PDF nicht dargestellt | Test mit teilweise gefülltem Plan |

---

## Hinweise für den AI-Agenten

- **PDF-Canvas vs. Compose:** Die PDF-Generierung verwendet den Android-nativen `Canvas`, nicht Compose. Zeichenoperationen sind imperativer Natur (`drawRect`, `drawText`, `drawBitmap`). VectorDrawables müssen zuerst in ein `Drawable`-Objekt konvertiert und dann über `drawable.draw(canvas)` gerendert werden.
- **Textumbruch:** Verwende `StaticLayout` (oder `StaticLayout.Builder`) für mehrzeiligen, zentrierten Text mit Ellipsis. Einfaches `canvas.drawText()` unterstützt keinen Zeilenumbruch.
- **Farbkonvertierung:** `android.graphics.Color` (int-basiert) ≠ `androidx.compose.ui.graphics.Color`. Im PDF-Generator arbeite durchgängig mit `android.graphics.Color`.
- **Testen des PDFs:** Öffne das generierte PDF auf dem Emulator mit einem PDF-Viewer. Prüfe auch den Druck-Dialog (über Share → Drucken), da manche Layout-Probleme erst beim Drucken sichtbar werden.
- **FileProvider ist Pflicht:** Ab Android 7.0 (API 24) können Datei-URIs nicht mehr direkt in Intents übergeben werden. Der `FileProvider` ist notwendig, damit andere Apps die PDF-Datei lesen können.
- **Leere Slots überspringen:** Im PDF werden nur gefüllte Slots gerendert. Wenn ein Plan 8 Slots hat, aber nur 5 gefüllt sind, zeigt das PDF 5 Karten.
