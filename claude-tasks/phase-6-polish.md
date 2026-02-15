# Phase 6: Polish und Onboarding — Vorlagen, Einführung und Einstellungen

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-10 (Vorlagen-System), AP-11 (Onboarding & Guided Tour), AP-12 (Einstellungen) |
| Abhängigkeiten | Phase 1–5 vollständig abgeschlossen |
| Ergebnis | Release-fertiger MVP mit Vorlagen, Onboarding-Flow und Einstellungen |
| Geschätzte Story Points | 7 |

## Voraussetzungen

Alle bisherigen Phasen müssen abgeschlossen sein. Die folgenden Artefakte werden in dieser Phase referenziert oder erweitert:

- Ablaufplan-Erstellung und -Übersicht (Phase 3)
- Aktivitäten mit Seed-Daten und Icon-Bibliothek (Phase 2)
- PDF-Export für Ablaufpläne und Wochenpläne (Phase 4 + 5)
- Room-Datenbank mit vollständigem Schema (Phase 1)
- Navigation mit allen Screens (Phase 1–5)
- DataStore für Preferences (Phase 1, angelegt aber ggf. noch leer)

---

## AP-10: Vorlagen-System

### 10.1 Konzept

Vorlagen sind vorgefertigte Ablaufpläne, die beim ersten App-Start in die Datenbank eingefügt werden. Sie sind als `RoutinePlanEntity` mit `isTemplate = true` markiert und können vom Nutzer nicht direkt bearbeitet oder gelöscht werden. Stattdessen kann der Nutzer eine Vorlage als Kopie verwenden und die Kopie frei anpassen.

### 10.2 Vorlagen-Definitionen

Die folgenden 7 Vorlagen werden mitgeliefert. Jede referenziert Aktivitäten aus den Seed-Daten (Phase 2) und der Icon-Bibliothek (Phase 2). Falls eine referenzierte Aktivität noch nicht als Seed existiert, muss sie in der Seed-Logik ergänzt werden.

#### Vorlage 1: Morgenroutine Kleinkind (3–5 Jahre)

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Aufwachen | ic_wake_up |
| 1 | Auf Toilette gehen | ic_toilet |
| 2 | Waschen | ic_wash_face |
| 3 | Anziehen | ic_get_dressed |
| 4 | Frühstücken | ic_breakfast |
| 5 | Zähne putzen | ic_brush_teeth |

#### Vorlage 2: Morgenroutine Schulkind (6–10 Jahre)

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Aufwachen | ic_wake_up |
| 1 | Bett machen | ic_make_bed |
| 2 | Waschen | ic_wash_face |
| 3 | Anziehen | ic_get_dressed |
| 4 | Frühstücken | ic_breakfast |
| 5 | Zähne putzen | ic_brush_teeth |
| 6 | Schuhe anziehen | ic_shoes |
| 7 | Schule | ic_school |

#### Vorlage 3: Abendroutine

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Abendessen | ic_dinner |
| 1 | Aufräumen | ic_tidy_up |
| 2 | Baden | ic_bath |
| 3 | Schlafanzug anziehen | ic_pajamas |
| 4 | Zähne putzen | ic_brush_teeth |
| 5 | Buch lesen | ic_read_book |
| 6 | Kuscheln | ic_cuddle |
| 7 | Gute Nacht sagen | ic_good_night |

#### Vorlage 4: Wochenend-Morgen

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Aufwachen | ic_wake_up |
| 1 | Kuscheln | ic_cuddle |
| 2 | Frühstücken | ic_breakfast |
| 3 | Anziehen | ic_get_dressed |
| 4 | Draußen spielen | ic_play_outside |

#### Vorlage 5: Kindergarten-Tag

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Aufwachen | ic_wake_up |
| 1 | Anziehen | ic_get_dressed |
| 2 | Frühstücken | ic_breakfast |
| 3 | Zähne putzen | ic_brush_teeth |
| 4 | Kindergarten | ic_kindergarten |
| 5 | Mittagessen | ic_lunch |
| 6 | Draußen spielen | ic_play_outside |
| 7 | Abendessen | ic_dinner |
| 8 | Schlafanzug anziehen | ic_pajamas |
| 9 | Gute Nacht sagen | ic_good_night |

