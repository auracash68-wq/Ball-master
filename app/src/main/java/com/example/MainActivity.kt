package com.example

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.GameViewModel
import com.example.game.ScreenRoute
import com.example.ui.screens.GameOverOverlay
import com.example.ui.screens.GamePausedOverlay
import com.example.ui.screens.GameplayScreen
import com.example.ui.screens.LevelCompletedOverlay
import com.example.ui.screens.LevelSelectScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by gameViewModel.uiState.collectAsStateWithLifecycle()

                // Enforce Portrait for Splash (Screen 01) and Landscape for Gameplay & Overlays (Screens 02-06)
                LaunchedEffect(uiState.screen) {
                    val targetOrientation = if (uiState.screen == ScreenRoute.Splash) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                    }
                    if (requestedOrientation != targetOrientation) {
                        requestedOrientation = targetOrientation
                    }
                }

                BackHandler(enabled = uiState.screen != ScreenRoute.Splash) {
                    gameViewModel.onSystemBackPressed()
                }

                BallMasterAppHost(
                    uiState = uiState,
                    onSelectSlot = gameViewModel::selectAnswerSlot,
                    onPauseClick = gameViewModel::pauseGame,
                    onResumeClick = gameViewModel::resumeGame,
                    onRestartLevel = gameViewModel::restartCurrentLevel,
                    onRetryResult = gameViewModel::retryAfterResult,
                    onContinueNext = gameViewModel::continueToNextLevel,
                    onLevelSkip = gameViewModel::skipCurrentLevel,
                    onOpenLevelSelect = gameViewModel::openLevelSelection,
                    onSelectLevel = gameViewModel::onLevelSelected,
                    onPrevLevelPage = gameViewModel::previousLevelPage,
                    onNextLevelPage = gameViewModel::nextLevelPage,
                    onToggleSound = gameViewModel::toggleSound,
                    onToggleVibration = gameViewModel::toggleVibration
                )
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            configureImmersiveSystemBars()
        }
    }

    override fun onPause() {
        super.onPause()
        val state = gameViewModel.uiState.value
        if (state.screen == ScreenRoute.Gameplay &&
            !state.isPaused &&
            state.phase != GamePhase.LevelCompleted &&
            state.phase != GamePhase.GameOver
        ) {
            gameViewModel.pauseGame()
        }
    }

    private fun configureImmersiveSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun BallMasterAppHost(
    uiState: GameUiState,
    onSelectSlot: (Int) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartLevel: () -> Unit,
    onRetryResult: () -> Unit,
    onContinueNext: () -> Unit,
    onLevelSkip: () -> Unit,
    onOpenLevelSelect: () -> Unit,
    onSelectLevel: (Int) -> Unit,
    onPrevLevelPage: () -> Unit,
    onNextLevelPage: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState.screen) {
            ScreenRoute.Splash -> {
                SplashScreen()
            }

            ScreenRoute.Gameplay -> {
                GameplayScreen(
                    uiState = uiState,
                    onSelectSlot = onSelectSlot,
                    onPauseClick = onPauseClick
                )

                // Screen 04 — Level Completed Overlay
                AnimatedVisibility(
                    visible = uiState.phase == GamePhase.LevelCompleted,
                    enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
                    exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
                ) {
                    LevelCompletedOverlay(
                        earnedStars = uiState.lastRoundStarsEarned,
                        coinsAwarded = uiState.lastRoundCoinsAwarded,
                        alreadyClaimed = uiState.lastRoundAlreadyClaimed,
                        isFinalLevel = uiState.currentLevel >= uiState.totalLevels,
                        onRetry = onRetryResult,
                        onContinue = onContinueNext
                    )
                }

                // Screen 06 — Game Over Overlay
                AnimatedVisibility(
                    visible = uiState.phase == GamePhase.GameOver,
                    enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(220)),
                    exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
                ) {
                    GameOverOverlay(
                        canSkipLevel = uiState.currentLevel < uiState.totalLevels,
                        onRetry = onRetryResult,
                        onLevelSkip = onLevelSkip
                    )
                }

                // Screen 05 — Game Paused Overlay
                AnimatedVisibility(
                    visible = uiState.isPaused,
                    enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.94f, animationSpec = tween(180)),
                    exit = fadeOut(tween(140)) + scaleOut(targetScale = 0.96f, animationSpec = tween(140))
                ) {
                    GamePausedOverlay(
                        soundEnabled = uiState.soundEnabled,
                        vibrationEnabled = uiState.vibrationEnabled,
                        onResume = onResumeClick,
                        onRestartLevel = onRestartLevel,
                        onLevelSelection = onOpenLevelSelect,
                        onToggleSound = onToggleSound,
                        onToggleVibration = onToggleVibration
                    )
                }
            }

            ScreenRoute.LevelSelect -> {
                LevelSelectScreen(
                    uiState = uiState,
                    onSelectLevel = onSelectLevel,
                    onPrevPage = onPrevLevelPage,
                    onNextPage = onNextLevelPage
                )
            }
        }
    }
}
