package com.example.ui.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.ui.theme.GamePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the radiant 3D metallic Golden Ball hidden beneath the clay cups.
 */
fun DrawScope.drawGoldenBall(
    cx: Float,
    cy: Float,
    radius: Float,
    shinePhase: Float = 0f
) {
    // Ground contact shadow
    drawOval(
        color = Color(0x88381502),
        topLeft = Offset(cx - radius * 1.05f, cy + radius * 0.65f),
        size = Size(radius * 2.1f, radius * 0.52f)
    )

    // Warm golden ambient aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GamePalette.SpecularGold.copy(alpha = 0.65f),
                GamePalette.BrightGold.copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = Offset(cx, cy),
            radius = radius * 1.65f
        ),
        radius = radius * 1.65f,
        center = Offset(cx, cy)
    )

    // 3D golden sphere body
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFFFDE),
                GamePalette.SpecularGold,
                GamePalette.BrightGold,
                GamePalette.AmberGoldMid,
                GamePalette.DeepGoldShadow
            ),
            center = Offset(cx - radius * 0.30f, cy - radius * 0.32f),
            radius = radius * 1.45f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )

    // Warm lower rim bounce reflection
    drawArc(
        color = GamePalette.SpecularGold.copy(alpha = 0.55f),
        startAngle = 25f,
        sweepAngle = 125f,
        useCenter = false,
        topLeft = Offset(cx - radius * 0.78f, cy - radius * 0.78f),
        size = Size(radius * 1.56f, radius * 1.56f),
        style = Stroke(width = radius * 0.14f, cap = StrokeCap.Round)
    )

    // Crisp gloss oval highlight
    drawOval(
        color = Color(0xCCFFFFFF),
        topLeft = Offset(cx - radius * 0.52f, cy - radius * 0.60f),
        size = Size(radius * 0.44f, radius * 0.28f)
    )

    // Dark brown cartoon outline
    drawCircle(
        color = GamePalette.DarkWoodOutline,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = (radius * 0.09f).coerceAtLeast(2f))
    )

    // Animated sparkle glint
    val glintScale = 0.75f + 0.35f * sin(shinePhase * 2f * PI).toFloat()
    drawSparkleGlint(
        cx = cx - radius * 0.36f,
        cy = cy - radius * 0.42f,
        radius = radius * 0.34f * glintScale
    )
}

/**
 * Draws the signature inverted terracotta clay pot/cup from Screens 01 and 02,
 * including the rounded dome profile, flared bottom rim, flat top lip, cylindrical
 * ceramic highlights, subtle clay speckles, and cream-white wave + circular dot pattern.
 */
