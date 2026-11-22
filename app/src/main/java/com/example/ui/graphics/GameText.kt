package com.example.ui.graphics

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.ui.theme.GamePalette

object GameFontCache {
    @Volatile
    private var lilitaTypeface: Typeface? = null

    @Volatile
    private var fredokaTypeface: Typeface? = null

    fun lilita(context: Context): Typeface {
        return lilitaTypeface ?: synchronized(this) {
            lilitaTypeface ?: (ResourcesCompat.getFont(context, R.font.lilita_one) ?: Typeface.DEFAULT_BOLD)
                .also { lilitaTypeface = it }
        }
    }

    fun fredoka(context: Context): Typeface {
        return fredokaTypeface ?: synchronized(this) {
            fredokaTypeface ?: (ResourcesCompat.getFont(context, R.font.fredoka) ?: Typeface.DEFAULT_BOLD)
                .also { fredokaTypeface = it }
        }
    }
}

fun DrawScope.drawOutlinedCartoonText(
    text: String,
    cx: Float,
    baselineY: Float,
    textSizePx: Float,
    typeface: Typeface,
    topColor: Color = GamePalette.TextWhite,
    bottomColor: Color = GamePalette.TextWhite,
    outlineColor: Color = GamePalette.DarkTextOutline,
    outlineWidthPx: Float = textSizePx * 0.16f,
    shadowOffsetY: Float = textSizePx * 0.08f,
    align: Paint.Align = Paint.Align.CENTER,
    skewX: Float = 0f,
    letterSpacing: Float = 0.01f
) {
    val native = drawContext.canvas.nativeCanvas

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = align
        this.textSkewX = skewX
        this.letterSpacing = letterSpacing
        this.style = Paint.Style.FILL_AND_STROKE
        this.strokeWidth = outlineWidthPx * 1.05f
        this.strokeJoin = Paint.Join.ROUND
        this.strokeCap = Paint.Cap.ROUND
        this.color = Color(0x99280C02).toArgb()
    }
    native.drawText(text, cx, baselineY + shadowOffsetY, shadowPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = align
        this.textSkewX = skewX
        this.letterSpacing = letterSpacing
        this.style = Paint.Style.STROKE
        this.strokeWidth = outlineWidthPx
        this.strokeJoin = Paint.Join.ROUND
        this.strokeCap = Paint.Cap.ROUND
        this.color = outlineColor.toArgb()
    }
    native.drawText(text, cx, baselineY, strokePaint)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = align
        this.textSkewX = skewX
        this.letterSpacing = letterSpacing
        this.style = Paint.Style.FILL
        if (topColor == bottomColor) {
            this.color = topColor.toArgb()
        } else {
            this.shader = LinearGradient(
                0f,
                baselineY - textSizePx * 0.85f,
                0f,
                baselineY + textSizePx * 0.12f,
                topColor.toArgb(),
                bottomColor.toArgb(),
                Shader.TileMode.CLAMP
            )
        }
    }
    native.drawText(text, cx, baselineY, fillPaint)
}

