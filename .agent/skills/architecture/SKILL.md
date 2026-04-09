---
name: architecture
description: Multi-module clean architecture for the Braintraining app — module map, dependency rules, layer responsibilities, data flow, DI, and navigation patterns.
---

# Architecture Skill

## Overview

Braintraining is an Android app built with Jetpack Compose, following the **Now in Android (NiA)** multi-module clean architecture. It is organized into three top-level module groups: `app`, `core`, and `feature`/`game`. Each layer has a strict dependency direction; inner layers never depend on outer ones.

---

## Module Structure

```
braintraining/
├── app/                        # Application entry point, DI root, navigation
├── core/
│   ├── model/                  # Pure Kotlin domain models (no Android deps)
│   ├── database/               # Room database, DAOs, entities, migrations
│   ├── network/                # Retrofit APIs, DTOs, NetworkDataSource
│   ├── data/                   # Repositories (interface + default impl), DI modules
│   └── systemdesign/           # Compose theme, colors, typography, shapes
├── feature/
│   ├── home/                   # Home screen + HomeViewModel
│   └── games/                  # Game list + detail screens + GameViewModel
└── game/
    └── compute_challenge/      # Self-contained game module (full screen)
```

**Dependency rules (strictest first):**
- `core:model` ← no project dependencies
- `core:database`, `core:network` ← depend only on `core:model`
- `core:data` ← depends on `core:database` + `core:network` + `core:model`
- `feature:*`, `game:*` ← depend on `core:data` + `core:model` + `core:systemdesign`
- `app` ← depends on everything; owns navigation and the Application class

---

## Layer Responsibilities

### `core:model`
- Plain Kotlin `data class` objects that represent the domain (e.g., `Game`, `Skill`, `SkillWithGame`).
- Zero Android or framework imports.

### `core:database`
- Room `@Database`, `@Entity` classes, `@Dao` interfaces.
- Extension functions `Entity.asExternalModel()` and `DomainModel.asEntity()` for mapping — defined in the entity file itself (NiA convention).
- `DatabaseMigrations` object for schema migrations.
- DI: `DatabaseModule` (provides `RoomDatabase`), `DaosModule` (provides individual DAOs).

### `core:network`
- Retrofit `@Api` interfaces (`GameApi`, `SkillApi`).
- Network DTOs (`NetworkGame`, `NetworkSkill`, `GameDto`, `SkillDto`).
- `NetworkDataSource` — `@Singleton` facade that wraps all API interfaces and exposes suspend functions.
- DI: `NetworkModule` (provides Retrofit, OkHttpClient, converters).

### `core:data`
- Repository interfaces (`GameRepository`, `SkillRepository`) — the contract the feature layer depends on.
- `Default*Repository` implementations (`@Inject constructor`) that coordinate between DAOs and `NetworkDataSource`.
- Repositories expose `Flow<T>` for reads and `suspend fun refresh*()` for remote sync.
- DI: `DataModule` binds interfaces → implementations with `@Binds`.

### `core:systemdesign`
- Compose `Theme`, `Color`, `Type`, `Shape` — app-wide design tokens.
- All UI modules import this; never depends on feature or data modules.

### `feature:*`
- One `@HiltViewModel` per screen, injecting repository interfaces (never DAOs or APIs directly).
- ViewModels expose `StateFlow` collected with `.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue)`.
- Screens are `@Composable` functions that take a `NavController` where navigation is needed.
- No business logic in composables — delegate to ViewModel.

### `game:*`
- Self-contained full-screen game modules.
- May own their own ViewModel and local state.
- Entered via the global `NavGraph`; should hide top/bottom bars using the `setShowTopBar`/`setShowBottomBar` callbacks.

### `app`
- `MainActivity` — single-activity, sets up `NavController`, `Scaffold`, top/bottom bar visibility state.
- `NavGraph.kt` — central `NavHost`; maps `Dest.*` string routes to composable screens.
- `Dest` object — typed route constants including parameterized routes (`"game_detail/{gameId}"`).
- `BottomNavbar` / `TopNavbar` — app chrome composables.
- `BrainTrainingApp` — `@HiltAndroidApp` Application class.

---

## Key Patterns

### Data Flow (offline-first)
```
UI (Composable)
  └─ ViewModel (StateFlow)
       └─ Repository (Flow from Room)
            ├─ Room DB  ← source of truth
            └─ Network  ← refresh via suspend fun refresh*()
```
Repositories never return network data directly to the UI. Room is always the single source of truth. Refreshes are triggered explicitly (e.g., on screen load).

### Entity ↔ Model Mapping
Mapping functions live in `core:database` entity files as top-level extension functions:
```kotlin
fun GameEntity.asExternalModel(): Game = ...
fun Game.asEntity(): GameEntity = ...
```

### Dependency Injection (Hilt)
- `@HiltAndroidApp` on Application.
- `@HiltViewModel` on every ViewModel.
- DI modules are scoped per layer: `DatabaseModule`, `DaosModule` in `core:database`; `NetworkModule` in `core:network`; `DataModule` in `core:data`.
- Feature modules do **not** define their own DI modules — Hilt picks up ViewModels automatically.

### Navigation
- String-based `NavHost` routes defined in `Dest` object.
- Arguments passed via route path (`{gameId}`), retrieved from `it.arguments?.getString("id")`.
- Bar visibility controlled by callbacks (`setShowTopBar`, `setShowBottomBar`, `setShowBackButton`) passed from `MainActivity` into `NavGraph`.

### State Management
- ViewModel exposes `StateFlow` via `.stateIn()` with `WhileSubscribed(5000)` to survive configuration changes and drop subscription 5 s after the last collector.
- UI collects with `collectAsStateWithLifecycle()` (preferred) or `collectAsState()`.

---

## Adding a New Feature

1. **New screen in existing feature module** — add `Screen.kt` + `ViewModel.kt`, inject repository interface, expose `StateFlow`, add route to `Dest` and `NavGraph`.
2. **New feature module** — create `feature/<name>/build.gradle.kts` mirroring existing feature modules; register in `settings.gradle.kts`; add `:feature:<name>` dependency in `app/build.gradle.kts`.
3. **New domain entity** — add model to `core:model`, entity + mapping to `core:database`, DTO to `core:network`, repository interface + impl to `core:data`, register DAO/module in DI.
4. **New self-contained game** — create `game/<name>/` module, set full-screen flag via `NavGraph` callbacks, add route to `Dest`.

---

## Tech Stack

| Concern | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| DI | Hilt (Dagger) |
| Async | Kotlin Coroutines + Flow |
| Local DB | Room |
| Network | Retrofit + OkHttp + kotlinx.serialization |
| Build | Gradle Kotlin DSL + Version Catalog (`libs.versions.toml`) |
| Code style | Spotless |
