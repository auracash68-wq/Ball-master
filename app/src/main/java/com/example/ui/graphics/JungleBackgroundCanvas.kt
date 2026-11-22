package com.example.ui.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas as ComposeCanvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.example.ui.theme.GamePalette
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

enum class JungleSceneMode {
    SplashPortrait,
    GameplayStonePlatform,
    JungleDirtPath
}

private data class BackgroundCacheKey(
    val mode: JungleSceneMode,
    val width: Int,
    val height: Int
)

/**
 * In-memory bitmap cache so each jungle background scene is rasterized ONCE per size/mode
 * and blitted in <1ms per frame, preventing UI-thread stalls and SurfaceSyncGroup timeouts.
 */
private object JungleBitmapCache {
    private val cache = LinkedHashMap<BackgroundCacheKey, ImageBitmap>(4, 0.75f, true)

    fun getOrRender(
        mode: JungleSceneMode,
        targetW: Int,
        targetH: Int
    ): ImageBitmap {
        // Cap internal rasterization to 1080px long edge for crisp visuals + rapid <20ms raster
        val maxEdge = 1080f
        val longEdge = max(targetW, targetH).coerceAtLeast(1).toFloat()
        val scale = if (longEdge > maxEdge) maxEdge / longEdge else 1f
        val bmpW = (targetW * scale).roundToInt().coerceAtLeast(64)
        val bmpH = (targetH * scale).roundToInt().coerceAtLeast(64)
        val key = BackgroundCacheKey(mode, bmpW, bmpH)

        synchronized(this) {
            cache[key]?.let { return it }
        }

        val bitmap = ImageBitmap(bmpW, bmpH, ImageBitmapConfig.Argb8888)
        val canvas = ComposeCanvas(bitmap)
        val drawScope = CanvasDrawScope()
        drawScope.draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(bmpW.toFloat(), bmpH.toFloat())
        ) {
            renderFullJungleScene(mode)
        }

        synchronized(this) {
            if (cache.size >= 4) {
                val oldestKey = cache.entries.firstOrNull()?.key
                if (oldestKey != null) cache.remove(oldestKey)
            }
            cache[key] = bitmap
        }
        return bitmap
    }
}

/**
 * Full-screen 100% code-drawn 3D cartoon tropical jungle environment matching all 6 reference screens,
 * backed by an automatic offscreen ImageBitmap cache for instant 60fps rendering.
 */
@Composable
fun JungleBackgroundCanvas(
    mode: JungleSceneMode,
    modifier: Modifier = Modifier
) {
    val holder = remember(mode) { arrayOfNulls<Pair<BackgroundCacheKey, ImageBitmap>>(1) }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width.roundToInt().coerceAtLeast(1)
        val h = size.height.roundToInt().coerceAtLeast(1)
        val cached = holder[0]
        val bmp = if (cached != null && cached.first.mode == mode && cached.first.width == w && cached.first.height == h) {
            cached.second
        } else {
            val rendered = JungleBitmapCache.getOrRender(mode, w, h)
            holder[0] = BackgroundCacheKey(mode, w, h) to rendered
            rendered
        }

        drawImage(
            image = bmp,
            srcOffset = IntOffset.Zero,
            srcSize = IntSize(bmp.width, bmp.height),
            dstOffset = IntOffset.Zero,
            dstSize = IntSize(w, h),
            filterQuality = FilterQuality.Medium
        )
    }
}