fun DrawScope.drawTerracottaClayCup(
    cx: Float,
    bottomY: Float,
    cupWidth: Float,
    cupHeight: Float,
    showInternalBallGlow: Boolean = false,
    glowPulse: Float = 0f,
    shadowScale: Float = 1f,
    shadowAlpha: Float = 0.55f
) {
    val halfW = cupWidth * 0.5f
    val topY = bottomY - cupHeight
    val topHalfW = cupWidth * 0.29f

    // 1. Soft dark contact shadow on stone surface
    drawOval(
        color = Color(0xFF2B1003).copy(alpha = shadowAlpha),
        topLeft = Offset(cx - halfW * 1.06f * shadowScale, bottomY - cupHeight * 0.06f),
        size = Size(cupWidth * 1.06f * shadowScale, cupHeight * 0.16f * shadowScale)
    )

    // 2. Main inverted terracotta dome body path
    val bodyPath = Path().apply {
        moveTo(cx - topHalfW, topY + cupHeight * 0.03f)
        // Smooth flat-ish rounded top edge
        quadraticTo(cx, topY - cupHeight * 0.02f, cx + topHalfW, topY + cupHeight * 0.03f)
        // Right rounded bell curve down to bottom rim
        cubicTo(
            cx + cupWidth * 0.45f, topY + cupHeight * 0.16f,
            cx + cupWidth * 0.50f, topY + cupHeight * 0.58f,
            cx + halfW * 0.98f, bottomY - cupHeight * 0.06f
        )
        // Flared bottom lip curve
        quadraticTo(cx, bottomY + cupHeight * 0.055f, cx - halfW * 0.98f, bottomY - cupHeight * 0.06f)
        // Left rounded bell curve up to top edge
        cubicTo(
            cx - cupWidth * 0.50f, topY + cupHeight * 0.58f,
            cx - cupWidth * 0.45f, topY + cupHeight * 0.16f,
            cx - topHalfW, topY + cupHeight * 0.03f
        )
        close()
    }

    clipPath(bodyPath) {
        // Horizontal 3D cylindrical terracotta lighting gradient
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0.00f to GamePalette.ClayDeep,
                    0.18f to GamePalette.ClayMid,
                    0.40f to GamePalette.ClayBright,
                    0.62f to GamePalette.ClayHighlight,
                    0.82f to GamePalette.ClayBright,
                    1.00f to GamePalette.ClayDeep
                ),
                startX = cx - halfW,
                endX = cx + halfW
            ),
            topLeft = Offset(cx - halfW, topY - cupHeight * 0.05f),
            size = Size(cupWidth, cupHeight * 1.12f)
        )

        // Vertical depth shading (warmer top shoulder, richer bottom shadow)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x33FFF0B8),
                    Color.Transparent,
                    Color(0x44631E02)
                ),
                startY = topY,
                endY = bottomY + cupHeight * 0.06f
            ),
            topLeft = Offset(cx - halfW, topY),
            size = Size(cupWidth, cupHeight * 1.08f)
        )

        // Warm glossy specular shoulder highlight band on right-center (matching reference)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x70FFE59E), Color(0x28FFBE66), Color.Transparent),
                center = Offset(cx + cupWidth * 0.14f, topY + cupHeight * 0.36f),
                radius = cupWidth * 0.32f
            ),
            topLeft = Offset(cx - cupWidth * 0.08f, topY + cupHeight * 0.12f),
            size = Size(cupWidth * 0.44f, cupHeight * 0.52f)
        )

        // Secondary subtle left rim backlight
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x40FFD984), Color.Transparent),
                center = Offset(cx - cupWidth * 0.30f, topY + cupHeight * 0.44f),
                radius = cupWidth * 0.22f
            ),
            topLeft = Offset(cx - cupWidth * 0.46f, topY + cupHeight * 0.22f),
            size = Size(cupWidth * 0.32f, cupHeight * 0.46f)
        )

        // Optional mystical glowing golden ball silhouette inside (Screen 01 Splash)
        if (showInternalBallGlow) {
            val ballR = cupWidth * 0.24f
            val ballCy = topY + cupHeight * 0.62f
            val pulseAlpha = 0.72f + 0.18f * sin(glowPulse * 2f * PI).toFloat()
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF8BD).copy(alpha = pulseAlpha),
                        GamePalette.BrightGold.copy(alpha = pulseAlpha * 0.75f),
                        GamePalette.OrangeWood.copy(alpha = pulseAlpha * 0.25f),
                        Color.Transparent
                    ),
                    center = Offset(cx, ballCy),
                    radius = ballR * 1.55f
                ),
                radius = ballR * 1.55f,
                center = Offset(cx, ballCy)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xCCFFF8C4),
                        GamePalette.BrightGold.copy(alpha = 0.68f),
                        GamePalette.AmberGoldMid.copy(alpha = 0.42f)
                    ),
                    center = Offset(cx - ballR * 0.2f, ballCy - ballR * 0.25f),
                    radius = ballR * 1.15f
                ),
                radius = ballR,
                center = Offset(cx, ballCy)
            )
        }

        // Cream-white sinusoidal decorative wave band & alternating circular dots
        val waveY = bottomY - cupHeight * 0.24f
        val waveAmp = cupHeight * 0.055f
        val waveLeft = cx - halfW * 0.98f
        val waveSpan = cupWidth * 0.98f
        val waveCycles = 3.5f
        val samples = 44

        val wavePath = Path().apply {
            for (i in 0..samples) {
                val frac = i / samples.toFloat()
                val wx = waveLeft + frac * waveSpan
                // Subtle upward curve at edges to simulate 3D cylindrical wrap
                val edgeLift = ((frac - 0.5f) * 2f).let { it * it } * (-cupHeight * 0.025f)
                val wy = waveY + sin(frac * waveCycles * 2f * PI).toFloat() * waveAmp + edgeLift
                if (i == 0) moveTo(wx, wy) else lineTo(wx, wy)
            }
        }
        drawPath(
            path = wavePath,
            color = GamePalette.ClayPatternCream.copy(alpha = 0.93f),
            style = Stroke(
                width = (cupHeight * 0.036f).coerceIn(2.6f, 6.5f),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Alternating cream-white circular ornaments in wave crests and troughs
        val dotCount = 7
        for (d in 0 until dotCount) {
            val frac = (d + 0.5f) / dotCount.toFloat()
            val dx = waveLeft + frac * waveSpan
            val isUpperDot = d % 2 == 1
            val edgeLift = ((frac - 0.5f) * 2f).let { it * it } * (-cupHeight * 0.025f)
            val dy = waveY + (if (isUpperDot) -waveAmp * 0.95f else waveAmp * 1.15f) + edgeLift
            val foreshorten = 1f - ((frac - 0.5f) * 2f).let { it * it } * 0.35f
            val dotR = cupHeight * 0.036f
            drawOval(
                color = GamePalette.ClayPatternCream.copy(alpha = 0.93f),
                topLeft = Offset(dx - dotR * foreshorten, dy - dotR),
                size = Size(dotR * 2f * foreshorten, dotR * 2f)
            )
        }
    }

    // 3. Top flat clay pot base rim oval
    val topRimH = cupHeight * 0.075f
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.ClayHighlight, GamePalette.ClayMid),
            startY = topY,
            endY = topY + topRimH
        ),
        topLeft = Offset(cx - topHalfW * 0.96f, topY - topRimH * 0.25f),
        size = Size(topHalfW * 1.92f, topRimH)
    )
    drawOval(
        color = GamePalette.ClayRimDark.copy(alpha = 0.6f),
        topLeft = Offset(cx - topHalfW * 0.96f, topY - topRimH * 0.25f),
        size = Size(topHalfW * 1.92f, topRimH),
        style = Stroke(width = 1.8f)
    )

    // 4. Flared bottom ceramic lip ridge
    val lipPath = Path().apply {
        moveTo(cx - halfW * 1.03f, bottomY - cupHeight * 0.055f)
        quadraticTo(cx, bottomY + cupHeight * 0.07f, cx + halfW * 1.03f, bottomY - cupHeight * 0.055f)
        quadraticTo(cx + halfW * 1.05f, bottomY + cupHeight * 0.01f, cx + halfW * 0.98f, bottomY + cupHeight * 0.025f)
        quadraticTo(cx, bottomY + cupHeight * 0.115f, cx - halfW * 0.98f, bottomY + cupHeight * 0.025f)
        quadraticTo(cx - halfW * 1.05f, bottomY + cupHeight * 0.01f, cx - halfW * 1.03f, bottomY - cupHeight * 0.055f)
        close()
    }
    drawPath(
        path = lipPath,
        brush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.0f to GamePalette.ClayDeep,
                0.28f to GamePalette.ClayBright,
                0.60f to GamePalette.ClayHighlight,
                0.85f to GamePalette.ClayBright,
                1.0f to GamePalette.ClayDeep
            ),
            startX = cx - halfW * 1.05f,
            endX = cx + halfW * 1.05f
        )
    )
    drawPath(
        path = lipPath,
        color = GamePalette.ClayRimDark,
        style = Stroke(width = (cupWidth * 0.022f).coerceIn(1.8f, 3.4f), join = StrokeJoin.Round)
    )

    // 5. Crisp terracotta body outer contour
    drawPath(
        path = bodyPath,
        color = GamePalette.ClayRimDark.copy(alpha = 0.85f),
        style = Stroke(width = (cupWidth * 0.02f).coerceIn(1.6f, 3.0f), join = StrokeJoin.Round)
    )
}

