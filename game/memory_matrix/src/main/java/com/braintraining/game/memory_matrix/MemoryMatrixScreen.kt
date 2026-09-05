package com.braintraining.game.memory_matrix

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

private enum class TileVisual { Idle, Highlighted, Selected, Correct, Wrong }

@Composable
fun MemoryMatrixScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
) {
    val viewModel: MemoryMatrixViewModel = viewModel(factory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MemoryMatrixViewModel() as T
            }
        }
    })
    val uiState by viewModel.uiState.collectAsState()
    val currentOnGameComplete by rememberUpdatedState(onGameComplete)
    val gameOver = uiState as? MemoryMatrixUiState.GameOver

    LaunchedEffect(gameOver) {
        if (gameOver != null) {
            currentOnGameComplete(gameOver.finalScore, gameOver.durationMs)
        }
    }

    when (val state = uiState) {
        is MemoryMatrixUiState.Loading -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        is MemoryMatrixUiState.ShowPattern -> GameScreen(
            level = state.level,
            score = state.score,
            instruction = "Memorize the pattern!",
            onExit = onExit,
            header = {
                ShowPhaseTimer(showDurationMs = state.showDurationMs)
            },
            grid = {
                GridBoard(
                    grid = state.grid,
                    getTileVisual = { r, c ->
                        if (state.grid[r][c]) TileVisual.Highlighted else TileVisual.Idle
                    },
                    onTileTapped = { _, _ -> },
                )
            },
        )

        is MemoryMatrixUiState.RecallPhase -> GameScreen(
            level = state.level,
            score = state.score,
            instruction = "Select ${state.tilesToSelect} tiles",
            onExit = onExit,
            header = { Spacer(Modifier.height(6.dp)) },
            grid = {
                GridBoard(
                    grid = state.grid,
                    getTileVisual = { r, c ->
                        if ((r to c) in state.selected) TileVisual.Selected else TileVisual.Idle
                    },
                    onTileTapped = viewModel::onTileTapped,
                )
            },
        )

        is MemoryMatrixUiState.RoundResult -> GameScreen(
            level = state.level,
            score = state.score,
            instruction = if (state.correct) "Correct!" else "Wrong!",
            instructionColor = if (state.correct) Color(0xFF4CAF50) else Color(0xFFF44336),
            onExit = onExit,
            header = { Spacer(Modifier.height(6.dp)) },
            grid = {
                GridBoard(
                    grid = state.grid,
                    getTileVisual = { r, c ->
                        val inPattern = state.grid[r][c]
                        val inSelected = (r to c) in state.selected
                        when {
                            inPattern && inSelected -> TileVisual.Correct
                            inPattern && !inSelected -> TileVisual.Wrong
                            !inPattern && inSelected -> TileVisual.Wrong
                            else -> TileVisual.Idle
                        }
                    },
                    onTileTapped = { _, _ -> },
                )
            },
        )

        is MemoryMatrixUiState.GameOver -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun GameScreen(
    level: Int,
    score: Int,
    instruction: String,
    onExit: () -> Unit,
    header: @Composable () -> Unit,
    grid: @Composable () -> Unit,
    instructionColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RoundHeader(level = level, score = score, onExit = onExit)
        header()
        Spacer(Modifier.height(8.dp))
        Text(
            text = instruction,
            style = MaterialTheme.typography.bodyMedium,
            color = instructionColor,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        grid()
    }
}

@Composable
private fun RoundHeader(level: Int, score: Int, onExit: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onExit) {
            Text("Exit", color = MaterialTheme.colorScheme.error)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "LVL $level",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun ShowPhaseTimer(showDurationMs: Long) {
    var progress by remember(showDurationMs) { mutableFloatStateOf(1f) }
    LaunchedEffect(showDurationMs) {
        val startTime = System.currentTimeMillis()
        while (progress > 0f) {
            delay(16L)
            val elapsed = System.currentTimeMillis() - startTime
            progress = (1f - elapsed.toFloat() / showDurationMs).coerceAtLeast(0f)
        }
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun GridBoard(
    grid: List<List<Boolean>>,
    getTileVisual: (row: Int, col: Int) -> TileVisual,
    onTileTapped: (row: Int, col: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cols = grid.firstOrNull()?.size ?: 3
    val rows = grid.size

    LazyVerticalGrid(
        columns = GridCells.Fixed(cols),
        modifier = modifier.fillMaxWidth(),
        userScrollEnabled = false,
    ) {
        items(rows * cols) { index ->
            val row = index / cols
            val col = index % cols
            TileCell(
                visual = getTileVisual(row, col),
                onClick = { onTileTapped(row, col) },
            )
        }
    }
}

@Composable
private fun TileCell(
    visual: TileVisual,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = when (visual) {
            TileVisual.Idle -> Color(0xFF2A2A3A)
            TileVisual.Highlighted -> Color(0xFF5C6BC0)
            TileVisual.Selected -> Color(0xFF1976D2)
            TileVisual.Correct -> Color(0xFF388E3C)
            TileVisual.Wrong -> Color(0xFFD32F2F)
        },
        animationSpec = tween(durationMillis = 200),
        label = "tileBg",
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(
                enabled = visual == TileVisual.Idle || visual == TileVisual.Selected,
                onClick = onClick,
            ),
    )
}
