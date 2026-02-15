# Phase 2: Aktivitäten-Kern — Icon-Bibliothek und Aktivitätenverwaltung

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-03 (Icon-Bibliothek & Asset-Management), AP-04 (Aktivitätenverwaltung CRUD) |
| Abhängigkeiten | Phase 1 vollständig abgeschlossen (Projekt, Architektur, Datenbank) |
| Ergebnis | Nutzer kann Aktivitäten mit Icons erstellen, bearbeiten und löschen |
| Geschätzte Story Points | 13 |

## Voraussetzungen

Vor Beginn dieser Phase müssen folgende Artefakte aus Phase 1 existieren und funktionieren:

- Room-Datenbank mit `ActivityEntity`, `ActivityDao`, `ActivityRepository`
- Hilt-DI-Konfiguration (Database- und Repository-Module)
- Navigation mit drei Tabs (Aktivitäten-Tab wird in dieser Phase befüllt)
- Theme mit `ActivityColors`-Liste (8 Farben)

---

## AP-03: Icon-Bibliothek und Asset-Management

### 3.1 Icon-Katalog

Die App enthält 50 vordefinierte Icons für typische Kinderaktivitäten. Jedes Icon wird als Android Vector Drawable (XML) bereitgestellt. Alle Icons müssen im selben visuellen Stil gehalten sein: vereinfachte, freundliche Formen mit einheitlicher Strichstärke (2dp), abgerundeten Ecken und einem begrenzten Farbspektrum.

**Empfohlene Quelle:** OpenMoji (CC BY-SA 4.0) oder eigens erstellte SVGs. Falls externe Icons verwendet werden, müssen sie als VectorDrawables konvertiert und im Stil vereinheitlicht werden. Alternativ können Material Symbols (Rounded, Filled) verwendet werden, sofern sie kindgerecht genug wirken.

#### Vollständiger Icon-Katalog

Die Icons sind in 6 Kategorien organisiert. Der `iconRef`-Wert entspricht dem Drawable-Ressourcennamen.

