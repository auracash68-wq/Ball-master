package com.example.ui.graphics

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GamePalette
import kotlin.math.PI
import kotlin.math.sin

/**
 * Draws a rich 3D cartoon wooden plank surface with multi-tone warm brown (or jungle green)
 * gradients, horizontal plank seams, natural wavy grain fibers, inner bevel shadows, and top sheen.
 */
fun DrawScope.drawWoodenPlankRect(
    rect: Rect,
    cornerRadius: Float,
    plankCount: Int = 3,
    isGreenWood: Boolean = false,
    drawBorder: Boolean = true,
    goldenTrim: Boolean = true,
    drawRivets: Boolean = true,
    rivetInsetFraction: Float = 0.08f
) {
    val cr = CornerRadius(cornerRadius, cornerRadius)

    // 1. Deep outer dimensional drop shadow
    drawRoundRect(
        color = Color(0x88280B02),
        topLeft = Offset(rect.left, rect.top + rect.height * 0.06f),
        size = rect.size,
        cornerRadius = cr
    )

    // 2. Clip interior for horizontal planks & wood grain
    val clip = Path().apply {
        addRoundRect(RoundRect(rect, cr))
    }

    clipPath(clip) {
        val baseColors = if (isGreenWood) {
            listOf(
                GamePalette.ResumeGreenTop,
                GamePalette.ResumeGreenMid,
                GamePalette.ResumeGreenBottom
            )
        } else {
            listOf(
                GamePalette.OrangeWood,
                GamePalette.MainWoodenBrown,
                GamePalette.DeepWoodBrown
            )
        }
        drawRect(
            brush = Brush.verticalGradient(
                colors = baseColors,
                startY = rect.top,
                endY = rect.bottom
            ),
            topLeft = rect.topLeft,
            size = rect.size
        )

        // Horizontal plank bands with subtle alternating warmth & dark plank seams
        val safePlanks = plankCount.coerceAtLeast(1)
        val plankH = rect.height / safePlanks
        for (p in 0 until safePlanks) {
            val py = rect.top + p * plankH
            if (p % 2 == 1) {
                drawRect(
                    color = if (isGreenWood) Color(0x18134D06) else Color(0x224A1D08),
                    topLeft = Offset(rect.left, py),
                    size = Size(rect.width, plankH)
                )
            }

            // Natural wavy horizontal wood grain lines inside each plank
            val grainCount = 3
            for (g in 1..grainCount) {
                val gy = py + plankH * (g / (grainCount + 1f))
                val grainPath = Path().apply {
                    moveTo(rect.left, gy)
                    val segments = 6
                    val segW = rect.width / segments
                    for (s in 1..segments) {
                        val sx = rect.left + s * segW
                        val wave = sin((p * 1.9 + g * 2.3 + s * 1.15)) * (plankH * 0.07f)
                        quadraticTo(
                            sx - segW * 0.5f,
                            gy + wave.toFloat(),
                            sx,
                            gy - wave.toFloat() * 0.35f
                        )
                    }
                }
                val grainColor = if (isGreenWood) {
                    if (g % 2 == 0) Color(0x28D4FF60) else Color(0x301B5B08)
                } else {
                    if (g % 2 == 0) Color(0x30FFB94D) else Color(0x444A1D08)
                }
                drawPath(
                    path = grainPath,
                    color = grainColor,
                    style = Stroke(width = (plankH * 0.045f).coerceIn(1.2f, 3.2f), cap = StrokeCap.Round)
                )
            }

            // Plank seam groove between planks
            if (p > 0) {
                drawLine(
                    color = if (isGreenWood) Color(0x66164D06) else GamePalette.DarkWoodOutline.copy(alpha = 0.85f),
                    start = Offset(rect.left, py),
                    end = Offset(rect.right, py),
                    strokeWidth = 2.6f
                )
                drawLine(
                    color = if (isGreenWood) Color(0x44B9FF55) else GamePalette.GoldenWoodHighlight.copy(alpha = 0.35f),
                    start = Offset(rect.left, py + 2.4f),
                    end = Offset(rect.right, py + 2.4f),
                    strokeWidth = 1.6f
                )
            }
        }

        // Top glossy highlight bevel inside plank
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x55FFF5BE), Color.Transparent),
                startY = rect.top,
                endY = rect.top + rect.height * 0.24f
            ),
            topLeft = rect.topLeft,
            size = Size(rect.width, rect.height * 0.24f)
        )

        // Bottom inner dark bevel shadow
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x663B1403)),
                startY = rect.bottom - rect.height * 0.22f,
                endY = rect.bottom
            ),
            topLeft = Offset(rect.left, rect.bottom - rect.height * 0.22f),
            size = Size(rect.width, rect.height * 0.22f)
        )
    }

    // 3. Dimensional golden metallic or beveled wood trim border + dark brown outline
    if (drawBorder) {
        val trimWidth = (minOf(rect.width, rect.height) * 0.085f).coerceIn(4f, 10f)
        val outerOutline = (trimWidth * 0.42f).coerceIn(2.2f, 4.2f)

        if (goldenTrim) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GamePalette.SpecularGold,
                        GamePalette.BrightGold,
                        GamePalette.OrangeWood,
                        GamePalette.GoldenWoodHighlight,
                        GamePalette.DeepGoldShadow
                    ),
                    start = rect.topLeft,
                    end = rect.bottomRight
                ),
                topLeft = rect.topLeft,
                size = rect.size,
                cornerRadius = cr,
                style = Stroke(width = trimWidth)
            )
            // Inner dark crease separating gold trim from wood body
            val innerRect = rect.deflate(trimWidth * 0.5f)
            val innerCr = (cornerRadius - trimWidth * 0.4f).coerceAtLeast(4f)
            drawRoundRect(
                color = GamePalette.DarkWoodOutline.copy(alpha = 0.8f),
                topLeft = innerRect.topLeft,
                size = innerRect.size,
                cornerRadius = CornerRadius(innerCr),
                style = Stroke(width = 1.8f)
            )
            // Subtle bright golden sparkle on top-left & top-right edge
            drawSparkleGlint(
                cx = rect.left + cornerRadius * 0.85f,
                cy = rect.top + trimWidth * 0.25f,
                radius = trimWidth * 1.15f
            )
        }

        // Crisp dark wood outer outline
        drawRoundRect(
            color = GamePalette.DarkWoodOutline,
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = cr,
            style = Stroke(width = outerOutline)
        )
    }

    // 4. Metallic rivets / bolts in corners
    if (drawRivets) {
        val rivetR = (minOf(rect.width, rect.height) * 0.055f).coerceIn(3f, 6.5f)
        val insetX = (rect.width * rivetInsetFraction).coerceIn(rivetR * 2.5f, cornerRadius * 0.9f)
        val insetY = (rect.height * 0.24f).coerceIn(rivetR * 2.2f, rect.height * 0.3f)
        drawMetallicRivet(rect.left + insetX, rect.top + insetY, rivetR)
        drawMetallicRivet(rect.right - insetX, rect.top + insetY, rivetR)
        drawMetallicRivet(rect.left + insetX, rect.bottom - insetY, rivetR)
        drawMetallicRivet(rect.right - insetX, rect.bottom - insetY, rivetR)
    }
}

