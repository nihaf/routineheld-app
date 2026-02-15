# Phase 5: Wochenplan — Erstellung und PDF-Export

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-08 (Wochenplan-Erstellung), AP-09 (PDF-Export Wochenplan) |
| Abhängigkeiten | Phase 1–4 vollständig (insbesondere Ablaufpläne, Aktivitäten, PDF-Engine) |
| Ergebnis | Matrix-basierter Wochenplan mit Zuordnung von Abläufen/Aktivitäten und PDF-Export im Querformat |
| Geschätzte Story Points | 18 |

## Voraussetzungen

Folgende Komponenten müssen existieren und funktionieren:

- `WeekPlanEntity`, `WeekPlanSlotEntity` mit DAOs und Repository (Phase 1)
- Ablaufpläne mit zugewiesenen Aktivitäten (Phase 3)
- `RoutinePlanPdfGenerator` als Vorlage für die PDF-Engine (Phase 4)
- Aktivitäten mit Icons (Phase 2)
- Navigation mit Tab „Wochenplan" (Phase 1)

---

## AP-08: Wochenplan-Erstellung

### 8.1 Datenmodell-Kontext

Ein Wochenplan ist eine 7×3-Matrix (7 Wochentage × 3 Tageszeiten). Jede Zelle kann entweder einen bestehenden Ablaufplan referenzieren oder eine einzelne Aktivität enthalten. Die `WeekPlanSlotEntity` speichert pro Zelle entweder eine `routinePlanId` oder eine `activityId` (nie beides gleichzeitig, beide nullable).

```
             Mo    Di    Mi    Do    Fr    Sa    So
Morgens    [ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ]
Mittags    [ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ]
Abends     [ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ][ Slot ]
```

Enums für die Achsen:

```kotlin
enum class DayOfWeek(val index: Int, val labelDE: String, val shortDE: String) {
    MONDAY(1, "Montag", "Mo"),
    TUESDAY(2, "Dienstag", "Di"),
    WEDNESDAY(3, "Mittwoch", "Mi"),
    THURSDAY(4, "Donnerstag", "Do"),
    FRIDAY(5, "Freitag", "Fr"),
    SATURDAY(6, "Samstag", "Sa"),
    SUNDAY(7, "Sonntag", "So")
}

enum class TimeOfDay(val index: Int, val labelDE: String) {
    MORNING(0, "Morgens"),
    AFTERNOON(1, "Mittags"),
    EVENING(2, "Abends")
}
```

### 8.2 Wochenplan-Übersicht

Bevor die Matrix angezeigt wird, braucht der Nutzer eine Übersicht seiner Wochenpläne (er kann mehrere haben, z.B. „Normalwoche" und „Ferienplan").

**Datei:** `ui/weekplan/WeekPlanListScreen.kt`

Falls noch kein Wochenplan existiert, zeige den Leerzustand mit CTA „Ersten Wochenplan erstellen". Falls genau ein Wochenplan existiert, kann dieser direkt geöffnet werden. Falls mehrere existieren, zeige eine Liste mit Karten (analog zur Plan-Übersicht in Phase 3).

**Erstellungsdialog:**

```
┌──────────────────────────────────┐
│ Neuen Wochenplan erstellen       │
├──────────────────────────────────┤
│                                  │
│  Name: [________________________]│  z.B. "Normalwoche", "Ferien"
│                                  │
│  [Abbrechen]          [Erstellen]│
└──────────────────────────────────┘
```

### 8.3 Matrix-Ansicht (Hauptscreen)

Der Kern des Wochenplan-Features ist die Matrix-Ansicht. Sie zeigt alle 21 Slots (7×3) in einer scrollbaren Tabelle.

**Datei:** `ui/weekplan/editor/WeekPlanEditorScreen.kt`
**Datei:** `ui/weekplan/editor/WeekPlanEditorViewModel.kt`

#### Layout-Konzept

