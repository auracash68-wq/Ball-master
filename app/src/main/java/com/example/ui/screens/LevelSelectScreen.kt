package com.example.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.GameUiState
import com.example.ui.graphics.GameFontCache
import com.example.ui.graphics.JungleBackgroundCanvas
import com.example.ui.graphics.JungleSceneMode
import com.example.ui.graphics.WoodenBoardPanel
import com.example.ui.graphics.WoodenCircleActionButton
import com.example.ui.graphics.WoodenHeaderSign
import com.example.ui.graphics.drawFacetedGoldenStar
import com.example.ui.graphics.drawGoldCoin
import com.example.ui.graphics.drawGoldenTriangleArrow
import com.example.ui.graphics.drawLeafCluster
import com.example.ui.graphics.drawOutlinedCartoonText
import com.example.ui.graphics.drawSilverPadlock
import com.example.ui.graphics.drawWoodenPlankRect
import com.example.ui.theme.GamePalette

/**
 * SCREEN 03 — LEVEL SELECTION (Landscape)
 * Exact code-driven recreation of the 5x3 circular wooden level grid board,
 * overlapping "Select Level" wooden header banner with leaves, upper-right wooden coin counter,
 * star badges for unlocked/completed levels, silver padlocks for locked levels,
 * and left/right circular wooden page navigation buttons covering all 50 levels.
 */
@Composable
fun LevelSelectScreen(
    uiState: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }

    val titleText = stringResource(R.string.select_level_title)
    val cdPrev = stringResource(R.string.cd_prev_levels)
    val cdNext = stringResource(R.string.cd_next_levels)
    val cdCoins = stringResource(R.string.cd_coins_balance, uiState.coins)

    Box(modifier = modifier.fillMaxSize().testTag("level_select_screen")) {
        JungleBackgroundCanvas(mode = JungleSceneMode.JungleDirtPath)

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            val boardW = (maxWidth * 0.78f).coerceIn(380.dp, 680.dp)
            val boardH = (maxHeight * 0.78f).coerceIn(250.dp, 380.dp)
            val headerW = (boardW * 0.56f).coerceIn(220.dp, 340.dp)
            val headerH = (boardH * 0.21f).coerceIn(48.dp, 68.dp)
            val arrowSize = (boardH * 0.19f).coerceIn(48.dp, 66.dp)

            // Upper-Right Coin Counter Wooden Container
            val coinBadgeW = (maxWidth * 0.20f).coerceIn(124.dp, 168.dp)
            val coinBadgeH = (maxHeight * 0.135f).coerceIn(46.dp, 58.dp)
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 12.dp)
                    .width(coinBadgeW)
                    .height(coinBadgeH)
                    .testTag("coin_counter_badge")
                    .semantics { contentDescription = cdCoins }
            ) {
                val padX = size.width * 0.04f
                val padY = size.height * 0.08f
                val rect = Rect(padX, padY, size.width - padX, size.height - padY)
                drawWoodenPlankRect(
                    rect = rect,
                    cornerRadius = rect.height * 0.45f,
                    plankCount = 2,
                    goldenTrim = true,
                    drawRivets = true,
                    rivetInsetFraction = 0.09f
                )
                drawGoldCoin(
                    cx = rect.left + rect.height * 0.60f,
                    cy = rect.center.y,
                    radius = rect.height * 0.34f,
                    showDollar = true
                )
                val fontSize = rect.height * 0.48f
                drawOutlinedCartoonText(
                    text = uiState.coins.toString(),
                    cx = rect.left + rect.width * 0.66f,
                    baselineY = rect.center.y + fontSize * 0.34f,
                    textSizePx = fontSize,
                    typeface = typeface,
                    topColor = GamePalette.PureWhite,
                    bottomColor = GamePalette.TextWhite
                )
            }

            // Main Centered Wooden Level Board + Overlapping Header & Page Arrows
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 14.dp)
                    .width(boardW + arrowSize * 0.9f)
                    .height(boardH + headerH * 0.45f),
                contentAlignment = Alignment.Center
            ) {
                // Wooden Plank Board
                WoodenBoardPanel(
                    modifier = Modifier
                        .width(boardW)
                        .height(boardH)
                        .align(Alignment.BottomCenter),
                    plankCount = 8,
                    showTopCornerLeaves = true,
                    showBottomLeaves = false
                ) {
                    val startLevel = uiState.levelSelectPage * GameUiState.LEVELS_PER_PAGE + 1
                    val nodeSize = (minOf((boardW - 76.dp) / 5f, (boardH - 64.dp) / 3f)).coerceIn(50.dp, 86.dp)

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                start = 30.dp,
                                end = 30.dp,
                                top = headerH * 0.56f,
                                bottom = 18.dp
                            ),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (row in 0 until 3) {
                            Row(
                                modifier = Modifier.width(boardW - 60.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0 until 5) {
                                    val levelNum = startLevel + row * 5 + col
                                    if (levelNum <= uiState.totalLevels) {
                                        val isUnlocked = levelNum <= uiState.unlockedLevel
                                        val stars = uiState.levelStars[levelNum] ?: 0
                                        val isCurrentFrontier = levelNum == uiState.unlockedLevel
                                        LevelMedallionNode(
                                            levelNumber = levelNum,
                                            isUnlocked = isUnlocked,
                                            starsEarned = stars,
                                            isFrontier = isCurrentFrontier,
                                            nodeSize = nodeSize,
                                            onClick = { onSelectLevel(levelNum) }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(nodeSize))
                                    }
                                }
                            }
                        }
                    }
                }

                // Overlapping "Select Level" Header Banner
                WoodenHeaderSign(
                    title = titleText,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(headerW)
                        .height(headerH),
                    isGoldenText = false,
                    showSideLeaves = true
                )

                // Left Circular Wooden Page Arrow (<)
                WoodenCircleActionButton(
                    onClick = onPrevPage,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset(y = headerH * 0.22f),
                    sizeDp = arrowSize,
                    testTagName = "level_page_prev",
                    contentDesc = cdPrev,
                    enabled = uiState.levelSelectPage > 0
                ) { cx, cy, r ->
                    drawGoldenTriangleArrow(cx, cy, r * 1.15f, pointingRight = false)
                }

                // Right Circular Wooden Page Arrow (>)
                WoodenCircleActionButton(
                    onClick = onNextPage,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(y = headerH * 0.22f),
                    sizeDp = arrowSize,
                    testTagName = "level_page_next",
                    contentDesc = cdNext,
                    enabled = uiState.levelSelectPage < uiState.totalLevelPages - 1
                ) { cx, cy, r ->
                    drawGoldenTriangleArrow(cx, cy, r * 1.15f, pointingRight = true)
                }
            }
        }
    }
}