fun DrawScope.drawArchedCartoonText(
    text: String,
    cx: Float,
    centerY: Float,
    arcWidth: Float,
    archLift: Float,
    textSizePx: Float,
    typeface: Typeface,
    topColor: Color = GamePalette.SpecularGold,
    midColor: Color = GamePalette.BrightGold,
    bottomColor: Color = GamePalette.OrangeWood,
    outlineColor: Color = GamePalette.DarkTextOutline
) {
    val native = drawContext.canvas.nativeCanvas
    val path = Path().apply {
        moveTo(cx - arcWidth * 0.5f, centerY + archLift * 0.45f)
        quadTo(cx, centerY - archLift, cx + arcWidth * 0.5f, centerY + archLift * 0.45f)
    }

    val strokeW = textSizePx * 0.18f

    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = Paint.Align.CENTER
        this.style = Paint.Style.FILL_AND_STROKE
        this.strokeWidth = strokeW * 1.1f
        this.strokeJoin = Paint.Join.ROUND
        this.color = Color(0xAA260C02).toArgb()
    }
    native.drawTextOnPath(text, path, 0f, textSizePx * 0.11f, shadowPaint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = Paint.Align.CENTER
        this.style = Paint.Style.STROKE
        this.strokeWidth = strokeW
        this.strokeJoin = Paint.Join.ROUND
        this.color = outlineColor.toArgb()
    }
    native.drawTextOnPath(text, path, 0f, 0f, strokePaint)

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
        this.textAlign = Paint.Align.CENTER
        this.style = Paint.Style.FILL
        this.shader = LinearGradient(
            0f,
            centerY - archLift - textSizePx * 0.6f,
            0f,
            centerY + textSizePx * 0.5f,
            intArrayOf(topColor.toArgb(), midColor.toArgb(), bottomColor.toArgb()),
            floatArrayOf(0f, 0.55f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    native.drawTextOnPath(text, path, 0f, 0f, fillPaint)
}

@Composable
fun OutlinedGameText(
    text: String,
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    topColor: Color = GamePalette.TextWhite,
    bottomColor: Color = GamePalette.TextWhite,
    outlineColor: Color = GamePalette.DarkTextOutline,
    outlineScale: Float = 0.16f,
    useDisplayFont: Boolean = true,
    skewX: Float = 0f,
    minHeight: Dp = 24.dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val typeface = remember(context, useDisplayFont) {
        if (useDisplayFont) GameFontCache.lilita(context) else GameFontCache.fredoka(context)
    }
    val textSizePx = with(density) { fontSize.toPx() }

    Canvas(modifier = modifier.defaultMinSize(minWidth = 24.dp, minHeight = minHeight)) {
        val baseline = size.height * 0.5f + textSizePx * 0.34f
        drawOutlinedCartoonText(
            text = text,
            cx = size.width * 0.5f,
            baselineY = baseline,
            textSizePx = textSizePx,
            typeface = typeface,
            topColor = topColor,
            bottomColor = bottomColor,
            outlineColor = outlineColor,
            outlineWidthPx = textSizePx * outlineScale,
            skewX = skewX
        )
    }
}

/**
 * Draws the multi-colored "Level 01 / 50" text matching Screen 02 Top-Left HUD.
 */
fun DrawScope.drawLevelHudText(
    levelNumber: Int,
    totalLevels: Int,
    cx: Float,
    cy: Float,
    textSizePx: Float,
    typeface: Typeface
) {
    val part1 = "Level "
    val part2 = "%02d".format(levelNumber)
    val part3 = " / $totalLevels"

    val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
    }
    val w1 = measurePaint.measureText(part1)
    val w2 = measurePaint.measureText(part2)
    val w3 = measurePaint.measureText(part3)
    val totalW = w1 + w2 + w3
    val startX = cx - totalW * 0.5f
    val baseline = cy + textSizePx * 0.34f

    drawOutlinedCartoonText(
        text = part1,
        cx = startX,
        baselineY = baseline,
        textSizePx = textSizePx,
        typeface = typeface,
        topColor = GamePalette.PureWhite,
        bottomColor = GamePalette.TextWhite,
        align = Paint.Align.LEFT
    )
    drawOutlinedCartoonText(
        text = part2,
        cx = startX + w1,
        baselineY = baseline,
        textSizePx = textSizePx * 1.06f,
        typeface = typeface,
        topColor = GamePalette.BrightLeaf,
        bottomColor = GamePalette.SuccessGreen,
        align = Paint.Align.LEFT
    )
    drawOutlinedCartoonText(
        text = part3,
        cx = startX + w1 + w2 * 1.04f,
        baselineY = baseline,
        textSizePx = textSizePx,
        typeface = typeface,
        topColor = GamePalette.PureWhite,
        bottomColor = GamePalette.TextWhite,
        align = Paint.Align.LEFT
    )
}

/**
 * Draws "Where is the ball?" with golden italic emphasis on "ball", matching Screen 02.
 */
fun DrawScope.drawQuestionBannerText(
    cx: Float,
    cy: Float,
    textSizePx: Float,
    typeface: Typeface
) {
    val p1 = "Where is the "
    val p2 = "ball"
    val p3 = "?"

    val paintNormal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx
    }
    val paintBall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSizePx * 1.05f
        this.textSkewX = -0.18f
    }
    val w1 = paintNormal.measureText(p1)
    val w2 = paintBall.measureText(p2)
    val w3 = paintNormal.measureText(p3)
    val totalW = w1 + w2 + w3
    val startX = cx - totalW * 0.5f
    val baseline = cy + textSizePx * 0.34f

    drawOutlinedCartoonText(
        text = p1,
        cx = startX,
        baselineY = baseline,
        textSizePx = textSizePx,
        typeface = typeface,
        topColor = GamePalette.PureWhite,
        bottomColor = GamePalette.TextWhite,
        align = Paint.Align.LEFT
    )
    drawOutlinedCartoonText(
        text = p2,
        cx = startX + w1,
        baselineY = baseline,
        textSizePx = textSizePx * 1.05f,
        typeface = typeface,
        topColor = GamePalette.SpecularGold,
        bottomColor = GamePalette.BrightGold,
        align = Paint.Align.LEFT,
        skewX = -0.18f
    )
    drawOutlinedCartoonText(
        text = p3,
        cx = startX + w1 + w2 + textSizePx * 0.04f,
        baselineY = baseline,
        textSizePx = textSizePx,
        typeface = typeface,
        topColor = GamePalette.PureWhite,
        bottomColor = GamePalette.TextWhite,
        align = Paint.Align.LEFT
    )
}
