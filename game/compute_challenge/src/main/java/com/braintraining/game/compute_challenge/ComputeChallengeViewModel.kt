package com.braintraining.game.compute_challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

private const val GAME_DURATION_MS = 60_000L
private const val TICK_MS = 50L
private const val DROP_SPEED_BASE = 0.0012f
internal const val INITIAL_LIVES = 3
private const val MAX_SIMULTANEOUS_DROPS = 4

internal enum class Operator(val symbol: String) {
    PLUS("+"), MINUS("−"), MULTIPLY("×");

    fun calculate(a: Int, b: Int): Int = when (this) {
        PLUS -> a + b
        MINUS -> a - b
        MULTIPLY -> a * b
    }
}

internal data class Drop(
    val id: Int,
    val equation: String,
    val isAnswerCorrect: Boolean,
    val yFraction: Float,
    val xFraction: Float,
)

internal sealed interface ComputeChallengeUiState {
    data class Playing(
        val drops: List<Drop>,
        val score: Int,
        val lives: Int,
        val timeRemainingMs: Long,
        val level: Int,
    ) : ComputeChallengeUiState

    data class GameOver(
        val score: Int,
        val durationMs: Long,
    ) : ComputeChallengeUiState
}

internal class ComputeChallengeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ComputeChallengeUiState>(
        ComputeChallengeUiState.Playing(
            drops = emptyList(),
            score = 0,
            lives = INITIAL_LIVES,
            timeRemainingMs = GAME_DURATION_MS,
            level = 1,
        )
    )
    val uiState: StateFlow<ComputeChallengeUiState> = _uiState.asStateFlow()

    private var nextDropId = 0
    private var timeSinceLastSpawnMs = 0L

    init {
        viewModelScope.launch { gameLoop() }
    }

    private suspend fun gameLoop() {
        while (true) {
            delay(TICK_MS)
            val state = _uiState.value as? ComputeChallengeUiState.Playing ?: return

            val newTimeRemaining = (state.timeRemainingMs - TICK_MS).coerceAtLeast(0L)
            val elapsed = GAME_DURATION_MS - newTimeRemaining
            val level = ((elapsed / 15_000L) + 1L).toInt().coerceIn(1, 5)
            val speed = DROP_SPEED_BASE * level

            timeSinceLastSpawnMs += TICK_MS
            var drops = state.drops.map { it.copy(yFraction = it.yFraction + speed) }

            val livesLost = drops.count { it.yFraction >= 1f && it.isAnswerCorrect }
            drops = drops.filter { it.yFraction < 1f }
            val newLives = state.lives - livesLost

            if (newLives <= 0 || newTimeRemaining == 0L) {
                _uiState.value = ComputeChallengeUiState.GameOver(
                    score = state.score,
                    durationMs = elapsed,
                )
                return
            }

            val spawnInterval = spawnIntervalMs(level)
            if (timeSinceLastSpawnMs >= spawnInterval && drops.size < MAX_SIMULTANEOUS_DROPS) {
                drops = drops + buildDrop(nextDropId++, level)
                timeSinceLastSpawnMs = 0L
            }

            _uiState.value = state.copy(
                drops = drops,
                lives = newLives,
                timeRemainingMs = newTimeRemaining,
                level = level,
            )
        }
    }

    fun onDropTapped(dropId: Int) {
        val state = _uiState.value as? ComputeChallengeUiState.Playing ?: return
        val drop = state.drops.find { it.id == dropId } ?: return
        val remaining = state.drops.filter { it.id != dropId }

        if (drop.isAnswerCorrect) {
            _uiState.value = state.copy(
                drops = remaining,
                score = state.score + 10 * state.level,
            )
        } else {
            val newLives = state.lives - 1
            if (newLives <= 0) {
                val elapsed = GAME_DURATION_MS - state.timeRemainingMs
                _uiState.value = ComputeChallengeUiState.GameOver(
                    score = state.score,
                    durationMs = elapsed,
                )
            } else {
                _uiState.value = state.copy(drops = remaining, lives = newLives)
            }
        }
    }

    private fun spawnIntervalMs(level: Int): Long = when (level) {
        1 -> 2500L
        2 -> 2000L
        3 -> 1500L
        4 -> 1200L
        else -> 1000L
    }

    private fun buildDrop(id: Int, level: Int): Drop {
        val (left, op, right) = equation(level)
        val correct = op.calculate(left, right)
        val showCorrect = Random.nextBoolean()
        val displayed = if (showCorrect) correct else wrongAnswer(correct)
        return Drop(
            id = id,
            equation = "$left ${op.symbol} $right = $displayed",
            isAnswerCorrect = showCorrect,
            yFraction = 0f,
            xFraction = 0.05f + Random.nextFloat() * 0.9f,
        )
    }

    private fun equation(level: Int): Triple<Int, Operator, Int> {
        val op = when (level) {
            1 -> if (Random.nextBoolean()) Operator.PLUS else Operator.MINUS
            else -> Operator.entries.random()
        }
        val (a, b) = when (level) {
            1 -> (1..10).random() to (1..10).random()
            2 -> (5..15).random() to (2..5).random()
            else -> (10..25).random() to (1..12).random()
        }
        return if (op == Operator.MINUS) Triple(maxOf(a, b), op, minOf(a, b))
        else Triple(a, op, b)
    }

    private fun wrongAnswer(correct: Int): Int =
        correct + listOf(-3, -2, -1, 1, 2, 3).random()
}