/**
 * Caches a high-detail terracotta clay cup sprite as an ImageBitmap so 60fps 3-cup shuffles
 * blit in <0.5ms without re-clipping or re-tessellating ceramic paths on every animation frame.
 */
object ClayCupSpriteCache {
    @Volatile
    private var cachedWidth: Int = 0

    @Volatile
    private var cachedBitmap: androidx.compose.ui.graphics.ImageBitmap? = null

    fun getCupBitmap(pixelWidth: Int): androidx.compose.ui.graphics.ImageBitmap {
        val targetW = pixelWidth.coerceIn(120, 420)
        val existing = cachedBitmap
        if (existing != null && kotlin.math.abs(cachedWidth - targetW) <= 12) {
            return existing
        }
        val pad = (targetW * 0.14f).toInt()
        val bmpW = targetW + pad * 2
        val cupH = (targetW * 0.96f).toInt()
        val bmpH = cupH + pad * 2
        val bmp = androidx.compose.ui.graphics.ImageBitmap(
            bmpW,
            bmpH,
            androidx.compose.ui.graphics.ImageBitmapConfig.Argb8888
        )
        val canvas = androidx.compose.ui.graphics.Canvas(bmp)
        val scope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()
        scope.draw(
            density = androidx.compose.ui.unit.Density(1f),
            layoutDirection = androidx.compose.ui.unit.LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(bmpW.toFloat(), bmpH.toFloat())
        ) {
            drawTerracottaClayCup(
                cx = bmpW * 0.5f,
                bottomY = bmpH - pad.toFloat(),
                cupWidth = targetW.toFloat(),
                cupHeight = cupH.toFloat(),
                showInternalBallGlow = false,
                shadowScale = 0f,
                shadowAlpha = 0f
            )
        }
        cachedWidth = targetW
        cachedBitmap = bmp
        return bmp
    }
}

