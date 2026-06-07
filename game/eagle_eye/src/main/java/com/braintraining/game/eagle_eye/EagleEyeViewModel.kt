package com.braintraining.game.eagle_eye

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val GAME_DURATION_MS = 60_000L
private const val COUNTDOWN_SECONDS = 3
private const val TICK_MS = 100L
private const val GRID_TIMEOUT_MS = 4_000L
private const val CORRECT_FEEDBACK_MS = 300L
private const val WRONG_FEEDBACK_MS = 400L

internal class EagleEyeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<EagleEyeUiState>(
        EagleEyeUiState.Countdown(COUNTDOWN_SECONDS)
    )
    val uiState: StateFlow<EagleEyeUiState> = _uiState.asStateFlow()

    private var timeoutJob: Job? = null
    private var previousOddIndex = -1

    private var correctAnswers = 0
    private var wrongAnswers = 0
    private var bestStreak = 0
    private var totalResponseMs = 0L
    private var easyCorrect = 0
    private var easyTotal = 0
    private var mediumCorrect = 0
    private var mediumTotal = 0
    private var hardCorrect = 0
    private var hardTotal = 0

    init {
        viewModelScope.launch { runCountdown() }
    }

    private suspend fun runCountdown() {
        for (s in COUNTDOWN_SECONDS downTo 1) {
            _uiState.value = EagleEyeUiState.Countdown(s)
            delay(1_000L)
        }
        startGame()
    }

    private fun startGame() {
        val grid = generateGrid(GridPhase.EASY)
        _uiState.value = EagleEyeUiState.Playing(
            grid = grid,
            gridIndex = 0,
            gridShownAtMs = System.currentTimeMillis(),
            score = 0,
            streak = 0,
            multiplier = 1,
            timeRemainingMs = GAME_DURATION_MS,
            lastCorrect = null,
            revealOdd = false,
        )
        viewModelScope.launch { runGameTimer() }
        scheduleTimeout()
    }

    private suspend fun runGameTimer() {
        while (true) {
            delay(TICK_MS)
            val state = _uiState.value as? EagleEyeUiState.Playing ?: return
            val newTime = (state.timeRemainingMs - TICK_MS).coerceAtLeast(0L)
            if (newTime == 0L) {
                timeoutJob?.cancel()
                endGame(state)
                return
            }
            _uiState.value = state.copy(timeRemainingMs = newTime)
        }
    }

    private fun scheduleTimeout() {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(GRID_TIMEOUT_MS)
            val state = _uiState.value as? EagleEyeUiState.Playing ?: return@launch
            if (state.lastCorrect != null) return@launch
            applyWrongResult(state)
        }
    }

    fun onCellTapped(index: Int) {
        val state = _uiState.value as? EagleEyeUiState.Playing ?: return
        if (state.lastCorrect != null) return
        timeoutJob?.cancel()
        if (index == state.grid.oddIndex) {
            val responseMs = System.currentTimeMillis() - state.gridShownAtMs
            applyCorrectResult(state, responseMs)
        } else {
            applyWrongResult(state)
        }
    }

    private fun applyCorrectResult(state: EagleEyeUiState.Playing, responseMs: Long) {
        val phase = phaseFromGridSize(state.grid.size)
        correctAnswers++
        when (phase) {
            GridPhase.EASY -> { easyCorrect++; easyTotal++ }
            GridPhase.MEDIUM -> { mediumCorrect++; mediumTotal++ }
            GridPhase.HARD -> { hardCorrect++; hardTotal++ }
        }
        totalResponseMs += responseMs
        val newStreak = state.streak + 1
        if (newStreak > bestStreak) bestStreak = newStreak
        val multiplier = multiplierFor(newStreak)
        val pts = pointsFor(responseMs) * multiplier
        _uiState.value = state.copy(
            score = state.score + pts,
            streak = newStreak,
            multiplier = multiplier,
            lastCorrect = true,
        )
        viewModelScope.launch {
            delay(CORRECT_FEEDBACK_MS)
            advanceGrid()
        }
    }

    private fun applyWrongResult(state: EagleEyeUiState.Playing) {
        val phase = phaseFromGridSize(state.grid.size)
        wrongAnswers++
        when (phase) {
            GridPhase.EASY -> easyTotal++
            GridPhase.MEDIUM -> mediumTotal++
            GridPhase.HARD -> hardTotal++
        }
        _uiState.value = state.copy(
            score = (state.score - 1).coerceAtLeast(0),
            streak = 0,
            multiplier = 1,
            lastCorrect = false,
            revealOdd = true,
        )
        viewModelScope.launch {
            delay(WRONG_FEEDBACK_MS)
            advanceGrid()
        }
    }

    private fun advanceGrid() {
        val state = _uiState.value as? EagleEyeUiState.Playing ?: return
        if (state.timeRemainingMs <= 0L) {
            endGame(state)
            return
        }
        val phase = phaseForCorrectCount(correctAnswers)
        val nextGrid = generateGrid(phase)
        _uiState.value = state.copy(
            grid = nextGrid,
            gridIndex = state.gridIndex + 1,
            gridShownAtMs = System.currentTimeMillis(),
            lastCorrect = null,
            revealOdd = false,
        )
        scheduleTimeout()
    }

    private fun endGame(state: EagleEyeUiState.Playing) {
        timeoutJob?.cancel()
        val elapsed = GAME_DURATION_MS - state.timeRemainingMs
        val avgMs = if (correctAnswers > 0) totalResponseMs / correctAnswers else 0L
        _uiState.value = EagleEyeUiState.GameOver(
            finalScore = state.score,
            correctAnswers = correctAnswers,
            wrongAnswers = wrongAnswers,
            bestStreak = bestStreak,
            avgResponseMs = avgMs,
            easyCorrect = easyCorrect,
            easyTotal = easyTotal,
            mediumCorrect = mediumCorrect,
            mediumTotal = mediumTotal,
            hardCorrect = hardCorrect,
            hardTotal = hardTotal,
            durationMs = elapsed,
        )
    }

    internal fun generateGrid(phase: GridPhase): GridState {
        val size = when (phase) {
            GridPhase.EASY -> 3
            GridPhase.MEDIUM -> 4
            GridPhase.HARD -> 5
        }
        val distractorDir = SymbolDirection.entries.random()
        val distractorColor = if (phase == GridPhase.EASY) SymbolColor.BLUE
            else SymbolColor.entries.random()
        val distractorShape = if (phase == GridPhase.HARD) SymbolShape.entries.random()
            else SymbolShape.CIRCLE
        val distractor = GridSymbol(distractorDir, distractorColor, distractorShape, 0f)

        val total = size * size
        var oddIdx: Int
        do {
            oddIdx = (0 until total).random()
        } while (oddIdx == previousOddIndex && total > 1)
        previousOddIndex = oddIdx

        val oddDir = SymbolDirection.entries.filter { it != distractorDir }.random()
        val oddColor = if (phase != GridPhase.EASY)
            SymbolColor.entries.filter { it != distractorColor }.random()
        else distractorColor
        val oddShape = if (phase == GridPhase.HARD)
            SymbolShape.entries.filter { it != distractorShape }.random()
        else distractorShape
        val oddRotation = if (phase == GridPhase.HARD) 15f else 0f

        val cells = MutableList(total) { distractor }
        cells[oddIdx] = GridSymbol(oddDir, oddColor, oddShape, oddRotation)
        return GridState(size, cells, oddIdx)
    }

    private fun phaseForCorrectCount(count: Int): GridPhase = when {
        count <= 10 -> GridPhase.EASY
        count <= 25 -> GridPhase.MEDIUM
        else -> GridPhase.HARD
    }

    private fun phaseFromGridSize(size: Int): GridPhase = when (size) {
        3 -> GridPhase.EASY
        4 -> GridPhase.MEDIUM
        else -> GridPhase.HARD
    }

    private fun multiplierFor(streak: Int): Int = when {
        streak >= 8 -> 3
        streak >= 4 -> 2
        else -> 1
    }

    private fun pointsFor(responseMs: Long): Int = when {
        responseMs < 1_000L -> 3
        responseMs < 2_500L -> 2
        else -> 1
    }
}