#### Vorlage 6: Hausaufgaben-Routine

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Snack | ic_snack |
| 1 | Hände waschen | ic_wash_hands |
| 2 | Hausaufgaben | ic_homework |
| 3 | Draußen spielen | ic_play_outside |

#### Vorlage 7: Bettgeh-Routine (kurz)

| Position | Aktivität | iconRef |
|----------|-----------|---------|
| 0 | Schlafanzug anziehen | ic_pajamas |
| 1 | Zähne putzen | ic_brush_teeth |
| 2 | Buch lesen | ic_read_book |
| 3 | Gute Nacht sagen | ic_good_night |

### 10.3 Seed-Implementierung

Die Vorlagen werden zusammen mit den Seed-Aktivitäten beim ersten App-Start eingefügt. Erweitere den bestehenden `SeedDataManager` aus Phase 2:

**Datei:** `data/seed/SeedDataManager.kt`

```kotlin
class SeedDataManager @Inject constructor(
    private val activityDao: ActivityDao,
    private val planDao: RoutinePlanDao,
    private val dataStore: DataStore<Preferences>
) {
    private val HAS_SEEDED = booleanPreferencesKey("has_seeded_v1")

    suspend fun seedIfNeeded() {
        val hasSeeded = dataStore.data.first()[HAS_SEEDED] ?: false
        if (hasSeeded) return

        // 1. Seed-Aktivitäten einfügen (erweiterte Liste)
        val activityIds = seedActivities()

        // 2. Vorlagen einfügen
        seedTemplates(activityIds)

        // 3. Flag setzen
        dataStore.edit { it[HAS_SEEDED] = true }
    }

    private suspend fun seedActivities(): Map<String, Long> {
        // Erstelle alle Aktivitäten, die in Vorlagen referenziert werden
        // Returniert: iconRef -> insertedId Mapping
        val activities = listOf(
            ActivityEntity(name = "Aufwachen", iconRef = "ic_wake_up", isUserCreated = false),
            ActivityEntity(name = "Bett machen", iconRef = "ic_make_bed", isUserCreated = false),
            ActivityEntity(name = "Auf Toilette gehen", iconRef = "ic_toilet", isUserCreated = false),
            ActivityEntity(name = "Waschen", iconRef = "ic_wash_face", isUserCreated = false),
            ActivityEntity(name = "Zähne putzen", iconRef = "ic_brush_teeth", isUserCreated = false),
            ActivityEntity(name = "Anziehen", iconRef = "ic_get_dressed", isUserCreated = false),
            ActivityEntity(name = "Schuhe anziehen", iconRef = "ic_shoes", isUserCreated = false),
            ActivityEntity(name = "Frühstücken", iconRef = "ic_breakfast", isUserCreated = false),
            ActivityEntity(name = "Mittagessen", iconRef = "ic_lunch", isUserCreated = false),
            ActivityEntity(name = "Abendessen", iconRef = "ic_dinner", isUserCreated = false),
            ActivityEntity(name = "Snack", iconRef = "ic_snack", isUserCreated = false),
            ActivityEntity(name = "Kindergarten", iconRef = "ic_kindergarten", isUserCreated = false),
            ActivityEntity(name = "Schule", iconRef = "ic_school", isUserCreated = false),
            ActivityEntity(name = "Draußen spielen", iconRef = "ic_play_outside", isUserCreated = false),
            ActivityEntity(name = "Aufräumen", iconRef = "ic_tidy_up", isUserCreated = false),
            ActivityEntity(name = "Hausaufgaben", iconRef = "ic_homework", isUserCreated = false),
            ActivityEntity(name = "Hände waschen", iconRef = "ic_wash_hands", isUserCreated = false),
            ActivityEntity(name = "Baden", iconRef = "ic_bath", isUserCreated = false),
            ActivityEntity(name = "Schlafanzug anziehen", iconRef = "ic_pajamas", isUserCreated = false),
            ActivityEntity(name = "Buch lesen", iconRef = "ic_read_book", isUserCreated = false),
            ActivityEntity(name = "Kuscheln", iconRef = "ic_cuddle", isUserCreated = false),
            ActivityEntity(name = "Gute Nacht sagen", iconRef = "ic_good_night", isUserCreated = false),
        )

        val ids = activityDao.insertAll(activities)
        return activities.mapIndexed { index, act -> act.iconRef to ids[index] }.toMap()
    }

    private suspend fun seedTemplates(activityIds: Map<String, Long>) {
        val templates = listOf(
            TemplateDefinition(
                name = "Morgenroutine Kleinkind",
                entries = listOf("ic_wake_up", "ic_toilet", "ic_wash_face", "ic_get_dressed", "ic_breakfast", "ic_brush_teeth")
            ),
            TemplateDefinition(
                name = "Morgenroutine Schulkind",
                entries = listOf("ic_wake_up", "ic_make_bed", "ic_wash_face", "ic_get_dressed", "ic_breakfast", "ic_brush_teeth", "ic_shoes", "ic_school")
            ),
            TemplateDefinition(
                name = "Abendroutine",
                entries = listOf("ic_dinner", "ic_tidy_up", "ic_bath", "ic_pajamas", "ic_brush_teeth", "ic_read_book", "ic_cuddle", "ic_good_night")
            ),
            TemplateDefinition(
                name = "Wochenend-Morgen",
                entries = listOf("ic_wake_up", "ic_cuddle", "ic_breakfast", "ic_get_dressed", "ic_play_outside")
            ),
            TemplateDefinition(
                name = "Kindergarten-Tag",
                entries = listOf("ic_wake_up", "ic_get_dressed", "ic_breakfast", "ic_brush_teeth", "ic_kindergarten", "ic_lunch", "ic_play_outside", "ic_dinner", "ic_pajamas", "ic_good_night")
            ),
            TemplateDefinition(
                name = "Hausaufgaben-Routine",
                entries = listOf("ic_snack", "ic_wash_hands", "ic_homework", "ic_play_outside")
            ),
            TemplateDefinition(
                name = "Bettgeh-Routine",
                entries = listOf("ic_pajamas", "ic_brush_teeth", "ic_read_book", "ic_good_night")
            ),
        )

        templates.forEach { template ->
            val planId = planDao.insert(
                RoutinePlanEntity(
                    name = template.name,
                    slotCount = template.entries.size,
                    isTemplate = true
                )
            )
            val entries = template.entries.mapIndexed { index, iconRef ->
                val activityId = activityIds[iconRef] ?: return@forEach
                RoutinePlanEntryEntity(planId = planId, activityId = activityId, position = index)
            }
            planDao.insertEntries(entries)
        }
    }

    private data class TemplateDefinition(val name: String, val entries: List<String>)
}
```

