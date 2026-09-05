package com.braintraining.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.braintraining.game.compute_challenge.ComputeChallengeScreen
import com.braintraining.game.eagle_eye.EagleEyeScreen
import com.braintraining.game.homeward.HomewardScreen
import com.braintraining.game.lost_in_migration.LostInMigrationScreen
import com.braintraining.game.matrix_deduction.MatrixDeductionScreen
import com.braintraining.game.memory_matrix.MemoryMatrixScreen
import com.braintraining.game.speed_match.SpeedMatchScreen

@Composable
fun PlayGameScreen(
    gameId: String,
    navController: NavHostController,
) {
    fun leave() {
        navController.popBackStack()
    }

    when (gameId) {
        GameIds.SpeedMatch -> SpeedMatchScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.MemoryMatrix -> MemoryMatrixScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.EagleEye -> EagleEyeScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.LostInMigration -> LostInMigrationScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.Homeward -> HomewardScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.MatrixDeduction -> MatrixDeductionScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        GameIds.ComputeChallenge -> ComputeChallengeScreen(
            onGameComplete = { _, _ -> leave() },
            onExit = { leave() },
        )
        else -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text("This game is not available yet.")
        }
    }
}
