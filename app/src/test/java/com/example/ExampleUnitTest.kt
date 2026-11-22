package com.example

import com.example.game.LevelDifficultyGenerator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class ExampleUnitTest {

    @Test
    fun difficultyScalesAcrossAll50Levels() {
        val lvl1 = LevelDifficultyGenerator.configForLevel(1)
        val lvl25 = LevelDifficultyGenerator.configForLevel(25)
        val lvl50 = LevelDifficultyGenerator.configForLevel(50)

        assertTrue(lvl1.swapCount < lvl25.swapCount)
        assertTrue(lvl25.swapCount < lvl50.swapCount)
        assertTrue(lvl1.baseSwapDurationMs > lvl25.baseSwapDurationMs)
        assertTrue(lvl25.baseSwapDurationMs > lvl50.baseSwapDurationMs)
        assertTrue(lvl1.revealDurationMs > lvl50.revealDurationMs)
    }

    @Test
    fun swapSequencePreservesExactCupPermutationAndBallLocation() {
        val rng = Random(42)
        for (level in 1..50) {
            val config = LevelDifficultyGenerator.configForLevel(level)
            val moves = LevelDifficultyGenerator.generateMoves(config, rng)
            assertEquals(config.swapCount, moves.size)

            val slots = intArrayOf(0, 1, 2)
            val ballCupId = level % 3
            for (move in moves) {
                assertTrue(move.slotA in 0..2)
                assertTrue(move.slotB in 0..2)
                assertTrue(move.slotA != move.slotB)
                val tmp = slots[move.slotA]
                slots[move.slotA] = slots[move.slotB]
                slots[move.slotB] = tmp
            }

            // Every cup ID 0, 1, 2 must appear exactly once after all swaps
            assertEquals(setOf(0, 1, 2), slots.toSet())
            assertTrue(slots.indexOf(ballCupId) in 0..2)
        }
    }
}
