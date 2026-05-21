package com.braintraining.game.compute_challenge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ComputeChallengeScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: ComputeChallengeViewModel = viewModel(factory = remember {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ComputeChallengeViewModel() as T
            }
        }
    }),
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentOnGameComplete by rememberUpdatedState(onGameComplete)
    val gameOver = uiState as? ComputeChallengeUiState.GameOver

    LaunchedEffect(gameOver) {
        if (gameOver != null) {
            currentOnGameComplete(gameOver.score, gameOver.durationMs)
        }
    }

    when (val state = uiState) {
        is ComputeChallengeUiState.Playing -> PlayingScreen(
            state = state,
            onDropTapped = viewModel::onDropTapped,
            onExit = onExit,
        )
        is ComputeChallengeUiState.GameOver -> Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Composable
private fun PlayingScreen(
    state: ComputeChallengeUiState.Playing,
    onDropTapped: (Int) -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        GameHeader(
            score = state.score,
            lives = state.lives,
            level = state.level,
            onExit = onExit,
        )
        TimerBar(timeRemainingMs = state.timeRemainingMs)
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Tap equations where the answer is correct",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
        )
        GameArea(
            drops = state.drops,
            onDropTapped = onDropTapped,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun GameHeader(
    score: Int,
    lives: Int,
    level: Int,
    onExit: () -> Unit,
) {
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(INITIAL_LIVES) { index ->
                Text(
                    text = if (index < lives) "♥" else "♡",
                    fontSize = 22.sp,
                    color = if (index < lives) Color(0xFFE53935) else Color.Gray,
                )
            }
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
private fun TimerBar(timeRemainingMs: Long) {
    val progress = (timeRemainingMs / 60_000f).coerceIn(0f, 1f)
    val color = when {
        progress > 0.5f -> Color(0xFF4CAF50)
        progress > 0.25f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
private fun GameArea(
    drops: List<Drop>,
    onDropTapped: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.clip(RectangleShape)) {
        val dropWidth = 160.dp
        val dropHeight = 60.dp

        drops.forEach { drop ->
            val x = ((maxWidth - dropWidth) * drop.xFraction).coerceAtLeast(0.dp)
            val y = (maxHeight * drop.yFraction).coerceAtLeast(0.dp)
            DropBubble(
                drop = drop,
                modifier = Modifier
                    .width(dropWidth)
                    .height(dropHeight)
                    .absoluteOffset(x = x, y = y)
                    .clickable { onDropTapped(drop.id) },
            )
        }
    }
}

@Composable
private fun DropBubble(
    drop: Drop,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0)))
            )
            .border(1.dp, Color(0xFF90CAF9), RoundedCornerShape(30.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = drop.equation,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}
