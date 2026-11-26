package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Topic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WordStrength
import com.example.ui.screens.practice.CameraScannerStudioSection
import com.example.ui.screens.practice.ClozeContextStudioSection
import com.example.ui.screens.practice.HskMockExamStudioSection
import com.example.ui.screens.practice.ListeningStudioSection
import com.example.ui.screens.practice.MeasureAndSynonymLabSection
import com.example.ui.screens.practice.RadicalsStudioSection
import com.example.ui.screens.practice.SentenceStudioSection
import com.example.ui.screens.practice.SpeakStudioSection
import com.example.ui.screens.practice.WriteStudioSection
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import com.example.viewmodel.PracticeStudioTab

@Composable
fun PracticeHubScreen(
    viewModel: PandaViewModel,
    onOpenLearnedVault: () -> Unit = {},
    onOpenTopicAnkiSelector: () -> Unit = {}
) {
    val activeStudio by viewModel.activePracticeStudio.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Studio Selector Ribbon (keeps all 5 existing studios 1 tap away!)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PracticeStudioTab.entries.forEach { tab ->
                val selected = tab == activeStudio
                val icon = when (tab) {
                    PracticeStudioTab.HUB -> Icons.Rounded.Dashboard
                    PracticeStudioTab.WRITE -> Icons.Rounded.Brush
                    PracticeStudioTab.SPEAK -> Icons.Rounded.Mic
                    PracticeStudioTab.LISTENING -> Icons.Rounded.Headphones
                    PracticeStudioTab.SENTENCES -> Icons.Rounded.Extension
                    PracticeStudioTab.CLOZE -> Icons.Rounded.EditNote
                    PracticeStudioTab.RADICALS -> Icons.Rounded.GridView
                    PracticeStudioTab.SCANNER -> Icons.Rounded.CameraAlt
                    PracticeStudioTab.MOCK_EXAM -> Icons.Rounded.Assignment
                    PracticeStudioTab.MEASURE_SYNONYMS -> Icons.Rounded.Balance
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { viewModel.selectPracticeStudio(tab) }
                        .testTag("practice_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(50),
                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Studio Content
        when (activeStudio) {
            PracticeStudioTab.HUB -> PracticeOverviewHub(
                viewModel = viewModel,
                onOpenLearnedVault = onOpenLearnedVault,
                onOpenTopicAnkiSelector = onOpenTopicAnkiSelector
            )
            PracticeStudioTab.WRITE -> WriteStudioSection(viewModel)
            PracticeStudioTab.SPEAK -> SpeakStudioSection(viewModel)
            PracticeStudioTab.LISTENING -> ListeningStudioSection(viewModel)
            PracticeStudioTab.SENTENCES -> SentenceStudioSection(viewModel)
            PracticeStudioTab.CLOZE -> ClozeContextStudioSection(viewModel)
            PracticeStudioTab.RADICALS -> RadicalsStudioSection(viewModel)
            PracticeStudioTab.SCANNER -> CameraScannerStudioSection(viewModel)
            PracticeStudioTab.MOCK_EXAM -> HskMockExamStudioSection(viewModel)
            PracticeStudioTab.MEASURE_SYNONYMS -> MeasureAndSynonymLabSection(viewModel)
        }
    }
}

@Composable
private fun PracticeOverviewHub(
    viewModel: PandaViewModel,
    onOpenLearnedVault: () -> Unit,
    onOpenTopicAnkiSelector: () -> Unit
) {
    val learnedEntries by viewModel.learnedWordEntries.collectAsStateWithLifecycle()
    val weakEntries = remember(learnedEntries) {
        learnedEntries.filter { it.strength == WordStrength.WEAK }
    }
    val dueEntries = remember(learnedEntries) {
        learnedEntries.filter { it.isDue }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screenshot-5 style Mint Header Banner: Search + "Already learned >"
        item(key = "hub_header") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                JadeLightContainer,
                                JadePrimary.copy(alpha = 0.22f)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.selectNavTab(1) },
                        modifier = Modifier.testTag("practice_hub_search_button")
                    ) {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = "Search HSK vocabulary",
                            tint = JadeDeep
                        )
                    }

                    Surface(
                        onClick = onOpenLearnedVault,
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        modifier = Modifier.testTag("practice_already_learned_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Already learned (${learnedEntries.size})",
                                style = MaterialTheme.typography.titleSmall,
                                color = JadeDeep,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = JadeDeep,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Screenshot-5 style "Specialized Courses" Lavender Card
        item(key = "specialized_courses") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = IndigoAccent.copy(alpha = 0.11f),
                border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Specialized Courses",
                        style = MaterialTheme.typography.titleMedium,
                        color = IndigoAccent,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpecializedCourseTile(
                            glyph = "ā",
                            badgeColor = JadePrimary,
                            title = "Pronunciation",
                            subtitle = "Tones & speaking",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("specialized_pronunciation_card"),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.SPEAK) }
                        )
                        SpecializedCourseTile(
                            glyph = "文",
                            badgeColor = IndigoAccent,
                            title = "Chinese Characters",
                            subtitle = "Strokes & radicals",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("specialized_characters_card"),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.WRITE) }
                        )
                    }
                }
            }
        }

        // Screenshot-5 style "Drills" Warm Peach Card
        item(key = "drills_section") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = GoldAccent.copy(alpha = 0.13f),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.28f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Drills & Anki Review",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFD97706),
                        fontWeight = FontWeight.ExtraBold
                    )

                    // Difficult Points row (exact Screenshot 5!)
                    Surface(
                        onClick = {
                            if (weakEntries.isNotEmpty()) {
                                viewModel.startAnkiReview(
                                    title = "Difficult Points (Weak Words)",
                                    subtitle = "${weakEntries.size} weak word(s) · Anki SM-2 Drill",
                                    entries = weakEntries
                                )
                            } else {
                                onOpenLearnedVault()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drill_difficult_points_card"),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Difficult Points",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Weak & missed words · Anki spaced repetition",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${weakEntries.size}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (weakEntries.isNotEmpty()) CoralVermilion else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Topic-by-Topic Anki Review + Due Anki Cards row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DrillQuickTile(
                            icon = Icons.Rounded.Topic,
                            badgeColor = IndigoAccent,
                            title = "Topic Anki Review",
                            subtitle = "Pick 1 to 14 topics",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("drill_topic_anki_card"),
                            onClick = onOpenTopicAnkiSelector
                        )
                        DrillQuickTile(
                            icon = Icons.Rounded.Psychology,
                            badgeColor = JadePrimary,
                            title = "Due Flashcards",
                            subtitle = "${dueEntries.size} ready now",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("drill_due_anki_card"),
                            onClick = { viewModel.startDueAnkiReview() }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DrillQuickTile(
                            icon = Icons.Rounded.Headphones,
                            badgeColor = Color(0xFFF59E0B),
                            title = "Listening & Pairs",
                            subtitle = "HSK + Minimal Pairs",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.LISTENING) }
                        )
                        DrillQuickTile(
                            icon = Icons.Rounded.Extension,
                            badgeColor = CoralVermilion,
                            title = "Sentence & AI",
                            subtitle = "Tiles + Transformer",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.SENTENCES) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DrillQuickTile(
                            icon = Icons.Rounded.EditNote,
                            badgeColor = IndigoAccent,
                            title = "Cloze Context",
                            subtitle = "Fill-in-blank cards",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("drill_cloze_context_card"),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.CLOZE) }
                        )
                        DrillQuickTile(
                            icon = Icons.Rounded.GridView,
                            badgeColor = JadePrimary,
                            title = "Radical Builder",
                            subtitle = "Fuse + Family Trees",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("drill_radical_builder_card"),
                            onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.RADICALS) }
                        )
                    }

                    DrillQuickTile(
                        icon = Icons.Rounded.CameraAlt,
                        badgeColor = CoralVermilion,
                        title = "Camera Point & Learn (Gemini Vision)",
                        subtitle = "Snap any object or sign for Hanzi, Pinyin, tones & HSK sentence",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drill_camera_scanner_card"),
                        onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.SCANNER) }
                    )

                    DrillQuickTile(
                        icon = Icons.Rounded.Assignment,
                        badgeColor = JadePrimary,
                        title = "HSK Mock Exam",
                        subtitle = "Official-style timed Listening, Reading & Writing (Score / 300)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drill_hsk_mock_exam_card"),
                        onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.MOCK_EXAM) }
                    )

                    DrillQuickTile(
                        icon = Icons.Rounded.Balance,
                        badgeColor = IndigoAccent,
                        title = "Measure & Synonyms ⚖️",
                        subtitle = "24 量词 visual classifiers + 16 近义词 PK battles & drills",
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drill_measure_synonyms_card"),
                        onClick = { viewModel.selectPracticeStudio(PracticeStudioTab.MEASURE_SYNONYMS) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun SpecializedCourseTile(
    glyph: String,
    badgeColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = glyph,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun DrillQuickTile(
    icon: ImageVector,
    badgeColor: Color,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
