# BrainTraining — AI Code Plan

## Project Goal

Clone the **Lumosity** brain-training Android app using a **Now in Android (NiA)** multi-module clean architecture. The app offers cognitive games grouped by skill area, tracks user performance over time, and provides daily training sessions.

---

## Architecture Reference

Follows [Now in Android](https://github.com/android/nowinandroid) architecture:

- **Unidirectional data flow**: events flow down, data flows up
- **Offline-first**: Room is the source of truth; network syncs into it
- **Three layers**: Data → Domain → UI
- **Multi-module**: `core:*` for shared infrastructure, `feature:*` for UI features, `game:*` for individual game implementations
- **Hilt** for DI throughout; all data components are interfaces with concrete implementations
- **Kotlin Flow / StateFlow** for reactive data streams
- **Jetpack Compose + Material3** for all UI

---

## Module Map

### Existing Modules

| Module | Role |
|---|---|
| `app` | Entry point, navigation host, Hilt application |
| `core:model` | Domain models: `Game`, `Skill`, `SkillWithGame` |
| `core:database` | Room DB, DAOs, entities |
| `core:network` | Retrofit APIs, DTOs |
| `core:data` | Repositories: `GameRepository`, `SkillRepository` |
| `core:firebase` | Firebase Auth + Firestore (stub) |
| `core:systemdesign` | Color, Type, Shape, AppTheme (Material3) |
| `feature:home` | Home screen — recommended daily games |
| `feature:games` | Game list + detail screens |
| `feature:account` | User profile screen (stub) |
| `game:compute_challenge` | First game implementation |

### Modules to Add

#### Core

| Module | Purpose |
|---|---|
| `core:domain` | Use cases (single-`invoke` classes); sits between data and UI layers |
| `core:testing` | Shared test utilities, fake repositories, coroutine test rules |
| `core:analytics` | Firebase Analytics wrapper; `AnalyticsHelper` interface + impl |
| `core:datastore` | Proto DataStore for user preferences (daily goal, reminder time, theme) |
| `core:notifications` | WorkManager-based reminder scheduler |

#### Feature

| Module | Purpose |
|---|---|
| `feature:training` | Daily training session flow (game queue, session summary) |
| `feature:results` | Post-game result screen + BPI chart |
| `feature:progress` | Historical performance graphs per skill area |
| `feature:onboarding` | First-run skill assessment flow |
| `feature:settings` | App preferences (theme, notifications, account) |

#### Game

Each game is its own module under `game/`:

| Module | Lumosity Equivalent | Skill Area |
|---|---|---|
| `game:compute_challenge` | Raindrops | Flexibility / Math |
| `game:memory_matrix` | Memory Matrix | Memory |
| `game:speed_match` | Speed Match | Speed |
| `game:train_of_thought` | Train of Thought | Attention |
| `game:word_bubbles` | Word Bubbles | Language |
| `game:color_match` | Color Match | Flexibility |
| `game:pattern_recall` | Scape Plan | Memory |
| `game:lost_in_migration` | Lost in Migration | Attention |
| `game:eagle_eye` | Eagle Eye | Attention |
| `game:familiar_faces` | Familiar Faces | Memory |

---

## Domain Models (core:model)

Extend existing models with:

```kotlin
// Skill areas
enum class SkillArea { MEMORY, ATTENTION, SPEED, FLEXIBILITY, PROBLEM_SOLVING, LANGUAGE }

// Game result after a session
data class GameResult(
    val gameId: String,
    val score: Int,
    val durationMs: Long,
    val playedAt: Instant,
    val skillArea: SkillArea,
)

// Aggregated brain performance index per skill area
data class PerformanceSnapshot(
    val skillArea: SkillArea,
    val bpi: Float,           // 0–1000 normalized score
    val takenAt: Instant,
)

// User preferences
data class UserPreferences(
    val dailyGameGoal: Int,   // default 3
    val reminderEnabled: Boolean,
    val reminderTimeMinutes: Int,  // minutes since midnight
    val useDarkTheme: Boolean,
)
```

---

## Data Layer (core:data + core:database + core:network)

### Repositories to Add

| Interface | Implementation | Data Sources |
|---|---|---|
| `GameResultRepository` | `DefaultGameResultRepository` | Room (local); Firestore (remote sync) |
| `PerformanceRepository` | `DefaultPerformanceRepository` | Derived from `GameResultRepository` |
| `UserPreferencesRepository` | `DefaultUserPreferencesRepository` | Proto DataStore only |
| `TrainingSessionRepository` | `DefaultTrainingSessionRepository` | Room + daily schedule logic |

### Offline-First Strategy

1. All writes go to Room first, then sync to Firestore via `WorkManager` with exponential backoff.
2. On app start, fetch latest remote data and upsert into Room.
3. Repositories always emit from Room Flows — UI never reads directly from network.

---

## Domain Layer (core:domain)

Use cases follow the NiA convention: one public `invoke` operator, injected via Hilt, no state.

| Use Case | Returns |
|---|---|
| `GetDailyTrainingSessionUseCase` | `Flow<TrainingSession>` |
| `RecordGameResultUseCase` | `suspend fun invoke(result: GameResult)` |
| `GetSkillPerformanceUseCase` | `Flow<List<PerformanceSnapshot>>` |
| `GetRecommendedGamesUseCase` | `Flow<List<Game>>` |
| `ComputeBpiUseCase` | `Float` (pure calculation) |

---

## UI Layer (feature modules)

### Screen → ViewModel → Use Case wiring

Every feature screen follows this pattern:

```
Screen.kt          — @Composable, no logic, receives UiState + lambdas
ScreenViewModel.kt — StateFlow<UiState>, calls use cases, handles events
UiState.kt         — sealed interface: Loading | Success(data) | Error
```

### Navigation

- Typed routes with `@Serializable` data classes (Compose Navigation 2.8+)
- Bottom bar destinations: **Home**, **Games**, **Progress**, **Account**
- Full-screen destinations (no bottom bar): Training session, Game play, Onboarding
- Each `feature:*` module owns its route data class and `NavGraphBuilder` extension function

---

## Game Module Contract

Every `game:*` module must expose exactly one Composable entry point:

```kotlin
@Composable
fun <GameName>Screen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

The `feature:training` module hosts all game screens and calls `RecordGameResultUseCase` via its ViewModel when `onGameComplete` fires.

Game modules **must not** depend on any `feature:*` or `core:data` modules — only on `core:systemdesign` and `core:model` if needed.

---

## Feature: Daily Training Session

Flow:
1. App opens → `GetDailyTrainingSessionUseCase` returns today's queue of N games (default 3).
2. `feature:training` navigates through each game in sequence.
3. After each game, score is recorded via `RecordGameResultUseCase`.
4. After all games, `feature:results` shows session summary + BPI delta.

Session selection logic:
- Round-robin across skill areas to ensure balanced training.
- Prioritize areas with lowest recent BPI scores.
- Never repeat the same game twice in a session.

---

## Feature: Brain Performance Index (BPI)

- BPI is a 0–1000 score per skill area, derived from the last 10 game results in that area.
- Computed by `ComputeBpiUseCase`: normalizes raw scores against expected score distribution.
- Displayed as a radar/spider chart in `feature:progress`.
- Overall BPI = weighted average across all skill areas.

---

## Firebase Integration (core:firebase)

| Service | Usage |
|---|---|
| Firebase Auth | Anonymous auth on first launch; link to Google account optionally |
| Firestore | Remote backup of `GameResult` and `UserPreferences` |
| Firebase Analytics | Screen views + game completion events via `AnalyticsHelper` |

Auth strategy: always create an anonymous user on first launch so data is persisted to the cloud immediately. Let the user optionally sign in with Google to link their account.

---

## Build Conventions

- All new modules should follow the pattern of existing modules: `build.gradle.kts` with `android { namespace "com.braintraining.<module>" }`.
- Use `libs.versions.toml` for all dependency versions — never hardcode versions in module build files.
- Apply `spotless` + `ktlint` to all new Kotlin files.
- `minSdk = 24`, `compileSdk = 36`, `targetSdk = 36`, `jvmTarget = "11"`.

---

## Testing Strategy (mirrors NiA)

- **No mocking libraries** — use fake implementations of repository interfaces from `core:testing`.
- Unit tests in every `core:domain` use case and ViewModel.
- `core:testing` provides:
  - `FakeGameRepository`, `FakeGameResultRepository`, etc.
  - `TestCoroutineRule` with `StandardTestDispatcher`
  - Compose `createAndroidComposeRule` helpers
- Screenshot tests (Roborazzi) for design system components in `core:systemdesign`.

---

## Development Order (Suggested)

1. **`core:domain`** — add use cases for daily training and result recording
2. **`core:datastore`** — user preferences with Proto DataStore
3. **`core:model`** updates — `GameResult`, `PerformanceSnapshot`, `SkillArea`
4. **`core:database`** updates — add `GameResultEntity`, `PerformanceDao`
5. **`core:data`** updates — `GameResultRepository`, `PerformanceRepository`
6. **`feature:training`** — daily session flow
7. **`feature:results`** — post-game summary
8. **3–4 more `game:*` modules** — `memory_matrix`, `speed_match`, `color_match`
9. **`feature:progress`** — BPI charts per skill area
10. **`feature:onboarding`** — first-run assessment
11. **`core:notifications`** — daily reminder via WorkManager
12. **`feature:settings`** — preferences screen
13. **`core:analytics`** — analytics wrapper
14. Remaining `game:*` modules

---

## Lumosity Feature Parity Checklist

- [ ] Daily training session (N games per day)
- [ ] 10+ distinct cognitive games across 5 skill areas
- [ ] Brain Performance Index (BPI) per skill area
- [ ] Performance history graphs
- [ ] Skill area breakdown (Memory, Attention, Speed, Flexibility, Language)
- [ ] Streaks (consecutive daily training days)
- [ ] User profile with avatar
- [ ] Anonymous → Google account upgrade flow
- [ ] Daily reminder notifications
- [ ] Dark/light theme toggle
- [ ] Offline play (all game data cached locally)
- [ ] Cloud sync of results

---

## Code Style Notes

- No comments unless the WHY is non-obvious.
- No `TODO` stubs — implement or omit.
- Composables: stateless leaf nodes receive state + lambdas; no ViewModel access inside composable functions directly.
- StateFlow over LiveData everywhere.
- Sealed interfaces over sealed classes for UI state.
- `@Immutable` on all Compose UI state data classes.
