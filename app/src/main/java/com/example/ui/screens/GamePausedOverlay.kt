package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.graphics.GameButtonIcon
import com.example.ui.graphics.JungleBackgroundCanvas
import com.example.ui.graphics.JungleSceneMode
import com.example.ui.graphics.WoodenBoardPanel
import com.example.ui.graphics.WoodenCircleActionButton
import com.example.ui.graphics.WoodenHeaderSign
import com.example.ui.graphics.WoodenPillButton
import com.example.ui.graphics.WoodenSettingToggleRow
import com.example.ui.graphics.drawGoldenCloseX

/**
 * SCREEN 05 — GAME PAUSED POPUP (Landscape)
 * Exact code-driven recreation of the pause overlay:
 * Large wooden board with vertical divider, overlapping "Game Paused" header,
 * top-right circular golden "X" close button, left column with green "Resume" +
 * orange-brown "Restart Level" & "Level Selection", and right column with
 * wooden "Sound" & "Vibration" toggle switches.
 */
@Composable
fun GamePausedOverlay(
    soundEnabled: Boolean,
    vibrationEnabled: Boolean,
    onResume: () -> Unit,
    onRestartLevel: () -> Unit,
    onLevelSelection: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText = stringResource(R.string.game_paused_title)
    val resumeText = stringResource(R.string.action_resume)
    val restartText = stringResource(R.string.action_restart_level)
    val levelSelectText = stringResource(R.string.action_level_selection)
    val soundText = stringResource(R.string.setting_sound)
    val vibrationText = stringResource(R.string.setting_vibration)
    val cdClose = stringResource(R.string.cd_close_pause)
    val cdSound = stringResource(R.string.cd_toggle_sound)
    val cdVibration = stringResource(R.string.cd_toggle_vibration)

    Box(modifier = modifier.fillMaxSize().testTag("game_paused_overlay")) {
        JungleBackgroundCanvas(mode = JungleSceneMode.JungleDirtPath)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            val boardW = (maxWidth * 0.82f).coerceIn(400.dp, 670.dp)
            val boardH = (maxHeight * 0.74f).coerceIn(230.dp, 345.dp)
            val headerW = (boardW * 0.56f).coerceIn(220.dp, 360.dp)
            val headerH = (boardH * 0.22f).coerceIn(50.dp, 70.dp)
            val closeSize = (boardH * 0.21f).coerceIn(48.dp, 66.dp)

            Box(
                modifier = Modifier
                    .width(boardW + closeSize * 0.35f)
                    .height(boardH + headerH * 0.45f),
                contentAlignment = Alignment.Center
            ) {
                WoodenBoardPanel(
                    modifier = Modifier
                        .width(boardW)
                        .height(boardH)
                        .align(Alignment.BottomCenter),
                    plankCount = 7,
                    showTopCornerLeaves = false,
                    showBottomLeaves = true,
                    showVerticalDivider = true,
                    dividerXFraction = 0.545f
                ) {
                    val leftBtnHeight = (boardH * 0.225f).coerceIn(48.dp, 64.dp)
                    val toggleRowHeight = (boardH * 0.235f).coerceIn(48.dp, 66.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 26.dp,
                                end = 24.dp,
                                top = headerH * 0.65f,
                                bottom = 20.dp
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Column: Resume (Green), Restart Level, Level Selection
                        Column(
                            modifier = Modifier
                                .weight(0.545f)
                                .fillMaxHeight()
                                .padding(end = 16.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WoodenPillButton(
                                text = resumeText,
                                onClick = onResume,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(leftBtnHeight),
                                testTagName = "pause_resume_button",
                                isGreen = true,
                                icon = GameButtonIcon.Play,
                                showSideLeaves = true,
                                textScale = 0.45f
                            )
                            WoodenPillButton(
                                text = restartText,
                                onClick = onRestartLevel,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(leftBtnHeight),
                                testTagName = "pause_restart_button",
                                isGreen = false,
                                icon = GameButtonIcon.Retry,
                                showSideLeaves = true,
                                textScale = 0.36f
                            )
                            WoodenPillButton(
                                text = levelSelectText,
                                onClick = onLevelSelection,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(leftBtnHeight),
                                testTagName = "pause_level_select_button",
                                isGreen = false,
                                icon = GameButtonIcon.BackArrow,
                                showSideLeaves = true,
                                textScale = 0.34f
                            )
                        }

                        // Right Column: Sound Toggle & Vibration Toggle
                        Column(
                            modifier = Modifier
                                .weight(0.455f)
                                .fillMaxHeight()
                                .padding(start = 14.dp, bottom = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WoodenSettingToggleRow(
                                label = soundText,
                                checked = soundEnabled,
                                onCheckedChange = onToggleSound,
                                isSoundIcon = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(toggleRowHeight),
                                testTagName = "pause_sound_toggle",
                                contentDesc = cdSound
                            )
                            WoodenSettingToggleRow(
                                label = vibrationText,
                                checked = vibrationEnabled,
                                onCheckedChange = onToggleVibration,
                                isSoundIcon = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(toggleRowHeight),
                                testTagName = "pause_vibration_toggle",
                                contentDesc = cdVibration
                            )
                        }
                    }
                }

                // Top-Center Overlapping "Game Paused" Header Sign
                WoodenHeaderSign(
                    title = titleText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(headerW)
                        .height(headerH),
                    isGoldenText = false,
                    showSideLeaves = true
                )

                // Top-Right Overlapping Circular Golden "X" Close Button
                WoodenCircleActionButton(
                    onClick = onResume,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-2).dp, y = headerH * 0.12f),
                    sizeDp = closeSize,
                    testTagName = "pause_close_button",
                    contentDesc = cdClose,
                    showLeaves = true
                ) { cx, cy, r ->
                    drawGoldenCloseX(cx, cy, r * 1.32f)
                }
            }
        }
    }
}