private fun DrawScope.renderFullJungleScene(mode: JungleSceneMode) {
    val w = size.width
    val h = size.height

    // 1. Bright Tropical Sky Gradient
    drawRect(
        brush = Brush.verticalGradient(
            colorStops = arrayOf(
                0.00f to GamePalette.SkyBlueTop,
                0.28f to GamePalette.SkyBlue,
                0.56f to GamePalette.SkyCyanHorizon,
                0.72f to Color(0xFF9DF0C8),
                1.00f to GamePalette.LeafGreen
            ),
            startY = 0f,
            endY = h
        )
    )

    // 2. Soft Cumulus Clouds in upper sky
    drawCloudCluster(w * 0.34f, h * 0.10f, minOf(w, h) * 0.14f)
    drawCloudCluster(w * 0.68f, h * 0.08f, minOf(w, h) * 0.16f)

    // 3. Atmospheric Distant Misty Jungle Forest Layers
    drawDistantJungleSilhouette(w, h, horizonY = h * 0.56f, color = Color(0xFF56C9B8), seed = 1f)
    drawDistantJungleSilhouette(w, h, horizonY = h * 0.62f, color = GamePalette.DistantForestBlueGreen, seed = 2.7f)
    drawDistantJungleSilhouette(w, h, horizonY = h * 0.68f, color = GamePalette.DistantCanopyGreen, seed = 4.3f)

    // 4. Diagonal Warm Golden Sunlight Rays streaming from upper left
    drawSunlightRays(w, h)

    // 5. Lush Green Meadow Midground
    val meadowTop = if (mode == JungleSceneMode.SplashPortrait) h * 0.64f else h * 0.66f
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.BrightLeaf, GamePalette.LeafGreen, GamePalette.JungleGreen),
            startY = meadowTop,
            endY = h
        ),
        topLeft = Offset(0f, meadowTop),
        size = Size(w, h - meadowTop)
    )

    // 6. Wooden Post-and-Rail Fences (left & right midground)
    val fenceY = if (mode == JungleSceneMode.SplashPortrait) h * 0.57f else h * 0.55f
    val fenceH = h * 0.16f
    drawWoodenFenceSegment(0f, fenceY, w * 0.26f, fenceH, posts = 3)
    drawWoodenFenceSegment(w * 0.74f, fenceY + fenceH * 0.12f, w * 0.28f, fenceH * 0.9f, posts = 3)

    // 7. Cozy Thatched-Roof Wooden Village Hut on the Right
    if (mode == JungleSceneMode.SplashPortrait) {
        drawVillageHut(
            cx = w * 0.90f,
            baseY = h * 0.62f,
            hutWidth = w * 0.44f,
            hutHeight = h * 0.23f
        )
    } else {
        drawVillageHut(
            cx = w * 0.94f,
            baseY = h * 0.64f,
            hutWidth = w * 0.23f,
            hutHeight = h * 0.36f
        )
    }

    // 8. Sunlit Winding Dirt Path (Splash bottom & JungleDirtPath screens)
    if (mode == JungleSceneMode.JungleDirtPath || mode == JungleSceneMode.SplashPortrait) {
        val pathTopY = if (mode == JungleSceneMode.SplashPortrait) h * 0.78f else h * 0.74f
        drawSunlitDirtPath(w, h, pathTopY)
    }

    // 9. Framing Left & Right Gnarled Jungle Tree Trunks + Overhanging Foliage
    drawFramingJungleTrees(w, h, isPortrait = mode == JungleSceneMode.SplashPortrait)

    // 10. Weathered Mossy 3D Stone Platform / Pedestal
    when (mode) {
        JungleSceneMode.SplashPortrait -> {
            drawStonePlatform(
                cx = w * 0.50f,
                topY = h * 0.61f,
                platformWidth = w * 0.88f,
                platformHeight = h * 0.24f,
                topThickness = h * 0.048f
            )
        }
        JungleSceneMode.GameplayStonePlatform -> {
            drawStonePlatform(
                cx = w * 0.50f,
                topY = h * 0.555f,
                platformWidth = w * 0.96f,
                platformHeight = h * 0.33f,
                topThickness = h * 0.085f
            )
        }
        JungleSceneMode.JungleDirtPath -> Unit
    }

    // 11. Foreground Mossy Boulders, Grass Tufts, Tropical Ferns & Hibiscus Flowers
    drawForegroundFloraAndFlowers(w, h, mode)
}

