package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.HskBadgeRank
import com.example.model.HskLevelBadgeProgress
import com.example.model.HskProfileShowcase
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class HskBadgePalette(
    val primary: Color,
    val accent: Color,
    val deep: Color
)

fun hskLevelPalette(level: Int): HskBadgePalette = when (level) {
    1 -> HskBadgePalette(JadePrimary, JadeVibrant, JadeDeep)
    2 -> HskBadgePalette(Color(0xFF0D9488), Color(0xFF2DD4BF), Color(0xFF115E59))
    3 -> HskBadgePalette(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF1E3A8A))
    4 -> HskBadgePalette(IndigoAccent, Color(0xFF818CF8), Color(0xFF312E81))
    5 -> HskBadgePalette(Color(0xFF9333EA), Color(0xFFC084FC), Color(0xFF581C87))
    6 -> HskBadgePalette(CoralVermilion, Color(0xFFF59E0B), Color(0xFF7C2D12))
    else -> HskBadgePalette(Color(0xFFD97706), GoldBright, Color(0xFF1F2937))
}

fun rankRimColor(rank: HskBadgeRank): Color = when (rank) {
    HskBadgeRank.LOCKED -> Color(0xFF94A3B8)
    HskBadgeRank.BRONZE -> Color(0xFFCD7F32)
    HskBadgeRank.SILVER -> Color(0xFF94A3B8)
    HskBadgeRank.GOLD -> GoldAccent
    HskBadgeRank.JADE_MASTER -> JadeVibrant
}

@Composable
fun HskMedallionBadge(
    badge: HskLevelBadgeProgress,
    size: Dp = 76.dp,
    modifier: Modifier = Modifier
) {
    val palette = hskLevelPalette(badge.level)
    val rimColor = rankRimColor(badge.rank)
    val isUnlocked = badge.isUnlocked
    val animatedProgress by animateFloatAsState(
        targetValue = badge.progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650),
        label = "hsk_badge_arc_${badge.level}"
    )
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    val lockedFill = MaterialTheme.colorScheme.surfaceVariant
    val lockedStroke = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val canvasSize = this.size.minDimension
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ringStroke = canvasSize * 0.082f
            val inset = ringStroke / 2f + 2f
            val arcSize = Size(this.size.width - inset * 2f, this.size.height - inset * 2f)

            // Outer progress track
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(
                    width = ringStroke,
                    cap = StrokeCap.Round,
                    pathEffect = if (!isUnlocked) PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) else null
                )
            )

            // Active progress arc
            if (animatedProgress > 0f || isUnlocked) {
                val sweep = (360f * animatedProgress).coerceAtLeast(if (isUnlocked) 18f else 0f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(palette.primary, palette.accent, GoldBright, palette.primary),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = ringStroke, cap = StrokeCap.Round)
                )
            }

            // Inner imperial octagonal medallion
            val octagonRadius = (canvasSize / 2f) - ringStroke - 5.dp.toPx()
            val octagonPath = Path().apply {
                val sides = 8
                for (i in 0 until sides) {
                    val angle = (2.0 * PI * i / sides) - (PI / 8.0)
                    val x = center.x + (octagonRadius * cos(angle)).toFloat()
                    val y = center.y + (octagonRadius * sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }

            if (isUnlocked) {
                drawPath(
                    path = octagonPath,
                    brush = Brush.radialGradient(
                        colors = listOf(palette.accent, palette.primary, palette.deep),
                        center = Offset(center.x - octagonRadius * 0.25f, center.y - octagonRadius * 0.25f),
                        radius = octagonRadius * 1.6f
                    )
                )
                drawPath(
                    path = octagonPath,
                    color = rimColor,
                    style = Stroke(width = 2.2.dp.toPx())
                )
            } else {
                drawPath(path = octagonPath, color = lockedFill)
                drawPath(
                    path = octagonPath,
                    color = lockedStroke,
                    style = Stroke(width = 1.4.dp.toPx())
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = badge.sealCharacter,
                fontFamily = HanziSerifFontFamily,
                fontSize = (size.value * 0.30f).sp,
                fontWeight = FontWeight.Black,
                color = if (isUnlocked) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            )
            Text(
                text = if (badge.level >= 7) "7–9" else "L${badge.level}",
                fontSize = (size.value * 0.13f).sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUnlocked) Color.White.copy(alpha = 0.92f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun HskBadgeStarsRow(
    stars: Int,
    starSize: Dp = 13.dp
) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..4) {
            Icon(
                imageVector = if (i <= stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                contentDescription = null,
                tint = if (i <= stars) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun HskBadgesMiniStripCard(
    showcase: HskProfileShowcase,
    onOpenProfileBadges: () -> Unit,
    onSelectBadgeLevel: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenProfileBadges() }
            .testTag("hsk_badges_mini_strip_card"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MilitaryTech,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "HSK 3.0 Mastery Badges",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${showcase.learnerTitle} (${showcase.learnerTitleZh}) · ${showcase.unlockedLevelBadgesCount}/7 unlocked",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GoldContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Profile",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldOnContainer,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Open profile and achievements",
                            tint = GoldOnContainer,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(showcase.levelBadges, key = { it.levelKey }) { badge ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectBadgeLevel(badge.levelKey) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("mini_hsk_badge_${badge.levelKey}")
                    ) {
                        HskMedallionBadge(badge = badge, size = 58.dp)
                        Text(
                            text = badge.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${badge.percentInt}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (badge.isUnlocked) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