```
┌──────────────────────────────────────────────────────────┐
│ ← Normalwoche                                      [⋮]  │
├────────┬───────┬───────┬───────┬───────┬───────┬───────┬─┤
│        │  Mo   │  Di   │  Mi   │  Do   │  Fr   │  Sa ► │  horizontal scrollbar
├────────┼───────┼───────┼───────┼───────┼───────┼───────┤
│Morgens │ ☀️🦷👕│ ☀️🦷👕│  [+]  │ ☀️🦷👕│ ☀️🦷👕│  [+]  │
│        │Morgen-│Morgen-│       │Morgen-│Morgen-│       │
│        │routine│routine│       │routine│routine│       │
├────────┼───────┼───────┼───────┼───────┼───────┼───────┤
│Mittags │  🍽️   │  🍽️   │  [+]  │  🍽️   │  🍽️   │  [+]  │
│        │Mittag-│Mittag-│       │Mittag-│Mittag-│       │
│        │essen  │essen  │       │essen  │essen  │       │
├────────┼───────┼───────┼───────┼───────┼───────┼───────┤
│Abends  │ 🛁📖🌙│ 🛁📖🌙│  [+]  │ 🛁📖🌙│ 🛁📖🌙│  [+]  │
│        │Abend- │Abend- │       │Abend- │Abend- │       │
│        │routine│routine│       │routine│routine│       │
└────────┴───────┴───────┴───────┴───────┴───────┴───────┘
         ↑ fixiert                ↑ scrollbar ──────────→
```

#### Technische Umsetzung der Matrix

Die Matrix besteht aus einer fixierten Zeilen-Header-Spalte (Tageszeiten) und einem horizontal scrollbaren Bereich für die Tages-Spalten. Verwende eine Kombination aus `Row` und `LazyRow` oder eine custom Scroll-Lösung:

```kotlin
@Composable
fun WeekPlanMatrix(
    slots: Map<Pair<Int, Int>, WeekPlanSlotEntity>,  // (dayOfWeek, timeOfDay) -> Slot
    plans: Map<Long, RoutinePlanWithEntries>,          // planId -> Plan mit Entries
    activities: Map<Long, ActivityEntity>,              // activityId -> Activity
    onSlotClick: (dayOfWeek: Int, timeOfDay: Int) -> Unit,
    onSlotLongPress: (dayOfWeek: Int, timeOfDay: Int) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(modifier = Modifier.fillMaxSize()) {
        // Fixierte Spalte: Tageszeiten-Header
        Column(
            modifier = Modifier.width(80.dp)
        ) {
            // Leere Ecke oben links
            Spacer(modifier = Modifier.height(48.dp))

            TimeOfDay.entries.forEach { time ->
                TimeOfDayHeader(
                    label = time.labelDE,
                    modifier = Modifier.height(CELL_HEIGHT)
                )
            }
        }

        // Scrollbarer Bereich: Tage
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .fillMaxWidth()
        ) {
            DayOfWeek.entries.forEach { day ->
                Column(modifier = Modifier.width(CELL_WIDTH)) {
                    // Tag-Header
                    DayHeader(
                        label = day.shortDE,
                        modifier = Modifier.height(48.dp)
                    )

                    // 3 Zellen pro Tag
                    TimeOfDay.entries.forEach { time ->
                        val key = Pair(day.index, time.index)
                        val slot = slots[key]

                        WeekPlanCell(
                            slot = slot,
                            plan = slot?.routinePlanId?.let { plans[it] },
                            activity = slot?.activityId?.let { activities[it] },
                            onClick = { onSlotClick(day.index, time.index) },
                            onLongPress = { onSlotLongPress(day.index, time.index) },
                            modifier = Modifier.height(CELL_HEIGHT)
                        )
                    }
                }
            }
        }
    }
}

private val CELL_WIDTH = 110.dp
private val CELL_HEIGHT = 120.dp
```

#### Zellen-Darstellung

**Datei:** `ui/weekplan/editor/WeekPlanCell.kt`