fun DrawScope.drawSparkleGlint(cx: Float, cy: Float, radius: Float) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xDDFFFFFF), Color(0x66FFE46B), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius
        ),
        radius = radius,
        center = Offset(cx, cy)
    )
    drawLine(
        color = Color(0xCCFFFFFF),
        start = Offset(cx - radius * 0.8f, cy),
        end = Offset(cx + radius * 0.8f, cy),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xCCFFFFFF),
        start = Offset(cx, cy - radius * 0.8f),
        end = Offset(cx, cy + radius * 0.8f),
        strokeWidth = 1.6f,
        cap = StrokeCap.Round
    )
}

/**
 * Large central wooden board used in Select Level, Level Completed, Game Paused, and Game Over.
 */
@Composable
fun WoodenBoardPanel(
    modifier: Modifier = Modifier,
    plankCount: Int = 7,
    showBottomLeaves: Boolean = false,
    showTopCornerLeaves: Boolean = true,
    showVerticalDivider: Boolean = false,
    dividerXFraction: Float = 0.54f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
                }
        ) {
            val padX = size.width * 0.03f
            val padY = size.height * 0.035f
            val boardRect = Rect(padX, padY, size.width - padX, size.height - padY)
            val cornerR = minOf(boardRect.width, boardRect.height) * 0.095f

            // Background decorative leaf clusters behind top & bottom corners
            if (showTopCornerLeaves) {
                drawLeafCluster(boardRect.left + cornerR * 0.35f, boardRect.top + cornerR * 0.35f, size.height * 0.13f, -140f, 3)
                drawLeafCluster(boardRect.right - cornerR * 0.35f, boardRect.top + cornerR * 0.35f, size.height * 0.13f, -40f, 3)
            }
            if (showBottomLeaves) {
                drawLeafCluster(boardRect.left + cornerR * 0.25f, boardRect.bottom - cornerR * 0.6f, size.height * 0.14f, 160f, 3)
                drawLeafCluster(boardRect.right - cornerR * 0.25f, boardRect.bottom - cornerR * 0.6f, size.height * 0.14f, 20f, 3)
            }

            // Thick outer carved wood-gold frame
            val outerFrameRect = boardRect.inflate(size.height * 0.014f)
            val outerCr = CornerRadius(cornerR * 1.12f)
            drawRoundRect(
                color = Color(0x99260B02),
                topLeft = Offset(outerFrameRect.left, outerFrameRect.top + size.height * 0.028f),
                size = outerFrameRect.size,
                cornerRadius = outerCr
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        GamePalette.GoldenWoodHighlight,
                        GamePalette.OrangeWood,
                        GamePalette.MainWoodenBrown,
                        GamePalette.OrangeWood,
                        GamePalette.DeepGoldShadow
                    ),
                    start = outerFrameRect.topLeft,
                    end = outerFrameRect.bottomRight
                ),
                topLeft = outerFrameRect.topLeft,
                size = outerFrameRect.size,
                cornerRadius = outerCr
            )
            drawRoundRect(
                color = GamePalette.DarkWoodOutline,
                topLeft = outerFrameRect.topLeft,
                size = outerFrameRect.size,
                cornerRadius = outerCr,
                style = Stroke(width = 4.5f)
            )

            // Inner horizontal wood plank interior
            val innerRect = boardRect.deflate(size.height * 0.025f)
            drawWoodenPlankRect(
                rect = innerRect,
                cornerRadius = cornerR * 0.82f,
                plankCount = plankCount,
                isGreenWood = false,
                drawBorder = true,
                goldenTrim = false,
                drawRivets = false
            )

            // Large metallic corner rivets on outer frame
            val rivetR = (size.height * 0.022f).coerceIn(4.5f, 8.5f)
            val rxOff = cornerR * 0.42f
            val ryOff = cornerR * 0.42f
            drawMetallicRivet(outerFrameRect.left + rxOff, outerFrameRect.top + ryOff, rivetR)
            drawMetallicRivet(outerFrameRect.right - rxOff, outerFrameRect.top + ryOff, rivetR)
            drawMetallicRivet(outerFrameRect.left + rxOff, outerFrameRect.bottom - ryOff, rivetR)
            drawMetallicRivet(outerFrameRect.right - rxOff, outerFrameRect.bottom - ryOff, rivetR)

            // Optional vertical wooden divider (Screen 05 Game Paused)
            if (showVerticalDivider) {
                val divX = innerRect.left + innerRect.width * dividerXFraction
                val divTop = innerRect.top + innerRect.height * 0.14f
                val divBottom = innerRect.bottom - innerRect.height * 0.12f
                val divW = (size.width * 0.012f).coerceIn(4f, 8f)
                drawRoundRect(
                    color = GamePalette.DarkWoodOutline,
                    topLeft = Offset(divX - divW * 0.5f + 2f, divTop + 2f),
                    size = Size(divW, divBottom - divTop),
                    cornerRadius = CornerRadius(divW * 0.5f)
                )
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(GamePalette.GoldenWoodHighlight, GamePalette.OrangeWood, GamePalette.DeepWoodBrown),
                        startX = divX - divW * 0.5f,
                        endX = divX + divW * 0.5f
                    ),
                    topLeft = Offset(divX - divW * 0.5f, divTop),
                    size = Size(divW, divBottom - divTop),
                    cornerRadius = CornerRadius(divW * 0.5f)
                )
            }
        }
        content()
    }
}

