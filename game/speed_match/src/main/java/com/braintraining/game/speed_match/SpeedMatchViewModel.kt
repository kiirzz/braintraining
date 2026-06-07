package com.braintraining.game.speed_match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random

private const val GAME_DURATION_MS = 60_000L
private const val TIMER_TICK_MS = 100L
private const val COUNTDOWN_FROM = 3
private const val FEEDBACK_DELAY_MS = 300L

internal enum class DifficultyPhase { EASY, MEDIUM, HARD }

internal class SpeedMatchViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<SpeedMatchUiState>(SpeedMatchUiState.Countdown(COUNTDOWN_FROM))
    val uiState: StateFlow<SpeedMatchUiState> = _uiState.asStateFlow()

    private var score = 0
    private var streak = 0
    private var bestStreak = 0
    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var cardIndex = 0
    private var timeRemainingMs = GAME_DURATION_MS
    private var cardTimeoutJob: Job? = null

    init {
        viewModelScope.launch {
            for (s in COUNTDOWN_FROM downTo 1) {
                _uiState.value = SpeedMatchUiState.Countdown(s)
                delay(1000L)
            }
            launchGameTimer()
            showNextCard(previousSymbol = null)
        }
    }

    private fun launchGameTimer() {
        viewModelScope.launch {
            while (timeRemainingMs > 0L) {
                delay(TIMER_TICK_MS)
                timeRemainingMs = (timeRemainingMs - TIMER_TICK_MS).coerceAtLeast(0L)
                val state = _uiState.value as? SpeedMatchUiState.Playing
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

    private fun showNextCard(previousSymbol: Symbol?) {
        if (_uiState.value is SpeedMatchUiState.GameOver) return
        cardTimeoutJob?.cancel()

        val phase = phase()
        val symbol = nextSymbol(previousSymbol, phase)
        val index = cardIndex++

        _uiState.value = SpeedMatchUiState.Playing(
            currentSymbol = symbol,
            previousSymbol = previousSymbol,
            cardIndex = index,
            score = score,
            multiplier = multiplier(),
            streak = streak,
            timeRemainingMs = timeRemainingMs,
            lastAnswerCorrect = null,
            showShapeColor = phase != DifficultyPhase.EASY,
        )

        cardTimeoutJob = viewModelScope.launch {
            delay(displayTimeMs(phase))
            if (previousSymbol == null) {
                val cur = (_uiState.value as? SpeedMatchUiState.Playing)?.currentSymbol ?: return@launch
                showNextCard(previousSymbol = cur)
            } else {
                processAnswer(matched = false)
            }
        }
    }

    fun onAnswer(matched: Boolean) {
        val state = _uiState.value as? SpeedMatchUiState.Playing ?: return
        if (state.previousSymbol == null || state.lastAnswerCorrect != null) return
        cardTimeoutJob?.cancel()
        processAnswer(matched)
    }

    private fun processAnswer(matched: Boolean) {
        val state = _uiState.value as? SpeedMatchUiState.Playing ?: return
        val previous = state.previousSymbol ?: return

        val currentMult = multiplier()
        val isCorrect = matched == doesMatch(state.currentSymbol, previous, phase())

        if (isCorrect) {
            streak++
            bestStreak = max(bestStreak, streak)
            correctAnswers++
            score += currentMult
        } else {
            streak = 0
            wrongAnswers++
        }

        _uiState.value = state.copy(
            score = score,
            multiplier = multiplier(),
            streak = streak,
            lastAnswerCorrect = isCorrect,
        )

        viewModelScope.launch {
            delay(FEEDBACK_DELAY_MS)
            val cur = (_uiState.value as? SpeedMatchUiState.Playing)?.currentSymbol ?: return@launch
            showNextCard(previousSymbol = cur)
        }
    }

    private fun endGame() {
        cardTimeoutJob?.cancel()
        val total = correctAnswers + wrongAnswers
        _uiState.value = SpeedMatchUiState.GameOver(
            finalScore = score,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            bestStreak = bestStreak,
            accuracy = if (total > 0) correctAnswers.toFloat() / total else 0f,
            durationMs = GAME_DURATION_MS - timeRemainingMs,
        )
    }

    private fun phase(): DifficultyPhase = when {
        correctAnswers < 11 -> DifficultyPhase.EASY
        correctAnswers < 26 -> DifficultyPhase.MEDIUM
        else -> DifficultyPhase.HARD
    }

    private fun multiplier(): Int = when {
        streak >= 6 -> 3
        streak >= 3 -> 2
        else -> 1
    }

    private fun displayTimeMs(phase: DifficultyPhase): Long = when (phase) {
        DifficultyPhase.EASY -> 1500L
        DifficultyPhase.MEDIUM -> 1000L
        DifficultyPhase.HARD -> 700L
    }

    private fun nextSymbol(previous: Symbol?, phase: DifficultyPhase): Symbol {
        if (previous == null) return Symbol(Shape.entries.random(), SymbolColor.entries.random())
        return if (Random.nextBoolean()) matchingSymbol(previous, phase) else differentSymbol(previous, phase)
    }

    private fun matchingSymbol(previous: Symbol, phase: DifficultyPhase): Symbol = when (phase) {
        DifficultyPhase.EASY, DifficultyPhase.MEDIUM ->
            Symbol(previous.shape, SymbolColor.entries.filter { it != previous.color }.random())
        DifficultyPhase.HARD ->
            Symbol(previous.shape, previous.color)
    }

    private fun differentSymbol(previous: Symbol, phase: DifficultyPhase): Symbol = when (phase) {
        DifficultyPhase.EASY, DifficultyPhase.MEDIUM -> {
            val shape = Shape.entries.filter { it != previous.shape }.random()
            Symbol(shape, SymbolColor.entries.random())
        }
        DifficultyPhase.HARD -> {
            if (Random.nextBoolean()) {
                Symbol(Shape.entries.filter { it != previous.shape }.random(), previous.color)
            } else {
                Symbol(previous.shape, SymbolColor.entries.filter { it != previous.color }.random())
            }
        }
    }

    private fun doesMatch(current: Symbol, previous: Symbol, phase: DifficultyPhase): Boolean = when (phase) {
        DifficultyPhase.EASY, DifficultyPhase.MEDIUM -> current.shape == previous.shape
        DifficultyPhase.HARD -> current.shape == previous.shape && current.color == previous.color
    }
}