/**
 * Draws the ornate 3D metallic Golden Royal Crown above the BALL MASTER title in Screen 01.
 */
fun DrawScope.drawRoyalGoldenCrown(
    cx: Float,
    cy: Float,
    width: Float,
    height: Float
) {
    val left = cx - width * 0.5f
    val right = cx + width * 0.5f
    val top = cy - height * 0.5f
    val bottom = cy + height * 0.5f
    val baseTopY = bottom - height * 0.24f

    // Crown main flared 5-point silhouette
    val crownPath = Path().apply {
        moveTo(left + width * 0.14f, baseTopY)
        lineTo(left + width * 0.02f, top + height * 0.24f)   // Far-left spike
        lineTo(left + width * 0.24f, top + height * 0.50f)
        lineTo(left + width * 0.28f, top + height * 0.16f)   // Mid-left spike
        lineTo(left + width * 0.40f, top + height * 0.44f)
        lineTo(cx, top + height * 0.05f)                     // Tall center spike
        lineTo(right - width * 0.40f, top + height * 0.44f)
        lineTo(right - width * 0.28f, top + height * 0.16f)  // Mid-right spike
        lineTo(right - width * 0.24f, top + height * 0.50f)
        lineTo(right - width * 0.02f, top + height * 0.24f)  // Far-right spike
        lineTo(right - width * 0.14f, baseTopY)
        quadraticTo(cx, bottom + height * 0.08f, left + width * 0.14f, baseTopY)
        close()
    }

    // Soft golden outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x66FFF49E), Color.Transparent),
            center = Offset(cx, cy),
            radius = width * 0.72f
        ),
        radius = width * 0.72f,
        center = Offset(cx, cy)
    )

    // Metallic gold fill
    drawPath(
        path = crownPath,
        brush = Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to GamePalette.DeepGoldShadow,
                0.22f to GamePalette.BrightGold,
                0.45f to GamePalette.SpecularGold,
                0.70f to GamePalette.BrightGold,
                1.00f to GamePalette.DeepGoldShadow
            ),
            start = Offset(left, top),
            end = Offset(right, bottom)
        )
    )

    // Curved bottom metallic band
    val bandPath = Path().apply {
        moveTo(left + width * 0.14f, baseTopY)
        quadraticTo(cx, baseTopY - height * 0.10f, right - width * 0.14f, baseTopY)
        lineTo(right - width * 0.16f, bottom - height * 0.04f)
        quadraticTo(cx, bottom + height * 0.07f, left + width * 0.16f, bottom - height * 0.04f)
        close()
    }
    drawPath(
        path = bandPath,
        brush = Brush.horizontalGradient(
            colorStops = arrayOf(
                0.0f to GamePalette.DeepGoldShadow,
                0.35f to GamePalette.SpecularGold,
                0.55f to Color.White,
                0.78f to GamePalette.BrightGold,
                1.0f to GamePalette.DarkWoodOutline
            ),
            startX = left + width * 0.14f,
            endX = right - width * 0.14f
        )
    )
    drawPath(bandPath, GamePalette.DarkWoodOutline, style = Stroke(width = 2.6f, join = StrokeJoin.Round))
    drawPath(crownPath, GamePalette.DarkWoodOutline, style = Stroke(width = 3.2f, join = StrokeJoin.Round))

    // Diamond jewels on crown face
    val jewels = listOf(
        Offset(cx, top + height * 0.44f) to (width * 0.055f),
        Offset(left + width * 0.28f, top + height * 0.56f) to (width * 0.036f),
        Offset(right - width * 0.28f, top + height * 0.56f) to (width * 0.036f)
    )
    jewels.forEach { (pt, r) ->
        val diamond = Path().apply {
            moveTo(pt.x, pt.y - r * 1.25f)
            lineTo(pt.x + r * 0.85f, pt.y)
            lineTo(pt.x, pt.y + r * 1.25f)
            lineTo(pt.x - r * 0.85f, pt.y)
            close()
        }
        drawPath(diamond, GamePalette.DarkWoodOutline)
        drawPath(diamond, GamePalette.SpecularGold, style = Stroke(1.6f))
    }

    // Golden finial spheres on the 5 crown tips
    val tips = listOf(
        Offset(left + width * 0.02f, top + height * 0.23f) to width * 0.042f,
        Offset(left + width * 0.28f, top + height * 0.15f) to width * 0.048f,
        Offset(cx, top + height * 0.03f) to width * 0.058f,
        Offset(right - width * 0.28f, top + height * 0.15f) to width * 0.048f,
        Offset(right - width * 0.02f, top + height * 0.23f) to width * 0.042f
    )
    tips.forEach { (pt, r) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, GamePalette.BrightGold, GamePalette.DeepGoldShadow),
                center = Offset(pt.x - r * 0.3f, pt.y - r * 0.3f),
                radius = r * 1.4f
            ),
            radius = r,
            center = pt
        )
        drawCircle(GamePalette.DarkWoodOutline, radius = r, center = pt, style = Stroke(2.2f))
    }
}

