package com.example.ui.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveAnkiSession
import com.example.model.LearnedWordEntry
import com.example.model.Sm2Calculator
import com.example.model.VocabWord
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant

@Composable
fun AnkiReviewSessionView(
    session: ActiveAnkiSession,
    onSpeak: (String, Float) -> Unit,
    onRateCard: (LearnedWordEntry, Int) -> Unit,
    onOpenWordBreakdown: (VocabWord) -> Unit,
    onPracticeWrite: (VocabWord) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    // Anki-style active queue: "Again" cards re-enter the queue 2-3 positions ahead
    val queue = remember(session.id) {
        mutableStateListOf<LearnedWordEntry>().apply { addAll(session.entries) }
    }
    var completedCount by remember(session.id) { mutableIntStateOf(0) }
    var againCount by remember(session.id) { mutableIntStateOf(0) }
    var hardCount by remember(session.id) { mutableIntStateOf(0) }
    var goodCount by remember(session.id) { mutableIntStateOf(0) }
    var easyCount by remember(session.id) { mutableIntStateOf(0) }
    var answerRevealed by remember(session.id) { mutableStateOf(false) }

    // Anki in-session Settings (matching Screenshot 6 top-right "Settings")
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showPinyinOnFront by remember { mutableStateOf(true) }
    var autoPlayAudio by remember { mutableStateOf(true) }
    var englishFrontMode by remember { mutableStateOf(false) }

    val totalOriginal = session.entries.size.coerceAtLeast(1)
    val currentEntry = queue.firstOrNull()

    LaunchedEffect(currentEntry?.word?.hanzi, englishFrontMode, autoPlayAudio) {
        if (currentEntry != null && autoPlayAudio && !englishFrontMode) {
            onSpeak(currentEntry.word.hanzi, 0.9f)
        }
    }

    if (currentEntry == null) {
        // Anki Session Complete Screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(JadeLightContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = JadePrimary,
                    modifier = Modifier.size(46.dp)
                )
            }
            Text(
                text = "Anki Review Complete!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${session.title} · $completedCount card(s) mastered",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnkiStatPill("Again", againCount, CoralVermilion)
                    AnkiStatPill("Hard", hardCount, GoldAccent)
                    AnkiStatPill("Good", goodCount, JadePrimary)
                    AnkiStatPill("Easy", easyCount, IndigoAccent)
                }
            }

            Spacer(Modifier.height(8.dp))

            PandaActionButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("anki_done_button")
            )
            OutlinedButton(
                onClick = {
                    queue.clear()
                    queue.addAll(session.entries.shuffled())
                    completedCount = 0
                    againCount = 0
                    hardCount = 0
                    goodCount = 0
                    easyCount = 0
                    answerRevealed = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Review Again")
            }
        }
        return
    }

    val word = currentEntry.word
    val srs = currentEntry.srsCard

    // Compute authentic Anki next-interval previews via SM-2
    val hardPreview = remember(word.hanzi, srs?.repetitions, srs?.intervalDays) {
        val res = Sm2Calculator.calculate(
            quality = 3,
            currentReps = srs?.repetitions?.toInt() ?: 0,
            currentInterval = srs?.intervalDays?.toInt() ?: 1,
            currentEase = srs?.easeFactor ?: 2.5,
            currentLapses = srs?.lapses?.toInt() ?: 0
        )
        "${res.intervalDays}d"
    }
    val goodPreview = remember(word.hanzi, srs?.repetitions, srs?.intervalDays) {
        val res = Sm2Calculator.calculate(
            quality = 4,
            currentReps = srs?.repetitions?.toInt() ?: 0,
            currentInterval = srs?.intervalDays?.toInt() ?: 1,
            currentEase = srs?.easeFactor ?: 2.5,
            currentLapses = srs?.lapses?.toInt() ?: 0
        )
        "${res.intervalDays}d"
    }
    val easyPreview = remember(word.hanzi, srs?.repetitions, srs?.intervalDays) {
        val res = Sm2Calculator.calculate(
            quality = 5,
            currentReps = srs?.repetitions?.toInt() ?: 0,
            currentInterval = srs?.intervalDays?.toInt() ?: 1,
            currentEase = srs?.easeFactor ?: 2.5,
            currentLapses = srs?.lapses?.toInt() ?: 0
        )
        "${(res.intervalDays + 2)}d"
    }

    fun submitRating(quality: Int) {
        onRateCard(currentEntry, quality)
        val finished = queue.removeAt(0)
        when (quality) {
            1 -> {
                againCount++
                // Authentic Anki learning step: re-insert card 2 spots later in active session
                val insertIdx = 2.coerceAtMost(queue.size)
                queue.add(insertIdx, finished)
            }
            3 -> {
                hardCount++
                completedCount++
            }
            4 -> {
                goodCount++
                completedCount++
            }
            else -> {
                easyCount++
                completedCount++
            }
        }
        answerRevealed = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JadeVibrant)
    ) {
        // Screenshot-6 style vibrant Jade top bar: X  |  1 / N  |  Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("anki_close_button")
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close Anki review", tint = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(completedCount + 1).coerceAtMost(totalOriginal)} / $totalOriginal",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
                if (queue.size > totalOriginal - completedCount) {
                    Text(
                        text = "${queue.size} remaining (incl. re-queued)",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
            TextButton(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.testTag("anki_settings_button")
            ) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Settings", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        LinearProgressIndicator(
            progress = { (completedCount.toFloat() / totalOriginal).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Color.White,
            trackColor = Color.White.copy(alpha = 0.25f)
        )

        // Clean elevated rounded sheet container matching Screenshot 6
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Center Scrollable Flashcard Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Subtle HSK + Strength Badge
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "HSK ${word.hskLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = currentEntry.strength.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(36.dp))

                    if (!englishFrontMode) {
                        // Hanzi Front (exact Screenshot 6 minimalist feel!)
                        if (showPinyinOnFront || answerRevealed) {
                            Text(
                                text = word.pinyin,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                        }

                        Text(
                            text = word.hanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(22.dp))

                        // Big crisp Jade Speaker icon button right under the Hanzi (Screenshot 6)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                onClick = { onSpeak(word.hanzi, 0.9f) },
                                shape = CircleShape,
                                color = JadeLightContainer.copy(alpha = 0.55f),
                                modifier = Modifier
                                    .size(58.dp)
                                    .testTag("anki_speak_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Listen pronunciation",
                                        tint = JadePrimary,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Surface(
                                onClick = { onSpeak(word.hanzi, 0.6f) },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.SlowMotionVideo,
                                        contentDescription = "Slow audio",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // English-first Recall Mode
                        Text(
                            text = word.english,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Answer Reveal Card
                    AnimatedVisibility(visible = answerRevealed) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.35f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (englishFrontMode) {
                                        Text(
                                            text = word.hanzi,
                                            fontFamily = HanziSerifFontFamily,
                                            fontSize = 38.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = JadePrimary
                                        )
                                        Text(
                                            text = word.pinyin,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = word.english,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    if (word.exampleHanzi.isNotBlank()) {
                                        Surface(
                                            onClick = { onSpeak(word.exampleHanzi, 0.9f) },
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = word.exampleHanzi,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (word.examplePinyin.isNotBlank()) {
                                                    Text(
                                                        text = word.examplePinyin,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = JadePrimary
                                                    )
                                                }
                                                if (word.exampleEnglish.isNotBlank()) {
                                                    Text(
                                                        text = word.exampleEnglish,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(
                                            onClick = { onOpenWordBreakdown(word) },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Rounded.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Story & Radicals")
                                        }
                                        OutlinedButton(
                                            onClick = { onPracticeWrite(word) },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Write")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Bottom Action Area (exact Screenshot 6 "SHOW ANSWER" → Anki 4-button intervals!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!answerRevealed) {
                        Text(
                            text = if (englishFrontMode) "How do you say this in Chinese?" else "What's the meaning of this word?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Surface(
                            onClick = {
                                answerRevealed = true
                                if (englishFrontMode && autoPlayAudio) {
                                    onSpeak(word.hanzi, 0.9f)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .testTag("anki_show_answer_button"),
                            color = JadeLightContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "SHOW ANSWER",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JadePrimary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "How well did you recall it? (Anki SM-2)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AnkiRateButton(
                                label = "Again",
                                interval = "<1m",
                                color = CoralVermilion,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("anki_rate_again"),
                                onClick = { submitRating(1) }
                            )
                            AnkiRateButton(
                                label = "Hard",
                                interval = hardPreview,
                                color = GoldAccent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("anki_rate_hard"),
                                onClick = { submitRating(3) }
                            )
                            AnkiRateButton(
                                label = "Good",
                                interval = goodPreview,
                                color = JadePrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("anki_rate_good"),
                                onClick = { submitRating(4) }
                            )
                            AnkiRateButton(
                                label = "Easy",
                                interval = easyPreview,
                                color = IndigoAccent,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("anki_rate_easy"),
                                onClick = { submitRating(5) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Anki Review Settings") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Pinyin on front", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = showPinyinOnFront, onCheckedChange = { showPinyinOnFront = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Auto-play Mandarin audio", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = autoPlayAudio, onCheckedChange = { autoPlayAudio = it })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("English → Hanzi mode", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = englishFrontMode, onCheckedChange = { englishFrontMode = it })
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun AnkiRateButton(
    label: String,
    interval: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Text(
                text = interval,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.85f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AnkiStatPill(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
