package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
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
import com.example.ui.graphics.WoodenPillButton
import com.example.ui.graphics.drawArchedCartoonText
import com.example.ui.graphics.drawFacetedGoldenStar
import com.example.ui.graphics.drawGoldCoin
import com.example.ui.graphics.drawLaurelBranch
import com.example.ui.graphics.drawLeafCluster
import com.example.ui.graphics.drawMetallicRivet
import com.example.ui.graphics.drawOutlinedCartoonText
import com.example.ui.graphics.drawWoodenPlankRect
import com.example.ui.theme.GamePalette
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class ConfettiPiece(
    val xFrac: Float,
    val startYFrac: Float,
    val speed: Float,
    val phaseOffset: Float,
    val color: Color,
    val scale: Float
)

/**
 * SCREEN 04 — LEVEL COMPLETED POPUP (Landscape)
 * Confetti and animated stars are isolated in leaf composables so the static wooden board,
 * reward plaque, and buttons never recompose on animation frames.
 */
@Composable
fun LevelCompletedOverlay(
    earnedStars: Int,
    coinsAwarded: Int,
    alreadyClaimed: Boolean,
    isFinalLevel: Boolean,
    onRetry: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }

    val titleText = stringResource(R.string.level_completed_title)
    val rewardText = if (!alreadyClaimed && coinsAwarded > 0) {
        stringResource(R.string.reward_coins_format, coinsAwarded)
    } else {
        stringResource(R.string.reward_already_claimed)
    }
    val retryText = stringResource(R.string.action_retry)
    val continueText = stringResource(R.string.action_continue)

    Box(modifier = modifier.fillMaxSize().testTag("level_completed_overlay")) {
        JungleBackgroundCanvas(mode = JungleSceneMode.JungleDirtPath)

        // Isolated animated confetti layer
        VictoryConfettiLayer()

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.Center
        ) {
            val boardW = (maxWidth * 0.76f).coerceIn(380.dp, 620.dp)
            val boardH = (maxHeight * 0.64f).coerceIn(205.dp, 290.dp)
            val topClusterH = (maxHeight * 0.34f).coerceIn(105.dp, 155.dp)

            Box(
                modifier = Modifier
                    .width(boardW)
                    .height(boardH + topClusterH * 0.68f),
                contentAlignment = Alignment.BottomCenter
            ) {
                WoodenBoardPanel(
                    modifier = Modifier
                        .width(boardW)
                        .height(boardH)
                        .align(Alignment.BottomCenter),
                    plankCount = 6,
                    showTopCornerLeaves = false,
                    showBottomLeaves = true
                ) {
                    val rewardW = (boardW * 0.56f).coerceIn(220.dp, 340.dp)
                    val rewardH = (boardH * 0.24f).coerceIn(48.dp, 64.dp)
                    Canvas(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = boardH * 0.14f)
                            .width(rewardW)
                            .height(rewardH)
                            .testTag("reward_coins_plaque")
                    ) {
                        val padX = size.width * 0.06f
                        val padY = size.height * 0.08f
                        val rect = Rect(padX, padY, size.width - padX, size.height - padY)
                        drawLeafCluster(rect.left + 8f, rect.center.y, rect.height * 0.45f, 180f, 3)
                        drawLeafCluster(rect.right - 8f, rect.center.y, rect.height * 0.45f, 0f, 3)

                        drawWoodenPlankRect(
                            rect = rect,
                            cornerRadius = rect.height * 0.24f,
                            plankCount = 2,
                            goldenTrim = false,
                            drawRivets = true,
                            rivetInsetFraction = 0.055f
                        )
                        drawGoldCoin(
                            cx = rect.left + rect.height * 0.68f,
                            cy = rect.center.y,
                            radius = rect.height * 0.42f,
                            showDollar = true
                        )
                        val textSize = rect.height * (if (!alreadyClaimed && coinsAwarded > 0) 0.50f else 0.38f)
                        drawOutlinedCartoonText(
                            text = rewardText,
                            cx = rect.left + rect.width * 0.60f,
                            baselineY = rect.center.y + textSize * 0.34f,
                            textSizePx = textSize,
                            typeface = typeface,
                            topColor = GamePalette.SpecularGold,
                            bottomColor = GamePalette.BrightGold,
                            outlineWidthPx = textSize * 0.18f
                        )
                    }

                    val btnHeight = (boardH * 0.27f).coerceIn(50.dp, 68.dp)
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 26.dp, vertical = 18.dp)
                            .width(boardW - 52.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WoodenPillButton(
                            text = retryText,
                            onClick = onRetry,
                            modifier = Modifier
                                .weight(1f)
                                .height(btnHeight),
                            testTagName = "victory_retry_button",
                            icon = GameButtonIcon.Retry,
                            showSideLeaves = true,
                            textScale = 0.46f
                        )
                        WoodenPillButton(
                            text = continueText,
                            onClick = onContinue,
                            modifier = Modifier
                                .weight(1f)
                                .height(btnHeight),
                            testTagName = "victory_continue_button",
                            icon = GameButtonIcon.DoubleChevron,
                            showSideLeaves = true,
                            textScale = 0.44f
                        )
                    }
                }

                // Isolated animated stars + sunburst + arched banner header
                VictoryHeaderCluster(
                    titleText = titleText,
                    earnedStars = earnedStars,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(boardW * 0.96f)
                        .height(topClusterH * 1.18f)
                )
            }
        }
    }
}