/**
 * Draws the iconic Screen 06 Sad Yellow Golden Ball Character peeking over the wooden ledge
 * with expressive sad puppy eyes, white cartoon gloved hands, and rotating dizzy stars/swirls.
 */
fun DrawScope.drawSadGoldenBallCharacter(
    cx: Float,
    cy: Float,
    radius: Float,
    dizzyPhase: Float
) {
    val ledgeY = cy + radius * 0.72f

    // 1. Dark recessed wooden pocket/hole where the ball sits
    drawOval(
        color = Color(0xCC2C0F02),
        topLeft = Offset(cx - radius * 1.38f, ledgeY - radius * 0.18f),
        size = Size(radius * 2.76f, radius * 0.36f)
    )

    // 2. Orbiting dizzy swirl loops behind/around head
    val swirlCy = cy - radius * 0.92f
    val orbitAngles = listOf(-8f, 4f, -3f)
    orbitAngles.forEachIndexed { idx, tilt ->
        withTransform({
            translate(cx, swirlCy + idx * radius * 0.08f)
            rotate(tilt, pivot = Offset.Zero)
        }) {
            val rx = radius * (1.05f + idx * 0.12f)
            val ry = radius * (0.26f + idx * 0.04f)
            drawOval(
                color = GamePalette.SpecularGold.copy(alpha = 0.85f - idx * 0.18f),
                topLeft = Offset(-rx, -ry),
                size = Size(rx * 2f, ry * 2f),
                style = Stroke(width = (radius * 0.045f).coerceAtLeast(2f))
            )
        }
    }

    // 3. Yellow golden ball head clipped slightly at bottom ledge
    val ballClip = Path().apply {
        addRect(Rect(cx - radius * 1.2f, cy - radius * 1.2f, cx + radius * 1.2f, ledgeY + radius * 0.06f))
    }
    clipPath(ballClip) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFFFCC),
                    GamePalette.SpecularGold,
                    GamePalette.BrightGold,
                    GamePalette.AmberGoldMid,
                    GamePalette.OrangeWood
                ),
                center = Offset(cx - radius * 0.28f, cy - radius * 0.35f),
                radius = radius * 1.45f
            ),
            radius = radius,
            center = Offset(cx, cy)
        )
        // Bright glossy top-left forehead shine
        drawOval(
            color = Color(0xB8FFFFFF),
            topLeft = Offset(cx - radius * 0.56f, cy - radius * 0.72f),
            size = Size(radius * 0.44f, radius * 0.25f)
        )
        drawCircle(
            color = GamePalette.DarkWoodOutline,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = (radius * 0.085f).coerceAtLeast(2.8f))
        )

        // Expressive sad eyes
        val eyeY = cy + radius * 0.08f
        val eyeSpacing = radius * 0.34f
        val eyeRx = radius * 0.21f
        val eyeRy = radius * 0.25f

        for (dir in listOf(-1f, 1f)) {
            val ex = cx + dir * eyeSpacing
            // White sclera
            drawOval(
                color = Color.White,
                topLeft = Offset(ex - eyeRx, eyeY - eyeRy),
                size = Size(eyeRx * 2f, eyeRy * 2f)
            )
            // Dark warm brown pupil/iris looking slightly inward & down
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF9C4710), Color(0xFF3E1403), Color(0xFF1A0701)),
                    center = Offset(ex - dir * eyeRx * 0.1f, eyeY + eyeRy * 0.12f),
                    radius = eyeRy * 0.95f
                ),
                topLeft = Offset(ex - eyeRx * 0.72f, eyeY - eyeRy * 0.62f),
                size = Size(eyeRx * 1.44f, eyeRy * 1.45f)
            )
            // Two big glossy watery highlights
            drawCircle(Color.White, radius = eyeRx * 0.30f, center = Offset(ex - eyeRx * 0.28f, eyeY - eyeRy * 0.22f))
            drawCircle(Color.White.copy(alpha = 0.85f), radius = eyeRx * 0.15f, center = Offset(ex + eyeRx * 0.24f, eyeY + eyeRy * 0.24f))

            // Droopy sad upper eyelid line & eye outline
            drawOval(
                color = GamePalette.DarkWoodOutline,
                topLeft = Offset(ex - eyeRx, eyeY - eyeRy),
                size = Size(eyeRx * 2f, eyeRy * 2f),
                style = Stroke(width = (radius * 0.055f).coerceAtLeast(2f))
            )

            // Worried tilted eyebrows (higher toward center, sloping down outward)
            val browPath = Path().apply {
                moveTo(ex - dir * eyeRx * 0.95f, eyeY - eyeRy * 1.38f)
                quadraticTo(
                    ex,
                    eyeY - eyeRy * 1.26f,
                    ex + dir * eyeRx * 1.05f,
                    eyeY - eyeRy * 0.82f
                )
            }
            drawPath(browPath, GamePalette.DarkWoodOutline, style = Stroke(width = radius * 0.11f, cap = StrokeCap.Round))
            drawPath(browPath, GamePalette.OrangeWood, style = Stroke(width = radius * 0.045f, cap = StrokeCap.Round))
        }

        // Sad downturned frown mouth
        val mouthY = cy + radius * 0.50f
        val mouthPath = Path().apply {
            moveTo(cx - radius * 0.20f, mouthY + radius * 0.06f)
            quadraticTo(cx, mouthY - radius * 0.12f, cx + radius * 0.20f, mouthY + radius * 0.06f)
        }
        drawPath(
            path = mouthPath,
            color = GamePalette.DarkWoodOutline,
            style = Stroke(width = (radius * 0.075f).coerceAtLeast(2.6f), cap = StrokeCap.Round)
        )
    }

    // 4. Two chunky white cartoon gloved hands resting on wooden panel left & right
    drawCartoonGlovedHand(cx - radius * 1.02f, ledgeY - radius * 0.05f, radius * 0.44f, isRight = false)
    drawCartoonGlovedHand(cx + radius * 1.02f, ledgeY - radius * 0.05f, radius * 0.44f, isRight = true)

    // 5. Three orbiting golden dizzy stars above head
    val starAngles = listOf(0.10f, 0.42f, 0.78f)
    starAngles.forEachIndexed { idx, base ->
        val angle = (base + dizzyPhase) * 2f * PI
        val sx = cx + cos(angle).toFloat() * radius * 1.08f
        val sy = swirlCy + sin(angle).toFloat() * radius * 0.30f - idx * radius * 0.04f
        val starR = radius * (if (idx == 1) 0.24f else 0.19f)
        drawFacetedGoldenStar(
            cx = sx,
            cy = sy,
            outerRadius = starR,
            rotationDeg = (dizzyPhase * 360f + idx * 40f),
            isLit = true,
            glowAlpha = 0.6f
        )
    }
}

