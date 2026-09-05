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

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DELETE FROM training_session_games")
            db.execSQL("DELETE FROM records")
            db.execSQL("DELETE FROM games")
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('speed_match', 'Speed Match', 'Decide whether the current symbol matches the previous one. Train processing speed under time pressure.', '1')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('memory_matrix', 'Memory Matrix', 'Memorize the highlighted tiles, then tap them back in the same pattern.', '2')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('eagle_eye', 'Eagle Eye', 'Find the one shape that does not belong before time runs out.', '3')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('lost_in_migration', 'Lost in Migration', 'Spot the bird flying in a different direction from the flock.', '3')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('homeward', 'Homeward', 'Trace a path home through the grid while planning each move carefully.', '4')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('matrix_deduction', 'Matrix Deduction', 'Study the pattern and choose the missing tile that completes the matrix.', '5')"
            )
            db.execSQL(
                "INSERT INTO games (id, name, description, skill_id) VALUES " +
                    "('compute_challenge', 'Compute Challenge', 'Tap falling equations that are true and let the false ones pass.', '6')"
            )
        }
    }
}
