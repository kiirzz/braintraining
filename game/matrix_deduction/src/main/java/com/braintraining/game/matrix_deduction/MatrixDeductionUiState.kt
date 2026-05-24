package com.braintraining.game.matrix_deduction

import androidx.compose.runtime.Immutable

enum class TileShape { CIRCLE, SQUARE, TRIANGLE }
enum class TileColor { BLUE, RED, GREEN }

@Immutable
data class Tile(
    val shape: TileShape,
    val color: TileColor,
    val count: Int,
)

@Immutable
data class MatrixPuzzle(
    val cells: List<Tile?>,
    val answer: Tile,
    val choices: List<Tile>,
    val answerIndex: Int,
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
        val lastCorrect: Boolean?,
        val revealAnswerIndex: Int?,
        val lastTappedIndex: Int?,
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