@Composable
private fun VictoryConfettiLayer() {
    val infinite = rememberInfiniteTransition(label = "victory_confetti")
    val confettiProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "confetti_fall"
    )
    val confettiPieces = remember {
        listOf(
            ConfettiPiece(0.06f, 0.10f, 1.0f, 0.1f, GamePalette.ConfettiPink, 1.05f),
            ConfettiPiece(0.09f, 0.35f, 1.1f, 1.2f, GamePalette.ConfettiGold, 0.95f),
            ConfettiPiece(0.05f, 0.58f, 0.9f, 2.3f, GamePalette.ConfettiBlue, 1.10f),
            ConfettiPiece(0.08f, 0.80f, 1.2f, 3.1f, GamePalette.ConfettiRed, 0.90f),
            ConfettiPiece(0.32f, 0.04f, 0.85f, 0.6f, GamePalette.ConfettiRed, 0.85f),
            ConfettiPiece(0.68f, 0.05f, 0.95f, 1.9f, GamePalette.ConfettiBlue, 0.88f),
            ConfettiPiece(0.91f, 0.14f, 1.05f, 0.4f, GamePalette.ConfettiGold, 1.12f),
            ConfettiPiece(0.95f, 0.36f, 1.15f, 2.0f, GamePalette.ConfettiRed, 0.96f),
            ConfettiPiece(0.92f, 0.56f, 0.92f, 1.1f, GamePalette.ConfettiPurple, 1.08f),
            ConfettiPiece(0.96f, 0.78f, 1.08f, 2.8f, GamePalette.ConfettiGold, 1.00f)
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        confettiPieces.forEach { piece ->
            val yFrac = (piece.startYFrac + confettiProgress * piece.speed) % 1.15f - 0.08f
            val sway = sin((confettiProgress * 2f * PI + piece.phaseOffset)).toFloat() * w * 0.022f
            val px = piece.xFrac * w + sway
            val py = yFrac * h
            val rot = (confettiProgress * 360f * piece.speed + piece.phaseOffset * 90f) % 360f
            val cw = minOf(w, h) * 0.048f * piece.scale
            val ch = cw * 0.44f

            withTransform({
                translate(px, py)
                rotate(rot, pivot = Offset.Zero)
            }) {
                val ribbon = Path().apply {
                    moveTo(-cw * 0.5f, -ch * 0.4f)
                    quadraticTo(0f, -ch * 0.9f, cw * 0.5f, -ch * 0.2f)
                    lineTo(cw * 0.45f, ch * 0.5f)
                    quadraticTo(0f, -ch * 0.1f, -cw * 0.5f, ch * 0.4f)
                    close()
                }
                drawPath(ribbon, piece.color)
            }
        }
    }
}

