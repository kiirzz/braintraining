package com.braintraining.game.memory_matrix

internal sealed interface MemoryMatrixUiState {

    data object Loading : MemoryMatrixUiState

    data class ShowPattern(
        val grid: List<List<Boolean>>,
        val level: Int,
        val score: Int,
        val showDurationMs: Long,
    ) : MemoryMatrixUiState

    data class RecallPhase(
        val grid: List<List<Boolean>>,
        val selected: Set<Pair<Int, Int>>,
        val level: Int,
        val score: Int,
        val tilesToSelect: Int,
    ) : MemoryMatrixUiState

    data class RoundResult(
        val grid: List<List<Boolean>>,
        val selected: Set<Pair<Int, Int>>,
        val correct: Boolean,
        val score: Int,
        val level: Int,
    ) : MemoryMatrixUiState

    data class GameOver(
        val finalScore: Int,
        val level: Int,
        val durationMs: Long,
    ) : MemoryMatrixUiState
}
