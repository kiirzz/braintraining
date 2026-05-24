# Lost in Migration — Game Specification

**Lumosity equivalent:** Lost in Migration
**Skill area:** `SkillArea.ATTENTION`
**Module:** `game:lost_in_migration`

---

## Core Mechanic

A row of 5 birds appears. The **center bird** is the target. Flanking birds may point in the same or a conflicting direction to distract. Tap the direction the center bird is flying — as fast and accurately as possible.

Based on the **Eriksen Flanker Task**, a classic selective-attention paradigm in cognitive psychology.

---

## Gameplay Loop

1. A 3-second countdown plays.
2. A flock of 5 birds appears (4 flankers + 1 center).
3. Player taps one of 4 directional buttons (↑ ↓ ← →).
4. Correct → score awarded (speed bonus if answered within 400 ms).
5. Wrong or timeout → streak resets, no points.
6. Next flock appears immediately after a 300 ms feedback flash.
7. Game ends after **60 seconds**.

---

## Flock Types

| Type | Flanker configuration | Example |
|---|---|---|
| Congruent | All flankers same as center | `← ← [←] ← ←` |
| Incongruent | All flankers opposite to center | `→ → [←] → →` |
| Mixed | Each flanker random direction | `↑ → [←] ↓ ↑` |

---

## Difficulty Ramp

| Phase | Trigger (total correct) | Flock type | Max display time |
|---|---|---|---|
| Easy | 0–10 | Congruent | 2.5 s |
| Medium | 11–25 | Incongruent | 1.5 s |
| Hard | 26+ | Mixed | 0.8 s |

When max display time expires without an answer, it is treated as a wrong answer.

---

## Scoring

```
answer correct, fast (< 400 ms)  → +2 pts × multiplier
answer correct, normal            → +1 pt  × multiplier
answer wrong or timeout           → streak resets, 0 pts
```

### Multiplier

| Streak | Multiplier |
|---|---|
| 0–4 | ×1 |
| 5–9 | ×2 |
| 10+ | ×3 |

---

## Module Contract

```kotlin
@Composable
fun LostInMigrationScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
enum class BirdDirection { UP, DOWN, LEFT, RIGHT }
enum class FlockType { CONGRUENT, INCONGRUENT, MIXED }

data class Flock(
    val centerDirection: BirdDirection,
    val flankerDirections: List<BirdDirection>,  // exactly 4: [left2, left1, right1, right2]
    val type: FlockType,
)

sealed interface LostInMigrationUiState {

    data class Countdown(val secondsLeft: Int) : LostInMigrationUiState

    data class Playing(
        val flock: Flock,
        val flockIndex: Int,           // key for AnimatedContent transitions
        val score: Int,
        val streak: Int,
        val multiplier: Int,
        val timeRemainingMs: Long,
        val lastAnswerCorrect: Boolean?,  // null = awaiting answer
    ) : LostInMigrationUiState

    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val congruentCorrect: Int,
        val congruentTotal: Int,
        val incongruentCorrect: Int,
        val incongruentTotal: Int,
        val mixedCorrect: Int,
        val mixedTotal: Int,
        val durationMs: Long,
    ) : LostInMigrationUiState
}
```

---

## Key Composables

| Composable | Responsibility |
|---|---|
| `FlockDisplay` | Row of 5 `BirdIcon` composables; center distinguished by size + color |
| `BirdIcon` | Single bird: arrow character in a rounded box; center = larger + primaryContainer bg |
| `DirectionButtons` | Cross layout (↑ on top, ← → in middle, ↓ at bottom); large tap targets |
| `ScoreHeader` | Score, multiplier badge, countdown timer + progress bar |
| `PlayingContent` | Full-screen flash overlay (green/red) for feedback |
| `GameOverContent` | Final score, accuracy breakdown by flock type, Continue button |

### BirdIcon visual rules

```
Flanker bird   → 48 dp, onSurface 65% alpha, no background
Center bird    → 60 dp, primaryColor, primaryContainer background, Bold
```

`AnimatedContent` keyed on `flockIndex` with a 120 ms fade transition handles flock changes.

---

## ViewModel Responsibilities

- Runs 3-second countdown, then starts game timer (100 ms tick) and shows first flock.
- Generates flocks via `generateFlock(phase)` using `BirdDirection.entries.random()`.
  - Congruent: all 4 flankers = center direction.
  - Incongruent: all 4 flankers = center.opposite().
  - Mixed: each flanker independently random.
- Cancellable `cardTimeoutJob` auto-submits wrong answer after `displayTimeMs(phase)`.
- Records `flockShownAtMs = System.currentTimeMillis()` when flock appears; uses it to compute response time for fast-answer bonus.
- Tracks per-type accuracy stats (congruent / incongruent / mixed) for GameOver breakdown.
- Exposes `uiState: StateFlow<LostInMigrationUiState>` and `onAnswer(BirdDirection)`.

---

## Dependencies

Only allowed (per game module contract):

- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` enum if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/lost_in_migration/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/lost_in_migration/
    ├── LostInMigrationScreen.kt    ← Composable entry point + all UI
    ├── LostInMigrationViewModel.kt ← game loop, flock generation, scoring
    └── LostInMigrationUiState.kt   ← sealed interface + BirdDirection + Flock + FlockType
```

---

## Testing Notes

- Unit-test `generateFlock(CONGRUENT)`: verify all 4 flankers == centerDirection.
- Unit-test `generateFlock(INCONGRUENT)`: verify all 4 flankers == centerDirection.opposite().
- Unit-test scoring: fast correct answer (mock responseTime < 400 ms) → +2 × multiplier.
- Unit-test timeout: `processAnswer(isTimeout = true)` → wrong, streak resets, 0 pts.
- Unit-test multiplier thresholds: streak 4 → ×1; streak 5 → ×2; streak 10 → ×3.
- Use `TestCoroutineRule` from `core:testing` to control timer delays.
- No mocking libraries.
