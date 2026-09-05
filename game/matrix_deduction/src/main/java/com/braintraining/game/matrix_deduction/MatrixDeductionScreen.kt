package com.braintraining.game.matrix_deduction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MatrixDeductionScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
) {
    val viewModel: MatrixDeductionViewModel = viewModel(factory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                MatrixDeductionViewModel() as T
        }
    })
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is MatrixDeductionUiState.Countdown -> CountdownScreen(s.secondsLeft)
        is MatrixDeductionUiState.Playing -> PlayingContent(
            state = s,
            onChoiceTapped = viewModel::onChoiceTapped,
            onExit = onExit,
        )
        is MatrixDeductionUiState.GameOver -> GameOverContent(
            state = s,
            onContinue = { onGameComplete(s.finalScore, s.durationMs) },
            onExit = onExit,
        )
    }
}

@Composable
private fun CountdownScreen(secondsLeft: Int) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = secondsLeft,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "countdown",
        ) { sec ->
            Text(
                text = sec.toString(),
                fontSize = 120.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PlayingContent(
    state: MatrixDeductionUiState.Playing,
    onChoiceTapped: (Int) -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        ScoreHeader(
            score = state.score,
            multiplier = state.multiplier,
            streak = state.streak,
            timeRemainingMs = state.timeRemainingMs,
            onExit = onExit,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnimatedContent(
                targetState = state.puzzle to state.puzzleIndex,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "puzzle",
            ) { (puzzle, _) ->
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MatrixGrid(puzzle = puzzle)
                    Text(
                        text = "Which tile completes the pattern?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    ChoiceGrid(
                        choices = puzzle.choices,
                        lastCorrect = state.lastCorrect,
                        revealAnswerIndex = state.revealAnswerIndex,
                        lastTappedIndex = state.lastTappedIndex,
                        onChoiceTapped = onChoiceTapped,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ScoreHeader(
    score: Int,
    multiplier: Int,
    streak: Int,
    timeRemainingMs: Long,
    onExit: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onExit) {
                Text("Exit", color = MaterialTheme.colorScheme.error)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (multiplier > 1) {
                    Text(
                        text = "×$multiplier",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = (timeRemainingMs / 1000L).toInt().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "streak $streak",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }
        val progress = (timeRemainingMs / 60_000f).coerceIn(0f, 1f)
        val barColor = when {
            progress > 0.5f -> Color(0xFF4CAF50)
            progress > 0.25f -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun MatrixGrid(
    puzzle: MatrixPuzzle,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(3) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(3) { col ->
                    val idx = row * 3 + col
                    val tile = puzzle.cells[idx]
                    if (tile == null) {
                        TargetCell(modifier = Modifier.weight(1f))
                    } else {
                        MatrixCell(tile = tile, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixCell(tile: Tile, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        TileCanvas(tile = tile, modifier = Modifier.fillMaxSize(0.88f))
    }
}

@Composable
private fun TargetCell(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ChoiceGrid(
    choices: List<Tile>,
    lastCorrect: Boolean?,
    revealAnswerIndex: Int?,
    lastTappedIndex: Int?,
    onChoiceTapped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceTile(
                tile = choices[0],
                feedback = feedbackFor(0, lastCorrect, revealAnswerIndex, lastTappedIndex),
                onClick = { onChoiceTapped(0) },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                tile = choices[1],
                feedback = feedbackFor(1, lastCorrect, revealAnswerIndex, lastTappedIndex),
                onClick = { onChoiceTapped(1) },
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ChoiceTile(
                tile = choices[2],
                feedback = feedbackFor(2, lastCorrect, revealAnswerIndex, lastTappedIndex),
                onClick = { onChoiceTapped(2) },
                modifier = Modifier.weight(1f),
            )
            ChoiceTile(
                tile = choices[3],
                feedback = feedbackFor(3, lastCorrect, revealAnswerIndex, lastTappedIndex),
                onClick = { onChoiceTapped(3) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private enum class ChoiceFeedback { NORMAL, CORRECT, WRONG, REVEAL }

private fun feedbackFor(
    index: Int,
    lastCorrect: Boolean?,
    revealAnswerIndex: Int?,
    lastTappedIndex: Int?,
): ChoiceFeedback = when {
    index == lastTappedIndex && lastCorrect == true -> ChoiceFeedback.CORRECT
    index == lastTappedIndex && lastCorrect == false -> ChoiceFeedback.WRONG
    index == revealAnswerIndex && lastCorrect == false -> ChoiceFeedback.REVEAL
    else -> ChoiceFeedback.NORMAL
}

@Composable
private fun ChoiceTile(
    tile: Tile,
    feedback: ChoiceFeedback,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor = when (feedback) {
        ChoiceFeedback.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
        ChoiceFeedback.CORRECT -> Color(0xFF4CAF50)
        ChoiceFeedback.WRONG -> Color(0xFFF44336).copy(alpha = 0.7f)
        ChoiceFeedback.REVEAL -> Color(0xFF4CAF50).copy(alpha = 0.75f)
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (feedback != ChoiceFeedback.NORMAL) 2.dp else 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(
                    alpha = if (feedback != ChoiceFeedback.NORMAL) 0.6f else 0.2f
                ),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        TileCanvas(tile = tile, modifier = Modifier.fillMaxSize(0.78f))
    }
}

@Composable
private fun TileCanvas(tile: Tile, modifier: Modifier = Modifier) {
    val color = tile.color.toComposeColor()
    Canvas(modifier = modifier) {
        drawTileSymbols(tile.shape, tile.count, color)
    }
}

private fun DrawScope.drawTileSymbols(shape: TileShape, count: Int, color: Color) {
    val centerY = size.height / 2f
    val (r, xPositions) = when (count) {
        1 -> Pair(
            minOf(size.width, size.height) * 0.28f,
            listOf(size.width / 2f),
        )
        2 -> Pair(
            minOf(size.width, size.height) * 0.20f,
            listOf(size.width * 0.28f, size.width * 0.72f),
        )
        else -> Pair(
            minOf(size.width, size.height) * 0.15f,
            listOf(size.width * 0.20f, size.width * 0.50f, size.width * 0.80f),
        )
    }
    xPositions.forEach { cx ->
        when (shape) {
            TileShape.CIRCLE -> drawCircle(color, radius = r, center = Offset(cx, centerY))
            TileShape.SQUARE -> drawRect(
                color,
                topLeft = Offset(cx - r, centerY - r),
                size = Size(r * 2f, r * 2f),
            )
            TileShape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(cx, centerY - r)
                    lineTo(cx + r, centerY + r * 0.75f)
                    lineTo(cx - r, centerY + r * 0.75f)
                    close()
                }
                drawPath(path, color)
            }
        }
    }
}

private fun TileColor.toComposeColor(): Color = when (this) {
    TileColor.BLUE -> Color(0xFF1976D2)
    TileColor.RED -> Color(0xFFE53935)
    TileColor.GREEN -> Color(0xFF43A047)
}

@Composable
private fun GameOverContent(
    state: MatrixDeductionUiState.GameOver,
    onContinue: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Game Over",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = state.finalScore.toString(),
            fontSize = 80.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "points",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )

        HorizontalDivider()

        StatRow("Correct", state.correctAnswers.toString())
        StatRow("Wrong", state.wrongAnswers.toString())
        StatRow("Best streak", state.bestStreak.toString())
        StatRow("Avg response", "${state.avgResponseMs} ms")

        HorizontalDivider()

        Text(
            text = "Phase breakdown",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        PhaseRow("Easy", state.easyCorrect, state.easyTotal)
        PhaseRow("Medium", state.mediumCorrect, state.mediumTotal)
        PhaseRow("Hard", state.hardCorrect, state.hardTotal)

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Exit")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PhaseRow(phase: String, correct: Int, total: Int) {
    val pct = if (total > 0) correct * 100 / total else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            phase,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        )
        Text(
            "$correct/$total ($pct%)",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