/**
 * Overlapping wooden header sign banner with lush green leaves behind both sides and rivets.
 */
@Composable
fun WoodenHeaderSign(
    title: String,
    modifier: Modifier = Modifier,
    isGoldenText: Boolean = false,
    showSideLeaves: Boolean = true
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }

    Canvas(modifier = modifier) {
        val h = size.height
        val w = size.width
        val bannerRect = Rect(w * 0.08f, h * 0.14f, w * 0.92f, h * 0.92f)
        val cornerR = bannerRect.height * 0.45f

        if (showSideLeaves) {
            drawLeafCluster(bannerRect.left + cornerR * 0.35f, bannerRect.center.y, h * 0.52f, -150f, 4)
            drawLeafCluster(bannerRect.right - cornerR * 0.35f, bannerRect.center.y, h * 0.52f, -30f, 4)
        }

        drawWoodenPlankRect(
            rect = bannerRect,
            cornerRadius = cornerR,
            plankCount = 2,
            isGreenWood = false,
            drawBorder = true,
            goldenTrim = true,
            drawRivets = true,
            rivetInsetFraction = 0.075f
        )

        val textSizePx = bannerRect.height * 0.58f
        drawOutlinedCartoonText(
            text = title,
            cx = bannerRect.center.x,
            baselineY = bannerRect.center.y + textSizePx * 0.34f,
            textSizePx = textSizePx,
            typeface = typeface,
            topColor = if (isGoldenText) GamePalette.SpecularGold else GamePalette.PureWhite,
            bottomColor = if (isGoldenText) GamePalette.BrightGold else GamePalette.TextWhite,
            outlineColor = GamePalette.DarkTextOutline,
            outlineWidthPx = textSizePx * 0.18f
        )
    }
}