private fun DrawScope.drawCartoonGlovedHand(
    cx: Float,
    cy: Float,
    handSize: Float,
    isRight: Boolean
) {
    val dir = if (isRight) 1f else -1f
    val strokeW = (handSize * 0.11f).coerceAtLeast(2.2f)

    // Palm mound
    val palmRect = Rect(cx - handSize * 0.72f, cy - handSize * 0.48f, cx + handSize * 0.72f, cy + handSize * 0.48f)
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(Color.White, Color(0xFFE7ECF2), Color(0xFFBFCBD6)),
            startY = palmRect.top,
            endY = palmRect.bottom
        ),
        topLeft = palmRect.topLeft,
        size = palmRect.size
    )
    drawOval(GamePalette.DarkWoodOutline, palmRect.topLeft, palmRect.size, style = Stroke(strokeW))

    // 3 plump fingers gripping the ledge
    val fingerOffsets = listOf(-0.42f, 0.0f, 0.42f)
    fingerOffsets.forEach { f ->
        val fx = cx + f * handSize * 0.85f * dir
        val fw = handSize * 0.42f
        val fh = handSize * 0.62f
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFFDDE5EE)),
                startY = cy - fh * 0.15f,
                endY = cy + fh * 0.85f
            ),
            topLeft = Offset(fx - fw * 0.5f, cy - fh * 0.12f),
            size = Size(fw, fh)
        )
        drawOval(
            color = GamePalette.DarkWoodOutline,
            topLeft = Offset(fx - fw * 0.5f, cy - fh * 0.12f),
            size = Size(fw, fh),
            style = Stroke(strokeW * 0.9f)
        )
    }
}
