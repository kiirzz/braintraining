package com.braintraining.game.homeward

internal data class Cell(val row: Int, val col: Int)

internal enum class AgentColor { RED, BLUE, GREEN, YELLOW, PURPLE }

internal data class AgentState(
    val id: Int,
    val color: AgentColor,
    val position: Cell,
    val isHome: Boolean = false,
)

internal data class HouseState(
    val agentId: Int,
    val color: AgentColor,
    val position: Cell,
)

internal data class BoardState(
    val rows: Int,
    val cols: Int,
    val agents: List<AgentState>,
    val houses: List<HouseState>,
    val obstacles: Set<Cell>,
)

internal data class LevelData(
    val boardState: BoardState,
    val stepBudget: Int,
    val minSteps: Int,
    val level: Int,
)
