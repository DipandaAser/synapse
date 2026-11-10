package com.aserdipanda.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import kotlin.math.min

/**
 * The Synapse glyph alone, in theme colors: the S stroke and the webhook node in [lineColor], the
 * incoming SMS node in [smsColor]. For illustrations; the brand mark is [SynapseLogo].
 */
@Composable
fun SynapseGlyph(lineColor: Color, smsColor: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        // Fits the glyph's 56 x 64 bounds into the canvas, centered.
        val fit = min(size.width / VIEW_WIDTH, size.height / VIEW_HEIGHT)
        translate(
            left = (size.width - VIEW_WIDTH * fit) / 2 - VIEW_LEFT * fit,
            top = (size.height - VIEW_HEIGHT * fit) / 2 - VIEW_TOP * fit,
        ) {
            scale(scale = fit, pivot = Offset.Zero) {
                drawPath(
                    path = GlyphPath,
                    color = lineColor,
                    style = Stroke(width = 7f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                drawCircle(color = smsColor, radius = 7f, center = Offset(68f, 34f))
                drawCircle(color = lineColor, radius = 7f, center = Offset(40f, 74f))
            }
        }
    }
}

// Same geometry as the launcher icon, on its 108 unit canvas.
private val GlyphPath = PathParser()
    .parsePathString("M68 34H50a10 10 0 0 0 0 20h8a10 10 0 0 1 0 20H40")
    .toPath()

private const val VIEW_LEFT = 26f
private const val VIEW_TOP = 22f
private const val VIEW_WIDTH = 56f
private const val VIEW_HEIGHT = 64f
