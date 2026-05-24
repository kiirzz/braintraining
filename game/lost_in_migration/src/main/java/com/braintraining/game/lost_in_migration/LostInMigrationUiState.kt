package com.braintraining.game.lost_in_migration

internal enum class BirdDirection {
    UP, DOWN, LEFT, RIGHT;

    fun opposite(): BirdDirection = when (this) {
        UP -> DOWN
        DOWN -> UP
        LEFT -> RIGHT
        RIGHT -> LEFT
    }

    fun toArrow(): String = when (this) {
        UP -> "↑"
        DOWN -> "↓"
        LEFT -> "←"
        RIGHT -> "→"
    }
}

internal enum class FlockType { CONGRUENT, INCONGRUENT, MIXED }

internal data class Flock(
    val centerDirection: BirdDirection,
    val flankerDirections: List<BirdDirection>,  // exactly 4: [left2, left1, right1, right2]
    val type: FlockType,
)

internal sealed interface LostInMigrationUiState {

    data class Countdown(
        val secondsLeft: Int,
    ) : LostInMigrationUiState

    data class Playing(
        val flock: Flock,
        val flockIndex: Int,
        val score: Int,
        val streak: Int,
        val multiplier: Int,
        val timeRemainingMs: Long,
        val lastAnswerCorrect: Boolean?,
    ) : LostInMigrationUiState

    data class GameOver(
        val finalScore: Int,
        val correctAnswers: Int,
        val wrongAnswers: Int,
        val bestStreak: Int,
        val congruentCorrect: Int,
        val congruentTotal: Int,
        val incongruentCorrect: Int,
        val incongruentTotal: Int,
        val mixedCorrect: Int,
        val mixedTotal: Int,
        val durationMs: Long,
    ) : LostInMigrationUiState
}
