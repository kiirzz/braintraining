package com.braintraining.game.speed_match

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun SpeedMatchScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
) {
    val viewModel: SpeedMatchViewModel = viewModel(factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                SpeedMatchViewModel() as T
        }
    })
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        when (val state = uiState) {
            is SpeedMatchUiState.Countdown -> CountdownContent(state.secondsLeft)
            is SpeedMatchUiState.Playing -> PlayingContent(
                state = state,
                onAnswer = viewModel::onAnswer,
                onExit = onExit,
            )
            is SpeedMatchUiState.GameOver -> GameOverContent(
                state = state,
                onContinue = { onGameComplete(state.finalScore, state.durationMs) },
            )
        }
    }
}

@Composable
private fun CountdownContent(secondsLeft: Int) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = secondsLeft,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "countdown",
        ) { seconds ->
            Text(
                text = seconds.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PlayingContent(
    state: SpeedMatchUiState.Playing,
    onAnswer: (Boolean) -> Unit,
    onExit: () -> Unit,
) {
    val flashOverlay = when (state.lastAnswerCorrect) {
        true -> Color(0xFF43A047).copy(alpha = 0.12f)
        false -> Color(0xFFE53935).copy(alpha = 0.12f)
        null -> Color.Transparent
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(flashOverlay),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScoreHeader(
            score = state.score,
            multiplier = state.multiplier,
            timeRemainingMs = state.timeRemainingMs,
            onExit = onExit,
        )

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = state.cardIndex,
            transitionSpec = {
                (fadeIn(tween(150)) + slideInVertically(tween(150)) { it / 3 }) togetherWith
                        fadeOut(tween(100))
            },
            label = "symbol_card",
        ) {
            SymbolCard(symbol = state.currentSymbol, showColor = state.showShapeColor)
        }

        Spacer(Modifier.height(32.dp))

        if (state.previousSymbol == null) {
            Text(
                text = "Remember this symbol...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(80.dp))
        } else {
            Text(
                text = "Does it match the previous?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(12.dp))
            MultiplierBadge(multiplier = state.multiplier, streak = state.streak)
            Spacer(Modifier.height(20.dp))
            AnswerButtons(
                onAnswer = onAnswer,
                enabled = state.lastAnswerCorrect == null,
            )
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ScoreHeader(
    score: Int,
    multiplier: Int,
    timeRemainingMs: Long,
    onExit: () -> Unit,
) {
    val progress = (timeRemainingMs / 60_000f).coerceIn(0f, 1f)
    val timerColor = when {
        progress > 0.5f -> Color(0xFF4CAF50)
        progress > 0.25f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

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
                    text = "x$multiplier",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = "${timeRemainingMs / 1000L}s",
            style = MaterialTheme.typography.titleMedium,
            color = timerColor,
            fontWeight = FontWeight.Bold,
        )
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp),
        color = timerColor,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun SymbolCard(symbol: Symbol, showColor: Boolean) {
    val shapeColor = if (showColor) symbol.color.toDisplayColor() else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .size(180.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(104.dp)) {
            drawSymbolShape(symbol.shape, shapeColor)
        }
    }
}

@Composable
private fun MultiplierBadge(multiplier: Int, streak: Int) {
    if (multiplier <= 1) return
    val color = if (multiplier == 2) Color(0xFFFF9800) else Color(0xFFF44336)
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = "x$multiplier  streak $streak",
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun AnswerButtons(onAnswer: (Boolean) -> Unit, enabled: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(
            onClick = { onAnswer(false) },
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
        ) {
            Text("NO", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
        Button(
            onClick = { onAnswer(true) },
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
        ) {
            Text("YES", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
private fun GameOverContent(
    state: SpeedMatchUiState.GameOver,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Time's Up!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Score: ${state.finalScore}",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(32.dp))
        StatRow("Accuracy", "${(state.accuracy * 100).roundToInt()}%")
        StatRow("Best Streak", state.bestStreak.toString())
        StatRow("Correct", state.correctAnswers.toString())
        StatRow("Wrong", state.wrongAnswers.toString())
        Spacer(Modifier.height(40.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun SymbolColor.toDisplayColor(): Color = when (this) {
    SymbolColor.RED -> Color(0xFFE53935)
    SymbolColor.BLUE -> Color(0xFF1E88E5)
    SymbolColor.GREEN -> Color(0xFF43A047)
    SymbolColor.YELLOW -> Color(0xFFFDD835)
    SymbolColor.PURPLE -> Color(0xFF8E24AA)
}

private fun DrawScope.drawSymbolShape(shape: Shape, color: Color) {
    when (shape) {
        Shape.CIRCLE -> drawCircle(color)
        Shape.SQUARE -> drawRect(color)
        Shape.TRIANGLE -> {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(path, color)
        }
        Shape.DIAMOND -> {
            val path = Path().apply {
                moveTo(size.width / 2f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(size.width / 2f, size.height)
                lineTo(0f, size.height / 2f)
                close()
            }
            drawPath(path, color)
        }
        Shape.STAR -> drawPath(starPath(size.width / 2f, size.height / 2f, size.width / 2f, size.width / 4.5f), color)
    }
}

private fun starPath(cx: Float, cy: Float, outerR: Float, innerR: Float): Path {
    val path = Path()
    for (i in 0 until 10) {
        val angle = Math.PI * i / 5.0 - Math.PI / 2.0
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (r * cos(angle)).toFloat()
        val y = cy + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}