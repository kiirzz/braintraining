package com.braintraining.game.speed_match

internal sealed interface SpeedMatchUiState {

    data class Countdown(
        val secondsLeft: Int,
    ) : SpeedMatchUiState

    data class Playing(
        val currentSymbol: Symbol,
        val previousSymbol: Symbol?,
        val cardIndex: Int,
        val score: Int,
        val multiplier: Int,
        val streak: Int,
        val timeRemainingMs: Long,
        val lastAnswerCorrect: Boolean?,
        val showShapeColor: Boolean,
    ) : SpeedMatchUiState

    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val accuracy: Float,
        val durationMs: Long,
    ) : SpeedMatchUiState
}