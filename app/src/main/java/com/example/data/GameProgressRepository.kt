package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameProgressDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "ball_master_progress_v1"
)

data class SavedGameProgress(
    val unlockedLevel: Int = 1,
    val currentLevel: Int = 1,
    val coins: Int = 0,
    val levelStars: Map<Int, Int> = emptyMap(),
    val rewardedLevels: Set<Int> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true
) {
    val totalStars: Int
        get() = levelStars.values.sum()
}

class GameProgressRepository(private val context: Context) {

    private object Keys {
        val UNLOCKED_LEVEL = intPreferencesKey("unlocked_level")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val COINS = intPreferencesKey("coins")
        val LEVEL_STARS = stringPreferencesKey("level_stars_map")
        val REWARDED_LEVELS = stringPreferencesKey("rewarded_levels_set")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
    }

    val progressFlow: Flow<SavedGameProgress> = context.gameProgressDataStore.data.map { prefs ->
        SavedGameProgress(
            unlockedLevel = (prefs[Keys.UNLOCKED_LEVEL] ?: 1).coerceIn(1, TOTAL_LEVELS),
            currentLevel = (prefs[Keys.CURRENT_LEVEL] ?: 1).coerceIn(1, TOTAL_LEVELS),
            coins = (prefs[Keys.COINS] ?: 0).coerceAtLeast(0),
            levelStars = decodeStarsMap(prefs[Keys.LEVEL_STARS].orEmpty()),
            rewardedLevels = decodeIntSet(prefs[Keys.REWARDED_LEVELS].orEmpty()),
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true
        )
    }

    suspend fun recordLevelCompletion(
        level: Int,
        earnedStars: Int,
        coinReward: Int
    ): Int {
        var coinsActuallyGranted = 0
        context.gameProgressDataStore.edit { prefs ->
            val currentUnlocked = (prefs[Keys.UNLOCKED_LEVEL] ?: 1).coerceIn(1, TOTAL_LEVELS)
            val nextUnlocked = maxOf(currentUnlocked, (level + 1).coerceAtMost(TOTAL_LEVELS))
            prefs[Keys.UNLOCKED_LEVEL] = nextUnlocked

            val starsMap = decodeStarsMap(prefs[Keys.LEVEL_STARS].orEmpty()).toMutableMap()
            val prevStars = starsMap[level] ?: 0
            starsMap[level] = maxOf(prevStars, earnedStars.coerceIn(1, 3))
            prefs[Keys.LEVEL_STARS] = encodeStarsMap(starsMap)

            val rewarded = decodeIntSet(prefs[Keys.REWARDED_LEVELS].orEmpty()).toMutableSet()
            if (!rewarded.contains(level)) {
                rewarded.add(level)
                prefs[Keys.REWARDED_LEVELS] = encodeIntSet(rewarded)
                val prevCoins = (prefs[Keys.COINS] ?: 0).coerceAtLeast(0)
                prefs[Keys.COINS] = prevCoins + coinReward
                coinsActuallyGranted = coinReward
            }
        }
        return coinsActuallyGranted
    }

    suspend fun skipLevel(currentLevel: Int): Int {
        val nextLevel = (currentLevel + 1).coerceAtMost(TOTAL_LEVELS)
        context.gameProgressDataStore.edit { prefs ->
            val currentUnlocked = (prefs[Keys.UNLOCKED_LEVEL] ?: 1).coerceIn(1, TOTAL_LEVELS)
            prefs[Keys.UNLOCKED_LEVEL] = maxOf(currentUnlocked, nextLevel)
            prefs[Keys.CURRENT_LEVEL] = nextLevel
        }
        return nextLevel
    }

    suspend fun setCurrentLevel(level: Int) {
        context.gameProgressDataStore.edit { prefs ->
            prefs[Keys.CURRENT_LEVEL] = level.coerceIn(1, TOTAL_LEVELS)
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.gameProgressDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.gameProgressDataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    private fun encodeStarsMap(map: Map<Int, Int>): String =
        map.entries.sortedBy { it.key }.joinToString(",") { "${it.key}:${it.value}" }

    private fun decodeStarsMap(raw: String): Map<Int, Int> {
        if (raw.isBlank()) return emptyMap()
        val result = mutableMapOf<Int, Int>()
        raw.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val lvl = parts[0].toIntOrNull()
                val stars = parts[1].toIntOrNull()
                if (lvl != null && stars != null && lvl in 1..TOTAL_LEVELS) {
                    result[lvl] = stars.coerceIn(0, 3)
                }
            }
        }
        return result
    }

    private fun encodeIntSet(set: Set<Int>): String =
        set.sorted().joinToString(",")

    private fun decodeIntSet(raw: String): Set<Int> {
        if (raw.isBlank()) return emptySet()
        return raw.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    companion object {
        const val TOTAL_LEVELS = 50
        const val LEVEL_REWARD_COINS = 100
    }
}
