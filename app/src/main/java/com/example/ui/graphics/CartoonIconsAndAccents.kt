package com.example.ui.graphics

import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.ui.theme.GamePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun DrawScope.drawTropicalLeaf(
    anchorX: Float,
    anchorY: Float,
    length: Float,
    width: Float,
    angleDeg: Float,
    bright: Boolean = true
) {
    withTransform({
        translate(anchorX, anchorY)
        rotate(angleDeg, pivot = Offset.Zero)
    }) {
        val path = Path().apply {
            moveTo(0f, 0f)
            cubicTo(length * 0.25f, -width * 0.65f, length * 0.72f, -width * 0.55f, length, 0f)
            cubicTo(length * 0.72f, width * 0.55f, length * 0.25f, width * 0.65f, 0f, 0f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = if (bright) {
                    listOf(GamePalette.JungleGreen, GamePalette.LeafGreen, GamePalette.BrightLeaf)
                } else {
                    listOf(GamePalette.DeepForestShadow, GamePalette.JungleGreen, GamePalette.LeafGreen)
                },
                start = Offset(0f, width * 0.4f),
                end = Offset(length, -width * 0.35f)
            )
        )
        // Upper sunlit half sheen
        val topHalf = Path().apply {
            moveTo(0f, 0f)
            cubicTo(length * 0.25f, -width * 0.62f, length * 0.72f, -width * 0.52f, length, 0f)
            lineTo(0f, 0f)
            close()
        }
        drawPath(topHalf, Color(0x33D9FF66))

        // Center vein & lateral veins
        drawLine(
            color = GamePalette.SunlitLeafTip.copy(alpha = 0.85f),
            start = Offset(length * 0.06f, 0f),
            end = Offset(length * 0.86f, 0f),
            strokeWidth = (width * 0.06f).coerceAtLeast(1.2f),
            cap = StrokeCap.Round
        )
        for (i in 1..3) {
            val t = i * 0.22f
            drawLine(
                color = GamePalette.SunlitLeafTip.copy(alpha = 0.55f),
                start = Offset(length * t, 0f),
                end = Offset(length * (t + 0.16f), -width * 0.24f),
                strokeWidth = (width * 0.04f).coerceAtLeast(1f)
            )
            drawLine(
                color = GamePalette.DeepForestShadow.copy(alpha = 0.45f),
                start = Offset(length * t, 0f),
                end = Offset(length * (t + 0.16f), width * 0.24f),
                strokeWidth = (width * 0.04f).coerceAtLeast(1f)
            )
        }
        drawPath(
            path = path,
            color = GamePalette.DeepForestShadow,
            style = Stroke(width = (width * 0.08f).coerceAtLeast(1.5f), join = StrokeJoin.Round)
        )
    }
}

fun DrawScope.drawLeafCluster(
    x: Float,
    y: Float,
    scale: Float,
    baseAngleDeg: Float,
    leafCount: Int = 3
) {
    val spread = 34f
    val startAngle = baseAngleDeg - spread * (leafCount - 1) * 0.5f
    for (i in 0 until leafCount) {
        val angle = startAngle + i * spread
        val len = scale * (if (i == leafCount / 2) 1.15f else 0.92f)
        drawTropicalLeaf(
            anchorX = x,
            anchorY = y,
            length = len,
            width = len * 0.48f,
            angleDeg = angle,
            bright = i % 2 == 0
        )
    }
}

fun DrawScope.drawMetallicRivet(cx: Float, cy: Float, radius: Float) {
    // Recessed dark shadow ring
    drawCircle(
        color = GamePalette.DarkWoodOutline,
        radius = radius * 1.15f,
        center = Offset(cx, cy + radius * 0.12f)
    )
    // Metallic sphere fill
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GamePalette.SilverBoltLight,
                GamePalette.SilverBoltMid,
                GamePalette.SilverBoltDark
            ),
            center = Offset(cx - radius * 0.28f, cy - radius * 0.28f),
            radius = radius * 1.4f
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    // Tiny bright specular highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.9f),
        radius = radius * 0.28f,
        center = Offset(cx - radius * 0.3f, cy - radius * 0.3f)
    )
}

/**
 * Draws a 3D beveled faceted 5-point golden star matching Screens 02, 03, and 04.
 */
