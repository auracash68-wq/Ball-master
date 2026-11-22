package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameAudioManager
import com.example.audio.GameHapticsManager
import com.example.data.GameProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameProgressRepository(application.applicationContext)
    private val audio = GameAudioManager()
    private val haptics = GameHapticsManager(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var roundJob: Job? = null
    private var hasInitializedSplash = false

    init {
        observeSavedProgress()
        runSplashSequence()
    }

    private fun observeSavedProgress() {
        viewModelScope.launch {
            repository.progressFlow.collect { saved ->
                audio.soundEnabled = saved.soundEnabled
                haptics.vibrationEnabled = saved.vibrationEnabled
                _uiState.update { state ->
                    state.copy(
                        unlockedLevel = saved.unlockedLevel,
                        coins = saved.coins,
                        levelStars = saved.levelStars,
                        rewardedLevels = saved.rewardedLevels,
                        soundEnabled = saved.soundEnabled,
                        vibrationEnabled = saved.vibrationEnabled
                    )
                }
            }
        }
    }

    private fun runSplashSequence() {
        if (hasInitializedSplash) return
        hasInitializedSplash = true
        viewModelScope.launch {
            val saved = repository.progressFlow.first()
            audio.soundEnabled = saved.soundEnabled
            haptics.vibrationEnabled = saved.vibrationEnabled

            delay(1750L)

            val startLevel = saved.currentLevel.coerceIn(1, saved.unlockedLevel)
            _uiState.update {
                it.copy(
                    screen = ScreenRoute.Gameplay,
                    splashProgress = 1f,
                    currentLevel = startLevel,
                    levelSelectPage = (startLevel - 1) / GameUiState.LEVELS_PER_PAGE
                )
            }
            startLevel(level = startLevel, resetAttempts = true)
        }
    }

    fun startLevel(level: Int, resetAttempts: Boolean = false) {
        roundJob?.cancel()
        val safeLevel = level.coerceIn(1, GameProgressRepository.TOTAL_LEVELS)

        viewModelScope.launch {
            repository.setCurrentLevel(safeLevel)
        }

        val initialBallCup = Random.nextInt(3)
        _uiState.update { state ->
            state.copy(
                screen = ScreenRoute.Gameplay,
                currentLevel = safeLevel,
                levelSelectPage = (safeLevel - 1) / GameUiState.LEVELS_PER_PAGE,
                phase = GamePhase.Preparing,
                isPaused = false,
                cups = GameUiState.defaultCups(),
                slotToCupId = listOf(0, 1, 2),
                ballCupId = initialBallCup,
                selectedSlot = null,
                currentLevelAttempts = if (resetAttempts) 0 else state.currentLevelAttempts
            )
        }

        roundJob = viewModelScope.launch {
            val config = LevelDifficultyGenerator.configForLevel(safeLevel)
            val moves = LevelDifficultyGenerator.generateMoves(config)

            // Brief settle pause before lifting cup
            pausableDelay(360L)

            // 1. Lift the cup containing the golden ball to show the player
            _uiState.update { it.copy(phase = GamePhase.RevealingInitial) }
            audio.playCupLift()
            animateCupLift(targetCupIds = setOf(initialBallCup), fromLift = 0f, toLift = 1f, durationMs = 300L)

            // 2. Hold so player sees the golden ball clearly
            pausableDelay(config.revealDurationMs)

            // 3. Lower the cup back over the ball
            _uiState.update { it.copy(phase = GamePhase.CoveringBall) }
            animateCupLift(targetCupIds = setOf(initialBallCup), fromLift = 1f, toLift = 0f, durationMs = 260L)
            pausableDelay(240L)

            // 4. Execute the cup shuffling sequence
            _uiState.update { it.copy(phase = GamePhase.Shuffling) }
            val currentSlots = intArrayOf(0, 1, 2)

            for (move in moves) {
                waitWhilePaused()
                val cupA = currentSlots[move.slotA]
                val cupB = currentSlots[move.slotB]
                val startXA = GameUiState.slotX(move.slotA)
                val startXB = GameUiState.slotX(move.slotB)

                audio.playCupMove()
                haptics.shuffleTick()

                animateSwap(
                    cupA = cupA,
                    cupB = cupB,
                    startXA = startXA,
                    endXA = startXB,
                    startXB = startXB,
                    endXB = startXA,
                    arcDirection = move.arcDirection,
                    durationMs = move.durationMs
                )

                // Commit logical swap immediately upon completion
                currentSlots[move.slotA] = cupB
                currentSlots[move.slotB] = cupA
                val committedSlots = currentSlots.toList()
                _uiState.update { state ->
                    state.copy(
                        slotToCupId = committedSlots,
                        cups = state.cups.map { cup ->
                            val slotIdx = committedSlots.indexOf(cup.cupId)
                            cup.copy(xNorm = GameUiState.slotX(slotIdx), zDepth = 0f)
                        }
                    )
                }

                if (config.pauseBetweenSwapsMs > 0L) {
                    pausableDelay(config.pauseBetweenSwapsMs)
                }
            }

            // 5. All shuffling stopped — allow exactly one answer
            _uiState.update { it.copy(phase = GamePhase.AwaitingChoice) }
        }
    }

    fun selectAnswerSlot(slotIndex: Int) {
        val state = _uiState.value
        if (!state.canChooseCup || slotIndex !in 0..2) return

        val chosenCupId = state.slotToCupId[slotIndex]
        val isCorrect = chosenCupId == state.ballCupId

        audio.playButtonClick()
        haptics.click()

        // Immediately lock out any second answer
        _uiState.update {
            it.copy(
                phase = GamePhase.RevealingChoice,
                selectedSlot = slotIndex
            )
        }

        roundJob?.cancel()
        roundJob = viewModelScope.launch {
            audio.playCupLift()
            animateCupLift(targetCupIds = setOf(chosenCupId), fromLift = 0f, toLift = 1f, durationMs = 310L)

            if (isCorrect) {
                audio.playCorrectAnswer()
                haptics.success()
                pausableDelay(520L)

                val attempts = _uiState.value.currentLevelAttempts
                val earnedStars = when {
                    attempts == 0 -> 3
                    attempts == 1 -> 2
                    else -> 1
                }
                val level = _uiState.value.currentLevel
                val alreadyClaimed = _uiState.value.rewardedLevels.contains(level)
                val awardedCoins = repository.recordLevelCompletion(
                    level = level,
                    earnedStars = earnedStars,
                    coinReward = GameProgressRepository.LEVEL_REWARD_COINS
                )

                audio.playLevelCompleted()
                if (awardedCoins > 0) {
                    launch {
                        delay(260L)
                        audio.playCoinReward()
                    }
                }

                _uiState.update {
                    it.copy(
                        phase = GamePhase.LevelCompleted,
                        lastRoundStarsEarned = earnedStars,
                        lastRoundCoinsAwarded = awardedCoins,
                        lastRoundAlreadyClaimed = alreadyClaimed
                    )
                }
            } else {
                audio.playWrongAnswer()
                haptics.error()
                pausableDelay(380L)

                // Also reveal where the golden ball actually was so the player knows it was fair!
                val realBallCupId = _uiState.value.ballCupId
                audio.playCupLift()
                animateCupLift(targetCupIds = setOf(realBallCupId), fromLift = 0f, toLift = 1f, durationMs = 280L)
                pausableDelay(680L)

                audio.playGameOver()
                _uiState.update {
                    it.copy(
                        phase = GamePhase.GameOver,
                        currentLevelAttempts = it.currentLevelAttempts + 1
                    )
                }
            }
        }
    }

    fun pauseGame() {
        val state = _uiState.value
        if (state.screen != ScreenRoute.Gameplay) return
        if (state.phase == GamePhase.LevelCompleted || state.phase == GamePhase.GameOver) return
        audio.playButtonClick()
        haptics.click()
        _uiState.update { it.copy(isPaused = true) }
    }

    fun resumeGame() {
        audio.playButtonClick()
        haptics.click()
        _uiState.update { it.copy(isPaused = false) }
    }

    fun restartCurrentLevel() {
        audio.playButtonClick()
        haptics.click()
        val lvl = _uiState.value.currentLevel
        startLevel(level = lvl, resetAttempts = false)
    }

    fun retryAfterResult() {
        audio.playButtonClick()
        haptics.click()
        val lvl = _uiState.value.currentLevel
        startLevel(level = lvl, resetAttempts = false)
    }

    fun continueToNextLevel() {
        audio.playButtonClick()
        haptics.click()
        val current = _uiState.value.currentLevel
        val next = (current + 1).coerceAtMost(GameProgressRepository.TOTAL_LEVELS)
        startLevel(level = next, resetAttempts = true)
    }

    fun skipCurrentLevel() {
        audio.playButtonClick()
        haptics.click()
        viewModelScope.launch {
            val nextLevel = repository.skipLevel(_uiState.value.currentLevel)
            startLevel(level = nextLevel, resetAttempts = true)
        }
    }

    fun openLevelSelection() {
        audio.playButtonClick()
        haptics.click()
        roundJob?.cancel()
        val page = (_uiState.value.currentLevel - 1) / GameUiState.LEVELS_PER_PAGE
        _uiState.update {
            it.copy(
                screen = ScreenRoute.LevelSelect,
                isPaused = false,
                levelSelectPage = page
            )
        }
    }

    fun onLevelSelected(level: Int) {
        val unlocked = _uiState.value.unlockedLevel
        if (level > unlocked) {
            audio.playLockedClick()
            haptics.error()
            return
        }
        audio.playButtonClick()
        haptics.click()
        startLevel(level = level, resetAttempts = true)
    }

    fun previousLevelPage() {
        val current = _uiState.value.levelSelectPage
        if (current > 0) {
            audio.playButtonClick()
            haptics.click()
            _uiState.update { it.copy(levelSelectPage = current - 1) }
        }
    }

    fun nextLevelPage() {
        val state = _uiState.value
        if (state.levelSelectPage < state.totalLevelPages - 1) {
            audio.playButtonClick()
            haptics.click()
            _uiState.update { it.copy(levelSelectPage = state.levelSelectPage + 1) }
        }
    }

    fun toggleSound(enabled: Boolean) {
        audio.soundEnabled = enabled
        if (enabled) audio.playButtonClick()
        haptics.click()
        _uiState.update { it.copy(soundEnabled = enabled) }
        viewModelScope.launch {
            repository.setSoundEnabled(enabled)
        }
    }

    fun toggleVibration(enabled: Boolean) {
        haptics.vibrationEnabled = enabled
        audio.playButtonClick()
        if (enabled) haptics.click()
        _uiState.update { it.copy(vibrationEnabled = enabled) }
        viewModelScope.launch {
            repository.setVibrationEnabled(enabled)
        }
    }

    fun onSystemBackPressed(): Boolean {
        val state = _uiState.value
        return when {
            state.screen == ScreenRoute.Splash -> false
            state.screen == ScreenRoute.LevelSelect -> {
                startLevel(state.currentLevel, resetAttempts = false)
                true
            }
            state.isPaused -> {
                resumeGame()
                true
            }
            state.phase == GamePhase.LevelCompleted || state.phase == GamePhase.GameOver -> {
                openLevelSelection()
                true
            }
            state.screen == ScreenRoute.Gameplay -> {
                pauseGame()
                true
            }
            else -> false
        }
    }

    private suspend fun waitWhilePaused() {
        while (_uiState.value.isPaused) {
            delay(32L)
        }
    }

    private suspend fun pausableDelay(totalMs: Long) {
        var remaining = totalMs
        val slice = 16L
        while (remaining > 0L) {
            waitWhilePaused()
            val step = minOf(slice, remaining)
            delay(step)
            remaining -= step
        }
    }

    private suspend fun animateCupLift(
        targetCupIds: Set<Int>,
        fromLift: Float,
        toLift: Float,
        durationMs: Long
    ) {
        val frameMs = 16L
        val steps = (durationMs / frameMs).toInt().coerceAtLeast(1)
        for (i in 1..steps) {
            waitWhilePaused()
            val rawT = i / steps.toFloat()
            // Smooth cubic ease-in-out
            val eased = rawT * rawT * (3f - 2f * rawT)
            val liftVal = fromLift + (toLift - fromLift) * eased
            _uiState.update { state ->
                state.copy(
                    cups = state.cups.map { cup ->
                        if (cup.cupId in targetCupIds) cup.copy(lift = liftVal) else cup
                    }
                )
            }
            delay(frameMs)
        }
    }

    private suspend fun animateSwap(
        cupA: Int,
        cupB: Int,
        startXA: Float,
        endXA: Float,
        startXB: Float,
        endXB: Float,
        arcDirection: Float,
        durationMs: Long
    ) {
        val frameMs = 16L
        val steps = (durationMs / frameMs).toInt().coerceAtLeast(1)
        for (i in 1..steps) {
            waitWhilePaused()
            val rawT = i / steps.toFloat()
            val eased = rawT * rawT * (3f - 2f * rawT)
            val arcDepth = sin(rawT * PI).toFloat() * arcDirection

            val xA = startXA + (endXA - startXA) * eased
            val xB = startXB + (endXB - startXB) * eased

            _uiState.update { state ->
                state.copy(
                    cups = state.cups.map { cup ->
                        when (cup.cupId) {
                            cupA -> cup.copy(xNorm = xA, zDepth = arcDepth)
                            cupB -> cup.copy(xNorm = xB, zDepth = -arcDepth)
                            else -> cup.copy(zDepth = 0f)
                        }
                    }
                )
            }
            delay(frameMs)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audio.release()
    }
}
