package com.braintraining.game.eagle_eye

import androidx.compose.runtime.Immutable

enum class SymbolDirection { RIGHT, LEFT, UP, DOWN }
enum class SymbolColor { BLUE, RED, GREEN, YELLOW }
enum class SymbolShape { CIRCLE, SQUARE, TRIANGLE, DIAMOND }
enum class GridPhase { EASY, MEDIUM, HARD }

@Immutable
data class GridSymbol(
    val direction: SymbolDirection,
    val color: SymbolColor,
    val shape: SymbolShape,
    val rotationDegrees: Float,
)

@Immutable
data class GridState(
    val size: Int,
    val cells: List<GridSymbol>,
    val oddIndex: Int,
)

sealed interface EagleEyeUiState {

    data class Countdown(
        val secondsLeft: Int,
    ) : EagleEyeUiState

    @Immutable
    data class Playing(
        val grid: GridState,
        val gridIndex: Int,
        val gridShownAtMs: Long,
        val score: Int,
        val streak: Int,
        val multiplier: Int,
        val timeRemainingMs: Long,
        val lastCorrect: Boolean?,
        val revealOdd: Boolean,
    ) : EagleEyeUiState

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
    ) : EagleEyeUiState
}
