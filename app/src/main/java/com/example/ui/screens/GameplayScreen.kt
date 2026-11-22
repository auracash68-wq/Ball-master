package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.CupVisualState
import com.example.game.GameUiState
import com.example.ui.graphics.ClayCupSpriteCache
import com.example.ui.graphics.GameFontCache
import com.example.ui.graphics.JungleBackgroundCanvas
import com.example.ui.graphics.JungleSceneMode
import com.example.ui.graphics.WoodenPillButton
import com.example.ui.graphics.drawFacetedGoldenStar
import com.example.ui.graphics.drawGoldenBall
import com.example.ui.graphics.drawLeafCluster
import com.example.ui.graphics.drawLevelHudText
import com.example.ui.graphics.drawOutlinedCartoonText
import com.example.ui.graphics.drawPauseIcon
import com.example.ui.graphics.drawQuestionBannerText
import com.example.ui.graphics.drawWoodenPlankRect
import com.example.ui.theme.GamePalette
import kotlin.math.roundToInt

/**
 * SCREEN 02 — MAIN GAMEPLAY (Landscape)
 * Static cached jungle backdrop + isolated 60fps cached-sprite cup stage + static HUDs & buttons.
 */
@Composable
fun GameplayScreen(
    uiState: GameUiState,
    onSelectSlot: (Int) -> Unit,
    onPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayTypeface = remember(context) { GameFontCache.lilita(context) }

    val gamePassLabel = stringResource(R.string.hud_game_pass)
    val cdPause = stringResource(R.string.cd_pause_game)
    val cdChoice1 = stringResource(R.string.cd_answer_button, 1)
    val cdChoice2 = stringResource(R.string.cd_answer_button, 2)
    val cdChoice3 = stringResource(R.string.cd_answer_button, 3)

    Box(modifier = modifier.fillMaxSize().testTag("gameplay_screen")) {
        // 1. Cached full-bleed illustrated jungle background with mossy stone platform (<1ms blit)
        JungleBackgroundCanvas(mode = JungleSceneMode.GameplayStonePlatform)

        // 2. Three Terracotta Clay Cups & Golden Ball on the Stone Platform (cached sprites)
        CupsAndBallStage(
            cups = uiState.cups,
            ballCupId = uiState.ballCupId,
            canChooseCup = uiState.canChooseCup,
            onSelectSlot = onSelectSlot
        )

        // 3. Safe-area HUD + Bottom Question Banner & Answer Buttons
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxW = maxWidth
            val maxH = maxHeight
            val hudHeight = (maxH * 0.135f).coerceIn(48.dp, 64.dp)
            val leftHudWidth = (maxW * 0.265f).coerceIn(170.dp, 240.dp)
            val rightHudWidth = (maxW * 0.345f).coerceIn(220.dp, 315.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-Left Wooden Level Indicator
                Canvas(
                    modifier = Modifier
                        .width(leftHudWidth)
                        .height(hudHeight)
                        .testTag("hud_level_badge")
                ) {
                    val padX = size.width * 0.045f
                    val padY = size.height * 0.08f
                    val rect = Rect(padX, padY, size.width - padX, size.height - padY)
                    val cornerR = rect.height * 0.45f

                    drawLeafCluster(rect.left + rect.height * 0.16f, rect.top + rect.height * 0.22f, rect.height * 0.48f, -155f, 3)
                    drawLeafCluster(rect.right - rect.height * 0.16f, rect.top + rect.height * 0.22f, rect.height * 0.48f, -25f, 3)

                    drawWoodenPlankRect(
                        rect = rect,
                        cornerRadius = cornerR,
                        plankCount = 2,
                        goldenTrim = true,
                        drawRivets = true,
                        rivetInsetFraction = 0.07f
                    )

                    drawLevelHudText(
                        levelNumber = uiState.currentLevel,
                        totalLevels = uiState.totalLevels,
                        cx = rect.center.x,
                        cy = rect.center.y,
                        textSizePx = rect.height * 0.46f,
                        typeface = displayTypeface
                    )
                }

                // Top-Right Wooden Game Pass + Pause + 3 Golden Stars Container
                Canvas(
                    modifier = Modifier
                        .width(rightHudWidth)
                        .height(hudHeight)
                        .testTag("pause_button")
                        .semantics { contentDescription = cdPause }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onPauseClick
                        )
                ) {
                    val padX = size.width * 0.035f
                    val padY = size.height * 0.08f
                    val rect = Rect(padX, padY, size.width - padX, size.height - padY)
                    val cornerR = rect.height * 0.45f

                    drawLeafCluster(rect.right - rect.height * 0.16f, rect.top + rect.height * 0.22f, rect.height * 0.46f, -35f, 3)

                    drawWoodenPlankRect(
                        rect = rect,
                        cornerRadius = cornerR,
                        plankCount = 2,
                        goldenTrim = true,
                        drawRivets = true,
                        rivetInsetFraction = 0.045f
                    )

                    val pauseCx = rect.left + rect.width * 0.135f
                    drawPauseIcon(pauseCx, rect.center.y, rect.height * 0.56f)

                    val labelSize = rect.height * 0.34f
                    drawOutlinedCartoonText(
                        text = gamePassLabel,
                        cx = rect.left + rect.width * 0.40f,
                        baselineY = rect.center.y + labelSize * 0.34f,
                        textSizePx = labelSize,
                        typeface = displayTypeface,
                        topColor = GamePalette.PureWhite,
                        bottomColor = GamePalette.TextWhite,
                        outlineWidthPx = labelSize * 0.18f
                    )

                    val starR = rect.height * 0.27f
                    val starStartX = rect.left + rect.width * 0.675f
                    val starStep = rect.width * 0.105f
                    for (i in 0 until 3) {
                        drawFacetedGoldenStar(
                            cx = starStartX + i * starStep,
                            cy = rect.center.y,
                            outerRadius = starR,
                            isLit = true,
                            glowAlpha = 0.25f
                        )
                    }
                }
            }

            // Bottom Gameplay Controls: Question Banner + 3 Answer Buttons (1, 2, 3)
            val questionWidth = (maxW * 0.62f).coerceIn(320.dp, 540.dp)
            val questionHeight = (maxH * 0.135f).coerceIn(48.dp, 64.dp)
            val answerRowWidth = (maxW * 0.56f).coerceIn(290.dp, 490.dp)
            val answerBtnHeight = (maxH * 0.145f).coerceIn(50.dp, 68.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(bottom = 8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .width(questionWidth)
                            .height(questionHeight)
                            .testTag("question_banner")
                    ) {
                        val padX = size.width * 0.055f
                        val padY = size.height * 0.06f
                        val rect = Rect(padX, padY, size.width - padX, size.height - padY)
                        val cornerR = rect.height * 0.38f

                        drawLeafCluster(rect.left + rect.height * 0.15f, rect.center.y, rect.height * 0.55f, 180f, 3)
                        drawLeafCluster(rect.right - rect.height * 0.15f, rect.center.y, rect.height * 0.55f, 0f, 3)

                        drawWoodenPlankRect(
                            rect = rect,
                            cornerRadius = cornerR,
                            plankCount = 2,
                            goldenTrim = true,
                            drawRivets = true,
                            rivetInsetFraction = 0.05f
                        )

                        drawQuestionBannerText(
                            cx = rect.center.x,
                            cy = rect.center.y,
                            textSizePx = rect.height * 0.50f,
                            typeface = displayTypeface
                        )
                    }

                    Row(
                        modifier = Modifier.width(answerRowWidth),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val descriptions = listOf(cdChoice1, cdChoice2, cdChoice3)
                        for (slot in 0..2) {
                            WoodenPillButton(
                                text = (slot + 1).toString(),
                                onClick = { onSelectSlot(slot) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(answerBtnHeight),
                                testTagName = "answer_button_${slot + 1}",
                                contentDesc = descriptions[slot],
                                showSideLeaves = true,
                                enabled = uiState.canChooseCup,
                                textScale = 0.62f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CupsAndBallStage(
    cups: List<CupVisualState>,
    ballCupId: Int,
    canChooseCup: Boolean,
    onSelectSlot: (Int) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(canChooseCup) {
                detectTapGestures { tapOffset ->
                    if (!canChooseCup) return@detectTapGestures
                    val w = size.width.toFloat()
                    val h = size.height.toFloat()
                    val cupZoneTop = h * 0.22f
                    val cupZoneBottom = h * 0.66f
                    if (tapOffset.y in cupZoneTop..cupZoneBottom) {
                        val slot = when {
                            tapOffset.x < w * 0.37f -> 0
                            tapOffset.x > w * 0.63f -> 2
                            else -> 1
                        }
                        onSelectSlot(slot)
                    }
                }
            }
    ) {
        val w = size.width
        val h = size.height
        val centerX = w * 0.5f
        val slotSpacing = (w * 0.268f).coerceAtMost(h * 0.52f)
        val baseCupW = (slotSpacing * 0.68f).coerceAtMost(h * 0.32f)
        val baseCupH = baseCupW * 0.96f
        val platformRestY = h * 0.592f

        val cupSprite = ClayCupSpriteCache.getCupBitmap(baseCupW.roundToInt())
        val padRatio = 0.14f / (1f + 0.28f)

        val sortedCups = cups.sortedBy { it.zDepth }
        sortedCups.forEach { cup ->
            val cupCx = centerX + cup.xNorm * slotSpacing
            val depthScale = 1f + cup.zDepth * 0.085f
            val depthYOffset = cup.zDepth * (h * 0.048f)
            val liftPixels = cup.lift * (baseCupH * 0.68f)

            val groundY = platformRestY + depthYOffset
            val drawnCupW = baseCupW * depthScale
            val drawnCupH = baseCupH * depthScale

            // Ground contact shadow on the stone platform
            val shadowScale = (1f - cup.lift * 0.22f).coerceIn(0.65f, 1.05f)
            val shadowAlpha = (0.55f - cup.lift * 0.26f).coerceIn(0.20f, 0.60f)
            drawOval(
                color = Color(0xFF2B1003).copy(alpha = shadowAlpha),
                topLeft = Offset(
                    cupCx - drawnCupW * 0.53f * shadowScale,
                    groundY - drawnCupH * 0.06f
                ),
                size = Size(drawnCupW * 1.06f * shadowScale, drawnCupH * 0.16f * shadowScale)
            )

            // Draw the Golden Ball beneath its cup when lifted
            if (cup.cupId == ballCupId && cup.lift > 0.03f) {
                val ballRadius = drawnCupW * 0.23f
                drawGoldenBall(
                    cx = cupCx,
                    cy = groundY - ballRadius * 0.92f,
                    radius = ballRadius,
                    shinePhase = 0.25f
                )
            }

            // Blit cached terracotta clay cup sprite (<0.2ms per cup)
            val totalDrawW = drawnCupW * 1.28f
            val totalDrawH = drawnCupH * 1.28f
            val bottomPad = totalDrawH * padRatio
            val dstLeft = (cupCx - totalDrawW * 0.5f).roundToInt()
            val dstTop = (groundY - liftPixels - totalDrawH + bottomPad).roundToInt()

            drawImage(
                image = cupSprite,
                srcOffset = IntOffset.Zero,
                srcSize = IntSize(cupSprite.width, cupSprite.height),
                dstOffset = IntOffset(dstLeft, dstTop),
                dstSize = IntSize(totalDrawW.roundToInt().coerceAtLeast(1), totalDrawH.roundToInt().coerceAtLeast(1)),
                filterQuality = FilterQuality.Medium
            )
        }
    }
}
