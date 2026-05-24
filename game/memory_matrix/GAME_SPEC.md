# Memory Matrix — Game Specification

**Lumosity equivalent:** Memory Matrix  
**Skill area:** `SkillArea.MEMORY`  
**Module:** `game:memory_matrix`

---

## Core Mechanic

A grid of tiles briefly highlights a random pattern, then goes blank. The player must tap the correct tiles to recreate the pattern from memory.

---

## Gameplay Loop

1. **Show phase** (1–2 s, decreasing with level): N tiles on an M×M grid light up.
2. **Hide phase**: all tiles go dark simultaneously.
3. **Recall phase**: player taps tiles they remember; each tap gives immediate visual feedback.
4. **Result phase**: correct tiles reveal green, missed/wrong tiles reveal red → advance to next round or end game.

One mistake ends the game (alternatively: 3-strike variant for a friendlier feel).

---

## Progression Table

| Level | Grid  | Tiles to remember | Show duration |
|-------|-------|-------------------|---------------|
| 1     | 3×3   | 3                 | 2.0 s         |
| 2     | 3×3   | 4                 | 1.8 s         |
| 3     | 4×4   | 5                 | 1.6 s         |
| 4     | 4×4   | 6–7               | 1.4 s         |
| 5+    | 5×5   | 8+                | 1.2 s         |

Grid size grows and show duration shrinks as the player advances.

---

## Score Formula

```
roundScore = tilesCorrect * gridSize * levelMultiplier - timePenalty
```

- `levelMultiplier` starts at 1.0 and increases by 0.2 per level.
- `timePenalty` is 0 during recall (no time pressure in recall phase by default).
- Final score = sum of all round scores.

---

## Module Contract

The screen exposes a single Composable entry point (no `feature:*` or `core:data` dependencies):

```kotlin
@Composable
fun MemoryMatrixScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
sealed interface MemoryMatrixUiState {
    data object Loading : MemoryMatrixUiState

    data class ShowPattern(
        val grid: List<List<Boolean>>,  // true = highlighted tile
        val level: Int,
        val showDurationMs: Long,
    ) : MemoryMatrixUiState

    data class RecallPhase(
        val grid: List<List<Boolean>>,
        val selected: Set<Pair<Int, Int>>,  // player-tapped positions
    ) : MemoryMatrixUiState

    data class RoundResult(
        val grid: List<List<Boolean>>,
        val selected: Set<Pair<Int, Int>>,
        val correct: Boolean,
        val score: Int,
    ) : MemoryMatrixUiState

    data class GameOver(
        val finalScore: Int,
        val level: Int,
        val durationMs: Long,
    ) : MemoryMatrixUiState
}
```

---

## Key Composables

| Composable       | Responsibility                                                              |
|------------------|-----------------------------------------------------------------------------|
| `GridBoard`      | Renders `cols × rows` `TileCell` items in a `LazyVerticalGrid`              |
| `TileCell`       | Animates between `Idle → Highlighted → Hidden → Selected → Correct/Wrong`   |
| `ShowPhaseTimer` | Progress bar counting down the show-phase duration                          |
| `RoundHeader`    | Displays current level and cumulative score                                 |

### TileCell animation states

```
Idle       → neutral background
Highlighted → accent color (shown during ShowPattern)
Hidden      → neutral background (player cannot yet tap)
Selected    → tinted color (player tapped, awaiting result)
Correct     → green
Wrong       → red
```

Use `AnimatedContent` or `animateColorAsState` for smooth transitions.

---

## ViewModel Responsibilities

- Generates the random highlighted-tile pattern for each round.
- Orchestrates the `ShowPattern → RecallPhase → RoundResult` timing using `delay` inside a `viewModelScope` coroutine.
- Validates the player's selected tiles against the pattern on recall completion.
- Advances level and regenerates grid on correct round; triggers `GameOver` on mistake.
- Exposes `uiState: StateFlow<MemoryMatrixUiState>` and a single `onTileTapped(row: Int, col: Int)` event handler.

---

## Dependencies

Only allowed dependencies (per game module contract):

- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` enum if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/memory_matrix/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/memory_matrix/
    ├── MemoryMatrixScreen.kt       ← Composable entry point
    ├── MemoryMatrixViewModel.kt    ← StateFlow + coroutine timing
    └── MemoryMatrixUiState.kt      ← sealed interface
```

---

## Testing Notes

- Unit-test the ViewModel's pattern generation (deterministic with a seeded random).
- Unit-test tile validation logic (correct set vs. selected set comparison).
- Use `TestCoroutineRule` from `core:testing` to control `delay` timing in tests.
- No mocking libraries — use fake implementations only.
