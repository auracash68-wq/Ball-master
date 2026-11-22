package com.example.game

import com.example.data.GameProgressRepository

enum class ScreenRoute {
    Splash,
    Gameplay,
    LevelSelect
}

enum class GamePhase {
    Preparing,
    RevealingInitial,
    CoveringBall,
    Shuffling,
    AwaitingChoice,
    RevealingChoice,
    LevelCompleted,
    GameOver
}

data class CupVisualState(
    val cupId: Int,
    /** Normalized horizontal slot coordinate: -1f (Left), 0f (Center), +1f (Right). */
    val xNorm: Float,
    /** 3D depth arc offset during swaps: >0 closer to camera, <0 farther back. */
    val zDepth: Float = 0f,
    /** Vertical lift fraction: 0f resting on stone, 1f fully lifted. */
    val lift: Float = 0f
)

data class SwapMove(
    val slotA: Int,
    val slotB: Int,
    val durationMs: Long,
    val arcDirection: Float = 1f
)

data class LevelConfig(
    val levelNumber: Int,
    val swapCount: Int,
    val baseSwapDurationMs: Long,
    val revealDurationMs: Long,
    val pauseBetweenSwapsMs: Long,
    val allowOuterSwaps: Boolean,
    val allowFeintReversals: Boolean
)

data class GameUiState(
    val screen: ScreenRoute = ScreenRoute.Splash,
    val splashProgress: Float = 0f,
    val currentLevel: Int = 1,
    val totalLevels: Int = GameProgressRepository.TOTAL_LEVELS,
    val unlockedLevel: Int = 1,
    val coins: Int = 0,
    val levelStars: Map<Int, Int> = emptyMap(),
    val rewardedLevels: Set<Int> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val levelSelectPage: Int = 0,

    // Active round state
    val phase: GamePhase = GamePhase.Preparing,
    val isPaused: Boolean = false,
    val cups: List<CupVisualState> = defaultCups(),
    val slotToCupId: List<Int> = listOf(0, 1, 2),
    val ballCupId: Int = 1,
    val selectedSlot: Int? = null,
    val lastRoundStarsEarned: Int = 3,
    val lastRoundCoinsAwarded: Int = GameProgressRepository.LEVEL_REWARD_COINS,
    val lastRoundAlreadyClaimed: Boolean = false,
    val currentLevelAttempts: Int = 0
) {
    val totalStars: Int
        get() = levelStars.values.sum()

    val currentLevelEarnedStars: Int
        get() = levelStars[currentLevel] ?: 0

    val canChooseCup: Boolean
        get() = phase == GamePhase.AwaitingChoice && !isPaused

    val totalLevelPages: Int
        get() = (totalLevels + LEVELS_PER_PAGE - 1) / LEVELS_PER_PAGE

    companion object {
        const val LEVELS_PER_PAGE = 15

        fun slotX(slot: Int): Float = when (slot) {
            0 -> -1f
            1 -> 0f
            else -> 1f
        }

        fun defaultCups(): List<CupVisualState> = listOf(
            CupVisualState(cupId = 0, xNorm = -1f),
            CupVisualState(cupId = 1, xNorm = 0f),
            CupVisualState(cupId = 2, xNorm = 1f)
        )
    }
}