@Composable
private fun VictoryHeaderCluster(
    titleText: String,
    earnedStars: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }

    var star1Visible by remember { mutableStateOf(false) }
    var star2Visible by remember { mutableStateOf(false) }
    var star3Visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60L); star1Visible = true
        delay(120L); star2Visible = true
        delay(120L); star3Visible = true
    }

    val s1Scale by animateFloatAsState(
        targetValue = if (star1Visible) 1f else 0.15f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "star1_scale"
    )
    val s2Scale by animateFloatAsState(
        targetValue = if (star2Visible) 1f else 0.15f,
        animationSpec = spring(dampingRatio = 0.50f, stiffness = Spring.StiffnessMediumLow),
        label = "star2_scale"
    )
    val s3Scale by animateFloatAsState(
        targetValue = if (star3Visible) 1f else 0.15f,
        animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
        label = "star3_scale"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val starBaseY = h * 0.36f

        // Static radiant golden sunburst behind stars
        val rayCount = 12
        val rayLen = h * 0.70f
        withTransform({ translate(cx, starBaseY) }) {
            for (i in 0 until rayCount) {
                val a1 = (i * (360f / rayCount)) * (PI / 180.0)
                val a2 = a1 + (11.0 * PI / 180.0)
                val ray = Path().apply {
                    moveTo(0f, 0f)
                    lineTo((cos(a1) * rayLen).toFloat(), (sin(a1) * rayLen).toFloat())
                    lineTo((cos(a2) * rayLen).toFloat(), (sin(a2) * rayLen).toFloat())
                    close()
                }
                drawPath(
                    ray,
                    Brush.radialGradient(
                        colors = listOf(Color(0x66FFF49E), Color.Transparent),
                        center = Offset.Zero,
                        radius = rayLen
                    )
                )
            }
        }

        drawLeafCluster(cx - w * 0.34f, h * 0.54f, h * 0.42f, -135f, 4)
        drawLeafCluster(cx + w * 0.34f, h * 0.54f, h * 0.42f, -45f, 4)
        drawLaurelBranch(cx - w * 0.30f, h * 0.56f, h * 0.44f, isRight = false)
        drawLaurelBranch(cx + w * 0.30f, h * 0.56f, h * 0.44f, isRight = true)

        val sideStarR = h * 0.24f
        val centerStarR = h * 0.29f
        drawFacetedGoldenStar(
            cx = cx - w * 0.17f,
            cy = starBaseY + h * 0.04f,
            outerRadius = sideStarR * s1Scale,
            rotationDeg = -14f,
            isLit = earnedStars >= 1,
            glowAlpha = 0.7f
        )
        drawFacetedGoldenStar(
            cx = cx + w * 0.17f,
            cy = starBaseY + h * 0.04f,
            outerRadius = sideStarR * s3Scale,
            rotationDeg = 14f,
            isLit = earnedStars >= 3,
            glowAlpha = 0.7f
        )
        drawFacetedGoldenStar(
            cx = cx,
            cy = starBaseY - h * 0.05f,
            outerRadius = centerStarR * s2Scale,
            rotationDeg = 0f,
            isLit = earnedStars >= 2,
            glowAlpha = 0.9f
        )

        val bannerW = w * 0.86f
        val bannerH = h * 0.38f
        val bannerTopY = h * 0.53f
        val archRise = h * 0.11f

        val ribbonPath = Path().apply {
            moveTo(cx - bannerW * 0.5f, bannerTopY + archRise)
            quadraticTo(cx, bannerTopY - archRise * 0.8f, cx + bannerW * 0.5f, bannerTopY + archRise)
            lineTo(cx + bannerW * 0.47f, bannerTopY + bannerH + archRise)
            quadraticTo(cx, bannerTopY + bannerH - archRise * 0.8f, cx - bannerW * 0.47f, bannerTopY + bannerH + archRise)
            close()
        }
        withTransform({ translate(0f, 6f) }) {
            drawPath(ribbonPath, Color(0x992A0B02))
        }
        drawPath(
            path = ribbonPath,
            brush = Brush.verticalGradient(
                colors = listOf(GamePalette.GoldenWoodHighlight, GamePalette.OrangeWood, GamePalette.MainWoodenBrown, GamePalette.DeepWoodBrown),
                startY = bannerTopY,
                endY = bannerTopY + bannerH + archRise
            )
        )
        drawPath(ribbonPath, GamePalette.BrightGold, style = Stroke(width = 7f, join = StrokeJoin.Round))
        drawPath(ribbonPath, GamePalette.DarkWoodOutline, style = Stroke(width = 3.5f, join = StrokeJoin.Round))

        val rvR = (bannerH * 0.085f).coerceIn(3.5f, 6f)
        drawMetallicRivet(cx - bannerW * 0.44f, bannerTopY + archRise + bannerH * 0.24f, rvR)
        drawMetallicRivet(cx - bannerW * 0.42f, bannerTopY + archRise + bannerH * 0.74f, rvR)
        drawMetallicRivet(cx + bannerW * 0.44f, bannerTopY + archRise + bannerH * 0.24f, rvR)
        drawMetallicRivet(cx + bannerW * 0.42f, bannerTopY + archRise + bannerH * 0.74f, rvR)

        drawArchedCartoonText(
            text = titleText,
            cx = cx,
            centerY = bannerTopY + bannerH * 0.68f,
            arcWidth = bannerW * 0.84f,
            archLift = archRise * 1.45f,
            textSizePx = (bannerH * 0.66f).coerceAtMost(w * 0.082f),
            typeface = typeface
        )
    }
}
