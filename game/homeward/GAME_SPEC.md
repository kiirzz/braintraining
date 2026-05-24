# Homeward — Game Specification

**Lumosity equivalent:** Original concept (path-planning puzzle)
**Skill area:** `SkillArea.PROBLEM_SOLVING`
**Module:** `game:homeward`

---

## Core Mechanic

A grid board holds N colored agents and N matching colored houses. The player moves agents one cell at a time (up / down / left / right) to guide each agent to the house with the same color. Every move costs 1 step from a fixed budget. Solve the board within the step budget to complete the level.

---

## Gameplay Loop

1. Level loads: grid shows agents (colored circles), houses (colored borders), and optional obstacle cells.
2. Player taps an agent to **select** it; valid adjacent cells highlight.
3. Player taps a highlighted cell to **move** the agent (−1 step from budget).
4. When an agent lands on its matching house it **locks in** (solved, can no longer be moved).
5. Once **all agents are home** → Level Complete.
6. If the step counter reaches 0 before all agents are home → Game Over.
7. On Level Complete the player chooses **Next Level** (harder) or **End Session**.

---

## Board Layout

| Cell type | Appearance | Rule |
|---|---|---|
| Normal | neutral surface | agents may enter freely |
| Obstacle | dark gray | impassable |
| House | colored border square | destination for the matching agent |
| Agent (unsolved) | colored filled circle | selectable and movable |
| Agent (solved) | filled house with ✓ | locked, impassable to other agents |

Multiple unsolved agents may occupy the same cell.
Solved agents block the house cell so other agents cannot enter it.

---

## Difficulty Table

| Level range | Grid | Agents | Obstacles | Buffer steps |
|---|---|---|---|---|
| 1–3 | 5×5 | 2 | 0 | +4 |
| 4–6 | 5×5 | 3 | 2 | +3 |
| 7–9 | 6×6 | 3 | 4 | +2 |
| 10–12 | 6×6 | 4 | 6 | +1 |
| 13+ | 7×7 | 5 | 8 | 0 |

---

## Minimum Steps Algorithm (BFS per agent)

Because agents can share cells and only obstacle cells block movement, each agent's shortest path is **independent**.

```
for each agent i:
    distance_i = BFS( agentStart_i → house_i, avoiding obstacles )

minSteps    = Σ distance_i
stepBudget  = minSteps + buffer(difficulty)
```

**BFS** per agent runs in O(R × C). Total generation cost is O(N × R × C).

A level is **invalid** (regenerated with the same seed) if any BFS returns null (agent unreachable). At difficulty 13+ the buffer is 0, so the player must find the globally optimal path for every agent.

### BFS pseudocode

```
bfs(from, to, rows, cols, obstacles):
    if from == to: return 0
    queue = [(from, 0)]
    visited = {from}
    dirs = [(-1,0),(1,0),(0,-1),(0,1)]
    while queue not empty:
        (cell, dist) = dequeue
        for d in dirs:
            next = cell + d
            if out-of-bounds or obstacle or visited: skip
            if next == to: return dist + 1
            visited.add(next); enqueue (next, dist+1)
    return null  // unreachable — level invalid
```

---

## Scoring

```
levelScore  = (stepBudget − stepsUsed) × 10 + level × 50
totalScore += levelScore   (accumulated across levels in one session)
```

Game over score = totalScore at the point of failure (no penalty added).

---

## Module Contract

```kotlin
@Composable
fun HomewardScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
)
```

`feature:training` hosts this screen and calls `RecordGameResultUseCase` when `onGameComplete` fires.

---

## UI State