private fun DrawScope.drawCloudCluster(cx: Float, cy: Float, scale: Float) {
    val cloudColor = Color(0xCCFFFFFF)
    val puffs = listOf(
        Offset(-scale * 0.7f, scale * 0.12f) to scale * 0.42f,
        Offset(-scale * 0.25f, -scale * 0.14f) to scale * 0.56f,
        Offset(scale * 0.32f, -scale * 0.08f) to scale * 0.50f,
        Offset(scale * 0.76f, scale * 0.14f) to scale * 0.38f
    )
    puffs.forEach { (off, r) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(cloudColor, Color(0x33FFFFFF), Color.Transparent),
                center = Offset(cx + off.x, cy + off.y),
                radius = r
            ),
            radius = r,
            center = Offset(cx + off.x, cy + off.y)
        )
    }
}

private fun DrawScope.drawDistantJungleSilhouette(
    w: Float,
    h: Float,
    horizonY: Float,
    color: Color,
    seed: Float
) {
    val trunkCount = 8
    for (i in 0 until trunkCount) {
        val tx = w * ((i + 0.4f) / trunkCount)
        val tw = w * (0.018f + (i % 3) * 0.006f)
        drawRect(
            color = color.copy(alpha = 0.55f),
            topLeft = Offset(tx, horizonY - h * 0.35f),
            size = Size(tw, h * 0.42f)
        )
    }
    val path = Path().apply {
        moveTo(0f, h)
        lineTo(0f, horizonY)
        val steps = 14
        for (i in 0..steps) {
            val frac = i / steps.toFloat()
            val px = frac * w
            val bump = sin((frac * 9f + seed).toDouble()).toFloat() * h * 0.045f +
                sin((frac * 21f + seed * 1.7f).toDouble()).toFloat() * h * 0.02f
            lineTo(px, horizonY - h * 0.09f + bump)
        }
        lineTo(w, h)
        close()
    }
    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0.85f), color.copy(alpha = 0.98f)),
            startY = horizonY - h * 0.15f,
            endY = horizonY + h * 0.2f
        )
    )
}

