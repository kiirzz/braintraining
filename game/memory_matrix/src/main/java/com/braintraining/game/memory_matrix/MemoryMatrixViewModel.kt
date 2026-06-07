package com.braintraining.game.memory_matrix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val RESULT_DISPLAY_MS = 1200L

internal data class LevelConfig(
    val gridSize: Int,
    val tilesToRemember: Int,
    val showDurationMs: Long,
)

private fun levelConfig(level: Int): LevelConfig = when (level) {
    1 -> LevelConfig(gridSize = 3, tilesToRemember = 3, showDurationMs = 2000L)
    2 -> LevelConfig(gridSize = 3, tilesToRemember = 4, showDurationMs = 1800L)
    3 -> LevelConfig(gridSize = 4, tilesToRemember = 5, showDurationMs = 1600L)
    4 -> LevelConfig(gridSize = 4, tilesToRemember = 6, showDurationMs = 1400L)
    else -> LevelConfig(gridSize = 5, tilesToRemember = (7 + (level - 5)).coerceAtMost(20), showDurationMs = 1200L)
}

internal class MemoryMatrixViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MemoryMatrixUiState>(MemoryMatrixUiState.Loading)
    val uiState: StateFlow<MemoryMatrixUiState> = _uiState.asStateFlow()

    private var level = 1
    private var totalScore = 0
    private var startTimeMs = 0L
    private var currentPattern: Set<Pair<Int, Int>> = emptySet()

    init {
        startTimeMs = System.currentTimeMillis()
        startRound()
    }

    private fun startRound() {
        val config = levelConfig(level)
        currentPattern = generatePattern(config.gridSize, config.tilesToRemember)
        _uiState.value = MemoryMatrixUiState.ShowPattern(
            grid = buildGrid(config.gridSize, currentPattern),
            level = level,
            score = totalScore,
            showDurationMs = config.showDurationMs,
        )
        viewModelScope.launch {
            delay(config.showDurationMs)
            if (_uiState.value is MemoryMatrixUiState.ShowPattern) {
                _uiState.value = MemoryMatrixUiState.RecallPhase(
                    grid = buildGrid(config.gridSize, emptySet()),
                    selected = emptySet(),
                    level = level,
                    score = totalScore,
                    tilesToSelect = config.tilesToRemember,
                )
            }
        }
    }

    fun onTileTapped(row: Int, col: Int) {
        val state = _uiState.value as? MemoryMatrixUiState.RecallPhase ?: return
        val pos = row to col
        val newSelected = if (pos in state.selected) state.selected - pos else state.selected + pos
        _uiState.value = state.copy(selected = newSelected)

        if (newSelected.size == state.tilesToSelect) {
            evaluateRound(newSelected)
        }
    }

    private fun evaluateRound(selected: Set<Pair<Int, Int>>) {
        val config = levelConfig(level)
        val correct = selected == currentPattern
        if (correct) {
            val levelMultiplier = 1.0f + (level - 1) * 0.2f
            totalScore += (config.tilesToRemember * config.gridSize * levelMultiplier).toInt()
        }

        _uiState.value = MemoryMatrixUiState.RoundResult(
            grid = buildGrid(config.gridSize, currentPattern),
            selected = selected,
            correct = correct,
            score = totalScore,
            level = level,
        )

        viewModelScope.launch {
            delay(RESULT_DISPLAY_MS)
            if (correct) {
                level++
                startRound()
            } else {
                _uiState.value = MemoryMatrixUiState.GameOver(
                    finalScore = totalScore,
                    level = level,
                    durationMs = System.currentTimeMillis() - startTimeMs,
                )
            }
        }
    }

    private fun generatePattern(gridSize: Int, count: Int): Set<Pair<Int, Int>> {
        val all = (0 until gridSize).flatMap { r -> (0 until gridSize).map { c -> r to c } }
        return all.shuffled().take(count).toSet()
    }

    private fun buildGrid(gridSize: Int, highlighted: Set<Pair<Int, Int>>): List<List<Boolean>> =
        (0 until gridSize).map { r -> (0 until gridSize).map { c -> (r to c) in highlighted } }
}
