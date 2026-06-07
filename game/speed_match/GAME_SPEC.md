# Speed Match — Game Specification

**Lumosity equivalent:** Speed Match  
**Skill area:** `SkillArea.SPEED`  
**Module:** `game:speed_match`

---

## Core Mechanic

Symbols appear one at a time. The player must decide as fast as possible whether the current symbol **matches the previous one**. Tests processing speed and single-item working memory.

---

## Gameplay Loop

1. **Display phase**: a symbol appears in the center of the screen.
2. **Decision phase**: player taps **YES** (matches previous) or **NO** (does not match).
3. Immediately after the tap (or after a max display time), the next symbol appears.
4. Game ends when the 60-second timer runs out.

A running score and streak multiplier are visible at all times.

---

## Symbol Set

| Difficulty phase | Symbol type         | Example values                          |
|------------------|---------------------|-----------------------------------------|
| Easy             | Shape only          | circle, square, triangle, star, diamond |
| Medium           | Shape + color       | red circle, blue square, …              |
| Hard             | Colored shape pair  | both shape AND color must match         |

The game starts in the Easy phase and advances automatically based on correct-answer count.

---

## Difficulty Ramp

| Phase  | Trigger (total correct) | Symbol type    | Max display time |
|--------|-------------------------|----------------|------------------|
| Easy   | 0–10                    | Shape only     | 1.5 s            |
| Medium | 11–25                   | Shape + color  | 1.0 s            |
| Hard   | 26+                     | Colored shapes | 0.7 s            |

When the max display time expires with no answer, it counts as a wrong answer.

---

## Scoring

```
answer correct   → +1 point × current multiplier
answer wrong     → multiplier resets to ×1, no point deducted
no answer (timeout) → treated as wrong
```

### Multiplier

| Streak of correct answers | Multiplier |
|---------------------------|------------|
| 0–2                       | ×1         |
| 3–5                       | ×2         |
| 6+                        | ×3         |

Final score = sum of all awarded points over the 60-second session.

---

## Module Contract

The screen exposes a single Composable entry point (no `feature:*` or `core:data` dependencies):

```kotlin
@Composable
fun SpeedMatchScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
sealed interface SpeedMatchUiState {
    data object Loading : SpeedMatchUiState

    data class Countdown(
        val secondsLeft: Int,
    ) : SpeedMatchUiState

    data class Playing(
        val currentSymbol: Symbol,
        val previousSymbol: Symbol?,   // null on the very first card
        val score: Int,
        val multiplier: Int,           // 1, 2, or 3
        val streak: Int,
        val timeRemainingMs: Long,
        val lastAnswerCorrect: Boolean?,  // null = no answer yet this card
    ) : SpeedMatchUiState

    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val accuracy: Float,           // 0f–1f
        val durationMs: Long,
    ) : SpeedMatchUiState
}

data class Symbol(
    val shape: Shape,
    val color: SymbolColor,
)

enum class Shape { CIRCLE, SQUARE, TRIANGLE, STAR, DIAMOND }
enum class SymbolColor { RED, BLUE, GREEN, YELLOW, PURPLE }
```

---

## Key Composables

| Composable        | Responsibility                                                                 |
|-------------------|--------------------------------------------------------------------------------|
| `SymbolCard`      | Renders the current symbol; animates in with a fade/slide (~150 ms)            |
| `AnswerButtons`   | YES / NO buttons; large tap targets; haptic feedback on tap                    |
| `ScoreHeader`     | Displays score, multiplier badge, and countdown timer                          |
| `MultiplierFlame` | Visual glow/flame effect that intensifies at ×2 and ×3 streaks                 |
| `SessionSummary`  | Shown on `GameOver`: score, accuracy %, best streak                            |

### SymbolCard animation

```
New symbol enters  → fade in or slide up (~150 ms)
Correct flash      → brief green overlay (~200 ms)
Wrong flash        → brief red overlay + subtle shake (~200 ms)
```

Use `AnimatedContent` keyed on the symbol index for the enter/exit transition.

---

## ViewModel Responsibilities

- Generates a random `Symbol` sequence where roughly 50 % of consecutive pairs match (to keep YES/NO balanced).
- Orchestrates the 3-second countdown before the game starts.
- Tracks the 60-second game timer using a `tickerFlow` inside `viewModelScope`.
- Enforces max display time per card; auto-submits a wrong answer on timeout.
- Updates score, multiplier, and streak on each answer.
- Advances difficulty phase based on total correct count.
- Exposes `uiState: StateFlow<SpeedMatchUiState>` and a single `onAnswer(matched: Boolean)` event handler.

---

## Dependencies

Only allowed dependencies (per game module contract):

- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` enum if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/speed_match/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/speed_match/
    ├── SpeedMatchScreen.kt       ← Composable entry point
    ├── SpeedMatchViewModel.kt    ← StateFlow + timer + scoring
    ├── SpeedMatchUiState.kt      ← sealed interface + Symbol model
    └── Symbol.kt                 ← Shape + SymbolColor enums
```

---

## Testing Notes

- Unit-test symbol sequence generation: verify ~50 % match rate over a large sample.
- Unit-test scoring logic: correct streaks produce correct multipliers and point totals.
- Unit-test timeout behaviour: no answer within max display time → wrong answer registered.
- Use `TestCoroutineRule` from `core:testing` to control timer `delay` calls.
- No mocking libraries — use fake implementations only.