Jede Zelle hat drei mögliche Zustände:

**Leer (kein Slot oder Slot ohne Zuweisung):**
- Gestrichelter Rahmen, „+"-Icon zentriert
- Hintergrund: leicht transparent `SurfaceVariant`
- Tap → Zuweisungs-Dialog

**Ablaufplan zugewiesen (routinePlanId gesetzt):**
- Solider Rahmen, `Surface`-Hintergrund
- Oben: Bis zu 3 Icons der Aktivitäten im Plan (kleine 24dp-Icons in einer Reihe)
- Unten: Name des Ablaufplans (klein, 1–2 Zeilen)
- Falls mehr als 3 Aktivitäten: „+X" Badge
- Tap → Zuweisungs-Dialog (zum Ändern)
- Long Press → Menü (Bearbeiten, Entfernen)

**Einzelne Aktivität zugewiesen (activityId gesetzt):**
- Solider Rahmen, Hintergrund in der Aktivitätsfarbe (falls vorhanden, sonst Surface)
- Zentriert: Icon (36dp) + Name darunter
- Tap → Zuweisungs-Dialog
- Long Press → Menü (Bearbeiten, Entfernen)

### 8.4 Zuweisungs-Dialog

Wenn der Nutzer auf eine Zelle tippt, öffnet sich ein BottomSheet mit zwei Optionen:

**Datei:** `ui/weekplan/editor/SlotAssignSheet.kt`

```
┌──────────────────────────────────┐
│ Dienstag · Morgens               │  Kontext-Info
├──────────────────────────────────┤
│                                  │
│  ○ Ablaufplan zuweisen           │  Option 1
│    Liste: [Morgenroutine    ▾]   │  Dropdown mit verfügbaren Plänen
│                                  │
│  ○ Einzelne Aktivität            │  Option 2
│    [Icon-Grid der Aktivitäten]   │  Tap-Auswahl
│                                  │
│  [Abbrechen]          [Zuweisen] │
└──────────────────────────────────┘
```

**Verhalten:**
- Der Nutzer wählt entweder einen Ablaufplan aus einer Dropdown-Liste oder tippt auf eine einzelne Aktivität.
- Bei „Ablaufplan zuweisen" wird `routinePlanId` gesetzt, `activityId` wird null.
- Bei „Einzelne Aktivität" wird `activityId` gesetzt, `routinePlanId` wird null.
- „Zuweisen" speichert den Slot sofort in der Datenbank.
- Ist die Zelle bereits belegt, wird die bestehende Zuweisung überschrieben.

### 8.5 Long-Press-Menü

Bei Long-Press auf eine belegte Zelle erscheint ein Popup-Menü:

| Aktion | Beschreibung |
|--------|-------------|
| Ändern | Öffnet den Zuweisungs-Dialog mit der aktuellen Auswahl vorselektiert |
| Auf ganze Woche kopieren | Kopiert diese Zuweisung auf den gleichen Tageszeit-Slot aller 7 Tage |
| Entfernen | Löscht den Slot (Zelle wird leer) |

Die Option „Auf ganze Woche kopieren" ist ein Effizienz-Feature: Wenn die Morgenroutine jeden Tag gleich ist, muss der Nutzer sie nur einmal zuweisen und kann sie dann auf alle Tage kopieren.

```kotlin
fun copySlotToWholeWeek(weekPlanId: Long, sourceDay: Int, timeOfDay: Int) {
    viewModelScope.launch {
        val sourceSlot = weekPlanRepository.getSlot(weekPlanId, sourceDay, timeOfDay) ?: return@launch

        DayOfWeek.entries.forEach { day ->
            if (day.index != sourceDay) {
                weekPlanRepository.upsertSlot(
                    WeekPlanSlotEntity(
                        weekPlanId = weekPlanId,
                        dayOfWeek = day.index,
                        timeOfDay = timeOfDay,
                        routinePlanId = sourceSlot.routinePlanId,
                        activityId = sourceSlot.activityId
                    )
                )
            }
        }
    }
}
```