enum class GameButtonIcon {
    None,
    Play,
    Retry,
    DoubleChevron,
    DoublePlay,
    BackArrow
}

/**
 * Interactive 3D cartoon wooden pill button with spring press bounce, golden trim,
 * rivets, optional side leaves, cartoon icon, and outlined bold label.
 */
@Composable
fun WoodenPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTagName: String = "",
    contentDesc: String = text,
    isGreen: Boolean = false,
    icon: GameButtonIcon = GameButtonIcon.None,
    showSideLeaves: Boolean = true,
    enabled: Boolean = true,
    textScale: Float = 0.44f
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.94f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.55f),
        label = "wooden_btn_scale"
    )

    Canvas(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.72f
            }
            .then(if (testTagName.isNotEmpty()) Modifier.testTag(testTagName) else Modifier)
            .semantics { contentDescription = contentDesc }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        val padX = if (showSideLeaves) size.width * 0.05f else size.width * 0.015f
        val padY = size.height * 0.06f
        val rect = Rect(padX, padY, size.width - padX, size.height - padY)
        val cornerR = rect.height * 0.48f

        if (showSideLeaves) {
            drawLeafCluster(rect.left + rect.height * 0.14f, rect.center.y, rect.height * 0.42f, 180f, 3)
            drawLeafCluster(rect.right - rect.height * 0.14f, rect.center.y, rect.height * 0.42f, 0f, 3)
        }

        drawWoodenPlankRect(
            rect = rect,
            cornerRadius = cornerR,
            plankCount = 2,
            isGreenWood = isGreen,
            drawBorder = true,
            goldenTrim = true,
            drawRivets = true,
            rivetInsetFraction = 0.085f
        )

        val textSizePx = rect.height * textScale
        if (icon == GameButtonIcon.None) {
            drawOutlinedCartoonText(
                text = text,
                cx = rect.center.x,
                baselineY = rect.center.y + textSizePx * 0.35f,
                textSizePx = textSizePx,
                typeface = typeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineWidthPx = textSizePx * 0.18f
            )
        } else {
            val iconSize = rect.height * 0.54f
            val iconCx = rect.left + rect.width * 0.24f
            when (icon) {
                GameButtonIcon.Play -> drawPlayIcon(iconCx, rect.center.y, iconSize)
                GameButtonIcon.Retry -> drawRetryArrowIcon(iconCx, rect.center.y, iconSize)
                GameButtonIcon.DoubleChevron -> drawDoubleChevronIcon(iconCx, rect.center.y, iconSize)
                GameButtonIcon.DoublePlay -> drawDoublePlayIcon(iconCx, rect.center.y, iconSize)
                GameButtonIcon.BackArrow -> drawBackArrowIcon(iconCx, rect.center.y, iconSize)
                GameButtonIcon.None -> Unit
            }
            val textCx = rect.left + rect.width * 0.60f
            drawOutlinedCartoonText(
                text = text,
                cx = textCx,
                baselineY = rect.center.y + textSizePx * 0.34f,
                textSizePx = textSizePx,
                typeface = typeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineWidthPx = textSizePx * 0.18f
            )
        }
    }
}

