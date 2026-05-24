package com.braintraining.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `game_results` (
                    `id` TEXT NOT NULL,
                    `game_id` TEXT NOT NULL,
                    `user_id` TEXT NOT NULL,
                    `score` INTEGER NOT NULL,
                    `duration_ms` INTEGER NOT NULL,
                    `played_at` INTEGER NOT NULL,
                    `skill_area` TEXT NOT NULL,
                    PRIMARY KEY(`id`),
                    FOREIGN KEY(`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_game_results_game_id` ON `game_results` (`game_id`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `performance_snapshots` (
                    `id` TEXT NOT NULL,
                    `skill_area` TEXT NOT NULL,
                    `bpi` REAL NOT NULL,
                    `taken_at` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `training_sessions` (
                    `id` TEXT NOT NULL,
                    `date` INTEGER NOT NULL,
                    `is_completed` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `training_session_games` (
                    `session_id` TEXT NOT NULL,
                    `game_id` TEXT NOT NULL,
                    `order_index` INTEGER NOT NULL,
                    `is_completed` INTEGER NOT NULL,
                    PRIMARY KEY(`session_id`, `game_id`),
                    FOREIGN KEY(`session_id`) REFERENCES `training_sessions`(`id`) ON DELETE CASCADE,
                    FOREIGN KEY(`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_training_session_games_session_id` ON `training_session_games` (`session_id`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_training_session_games_game_id` ON `training_session_games` (`game_id`)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `daily_streaks` (
                    `id` INTEGER NOT NULL,
                    `current_streak` INTEGER NOT NULL,
                    `longest_streak` INTEGER NOT NULL,
                    `last_played_date` INTEGER,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `user_stats` (
                    `user_id` TEXT NOT NULL,
                    `elo_rating` INTEGER NOT NULL,
                    `global_rank` INTEGER,
                    `last_synced_at` INTEGER NOT NULL,
                    PRIMARY KEY(`user_id`)
                )
                """.trimIndent()
            )
        }
    }
}