fun DrawScope.drawFacetedGoldenStar(
    cx: Float,
    cy: Float,
    outerRadius: Float,
    rotationDeg: Float = 0f,
    isLit: Boolean = true,
    glowAlpha: Float = 0f
) {
    val innerRadius = outerRadius * 0.45f
    if (isLit && glowAlpha > 0f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GamePalette.SpecularGold.copy(alpha = glowAlpha * 0.75f),
                    GamePalette.BrightGold.copy(alpha = glowAlpha * 0.3f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = outerRadius * 1.75f
            ),
            radius = outerRadius * 1.75f,
            center = Offset(cx, cy)
        )
    }

    withTransform({
        translate(cx, cy)
        rotate(rotationDeg, pivot = Offset.Zero)
    }) {
        val points = Array(10) { idx ->
            val angleRad = ((-90.0 + idx * 36.0) * PI / 180.0)
            val r = if (idx % 2 == 0) outerRadius else innerRadius
            Offset((cos(angleRad) * r).toFloat(), (sin(angleRad) * r).toFloat())
        }

        val fullStar = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until 10) lineTo(points[i].x, points[i].y)
            close()
        }

        // Drop shadow
        withTransform({ translate(0f, outerRadius * 0.1f) }) {
            drawPath(fullStar, Color(0x66341302))
        }

        if (isLit) {
            // 10 beveled triangular facets for authentic 3D gold cartoon star look
            for (i in 0 until 10) {
                val p1 = points[i]
                val p2 = points[(i + 1) % 10]
                val facet = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    close()
                }
                val isBrightSide = i % 2 == 0
                val topBias = (p1.y + p2.y) < 0f
                val facetColor = when {
                    isBrightSide && topBias -> GamePalette.SpecularGold
                    isBrightSide -> GamePalette.BrightGold
                    topBias -> GamePalette.AmberGoldMid
                    else -> GamePalette.OrangeWood
                }
                drawPath(facet, facetColor)
            }
            // Inner golden gleam highlight
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99FFFFFF), Color.Transparent),
                    center = Offset(-outerRadius * 0.15f, -outerRadius * 0.2f),
                    radius = outerRadius * 0.55f
                ),
                radius = outerRadius * 0.55f,
                center = Offset(-outerRadius * 0.15f, -outerRadius * 0.2f)
            )
        } else {
            // Unlit recessed brown-gold star slot
            drawPath(
                fullStar,
                Brush.verticalGradient(
                    colors = listOf(GamePalette.DeepWoodBrown, GamePalette.DarkWoodOutline)
                )
            )
        }

        // Thick crisp dark brown outline
        drawPath(
            path = fullStar,
            color = GamePalette.DarkWoodOutline,
            style = Stroke(
                width = (outerRadius * 0.14f).coerceAtLeast(1.8f),
                join = StrokeJoin.Round
            )
        )
    }
}

/**
 * Draws the glossy 3D Golden Coin with embossed "$" emblem matching Screen 04 & Screen 03.
 */
