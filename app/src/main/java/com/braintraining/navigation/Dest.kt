package com.braintraining.navigation

object Dest {
    const val Home = "home"
    const val Games = "games"
    const val Stats = "stats"
    const val Account = "account"
    const val GameDetailWithId = "game_detail/{gameId}/{skillId}"
    const val PlayGameWithId = "play/{gameId}"

    fun gameDetail(gameId: String, skillId: String) = "game_detail/$gameId/$skillId"

    fun playGame(gameId: String) = "play/$gameId"
}

object GameIds {
    const val SpeedMatch = "speed_match"
    const val MemoryMatrix = "memory_matrix"
    const val EagleEye = "eagle_eye"
    const val LostInMigration = "lost_in_migration"
    const val Homeward = "homeward"
    const val MatrixDeduction = "matrix_deduction"
    const val ComputeChallenge = "compute_challenge"
}
