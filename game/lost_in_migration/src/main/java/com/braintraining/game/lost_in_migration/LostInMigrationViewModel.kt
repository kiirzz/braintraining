package com.braintraining.game.lost_in_migration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max

private const val GAME_DURATION_MS = 60_000L
private const val TIMER_TICK_MS = 100L
private const val COUNTDOWN_FROM = 3
private const val FEEDBACK_DELAY_MS = 300L
private const val FAST_THRESHOLD_MS = 400L

internal class LostInMigrationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<LostInMigrationUiState>(
        LostInMigrationUiState.Countdown(COUNTDOWN_FROM)
    )
    val uiState: StateFlow<LostInMigrationUiState> = _uiState.asStateFlow()

    private var score = 0
    private var streak = 0
    private var bestStreak = 0
    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var flockIndex = 0
    private var timeRemainingMs = GAME_DURATION_MS
    private var cardTimeoutJob: Job? = null
    private var flockShownAtMs = 0L

    private var congruentCorrect = 0; private var congruentTotal = 0
    private var incongruentCorrect = 0; private var incongruentTotal = 0
    private var mixedCorrect = 0; private var mixedTotal = 0

    init {
        viewModelScope.launch {
            for (s in COUNTDOWN_FROM downTo 1) {
                _uiState.value = LostInMigrationUiState.Countdown(s)
                delay(1000L)
            }
            launchGameTimer()
            showNextFlock()
        }
    }

    private fun launchGameTimer() {
        viewModelScope.launch {
            while (timeRemainingMs > 0L) {
                delay(TIMER_TICK_MS)
                timeRemainingMs = (timeRemainingMs - TIMER_TICK_MS).coerceAtLeast(0L)
                val state = _uiState.value as? LostInMigrationUiState.Playing
                if (state != null) {
                    _uiState.value = state.copy(timeRemainingMs = timeRemainingMs)
                }
                if (timeRemainingMs == 0L) {
                    endGame()
                    return@launch
                }
            }
        }
    }

    private fun showNextFlock() {
        if (_uiState.value is LostInMigrationUiState.GameOver) return
        cardTimeoutJob?.cancel()

        val phase = currentPhase()
        val flock = generateFlock(phase)
        val index = flockIndex++
        flockShownAtMs = System.currentTimeMillis()

        _uiState.value = LostInMigrationUiState.Playing(
            flock = flock,
            flockIndex = index,
            score = score,
            streak = streak,
            multiplier = multiplier(),
            timeRemainingMs = timeRemainingMs,
            lastAnswerCorrect = null,
        )

        cardTimeoutJob = viewModelScope.launch {
            delay(displayTimeMs(phase))
            processAnswer(direction = BirdDirection.UP, isTimeout = true)
        }
    }

    fun onAnswer(direction: BirdDirection) {
        val state = _uiState.value as? LostInMigrationUiState.Playing ?: return
        if (state.lastAnswerCorrect != null) return
        cardTimeoutJob?.cancel()
        processAnswer(direction, isTimeout = false)
    }

    private fun processAnswer(direction: BirdDirection, isTimeout: Boolean) {
        val state = _uiState.value as? LostInMigrationUiState.Playing ?: return
        val isCorrect = !isTimeout && direction == state.flock.centerDirection
        val isFast = isCorrect && (System.currentTimeMillis() - flockShownAtMs) < FAST_THRESHOLD_MS

        when (state.flock.type) {
            FlockType.CONGRUENT -> { congruentTotal++; if (isCorrect) congruentCorrect++ }
            FlockType.INCONGRUENT -> { incongruentTotal++; if (isCorrect) incongruentCorrect++ }
            FlockType.MIXED -> { mixedTotal++; if (isCorrect) mixedCorrect++ }
        }

        val currentMult = multiplier()
        if (isCorrect) {
            streak++
            bestStreak = max(bestStreak, streak)
            correctAnswers++
            score += (if (isFast) 2 else 1) * currentMult
        } else {
            streak = 0
            wrongAnswers++
        }

        _uiState.value = state.copy(
            score = score,
            streak = streak,
            multiplier = multiplier(),
            lastAnswerCorrect = isCorrect,
        )

        viewModelScope.launch {
            delay(FEEDBACK_DELAY_MS)
            showNextFlock()
        }
    }

    private fun endGame() {
        cardTimeoutJob?.cancel()
        _uiState.value = LostInMigrationUiState.GameOver(
            finalScore = score,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            bestStreak = bestStreak,
            congruentCorrect = congruentCorrect,
            congruentTotal = congruentTotal,
            incongruentCorrect = incongruentCorrect,
            incongruentTotal = incongruentTotal,
            mixedCorrect = mixedCorrect,
            mixedTotal = mixedTotal,
            durationMs = GAME_DURATION_MS - timeRemainingMs,
        )
    }

    private fun currentPhase(): FlockType = when {
        correctAnswers < 11 -> FlockType.CONGRUENT
        correctAnswers < 26 -> FlockType.INCONGRUENT
        else -> FlockType.MIXED
    }

    private fun multiplier(): Int = when {
        streak >= 10 -> 3
        streak >= 5 -> 2
        else -> 1
    }

    private fun displayTimeMs(phase: FlockType): Long = when (phase) {
        FlockType.CONGRUENT -> 2500L
        FlockType.INCONGRUENT -> 1500L
        FlockType.MIXED -> 800L
    }

    private fun generateFlock(phase: FlockType): Flock {
        val center = BirdDirection.entries.random()
        val flankers = when (phase) {
            FlockType.CONGRUENT -> List(4) { center }
            FlockType.INCONGRUENT -> List(4) { center.opposite() }
            FlockType.MIXED -> List(4) { BirdDirection.entries.random() }
        }
        return Flock(center, flankers, phase)
    }
}
