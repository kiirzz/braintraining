package com.braintraining.game.lost_in_migration

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.roundToInt

@Composable
fun LostInMigrationScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: LostInMigrationViewModel = viewModel(factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                LostInMigrationViewModel() as T
        }
    }),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        when (val state = uiState) {
            is LostInMigrationUiState.Countdown -> CountdownContent(state.secondsLeft)
            is LostInMigrationUiState.Playing -> PlayingContent(
                state = state,
                onAnswer = viewModel::onAnswer,
                onExit = onExit,
            )
            is LostInMigrationUiState.GameOver -> GameOverContent(
                state = state,
                onContinue = { onGameComplete(state.finalScore, state.durationMs) },
            )
        }
    }
}

@Composable
private fun CountdownContent(secondsLeft: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AnimatedContent(
            targetState = secondsLeft,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "countdown",
        ) { s ->
            Text(
                text = s.toString(),
                fontSize = 96.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun PlayingContent(
    state: LostInMigrationUiState.Playing,
    onAnswer: (BirdDirection) -> Unit,
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

        Text(
            text = "Which way is the center bird flying?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(Modifier.height(28.dp))

        AnimatedContent(
            targetState = state.flockIndex,
            transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(80)) },
            label = "flock",
        ) {
            FlockDisplay(flock = state.flock)
        }

        Spacer(Modifier.weight(1f))

        DirectionButtons(
            onAnswer = onAnswer,
            enabled = state.lastAnswerCorrect == null,
        )
        Spacer(Modifier.height(32.dp))
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
                    style = MaterialTheme.typography.labelSmall,
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
private fun FlockDisplay(flock: Flock) {
    val birds = listOf(
        flock.flankerDirections[0] to false,
        flock.flankerDirections[1] to false,
        flock.centerDirection to true,
        flock.flankerDirections[2] to false,
        flock.flankerDirections[3] to false,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        birds.forEachIndexed { i, (dir, isCenter) ->
            BirdIcon(direction = dir, isCenter = isCenter)
            if (i < birds.lastIndex) Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun BirdIcon(direction: BirdDirection, isCenter: Boolean) {
    Box(
        modifier = Modifier
            .size(if (isCenter) 60.dp else 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isCenter) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = direction.toArrow(),
            fontSize = if (isCenter) 30.sp else 22.sp,
            color = if (isCenter) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            fontWeight = if (isCenter) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun DirectionButtons(onAnswer: (BirdDirection) -> Unit, enabled: Boolean) {
    val size = 72.dp

    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DirButtonRow(center = BirdDirection.UP, size = size, enabled = enabled, onAnswer = onAnswer)
        Row {
            DirButton(BirdDirection.LEFT, size, enabled, onAnswer)
            Spacer(Modifier.size(size))
            DirButton(BirdDirection.RIGHT, size, enabled, onAnswer)
        }
        DirButtonRow(center = BirdDirection.DOWN, size = size, enabled = enabled, onAnswer = onAnswer)
    }
}

@Composable
private fun DirButtonRow(
    center: BirdDirection,
    size: Dp,
    enabled: Boolean,
    onAnswer: (BirdDirection) -> Unit,
) {
    Row {
        Spacer(Modifier.size(size))
        DirButton(center, size, enabled, onAnswer)
        Spacer(Modifier.size(size))
    }
}

@Composable
private fun DirButton(
    direction: BirdDirection,
    size: Dp,
    enabled: Boolean,
    onClick: (BirdDirection) -> Unit,
) {
    Button(
        onClick = { onClick(direction) },
        enabled = enabled,
        modifier = Modifier.size(size),
        contentPadding = PaddingValues(0.dp),
    ) {
        Text(direction.toArrow(), fontSize = 28.sp)
    }
}

@Composable
private fun GameOverContent(
    state: LostInMigrationUiState.GameOver,
    onContinue: () -> Unit,
) {
    val total = state.correctAnswers + state.wrongAnswers
    val accuracy = if (total > 0) state.correctAnswers.toFloat() / total else 0f

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
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.finalScore.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))
        StatRow("Accuracy", "${(accuracy * 100).roundToInt()}%")
        StatRow("Best Streak", state.bestStreak.toString())
        StatRow("Correct / Wrong", "${state.correctAnswers} / ${state.wrongAnswers}")

        if (state.congruentTotal > 0) {
            val pct = (state.congruentCorrect.toFloat() / state.congruentTotal * 100).roundToInt()
            StatRow("Easy (congruent)", "$pct%  (${state.congruentCorrect}/${state.congruentTotal})")
        }
        if (state.incongruentTotal > 0) {
            val pct = (state.incongruentCorrect.toFloat() / state.incongruentTotal * 100).roundToInt()
            StatRow("Medium (opposing)", "$pct%  (${state.incongruentCorrect}/${state.incongruentTotal})")
        }
        if (state.mixedTotal > 0) {
            val pct = (state.mixedCorrect.toFloat() / state.mixedTotal * 100).roundToInt()
            StatRow("Hard (mixed)", "$pct%  (${state.mixedCorrect}/${state.mixedTotal})")
        }

        Spacer(Modifier.height(32.dp))
        Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) {
            Text("Continue")
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
