# Phase 1: Fundament — Projektsetup und Datenmodell

## Übersicht

| Eigenschaft | Wert |
|-------------|------|
| Arbeitspakete | AP-01 (Projektsetup & Architektur), AP-02 (Datenmodell & Datenbank) |
| Abhängigkeiten | Keine (erste Phase) |
| Ergebnis | Lauffähige Android-App mit leeren Screens, Navigation und vollständigem Datenmodell |
| Geschätzte Story Points | 13 |

## Kontext

Die App **RoutineHeld** ist ein Familien-Routineplaner für Android. Eltern erstellen visuelle Ablaufpläne und Wochenpläne für die Routinen ihrer Kinder. Die Pläne bestehen aus Aktivitäten mit Icons und werden per Drag & Drop zusammengestellt. Der MVP erlaubt das Erstellen, Bearbeiten und als PDF-Exportieren von Tagesabläufen und Wochenplänen.

Diese Phase legt das technische Fundament: Projektstruktur, Architektur-Patterns, Dependency Injection, Datenbank-Schema und Navigation.

---

## Technologie-Stack

| Bereich | Technologie | Version (min.) |
|---------|-------------|---------------|
| Sprache | Kotlin | 1.9+ |
| UI | Jetpack Compose + Material 3 | BOM 2024.x |
| Architektur | MVVM + Repository Pattern | — |
| Datenbank | Room | 2.6+ |
| Preferences | Jetpack DataStore (Preferences) | 1.1+ |
| Navigation | Compose Navigation (Type-Safe) | 2.8+ |
| DI | Hilt | 2.51+ |
| Testing | JUnit 5, Room Testing, Compose UI Testing | — |
| Min SDK | 26 (Android 8.0) | — |
| Target SDK | 35 | — |
| Build | Gradle Kotlin DSL mit Version Catalogs | — |

---

## AP-01: Projektsetup und Architektur

### 1.1 Projekt erstellen

Erstelle ein neues Android-Projekt mit folgenden Parametern:

- **Package:** `de.routineheld.app`
- **Application ID:** `de.routineheld.app`
- **Min SDK:** 26
- **Target SDK:** 35
- **Compose:** Aktiviert
- **Build-System:** Gradle Kotlin DSL mit Version Catalog (`libs.versions.toml`)

### 1.2 Package-Struktur

```
de.routineheld.app/
├── RoutineHeldApp.kt              // Application-Klasse (@HiltAndroidApp)
├── MainActivity.kt                 // Single Activity (@AndroidEntryPoint)
├── navigation/
│   ├── AppNavigation.kt            // NavHost mit allen Routes
│   ├── Screen.kt                   // Sealed class/interface für Routes
│   └── BottomNavBar.kt             // Bottom Navigation Composable
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          // Room Database Definition
│   │   ├── entity/
│   │   │   ├── ActivityEntity.kt
│   │   │   ├── RoutinePlanEntity.kt
│   │   │   ├── RoutinePlanEntryEntity.kt
│   │   │   ├── WeekPlanEntity.kt
│   │   │   └── WeekPlanSlotEntity.kt
│   │   ├── dao/
│   │   │   ├── ActivityDao.kt
│   │   │   ├── RoutinePlanDao.kt
│   │   │   └── WeekPlanDao.kt
│   │   └── relation/
│   │       ├── RoutinePlanWithEntries.kt
│   │       └── WeekPlanWithSlots.kt
│   └── repository/
│       ├── ActivityRepository.kt
│       ├── RoutinePlanRepository.kt
│       └── WeekPlanRepository.kt
├── di/
│   ├── DatabaseModule.kt           // Hilt-Modul für Room
│   └── RepositoryModule.kt         // Hilt-Modul für Repositories
├── ui/
│   ├── theme/
│   │   ├── Theme.kt
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   ├── activities/
│   │   ├── ActivitiesScreen.kt     // Platzhalter-Screen
│   │   └── ActivitiesViewModel.kt
│   ├── plans/
│   │   ├── PlansScreen.kt          // Platzhalter-Screen
│   │   └── PlansViewModel.kt
│   ├── weekplan/
│   │   ├── WeekPlanScreen.kt       // Platzhalter-Screen
│   │   └── WeekPlanViewModel.kt
│   └── components/
│       └── EmptyState.kt           // Wiederverwendbarer Platzhalter
└── util/
    └── DateTimeUtils.kt
```

