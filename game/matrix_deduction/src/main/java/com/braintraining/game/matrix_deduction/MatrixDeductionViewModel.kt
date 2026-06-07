package com.braintraining.game.matrix_deduction

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
private const val CORRECT_FEEDBACK_MS = 300L
private const val WRONG_FEEDBACK_MS = 500L

internal enum class MatrixPhase { EASY, MEDIUM, HARD }

private fun timeoutMs(phase: MatrixPhase): Long = when (phase) {
    MatrixPhase.EASY -> 8_000L
    MatrixPhase.MEDIUM -> 7_000L
    MatrixPhase.HARD -> 6_000L
}

internal class MatrixDeductionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<MatrixDeductionUiState>(
        MatrixDeductionUiState.Countdown(COUNTDOWN_SECONDS)
    )
    val uiState: StateFlow<MatrixDeductionUiState> = _uiState.asStateFlow()

    private var timeoutJob: Job? = null
    private var puzzleShownAtMs = 0L

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
            _uiState.value = MatrixDeductionUiState.Countdown(s)
            delay(1_000L)
        }
        startGame()
    }

    private fun startGame() {
        val phase = MatrixPhase.EASY
        val puzzle = generatePuzzle(phase)
        puzzleShownAtMs = System.currentTimeMillis()
        _uiState.value = MatrixDeductionUiState.Playing(
            puzzle = puzzle,
            puzzleIndex = 0,
            score = 0,
            streak = 0,
            multiplier = 1,
            timeRemainingMs = GAME_DURATION_MS,
            lastCorrect = null,
            revealAnswerIndex = null,
            lastTappedIndex = null,
        )
        viewModelScope.launch { runGameTimer() }
        scheduleTimeout(phase)
    }

    private suspend fun runGameTimer() {
        while (true) {
            delay(TICK_MS)
            val state = _uiState.value as? MatrixDeductionUiState.Playing ?: return
            val newTime = (state.timeRemainingMs - TICK_MS).coerceAtLeast(0L)
            if (newTime == 0L) {
                timeoutJob?.cancel()
                endGame(state)
                return
            }
            _uiState.value = state.copy(timeRemainingMs = newTime)
        }
    }

    private fun scheduleTimeout(phase: MatrixPhase) {
        timeoutJob?.cancel()
        timeoutJob = viewModelScope.launch {
            delay(timeoutMs(phase))
            val state = _uiState.value as? MatrixDeductionUiState.Playing ?: return@launch
            if (state.lastCorrect != null) return@launch
            applyWrongResult(state, tappedIndex = null)
        }
    }

    fun onChoiceTapped(choiceIndex: Int) {
        val state = _uiState.value as? MatrixDeductionUiState.Playing ?: return
        if (state.lastCorrect != null) return
        timeoutJob?.cancel()
        val responseMs = System.currentTimeMillis() - puzzleShownAtMs
        if (choiceIndex == state.puzzle.answerIndex) {
            applyCorrectResult(state, responseMs, choiceIndex)
        } else {
            applyWrongResult(state, choiceIndex)
        }
    }

    private fun applyCorrectResult(
        state: MatrixDeductionUiState.Playing,
        responseMs: Long,
        tappedIndex: Int,
    ) {
        val phase = phaseForCorrectCount(correctAnswers)
        correctAnswers++
        when (phase) {
            MatrixPhase.EASY -> { easyCorrect++; easyTotal++ }
            MatrixPhase.MEDIUM -> { mediumCorrect++; mediumTotal++ }
            MatrixPhase.HARD -> { hardCorrect++; hardTotal++ }
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
            revealAnswerIndex = null,
            lastTappedIndex = tappedIndex,
        )
        viewModelScope.launch {
            delay(CORRECT_FEEDBACK_MS)
            advancePuzzle()
        }
    }

    private fun applyWrongResult(
        state: MatrixDeductionUiState.Playing,
        tappedIndex: Int?,
    ) {
        val phase = phaseForCorrectCount(correctAnswers)
        wrongAnswers++
        when (phase) {
            MatrixPhase.EASY -> easyTotal++
            MatrixPhase.MEDIUM -> mediumTotal++
            MatrixPhase.HARD -> hardTotal++
        }
        _uiState.value = state.copy(
            score = (state.score - 1).coerceAtLeast(0),
            streak = 0,
            multiplier = 1,
            lastCorrect = false,
            revealAnswerIndex = state.puzzle.answerIndex,
            lastTappedIndex = tappedIndex,
        )
        viewModelScope.launch {
            delay(WRONG_FEEDBACK_MS)
            advancePuzzle()
        }
    }

    private fun advancePuzzle() {
        val state = _uiState.value as? MatrixDeductionUiState.Playing ?: return
        if (state.timeRemainingMs <= 0L) {
            endGame(state)
            return
        }
        val phase = phaseForCorrectCount(correctAnswers)
        val nextPuzzle = generatePuzzle(phase)
        puzzleShownAtMs = System.currentTimeMillis()
        _uiState.value = state.copy(
            puzzle = nextPuzzle,
            puzzleIndex = state.puzzleIndex + 1,
            lastCorrect = null,
            revealAnswerIndex = null,
            lastTappedIndex = null,
        )
        scheduleTimeout(phase)
    }

    private fun endGame(state: MatrixDeductionUiState.Playing) {
        timeoutJob?.cancel()
        val elapsed = GAME_DURATION_MS - state.timeRemainingMs
        val avgMs = if (correctAnswers > 0) totalResponseMs / correctAnswers else 0L
        _uiState.value = MatrixDeductionUiState.GameOver(
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

    internal fun generatePuzzle(phase: MatrixPhase): MatrixPuzzle {
        val shapes = when (phase) {
            MatrixPhase.HARD -> latinRows(TileShape.entries)
            else -> constRows(TileShape.entries.random())
        }
        val colors = when (phase) {
            MatrixPhase.EASY -> constRows(TileColor.entries.random())
            else -> latinRows(TileColor.entries)
        }
        val counts = latinRows(listOf(1, 2, 3))

        val allCells = List(9) { idx ->
            Tile(shape = shapes[idx / 3][idx % 3], color = colors[idx / 3][idx % 3], count = counts[idx / 3][idx % 3])
        }
        val answer = allCells[8]
        val cells: MutableList<Tile?> = allCells.toMutableList()
        cells[8] = null

        val distractors = buildDistractors(answer, phase)
        val allChoices = (distractors + answer).shuffled()
        return MatrixPuzzle(
            cells = cells,
            answer = answer,
            choices = allChoices,
            answerIndex = allChoices.indexOf(answer),
        )
    }

    private fun <T> latinRows(values: List<T>): List<List<T>> = List(3) { values.shuffled() }

    private fun <T> constRows(value: T): List<List<T>> = List(3) { List(3) { value } }

    private fun buildDistractors(answer: Tile, phase: MatrixPhase): List<Tile> {
        val wrongCounts = (1..3).filter { it != answer.count }
        val wrongColors = TileColor.entries.filter { it != answer.color }
        val wrongShapes = TileShape.entries.filter { it != answer.shape }
        return when (phase) {
            MatrixPhase.EASY -> listOf(
                answer.copy(count = wrongCounts[0]),
                answer.copy(count = wrongCounts[1]),
                answer.copy(count = wrongCounts[0], color = wrongColors[0]),
            )
            MatrixPhase.MEDIUM -> listOf(
                answer.copy(count = wrongCounts[0]),
                answer.copy(color = wrongColors[0]),
                answer.copy(count = wrongCounts[1], color = wrongColors[1]),
            )
            MatrixPhase.HARD -> listOf(
                answer.copy(count = wrongCounts.random()),
                answer.copy(color = wrongColors.random()),
                answer.copy(shape = wrongShapes.random()),
            )
        }
    }

    private fun phaseForCorrectCount(count: Int): MatrixPhase = when {
        count <= 8 -> MatrixPhase.EASY
        count <= 20 -> MatrixPhase.MEDIUM
        else -> MatrixPhase.HARD
    }

    private fun multiplierFor(streak: Int): Int = when {
        streak >= 8 -> 3
        streak >= 4 -> 2
        else -> 1
    }

    private fun pointsFor(responseMs: Long): Int = when {
        responseMs < 2_000L -> 4
        responseMs < 4_000L -> 2
        else -> 1
    }
}