private fun DrawScope.drawSunlightRays(w: Float, h: Float) {
    val origin = Offset(w * 0.06f, h * 0.06f)
    val rays = listOf(
        Triple(0.24f, 0.38f, 0.34f),
        Triple(0.42f, 0.54f, 0.28f),
        Triple(0.58f, 0.72f, 0.22f)
    )
    rays.forEach { (startFrac, endFrac, alpha) ->
        val rayPath = Path().apply {
            moveTo(origin.x, origin.y)
            lineTo(w * startFrac, h * 0.65f)
            lineTo(w * endFrac, h * 0.65f)
            close()
        }
        drawPath(
            path = rayPath,
            brush = Brush.linearGradient(
                colors = listOf(
                    GamePalette.SunRayBright.copy(alpha = alpha),
                    GamePalette.SunRayWarm.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                start = origin,
                end = Offset(w * ((startFrac + endFrac) * 0.5f), h * 0.65f)
            )
        )
    }
}

private fun DrawScope.drawWoodenFenceSegment(
    startX: Float,
    topY: Float,
    width: Float,
    height: Float,
    posts: Int
) {
    val railH = height * 0.16f
    for (ry in listOf(topY + height * 0.25f, topY + height * 0.62f)) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(GamePalette.GoldenWoodHighlight, GamePalette.OrangeWood, GamePalette.MainWoodenBrown),
                startY = ry,
                endY = ry + railH
            ),
            topLeft = Offset(startX, ry),
            size = Size(width, railH),
            cornerRadius = CornerRadius(4f)
        )
        drawRoundRect(
            color = GamePalette.DarkWoodOutline,
            topLeft = Offset(startX, ry),
            size = Size(width, railH),
            cornerRadius = CornerRadius(4f),
            style = Stroke(2.2f)
        )
    }
    val postW = (width / (posts * 2.4f)).coerceIn(10f, 24f)
    for (i in 0 until posts) {
        val px = startX + width * ((i + 0.35f) / posts)
        val postPath = Path().apply {
            moveTo(px, topY + postW * 0.45f)
            lineTo(px + postW * 0.5f, topY)
            lineTo(px + postW, topY + postW * 0.45f)
            lineTo(px + postW, topY + height)
            lineTo(px, topY + height)
            close()
        }
        drawPath(
            postPath,
            Brush.horizontalGradient(
                colors = listOf(GamePalette.GoldenWoodHighlight, GamePalette.OrangeWood, GamePalette.DeepWoodBrown),
                startX = px,
                endX = px + postW
            )
        )
        drawPath(postPath, GamePalette.DarkWoodOutline, style = Stroke(2.2f, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawVillageHut(
    cx: Float,
    baseY: Float,
    hutWidth: Float,
    hutHeight: Float
) {
    val wallW = hutWidth * 0.74f
    val wallH = hutHeight * 0.52f
    val wallLeft = cx - wallW * 0.5f
    val wallTop = baseY - wallH

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.HutWallShadow, GamePalette.HutWallLight, GamePalette.HutWallShadow),
            startY = wallTop,
            endY = baseY
        ),
        topLeft = Offset(wallLeft, wallTop),
        size = Size(wallW, wallH)
    )
    val planks = 7
    for (i in 1 until planks) {
        val lx = wallLeft + wallW * (i / planks.toFloat())
        drawLine(
            color = GamePalette.MainWoodenBrown.copy(alpha = 0.45f),
            start = Offset(lx, wallTop),
            end = Offset(lx, baseY),
            strokeWidth = 1.6f
        )
    }
    drawRect(GamePalette.DarkWoodOutline, Offset(wallLeft, wallTop), Size(wallW, wallH), style = Stroke(2.6f))

    val winSize = minOf(wallW * 0.28f, wallH * 0.42f)
    val winLeft = wallLeft + wallW * 0.22f
    val winTop = wallTop + wallH * 0.24f
    drawRect(Color(0xFF472512), Offset(winLeft, winTop), Size(winSize, winSize))
    drawRect(Color(0xFF2B4C59), Offset(winLeft + 4f, winTop + 4f), Size(winSize - 8f, winSize - 8f))
    drawLine(GamePalette.MainWoodenBrown, Offset(winLeft + winSize * 0.5f, winTop), Offset(winLeft + winSize * 0.5f, winTop + winSize), 3.2f)
    drawLine(GamePalette.MainWoodenBrown, Offset(winLeft, winTop + winSize * 0.5f), Offset(winLeft + winSize, winTop + winSize * 0.5f), 3.2f)
    drawRect(GamePalette.DarkWoodOutline, Offset(winLeft, winTop), Size(winSize, winSize), style = Stroke(3.2f))

    val roofPeakY = baseY - hutHeight
    val roofBottomY = wallTop + hutHeight * 0.10f
    val roofPath = Path().apply {
        moveTo(cx, roofPeakY)
        quadraticTo(cx - hutWidth * 0.32f, roofPeakY + hutHeight * 0.20f, cx - hutWidth * 0.58f, roofBottomY)
        val teeth = 8
        for (i in 0..teeth) {
            val tx = (cx - hutWidth * 0.58f) + (hutWidth * 1.16f) * (i / teeth.toFloat())
            val ty = roofBottomY + (if (i % 2 == 0) 6f else -2f)
            lineTo(tx, ty)
        }
        quadraticTo(cx + hutWidth * 0.32f, roofPeakY + hutHeight * 0.20f, cx, roofPeakY)
        close()
    }
    drawPath(
        roofPath,
        Brush.verticalGradient(
            colors = listOf(GamePalette.ThatchLight, GamePalette.ThatchMid, GamePalette.ThatchShadow),
            startY = roofPeakY,
            endY = roofBottomY
        )
    )
    for (i in -4..4) {
        drawLine(
            color = GamePalette.ThatchShadow.copy(alpha = 0.55f),
            start = Offset(cx, roofPeakY + 6f),
            end = Offset(cx + i * hutWidth * 0.11f, roofBottomY - 2f),
            strokeWidth = 2f
        )
    }
    drawPath(roofPath, GamePalette.DarkWoodOutline, style = Stroke(2.8f, join = StrokeJoin.Round))
}