### 8.6 WeekPlanEditorViewModel

```kotlin
@HiltViewModel
class WeekPlanEditorViewModel @Inject constructor(
    private val weekPlanRepository: WeekPlanRepository,
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val weekPlanId: Long = savedStateHandle.get<Long>("weekPlanId")
        ?: throw IllegalArgumentException("weekPlanId required")

    // Wochenplan mit Slots als reaktiver Flow
    val weekPlanWithSlots: StateFlow<WeekPlanWithSlots?> =
        weekPlanRepository.getWeekPlanWithSlots(weekPlanId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Alle Ablaufpläne (für die Auswahl im Zuweisungs-Dialog)
    val availablePlans: StateFlow<List<RoutinePlanWithEntries>> =
        planRepository.getAllPlansWithEntries()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Alle Aktivitäten (für Einzelzuweisung und Icon-Darstellung)
    val allActivities: StateFlow<List<ActivityEntity>> =
        activityRepository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Convenience: Slots als Map für schnellen Zugriff
    val slotMap: StateFlow<Map<Pair<Int, Int>, WeekPlanSlotEntity>> =
        weekPlanWithSlots.map { weekPlan ->
            weekPlan?.slots?.associateBy { Pair(it.dayOfWeek, it.timeOfDay) } ?: emptyMap()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun assignPlanToSlot(dayOfWeek: Int, timeOfDay: Int, planId: Long) { ... }
    fun assignActivityToSlot(dayOfWeek: Int, timeOfDay: Int, activityId: Long) { ... }
    fun clearSlot(dayOfWeek: Int, timeOfDay: Int) { ... }
    fun copySlotToWholeWeek(sourceDay: Int, timeOfDay: Int) { ... }
}
```

### 8.7 Navigation

Die Navigation für den Wochenplan-Bereich:

```kotlin
// Neue Routes in Screen sealed interface
@Serializable data class WeekPlanEditor(val weekPlanId: Long) : Screen
```

Flow: Tab „Wochenplan" → WeekPlanListScreen → (Auswahl oder Erstellen) → WeekPlanEditorScreen.

Falls nur ein Wochenplan existiert, kann der ListScreen übersprungen und direkt der Editor angezeigt werden (mit Zurück-Möglichkeit zur Liste).

### 8.8 Tageszeit-Farbkodierung

Die drei Tageszeiten werden farblich unterschieden, sowohl in der Matrix als auch im PDF:

```kotlin
object TimeOfDayColors {
    val morningBackground = Color(0xFFFFF8E1)  // Warmes Gelb
    val morningAccent = Color(0xFFF9A825)       // Sonnengelb
    val afternoonBackground = Color(0xFFE8F5E9) // Helles Grün
    val afternoonAccent = Color(0xFF66BB6A)     // Frisches Grün
    val eveningBackground = Color(0xFFE3F2FD)   // Helles Blau
    val eveningAccent = Color(0xFF42A5F5)       // Sanftes Blau
}
```

---

## AP-09: PDF-Export (Wochenplan)

### 9.1 Design-Spezifikation

Das Wochenplan-PDF wird im **A4-Querformat** (842 × 595 pt) generiert und zeigt die 7×3-Matrix als Tabelle.

**Layout:**

