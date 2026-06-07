package com.braintraining.game.homeward

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomewardScreen(
    onGameComplete: (score: Int, durationMs: Long) -> Unit,
    onExit: () -> Unit,
    viewModel: HomewardViewModel = viewModel(factory = remember {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                HomewardViewModel() as T
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
            is HomewardUiState.Playing -> PlayingContent(
                state = state,
                onCellTapped = viewModel::onCellTapped,
                onResetLevel = viewModel::onResetLevel,
                onExit = onExit,
            )
            is HomewardUiState.LevelComplete -> LevelCompleteContent(
                state = state,
                onNextLevel = viewModel::onNextLevel,
                onEndSession = { onGameComplete(state.totalScore, state.durationMs) },
            )
            is HomewardUiState.GameOver -> GameOverContent(
                state = state,
                onContinue = { onGameComplete(state.totalScore, state.durationMs) },
            )
        }
    }
}

@Composable
private fun PlayingContent(
    state: HomewardUiState.Playing,
    onCellTapped: (Cell) -> Unit,
    onResetLevel: () -> Unit,
    onExit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LevelHeader(level = state.level, onReset = onResetLevel, onExit = onExit)
        Spacer(Modifier.height(8.dp))
        StepCounter(stepBudget = state.stepBudget, stepsUsed = state.stepsUsed)
        Spacer(Modifier.height(20.dp))
        GameBoard(
            board = state.board,
            selectedAgentId = state.selectedAgentId,
            validMoveCells = state.validMoveCells,
            onCellTapped = onCellTapped,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(state.board.cols.toFloat() / state.board.rows.toFloat()),
        )
        Spacer(Modifier.height(16.dp))
        AgentLegend(agents = state.board.agents)
    }
}

@Composable
private fun LevelHeader(level: Int, onReset: () -> Unit, onExit: () -> Unit) {
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
        Text(
            text = "Level $level",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = onReset) {
            Text("Reset")
        }
    }
}

@Composable
private fun StepCounter(stepBudget: Int, stepsUsed: Int) {
    val stepsLeft = stepBudget - stepsUsed
    val progress = 1f - stepsUsed.toFloat() / stepBudget.toFloat().coerceAtLeast(1f)
    val barColor = when {
        progress > 0.5f -> Color(0xFF4CAF50)
        progress > 0.25f -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Steps: $stepsUsed / $stepBudget",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
            Text(
                text = "$stepsLeft left",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = barColor,
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun GameBoard(
    board: BoardState,
    selectedAgentId: Int?,
    validMoveCells: Set<Cell>,
    onCellTapped: (Cell) -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val cellSize = maxWidth / board.cols

        Column(
            modifier = Modifier
                .size(cellSize * board.cols, cellSize * board.rows)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
        ) {
            repeat(board.rows) { row ->
                Row(modifier = Modifier.height(cellSize)) {
                    repeat(board.cols) { col ->
                        val cell = Cell(row, col)
                        GridCell(
                            agent = board.agents.find { it.position == cell },
                            house = board.houses.find { it.position == cell },
                            isObstacle = cell in board.obstacles,
                            isSelected = board.agents
                                .find { !it.isHome && it.id == selectedAgentId }
                                ?.position == cell,
                            isValidMove = cell in validMoveCells,
                            modifier = Modifier.size(cellSize),
                            onClick = { onCellTapped(cell) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridCell(
    agent: AgentState?,
    house: HouseState?,
    isObstacle: Boolean,
    isSelected: Boolean,
    isValidMove: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isObstacle -> Color(0xFF37474F)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isValidMove -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = modifier
            .background(bgColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
            .then(if (!isObstacle) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (house != null) {
            val solvedHere = agent?.isHome == true
            if (solvedHere) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(house.color.toDisplayColor().copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(3.dp)
                        .border(2.dp, house.color.toDisplayColor(), RoundedCornerShape(6.dp)),
                )
            }
        }

        val unsolved = agent?.takeIf { !it.isHome }
        if (unsolved != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.62f)
                    .clip(CircleShape)
                    .background(unsolved.color.toDisplayColor())
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    ),
            )
        }
    }
}

@Composable
private fun AgentLegend(agents: List<AgentState>) {
    val unsolved = agents.filter { !it.isHome }
    if (unsolved.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${unsolved.size} remaining  ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
        )
        unsolved.forEach { agent ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(agent.color.toDisplayColor()),
            )
            Spacer(Modifier.width(6.dp))
        }
    }
}

@Composable
private fun LevelCompleteContent(
    state: HomewardUiState.LevelComplete,
    onNextLevel: () -> Unit,
    onEndSession: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Level ${state.level} Complete!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        StatRow("Steps used", "${state.stepsUsed} / ${state.stepBudget}")
        StatRow("Optimal path", "${state.minSteps} steps")
        StatRow("Efficiency bonus", "+${(state.stepBudget - state.stepsUsed) * 10}")
        StatRow("Level score", "+${state.levelScore}")
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        StatRow("Total score", state.totalScore.toString())
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNextLevel, modifier = Modifier.fillMaxWidth()) {
            Text("Next Level")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onEndSession, modifier = Modifier.fillMaxWidth()) {
            Text("End Session")
        }
    }
}

@Composable
private fun GameOverContent(
    state: HomewardUiState.GameOver,
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
            text = "Out of Steps!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        StatRow("Levels completed", state.levelsCompleted.toString())
        StatRow("Final score", state.totalScore.toString())
        Spacer(Modifier.height(40.dp))
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

private fun AgentColor.toDisplayColor(): Color = when (this) {
    AgentColor.RED -> Color(0xFFE53935)
    AgentColor.BLUE -> Color(0xFF1E88E5)
    AgentColor.GREEN -> Color(0xFF43A047)
    AgentColor.YELLOW -> Color(0xFFFDD835)
    AgentColor.PURPLE -> Color(0xFF8E24AA)
}
