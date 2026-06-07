package com.braintraining.game.homeward

internal sealed interface HomewardUiState {

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
