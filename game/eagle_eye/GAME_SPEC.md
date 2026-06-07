# Eagle Eye — Game Specification

**Lumosity equivalent:** Eagle Eye
**Skill area:** `SkillArea.ATTENTION`
**Module:** `game:eagle_eye`

---

## Core Mechanic

A grid of symbols appears — all identical except **one odd one out**. The player taps the different symbol as fast as possible. Tests visual search, peripheral attention, and the ability to discriminate subtle differences under time pressure.

---

## Gameplay Loop

1. A 3-second countdown plays.
2. A grid of N×N symbols appears with one cell visually different from the rest.
3. Player taps the odd-one-out cell.
4. Correct → points awarded based on response speed; brief green flash.
5. Wrong → streak resets, −1 pt; brief red shake, then correct cell is revealed.
6. Timeout (> 4 s) → treated as wrong.
7. Next grid appears after a 400 ms pause.
8. Game ends after **60 seconds**.

---

## Symbol Properties

Three visual properties can differ between the target and distractors:

| Property | Distractor value | Target value (example) |
|---|---|---|
| Direction | All pointing right `→` | One pointing left `←` |
| Color | All blue | One red |
| Shape | All circles | One diamond |

At Hard difficulty two properties differ simultaneously (e.g., different shape **and** rotated 15°) to create near-matches that require careful scrutiny.

---

## Difficulty Ramp

| Phase | Trigger (total correct) | Grid size | Difference type | Timeout |
|---|---|---|---|---|
| Easy | 0–10 | 3×3 (9 cells) | Direction only | 4.0 s |
| Medium | 11–25 | 4×4 (16 cells) | Direction + color | 4.0 s |
| Hard | 26+ | 5×5 (25 cells) | Direction + color + shape; subtle 15° rotation | 4.0 s |

The timeout is constant across phases — the larger grid and subtler difference provide the difficulty increase.

---

## Scoring

```
correct + fast   (responseMs < 1000)  → +3 pts × multiplier
correct + normal (responseMs < 2500)  → +2 pts × multiplier
correct + slow   (responseMs ≥ 2500)  → +1 pt  × multiplier
wrong tap                              → streak resets, −1 pt (floor 0)
timeout                                → treated as wrong, streak resets
```

### Multiplier

| Correct-answer streak | Multiplier |
|---|---|
| 0–3 | ×1 |
| 4–7 | ×2 |
| 8+ | ×3 |

---

## Module Contract

```kotlin
@Composable
fun EagleEyeScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
enum class SymbolDirection { RIGHT, LEFT, UP, DOWN }
enum class SymbolColor { BLUE, RED, GREEN, YELLOW }
enum class SymbolShape { CIRCLE, SQUARE, TRIANGLE, DIAMOND }
enum class GridPhase { EASY, MEDIUM, HARD }

data class GridSymbol(
    val direction: SymbolDirection,
    val color: SymbolColor,
    val shape: SymbolShape,
    val rotationDegrees: Float,  // 0f normal; 15f for hard-phase subtle variant
)

data class GridState(
    val size: Int,               // 3, 4, or 5
    val cells: List<GridSymbol>, // size × size, row-major order
    val oddIndex: Int,           // index of the odd-one-out cell
)

sealed interface EagleEyeUiState {

    data class Countdown(
        val secondsLeft: Int,
    ) : EagleEyeUiState

    data class Playing(
        val grid: GridState,
        val gridIndex: Int,           // key for AnimatedContent grid transitions
        val gridShownAtMs: Long,      // used by ViewModel to compute responseMs
        val score: Int,
        val streak: Int,
        val multiplier: Int,
        val timeRemainingMs: Long,
        val lastCorrect: Boolean?,    // null = awaiting tap; true/false = feedback
        val revealOdd: Boolean,       // true during wrong-answer reveal phase
    ) : EagleEyeUiState

    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val avgResponseMs: Long,      // average of all correct-answer response times
        val easyCorrect: Int,
        val easyTotal: Int,
        val mediumCorrect: Int,
        val mediumTotal: Int,
        val hardCorrect: Int,
        val hardTotal: Int,
        val durationMs: Long,
    ) : EagleEyeUiState
}
```