### 10.4 Vorlagen in der Ablaufplan-Übersicht

In der PlansScreen (Phase 3, AP-06) werden Vorlagen visuell von Nutzerplänen unterschieden:

**Darstellung:**
- Vorlagen haben ein kleines Badge „Vorlage" in `SecondaryContainer`-Farbe oben rechts auf der Karte.
- Vorlagen sind in einem eigenen Abschnitt am Ende der Liste oder oben in einer horizontalen LazyRow gruppiert.
- Tippen auf eine Vorlage öffnet eine Vorschau (Read-Only-Ansicht des Plans) mit einem prominenten Button „Als Kopie verwenden".

**Vorschau-Screen:**

**Datei:** `ui/plans/TemplatePreviewer.kt`

```
┌──────────────────────────────────┐
│ ← Morgenroutine Kleinkind       │
├──────────────────────────────────┤
│          🏷️ Vorlage              │
│                                  │
│  1. ☀️ Aufwachen                 │  Read-Only Liste
│  2. 🚽 Auf Toilette gehen       │
│  3. 💧 Waschen                   │
│  4. 👕 Anziehen                  │
│  5. 🥣 Frühstücken              │
│  6. 🦷 Zähne putzen             │
│                                  │
│  ┌──────────────────────────────┐│
│  │   Als Kopie verwenden        ││  Prominenter CTA-Button
│  └──────────────────────────────┘│
│                                  │
│  Diese Vorlage wird kopiert und  │
│  kann anschließend frei          │
│  angepasst werden.               │
└──────────────────────────────────┘
```

