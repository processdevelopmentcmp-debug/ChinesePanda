package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.DailyTrackerSnapshot
import com.example.model.HskLevelBadgeProgress
import com.example.model.HskProfileShowcase
import com.example.model.LearnerAchievement
import com.example.model.UserProfile
import com.example.ui.components.HskBadgeStarsRow
import com.example.ui.components.HskMedallionBadge
import com.example.ui.components.PandaMascotAvatar
import com.example.ui.components.hskLevelPalette
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileAchievementsScreen(
    profile: UserProfile,
    userEmail: String,
    dailyTracker: DailyTrackerSnapshot,
    showcase: HskProfileShowcase,
    onUpdateTargetHskLevel: (Int) -> Unit,
    onPracticeHskLevel: (String) -> Unit,
    onOpenWordGoalSetter: () -> Unit,
    onOpenDailyTracker: () -> Unit,
    onOpenFontSettings: () -> Unit,
    onSignOut: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var selectedBadgeDetail by remember { mutableStateOf<HskLevelBadgeProgress?>(null) }
    // 0 = All, 1 = Unlocked, 2 = In Progress
    var achievementFilter by remember { mutableIntStateOf(0) }
    val numFmt = remember { NumberFormat.getIntegerInstance(Locale.US) }

    val filteredAchievements = remember(showcase.achievements, achievementFilter) {
        when (achievementFilter) {
            1 -> showcase.achievements.filter { it.isUnlocked }
            2 -> showcase.achievements.filter { !it.isUnlocked }
            else -> showcase.achievements
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_achievements_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header Bar
        item(key = "header_bar") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(R.string.profile_achievements_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = stringResource(R.string.profile_achievements_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Imperial Hero Profile Card
        item(key = "hero_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(JadeDeep, JadePrimary, Color(0xFF0A4F48))
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            border = BorderStroke(2.dp, GoldBright),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                PandaMascotAvatar(size = 56.dp, animated = false)
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (userEmail.isNotBlank()) {
                                Text(
                                    text = userEmail,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = GoldBright
                                ) {
                                    Text(
                                        text = "${showcase.learnerTitleZh} · ${showcase.learnerTitle}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GoldOnContainer,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 4-Stat Trophy Ribbon
                    val bestStreak = maxOf(profile.streak.toInt(), dailyTracker.currentStreakDays, dailyTracker.bestStreakDays)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeroStatBox(numFmt.format(profile.xp), "Total XP", Modifier.weight(1f))
                        HeroStatBox("${bestStreak}d", "Best Streak", Modifier.weight(1f))
                        HeroStatBox(numFmt.format(showcase.totalLearnedWords), "Learned", Modifier.weight(1f))
                        HeroStatBox("${showcase.unlockedLevelBadgesCount}/7", "HSK Badges", Modifier.weight(1f))
                    }

                    // Overall HSK 3.0 Mastery Bar
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HSK 3.0 Lexicon Mastery",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${numFmt.format(showcase.totalLearnedWords)} / ${numFmt.format(showcase.totalCatalogWords)} words",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldBright,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { showcase.overallCatalogProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = GoldBright,
                            trackColor = Color.White.copy(alpha = 0.22f)
                        )
                    }
                }
            }
        }

        // HSK 3.0 Visual Progress Badges Section Header
        item(key = "hsk_badges_header") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "HSK 3.0 PROGRESS BADGES",
                        style = MaterialTheme.typography.labelSmall,
                        color = JadePrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Collect Bronze → Silver → Gold → Jade Master",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = JadeLightContainer
                ) {
                    Text(
                        text = "${showcase.unlockedLevelBadgesCount} / 7",
                        style = MaterialTheme.typography.labelSmall,
                        color = JadeDeep,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2-Column Visual HSK Badge Medallion Grid
        val badgeRows = showcase.levelBadges.chunked(2)
        items(badgeRows, key = { row -> row.joinToString("_") { it.levelKey } }) { rowPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPair.forEach { badge ->
                    HskBadgeGridCard(
                        badge = badge,
                        numFmt = numFmt,
                        onClick = { selectedBadgeDetail = badge },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowPair.size == 1) {
                    // Highlight summary card beside HSK 7–9 so the 7-item grid is visually balanced
                    HskCollectionSummaryTile(
                        showcase = showcase,
                        onPracticeTopLevel = {
                            onBack()
                            onPracticeHskLevel("${profile.currentHskLevel.coerceIn(1, 6)}")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Milestone Achievements Trophy Case
        item(key = "achievements_header") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                        Text(
                            text = "ACHIEVEMENTS (${showcase.unlockedAchievementsCount}/${showcase.achievements.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Unlocked", "In Progress").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = achievementFilter == idx,
                            onClick = { achievementFilter = idx },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JadePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("achievement_filter_$idx")
                        )
                    }
                }
            }
        }

        if (filteredAchievements.isEmpty()) {
            item(key = "achievements_empty") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = if (achievementFilter == 1) {
                            "Complete your first HSK lesson or hit a daily goal to unlock your first trophy!"
                        } else {
                            "You have unlocked every single achievement — incredible dedication!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }
            }
        } else {
            items(filteredAchievements, key = { it.id }) { achievement ->
                AchievementTrophyRow(achievement = achievement)
            }
        }

        // Preferences, Target HSK Level & Account Actions
        item(key = "settings_and_account") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "STUDY PREFERENCES & ACCOUNT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Target HSK 3.0 Level (Current: HSK ${profile.currentHskLevel})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1, 2, 3, 4, 5, 6, 7).forEach { lvl ->
                            FilterChip(
                                selected = profile.currentHskLevel.toInt() == lvl,
                                onClick = { onUpdateTargetHskLevel(lvl) },
                                label = { Text(if (lvl == 7) "HSK 7–9" else "HSK $lvl") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JadePrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onOpenWordGoalSetter,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("${dailyTracker.wordsGoal}/day Goal", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        FilledTonalButton(
                            onClick = onOpenDailyTracker,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Insights, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Daily Tracker", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onOpenFontSettings,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.FormatSize, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Font & Theme")
                        }
                        OutlinedButton(
                            onClick = onSignOut,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sign_out_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, tint = CoralVermilion, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(5.dp))
                            Text(stringResource(R.string.sign_out), color = CoralVermilion)
                        }
                    }
                }
            }
        }

        item(key = "bottom_spacer") {
            Spacer(Modifier.height(12.dp))
        }
    }

    // Detailed HSK Badge Modal Dialog
    val detailBadge = selectedBadgeDetail
    if (detailBadge != null) {
        HskBadgeDetailDialog(
            badge = detailBadge,
            numFmt = numFmt,
            onPracticeLevel = {
                selectedBadgeDetail = null
                onBack()
                onPracticeHskLevel(detailBadge.levelKey)
            },
            onDismiss = { selectedBadgeDetail = null }
        )
    }
}