private fun DrawScope.drawSunlitDirtPath(w: Float, h: Float, topY: Float) {
    val path = Path().apply {
        moveTo(w * 0.34f, topY)
        quadraticTo(w * 0.26f, (topY + h) * 0.5f, w * 0.16f, h)
        lineTo(w * 0.84f, h)
        quadraticTo(w * 0.74f, (topY + h) * 0.5f, w * 0.66f, topY)
        close()
    }
    drawPath(
        path,
        Brush.verticalGradient(
            colors = listOf(GamePalette.DirtPathLight, GamePalette.DirtPathMid, GamePalette.DirtPathLight),
            startY = topY,
            endY = h
        )
    )
    drawOval(
        color = Color(0x55FFF7B8),
        topLeft = Offset(w * 0.34f, topY + (h - topY) * 0.25f),
        size = Size(w * 0.32f, (h - topY) * 0.45f)
    )
}

private fun DrawScope.drawFramingJungleTrees(w: Float, h: Float, isPortrait: Boolean) {
    val trunkW = if (isPortrait) w * 0.13f else w * 0.085f

    val leftTrunk = Path().apply {
        moveTo(-trunkW * 0.2f, h * 0.66f)
        cubicTo(trunkW * 0.25f, h * 0.42f, trunkW * 0.45f, h * 0.20f, trunkW * 1.45f, -h * 0.04f)
        lineTo(trunkW * 0.45f, -h * 0.04f)
        cubicTo(-trunkW * 0.1f, h * 0.18f, -trunkW * 0.25f, h * 0.40f, -trunkW * 0.45f, h * 0.66f)
        close()
    }
    drawTreeTrunkWithBark(leftTrunk, 0f, trunkW * 1.5f)

    val rightTrunk = Path().apply {
        moveTo(w + trunkW * 0.2f, h * 0.64f)
        cubicTo(w - trunkW * 0.25f, h * 0.42f, w - trunkW * 0.45f, h * 0.20f, w - trunkW * 1.45f, -h * 0.04f)
        lineTo(w - trunkW * 0.45f, -h * 0.04f)
        cubicTo(w + trunkW * 0.1f, h * 0.18f, w + trunkW * 0.25f, h * 0.40f, w + trunkW * 0.45f, h * 0.64f)
        close()
    }
    drawTreeTrunkWithBark(rightTrunk, w - trunkW * 1.5f, w)

    val leafScale = minOf(w, h) * (if (isPortrait) 0.16f else 0.18f)
    val leftCanopyPoints = listOf(
        Offset(w * 0.02f, h * 0.02f) to 25f,
        Offset(w * 0.12f, h * 0.01f) to 38f,
        Offset(w * 0.22f, h * 0.02f) to 48f,
        Offset(w * 0.04f, h * 0.13f) to 18f,
        Offset(w * 0.15f, h * 0.10f) to 32f
    )
    leftCanopyPoints.forEach { (pt, ang) ->
        drawLeafCluster(pt.x, pt.y, leafScale, ang, 4)
    }

    val rightCanopyPoints = listOf(
        Offset(w * 0.98f, h * 0.02f) to 155f,
        Offset(w * 0.88f, h * 0.01f) to 142f,
        Offset(w * 0.78f, h * 0.02f) to 132f,
        Offset(w * 0.96f, h * 0.13f) to 162f,
        Offset(w * 0.85f, h * 0.10f) to 148f
    )
    rightCanopyPoints.forEach { (pt, ang) ->
        drawLeafCluster(pt.x, pt.y, leafScale, ang, 4)
    }
}