```
┌───────────────────────────────────────────────────────────────────────────┐
│                        WOCHENPLAN                                         │
│                    ~ für [Kindname] ~                                      │
├────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────────┬─────┤
│        │   Mo    │   Di    │   Mi    │   Do    │   Fr    │   Sa    │ So  │
├────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────┤
│        │☀️🦷👕   │☀️🦷👕   │  —      │☀️🦷👕   │☀️🦷👕   │  —      │ —   │
│Morgens │Morgen-  │Morgen-  │         │Morgen-  │Morgen-  │         │     │
│        │routine  │routine  │         │routine  │routine  │         │     │
├────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────┤
│        │  🍽️     │  🍽️     │  —      │  🍽️     │  🍽️     │  —      │ —   │
│Mittags │Mittag-  │Mittag-  │         │Mittag-  │Mittag-  │         │     │
│        │essen    │essen    │         │essen    │essen    │         │     │
├────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────────┼─────┤
│        │🛁📖🌙   │🛁📖🌙   │  —      │🛁📖🌙   │🛁📖🌙   │  —      │ —   │
│Abends  │Abend-   │Abend-   │         │Abend-   │Abend-   │         │     │
│        │routine  │routine  │         │routine  │routine  │         │     │
├────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────────┴─────┤
│                              RoutineHeld                                  │
└───────────────────────────────────────────────────────────────────────────┘
```

### 9.2 Maße (in PDF-Punkten)

```
Seitenbreite:         842 pt (A4 Querformat)
Seitenhöhe:           595 pt
Seitenränder:         30 pt
Nutzbare Breite:      782 pt
Nutzbare Höhe:        535 pt

Titel-Bereich:        60 pt Höhe

Tabelle:
  Header-Spalte (Tageszeiten): 70 pt breit
  Tages-Spalten:      (782 - 70) / 7 ≈ 101 pt breit
  Header-Zeile (Tage): 32 pt hoch
  Daten-Zeilen:        (535 - 60 - 32 - 20) / 3 ≈ 141 pt hoch
                       (20pt für Footer)

In jeder Zelle:
  Padding:             6 pt
  Icons:               Max. 4 Icons, je 20 pt, in einer Reihe
  Plan/Aktivitätsname: 9 pt Schrift, max. 2 Zeilen
```

### 9.3 Implementierung

**Datei:** `util/pdf/WeekPlanPdfGenerator.kt`