---

## Key Composables

| Composable | Responsibility |
|---|---|
| `EyeGrid` | Renders an N×N `LazyVerticalGrid` of `GridCell` items; sized to fill available width |
| `GridCell` | Draws the symbol (direction arrow / colored shape) inside a tappable card; animates in with a staggered 20 ms-per-cell fade-in; flashes green on correct, shakes red on wrong |
| `OddReveal` | Briefly highlights the correct cell after a wrong tap (400 ms pulse) |
| `ScoreHeader` | Score, multiplier badge, 60 s countdown timer + progress bar |
| `GameOverContent` | Final score, avg reaction time, per-phase accuracy breakdown, Continue button |

### GridCell visual states

```
Normal      → surfaceVariant background, symbol drawn centred
Correct tap → green background flash (~300 ms), then next grid
Wrong tap   → red background + subtle horizontal shake (~200 ms)
Reveal      → yellow highlight on the actual odd cell after a wrong tap
```

### Symbol rendering

Draw symbols with `Canvas` + `Path` inside each cell, rotated by `GridSymbol.rotationDegrees`:
- **Arrow** — filled arrowhead pointing in `direction` (primary use case for Easy)
- **Circle / Square / Triangle / Diamond** — filled shapes in `GridSymbol.color` (Medium+)
- Apply `rotate(rotationDegrees)` on the `DrawScope` for the Hard-phase subtle variant

---

## Grid Generation Algorithm

```
1. Pick distractor template:
   - direction  = random SymbolDirection
   - color      = random SymbolColor (MEDIUM+ only)
   - shape      = random SymbolShape (HARD only)
   - rotation   = 0f

2. Fill all size×size cells with the distractor template.

3. Pick oddIndex = random index ≠ any previous oddIndex (avoid same position twice in a row).

4. Mutate the odd cell:
   - EASY:   flip direction only         (opposite of distractor direction)
   - MEDIUM: flip direction + change color
   - HARD:   flip direction + change color + change shape + rotation = 15f

5. Verify the odd cell is visually distinct from all neighbours
   (trivially true since all other cells are identical).
```

---

## ViewModel Responsibilities

- Runs 3-second countdown, then starts game timer (100 ms tick) and shows first grid.
- Generates grids via `generateGrid(phase)` on each new round.
- Stamps `gridShownAtMs = System.currentTimeMillis()` when grid appears; reads it on tap to compute `responseMs`.
- Cancellable `timeoutJob` fires 4 s after grid appears, registering a wrong answer if not yet tapped.
- On correct tap: awards points by speed bracket, increments streak; after 300 ms feedback → next grid.
- On wrong tap: decrements score (floor 0), resets streak, sets `revealOdd = true` for 400 ms → next grid.
- Tracks per-phase stats (easy/medium/hard correct/total) and a running `totalResponseMs` sum for average.
- Exposes `uiState: StateFlow<EagleEyeUiState>` and `onCellTapped(index: Int)`.

---

## Dependencies

Only allowed (per game module contract):

- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` enum if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/eagle_eye/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/eagle_eye/
    ├── EagleEyeScreen.kt       ← Composable entry point + all UI composables
    ├── EagleEyeViewModel.kt    ← game loop, grid generation, scoring
    └── EagleEyeUiState.kt      ← sealed interface + GridSymbol + GridState + enums
```

---

## Testing Notes

- Unit-test `generateGrid(EASY)`: all cells identical except `oddIndex`; odd cell has flipped direction.
- Unit-test `generateGrid(HARD)`: odd cell differs in direction, color, shape, and rotation.
- Unit-test `oddIndex` anti-repeat: two consecutive generated grids never share the same `oddIndex`.
- Unit-test scoring: responseMs < 1000 → +3 × multiplier; 1000–2499 → +2; ≥ 2500 → +1.
- Unit-test wrong-tap: score never goes below 0; streak resets to 0.
- Unit-test timeout: registered as wrong answer, streak resets, `revealOdd` becomes true.
- Use `TestCoroutineRule` from `core:testing` to control timer and timeout delays.
- No mocking libraries.
