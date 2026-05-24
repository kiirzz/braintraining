package com.braintraining.game.eagle_eye

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

@Composable
fun EagleEyeScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: EagleEyeViewModel = viewModel(factory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                EagleEyeViewModel() as T
        }
    }),
) {
    val state by viewModel.uiState.collectAsState()

    when (val s = state) {
        is EagleEyeUiState.Countdown -> CountdownScreen(s.secondsLeft)
        is EagleEyeUiState.Playing -> PlayingContent(
            state = s,
            onCellTapped = viewModel::onCellTapped,
            onExit = onExit,
        )
        is EagleEyeUiState.GameOver -> GameOverContent(
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
    state: EagleEyeUiState.Playing,
    onCellTapped: (Int) -> Unit,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedContent(
                targetState = state.grid to state.gridIndex,
                transitionSpec = { fadeIn(tween(150)) togetherWith fadeOut(tween(150)) },
                label = "grid",
            ) { (grid, gridIndex) ->
                EyeGrid(
                    grid = grid,
                    gridKey = gridIndex,
                    lastCorrect = state.lastCorrect,
                    revealOdd = state.revealOdd,
                    onCellTapped = onCellTapped,
                )
            }
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
private fun EyeGrid(
    grid: GridState,
    gridKey: Int,
    lastCorrect: Boolean?,
    revealOdd: Boolean,
    onCellTapped: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(grid.size),
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        userScrollEnabled = false,
    ) {
        itemsIndexed(grid.cells, key = { index, _ -> index }) { index, symbol ->
            val visualState = when {
                lastCorrect == true -> CellVisualState.CORRECT
                revealOdd && index == grid.oddIndex -> CellVisualState.REVEAL
                lastCorrect == false -> CellVisualState.WRONG
                else -> CellVisualState.NORMAL
            }
            GridCell(
                symbol = symbol,
                visualState = visualState,
                cellIndex = index,
                gridKey = gridKey,
                onClick = { onCellTapped(index) },
            )
        }
    }
}

private enum class CellVisualState { NORMAL, CORRECT, WRONG, REVEAL }

@Composable
private fun GridCell(
    symbol: GridSymbol,
    visualState: CellVisualState,
    cellIndex: Int,
    gridKey: Int,
    onClick: () -> Unit,
) {
    val shakeOffset = remember { Animatable(0f) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(gridKey) {
        visible = false
        delay(cellIndex * 20L)
        visible = true
    }

    LaunchedEffect(visualState) {
        if (visualState == CellVisualState.WRONG) {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(8f, tween(40))
            shakeOffset.animateTo(-8f, tween(40))
            shakeOffset.animateTo(6f, tween(35))
            shakeOffset.animateTo(-6f, tween(35))
            shakeOffset.animateTo(0f, tween(30))
        }
    }

    val bgColor = when (visualState) {
        CellVisualState.NORMAL -> MaterialTheme.colorScheme.surfaceVariant
        CellVisualState.CORRECT -> Color(0xFF4CAF50)
        CellVisualState.WRONG -> Color(0xFFF44336).copy(alpha = 0.7f)
        CellVisualState.REVEAL -> Color(0xFFFFB300)
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)),
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .offset(x = shakeOffset.value.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(bgColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            SymbolCanvas(
                symbol = symbol,
                modifier = Modifier
                    .fillMaxSize(0.6f)
                    .aspectRatio(1f),
            )
        }
    }
}

@Composable
private fun SymbolCanvas(symbol: GridSymbol, modifier: Modifier = Modifier) {
    val color = symbol.color.toComposeColor()
    Canvas(modifier = modifier) {
        if (symbol.shape == SymbolShape.CIRCLE) {
            drawDirectionalArrow(symbol.direction, symbol.rotationDegrees, color)
        } else {
            drawGeometricShape(symbol.shape, symbol.direction, symbol.rotationDegrees, color)
        }
    }
}

private fun DrawScope.drawDirectionalArrow(
    direction: SymbolDirection,
    extraRotation: Float,
    color: Color,
) {
    rotate(direction.baseRotationDeg() + extraRotation, pivot = center) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val halfH = size.height * 0.30f
        val path = Path().apply {
            moveTo(cx + size.width * 0.35f, cy)
            lineTo(cx - size.width * 0.25f, cy - halfH)
            lineTo(cx - size.width * 0.25f, cy + halfH)
            close()
        }
        drawPath(path, color)
    }
}

private fun DrawScope.drawGeometricShape(
    shape: SymbolShape,
    direction: SymbolDirection,
    extraRotation: Float,
    color: Color,
) {
    rotate(direction.baseRotationDeg() + extraRotation, pivot = center) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = minOf(size.width, size.height) * 0.38f
        when (shape) {
            SymbolShape.CIRCLE -> drawCircle(color, radius = r, center = Offset(cx, cy))
            SymbolShape.SQUARE -> drawRect(
                color,
                topLeft = Offset(cx - r, cy - r),
                size = Size(r * 2f, r * 2f),
            )
            SymbolShape.TRIANGLE -> {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy + r * 0.7f)
                    lineTo(cx - r, cy + r * 0.7f)
                    close()
                }
                drawPath(path, color)
            }
            SymbolShape.DIAMOND -> {
                val path = Path().apply {
                    moveTo(cx, cy - r)
                    lineTo(cx + r, cy)
                    lineTo(cx, cy + r)
                    lineTo(cx - r, cy)
                    close()
                }
                drawPath(path, color)
            }
        }
    }
}

private fun SymbolDirection.baseRotationDeg(): Float = when (this) {
    SymbolDirection.RIGHT -> 0f
    SymbolDirection.DOWN -> 90f
    SymbolDirection.LEFT -> 180f
    SymbolDirection.UP -> 270f
}

private fun SymbolColor.toComposeColor(): Color = when (this) {
    SymbolColor.BLUE -> Color(0xFF1976D2)
    SymbolColor.RED -> Color(0xFFE53935)
    SymbolColor.GREEN -> Color(0xFF43A047)
    SymbolColor.YELLOW -> Color(0xFFFFB300)
}

@Composable
private fun GameOverContent(
    state: EagleEyeUiState.GameOver,
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
