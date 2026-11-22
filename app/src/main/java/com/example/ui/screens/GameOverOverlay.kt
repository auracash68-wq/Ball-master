package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.graphics.GameButtonIcon
import com.example.ui.graphics.GameFontCache
import com.example.ui.graphics.JungleBackgroundCanvas
import com.example.ui.graphics.JungleSceneMode
import com.example.ui.graphics.WoodenBoardPanel
import com.example.ui.graphics.WoodenHeaderSign
import com.example.ui.graphics.WoodenPillButton
import com.example.ui.graphics.drawOutlinedCartoonText
import com.example.ui.graphics.drawSadGoldenBallCharacter
import com.example.ui.theme.GamePalette

/**
 * SCREEN 06 — GAME OVER POPUP (Landscape)
 * The animated dizzy golden ball character is isolated inside a leaf composable so
 * the wooden board, header, and Retry/Level Skip buttons never recompose at 60fps.
 */
@Composable
fun GameOverOverlay(
    canSkipLevel: Boolean,
    onRetry: () -> Unit,
    onLevelSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val titleText = stringResource(R.string.game_over_title)
    val line1Text = stringResource(R.string.game_over_line_1)
    val line2Text = stringResource(R.string.game_over_line_2)
    val retryText = stringResource(R.string.action_retry)
    val skipText = stringResource(R.string.action_level_skip)

    Box(modifier = modifier.fillMaxSize().testTag("game_over_overlay")) {
        JungleBackgroundCanvas(mode = JungleSceneMode.JungleDirtPath)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            val boardW = (maxWidth * 0.78f).coerceIn(390.dp, 640.dp)
            val boardH = (maxHeight * 0.72f).coerceIn(220.dp, 330.dp)
            val headerW = (boardW * 0.74f).coerceIn(270.dp, 450.dp)
            val headerH = (boardH * 0.25f).coerceIn(54.dp, 76.dp)
            val btnHeight = (boardH * 0.24f).coerceIn(50.dp, 66.dp)

            Box(
                modifier = Modifier
                    .width(boardW)
                    .height(boardH + headerH * 0.45f),
                contentAlignment = Alignment.Center
            ) {
                WoodenBoardPanel(
                    modifier = Modifier
                        .width(boardW)
                        .height(boardH)
                        .align(Alignment.BottomCenter),
                    plankCount = 7,
                    showTopCornerLeaves = true,
                    showBottomLeaves = true
                ) {
                    GameOverCharacterAndMessage(
                        line1Text = line1Text,
                        line2Text = line2Text,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = headerH * 0.56f,
                                bottom = btnHeight * 0.92f
                            )
                    )

                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                            .width(boardW - 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WoodenPillButton(
                            text = retryText,
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .height(btnHeight),
                            testTagName = "gameover_retry_button",
                            icon = GameButtonIcon.Retry,
                            showSideLeaves = true,
                            textScale = 0.48f
                        )
                        WoodenPillButton(
                            text = skipText,
                            onClick = onLevelSkip,
                            modifier = Modifier
                                .weight(1f)
                                .height(btnHeight),
                            testTagName = "gameover_skip_button",
                            icon = GameButtonIcon.DoublePlay,
                            showSideLeaves = true,
                            enabled = canSkipLevel,
                            textScale = 0.39f
                        )
                    }
                }

                WoodenHeaderSign(
                    title = titleText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(headerW)
                        .height(headerH),
                    isGoldenText = true,
                    showSideLeaves = true
                )
            }
        }
    }
}

@Composable
private fun GameOverCharacterAndMessage(
    line1Text: String,
    line2Text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }

    val infinite = rememberInfiniteTransition(label = "gameover_dizzy")
    val dizzyPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dizzy_orbit"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val charRadius = minOf(w * 0.145f, h * 0.36f)
        drawSadGoldenBallCharacter(
            cx = w * 0.31f,
            cy = h * 0.58f,
            radius = charRadius,
            dizzyPhase = dizzyPhase
        )

        val textCx = w * 0.72f
        val textCy = h * 0.48f
        val lineSize = (minOf(w * 0.088f, h * 0.28f)).coerceAtLeast(24f)

        withTransform({
            translate(textCx, textCy)
            rotate(-4.5f, pivot = Offset.Zero)
        }) {
            drawOutlinedCartoonText(
                text = line1Text,
                cx = 0f,
                baselineY = -lineSize * 0.16f,
                textSizePx = lineSize,
                typeface = typeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineWidthPx = lineSize * 0.19f
            )
            drawOutlinedCartoonText(
                text = line2Text,
                cx = 0f,
                baselineY = lineSize * 0.84f,
                textSizePx = lineSize,
                typeface = typeface,
                topColor = GamePalette.SpecularGold,
                bottomColor = GamePalette.BrightGold,
                outlineWidthPx = lineSize * 0.19f
            )

            val swooshW = lineSize * 4.4f
            val swooshY = lineSize * 1.15f
            val swoosh = Path().apply {
                moveTo(-swooshW * 0.52f, swooshY + lineSize * 0.15f)
                quadraticTo(0f, swooshY - lineSize * 0.18f, swooshW * 0.52f, swooshY + lineSize * 0.08f)
            }
            drawPath(
                swoosh,
                Brush.horizontalGradient(
                    colors = listOf(
                        GamePalette.OrangeWood,
                        GamePalette.BrightGold,
                        GamePalette.SpecularGold,
                        GamePalette.OrangeWood
                    ),
                    startX = -swooshW * 0.52f,
                    endX = swooshW * 0.52f
                ),
                style = Stroke(width = (lineSize * 0.11f).coerceAtLeast(3f), cap = StrokeCap.Round)
            )
        }
    }
}