/**
 * Circular wooden medallion button with golden rim (Close X, Prev/Next Page arrows).
 */
@Composable
fun WoodenCircleActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 52.dp,
    testTagName: String = "",
    contentDesc: String = "",
    enabled: Boolean = true,
    showLeaves: Boolean = false,
    drawSymbol: DrawScope.(cx: Float, cy: Float, radius: Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.55f),
        label = "circle_btn_scale"
    )

    Canvas(
        modifier = modifier
            .size(sizeDp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .then(if (testTagName.isNotEmpty()) Modifier.testTag(testTagName) else Modifier)
            .semantics { contentDescription = contentDesc }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.5f
        val r = minOf(size.width, size.height) * 0.44f

        if (showLeaves) {
            drawLeafCluster(cx + r * 0.25f, cy + r * 0.45f, r * 0.72f, 60f, 3)
        }

        // Shadow
        drawCircle(Color(0x882A0C02), radius = r * 1.04f, center = Offset(cx, cy + r * 0.1f))
        // Golden rim
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(GamePalette.SpecularGold, GamePalette.BrightGold, GamePalette.OrangeWood, GamePalette.DeepGoldShadow),
                start = Offset(cx - r, cy - r),
                end = Offset(cx + r, cy + r)
            ),
            radius = r,
            center = Offset(cx, cy)
        )
        drawCircle(GamePalette.DarkWoodOutline, radius = r, center = Offset(cx, cy), style = Stroke(3.2f))

        // Inner wood circle
        val innerR = r * 0.80f
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(GamePalette.OrangeWood, GamePalette.MainWoodenBrown, GamePalette.DeepWoodBrown),
                startY = cy - innerR,
                endY = cy + innerR
            ),
            radius = innerR,
            center = Offset(cx, cy)
        )
        // Subtle horizontal grain lines
        for (dy in listOf(-0.35f, 0.0f, 0.35f)) {
            val y = cy + innerR * dy
            val halfSpan = innerR * 0.78f
            drawLine(
                color = GamePalette.DarkWoodOutline.copy(alpha = 0.35f),
                start = Offset(cx - halfSpan, y),
                end = Offset(cx + halfSpan, y),
                strokeWidth = 1.6f
            )
        }
        drawCircle(GamePalette.DarkWoodOutline, radius = innerR, center = Offset(cx, cy), style = Stroke(2f))

        drawSymbol(cx, cy, r)
    }
}

