# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**RoutineHeld** is an Android family routine planner app. Parents create visual routine plans and weekly schedules for their children using icons and drag-and-drop. Plans can be exported as PDFs to print and display at home.

- **Package**: `de.routineheld.app`
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35

## Common Commands

### Build & Run
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install and run on connected device/emulator
./gradlew installDebug
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest --tests "de.routineheld.app.DatabaseTest"
```

### Database Schema
```bash
# Room schema is exported to app/schemas/
# After database changes, the schema JSON is auto-generated during build
```

### Linting & Code Quality
```bash
# Run lint checks
./gradlew lint

# Format code (if ktlint is added)
./gradlew ktlintFormat
```

## Architecture

### Pattern: MVVM + Repository

```
UI Layer (Compose Screens)
    ↓ observes StateFlow/Flow
ViewModels (@HiltViewModel)
    ↓ calls
Repositories (@Inject)
    ↓ accesses
DAOs (Room)
    ↓ queries
Database (AppDatabase)
```

### Key Components

- **Single Activity**: `MainActivity` with Jetpack Compose
- **Navigation**: Type-safe navigation using Kotlin Serialization (`@Serializable` sealed interface `Screen`)
- **DI**: Hilt for dependency injection (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **Database**: Room with 5 entities and cascade delete relationships

## Database Schema

The database models a hierarchy: **Activities** → **RoutinePlans** → **WeekPlans**

### Entities

1. **ActivityEntity**: Individual activities (e.g., "Brush teeth", "Breakfast")
   - Fields: `id`, `name`, `iconRef`, `color`, `isUserCreated`, `createdAt`

2. **RoutinePlanEntity**: Named sequence of activities (e.g., "Morning routine")
   - Fields: `id`, `name`, `slotCount`, `isTemplate`, `createdAt`, `updatedAt`

3. **RoutinePlanEntryEntity**: Links an activity to a plan slot
   - Fields: `id`, `planId`, `activityId`, `position`
   - Foreign keys: CASCADE delete on `planId` and `activityId`
   - Unique constraint: `(planId, position)` ensures no duplicate positions

4. **WeekPlanEntity**: Container for weekly schedule
   - Fields: `id`, `name`, `createdAt`, `updatedAt`

5. **WeekPlanSlotEntity**: Maps day+time to a routine plan or single activity
   - Fields: `id`, `weekPlanId`, `dayOfWeek` (1-7), `timeOfDay` (0-2), `routinePlanId`, `activityId`
   - Foreign keys: CASCADE delete on `weekPlanId`, SET_NULL on `routinePlanId` and `activityId`
   - Unique constraint: `(weekPlanId, dayOfWeek, timeOfDay)`
   - Either `routinePlanId` OR `activityId` should be set (not both)

### Relations

- **RoutinePlanWithEntries**: `@Embedded` plan + `@Relation` list of entries
- **WeekPlanWithSlots**: `@Embedded` weekPlan + `@Relation` list of slots

### DAOs

All DAOs use `Flow` for reactive queries and `suspend` for mutations:
- **ActivityDao**: CRUD + `getUsageCount()` to check if activity is used in plans
- **RoutinePlanDao**: Manages plans and entries, includes `getPlanWithEntries()` with `@Transaction`
- **WeekPlanDao**: Manages week plans and slots

## Navigation Structure

Type-safe navigation using Kotlin Serialization:

```kotlin
@Serializable
sealed interface Screen {
    data object Activities
    data object Plans
    data object WeekPlan
    data class PlanEditor(val planId: Long? = null)
    data object Settings
}
```

Bottom navigation shows 3 main tabs:
- **Activities** (Icons.Outlined.Category)
- **Plans/Abläufe** (Icons.Outlined.ViewList)
- **WeekPlan** (Icons.Outlined.CalendarMonth)

Navigation uses `navController.navigate(screen)` with state preservation.

## UI Theme

Material 3 with custom warm, child-friendly color palette:
- **Primary**: Warm peach/apricot (`#E8A87C`)
- **Primary Container**: Light honey yellow (`#F2D8A7`)
- **Secondary**: Sage green (`#A3BFAD`)
- **Tertiary**: Soft blue (`#A9C6D9`)
- **Background**: Warm off-white (`#F2EEEB`)

8 activity card colors defined in `ActivityColors` list for color-coding activities.

## Development Phases

Project is organized in phases (see `claude-tasks/` directory):

1. **Phase 1** (current): Foundation - project setup, database, navigation, empty screens
2. **Phase 2**: Activity management with icon library
3. **Phase 3**: Routine plan creation with drag-and-drop
4. **Phase 4**: PDF export for routine plans
5. **Phase 5**: Week plan creation and export
6. **Phase 6**: Polish - templates, onboarding, settings

Each phase has detailed specifications in `claude-tasks/phase-N-*.md`.

## Testing Strategy

- **Unit tests**: In `app/src/test/` (local JVM tests)
- **Instrumented tests**: In `app/src/androidTest/` (requires Android runtime)
- **Database tests**: Must be instrumented tests (Room needs Android context)
  - Use in-memory database: `Room.inMemoryDatabaseBuilder()`
  - Test cascade deletes, foreign key constraints, and unique constraints
  - Minimum 15 DAO tests covering CRUD and relationships

## Project Structure

```
app/src/main/java/de/routineheld/app/
├── MainActivity.kt
├── RoutineHeldApp.kt
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt
│   │   ├── entity/         (Room entities)
│   │   ├── dao/            (Room DAOs)
│   │   └── relation/       (Embedded relations)
│   └── repository/         (Repository pattern)
├── di/                     (Hilt modules)
├── navigation/             (Type-safe navigation)
├── ui/
│   ├── theme/
│   ├── components/
│   ├── activities/         (Feature: Activity management)
│   ├── plans/              (Feature: Routine plans)
│   └── weekplan/           (Feature: Week planning)
└── util/
```

## Key Conventions

- **ViewModels**: Use `@HiltViewModel` and inject repositories via constructor
- **State**: Expose UI state as `StateFlow` or `Flow`, collect in Composables
- **Database operations**: Always use `suspend` or `Flow`, never blocking calls
- **Foreign keys**: Check cascade behavior - most use CASCADE delete, some use SET_NULL
- **Version Catalog**: All dependencies in `gradle/libs.versions.toml`, reference with `libs.*`
- **KSP**: Used instead of KAPT for Room and Hilt annotation processing
