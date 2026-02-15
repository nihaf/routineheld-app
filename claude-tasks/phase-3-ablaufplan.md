# Phase 3: Ablaufplan — Erstellung mit Drag & Drop und Übersicht

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-05 (Ablaufplan-Erstellung mit Drag & Drop), AP-06 (Ablaufplan-Übersicht & Verwaltung) |
| Abhängigkeiten | Phase 1 (Architektur, Datenbank), Phase 2 (Aktivitäten, Icons, IconPicker) |
| Ergebnis | Vollständige Ablaufplan-Erstellung per Drag & Drop und Verwaltungsübersicht |
| Geschätzte Story Points | 16 |
| Risiko | Hoch — Drag & Drop in Compose ist das technisch anspruchsvollste Feature |

## Voraussetzungen

Aus Phase 1 und 2 müssen folgende Komponenten existieren und funktionieren:

- `RoutinePlanEntity`, `RoutinePlanEntryEntity` mit DAOs und Repository
- `ActivityEntity` mit vollständigem CRUD
- `IconRegistry` mit mindestens 15 funktionierenden Icons
- Navigation mit Compose Navigation (Route `PlanEditor(planId)` bereits als Sealed-Class definiert)
- Theme und `ActivityColors`

---

## AP-05: Ablaufplan-Erstellung mit Drag & Drop

### 5.1 Neuen Plan erstellen — Einstiegsdialog

Bevor der Editor geöffnet wird, muss der Nutzer grundlegende Plan-Parameter festlegen. Dies geschieht über einen einfachen Dialog oder BottomSheet.

**Datei:** `ui/plans/CreatePlanDialog.kt`

```
┌──────────────────────────────────┐
│ Neuen Ablaufplan erstellen       │
├──────────────────────────────────┤
│                                  │
│  Name: [________________________]│  max 40 Zeichen
│                                  │
│  Anzahl Plätze:                  │
│     3  ────●──────────── 12      │  Slider, Integer-Schritte
│              6                   │  Aktuelle Zahl anzeigen
│                                  │
│  [Abbrechen]          [Erstellen]│
└──────────────────────────────────┘
```

**Verhalten:**
- Standard-Slot-Anzahl: 6 (konfigurierbar über Settings in Phase 6)
- Name darf nicht leer sein
- Nach „Erstellen" wird ein neuer `RoutinePlanEntity` in der Datenbank angelegt und der Editor für diesen Plan geöffnet

### 5.2 Plan-Editor — Gesamtlayout

Der Plan-Editor ist der Kernscreen der App. Er besteht aus zwei Bereichen: der Slot-Liste (Hauptbereich) und dem Aktivitäten-Auswahl-Panel.

**Datei:** `ui/plans/editor/PlanEditorScreen.kt`
**Datei:** `ui/plans/editor/PlanEditorViewModel.kt`

```
┌──────────────────────────────────┐
│ ← Morgenroutine            [⋮]  │  TopAppBar mit Plan-Name und Menü
├──────────────────────────────────┤
│                                  │
│  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │ 1  ☀️ Aufwachen            │  │  Gefüllter Slot
│  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│  ┌─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┐  │
│  │ 2  🦷 Zähne putzen         │  │  Gefüllter Slot
│  └─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─┘  │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  │
│  │ 3  [+] Aktivität wählen   │  │  Leerer Slot (gestrichelt)
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
│  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  │
│  │ 4  [+] Aktivität wählen   │  │  Leerer Slot (gestrichelt)
│  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ Aktivitäten auswählen    [▲]│ │  Ausklappbares Bottom-Panel
│ │ ☀️🦷👕🥣🏫🍽️🌳🛁📖🌙 │ │  Horizontales Grid
│ └──────────────────────────────┘ │
└──────────────────────────────────┘
```

### 5.3 Slot-Darstellung

Jeder Slot ist ein Card-Composable mit zwei Zuständen:

**Datei:** `ui/plans/editor/PlanSlot.kt`