```kotlin
class WeekPlanPdfGenerator(
    private val context: Context
) {
    fun generate(
        weekPlan: WeekPlanWithSlots,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        childName: String? = null
    ): File {
        val document = PdfDocument()
        // Querformat: Breite=842, Höhe=595
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
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

    private fun drawTable(
        canvas: Canvas,
        slots: List<WeekPlanSlotEntity>,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>
    ) {
        val slotMap = slots.associateBy { Pair(it.dayOfWeek, it.timeOfDay) }

        val tableLeft = 30f
        val tableTop = 90f   // Unter dem Titel
        val headerColWidth = 70f
        val dayColWidth = (782f - headerColWidth) / 7
        val headerRowHeight = 32f
        val dataRowHeight = 141f

        // Tage-Header zeichnen
        DayOfWeek.entries.forEach { day ->
            val x = tableLeft + headerColWidth + (day.index - 1) * dayColWidth
            drawDayHeader(canvas, day.shortDE, x, tableTop, dayColWidth, headerRowHeight)
        }

        // Tageszeiten-Zeilen
        TimeOfDay.entries.forEach { time ->
            val y = tableTop + headerRowHeight + time.index * dataRowHeight

            // Tageszeit-Header (linke Spalte)
            drawTimeHeader(canvas, time, tableLeft, y, headerColWidth, dataRowHeight)

            // 7 Zellen pro Zeile
            DayOfWeek.entries.forEach { day ->
                val x = tableLeft + headerColWidth + (day.index - 1) * dayColWidth
                val slot = slotMap[Pair(day.index, time.index)]

                drawTableCell(canvas, slot, plans, activities, x, y, dayColWidth, dataRowHeight)
            }
        }

        // Tabellenrahmen
        drawTableBorders(canvas, tableLeft, tableTop, headerColWidth, dayColWidth, headerRowHeight, dataRowHeight)
    }

    private fun drawTableCell(
        canvas: Canvas,
        slot: WeekPlanSlotEntity?,
        plans: Map<Long, RoutinePlanWithEntries>,
        activities: Map<Long, ActivityEntity>,
        x: Float, y: Float, width: Float, height: Float
    ) {
        val padding = 6f

        if (slot == null) {
            // Leere Zelle: Dezenter Platzhalter (Gedankenstrich oder leicht grauer Hintergrund)
            val emptyPaint = Paint().apply {
                color = Color.parseColor("#D0C8C0")
                textSize = 14f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("—", x + width / 2, y + height / 2 + 5f, emptyPaint)
            return
        }

        // Ablaufplan zugewiesen
        slot.routinePlanId?.let { planId ->
            val plan = plans[planId] ?: return@let
            val planActivities = plan.entries
                .sortedBy { it.position }
                .mapNotNull { activities[it.activityId] }

            // Icons zeichnen (max. 4)
            val iconSize = 20
            val iconsToShow = planActivities.take(4)
            val totalIconWidth = iconsToShow.size * (iconSize + 2)
            var iconX = x + (width - totalIconWidth) / 2

            iconsToShow.forEach { activity ->
                drawIcon(canvas, activity.iconRef, iconX, y + padding, iconSize)
                iconX += iconSize + 2
            }
            if (planActivities.size > 4) {
                val morePaint = Paint().apply {
                    color = Color.parseColor("#8B7B6B")
                    textSize = 8f
                    isAntiAlias = true
                }
                canvas.drawText("+${planActivities.size - 4}", iconX, y + padding + iconSize - 2, morePaint)
            }

            // Planname
            val namePaint = TextPaint().apply {
                color = Color.parseColor("#5C3D2E")
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

        // Einzelne Aktivität zugewiesen
        slot.activityId?.let { activityId ->
            val activity = activities[activityId] ?: return@let

            // Icon zentriert
            val iconSize = 28
            val iconX = x + (width - iconSize) / 2
            drawIcon(canvas, activity.iconRef, iconX, y + padding + 10, iconSize)

            // Name
            val namePaint = TextPaint().apply {
                color = Color.parseColor("#5C3D2E")
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

    private fun drawTimeHeader(canvas: Canvas, time: TimeOfDay, x: Float, y: Float, width: Float, height: Float) {
        // Farbiger Hintergrund je Tageszeit
        val bgColor = when (time) {
            TimeOfDay.MORNING -> Color.parseColor("#FFF8E1")
            TimeOfDay.AFTERNOON -> Color.parseColor("#E8F5E9")
            TimeOfDay.EVENING -> Color.parseColor("#E3F2FD")
        }
        val bgPaint = Paint().apply { color = bgColor; style = Paint.Style.FILL }
        canvas.drawRect(x, y, x + width, y + height, bgPaint)

        // Text vertikal zentriert
        val textPaint = Paint().apply {
            color = Color.parseColor("#442B1A")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(time.labelDE, x + width / 2, y + height / 2 + 4f, textPaint)
    }
}
```

### 9.4 Integration des Wochenplan-Exports

Der Export wird analog zum Ablaufplan-Export über das ViewModel ausgelöst:

```kotlin
fun exportWeekPlanAsPdf(weekPlanId: Long) {
    viewModelScope.launch {
        _uiState.update { it.copy(isExporting = true) }

        val weekPlan = weekPlanRepository.getWeekPlanWithSlotsOnce(weekPlanId) ?: return@launch

        // Alle referenzierten Pläne und Aktivitäten laden
        val planIds = weekPlan.slots.mapNotNull { it.routinePlanId }.distinct()
        val plans = planIds.associateWith { planRepository.getPlanWithEntriesOnce(it) }
            .filterValues { it != null }
            .mapValues { it.value!! }

        val activityIds = mutableSetOf<Long>()
        weekPlan.slots.mapNotNull { it.activityId }.let { activityIds.addAll(it) }
        plans.values.flatMap { it.entries.map { e -> e.activityId } }.let { activityIds.addAll(it) }
        val activities = activityIds.associateWith { activityRepository.getById(it) }
            .filterValues { it != null }
            .mapValues { it.value!! }

        val result = weekPlanPdfExportManager.exportWeekPlan(weekPlan, plans, activities)
        // ... Handle result analog zu Phase 4
    }
}
```

