package com.braintraining.game.homeward

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val DIRS = listOf(Cell(-1, 0), Cell(1, 0), Cell(0, -1), Cell(0, 1))

internal class HomewardViewModel : ViewModel() {

    private var currentLevelData: LevelData = LevelGenerator.generate(1)
    private var totalScore = 0
    private var levelsCompleted = 0
    private val gameStartTimeMs = System.currentTimeMillis()

    private val _uiState = MutableStateFlow<HomewardUiState>(
        HomewardUiState.Playing(
            board = currentLevelData.boardState,
            level = 1,
            stepBudget = currentLevelData.stepBudget,
            stepsUsed = 0,
            selectedAgentId = null,
            validMoveCells = emptySet(),
            totalScore = 0,
        )
    )
    val uiState: StateFlow<HomewardUiState> = _uiState.asStateFlow()

    fun onCellTapped(cell: Cell) {
        val state = _uiState.value as? HomewardUiState.Playing ?: return

        if (state.selectedAgentId != null && cell in state.validMoveCells) {
            performMove(state, state.selectedAgentId, cell)
            return
        }

        val agentAtCell = state.board.agents.find { !it.isHome && it.position == cell }
        if (agentAtCell != null) {
            val newId = if (state.selectedAgentId == agentAtCell.id) null else agentAtCell.id
            _uiState.value = state.copy(
                selectedAgentId = newId,
                validMoveCells = if (newId != null) validMoves(cell, state.board) else emptySet(),
            )
            return
        }

        _uiState.value = state.copy(selectedAgentId = null, validMoveCells = emptySet())
    }

    fun onResetLevel() {
        val state = _uiState.value as? HomewardUiState.Playing ?: return
        loadLevel(state.level)
    }

    fun onNextLevel() {
        val state = _uiState.value as? HomewardUiState.LevelComplete ?: return
        loadLevel(state.level + 1)
    }

    private fun loadLevel(level: Int) {
        currentLevelData = LevelGenerator.generate(level)
        _uiState.value = HomewardUiState.Playing(
            board = currentLevelData.boardState,
            level = level,
            stepBudget = currentLevelData.stepBudget,
            stepsUsed = 0,
            selectedAgentId = null,
            validMoveCells = emptySet(),
            totalScore = totalScore,
        )
    }

    private fun performMove(state: HomewardUiState.Playing, agentId: Int, target: Cell) {
        val house = state.board.houses.find { it.agentId == agentId } ?: return
        val isHome = target == house.position

        val updatedAgents = state.board.agents.map { agent ->
            if (agent.id == agentId) agent.copy(position = target, isHome = isHome) else agent
        }
        val updatedBoard = state.board.copy(agents = updatedAgents)
        val newStepsUsed = state.stepsUsed + 1
        val elapsed = System.currentTimeMillis() - gameStartTimeMs

        if (updatedAgents.all { it.isHome }) {
            val levelScore = (state.stepBudget - newStepsUsed) * 10 + state.level * 50
            levelsCompleted++
            totalScore += levelScore
            _uiState.value = HomewardUiState.LevelComplete(
                level = state.level,
                stepsUsed = newStepsUsed,
                stepBudget = state.stepBudget,
                minSteps = currentLevelData.minSteps,
                levelScore = levelScore,
                totalScore = totalScore,
                durationMs = elapsed,
            )
            return
        }

        if (newStepsUsed >= state.stepBudget) {
            _uiState.value = HomewardUiState.GameOver(
                levelsCompleted = levelsCompleted,
                totalScore = totalScore,
                durationMs = elapsed,
            )
            return
        }

        val nextSelectedId = if (isHome) null else agentId
        _uiState.value = state.copy(
            board = updatedBoard,
            stepsUsed = newStepsUsed,
            selectedAgentId = nextSelectedId,
            validMoveCells = if (nextSelectedId != null) validMoves(target, updatedBoard) else emptySet(),
        )
    }

    private fun validMoves(from: Cell, board: BoardState): Set<Cell> {
        val solvedCells = board.agents.filter { it.isHome }.map { it.position }.toSet()
        return DIRS.mapNotNull { d ->
            val next = Cell(from.row + d.row, from.col + d.col)
            next.takeIf {
                it.row in 0 until board.rows &&
                it.col in 0 until board.cols &&
                it !in board.obstacles &&
                it !in solvedCells
            }
        }.toSet()
    }
}
