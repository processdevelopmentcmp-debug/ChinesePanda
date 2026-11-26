package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SandhiToneTile
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.Tone1Red
import com.example.ui.theme.Tone2Green
import com.example.ui.theme.Tone3Blue
import com.example.ui.theme.Tone4Purple
import com.example.ui.theme.ToneNeutralGray

@Composable
fun PandaMascotAvatar(
    size: Dp = 44.dp,
    animated: Boolean = true,
    showScholarHat: Boolean = true,
    modifier: Modifier = Modifier
) {
    val scale = if (animated) {
        val transition = rememberInfiniteTransition(label = "panda_pulse")
        val anim by transition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "panda_scale"
        )
        anim
    } else 1f

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            .semantics { contentDescription = "Panda Laoshi mascot" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.86f)) {
            val w = this.size.width
            val h = this.size.height
            val dark = Color(0xFF1E293B)

            // Ears
            drawCircle(dark, radius = w * 0.145f, center = Offset(w * 0.24f, h * 0.26f))
            drawCircle(dark, radius = w * 0.145f, center = Offset(w * 0.76f, h * 0.26f))

            // Face
            drawCircle(Color.White, radius = w * 0.37f, center = Offset(w * 0.50f, h * 0.55f))
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = w * 0.37f,
                center = Offset(w * 0.50f, h * 0.55f),
                style = Stroke(width = w * 0.02f)
            )

            // Eye patches
            rotate(-14f, pivot = Offset(w * 0.34f, h * 0.51f)) {
                drawOval(
                    color = dark,
                    topLeft = Offset(w * 0.23f, h * 0.38f),
                    size = Size(w * 0.22f, h * 0.26f)
                )
            }
            rotate(14f, pivot = Offset(w * 0.66f, h * 0.51f)) {
                drawOval(
                    color = dark,
                    topLeft = Offset(w * 0.55f, h * 0.38f),
                    size = Size(w * 0.22f, h * 0.26f)
                )
            }

            // Eyes
            drawCircle(Color.White, radius = w * 0.045f, center = Offset(w * 0.36f, h * 0.49f))
            drawCircle(Color.White, radius = w * 0.045f, center = Offset(w * 0.64f, h * 0.49f))
            drawCircle(Color.Black, radius = w * 0.022f, center = Offset(w * 0.37f, h * 0.49f))
            drawCircle(Color.Black, radius = w * 0.022f, center = Offset(w * 0.63f, h * 0.49f))

            // Rosy cheeks
            drawOval(
                color = Color(0xFFF472B6).copy(alpha = 0.48f),
                topLeft = Offset(w * 0.19f, h * 0.59f),
                size = Size(w * 0.12f, h * 0.065f)
            )
            drawOval(
                color = Color(0xFFF472B6).copy(alpha = 0.48f),
                topLeft = Offset(w * 0.69f, h * 0.59f),
                size = Size(w * 0.12f, h * 0.065f)
            )

            // Nose
            drawOval(
                color = dark,
                topLeft = Offset(w * 0.435f, h * 0.57f),
                size = Size(w * 0.13f, h * 0.08f)
            )

            // Smile
            val smile = Path().apply {
                moveTo(w * 0.43f, h * 0.67f)
                quadraticTo(w * 0.50f, h * 0.74f, w * 0.57f, h * 0.67f)
            }
            drawPath(smile, color = dark, style = Stroke(width = w * 0.03f, cap = StrokeCap.Round))

            // Scholar hat diamond accent
            if (showScholarHat) {
                val hat = Path().apply {
                    moveTo(w * 0.50f, h * 0.11f)
                    lineTo(w * 0.68f, h * 0.21f)
                    lineTo(w * 0.50f, h * 0.28f)
                    lineTo(w * 0.32f, h * 0.21f)
                    close()
                }
                drawPath(hat, color = GoldAccent)
            }
        }
    }
}

fun toneColor(tone: Int): Color = when (tone) {
    1 -> Tone1Red
    2 -> Tone2Green
    3 -> Tone3Blue
    4 -> Tone4Purple
    else -> ToneNeutralGray
}

fun toneLabel(tone: Int): String = when (tone) {
    1 -> "T1 ˉ"
    2 -> "T2 ˊ"
    3 -> "T3 ˇ"
    4 -> "T4 ˋ"
    else -> "T5 ·"
}

@Composable
fun SandhiToneTileCard(tile: SandhiToneTile, modifier: Modifier = Modifier) {
    val color = toneColor(tile.spokenTone)
    val changed = tile.basePinyin != tile.spokenPinyin
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tile.char,
                fontFamily = HanziSerifFontFamily,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = tile.spokenPinyin,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.ExtraBold
            )
            if (changed) {
                Text(
                    text = "(from ${tile.basePinyin})",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = toneLabel(tile.spokenTone),
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
fun HskLevelPill(level: Int, modifier: Modifier = Modifier) {
    val bg = when {
        level <= 2 -> MaterialTheme.colorScheme.primaryContainer
        level <= 4 -> MaterialTheme.colorScheme.secondaryContainer
        level <= 6 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> Tone4Purple.copy(alpha = 0.16f)
    }
    val fg = when {
        level <= 2 -> MaterialTheme.colorScheme.onPrimaryContainer
        level <= 4 -> MaterialTheme.colorScheme.onSecondaryContainer
        level <= 6 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> Tone4Purple
    }
    val label = if (level >= 7) "HSK 7–9" else "HSK $level"
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PandaActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = JadePrimary,
    contentColor: Color = Color.White,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun StatBadgePill(
    label: String,
    bgColor: Color,
    fgColor: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, fgColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            icon()
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = fgColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
