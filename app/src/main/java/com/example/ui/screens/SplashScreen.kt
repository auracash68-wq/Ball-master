package com.example.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.ui.graphics.GameFontCache
import com.example.ui.graphics.JungleBackgroundCanvas
import com.example.ui.graphics.JungleSceneMode
import com.example.ui.graphics.drawOutlinedCartoonText
import com.example.ui.graphics.drawRoyalGoldenCrown
import com.example.ui.graphics.drawSparkleGlint
import com.example.ui.graphics.drawTerracottaClayCup
import com.example.ui.theme.GamePalette

/**
 * SCREEN 01 — SPLASH / LOADING SCREEN (Portrait)
 * Static artwork (background, crown, BALL MASTER title, clay pot, Loading text) is separated
 * from the lightweight rotating golden spinner so frames render in <2ms without SurfaceSyncGroup timeouts.
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val displayTypeface = remember(context) { GameFontCache.lilita(context) }
    val subtitleTypeface = remember(context) { GameFontCache.fredoka(context) }

    val ballText = stringResource(R.string.splash_title_ball)
    val masterText = stringResource(R.string.splash_title_master)
    val subtitleText = stringResource(R.string.splash_subtitle)
    val loadingText = stringResource(R.string.splash_loading)

    Box(modifier = modifier.fillMaxSize().testTag("splash_screen")) {
        // Layer 1: Cached full-bleed jungle backdrop
        JungleBackgroundCanvas(mode = JungleSceneMode.SplashPortrait)

        // Layer 2: Static title, crown, terracotta pot, and "Loading..." text (drawn once)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f

            val crownW = w * 0.36f
            val crownH = crownW * 0.62f
            drawRoyalGoldenCrown(
                cx = cx,
                cy = h * 0.088f,
                width = crownW,
                height = crownH
            )

            val ballFontSize = (w * 0.255f).coerceAtMost(h * 0.135f)
            val ballBaseline = h * 0.222f
            drawOutlinedCartoonText(
                text = ballText,
                cx = cx,
                baselineY = ballBaseline + ballFontSize * 0.06f,
                textSizePx = ballFontSize,
                typeface = displayTypeface,
                topColor = GamePalette.DeepGoldShadow,
                bottomColor = GamePalette.DarkWoodOutline,
                outlineColor = GamePalette.DeepestWoodOutline,
                outlineWidthPx = ballFontSize * 0.17f,
                skewX = -0.11f
            )
            drawOutlinedCartoonText(
                text = ballText,
                cx = cx,
                baselineY = ballBaseline,
                textSizePx = ballFontSize,
                typeface = displayTypeface,
                topColor = GamePalette.SpecularGold,
                bottomColor = GamePalette.OrangeWood,
                outlineColor = GamePalette.DarkWoodOutline,
                outlineWidthPx = ballFontSize * 0.14f,
                skewX = -0.11f
            )

            val masterFontSize = (w * 0.215f).coerceAtMost(h * 0.115f)
            val masterBaseline = ballBaseline + masterFontSize * 0.90f
            drawOutlinedCartoonText(
                text = masterText,
                cx = cx,
                baselineY = masterBaseline + masterFontSize * 0.06f,
                textSizePx = masterFontSize,
                typeface = displayTypeface,
                topColor = GamePalette.SilverOutline,
                bottomColor = Color(0xFF0E2140),
                outlineColor = Color(0xFF0E2140),
                outlineWidthPx = masterFontSize * 0.16f,
                skewX = -0.11f
            )
            drawOutlinedCartoonText(
                text = masterText,
                cx = cx,
                baselineY = masterBaseline,
                textSizePx = masterFontSize,
                typeface = displayTypeface,
                topColor = GamePalette.SilverTop,
                bottomColor = GamePalette.SilverBottom,
                outlineColor = GamePalette.SilverOutline,
                outlineWidthPx = masterFontSize * 0.13f,
                skewX = -0.11f
            )

            val swooshY = masterBaseline + masterFontSize * 0.14f
            val swooshHalfW = w * 0.34f
            val swooshPath = Path().apply {
                moveTo(cx - swooshHalfW, swooshY + h * 0.012f)
                quadraticTo(cx, swooshY - h * 0.018f, cx + swooshHalfW, swooshY + h * 0.012f)
                quadraticTo(cx, swooshY - h * 0.002f, cx - swooshHalfW, swooshY + h * 0.012f)
                close()
            }
            drawPath(
                path = swooshPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        GamePalette.OrangeWood,
                        GamePalette.BrightGold,
                        GamePalette.SpecularGold,
                        GamePalette.BrightGold,
                        GamePalette.OrangeWood
                    ),
                    startX = cx - swooshHalfW,
                    endX = cx + swooshHalfW
                )
            )
            drawSparkleGlint(cx, swooshY - h * 0.005f, w * 0.045f)

            val subSize = (w * 0.048f).coerceAtMost(h * 0.032f)
            drawOutlinedCartoonText(
                text = subtitleText,
                cx = cx,
                baselineY = swooshY + subSize * 1.55f,
                textSizePx = subSize,
                typeface = subtitleTypeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineColor = GamePalette.DarkTextOutline,
                outlineWidthPx = subSize * 0.22f,
                letterSpacing = 0.08f
            )

            val potWidth = (w * 0.43f).coerceAtMost(h * 0.26f)
            val potHeight = potWidth * 0.98f
            val potBottomY = h * 0.628f
            drawTerracottaClayCup(
                cx = cx,
                bottomY = potBottomY,
                cupWidth = potWidth,
                cupHeight = potHeight,
                showInternalBallGlow = true,
                glowPulse = 0.25f
            )

            val ringR = (w * 0.072f).coerceIn(24f, 44f)
            val ringCy = h * 0.752f
            val loadSize = (w * 0.056f).coerceAtMost(h * 0.036f)
            drawOutlinedCartoonText(
                text = loadingText,
                cx = cx,
                baselineY = ringCy + ringR + loadSize * 1.35f,
                textSizePx = loadSize,
                typeface = subtitleTypeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineColor = GamePalette.DarkTextOutline,
                outlineWidthPx = loadSize * 0.22f,
                align = Paint.Align.CENTER
            )
        }

        // Layer 3: Isolated lightweight rotating golden loading ring
        SplashGoldenSpinnerOverlay()
    }
}

@Composable
private fun SplashGoldenSpinnerOverlay() {
    val infinite = rememberInfiniteTransition(label = "splash_spinner")
    val spinnerAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val ringR = (w * 0.072f).coerceIn(24f, 44f)
        val ringCy = h * 0.752f
        val ringStroke = ringR * 0.22f

        drawCircle(
            color = Color(0x661C0E04),
            radius = ringR + ringStroke * 0.65f,
            center = Offset(cx, ringCy)
        )
        drawCircle(
            color = GamePalette.DeepWoodBrown.copy(alpha = 0.85f),
            radius = ringR,
            center = Offset(cx, ringCy),
            style = Stroke(width = ringStroke)
        )
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    GamePalette.DeepGoldShadow,
                    GamePalette.BrightGold,
                    GamePalette.SpecularGold,
                    Color.White
                ),
                center = Offset(cx, ringCy)
            ),
            startAngle = spinnerAngle - 90f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(cx - ringR, ringCy - ringR),
            size = Size(ringR * 2f, ringR * 2f),
            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )
    }
}
