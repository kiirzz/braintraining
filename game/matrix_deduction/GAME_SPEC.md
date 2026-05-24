# Matrix Deduction — Game Specification

**Lumosity equivalent:** (original) — Abstract pattern completion
**Skill area:** `SkillArea.PROBLEM_SOLVING`
**Module:** `game:matrix_deduction`

---

## Core Mechanic

A 3×3 grid of symbol tiles is shown with the **bottom-right cell blank**. The other 8 tiles follow hidden rules governing shape, color, and count. The player picks the correct missing tile from 4 answer choices. Tests abstract reasoning, logical deduction, and rule inference.

---

## Gameplay Loop

1. A 3-second countdown plays.
2. A 3×3 grid appears: 8 filled tiles + 1 blank target cell (bottom-right).
3. Four answer choices appear below the grid.
4. Player taps one choice.
5. Correct → green flash on tapped tile, points awarded; next puzzle after 300 ms.
6. Wrong → red flash on tapped tile + green reveal of correct tile (500 ms); next puzzle.
7. Timeout (> phase timeout) → treated as wrong; correct tile revealed for 500 ms.
8. Game ends after **60 seconds**.

---

## Symbol Properties

Each tile displays a fixed number of shapes of a given type and color:

| Property | Values |
|---|---|
| Shape | Circle, Square, Triangle |
| Color | Blue, Red, Green |
| Count | 1, 2, 3 (number of shapes drawn per tile) |

---

## Grid Rule

For every **active** property, the grid follows the **Latin Row** rule:
- Each row contains all 3 possible values exactly once, in some order.
- The missing cell's value is therefore uniquely determined by the other two cells in row 2.

**Inactive** properties are constant — all 9 tiles share the same value for that property.

---

## Difficulty Ramp

| Phase | Trigger (total correct) | Active properties | Timeout |
|---|---|---|---|
| Easy | 0–8 | Count only | 8 s |
| Medium | 9–20 | Count + Color | 7 s |
| Hard | 21+ | Count + Color + Shape | 6 s |

Example (Hard): Every row has each shape, each color, and each count exactly once. The missing tile is the one combination not yet seen in row 2.

---

## Scoring

```
correct + fast   (responseMs < 2000)  → +4 pts × multiplier
correct + normal (responseMs < 4000)  → +2 pts × multiplier
correct + slow   (responseMs ≥ 4000)  → +1 pt  × multiplier
wrong / timeout                        → streak resets, −1 pt (floor 0)
```

### Multiplier

| Correct-answer streak | Multiplier |
|---|---|
| 0–3 | ×1 |
| 4–7 | ×2 |
| 8+ | ×3 |

---

## Distractor Generation

| Phase | Strategy |
|---|---|
| Easy | 2 distractors: wrong count; 1 distractor: wrong count + wrong color (trap) |
| Medium | 1 distractor changes count only; 1 changes color only; 1 changes both |
| Hard | Each distractor changes exactly 1 property — maximum subtlety |

---

## Module Contract

```kotlin
@Composable
fun MatrixDeductionScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
enum class TileShape { CIRCLE, SQUARE, TRIANGLE }
enum class TileColor { BLUE, RED, GREEN }

data class Tile(
    val shape: TileShape,
    val color: TileColor,
    val count: Int,          // 1, 2, or 3
)

data class MatrixPuzzle(
    val cells: List<Tile?>,  // 9 entries; cells[8] is always null (blank target)
    val answer: Tile,
    val choices: List<Tile>, // 4 choices including answer, shuffled
    val answerIndex: Int,    // index in choices of the correct tile
)

sealed interface MatrixDeductionUiState {

    data class Countdown(
        val secondsLeft: Int,
    ) : MatrixDeductionUiState

    @Immutable
    data class Playing(
        val puzzle: MatrixPuzzle,
        val puzzleIndex: Int,
        val score: Int,
        val streak: Int,
        val multiplier: Int,
        val timeRemainingMs: Long,
        val lastCorrect: Boolean?,    // null = awaiting tap
        val revealAnswerIndex: Int?,  // set after wrong tap to highlight correct choice
        val lastTappedIndex: Int?,    // index of the choice the player tapped (for feedback)
    ) : MatrixDeductionUiState

    @Immutable
    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val avgResponseMs: Long,
        val easyCorrect: Int,
        val easyTotal: Int,
        val mediumCorrect: Int,
        val mediumTotal: Int,
        val hardCorrect: Int,
        val hardTotal: Int,
        val durationMs: Long,
    ) : MatrixDeductionUiState
}
```