@Composable
private fun HeroStatBox(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HskBadgeGridCard(
    badge: HskLevelBadgeProgress,
    numFmt: NumberFormat,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = hskLevelPalette(badge.level)
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("profile_hsk_badge_${badge.levelKey}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (badge.isUnlocked) 2.dp else 0.dp,
        border = BorderStroke(
            width = if (badge.isUnlocked) 1.5.dp else 1.dp,
            color = if (badge.isUnlocked) palette.primary.copy(alpha = 0.55f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HskMedallionBadge(badge = badge, size = 76.dp)

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = badge.emblemTitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (badge.isUnlocked) palette.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HskBadgeStarsRow(stars = badge.rank.stars)

            Surface(
                shape = RoundedCornerShape(50),
                color = if (badge.isUnlocked) palette.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${badge.rank.label} · ${badge.percentInt}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) palette.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                )
            }

            Text(
                text = "${numFmt.format(badge.learnedCount)} / ${numFmt.format(badge.totalWords)} words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HskCollectionSummaryTile(
    showcase: HskProfileShowcase,
    onPracticeTopLevel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = GoldContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.2.dp, GoldAccent.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GoldBright),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = GoldOnContainer,
                    modifier = Modifier.size(30.dp)
                )
            }
            Text(
                text = "Rank Up!",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = GoldOnContainer
            )
            Text(
                text = "${showcase.totalStrongWords} Strong SRS words mastered across HSK 1–9.",
                style = MaterialTheme.typography.bodySmall,
                color = GoldOnContainer.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onPracticeTopLevel,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Study", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AchievementTrophyRow(achievement: LearnerAchievement) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("achievement_card_${achievement.id}"),
        shape = RoundedCornerShape(18.dp),
        color = if (achievement.isUnlocked) GoldContainer.copy(alpha = 0.42f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (achievement.isUnlocked) 1.5.dp else 1.dp,
            color = if (achievement.isUnlocked) GoldAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (achievement.isUnlocked) GoldAccent else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (achievement.isUnlocked) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "In progress",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = achievement.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (achievement.isUnlocked) JadeLightContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = achievement.chineseIdiom,
                                fontFamily = HanziSerifFontFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (achievement.isUnlocked) JadeDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${achievement.current.coerceAtMost(achievement.target)}/${achievement.target}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (achievement.isUnlocked) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LinearProgressIndicator(
                    progress = { achievement.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (achievement.isUnlocked) JadePrimary else GoldAccent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HskBadgeDetailDialog(
    badge: HskLevelBadgeProgress,
    numFmt: NumberFormat,
    onPracticeLevel: () -> Unit,
    onDismiss: () -> Unit
) {
    val nextRank = badge.rank.nextRank()
    val palette = hskLevelPalette(badge.level)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { HskMedallionBadge(badge = badge, size = 104.dp) },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${badge.label} · ${badge.emblemTitle}",
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                HskBadgeStarsRow(stars = badge.rank.stars, starSize = 16.dp)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${badge.bandName} Band · Current Rank: ${badge.rank.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.primary,
                    fontWeight = FontWeight.Bold
                )
                LinearProgressIndicator(
                    progress = { badge.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(50)),
                    color = palette.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    text = "${numFmt.format(badge.learnedCount)} / ${numFmt.format(badge.totalWords)} words learned (${badge.percentInt}%)\n${numFmt.format(badge.strongCount)} Strong SRS mastery words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (nextRank != null) {
                            "Acquire ${numFmt.format(badge.wordsToNextRank)} more ${badge.label} word(s) to unlock ${nextRank.label}!"
                        } else {
                            "Highest rank achieved! You are a ${badge.label} Jade Master."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onPracticeLevel,
                colors = ButtonDefaults.buttonColors(containerColor = JadePrimary),
                modifier = Modifier.testTag("badge_detail_practice_button")
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Practice ${badge.label}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