**Kategorie: Morgenroutine (8 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 1 | Aufwachen | `ic_wake_up` | Sonne mit Strahlen |
| 2 | Bett machen | `ic_make_bed` | Bett mit Decke |
| 3 | Auf Toilette gehen | `ic_toilet` | Toilette |
| 4 | Waschen | `ic_wash_face` | Wassertropfen/Gesicht |
| 5 | Zähne putzen | `ic_brush_teeth` | Zahnbürste |
| 6 | Haare kämmen | `ic_comb_hair` | Kamm/Bürste |
| 7 | Anziehen | `ic_get_dressed` | T-Shirt/Kleidung |
| 8 | Schuhe anziehen | `ic_shoes` | Paar Schuhe |

**Kategorie: Mahlzeiten (7 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 9 | Frühstücken | `ic_breakfast` | Müslischale |
| 10 | Mittagessen | `ic_lunch` | Teller mit Besteck |
| 11 | Abendessen | `ic_dinner` | Teller mit Dampf |
| 12 | Snack | `ic_snack` | Apfel |
| 13 | Trinken | `ic_drink` | Trinkflasche |
| 14 | Tisch decken | `ic_set_table` | Teller + Besteck-Arrangement |
| 15 | Tisch abräumen | `ic_clear_table` | Hand mit Teller |

**Kategorie: Aktivitäten & Spiel (12 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 16 | Draußen spielen | `ic_play_outside` | Baum mit Schaukel |
| 17 | Drinnen spielen | `ic_play_inside` | Bauklötze |
| 18 | Malen | `ic_paint` | Pinsel mit Farbklecks |
| 19 | Basteln | `ic_craft` | Schere + Papier |
| 20 | Buch lesen | `ic_read_book` | Offenes Buch |
| 21 | Musik hören | `ic_listen_music` | Kopfhörer |
| 22 | Ball spielen | `ic_ball` | Ball |
| 23 | Fußball | `ic_soccer` | Fußball |
| 24 | Turnen | `ic_gymnastics` | Figur in Bewegung |
| 25 | Schwimmen | `ic_swimming` | Wellen + Figur |
| 26 | Fahrrad fahren | `ic_bicycle` | Fahrrad |
| 27 | Spielplatz | `ic_playground` | Rutsche |

**Kategorie: Pflichten & Lernen (8 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 28 | Aufräumen | `ic_tidy_up` | Aufräumkiste |
| 29 | Lernen | `ic_study` | Stift + Heft |
| 30 | Hausaufgaben | `ic_homework` | Schultasche |
| 31 | Einkaufen | `ic_shopping` | Einkaufstasche |
| 32 | Hände waschen | `ic_wash_hands` | Hände unter Wasser |
| 33 | Gassi gehen | `ic_walk_dog` | Hund an Leine |
| 34 | Blumen gießen | `ic_water_plants` | Gießkanne |
| 35 | Müll rausbringen | `ic_take_trash` | Mülleimer |

**Kategorie: Orte & Wege (8 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 36 | Kindergarten | `ic_kindergarten` | Gebäude mit Kindern |
| 37 | Schule | `ic_school` | Schulgebäude |
| 38 | Oma/Opa besuchen | `ic_grandparents` | Älteres Paar |
| 39 | Freunde treffen | `ic_meet_friends` | Zwei Kinder |
| 40 | Arzt | `ic_doctor` | Stethoskop |
| 41 | Ausflug | `ic_trip` | Auto |
| 42 | Wald | `ic_forest` | Bäume |
| 43 | Zoo | `ic_zoo` | Tierfigur |

**Kategorie: Abendroutine (7 Icons)**

| # | Name (DE) | iconRef | Beschreibung |
|---|-----------|---------|--------------|
| 44 | Baden | `ic_bath` | Badewanne |
| 45 | Duschen | `ic_shower` | Duschkopf |
| 46 | Schlafanzug anziehen | `ic_pajamas` | Pyjama |
| 47 | Kuscheln | `ic_cuddle` | Herz / Umarmung |
| 48 | Gute Nacht sagen | `ic_good_night` | Mond mit Schlafmütze |
| 49 | Fernsehen | `ic_watch_tv` | Fernseher |
| 50 | Medienzeit | `ic_screen_time` | Tablet |

### 3.2 Icon-Datenmodell

Erstelle eine Datenklasse und ein Registry-Objekt, das alle Icons zentral verwaltet:

```kotlin
data class IconInfo(
    val ref: String,           // Drawable-Ressourcenname, z.B. "ic_wake_up"
    val nameDE: String,        // Deutscher Anzeigename
    val category: IconCategory
)

enum class IconCategory(val labelDE: String) {
    MORNING("Morgenroutine"),
    MEALS("Mahlzeiten"),
    ACTIVITIES("Aktivitäten & Spiel"),
    CHORES("Pflichten & Lernen"),
    PLACES("Orte & Wege"),
    EVENING("Abendroutine")
}

object IconRegistry {
    val allIcons: List<IconInfo> = listOf(
        IconInfo("ic_wake_up", "Aufwachen", IconCategory.MORNING),
        IconInfo("ic_make_bed", "Bett machen", IconCategory.MORNING),
        // ... alle 50 Icons
    )

    fun getByRef(ref: String): IconInfo? = allIcons.find { it.ref == ref }

    fun getByCategory(category: IconCategory): List<IconInfo> =
        allIcons.filter { it.category == category }

    fun search(query: String): List<IconInfo> =
        allIcons.filter { it.nameDE.contains(query, ignoreCase = true) }

    @DrawableRes
    fun getDrawableRes(context: Context, ref: String): Int {
        return context.resources.getIdentifier(ref, "drawable", context.packageName)
    }
}
```

### 3.3 Icon-Assets erstellen

Für jedes Icon im Katalog muss ein Android Vector Drawable unter `res/drawable/` existieren. Format:

```xml
<!-- res/drawable/ic_wake_up.xml -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp"
    android:height="48dp"
    android:viewportWidth="48"
    android:viewportHeight="48">
    <path
        android:fillColor="#F2C94C"
        android:pathData="..." />
    <!-- Weitere Paths -->
</vector>
```

**Stilrichtlinien:**
- Viewport: 48×48
- Maximale Farbanzahl pro Icon: 3–4 Farben
- Strichstärke bei Outlines: 2 Viewport-Einheiten
- Abgerundete Linecaps und Linejoins
- Keine Texte innerhalb der Icons
- Farben sollten warm und freundlich sein (keine grellen oder dunklen Töne)

**Pragmatische Alternative:** Falls das Erstellen von 50 individuellen Vector Drawables zu aufwändig ist, verwende Material Symbols (Rounded, Weight 400, Fill) als Basis und ergänze sie mit 10–15 custom Icons für Aktivitäten ohne passendes Material-Symbol. Jedes Material-Symbol muss trotzdem als eigenes Drawable mit dem korrekten `ic_`-Prefix abgelegt werden, damit `IconRegistry` einheitlich funktioniert.

### 3.4 IconPicker-Composable

Erstelle ein wiederverwendbares Composable für die Icon-Auswahl:

```
Datei: ui/components/IconPicker.kt
```

**Aufbau des IconPickers:**

1. **Suchfeld** (oben): `OutlinedTextField` mit Suche-Icon. Filtert Icons nach `nameDE`.
2. **Kategorie-Chips** (darunter): Horizontale `LazyRow` mit `FilterChip` pro `IconCategory`. Ein Chip „Alle" ist standardmäßig ausgewählt.
3. **Icon-Grid** (Hauptbereich): `LazyVerticalGrid` mit 4 Spalten. Jede Zelle zeigt das Icon (40dp) und den Namen darunter (klein, einzeilig, ellipsiert).
4. **Auswahl-Feedback**: Das aktuell gewählte Icon hat einen farbigen Rahmen (Primary-Farbe).

**Composable-Signatur:**

```kotlin
@Composable
fun IconPicker(
    selectedIconRef: String?,
    onIconSelected: (String) -> Unit,
    modifier: Modifier = Modifier
)
```

**Verhalten:**
- Beim Tippen auf ein Icon wird `onIconSelected` mit dem `iconRef` aufgerufen.
- Die Suche filtert Icons nach Name. Wenn die Suche aktiv ist, werden die Kategorie-Chips deaktiviert.
- Leere Suchergebnisse zeigen einen „Keine Icons gefunden"-Platzhalter.

---

## AP-04: Aktivitätenverwaltung (CRUD-Screen)

### 4.1 Screen-Aufbau

Der Aktivitäten-Screen ersetzt den Platzhalter im Tab „Aktivitäten" und zeigt alle vorhandenen Aktivitäten.

```
Datei: ui/activities/ActivitiesScreen.kt
Datei: ui/activities/ActivitiesViewModel.kt
Datei: ui/activities/CreateEditActivityDialog.kt
```

#### Layout des Screens

```
┌──────────────────────────────────┐
│ TopAppBar: "Aktivitäten"         │
├──────────────────────────────────┤
│                                  │
│  ┌──────┐  ┌──────┐  ┌──────┐  │
│  │ Icon │  │ Icon │  │ Icon │  │   LazyVerticalGrid (3 Spalten)
│  │ Name │  │ Name │  │ Name │  │   oder LazyColumn (Liste)
│  └──────┘  └──────┘  └──────┘  │
│                                  │
│  ┌──────┐  ┌──────┐  ┌──────┐  │
│  │ Icon │  │ Icon │  │ Icon │  │
│  │ Name │  │ Name │  │ Name │  │
│  └──────┘  └──────┘  └──────┘  │
│                                  │
│                          [FAB +] │
└──────────────────────────────────┘
```

**Aktivitätskarte (einzelnes Item):**
- Größe: ca. 100×120dp
- Oben: Icon (56dp, zentriert), optional mit Farbhintergrund (Kreis oder abgerundetes Quadrat)
- Unten: Name (max. 2 Zeilen, zentriert, `bodySmall`)
- Tap → Bearbeitungsdialog öffnen
- Long Press → Kontextmenü (Bearbeiten, Löschen)

**Leerer Zustand:**
Wenn keine Aktivitäten vorhanden sind, zeige:
- Illustration oder dezentes Icon (z.B. ein großes `+` mit Kreisen)
- Text: „Noch keine Aktivitäten vorhanden"
- Subtext: „Tippe auf +, um deine erste Aktivität zu erstellen"

### 4.2 CreateEditActivityDialog

Ein `ModalBottomSheet` oder `AlertDialog` für das Erstellen und Bearbeiten einer Aktivität.

**Aufbau des Dialogs:**

```
┌──────────────────────────────────┐
│ Neue Aktivität / Aktivität bearbeiten │
├──────────────────────────────────┤
│                                  │
│  Name: [_________________________]│  OutlinedTextField, max 30 Zeichen
│                                  │
│  Icon:  [Gewähltes Icon]  [Ändern]│  Zeigt aktuelles Icon, Tap öffnet IconPicker
│                                  │
│  Farbe (optional):               │
│  ○ ○ ○ ○ ○ ○ ○ ○  ○(keine)     │  8 Farbkreise + „keine"-Option
│                                  │
│  [Abbrechen]         [Speichern] │
└──────────────────────────────────┘
```

**Validierung:**
- Name darf nicht leer sein (Speichern-Button ist deaktiviert)
- Name darf maximal 30 Zeichen lang sein (Zeichenzähler anzeigen)
- Ein Icon muss gewählt sein (Speichern-Button ist deaktiviert ohne Icon)
- Farbe ist optional (Standard: keine)

**Wenn über IconPicker geöffnet:** Der IconPicker erscheint als eigener Screen oder als Fullscreen-BottomSheet, da er viel Platz benötigt. Nach Auswahl kehrt der Nutzer zum Dialog zurück.

### 4.3 Löschen mit Verwendungsprüfung

Beim Löschen einer Aktivität prüfe über `ActivityRepository.getUsageCount(activityId)`, ob die Aktivität in Ablaufplänen verwendet wird.

**Fall 1: Nicht verwendet**
Zeige Standard-Bestätigungsdialog:
> „Aktivität ‚Zähne putzen' wirklich löschen?"
> [Abbrechen] [Löschen]

**Fall 2: In Plänen verwendet**
Zeige erweiterten Dialog:
> „Aktivität ‚Zähne putzen' wird in 3 Ablaufplänen verwendet. Beim Löschen wird sie auch aus diesen Plänen entfernt."
> [Abbrechen] [Trotzdem löschen]

### 4.4 ActivitiesViewModel

```kotlin
@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val repository: ActivityRepository
) : ViewModel() {

    val activities: StateFlow<List<ActivityEntity>> =
        repository.getAllActivities()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    fun createActivity(name: String, iconRef: String, color: Int?) {
        viewModelScope.launch {
            repository.create(name.trim(), iconRef, color)
        }
    }

    fun updateActivity(activity: ActivityEntity) {
        viewModelScope.launch {
            repository.update(activity.copy(name = activity.name.trim()))
        }
    }

    fun checkAndDelete(activity: ActivityEntity) {
        viewModelScope.launch {
            val usageCount = repository.getUsageCount(activity.id)
            _uiState.update {
                it.copy(
                    deleteCandidate = activity,
                    deleteUsageCount = usageCount,
                    showDeleteDialog = true
                )
            }
        }
    }

    fun confirmDelete() {
        viewModelScope.launch {
            _uiState.value.deleteCandidate?.let { activity ->
                repository.delete(activity.id)
            }
            _uiState.update {
                it.copy(deleteCandidate = null, showDeleteDialog = false)
            }
        }
    }

    fun dismissDelete() {
        _uiState.update {
            it.copy(deleteCandidate = null, showDeleteDialog = false)
        }
    }
}

data class ActivitiesUiState(
    val deleteCandidate: ActivityEntity? = null,
    val deleteUsageCount: Int = 0,
    val showDeleteDialog: Boolean = false,
    val showCreateDialog: Boolean = false,
    val editingActivity: ActivityEntity? = null
)
```

### 4.5 Seed-Daten (vordefinierte Aktivitäten)

Beim allerersten App-Start (erkennbar über DataStore-Flag `isFirstLaunch`) werden 12 Standard-Aktivitäten in die Datenbank eingefügt. Diese dienen als Startpunkt, damit der Nutzer sofort einen Plan erstellen kann, ohne erst Aktivitäten anlegen zu müssen.

```kotlin
val defaultActivities = listOf(
    ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up", isUserCreated = false),
    ActivityEntity(name = "Zähne putzen", iconRef = "ic_brush_teeth", isUserCreated = false),
    ActivityEntity(name = "Anziehen", iconRef = "ic_get_dressed", isUserCreated = false),
    ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast", isUserCreated = false),
    ActivityEntity(name = "Kindergarten", iconRef = "ic_kindergarten", isUserCreated = false),
    ActivityEntity(name = "Mittagessen", iconRef = "ic_lunch", isUserCreated = false),
    ActivityEntity(name = "Draußen spielen", iconRef = "ic_play_outside", isUserCreated = false),
    ActivityEntity(name = "Abendessen", iconRef = "ic_dinner", isUserCreated = false),
    ActivityEntity(name = "Baden", iconRef = "ic_bath", isUserCreated = false),
    ActivityEntity(name = "Schlafanzug anziehen", iconRef = "ic_pajamas", isUserCreated = false),
    ActivityEntity(name = "Buch lesen", iconRef = "ic_read_book", isUserCreated = false),
    ActivityEntity(name = "Gute Nacht sagen", iconRef = "ic_good_night", isUserCreated = false),
)
```

Die Seed-Logik wird in der `Application`-Klasse oder in einem `SeedDataManager` aufgerufen, der beim Start prüft, ob bereits Aktivitäten existieren.

---

## Abnahmekriterien Phase 2

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Mindestens 50 Icon-Drawables existieren unter `res/drawable/` und werden korrekt angezeigt | Visuelle Prüfung |
| 2 | `IconRegistry.allIcons` enthält genau 50 Einträge mit gültigen Drawable-Referenzen | Unit-Test |
| 3 | `IconRegistry.search("Zähne")` liefert „Zähne putzen" zurück | Unit-Test |
| 4 | `IconRegistry.getByCategory(MORNING)` liefert genau 8 Icons | Unit-Test |
| 5 | Der IconPicker zeigt alle Icons in einem Grid mit Kategoriefilter | Manueller UI-Test |
| 6 | Die Suche im IconPicker filtert Icons korrekt und zeigt einen Leerzustand | Manueller UI-Test |
| 7 | Beim ersten Start werden 12 Standard-Aktivitäten in der Datenbank angelegt | Instrumentierter Test |
| 8 | Eine neue Aktivität kann erstellt werden (Name + Icon + optionale Farbe) | Manueller Test |
| 9 | Eine bestehende Aktivität kann bearbeitet werden (Name, Icon, Farbe ändern) | Manueller Test |
| 10 | Löschen zeigt Bestätigungsdialog; bei Verwendung in Plänen wird gewarnt | Manueller Test |
| 11 | Der Name ist auf 30 Zeichen begrenzt; leerer Name verhindert Speichern | Manueller Test |
| 12 | Der Leerzustand (keine Aktivitäten) zeigt Platzhalter mit Anleitung | Manueller Test (nach Löschen aller) |
| 13 | Icons rendern korrekt in Größen von 24dp bis 120dp | Visuelle Prüfung |

---

## Hinweise für den AI-Agenten

- Falls das Erstellen aller 50 Vector Drawables nicht möglich ist, erstelle mindestens 15 repräsentative Icons (mind. 2 pro Kategorie) und verwende für die übrigen ein Platzhalter-Icon (`ic_placeholder`) mit einem Fragezeichen-Symbol. Dokumentiere, welche Icons noch fehlen.
- Der IconPicker wird in Phase 3 (Ablaufplan-Editor) und Phase 5 (Wochenplan) wiederverwendet — achte auf eine saubere, modulare API.
- Material-3-BottomSheet (`ModalBottomSheet`) ist die bevorzugte Variante für den Erstellungsdialog, da er auf Mobilgeräten besser erreichbar ist als ein zentrierter Dialog.
- Die `ActivityColors`-Liste aus Phase 1 (Theme) wird hier als Farbauswahl im Dialog verwendet.
- Die Seed-Daten dürfen nur beim allerersten Start eingefügt werden, nicht bei jedem App-Start. Verwende einen DataStore-Boolean `hasSeeded` als Guard.