@Composable
private fun LevelMedallionNode(
    levelNumber: Int,
    isUnlocked: Boolean,
    starsEarned: Int,
    isFrontier: Boolean,
    nodeSize: Dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val typeface = remember(context) { GameFontCache.lilita(context) }
    val cd = if (isUnlocked) {
        stringResource(R.string.cd_level_unlocked, levelNumber)
    } else {
        stringResource(R.string.cd_level_locked, levelNumber)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && isUnlocked) 0.90f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.55f),
        label = "level_node_scale"
    )

    Canvas(
        modifier = Modifier
            .size(nodeSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .testTag("level_button_$levelNumber")
            .semantics { contentDescription = cd }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val circleCy = h * 0.43f
        val r = minOf(w, h) * 0.37f

        // Small decorative tropical leaves peeking out left & right
        drawLeafCluster(cx - r * 0.85f, circleCy, r * 0.62f, 180f, 2)
        drawLeafCluster(cx + r * 0.85f, circleCy, r * 0.62f, 0f, 2)

        // Subtle golden glow behind current unlocked frontier level
        if (isFrontier) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GamePalette.SpecularGold.copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(cx, circleCy),
                    radius = r * 1.48f
                ),
                radius = r * 1.48f,
                center = Offset(cx, circleCy)
            )
        }

        // Drop shadow under wooden medallion
        drawCircle(Color(0x802A0C02), radius = r * 1.04f, center = Offset(cx, circleCy + r * 0.12f))

        // Golden metallic outer ring
        drawCircle(
            brush = Brush.linearGradient(
                colors = if (isUnlocked) {
                    listOf(GamePalette.SpecularGold, GamePalette.BrightGold, GamePalette.OrangeWood, GamePalette.DeepGoldShadow)
                } else {
                    listOf(GamePalette.GoldenWoodHighlight, GamePalette.OrangeWood, GamePalette.DeepWoodBrown)
                },
                start = Offset(cx - r, circleCy - r),
                end = Offset(cx + r, circleCy + r)
            ),
            radius = r,
            center = Offset(cx, circleCy)
        )
        drawCircle(GamePalette.DarkWoodOutline, radius = r, center = Offset(cx, circleCy), style = Stroke(2.8f))

        // Inner wooden circular face
        val innerR = r * 0.80f
        drawCircle(
            brush = Brush.verticalGradient(
                colors = if (isUnlocked) {
                    listOf(GamePalette.OrangeWood, GamePalette.MainWoodenBrown, GamePalette.DeepWoodBrown)
                } else {
                    listOf(Color(0xFFA8561B), Color(0xFF7B370D), Color(0xFF5A2506))
                },
                startY = circleCy - innerR,
                endY = circleCy + innerR
            ),
            radius = innerR,
            center = Offset(cx, circleCy)
        )
        // Subtle horizontal grain lines
        for (dy in listOf(-0.35f, 0.05f, 0.40f)) {
            val gy = circleCy + innerR * dy
            drawLine(
                color = GamePalette.DarkWoodOutline.copy(alpha = 0.30f),
                start = Offset(cx - innerR * 0.72f, gy),
                end = Offset(cx + innerR * 0.72f, gy),
                strokeWidth = 1.4f
            )
        }
        drawCircle(GamePalette.DarkWoodOutline, radius = innerR, center = Offset(cx, circleCy), style = Stroke(1.8f))

        // Small wooden pedestal bar underneath circle
        val ribW = r * 1.82f
        val ribH = h * 0.23f
        val ribTop = h * 0.72f
        val ribRect = Rect(cx - ribW * 0.5f, ribTop, cx + ribW * 0.5f, ribTop + ribH)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(GamePalette.OrangeWood, GamePalette.DeepWoodBrown),
                startY = ribRect.top,
                endY = ribRect.bottom
            ),
            topLeft = ribRect.topLeft,
            size = ribRect.size,
            cornerRadius = CornerRadius(ribH * 0.42f)
        )
        drawRoundRect(
            color = GamePalette.DarkWoodOutline,
            topLeft = ribRect.topLeft,
            size = ribRect.size,
            cornerRadius = CornerRadius(ribH * 0.42f),
            style = Stroke(2.2f)
        )

        if (isUnlocked) {
            // Centered bold white outlined level number
            val numSize = r * 1.06f
            drawOutlinedCartoonText(
                text = levelNumber.toString(),
                cx = cx,
                baselineY = circleCy + numSize * 0.34f,
                textSizePx = numSize,
                typeface = typeface,
                topColor = GamePalette.PureWhite,
                bottomColor = GamePalette.TextWhite,
                outlineWidthPx = numSize * 0.19f
            )

            // 3 small faceted golden stars on the lower wooden ribbon
            val starR = ribH * 0.58f
            val starSpacing = ribW * 0.28f
            for (s in 0 until 3) {
                val sx = cx + (s - 1) * starSpacing
                val sy = ribRect.center.y - (if (s == 1) starR * 0.14f else 0f)
                drawFacetedGoldenStar(
                    cx = sx,
                    cy = sy,
                    outerRadius = if (s == 1) starR * 1.12f else starR,
                    isLit = s < starsEarned,
                    glowAlpha = if (s < starsEarned) 0.3f else 0f
                )
            }
        } else {
            // Locked state: number in upper half, crisp metallic silver padlock over lower half
            val numSize = r * 0.78f
            drawOutlinedCartoonText(
                text = levelNumber.toString(),
                cx = cx,
                baselineY = circleCy - r * 0.08f + numSize * 0.30f,
                textSizePx = numSize,
                typeface = typeface,
                topColor = GamePalette.TextWhite.copy(alpha = 0.92f),
                bottomColor = GamePalette.TextWhite.copy(alpha = 0.85f),
                outlineWidthPx = numSize * 0.20f
            )
            drawSilverPadlock(
                cx = cx,
                cy = circleCy + r * 0.72f,
                size = r * 0.92f
            )
        }
    }
}
