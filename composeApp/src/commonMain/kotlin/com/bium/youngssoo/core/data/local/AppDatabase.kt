package com.bium.youngssoo.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.bium.youngssoo.core.data.database.StringListTypeConverter
import com.bium.youngssoo.minigame.data.local.MiniGameProgressDao
import com.bium.youngssoo.minigame.data.local.MiniGameProgressEntity
import com.bium.youngssoo.reward.data.local.UserStatsDao
import com.bium.youngssoo.reward.data.local.UserStatsEntity

@Database(
    entities = [UserStatsEntity::class, QuestionEntity::class, MiniGameProgressEntity::class],
    version = 4
)
@TypeConverters(StringListTypeConverter::class, QuestionCategoryConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun questionDao(): QuestionDao
    abstract fun miniGameProgressDao(): MiniGameProgressDao

    companion object {
        // Version 3 -> 4: 미니게임 진행도 테이블 추가
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL("""
                    CREATE TABLE IF NOT EXISTS mini_game_progress (
                        gameId TEXT NOT NULL PRIMARY KEY,
                        currentStage INTEGER NOT NULL DEFAULT 1,
                        highScore INTEGER NOT NULL DEFAULT 0,
                        totalPlayCount INTEGER NOT NULL DEFAULT 0,
                        totalPlayTime INTEGER NOT NULL DEFAULT 0,
                        lastPlayedAt INTEGER NOT NULL DEFAULT 0,
                        customData TEXT NOT NULL DEFAULT '{}'
                    )
                """.trimIndent())
            }
        }

        // Version 2 -> 3: 새로운 컬럼 추가 (연속출석, 에너지, 레벨 시스템)
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // 연속 출석 관련 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN longestStreak INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN lastStreakDate TEXT NOT NULL DEFAULT ''")

                // 에너지 시스템 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN energy INTEGER NOT NULL DEFAULT 100")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN maxEnergy INTEGER NOT NULL DEFAULT 100")

                // 레벨 시스템 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN level INTEGER NOT NULL DEFAULT 1")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN experience INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalExperience INTEGER NOT NULL DEFAULT 0")

                // 통계 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalGamesPlayed INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalCorrectAnswers INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            }
        }

        // Version 1 -> 3: 처음부터 업그레이드하는 경우
        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(connection: SQLiteConnection) {
                // hanjaDailyCount (v1 -> v2에서 추가된 것)
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN hanjaDailyCount INTEGER NOT NULL DEFAULT 0")

                // 연속 출석 관련 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN currentStreak INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN longestStreak INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN lastStreakDate TEXT NOT NULL DEFAULT ''")

                // 에너지 시스템 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN energy INTEGER NOT NULL DEFAULT 100")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN maxEnergy INTEGER NOT NULL DEFAULT 100")

                // 레벨 시스템 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN level INTEGER NOT NULL DEFAULT 1")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN experience INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalExperience INTEGER NOT NULL DEFAULT 0")

                // 통계 컬럼
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalGamesPlayed INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN totalCorrectAnswers INTEGER NOT NULL DEFAULT 0")
                connection.execSQL("ALTER TABLE user_stats ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}
