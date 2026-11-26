package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PitchAnalysisEngine
import com.example.ui.theme.HanziSerifFontFamily

/**
 * Standard high-contrast Mandarin tone color palette:
 * 1st = Red, 2nd = Orange, 3rd = Green, 4th = Blue, Neutral = Gray.
 */
object MandarinTonePalette {
    val Tone1Red = Color(0xFFE11D48)
    val Tone2Orange = Color(0xFFEA580C)
    val Tone3Green = Color(0xFF15803D)
    val Tone4Blue = Color(0xFF2563EB)
    val Tone5Gray = Color(0xFF64748B)

    fun colorForTone(tone: Int): Color = when (tone) {
        1 -> Tone1Red
        2 -> Tone2Orange
        3 -> Tone3Green
        4 -> Tone4Blue
        else -> Tone5Gray
    }
}

/**
 * Splits a Pinyin string into syllable tokens and extracts tone numbers (1..5) for each syllable.
 */
fun extractPinyinSyllableTones(pinyin: String): List<Pair<String, Int>> {
    val tokens = pinyin.trim()
        .replace("，", " ")
        .replace("。", " ")
        .replace("？", " ")
        .replace("！", " ")
        .replace(",", " ")
        .replace(".", " ")
        .replace("?", " ")
        .replace("!", " ")
        .replace("'", " ")
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }

    return tokens.map { syl -> syl to PitchAnalysisEngine.extractToneNumber(syl) }
}

/**
 * Builds an AnnotatedString where every Hanzi character is automatically colored
 * according to the corresponding Pinyin syllable's Mandarin tone.
 */
fun buildToneColoredHanziString(
    hanzi: String,
    pinyin: String,
    defaultColor: Color
): AnnotatedString {
    val syllableTones = extractPinyinSyllableTones(pinyin)
    return buildAnnotatedString {
        var toneCursor = 0
        hanzi.forEach { ch ->
            val isHan = Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.HAN
            if (isHan && toneCursor < syllableTones.size) {
                val tone = syllableTones[toneCursor].second
                toneCursor++
                withStyle(SpanStyle(color = MandarinTonePalette.colorForTone(tone), fontWeight = FontWeight.Bold)) {
                    append(ch)
                }
            } else {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(ch)
                }
            }
        }
    }
}

/**
 * Builds an AnnotatedString where every Pinyin syllable is colored by its Mandarin tone.
 */
fun buildToneColoredPinyinString(pinyin: String): AnnotatedString {
    val tokens = pinyin.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return buildAnnotatedString {
        tokens.forEachIndexed { idx, token ->
            val tone = PitchAnalysisEngine.extractToneNumber(token)
            withStyle(SpanStyle(color = MandarinTonePalette.colorForTone(tone), fontWeight = FontWeight.SemiBold)) {
                append(token)
            }
            if (idx < tokens.lastIndex) append(" ")
        }
    }
}

@Composable
fun ToneColoredHanziPinyinBlock(
    hanzi: String,
    pinyin: String,
    modifier: Modifier = Modifier,
    hanziFontSize: TextUnit = 24.sp,
    showPinyin: Boolean = true,
    enabled: Boolean = true
) {
    val fallbackColor = MaterialTheme.colorScheme.onSurface
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (enabled && pinyin.isNotBlank()) {
            Text(
                text = buildToneColoredHanziString(hanzi, pinyin, fallbackColor),
                fontFamily = HanziSerifFontFamily,
                fontSize = hanziFontSize,
                fontWeight = FontWeight.Bold
            )
            if (showPinyin) {
                Text(
                    text = buildToneColoredPinyinString(pinyin),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Text(
                text = hanzi,
                fontFamily = HanziSerifFontFamily,
                fontSize = hanziFontSize,
                fontWeight = FontWeight.Bold,
                color = fallbackColor
            )
            if (showPinyin && pinyin.isNotBlank()) {
                Text(
                    text = pinyin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToneColorLegendRow(modifier: Modifier = Modifier) {
    val items = listOf(
        "1st ā" to MandarinTonePalette.Tone1Red,
        "2nd á" to MandarinTonePalette.Tone2Orange,
        "3rd ǎ" to MandarinTonePalette.Tone3Green,
        "4th à" to MandarinTonePalette.Tone4Blue,
        "5th a" to MandarinTonePalette.Tone5Gray
    )
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEach { (label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    modifier = Modifier.size(8.dp),
                    shape = RoundedCornerShape(50),
                    color = color
                ) {}
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}