### 1.3 Navigation

Die App verwendet eine Bottom Navigation Bar mit drei Tabs:

| Tab | Label | Icon | Route |
|-----|-------|------|-------|
| 1 | Aktivitäten | `Icons.Outlined.Category` | `activities` |
| 2 | Abläufe | `Icons.Outlined.ViewList` | `plans` |
| 3 | Wochenplan | `Icons.Outlined.CalendarMonth` | `weekplan` |

Die Navigation wird über eine Sealed Class oder Sealed Interface definiert:

```kotlin
sealed interface Screen {
    @Serializable data object Activities : Screen
    @Serializable data object Plans : Screen
    @Serializable data object WeekPlan : Screen
    // Spätere Screens:
    @Serializable data class PlanEditor(val planId: Long? = null) : Screen
    @Serializable data object Settings : Screen
}
```

Jeder Tab-Screen zeigt vorerst nur einen Platzhalter-Text (z.B. „Aktivitäten – Inhalt folgt in Phase 2").

### 1.4 Theme und Design-Sprache

Die App verwendet Material 3 mit einer warmen, kindgerechten Farbpalette. Das Standardtheme heißt „Honig" und basiert auf folgenden Farben:

```kotlin
// Primärfarben (warm, einladend)
val Primary = Color(0xFFE8A87C)        // Warmes Pfirsich/Apricot
val OnPrimary = Color(0xFF442B1A)
val PrimaryContainer = Color(0xFFF2D8A7) // Helles Honiggelb
val OnPrimaryContainer = Color(0xFF2C1A0A)

// Sekundärfarben (beruhigend)
val Secondary = Color(0xFFA3BFAD)       // Salbeigrün
val OnSecondary = Color(0xFF1A2E20)
val SecondaryContainer = Color(0xFFD9E8DD)
val OnSecondaryContainer = Color(0xFF0F1F14)

// Tertiärfarben (Akzent)
val Tertiary = Color(0xFFA9C6D9)        // Sanftes Blau
val OnTertiary = Color(0xFF1A2C36)
val TertiaryContainer = Color(0xFFD4E6F0)
val OnTertiaryContainer = Color(0xFF0E1E28)

// Hintergrund
val Background = Color(0xFFF2EEEB)      // Warmes Off-White
val Surface = Color(0xFFFFFBF8)
val SurfaceVariant = Color(0xFFF0E6DA)

// Karten-Farben für Aktivitäten (8 wählbare Farben)
val ActivityColors = listOf(
    Color(0xFFF2D8A7), // Honig
    Color(0xFFD9E8DD), // Salbei
    Color(0xFFD4E6F0), // Himmel
    Color(0xFFF2BEA0), // Pfirsich
    Color(0xFFE8D4E8), // Lavendel
    Color(0xFFF5E6A3), // Sonnengelb
    Color(0xFFD4ECEC), // Mint
    Color(0xFFF0D4D4), // Rosa
)
```

Typografie: Verwende `Rounded`-Varianten oder eine freundliche Sans-Serif-Schrift. Runde Ecken (12dp für Karten, 16dp für Dialoge).

### 1.5 Hilt-Setup

**DatabaseModule:**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "routineheld.db"
        ).build()
    }

    @Provides fun provideActivityDao(db: AppDatabase) = db.activityDao()
    @Provides fun provideRoutinePlanDao(db: AppDatabase) = db.routinePlanDao()
    @Provides fun provideWeekPlanDao(db: AppDatabase) = db.weekPlanDao()
}
```

**RepositoryModule:**
Binde Repository-Interfaces an ihre Implementierungen via `@Binds` oder stelle sie direkt als `@Provides` bereit.

---

## AP-02: Datenmodell und Datenbank

### 2.1 Entity-Definitionen

#### ActivityEntity

Repräsentiert eine einzelne Aktivität (z.B. „Zähne putzen", „Frühstücken").

```kotlin
@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // Max 30 Zeichen, nicht leer
    val iconRef: String,                 // Referenz auf Icon-Asset, z.B. "ic_teeth_brushing"
    val color: Int? = null,              // Index in ActivityColors (0-7), null = kein Farbcode
    val isUserCreated: Boolean = true,   // false für vordefinierte Aktivitäten aus Vorlagen
    val createdAt: Long = System.currentTimeMillis()
)
```

#### RoutinePlanEntity

Ein Ablaufplan — eine benannte, geordnete Sammlung von Aktivitäten.

```kotlin
@Entity(tableName = "routine_plans")
data class RoutinePlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // Max 40 Zeichen, nicht leer
    val slotCount: Int,                  // 3–12, Anzahl der Plätze
    val isTemplate: Boolean = false,     // true für mitgelieferte Vorlagen
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### RoutinePlanEntryEntity