**Aktion „Als Kopie verwenden":**
1. Die bestehende `duplicatePlan`-Logik aus AP-06 wird aufgerufen.
2. Die Kopie bekommt den Namen der Vorlage ohne „(Kopie)"-Suffix (der Nutzer benennt sie vermutlich ohnehin um).
3. Die Kopie hat `isTemplate = false`.
4. Der Nutzer wird direkt zum Plan-Editor der Kopie navigiert.

### 10.5 Vorlagen dürfen nicht bearbeitet oder gelöscht werden

Im Kontextmenü einer Vorlage fehlen die Optionen „Bearbeiten" und „Löschen". Es werden nur angezeigt: „Vorschau" und „Als Kopie verwenden".

Im Plan-Editor: Falls der Nutzer auf einem Weg einen Vorlagen-Plan direkt öffnet (z.B. Deep Link oder Bug), prüfe `isTemplate` und zeige stattdessen die Vorschau.

---

## AP-11: Onboarding und Guided Tour

### 11.1 Onboarding-Screens

Das Onboarding besteht aus 4 horizontal wischbaren Screens, die nur beim allerersten App-Start erscheinen.

**Datei:** `ui/onboarding/OnboardingScreen.kt`

#### Screen 1: Willkommen

```
┌──────────────────────────────────┐
│                                  │
│         [Illustration]           │  Kindgerechte Illustration:
│         Familie mit Kalender     │  Familie mit buntem Kalender
│                                  │
│     Willkommen bei RoutineHeld   │  Titel
│                                  │
│     Erstelle visuelle Abläufe    │  Beschreibung
│     für den Alltag deines        │
│     Kindes — einfach, bunt       │
│     und zum Ausdrucken.          │
│                                  │
│         ● ○ ○ ○                  │  Page Indicator
│                                  │
│                    [Weiter →]    │
│  [Überspringen]                  │
└──────────────────────────────────┘
```

#### Screen 2: Ablaufpläne

```
Illustration: Karten mit Icons in einer Reihe (wie ein Plan)
Titel: "Ablaufpläne erstellen"
Text: "Stelle aus Aktivitäten einen Tagesablauf zusammen.
       Dein Kind sieht auf einen Blick, was als nächstes kommt."
```

#### Screen 3: Wochenplan

```
Illustration: Kalender-Matrix mit bunten Zellen
Titel: "Wochenplan auf einen Blick"
Text: "Plane die ganze Woche: Morgens, Mittags, Abends.
       Jeder Tag kann seinen eigenen Ablauf haben."
```

#### Screen 4: Ausdrucken

```
Illustration: Smartphone mit Pfeil zu einem ausgedruckten Blatt am Kühlschrank
Titel: "Ausdrucken & aufhängen"
Text: "Exportiere jeden Plan als PDF — perfekt zum Ausdrucken
       und Aufhängen im Kinderzimmer oder in der Küche."

Button: [Los geht's! →] (statt "Weiter")
```

### 11.2 Technische Umsetzung

Verwende einen `HorizontalPager` (Compose Foundation) mit `PagerState`:

```kotlin
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // Überspringen-Button (nur auf Screens 1–3)
        if (pagerState.currentPage < 3) {
            TextButton(
                onClick = onComplete,
                modifier = Modifier.align(Alignment.End).padding(16.dp)
            ) {
                Text("Überspringen")
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPage(page = page)
        }

        // Page Indicator + Navigation
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == pagerState.currentPage) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            // Button
            if (pagerState.currentPage < 3) {
                FilledTonalButton(onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }) {
                    Text("Weiter")
                }
            } else {
                Button(onClick = onComplete) {
                    Text("Los geht's!")
                }
            }
        }
    }
}
```

### 11.3 Onboarding-Guard

Das Onboarding wird durch einen DataStore-Boolean gesteuert:

```kotlin
private val HAS_COMPLETED_ONBOARDING = booleanPreferencesKey("has_completed_onboarding")

// In der Navigation oder MainActivity:
val hasCompletedOnboarding: Flow<Boolean> = dataStore.data.map { prefs ->
    prefs[HAS_COMPLETED_ONBOARDING] ?: false
}

// Nach Abschluss:
suspend fun completeOnboarding() {
    dataStore.edit { it[HAS_COMPLETED_ONBOARDING] = true }
}
```