Der `PdfExportManager` aus Phase 4 wird um eine Methode `exportWeekPlan` erweitert, die den `WeekPlanPdfGenerator` aufruft.

---

## Abnahmekriterien Phase 5

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Der Wochenplan-Tab zeigt eine Übersicht vorhandener Wochenpläne | Manueller Test |
| 2 | Ein neuer Wochenplan kann mit Name erstellt werden | Manueller Test |
| 3 | Die Matrix-Ansicht zeigt eine 7×3-Tabelle mit Tagen und Tageszeiten | Manueller Test |
| 4 | Die Tageszeiten-Spalte ist beim horizontalen Scrollen fixiert | Manueller Test |
| 5 | Tippen auf eine leere Zelle öffnet den Zuweisungs-Dialog | Manueller Test |
| 6 | Ein Ablaufplan kann einer Zelle zugewiesen werden und wird als Icon-Vorschau + Name dargestellt | Manueller Test |
| 7 | Eine einzelne Aktivität kann einer Zelle zugewiesen werden und wird als Icon + Name dargestellt | Manueller Test |
| 8 | Long-Press bietet „Ändern", „Auf ganze Woche kopieren" und „Entfernen" | Manueller Test |
| 9 | „Auf ganze Woche kopieren" dupliziert die Zuweisung auf alle 7 Tage der gleichen Tageszeit | Manueller Test |
| 10 | Alle Änderungen werden sofort in der DB gespeichert | Kill-and-Restart Test |
| 11 | Der PDF-Export erzeugt ein valides PDF im A4-Querformat | Manueller Test |
| 12 | Das PDF zeigt die 7×3-Matrix als lesbare Tabelle mit farbigen Tageszeit-Headern | Visueller Test |
| 13 | Leere Zellen im PDF zeigen einen dezenten Platzhalter (—) | Visueller Test |
| 14 | Icons im PDF sind scharf und korrekt positioniert | Visueller Test |
| 15 | Der Share-Intent funktioniert wie beim Ablaufplan-Export | Manueller Test |
| 16 | Mindestens zwei Wochenpläne können unabhängig voneinander existieren | Manueller Test |

---

## Hinweise für den AI-Agenten

- **Fixierter Header:** Die technische Herausforderung liegt im fixierten Zeilen-Header. In Compose gibt es keine native „frozen column"-Funktionalität wie in HTML-Tabellen. Die Lösung ist eine `Row` mit einer festen `Column` (nicht scrollbar) und einer scrollbaren `Row`/`LazyRow` daneben, die denselben horizontalen `ScrollState` teilen.
- **Performance:** Die Matrix hat nur 21 Zellen — Performance ist kein Problem. Eine `LazyRow` ist hier nicht nötig; eine reguläre `Row` mit `horizontalScroll` reicht aus.
- **Daten-Loading:** Der ViewModel muss drei Datenströme parallel laden (Wochenplan, alle Ablaufpläne, alle Aktivitäten). Verwende `combine` um diese zu einer einzigen UI-State-Flow zusammenzuführen, und zeige einen Ladeindikator bis alle Daten geladen sind.
- **Upsert-Logik:** `upsertSlot` muss prüfen, ob bereits ein Slot für die gegebene Kombination (weekPlanId, dayOfWeek, timeOfDay) existiert. Falls ja, wird er aktualisiert; falls nein, wird ein neuer eingefügt. Implementiere dies als Room `@Insert(onConflict = OnConflictStrategy.REPLACE)` oder als manuelles Check-then-Insert/Update in einer `@Transaction`.
- **Querformat-PDF:** Der `PdfDocument.PageInfo.Builder` wird einfach mit vertauschten Dimensionen aufgerufen (842 × 595 statt 595 × 842). Der Rest der Zeichenlogik arbeitet normal.