private fun DrawScope.drawTreeTrunkWithBark(trunkPath: Path, startX: Float, endX: Float) {
    drawPath(
        path = trunkPath,
        brush = Brush.horizontalGradient(
            colors = listOf(GamePalette.DeepWoodBrown, GamePalette.MainWoodenBrown, GamePalette.OrangeWood),
            startX = startX,
            endX = endX
        )
    )
    clipPath(trunkPath) {
        val step = (endX - startX) / 5f
        for (i in 1..4) {
            val bx = startX + i * step
            drawLine(
                color = if (i % 2 == 0) GamePalette.DarkWoodOutline.copy(alpha = 0.55f) else GamePalette.GoldenWoodHighlight.copy(alpha = 0.35f),
                start = Offset(bx, 0f),
                end = Offset(bx - step * 0.4f, size.height * 0.65f),
                strokeWidth = 3f
            )
        }
    }
    drawPath(trunkPath, GamePalette.DarkWoodOutline, style = Stroke(3.2f, join = StrokeJoin.Round))
}

fun DrawScope.drawStonePlatform(
    cx: Float,
    topY: Float,
    platformWidth: Float,
    platformHeight: Float,
    topThickness: Float
) {
    val left = cx - platformWidth * 0.5f
    val right = cx + platformWidth * 0.5f
    val bottom = topY + platformHeight

    drawOval(
        color = Color(0x88153308),
        topLeft = Offset(left + platformWidth * 0.03f, bottom - platformHeight * 0.14f),
        size = Size(platformWidth * 0.94f, platformHeight * 0.24f)
    )

    val sidePath = Path().apply {
        moveTo(left + platformWidth * 0.05f, topY + topThickness * 0.5f)
        lineTo(left, topY + platformHeight * 0.45f)
        quadraticTo(left + platformWidth * 0.02f, bottom, left + platformWidth * 0.14f, bottom)
        lineTo(right - platformWidth * 0.14f, bottom)
        quadraticTo(right - platformWidth * 0.02f, bottom, right, topY + platformHeight * 0.45f)
        lineTo(right - platformWidth * 0.05f, topY + topThickness * 0.5f)
        close()
    }
    drawPath(
        path = sidePath,
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.StoneSideMid, GamePalette.StoneSideDark, Color(0xFF3A342B)),
            startY = topY + topThickness * 0.4f,
            endY = bottom
        )
    )
    clipPath(sidePath) {
        val rockFacets = 8
        for (i in 1 until rockFacets) {
            val rx = left + platformWidth * (i / rockFacets.toFloat())
            drawLine(
                color = GamePalette.StoneCrack.copy(alpha = 0.6f),
                start = Offset(rx, topY + topThickness * 0.5f),
                end = Offset(rx + (if (i % 2 == 0) 14f else -14f), bottom),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0x33F2E8D5),
                start = Offset(rx + 4f, topY + topThickness * 0.6f),
                end = Offset(rx + (if (i % 2 == 0) 18f else -10f), bottom - 10f),
                strokeWidth = 2f
            )
        }
    }
    drawPath(sidePath, GamePalette.StoneCrack, style = Stroke(3.2f, join = StrokeJoin.Round))

    val topSurfaceRect = Rect(
        left + platformWidth * 0.02f,
        topY - topThickness * 0.35f,
        right - platformWidth * 0.02f,
        topY + topThickness * 1.15f
    )
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(GamePalette.StoneTopLight, GamePalette.StoneTopMid, GamePalette.StoneSideMid),
            startY = topSurfaceRect.top,
            endY = topSurfaceRect.bottom
        ),
        topLeft = topSurfaceRect.topLeft,
        size = topSurfaceRect.size
    )
    drawOval(
        color = GamePalette.StoneCrack.copy(alpha = 0.75f),
        topLeft = topSurfaceRect.topLeft,
        size = topSurfaceRect.size,
        style = Stroke(width = 2.6f)
    )

    val mossSpots = listOf(0.10f, 0.24f, 0.39f, 0.58f, 0.74f, 0.88f)
    mossSpots.forEachIndexed { idx, frac ->
        val mx = left + platformWidth * frac
        val my = topY + topThickness * (0.72f + (idx % 2) * 0.14f)
        val mw = platformWidth * (0.065f + (idx % 3) * 0.015f)
        val mh = platformHeight * (0.14f + (idx % 2) * 0.07f)
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(GamePalette.BrightLeaf, GamePalette.LeafGreen, GamePalette.JungleGreen),
                startY = my - mh * 0.4f,
                endY = my + mh
            ),
            topLeft = Offset(mx - mw * 0.5f, my - mh * 0.35f),
            size = Size(mw, mh)
        )
        drawOval(
            color = GamePalette.DeepForestShadow.copy(alpha = 0.65f),
            topLeft = Offset(mx - mw * 0.5f, my - mh * 0.35f),
            size = Size(mw, mh),
            style = Stroke(1.6f)
        )
    }
}