Verknüpft eine Aktivität mit einem Platz in einem Ablaufplan.

```kotlin
@Entity(
    tableName = "routine_plan_entries",
    foreignKeys = [
        ForeignKey(
            entity = RoutinePlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("planId"),
        Index("activityId"),
        Index(value = ["planId", "position"], unique = true)
    ]
)
data class RoutinePlanEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val planId: Long,
    val activityId: Long,
    val position: Int                    // 0-basiert, bestimmt Reihenfolge
)
```

#### WeekPlanEntity

Ein Wochenplan — ein benannter Container für Wochenplan-Slots.

```kotlin
@Entity(tableName = "week_plans")
data class WeekPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                    // z.B. "Normaler Wochentag", "Ferienplan"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

#### WeekPlanSlotEntity

Verknüpft einen Wochenplan-Slot (Tag + Tageszeit) mit einem Ablaufplan oder einer einzelnen Aktivität.

```kotlin
@Entity(
    tableName = "week_plan_slots",
    foreignKeys = [
        ForeignKey(
            entity = WeekPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["weekPlanId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RoutinePlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["routinePlanId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("weekPlanId"),
        Index("routinePlanId"),
        Index("activityId"),
        Index(value = ["weekPlanId", "dayOfWeek", "timeOfDay"], unique = true)
    ]
)
data class WeekPlanSlotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val weekPlanId: Long,
    val dayOfWeek: Int,                  // 1=Montag, 2=Dienstag, ..., 7=Sonntag
    val timeOfDay: Int,                  // 0=Morgens, 1=Mittags, 2=Abends
    val routinePlanId: Long? = null,     // Referenz auf einen Ablaufplan ODER
    val activityId: Long? = null         // Referenz auf eine einzelne Aktivität (nur eins von beiden)
)
```

### 2.2 Relations

```kotlin
data class RoutinePlanWithEntries(
    @Embedded val plan: RoutinePlanEntity,
    @Relation(
        entity = RoutinePlanEntryEntity::class,
        parentColumn = "id",
        entityColumn = "planId"
    )
    val entries: List<RoutinePlanEntryEntity>
)

data class WeekPlanWithSlots(
    @Embedded val weekPlan: WeekPlanEntity,
    @Relation(
        entity = WeekPlanSlotEntity::class,
        parentColumn = "id",
        entityColumn = "weekPlanId"
    )
    val slots: List<WeekPlanSlotEntity>
)
```

### 2.3 DAOs

#### ActivityDao

```kotlin
@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY name ASC")
    fun getAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getById(id: Long): ActivityEntity?

    @Query("SELECT * FROM activities WHERE isUserCreated = 0")
    fun getDefaultActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT COUNT(*) FROM routine_plan_entries WHERE activityId = :activityId")
    suspend fun getUsageCount(activityId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: ActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activities: List<ActivityEntity>): List<Long>

    @Update
    suspend fun update(activity: ActivityEntity)

    @Delete
    suspend fun delete(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

#### RoutinePlanDao

```kotlin
@Dao
interface RoutinePlanDao {
    @Query("SELECT * FROM routine_plans ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<RoutinePlanEntity>>

    @Query("SELECT * FROM routine_plans WHERE isTemplate = 1")
    fun getTemplates(): Flow<List<RoutinePlanEntity>>

    @Query("SELECT * FROM routine_plans WHERE isTemplate = 0 ORDER BY updatedAt DESC")
    fun getUserPlans(): Flow<List<RoutinePlanEntity>>

    @Transaction
    @Query("SELECT * FROM routine_plans WHERE id = :id")
    fun getPlanWithEntries(id: Long): Flow<RoutinePlanWithEntries?>

    @Transaction
    @Query("SELECT * FROM routine_plans WHERE id = :id")
    suspend fun getPlanWithEntriesOnce(id: Long): RoutinePlanWithEntries?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: RoutinePlanEntity): Long

    @Update
    suspend fun update(plan: RoutinePlanEntity)

    @Delete
    suspend fun delete(plan: RoutinePlanEntity)

    // Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: RoutinePlanEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<RoutinePlanEntryEntity>)

    @Delete
    suspend fun deleteEntry(entry: RoutinePlanEntryEntity)

    @Query("DELETE FROM routine_plan_entries WHERE planId = :planId")
    suspend fun deleteAllEntries(planId: Long)

    @Query("UPDATE routine_plan_entries SET position = :newPosition WHERE id = :entryId")
    suspend fun updateEntryPosition(entryId: Long, newPosition: Int)
}
```

#### WeekPlanDao

```kotlin
@Dao
interface WeekPlanDao {
    @Query("SELECT * FROM week_plans ORDER BY updatedAt DESC")
    fun getAll(): Flow<List<WeekPlanEntity>>

    @Transaction
    @Query("SELECT * FROM week_plans WHERE id = :id")
    fun getWeekPlanWithSlots(id: Long): Flow<WeekPlanWithSlots?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weekPlan: WeekPlanEntity): Long

    @Update
    suspend fun update(weekPlan: WeekPlanEntity)

    @Delete
    suspend fun delete(weekPlan: WeekPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: WeekPlanSlotEntity): Long

    @Update
    suspend fun updateSlot(slot: WeekPlanSlotEntity)

    @Query("DELETE FROM week_plan_slots WHERE id = :slotId")
    suspend fun deleteSlot(slotId: Long)

    @Query("DELETE FROM week_plan_slots WHERE weekPlanId = :weekPlanId")
    suspend fun deleteAllSlots(weekPlanId: Long)
}
```

### 2.4 Repository-Schicht

Jedes Repository kapselt den DAO-Zugriff und bietet eine saubere API für ViewModels. Die Repositories verwenden Constructor Injection via Hilt.

Beispiel für `ActivityRepository`:

```kotlin
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao
) {
    fun getAllActivities(): Flow<List<ActivityEntity>> = activityDao.getAll()

    suspend fun getById(id: Long): ActivityEntity? = activityDao.getById(id)

    suspend fun getUsageCount(activityId: Long): Int = activityDao.getUsageCount(activityId)

    suspend fun create(name: String, iconRef: String, color: Int? = null): Long {
        return activityDao.insert(
            ActivityEntity(name = name, iconRef = iconRef, color = color)
        )
    }

    suspend fun update(activity: ActivityEntity) = activityDao.update(activity)

    suspend fun delete(id: Long) = activityDao.deleteById(id)
}
```

Analoges Pattern für `RoutinePlanRepository` und `WeekPlanRepository`.

### 2.5 Database-Definition

```kotlin
@Database(
    entities = [
        ActivityEntity::class,
        RoutinePlanEntity::class,
        RoutinePlanEntryEntity::class,
        WeekPlanEntity::class,
        WeekPlanSlotEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun routinePlanDao(): RoutinePlanDao
    abstract fun weekPlanDao(): WeekPlanDao
}
```

### 2.6 Unit-Tests

Erstelle instrumentierte Tests (androidTest) für alle DAOs. Verwende eine In-Memory-Datenbank. Folgende Szenarien müssen abgedeckt sein (Minimum 15 Tests):

**ActivityDao (5 Tests):**
1. Insert und Abruf einer Aktivität
2. Update einer bestehenden Aktivität
3. Löschen einer Aktivität
4. `getUsageCount` gibt 0 zurück bei unverwendeter Aktivität
5. `getUsageCount` gibt korrekte Zahl zurück bei verwendeter Aktivität

**RoutinePlanDao (6 Tests):**
1. Insert eines Plans und Abruf via `getAll`
2. Insert von Entries und Abruf via `getPlanWithEntries`
3. `deleteAllEntries` entfernt alle Entries eines Plans
4. `updateEntryPosition` ändert die Position korrekt
5. CASCADE-Delete: Löschen eines Plans entfernt auch dessen Entries
6. CASCADE-Delete: Löschen einer Aktivität entfernt referenzierende Entries

**WeekPlanDao (4 Tests):**
1. Insert eines Wochenplans und Abruf
2. Insert von Slots und Abruf via `getWeekPlanWithSlots`
3. Unique-Constraint: Gleiche Kombination weekPlanId + dayOfWeek + timeOfDay verhindert Duplikat
4. SET_NULL: Löschen eines referenzierten Plans setzt `routinePlanId` auf null

---

## Abnahmekriterien Phase 1

| # | Kriterium | Prüfmethode |
|---|-----------|-------------|
| 1 | Das Projekt kompiliert fehlerfrei mit `./gradlew assembleDebug` | Build-Log |
| 2 | Die App startet auf einem Emulator (API 26+) ohne Crash | Manueller Test |
| 3 | Drei Tabs (Aktivitäten, Abläufe, Wochenplan) sind sichtbar und navigierbar | Manueller Test |
| 4 | Jeder Tab zeigt einen Platzhalter-Text | Manueller Test |
| 5 | Die Room-Datenbank wird beim ersten Start erstellt | Logcat: Room-Initialisierung |
| 6 | Alle DAOs sind per Hilt injizierbar | Compile-Time-Prüfung (Hilt generiert Factories) |
| 7 | Mindestens 15 Unit-Tests laufen grün mit `./gradlew connectedAndroidTest` | Test-Report |
| 8 | Version Catalog (`libs.versions.toml`) definiert alle Dependencies zentral | Datei-Prüfung |
| 9 | Schema-Export ist aktiviert (`exportSchema = true`) und erzeugt JSON unter `schemas/` | Datei-Prüfung |

---

## Hinweise für den AI-Agenten

- Verwende die aktuelle stabile Version aller Jetpack-Libraries (keine Alpha/Beta).
- Stelle sicher, dass `kapt` oder `ksp` für Room und Hilt korrekt konfiguriert ist (bevorzugt KSP wo möglich).
- Die `ActivityColors`-Liste ist im Theme-Paket definiert und wird später in der UI referenziert.
- Der `iconRef`-String in `ActivityEntity` wird in Phase 2 mit echten Asset-Namen befüllt. Für Tests verwende Platzhalter-Strings wie `"ic_placeholder"`.
- Teste die Datenbank in `androidTest`, nicht in `test` (Room benötigt Android-Context).