#### Leerer Slot
- Gestrichelte Umrandung (DashedBorder via `drawBehind` + `PathEffect.dashPathEffect`)
- Nummer links (z.B. „3"), zentriertes Plus-Icon und Text „Aktivität wählen"
- Hintergrundfarbe: `SurfaceVariant` mit niedrigem Alpha
- Tap-Action: Öffnet Aktivitäten-Auswahl-Sheet

#### Gefüllter Slot
- Solide Umrandung, leicht erhöht (Elevation 2dp)
- Links: Nummer in einem farbigen Kreis
- Mitte: Icon (40dp) + Aktivitätsname
- Rechts: Drag-Handle (≡ Icon, nur sichtbar wenn nicht im Drag)
- Optional: Farbiger linker Rand wenn Aktivität eine Farbe hat
- Long-Press initiiert Drag-Reorder
- Swipe-to-remove: Wisch nach links zeigt roten „Entfernen"-Hintergrund

### 5.4 Drag & Drop — Implementierung

Dies ist das technisch anspruchsvollste Feature der App. Es gibt zwei Drag-Interaktionen:

#### Interaktion A: Aktivität auf leeren Slot ziehen (Assign)

Der Nutzer tippt auf einen leeren Slot. Es öffnet sich ein BottomSheet mit allen verfügbaren Aktivitäten (ähnlich dem IconPicker, aber hier werden Aktivitäten gezeigt, nicht Icons). Der Nutzer wählt eine Aktivität durch Tippen. Der Slot wird gefüllt.

**Hinweis:** Echtes Drag-von-unten-nach-oben ist in Compose mit BottomSheets schwierig und UX-fragwürdig auf kleinen Bildschirmen. Die Tap-Zuweisung ist die zuverlässigere Lösung für die Slot-Befüllung.

**Datei:** `ui/plans/editor/ActivityPickerSheet.kt`

```kotlin
@Composable
fun ActivityPickerSheet(
    activities: List<ActivityEntity>,
    onActivitySelected: (ActivityEntity) -> Unit,
    onDismiss: () -> Unit
)
```

Das Sheet zeigt Aktivitäten als Grid (3 Spalten) mit Icon + Name. Bereits im Plan verwendete Aktivitäten werden mit einem Häkchen markiert, sind aber weiterhin wählbar (eine Aktivität darf mehrfach im Plan vorkommen, z.B. „Hände waschen" vor und nach dem Essen).

#### Interaktion B: Reihenfolge ändern (Reorder)

Gefüllte Slots können per Long-Press + Drag in ihrer Reihenfolge verändert werden. Dies ist eine **LazyColumn-Reorder**-Implementierung.

**Empfohlene Implementierungsstrategie:**

Verwende `org.burnoutcrew:reorderable` oder die in Compose Foundation (ab 1.6) verfügbare experimentelle `LazyListItemInfo`-basierte Reorder-Logik. Falls keine stabile Library verfügbar ist, implementiere einen eigenen Reorder-Mechanismus:

```kotlin
// State für den Reorder
class ReorderState {
    var draggedIndex by mutableIntStateOf(-1)
    var targetIndex by mutableIntStateOf(-1)
    var dragOffset by mutableFloatStateOf(0f)

    fun onDragStart(index: Int) { ... }
    fun onDrag(offset: Float) { ... }
    fun onDragEnd(): Pair<Int, Int>? { ... } // Returns (from, to) or null
    fun onDragCancel() { ... }
}
```

**Visuelles Feedback beim Drag:**
- Das gedraggte Item hat erhöhte Elevation (8dp) und leichte Skalierung (1.05)
- Andere Items animieren ihre Position sanft (animateItemPlacement in LazyColumn)
- Ein halbtransparenter Platzhalter bleibt an der Ursprungsposition
- Die Nummern aktualisieren sich in Echtzeit während des Drags

**Fallback:** Falls die Drag-Reorder-Implementierung in Compose zu instabil ist, biete als Alternative „Hoch/Runter"-Buttons pro Slot an (kleine Pfeil-Icons rechts). Dies ist weniger elegant, aber zuverlässig.

### 5.5 Slot entfernen

Ein gefüllter Slot kann auf zwei Wegen geleert werden:

1. **Swipe-to-dismiss:** Wisch nach links mit rotem Hintergrund und Papierkorb-Icon. Verwendet `SwipeToDismissBox` aus Material 3.
2. **Kontextmenü:** Long-Press (wenn kein Drag aktiv) zeigt Popup-Menü mit „Entfernen" und „Ersetzen".

Nach dem Entfernen wird der Slot wieder leer (gestrichelter Rahmen). Die Nummern der verbleibenden gefüllten Slots aktualisieren sich nicht automatisch — leere Slots in der Mitte bleiben als Lücken bestehen. Der Nutzer kann sie später neu befüllen.

### 5.6 Auto-Save

Jede Änderung wird sofort in der Datenbank persistiert. Es gibt keinen expliziten Speichern-Button.

```kotlin
// Im ViewModel: Jede Mutation löst einen DB-Write aus
fun assignActivity(slotPosition: Int, activity: ActivityEntity) {
    viewModelScope.launch {
        repository.addEntry(currentPlanId, activity.id, slotPosition)
        // Plan-Timestamp aktualisieren
        repository.touchPlan(currentPlanId)
    }
}

fun reorderSlots(fromPosition: Int, toPosition: Int) {
    viewModelScope.launch {
        repository.reorderEntries(currentPlanId, fromPosition, toPosition)
        repository.touchPlan(currentPlanId)
    }
}

fun removeFromSlot(slotPosition: Int) {
    viewModelScope.launch {
        repository.removeEntry(currentPlanId, slotPosition)
        repository.touchPlan(currentPlanId)
    }
}
```

Die Methode `touchPlan` aktualisiert `updatedAt` des Plans auf den aktuellen Timestamp.

### 5.7 PlanEditorViewModel

```kotlin
@HiltViewModel
class PlanEditorViewModel @Inject constructor(
    private val planRepository: RoutinePlanRepository,
    private val activityRepository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val planId: Long = savedStateHandle.get<Long>("planId")
        ?: throw IllegalArgumentException("planId required")

    // Plan mit Entries, reaktiv aus der DB
    val planWithEntries: StateFlow<RoutinePlanWithEntries?> =
        planRepository.getPlanWithEntries(planId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Alle verfügbaren Aktivitäten für den Picker
    val allActivities: StateFlow<List<ActivityEntity>> =
        activityRepository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(PlanEditorUiState())
    val uiState: StateFlow<PlanEditorUiState> = _uiState.asStateFlow()

    fun assignActivity(slotPosition: Int, activity: ActivityEntity) { ... }
    fun removeFromSlot(entryId: Long) { ... }
    fun reorderSlots(fromIndex: Int, toIndex: Int) { ... }

    fun showActivityPicker(forSlotPosition: Int) { ... }
    fun hideActivityPicker() { ... }
}

data class PlanEditorUiState(
    val activeSlotPosition: Int? = null,    // Slot, für den gerade eine Aktivität gewählt wird
    val showActivityPicker: Boolean = false,
    val isReordering: Boolean = false
)
```

### 5.8 Reorder-Logik im Repository

Die Reorder-Operation muss atomar sein (Transaction), da mehrere Positionen gleichzeitig aktualisiert werden:

```kotlin
// In RoutinePlanRepository
suspend fun reorderEntries(planId: Long, fromPosition: Int, toPosition: Int) {
    planDao.reorderEntries(planId, fromPosition, toPosition)
}

// Im DAO (Room @Transaction)
@Transaction
suspend fun reorderEntries(planId: Long, fromPos: Int, toPos: Int) {
    // 1. Hole alle Entries des Plans sortiert nach Position
    // 2. Entferne das Element an fromPos
    // 3. Füge es an toPos ein
    // 4. Aktualisiere alle Positionen sequentiell
    // Alternativ: Verwende temporäre negative Positionen, um Unique-Constraint nicht zu verletzen
}
```

**Wichtig:** Da `(planId, position)` ein Unique-Index ist, müssen beim Reorder zuerst alle Positionen in einen temporären Bereich verschoben werden (z.B. Position + 1000), dann neu zugewiesen werden.

### 5.9 Menü im TopAppBar

Das 3-Punkt-Menü im Editor bietet:
- **Plan umbenennen** → Dialog mit aktuellen Namen
- **Plätze hinzufügen** → Erhöht `slotCount` um 1 (max. 12)
- **Platz entfernen** → Verringert `slotCount` um 1 (min. 3, nur wenn letzter Slot leer)
- **Plan als PDF exportieren** → Leitet zu Phase 4 weiter (vorerst als Platzhalter)

---

## AP-06: Ablaufplan-Übersicht und -Verwaltung

### 6.1 Screen-Aufbau

Der Übersichtsscreen ersetzt den Platzhalter im Tab „Abläufe" und zeigt alle erstellten Pläne.

**Datei:** `ui/plans/PlansScreen.kt`
**Datei:** `ui/plans/PlansViewModel.kt`

```
┌──────────────────────────────────┐
│ Ablaufpläne              [🔍]   │  TopAppBar mit Suchfeld
├──────────────────────────────────┤
│                                  │
│  ┌──────────────────────────────┐│
│  │ Morgenroutine                ││
│  │ ☀️🦷👕🥣  ·  8 Aktivitäten  ││  Plan-Karte
│  │ Erstellt: 14.02.2026         ││
│  └──────────────────────────────┘│
│                                  │
│  ┌──────────────────────────────┐│
│  │ Abendroutine                 ││
│  │ 🛁👕📖🌙  ·  6 Aktivitäten  ││
│  │ Erstellt: 14.02.2026         ││
│  └──────────────────────────────┘│
│                                  │
│                          [FAB +] │
└──────────────────────────────────┘
```

### 6.2 Plan-Karte

**Datei:** `ui/plans/PlanCard.kt`

Jede Karte zeigt:
- **Oben:** Planname (fett, `titleMedium`)
- **Mitte:** Vorschau-Icons der ersten 4 zugewiesenen Aktivitäten als kleine Icons (28dp) in einer Reihe. Falls mehr als 4 vorhanden: „+3" Badge nach dem 4. Icon.
- **Unten:** Metadaten: „X Aktivitäten · Erstellt am DD.MM.YYYY"
- **Rechts oben:** 3-Punkt-Menü-Icon

Für Vorlagen (isTemplate = true): Zusätzliches Badge „Vorlage" in SecondaryContainer-Farbe.

### 6.3 Kontextmenü pro Plan

Das 3-Punkt-Menü bietet folgende Aktionen:

| Aktion | Beschreibung |
|--------|--------------|
| Bearbeiten | Navigiert zum PlanEditor (AP-05) |
| Duplizieren | Erstellt eine Kopie mit Suffix „ (Kopie)" inkl. aller Entries |
| Als PDF exportieren | Platzhalter bis Phase 4 → dann PDF-Export |
| Löschen | Bestätigungsdialog, dann endgültiges Löschen |

**Duplizieren-Logik im Repository:**

```kotlin
suspend fun duplicatePlan(planId: Long): Long {
    val original = planDao.getPlanWithEntriesOnce(planId) ?: return -1
    val newPlan = original.plan.copy(
        id = 0,
        name = "${original.plan.name} (Kopie)",
        isTemplate = false,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
    val newPlanId = planDao.insert(newPlan)
    val newEntries = original.entries.map {
        it.copy(id = 0, planId = newPlanId)
    }
    planDao.insertEntries(newEntries)
    return newPlanId
}
```

### 6.4 Suchfunktion

Die Suchleiste filtert Pläne nach Name. Die Implementierung erfolgt über einen `searchQuery`-State im ViewModel, der die Plan-Liste filtert:

```kotlin
val searchQuery = MutableStateFlow("")

val filteredPlans: StateFlow<List<RoutinePlanWithEntries>> = combine(
    planRepository.getAllPlansWithEntries(),
    searchQuery
) { plans, query ->
    if (query.isBlank()) plans
    else plans.filter { it.plan.name.contains(query, ignoreCase = true) }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
```

### 6.5 Leerer Zustand

Wenn keine Pläne vorhanden sind, wird angezeigt:
- Große Illustration oder dezentes Kalender-Icon
- Text: „Noch keine Ablaufpläne vorhanden"
- Subtext: „Erstelle deinen ersten Ablaufplan und gestalte den Alltag deines Kindes"
- CTA-Button: „Ersten Plan erstellen" → Öffnet CreatePlanDialog

### 6.6 Navigation

- **Tab „Abläufe" → PlansScreen** (Übersicht)
- **FAB oder CTA → CreatePlanDialog** → nach Erstellen → **PlanEditorScreen(planId)**
- **Tap auf Karte → PlanEditorScreen(planId)** (bestehender Plan bearbeiten)
- **Zurück-Button im Editor → PlansScreen**

Die Navigation zum Editor erfolgt über die in Phase 1 definierte Route:

```kotlin
navController.navigate(Screen.PlanEditor(planId = newPlanId))
```

---

## Abnahmekriterien Phase 3

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Ein neuer Plan kann mit Name (max. 40 Zeichen) und Slot-Anzahl (3–12) erstellt werden | Manueller Test |
| 2 | Der Plan-Editor zeigt nummerierte Slots an: leere Slots gestrichelt, gefüllte Slots mit Icon + Name | Manueller Test |
| 3 | Tippen auf einen leeren Slot öffnet den Aktivitäten-Picker als BottomSheet | Manueller Test |
| 4 | Auswahl einer Aktivität füllt den Slot und speichert sofort in der DB | Manueller Test + DB-Prüfung |
| 5 | Gefüllte Slots können per Long-Press + Drag in der Reihenfolge geändert werden (oder per Hoch/Runter-Buttons als Fallback) | Manueller Test |
| 6 | Nummern aktualisieren sich korrekt nach Reorder | Manueller Test |
| 7 | Ein gefüllter Slot kann per Swipe-to-dismiss oder Kontextmenü entfernt (geleert) werden | Manueller Test |
| 8 | Alle Änderungen werden automatisch gespeichert (Auto-Save) | Kill-and-Restart Test |
| 9 | Die Plan-Übersicht zeigt alle Pläne als Karten mit Vorschau-Icons, Name und Datum | Manueller Test |
| 10 | Tippen auf eine Karte öffnet den Plan im Editor | Manueller Test |
| 11 | Duplizieren erstellt eine exakte Kopie mit Suffix „ (Kopie)" | Manueller Test + DB-Prüfung |
| 12 | Löschen eines Plans erfordert Bestätigung und entfernt Plan + Entries | Manueller Test |
| 13 | Die Suchleiste filtert Pläne nach Name | Manueller Test |
| 14 | Der Leerzustand zeigt Platzhalter mit CTA-Button | Manueller Test |
| 15 | Konfigurationswechsel (Bildschirmdrehen) behält den Editor-Zustand bei | Manueller Test |
| 16 | Performance: 12 Slots scrollen und reagieren flüssig (kein sichtbarer Lag) | Manueller Test |

---

## Hinweise für den AI-Agenten

- **Drag & Drop Priorität:** Investiere den Großteil der Entwicklungszeit in AP-05. Wenn die Reorder-Geste zu instabil ist, implementiere die Hoch/Runter-Button-Fallback-Lösung sofort mit und mache die Geste optional aktivierbar. Der Nutzer muss in jedem Fall die Reihenfolge ändern können.
- **Reorder-Bibliothek:** Falls `sh.calvin.reorderable:reorderable` (Compose-kompatibel) verfügbar ist, verwende diese. Ansonsten implementiere einen eigenen `detectDragGesturesAfterLongPress`-Mechanismus auf der `LazyColumn`.
- **Unique-Constraint bei Reorder:** Der Index `(planId, position)` ist unique. Bei Reorder-Operationen müssen Positionen in einer Transaction atomar aktualisiert werden. Strategie: Alle Entries löschen, dann mit neuen Positionen wieder einfügen — oder temporäre Offset-Positionen verwenden.
- **ActivityPickerSheet vs. Drag:** Die Spezifikation bevorzugt Tap-Zuweisung über ein BottomSheet statt echtem Cross-Container-Drag, da dies auf Mobilgeräten zuverlässiger ist. Drag wird nur für Reorder innerhalb der Slot-Liste verwendet.
- **Duplikate erlaubt:** Eine Aktivität darf mehrfach im selben Plan vorkommen (z.B. „Hände waschen" an Position 3 und Position 7).