Die Navigation prüft beim Start, ob das Onboarding abgeschlossen ist:

```kotlin
// In AppNavigation.kt
val hasOnboarded by viewModel.hasCompletedOnboarding.collectAsState(initial = null)

when (hasOnboarded) {
    null -> { /* Ladeindikator */ }
    false -> OnboardingScreen(onComplete = { viewModel.completeOnboarding() })
    true -> MainAppContent(navController)
}
```

### 11.4 Nach dem Onboarding: Erster Plan

Nach „Los geht's!" wird der Nutzer nicht einfach auf einen leeren Homescreen geworfen. Stattdessen wird er zur Vorlagen-Auswahl geleitet, sodass er sofort seinen ersten Plan aus einer Vorlage erstellen kann. Die Navigationsfolge ist:

Onboarding → Vorlagen-Übersicht (mit Hinweis „Wähle eine Vorlage als Startpunkt") → Vorschau → „Als Kopie verwenden" → Plan-Editor.

Falls der Nutzer die Vorlagen überspringt, landet er auf dem normalen Ablaufplan-Tab.

---

## AP-12: Einstellungen und App-Konfiguration

### 12.1 Screen-Aufbau

**Datei:** `ui/settings/SettingsScreen.kt`
**Datei:** `ui/settings/SettingsViewModel.kt`

Der Einstellungs-Screen ist über ein Zahnrad-Icon in der TopAppBar (nicht in der Bottom Navigation) erreichbar. Er wird in die Navigation als eigener Screen integriert.

```
┌──────────────────────────────────┐
│ ← Einstellungen                  │
├──────────────────────────────────┤
│                                  │
│  ALLGEMEIN                       │  Sektions-Header
│  ─────────────────────────────── │
│  Kindname(n)              [Lisa] │  Tippen → Eingabedialog
│  Standard-Plätze            [6]  │  Tippen → Slider-Dialog (3–12)
│                                  │
│  APP                             │
│  ─────────────────────────────── │
│  Onboarding erneut anzeigen  [→] │  Setzt Onboarding-Flag zurück
│  Alle Daten löschen          [→] │  Warndialog mit 2-stufiger Bestätigung
│                                  │
│  INFO                            │
│  ─────────────────────────────── │
│  Version                  1.0.0  │
│  Datenschutz                 [→] │  Öffnet Datenschutz-Text
│  Lizenzen                    [→] │  Öffnet OssLicensesMenuActivity oder eigenen Screen
│                                  │
└──────────────────────────────────┘
```

### 12.2 Einstellungen im Detail

#### Kindname

Ein optionaler Textfeld-Dialog. Der Name wird im PDF-Header verwendet (z.B. „Tagesplan für Lisa"). Wird über DataStore gespeichert.

```kotlin
private val CHILD_NAME = stringPreferencesKey("child_name")
```

Falls das Feld leer ist, wird im PDF kein „für ..."-Untertitel angezeigt.

#### Standard-Plätze

Die Standardanzahl der Slots, die beim Erstellen eines neuen Plans vorgeschlagen wird. Default: 6. Einstellbar über einen Slider-Dialog (3–12).

```kotlin
private val DEFAULT_SLOT_COUNT = intPreferencesKey("default_slot_count")
```

Dieser Wert wird in `CreatePlanDialog` (Phase 3) als initialer Slider-Wert verwendet.

#### Onboarding erneut anzeigen

Setzt den DataStore-Boolean `has_completed_onboarding` auf `false`. Beim nächsten Mal, wenn der Nutzer zum Hauptscreen navigiert, wird das Onboarding erneut angezeigt. Nach dem Tippen erscheint ein kurzer Snackbar-Hinweis: „Onboarding wird beim nächsten Start angezeigt."

#### Alle Daten löschen

Zweistufige Bestätigung:

**Dialog 1:**
> „Alle Daten löschen? Alle Ablaufpläne, Wochenpläne und eigenen Aktivitäten werden unwiderruflich gelöscht. Vorlagen werden beim nächsten Start neu erstellt."
> [Abbrechen] [Weiter]

**Dialog 2:**
> „Wirklich alles löschen? Diese Aktion kann nicht rückgängig gemacht werden."
> [Abbrechen] [Endgültig löschen]

Nach Bestätigung:
1. Alle Room-Tabellen leeren (`DELETE FROM ...` für alle Tabellen)
2. DataStore-Flag `has_seeded_v1` zurücksetzen
3. Optional: DataStore-Flag `has_completed_onboarding` zurücksetzen
4. App-Neustart erzwingen oder zum Onboarding navigieren

```kotlin
fun deleteAllData() {
    viewModelScope.launch {
        database.clearAllTables()
        dataStore.edit { prefs ->
            prefs.remove(HAS_SEEDED)
            prefs.remove(HAS_COMPLETED_ONBOARDING)
            // child_name und default_slot_count beibehalten (persönliche Einstellungen)
        }
        // Trigger App-Neustart oder Navigation zum Onboarding
    }
}
```

#### Datenschutz

Ein einfacher Textscreen mit grundlegenden Datenschutzinformationen. Da die App im MVP keine Cloud-Dienste verwendet und alle Daten lokal gespeichert werden, ist der Text kurz:

```
Datenschutz

RoutineHeld speichert alle Daten ausschließlich lokal auf deinem Gerät.
Es werden keine Daten an Server übertragen oder mit Dritten geteilt.

Die App benötigt keine Internetverbindung und erhebt keine personenbezogenen Daten
über die von dir eingegebenen Inhalte (Kindnamen, Aktivitäten, Pläne) hinaus.

Bei Deinstallation der App werden alle Daten vollständig gelöscht.
```

#### Lizenzen

Zeige Open-Source-Lizenzen der verwendeten Bibliotheken. Verwende das `oss-licenses-plugin` von Google oder erstelle einen manuellen Lizenz-Screen, der die Lizenzen der Kern-Bibliotheken (Jetpack Compose, Room, Hilt, Material 3 etc.) auflistet.

### 12.3 SettingsViewModel

```kotlin
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val database: AppDatabase
) : ViewModel() {

    val childName: StateFlow<String> = dataStore.data
        .map { it[CHILD_NAME] ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val defaultSlotCount: StateFlow<Int> = dataStore.data
        .map { it[DEFAULT_SLOT_COUNT] ?: 6 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 6)

    fun updateChildName(name: String) {
        viewModelScope.launch {
            dataStore.edit { it[CHILD_NAME] = name.trim() }
        }
    }

    fun updateDefaultSlotCount(count: Int) {
        viewModelScope.launch {
            dataStore.edit { it[DEFAULT_SLOT_COUNT] = count.coerceIn(3, 12) }
        }
    }

    fun resetOnboarding() { ... }
    fun deleteAllData() { ... }

    companion object {
        private val CHILD_NAME = stringPreferencesKey("child_name")
        private val DEFAULT_SLOT_COUNT = intPreferencesKey("default_slot_count")
    }
}
```

### 12.4 Integration in die Navigation

Füge den Settings-Screen zur Navigation hinzu. Das Zahnrad-Icon erscheint in der TopAppBar aller drei Tab-Screens:

```kotlin
// In jeder TopAppBar:
IconButton(onClick = { navController.navigate(Screen.Settings) }) {
    Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen")
}
```

### 12.5 Kindname im PDF-Export nutzen

Der `PdfExportManager` (Phase 4) und `WeekPlanPdfGenerator` (Phase 5) müssen den Kindnamen aus den Einstellungen erhalten. Erweitere die Export-ViewModels, um den Kindnamen aus dem DataStore zu lesen und an den Generator zu übergeben:

```kotlin
// Im ViewModel, das den Export auslöst:
private val childName: StateFlow<String> = settingsRepository.childName
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

fun exportAsPdf(planId: Long) {
    viewModelScope.launch {
        val name = childName.value.ifBlank { null }
        val result = pdfExportManager.exportPlan(plan, activityMap, childName = name)
        // ...
    }
}
```

---

## Abnahmekriterien Phase 6

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Beim ersten Start (nach frischer Installation) werden 7 Vorlagen in der Datenbank angelegt | Instrumentierter Test |
| 2 | Vorlagen sind in der Ablaufplan-Übersicht als „Vorlage" gekennzeichnet | Manueller Test |
| 3 | Vorlagen können nicht direkt bearbeitet oder gelöscht werden | Manueller Test |
| 4 | Tippen auf eine Vorlage zeigt eine Vorschau mit „Als Kopie verwenden" | Manueller Test |
| 5 | „Als Kopie verwenden" erstellt einen editierbaren Plan und öffnet den Editor | Manueller Test |
| 6 | Beim allerersten App-Start erscheinen 4 Onboarding-Screens | Manueller Test |
| 7 | Das Onboarding kann übersprungen werden | Manueller Test |
| 8 | Nach dem Onboarding erscheint es nicht erneut (außer über Einstellungen) | Reinstall-Test |
| 9 | In den Einstellungen kann ein Kindname eingegeben werden | Manueller Test |
| 10 | Der Kindname erscheint im PDF-Export als Untertitel | Visueller Test |
| 11 | Die Standard-Slot-Anzahl ist konfigurierbar und wird bei neuen Plänen übernommen | Manueller Test |
| 12 | „Alle Daten löschen" erfordert 2-stufige Bestätigung und löscht alle Nutzerdaten | Manueller Test + DB-Prüfung |
| 13 | Nach Datenlöschung werden Vorlagen beim nächsten Start neu angelegt | Manueller Test |
| 14 | „Onboarding erneut anzeigen" funktioniert korrekt | Manueller Test |
| 15 | Die Datenschutzseite ist erreichbar und zeigt Informationstext an | Manueller Test |

---

## Hinweise für den AI-Agenten

- **Seed-Reihenfolge:** Der `SeedDataManager` muss zuerst die Aktivitäten einfügen (um deren IDs zu erhalten) und dann die Vorlagen-Entries, die auf diese IDs verweisen. Da Room `autoGenerate = true` verwendet, sind die IDs erst nach dem Insert bekannt.
- **Vorlagen-Aktualisierung:** Wenn in einem zukünftigen Update neue Vorlagen hinzukommen, muss die Seed-Logik versioniert werden (z.B. `has_seeded_v1`, `has_seeded_v2`). In Phase 6 genügt eine einzelne Version.
- **Onboarding-Illustrationen:** Für den MVP genügen einfache Compose-Grafiken (große Material-Icons mit farbigem Hintergrund in einem Kreis). Falls professionelle Illustrationen gewünscht sind, können diese später als PNG/SVG-Assets ergänzt werden.
- **Settings-Screen:** Verwende nicht die deprecated `PreferenceScreen`-API. Baue den Screen komplett in Compose mit normalen Composables (`Text`, `Row`, `Clickable`, `Switch` etc.).
- **Verknüpfung Phase 4/5 → Phase 6:** Nach Abschluss dieser Phase muss überprüft werden, ob der `childName` korrekt an beide PDF-Generatoren weitergegeben wird. Dies erfordert ggf. eine kleine Anpassung in den Export-ViewModels aus Phase 4 und 5.

---

## MVP-Abschluss-Checkliste

Nach Abschluss aller 6 Phasen sollte die App folgende End-to-End-Flows unterstützen:

1. **Erstinstallation → Erster Plan:** App installieren → Onboarding durchlaufen → Vorlage wählen → Kopie anpassen → PDF exportieren → Ausdrucken.
2. **Eigenen Plan erstellen:** Neuen Plan erstellen → Slots definieren → Aktivitäten zuweisen per Tap → Reihenfolge per Drag ändern → PDF exportieren.
3. **Wochenplan aufbauen:** Wochenplan erstellen → Matrix-Zellen mit Abläufen oder Aktivitäten füllen → „Auf ganze Woche kopieren" nutzen → PDF exportieren.
4. **Anpassen und wiederverwenden:** Bestehenden Plan öffnen → Aktivitäten austauschen → Neues PDF exportieren → Neues Blatt an den Kühlschrank hängen.

Alle Daten werden ausschließlich lokal gespeichert. Es wird kein Internet benötigt. Die App ist werbefrei.