/**
 * Horizontal wooden settings row with icon, outlined label, and glossy green/brown toggle switch
 * matching Screen 05 (Sound & Vibration).
 */
@Composable
fun WoodenSettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isSoundIcon: Boolean,
    modifier: Modifier = Modifier,
    testTagName: String = "",
    contentDesc: String = label
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }
    val thumbProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.7f),
        label = "toggle_thumb"
    )

    Canvas(
        modifier = modifier
            .then(if (testTagName.isNotEmpty()) Modifier.testTag(testTagName) else Modifier)
            .semantics { contentDescription = contentDesc }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) }
            )
    ) {
        val padX = size.width * 0.04f
        val padY = size.height * 0.08f
        val rect = Rect(padX, padY, size.width - padX, size.height - padY)
        val cornerR = rect.height * 0.44f

        // Decorative leaves on left side matching Screen 05
        drawLeafCluster(rect.left + rect.height * 0.12f, rect.center.y, rect.height * 0.42f, 180f, 3)

        drawWoodenPlankRect(
            rect = rect,
            cornerRadius = cornerR,
            plankCount = 2,
            isGreenWood = false,
            drawBorder = true,
            goldenTrim = true,
            drawRivets = true,
            rivetInsetFraction = 0.06f
        )

        // Left icon
        val iconSize = rect.height * 0.56f
        val iconCx = rect.left + rect.width * 0.18f
        if (isSoundIcon) {
            drawSpeakerIcon(iconCx, rect.center.y, iconSize, enabled = checked)
        } else {
            drawVibrationPhoneIcon(iconCx, rect.center.y, iconSize, enabled = checked)
        }

        // Center-left label
        val textSizePx = rect.height * 0.36f
        drawOutlinedCartoonText(
            text = label,
            cx = rect.left + rect.width * 0.48f,
            baselineY = rect.center.y + textSizePx * 0.34f,
            textSizePx = textSizePx,
            typeface = typeface,
            topColor = GamePalette.PureWhite,
            bottomColor = GamePalette.TextWhite,
            outlineWidthPx = textSizePx * 0.17f
        )

        // Right pill toggle switch
        val trackW = rect.width * 0.25f
        val trackH = rect.height * 0.50f
        val trackLeft = rect.right - rect.width * 0.31f
        val trackTop = rect.center.y - trackH * 0.5f
        val trackCr = CornerRadius(trackH * 0.5f)

        val trackColors = if (checked) {
            listOf(GamePalette.BrightLeaf, GamePalette.SuccessGreen, GamePalette.JungleGreen)
        } else {
            listOf(GamePalette.DeepWoodBrown, GamePalette.DarkWoodOutline)
        }

        drawRoundRect(
            brush = Brush.verticalGradient(trackColors, startY = trackTop, endY = trackTop + trackH),
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackW, trackH),
            cornerRadius = trackCr
        )
        drawRoundRect(
            color = GamePalette.DarkWoodOutline,
            topLeft = Offset(trackLeft, trackTop),
            size = Size(trackW, trackH),
            cornerRadius = trackCr,
            style = Stroke(width = 2.8f)
        )

        val thumbR = trackH * 0.43f
        val thumbMinX = trackLeft + thumbR + 3f
        val thumbMaxX = trackLeft + trackW - thumbR - 3f
        val thumbCx = thumbMinX + (thumbMaxX - thumbMinX) * thumbProgress
        val thumbCy = trackTop + trackH * 0.5f

        // Glossy white sphere thumb
        drawCircle(Color(0x66220802), radius = thumbR * 1.05f, center = Offset(thumbCx, thumbCy + 2f))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, Color(0xFFE9F0F5), Color(0xFFB5C4CE)),
                center = Offset(thumbCx - thumbR * 0.25f, thumbCy - thumbR * 0.25f),
                radius = thumbR * 1.4f
            ),
            radius = thumbR,
            center = Offset(thumbCx, thumbCy)
        )
        drawCircle(GamePalette.DarkWoodOutline, radius = thumbR, center = Offset(thumbCx, thumbCy), style = Stroke(2.2f))
    }
}