---

## Key Composables

| Composable | Responsibility |
|---|---|
| `MatrixGrid` | 3×3 grid of `MatrixCell` + 1 `TargetCell` (blank, bottom-right) |
| `MatrixCell` | Draws tile symbols via Canvas; surfaceVariant background |
| `TargetCell` | Blank cell with primary-colored border + "?" label |
| `ChoiceGrid` | 2×2 grid of 4 `ChoiceTile` items |
| `ChoiceTile` | Same drawing as `MatrixCell`; flashes green/red on feedback |
| `ScoreHeader` | Score, ×multiplier badge, 60 s timer + progress bar |
| `GameOverContent` | Final score, stats, phase breakdown, Continue button |

### ChoiceTile visual states

```
Normal       → surfaceVariant background
Correct tap  → green background (~300 ms)
Wrong tap    → red background on tapped tile; green reveal on correct tile (~500 ms)
```

### Tile rendering

Draw `count` shapes of type `shape` in `color` inside each tile's Canvas:

```
count = 1 → 1 shape centred at (50%, 50%)
count = 2 → shapes at (28%, 50%) and (72%, 50%)
count = 3 → shapes at (20%, 50%), (50%, 50%), and (80%, 50%)
```

Shape radius scales with count: `r = min(w,h) × { 0.28 | 0.20 | 0.14 }` for counts 1/2/3.

Use `drawCircle` for Circle; `drawRect` for Square; `Path` triangle for Triangle.

---

## Puzzle Generation Algorithm

```
1. Determine active properties from phase:
   EASY   → count active; shape+color constant
   MEDIUM → count+color active; shape constant
   HARD   → all three active

2. For each ACTIVE property: generate 3 rows, each a shuffled permutation of [V0,V1,V2].
3. For each INACTIVE property: pick one random constant value for all 9 cells.

4. Build the 9-cell grid: cell(r,c) = Tile(shape[r][c], color[r][c], count[r][c]).
5. answer = cell(2,2).

6. Generate 3 distractors per phase:
   EASY:   (answer.count=wrongC[0]), (answer.count=wrongC[1]), (answer.count=wrongC[0], answer.color=wrongColor)
   MEDIUM: (answer.count=wrongC[0]), (answer.color=wrongColor[0]), (answer.count=wrongC[1], answer.color=wrongColor[1])
   HARD:   (answer.count=wrong), (answer.color=wrong), (answer.shape=wrong)

7. choices = shuffle([distractor×3, answer]); record answerIndex.
```

---

## ViewModel Responsibilities

- Runs 3 s countdown, then starts game timer (100 ms tick).
- Stamps `puzzleShownAtMs = System.currentTimeMillis()` when puzzle appears; computes `responseMs` on tap.
- Cancellable `timeoutJob` fires 6–8 s after puzzle appears (phase-dependent).
- On correct tap: awards `pointsFor(responseMs) × multiplier`, increments streak, 300 ms feedback → next puzzle.
- On wrong/timeout: decrements score (floor 0), resets streak, 500 ms reveal → next puzzle.
- Tracks per-phase stats and running `totalResponseMs` for average.
- Exposes `uiState: StateFlow<MatrixDeductionUiState>` and `onChoiceTapped(index: Int)`.

---

## Dependencies

Only allowed (per game module contract):
- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/matrix_deduction/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/matrix_deduction/
    ├── MatrixDeductionScreen.kt    ← Composable entry point + all UI composables
    ├── MatrixDeductionViewModel.kt ← game loop, puzzle generation, scoring
    └── MatrixDeductionUiState.kt   ← sealed interface + Tile + MatrixPuzzle + enums
```

---

## Testing Notes

- Unit-test `generatePuzzle(EASY)`: shape and color constant; count values in row 2 are all distinct; answer is determined by the missing count.
- Unit-test `generatePuzzle(HARD)`: each of the 3 distractors differs from the answer in exactly 1 property.
- Unit-test distractors distinct: no two choices in a puzzle are equal.
- Unit-test scoring: responseMs < 2000 → +4 × multiplier; 2000–3999 → +2; ≥ 4000 → +1.
- Unit-test wrong tap: score never below 0; streak resets; `revealAnswerIndex == puzzle.answerIndex`.
- Unit-test timeout: treated as wrong; `lastCorrect == false`.
- Use `TestCoroutineRule` from `core:testing` to control timer and timeout delays.
- No mocking libraries.