```kotlin
sealed interface HomewardUiState {

    data class Playing(
        val board: BoardState,
        val level: Int,
        val stepBudget: Int,
        val stepsUsed: Int,
        val selectedAgentId: Int?,
        val validMoveCells: Set<Cell>,
        val totalScore: Int,
    ) : HomewardUiState

    data class LevelComplete(
        val level: Int,
        val stepsUsed: Int,
        val stepBudget: Int,
        val minSteps: Int,
        val levelScore: Int,
        val totalScore: Int,
        val durationMs: Long,
    ) : HomewardUiState

    data class GameOver(
        val levelsCompleted: Int,
        val totalScore: Int,
        val durationMs: Long,
    ) : HomewardUiState
}

data class Cell(val row: Int, val col: Int)
enum class AgentColor { RED, BLUE, GREEN, YELLOW, PURPLE }

data class AgentState(val id: Int, val color: AgentColor, val position: Cell, val isHome: Boolean)
data class HouseState(val agentId: Int, val color: AgentColor, val position: Cell)
data class BoardState(val rows: Int, val cols: Int, val agents: List<AgentState>,
                      val houses: List<HouseState>, val obstacles: Set<Cell>)
data class LevelData(val boardState: BoardState, val stepBudget: Int, val minSteps: Int, val level: Int)
```

---

## Key Composables

| Composable | Responsibility |
|---|---|
| `GameBoard` | Renders R×C grid, sized to fill available width via `BoxWithConstraints` |
| `GridCell` | Single cell: background color, house border, agent circle, obstacle fill |
| `StepCounter` | Progress bar + "N left" label; color shifts green→yellow→red |
| `LevelHeader` | Level number, Reset button, Exit button |
| `LevelCompleteContent` | Score breakdown, Next Level / End Session buttons |
| `GameOverContent` | Final score, levels completed, Continue button |
| `AgentLegend` | Row of colored dots showing unsolved agents remaining |

### GridCell visual states

```
Normal cell      → surface background
Obstacle         → dark gray, not clickable
Valid move       → tertiaryContainer highlight
Selected agent cell → primaryContainer background

Agent (unsolved) → colored circle (65 % of cell), white ring when selected
Agent (solved)   → colored rounded rect filling cell, white ✓ in centre
House (empty)    → 2 dp colored border, rounded corners
House (solved)   → replaced by filled agent rendering above
```

---

## ViewModel Responsibilities

- Calls `LevelGenerator.generate(level)` (seeded by level number for determinism).
- Tracks `selectedAgentId` and recomputes `validMoveCells` on each selection change.
- On `onCellTapped`:
  - If valid-move cell → `performMove` (update position, check win/lose)
  - If unsolved agent cell → select / deselect
  - Otherwise → deselect
- On `onResetLevel` → reload current level (restores original positions and step budget).
- On `onNextLevel` (from LevelComplete) → load next level, accumulate score.
- Exposes `uiState: StateFlow<HomewardUiState>`.

---

## Level Generation

Levels are **seeded** (`seed = level × 31337L`) so the same level number always produces the same puzzle. Up to 200 generation attempts are made per level; a deterministic fallback (agents at row 0, houses at last row) is used if all attempts fail.

---

## Dependencies

Only allowed (per game module contract):

- `core:systemdesign` — theme, colors, typography
- `core:model` — `SkillArea` enum if needed

No `feature:*` or `core:data` imports.

---

## File Structure

```
game/homeward/
├── build.gradle.kts
├── src/main/AndroidManifest.xml
└── src/main/java/com/braintraining/game/homeward/
    ├── HomewardScreen.kt       ← Composable entry point + all UI composables
    ├── HomewardViewModel.kt    ← game logic, selection, move, win/lose
    ├── HomewardUiState.kt      ← sealed interface
    ├── LevelData.kt            ← Cell, AgentColor, AgentState, HouseState, BoardState, LevelData
    └── LevelGenerator.kt       ← BFS algorithm + seeded procedural level generation
```

---

## Testing Notes

- Unit-test `LevelGenerator.bfs()`: 3×3 grid with one obstacle, verify correct distance.
- Unit-test `LevelGenerator.bfs()`: disconnected grid (agent blocked), verify null returned.
- Unit-test `LevelGenerator.generate()`: for levels 1–13, assert all agents reachable and `minSteps ≤ stepBudget`.
- Unit-test `HomewardViewModel`: tap agent → selected; tap valid cell → agent moves, stepsUsed++; all home → LevelComplete state; steps exhausted → GameOver state.
- Unit-test `HomewardViewModel.onResetLevel`: board and stepBudget restored to initial values.
- No mocking libraries — test ViewModel directly (no coroutine timer dependency).
