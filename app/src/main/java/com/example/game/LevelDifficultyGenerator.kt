package com.example.game

import kotlin.math.roundToLong
import kotlin.random.Random

/**
 * Generates deterministic-difficulty, randomized shuffle patterns for all 50 levels.
 * Difficulty scales progressively via swap count, speed, shorter observation window,
 * wider outer swaps, and deceptive quick-reversal patterns.
 */
object LevelDifficultyGenerator {

    fun configForLevel(level: Int): LevelConfig {
        val lvl = level.coerceIn(1, 50)
        val progress = (lvl - 1) / 49f // 0.0 at level 1 -> 1.0 at level 50

        val swapCount = when {
            lvl <= 3 -> 4 + (lvl - 1)                // 4..6 swaps
            lvl <= 10 -> 6 + ((lvl - 3) * 0.7f).toInt() // 6..10 swaps
            lvl <= 25 -> 10 + ((lvl - 10) * 0.45f).toInt() // 10..16 swaps
            else -> 16 + ((lvl - 25) * 0.36f).toInt()      // 16..25 swaps
        }

        // Smooth speed curve: 540ms at Level 1 down to 165ms at Level 50
        val baseDuration = (540f - progress * 375f).roundToLong().coerceIn(165L, 540L)

        // Initial reveal observation duration: 1350ms at Level 1 down to 680ms at Level 50
        val revealDuration = (1350f - progress * 670f).roundToLong().coerceIn(680L, 1350L)

        // Pause between swaps shrinks as level rises
        val pauseBetween = (120f - progress * 100f).roundToLong().coerceIn(18L, 120L)

        return LevelConfig(
            levelNumber = lvl,
            swapCount = swapCount,
            baseSwapDurationMs = baseDuration,
            revealDurationMs = revealDuration,
            pauseBetweenSwapsMs = pauseBetween,
            allowOuterSwaps = lvl >= 2,
            allowFeintReversals = lvl >= 7
        )
    }

    fun generateMoves(config: LevelConfig, random: Random = Random.Default): List<SwapMove> {
        val moves = mutableListOf<SwapMove>()
        val pairs = if (config.allowOuterSwaps) {
            listOf(0 to 1, 1 to 2, 0 to 2)
        } else {
            listOf(0 to 1, 1 to 2)
        }

        var lastPair: Pair<Int, Int>? = null
        var i = 0
        while (i < config.swapCount) {
            val candidates = pairs.filter { it != lastPair }
            val chosen = if (config.allowFeintReversals && lastPair != null && random.nextFloat() < 0.22f) {
                // Quick back-and-forth feint swap to trick the player's eye
                lastPair
            } else {
                candidates[random.nextInt(candidates.size)]
            }

            // Slight tempo variance (+-12%) and outer swaps get +12% duration so speed feels natural
            val outerBonus = if (chosen == (0 to 2)) 1.14f else 1.0f
            val speedJitter = 0.90f + random.nextFloat() * 0.20f
            val duration = (config.baseSwapDurationMs * outerBonus * speedJitter)
                .roundToLong()
                .coerceAtLeast(155L)

            val arcDir = if (random.nextBoolean()) 1f else -1f
            moves.add(
                SwapMove(
                    slotA = chosen.first,
                    slotB = chosen.second,
                    durationMs = duration,
                    arcDirection = arcDir
                )
            )
            lastPair = chosen
            i++
        }
        return moves
    }
}
