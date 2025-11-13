package com.braintraining.feature.games

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.braintraining.core.model.Game
import com.braintraining.core.model.Skill
import com.braintraining.core.model.SkillWithGame

@Composable
fun GameScreen(
    navController: NavHostController,
    viewModel: GameViewModel = hiltViewModel()
) {
    val skillWithGame by viewModel.skillWithGames.collectAsState()

    GameScreenContent(skillWithGame = skillWithGame, navController = navController)
}

@Composable
fun GameScreenContent(
    skillWithGame: List<SkillWithGame>,
    navController: NavHostController
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Games",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(skillWithGame) { item ->
            Text(
                text = item.skill.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(item.games) { g ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                navController.navigate("game_detail/${g.id}")
                            }
                    ) {
                        GameBox(g, item.skill)
                    }
                }
            }
        }
    }
}

@Composable
fun GameBox(game: Game, skill: Skill) {
    Box(
        modifier = Modifier
            .background(Color.White, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 160.dp, height = 100.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
            Text(
                text = game.name,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = skill.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}
