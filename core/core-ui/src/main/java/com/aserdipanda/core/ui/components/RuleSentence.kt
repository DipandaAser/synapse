package com.aserdipanda.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aserdipanda.core.ui.theme.mono

/** One piece of a rule sentence such as "When sender is 7777". */
sealed interface RulePart {
    /** Connecting words, like "When " or " and ". */
    data class Words(val text: String) : RulePart

    /** A field or the method, like "sender" or "POST". */
    data class Keyword(val text: String) : RulePart

    /** A value from the rule, like "7777". Shown in a mono chip. */
    data class Value(val text: String) : RulePart
}

/**
 * A trigger's rules as one sentence, the way trigger cards read them: keywords in medium
 * weight, values in mono chips that stay on one line.
 */
@Composable
fun RuleSentence(parts: List<RulePart>, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val valueStyle = MaterialTheme.typography.mono.toSpanStyle().copy(color = colors.onSurface)
    val keywordStyle = SpanStyle(fontWeight = FontWeight.Medium, color = colors.onSurface)
    val sentence = remember(parts, valueStyle, keywordStyle) {
        buildAnnotatedString {
            for (part in parts) {
                when (part) {
                    is RulePart.Words -> append(part.text)
                    is RulePart.Keyword -> withStyle(keywordStyle) { append(part.text) }
                    is RulePart.Value -> {
                        pushStringAnnotation(VALUE_TAG, part.text)
                        withStyle(valueStyle) {
                            // No-break spaces pad the chip.
                            append("$NBSP${part.text.unbreakable()}$NBSP")
                        }
                        pop()
                    }
                }
            }
        }
    }
    val chipColor = colors.surfaceContainerHighest
    val valueFontSize = valueStyle.fontSize
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = sentence,
        modifier = modifier.drawBehind {
            val result = layout ?: return@drawBehind
            val fontSize = valueFontSize.toPx()
            sentence.getStringAnnotations(VALUE_TAG, 0, sentence.length).forEach { value ->
                drawValueChip(result, value.start, value.end, chipColor, fontSize)
            }
        },
        color = colors.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
        onTextLayout = { layout = it },
    )
}

// Draws one rounded chip per line the value spans (one, unless the value is wider than the line).
private fun DrawScope.drawValueChip(
    layout: TextLayoutResult,
    start: Int,
    end: Int,
    color: Color,
    fontSize: Float,
) {
    for (line in layout.getLineForOffset(start)..layout.getLineForOffset(end - 1)) {
        val lineStart = maxOf(start, layout.getLineStart(line))
        val lineEnd = minOf(end, layout.getLineEnd(line))
        if (lineStart >= lineEnd) continue
        val first = layout.getBoundingBox(lineStart)
        val last = layout.getBoundingBox(lineEnd - 1)
        val left = minOf(first.left, last.left)
        val right = maxOf(first.right, last.right)
        // About the mono font's ascent and descent around the baseline, plus 1 dp.
        val baseline = layout.getLineBaseline(line)
        val top = baseline - fontSize * 1.05f - 1.dp.toPx()
        val bottom = baseline + fontSize * 0.27f + 1.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(right - left, bottom - top),
            cornerRadius = CornerRadius(6.dp.toPx()),
        )
    }
}

// Keeps a value on one line: spaces become no-break spaces, and word joiners stop ASCII
// punctuation, like the "/" in a URL, from offering a line break.
private fun String.unbreakable(): String = buildString {
    for (char in this@unbreakable) {
        when {
            char == ' ' -> append(NBSP)
            char.code < 128 && !char.isLetterOrDigit() -> append(WORD_JOINER).append(char).append(WORD_JOINER)
            else -> append(char)
        }
    }
}

private const val VALUE_TAG = "rule-value"
private const val NBSP = ' '
private const val WORD_JOINER = '⁠'