fun DrawScope.drawGoldCoin(
    cx: Float,
    cy: Float,
    radius: Float,
    showDollar: Boolean = true
) {
    // Drop shadow
    drawCircle(
        color = Color(0x77361202),
        radius = radius * 1.03f,
        center = Offset(cx, cy + radius * 0.1f)
    )
    // Outer thick metallic gold rim
    drawCircle(
        brush = Brush.linearGradient(
            colors = listOf(
                GamePalette.SpecularGold,
                GamePalette.BrightGold,
                GamePalette.DeepGoldShadow,
                GamePalette.GoldenWoodHighlight
            ),
            start = Offset(cx - radius, cy - radius),
            end = Offset(cx + radius, cy + radius)
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    // Dark outline
    drawCircle(
        color = GamePalette.DarkWoodOutline,
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = (radius * 0.12f).coerceAtLeast(2f))
    )
    // Recessed inner disc
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GamePalette.SpecularGold,
                GamePalette.BrightGold,
                GamePalette.AmberGoldMid
            ),
            center = Offset(cx - radius * 0.2f, cy - radius * 0.22f),
            radius = radius * 0.95f
        ),
        radius = radius * 0.77f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = GamePalette.DeepGoldShadow,
        radius = radius * 0.77f,
        center = Offset(cx, cy),
        style = Stroke(width = (radius * 0.07f).coerceAtLeast(1.3f))
    )

    if (showDollar) {
        val sStroke = (radius * 0.14f).coerceAtLeast(2f)
        // Vertical bar of $
        drawLine(
            color = GamePalette.DeepGoldShadow,
            start = Offset(cx, cy - radius * 0.46f),
            end = Offset(cx, cy + radius * 0.46f),
            strokeWidth = sStroke * 0.78f,
            cap = StrokeCap.Round
        )
        // S curve of $
        val sPath = Path().apply {
            moveTo(cx + radius * 0.25f, cy - radius * 0.22f)
            cubicTo(
                cx + radius * 0.22f, cy - radius * 0.44f,
                cx - radius * 0.28f, cy - radius * 0.42f,
                cx - radius * 0.25f, cy - radius * 0.14f
            )
            cubicTo(
                cx - radius * 0.22f, cy + radius * 0.04f,
                cx + radius * 0.28f, cy - radius * 0.02f,
                cx + radius * 0.26f, cy + radius * 0.18f
            )
            cubicTo(
                cx + radius * 0.24f, cy + radius * 0.44f,
                cx - radius * 0.24f, cy + radius * 0.44f,
                cx - radius * 0.28f, cy + radius * 0.22f
            )
        }
        drawPath(
            path = sPath,
            color = GamePalette.DeepGoldShadow,
            style = Stroke(width = sStroke * 1.15f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawPath(
            path = sPath,
            color = GamePalette.SpecularGold,
            style = Stroke(width = sStroke * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Specular shine arc
    drawCircle(
        color = Color(0x88FFFFFF),
        radius = radius * 0.18f,
        center = Offset(cx - radius * 0.36f, cy - radius * 0.38f)
    )
}

/**
 * Draws the metallic silver padlock icon matching locked levels in Screen 03.
 */
fun DrawScope.drawSilverPadlock(cx: Float, cy: Float, size: Float) {
    val bodyW = size * 0.72f
    val bodyH = size * 0.58f
    val bodyTop = cy - bodyH * 0.15f
    val strokeW = (size * 0.08f).coerceAtLeast(1.8f)

    // Shackle U-arch
    val shackleW = bodyW * 0.56f
    val shackleH = size * 0.42f
    val shacklePath = Path().apply {
        moveTo(cx - shackleW * 0.5f, bodyTop + bodyH * 0.1f)
        lineTo(cx - shackleW * 0.5f, bodyTop - shackleH * 0.4f)
        cubicTo(
            cx - shackleW * 0.5f, bodyTop - shackleH * 1.05f,
            cx + shackleW * 0.5f, bodyTop - shackleH * 1.05f,
            cx + shackleW * 0.5f, bodyTop - shackleH * 0.4f
        )
        lineTo(cx + shackleW * 0.5f, bodyTop + bodyH * 0.1f)
    }
    drawPath(shacklePath, GamePalette.DarkWoodOutline, style = Stroke(width = size * 0.21f, cap = StrokeCap.Round))
    drawPath(shacklePath, GamePalette.SilverBoltLight, style = Stroke(width = size * 0.11f, cap = StrokeCap.Round))

    // Drop shadow behind body
    drawRoundRect(
        color = Color(0x66301002),
        topLeft = Offset(cx - bodyW * 0.5f, bodyTop + size * 0.06f),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(size * 0.14f)
    )
    // Padlock body metallic gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.SilverBoltLight, GamePalette.SilverBoltMid, GamePalette.SilverBoltDark),
            startY = bodyTop,
            endY = bodyTop + bodyH
        ),
        topLeft = Offset(cx - bodyW * 0.5f, bodyTop),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(size * 0.14f)
    )
    drawRoundRect(
        color = GamePalette.DarkWoodOutline,
        topLeft = Offset(cx - bodyW * 0.5f, bodyTop),
        size = Size(bodyW, bodyH),
        cornerRadius = CornerRadius(size * 0.14f),
        style = Stroke(width = strokeW)
    )
    // Keyhole
    val keyY = bodyTop + bodyH * 0.42f
    drawCircle(color = GamePalette.DeepestWoodOutline, radius = size * 0.085f, center = Offset(cx, keyY))
    val keySlot = Path().apply {
        moveTo(cx - size * 0.05f, keyY)
        lineTo(cx + size * 0.05f, keyY)
        lineTo(cx + size * 0.075f, bodyTop + bodyH * 0.78f)
        lineTo(cx - size * 0.075f, bodyTop + bodyH * 0.78f)
        close()
    }
    drawPath(keySlot, GamePalette.DeepestWoodOutline)
}

/**
 * Draws the golden curved laurel wheat/leaf branches flanking the 3 stars on Screen 04.
 */
fun DrawScope.drawLaurelBranch(
    baseX: Float,
    baseY: Float,
    height: Float,
    isRight: Boolean
) {
    val dir = if (isRight) 1f else -1f
    val stemPath = Path().apply {
        moveTo(baseX, baseY)
        quadraticTo(
            baseX + dir * height * 0.25f,
            baseY - height * 0.5f,
            baseX + dir * height * 0.12f,
            baseY - height
        )
    }
    drawPath(stemPath, GamePalette.DarkWoodOutline, style = Stroke(width = height * 0.08f, cap = StrokeCap.Round))
    drawPath(stemPath, GamePalette.BrightGold, style = Stroke(width = height * 0.045f, cap = StrokeCap.Round))

    val leafPairs = 5
    for (i in 0 until leafPairs) {
        val t = (i + 0.6f) / (leafPairs + 0.5f)
        val px = baseX + dir * height * (0.22f * sin(t * PI).toFloat() + 0.08f * t)
        val py = baseY - height * t
        val leafLen = height * (0.26f - t * 0.05f)
        val outerAngle = if (isRight) -55f + t * 20f else -125f - t * 20f
        val innerAngle = if (isRight) -135f + t * 25f else -45f - t * 25f

        drawGoldenLaurelLeaf(px, py, leafLen, leafLen * 0.44f, outerAngle)
        drawGoldenLaurelLeaf(px, py, leafLen * 0.9f, leafLen * 0.4f, innerAngle)
    }
}

private fun DrawScope.drawGoldenLaurelLeaf(
    x: Float,
    y: Float,
    len: Float,
    wid: Float,
    angleDeg: Float
) {
    withTransform({
        translate(x, y)
        rotate(angleDeg, pivot = Offset.Zero)
    }) {
        val p = Path().apply {
            moveTo(0f, 0f)
            cubicTo(len * 0.3f, -wid * 0.6f, len * 0.75f, -wid * 0.45f, len, 0f)
            cubicTo(len * 0.75f, wid * 0.45f, len * 0.3f, wid * 0.6f, 0f, 0f)
            close()
        }
        drawPath(
            p,
            Brush.linearGradient(
                colors = listOf(GamePalette.OrangeWood, GamePalette.BrightGold, GamePalette.SpecularGold),
                start = Offset.Zero,
                end = Offset(len, 0f)
            )
        )
        drawPath(p, GamePalette.DarkWoodOutline, style = Stroke(width = 2f, join = StrokeJoin.Round))
    }
}

/**
 * Draws the detailed cartoon tropical hibiscus flower seen in foreground corners across all 6 screens.
 */
fun DrawScope.drawHibiscusFlower(
    cx: Float,
    cy: Float,
    radius: Float,
    isYellowOrange: Boolean = false
) {
    val petalOuter = if (isYellowOrange) GamePalette.HibiscusOrangeLight else GamePalette.HibiscusPinkLight
    val petalMid = if (isYellowOrange) GamePalette.HibiscusOrangeMid else GamePalette.HibiscusPinkMid
    val petalInner = if (isYellowOrange) GamePalette.OrangeWood else GamePalette.HibiscusPinkDeep

    for (i in 0 until 5) {
        val angleDeg = i * 72f - 18f
        withTransform({
            translate(cx, cy)
            rotate(angleDeg, pivot = Offset.Zero)
        }) {
            val petal = Path().apply {
                moveTo(0f, 0f)
                cubicTo(-radius * 0.55f, -radius * 0.35f, -radius * 0.48f, -radius * 1.08f, 0f, -radius)
                cubicTo(radius * 0.48f, -radius * 1.08f, radius * 0.55f, -radius * 0.35f, 0f, 0f)
                close()
            }
            drawPath(
                petal,
                Brush.radialGradient(
                    colors = listOf(petalInner, petalMid, petalOuter),
                    center = Offset(0f, -radius * 0.15f),
                    radius = radius * 1.1f
                )
            )
            drawPath(
                petal,
                petalInner.copy(alpha = 0.75f),
                style = Stroke(width = (radius * 0.055f).coerceAtLeast(1.3f))
            )
        }
    }
    // Stamen stem + golden pollen dots
    drawLine(
        color = GamePalette.SpecularGold,
        start = Offset(cx, cy),
        end = Offset(cx + radius * 0.28f, cy - radius * 0.38f),
        strokeWidth = (radius * 0.09f).coerceAtLeast(2f),
        cap = StrokeCap.Round
    )
    val pollenCenters = listOf(
        Offset(cx + radius * 0.28f, cy - radius * 0.38f),
        Offset(cx + radius * 0.18f, cy - radius * 0.42f),
        Offset(cx + radius * 0.36f, cy - radius * 0.28f),
        Offset(cx + radius * 0.22f, cy - radius * 0.26f)
    )
    pollenCenters.forEach { pt ->
        drawCircle(color = GamePalette.BrightGold, radius = radius * 0.1f, center = pt)
        drawCircle(color = GamePalette.DeepGoldShadow, radius = radius * 0.1f, center = pt, style = Stroke(1.2f))
    }
}

// =====================================================================
// CARTOON GAME BUTTON ICONS (Bold outlined white / golden icons)
// =====================================================================

fun DrawScope.drawPauseIcon(cx: Float, cy: Float, size: Float) {
    val barW = size * 0.28f
    val barH = size * 0.86f
    val gap = size * 0.18f
    val corner = CornerRadius(barW * 0.4f)
    val leftX = cx - gap * 0.5f - barW
    val rightX = cx + gap * 0.5f
    val topY = cy - barH * 0.5f
    val strokeW = (size * 0.11f).coerceAtLeast(2.2f)

    listOf(leftX, rightX).forEach { x ->
        drawRoundRect(GamePalette.DarkTextOutline, Offset(x, topY + size * 0.06f), Size(barW, barH), corner)
        drawRoundRect(GamePalette.PureWhite, Offset(x, topY), Size(barW, barH), corner)
        drawRoundRect(GamePalette.DarkTextOutline, Offset(x, topY), Size(barW, barH), corner, style = Stroke(strokeW))
    }
}

fun DrawScope.drawPlayIcon(cx: Float, cy: Float, size: Float) {
    val path = Path().apply {
        moveTo(cx - size * 0.34f, cy - size * 0.44f)
        lineTo(cx + size * 0.46f, cy)
        lineTo(cx - size * 0.34f, cy + size * 0.44f)
        close()
    }
    drawOutlinedWhiteIconPath(path, size)
}

fun DrawScope.drawRetryArrowIcon(cx: Float, cy: Float, size: Float) {
    val r = size * 0.38f
    val strokeW = size * 0.18f
    val outlineW = strokeW + size * 0.12f
    val rect = Rect(cx - r, cy - r, cx + r, cy + r)

    val arcPath = Path().apply {
        addArc(rect, startAngleDegrees = -50f, sweepAngleDegrees = 285f)
    }
    // Shadow
    withTransform({ translate(0f, size * 0.06f) }) {
        drawPath(arcPath, Color(0x88321102), style = Stroke(outlineW, cap = StrokeCap.Round))
    }
    // Dark outline
    drawPath(arcPath, GamePalette.DarkTextOutline, style = Stroke(outlineW, cap = StrokeCap.Round))
    // White fill stroke
    drawPath(arcPath, GamePalette.PureWhite, style = Stroke(strokeW, cap = StrokeCap.Round))

    // Arrowhead at top-left end of arc (~235 deg -> pointing left-down/counter-clockwise or top-left)
    val headPath = Path().apply {
        moveTo(cx - r * 0.55f, cy - r * 1.18f)
        lineTo(cx - r * 1.18f, cy - r * 0.32f)
        lineTo(cx - r * 0.14f, cy - r * 0.25f)
        close()
    }
    drawOutlinedWhiteIconPath(headPath, size * 0.85f)
}

fun DrawScope.drawDoubleChevronIcon(cx: Float, cy: Float, size: Float) {
    val strokeW = size * 0.2f
    val outlineW = strokeW + size * 0.12f
    for (offset in listOf(-size * 0.2f, size * 0.18f)) {
        val p = Path().apply {
            moveTo(cx + offset - size * 0.18f, cy - size * 0.36f)
            lineTo(cx + offset + size * 0.18f, cy)
            lineTo(cx + offset - size * 0.18f, cy + size * 0.36f)
        }
        drawPath(p, GamePalette.DarkTextOutline, style = Stroke(outlineW, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(p, GamePalette.PureWhite, style = Stroke(strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

fun DrawScope.drawDoublePlayIcon(cx: Float, cy: Float, size: Float) {
    for (offset in listOf(-size * 0.22f, size * 0.18f)) {
        val p = Path().apply {
            moveTo(cx + offset - size * 0.24f, cy - size * 0.42f)
            lineTo(cx + offset + size * 0.28f, cy)
            lineTo(cx + offset - size * 0.24f, cy + size * 0.42f)
            close()
        }
        drawOutlinedWhiteIconPath(p, size * 0.9f)
    }
}

fun DrawScope.drawBackArrowIcon(cx: Float, cy: Float, size: Float) {
    val p = Path().apply {
        moveTo(cx - size * 0.48f, cy)
        lineTo(cx + size * 0.02f, cy - size * 0.42f)
        lineTo(cx + size * 0.02f, cy - size * 0.18f)
        lineTo(cx + size * 0.46f, cy - size * 0.18f)
        lineTo(cx + size * 0.46f, cy + size * 0.18f)
        lineTo(cx + size * 0.02f, cy + size * 0.18f)
        lineTo(cx + size * 0.02f, cy + size * 0.42f)
        close()
    }
    drawOutlinedWhiteIconPath(p, size)
}

fun DrawScope.drawSpeakerIcon(cx: Float, cy: Float, size: Float, enabled: Boolean) {
    val body = Path().apply {
        moveTo(cx - size * 0.46f, cy - size * 0.18f)
        lineTo(cx - size * 0.2f, cy - size * 0.18f)
        lineTo(cx + size * 0.1f, cy - size * 0.44f)
        lineTo(cx + size * 0.1f, cy + size * 0.44f)
        lineTo(cx - size * 0.2f, cy + size * 0.18f)
        lineTo(cx - size * 0.46f, cy + size * 0.18f)
        close()
    }
    drawOutlinedWhiteIconPath(body, size)

    if (enabled) {
        for (i in 1..2) {
            val r = size * (0.22f + i * 0.15f)
            val waveRect = Rect(cx + size * 0.06f - r * 0.35f, cy - r * 0.72f, cx + size * 0.06f + r * 0.95f, cy + r * 0.72f)
            val arc = Path().apply { addArc(waveRect, -48f, 96f) }
            drawPath(arc, GamePalette.DarkTextOutline, style = Stroke(size * 0.15f, cap = StrokeCap.Round))
            drawPath(arc, GamePalette.PureWhite, style = Stroke(size * 0.08f, cap = StrokeCap.Round))
        }
    } else {
        // Red/white mute cross
        val x1 = cx + size * 0.22f
        val x2 = cx + size * 0.48f
        drawLine(GamePalette.DarkTextOutline, Offset(x1, cy - size * 0.16f), Offset(x2, cy + size * 0.16f), size * 0.16f, StrokeCap.Round)
        drawLine(GamePalette.DarkTextOutline, Offset(x2, cy - size * 0.16f), Offset(x1, cy + size * 0.16f), size * 0.16f, StrokeCap.Round)
        drawLine(GamePalette.ConfettiRed, Offset(x1, cy - size * 0.16f), Offset(x2, cy + size * 0.16f), size * 0.09f, StrokeCap.Round)
        drawLine(GamePalette.ConfettiRed, Offset(x2, cy - size * 0.16f), Offset(x1, cy + size * 0.16f), size * 0.09f, StrokeCap.Round)
    }
}

fun DrawScope.drawVibrationPhoneIcon(cx: Float, cy: Float, size: Float, enabled: Boolean) {
    val w = size * 0.44f
    val h = size * 0.78f
    val topLeft = Offset(cx - w * 0.5f, cy - h * 0.5f)
    val cr = CornerRadius(size * 0.1f)
    val outline = size * 0.1f

    drawRoundRect(GamePalette.DarkTextOutline, topLeft, Size(w, h), cr, style = Stroke(outline * 1.4f))
    drawRoundRect(GamePalette.PureWhite, topLeft, Size(w, h), cr)
    // Inner dark screen
    drawRoundRect(
        GamePalette.DeepWoodBrown,
        Offset(cx - w * 0.34f, cy - h * 0.34f),
        Size(w * 0.68f, h * 0.62f),
        CornerRadius(size * 0.04f)
    )
    drawCircle(GamePalette.DarkTextOutline, radius = size * 0.04f, center = Offset(cx, cy + h * 0.37f))

    if (enabled) {
        val waveAlpha = 1f
        for (dir in listOf(-1f, 1f)) {
            for (step in listOf(0.35f, 0.49f)) {
                val wx = cx + dir * size * step
                val wave = Path().apply {
                    moveTo(wx, cy - size * 0.25f)
                    lineTo(wx + dir * size * 0.06f, cy - size * 0.1f)
                    lineTo(wx - dir * size * 0.03f, cy + size * 0.06f)
                    lineTo(wx + dir * size * 0.05f, cy + size * 0.24f)
                }
                drawPath(wave, GamePalette.DarkTextOutline.copy(alpha = waveAlpha), style = Stroke(size * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawPath(wave, GamePalette.PureWhite.copy(alpha = waveAlpha), style = Stroke(size * 0.06f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
        }
    }
}

fun DrawScope.drawGoldenCloseX(cx: Float, cy: Float, size: Float) {
    val r = size * 0.28f
    val strokeW = size * 0.19f
    val outlineW = strokeW + size * 0.12f
    val p1 = Path().apply {
        moveTo(cx - r, cy - r)
        lineTo(cx + r, cy + r)
    }
    val p2 = Path().apply {
        moveTo(cx + r, cy - r)
        lineTo(cx - r, cy + r)
    }
    listOf(p1, p2).forEach { p ->
        drawPath(p, GamePalette.DarkWoodOutline, style = Stroke(outlineW, cap = StrokeCap.Round))
    }
    listOf(p1, p2).forEach { p ->
        drawPath(
            p,
            Brush.verticalGradient(listOf(GamePalette.SpecularGold, GamePalette.BrightGold, GamePalette.AmberGoldMid)),
            style = Stroke(strokeW, cap = StrokeCap.Round)
        )
    }
}

fun DrawScope.drawGoldenTriangleArrow(cx: Float, cy: Float, size: Float, pointingRight: Boolean) {
    val dir = if (pointingRight) 1f else -1f
    val p = Path().apply {
        moveTo(cx - dir * size * 0.24f, cy - size * 0.34f)
        lineTo(cx + dir * size * 0.32f, cy)
        lineTo(cx - dir * size * 0.24f, cy + size * 0.34f)
        close()
    }
    drawPath(p, GamePalette.DarkWoodOutline, style = Stroke(size * 0.18f, join = StrokeJoin.Round))
    drawPath(
        p,
        Brush.verticalGradient(listOf(GamePalette.SpecularGold, GamePalette.BrightGold, GamePalette.AmberGoldMid))
    )
}

private fun DrawScope.drawOutlinedWhiteIconPath(path: Path, size: Float) {
    val strokeW = (size * 0.13f).coerceAtLeast(2.4f)
    withTransform({ translate(0f, size * 0.06f) }) {
        drawPath(path, Color(0x882E0F02))
    }
    drawPath(path, GamePalette.DarkTextOutline, style = Stroke(strokeW, join = StrokeJoin.Round, cap = StrokeCap.Round))
    drawPath(
        path,
        Brush.verticalGradient(listOf(GamePalette.PureWhite, GamePalette.TextWhite))
    )
}