private fun DrawScope.drawForegroundFloraAndFlowers(
    w: Float,
    h: Float,
    mode: JungleSceneMode
) {
    val unit = minOf(w, h)

    drawForegroundRock(w * 0.20f, h * 0.94f, unit * 0.16f, unit * 0.09f)
    drawForegroundRock(w * 0.84f, h * 0.92f, unit * 0.18f, unit * 0.10f)

    drawLeafCluster(w * 0.03f, h * 0.97f, unit * 0.24f, -48f, 5)
    drawLeafCluster(w * 0.97f, h * 0.97f, unit * 0.24f, -132f, 5)

    val flowerR = unit * (if (mode == JungleSceneMode.SplashPortrait) 0.095f else 0.088f)
    drawHibiscusFlower(w * 0.055f, h * 0.84f, flowerR, isYellowOrange = false)
    if (mode == JungleSceneMode.GameplayStonePlatform) {
        drawHibiscusFlower(w * 0.042f, h * 0.93f, flowerR * 0.85f, isYellowOrange = true)
        drawHibiscusFlower(w * 0.945f, h * 0.89f, flowerR * 0.88f, isYellowOrange = false)
    } else if (mode == JungleSceneMode.SplashPortrait) {
        drawHibiscusFlower(w * 0.075f, h * 0.74f, flowerR * 0.88f, isYellowOrange = false)
    } else {
        drawHibiscusFlower(w * 0.945f, h * 0.88f, flowerR * 0.92f, isYellowOrange = false)
    }
}

private fun DrawScope.drawForegroundRock(cx: Float, cy: Float, rw: Float, rh: Float) {
    val p = Path().apply {
        moveTo(cx - rw * 0.5f, cy + rh * 0.3f)
        cubicTo(cx - rw * 0.45f, cy - rh * 0.4f, cx - rw * 0.1f, cy - rh * 0.55f, cx + rw * 0.15f, cy - rh * 0.42f)
        cubicTo(cx + rw * 0.42f, cy - rh * 0.30f, cx + rw * 0.52f, cy + rh * 0.1f, cx + rw * 0.48f, cy + rh * 0.35f)
        close()
    }
    drawPath(
        p,
        Brush.verticalGradient(
            colors = listOf(GamePalette.StoneTopMid, GamePalette.StoneSideMid, GamePalette.StoneSideDark),
            startY = cy - rh * 0.55f,
            endY = cy + rh * 0.35f
        )
    )
    drawPath(p, GamePalette.StoneCrack, style = Stroke(2.4f, join = StrokeJoin.Round))
}
