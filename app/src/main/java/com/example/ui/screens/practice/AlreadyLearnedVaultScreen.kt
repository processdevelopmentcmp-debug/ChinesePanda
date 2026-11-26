package com.example.ui.screens.practice

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Topic
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GrammarVaultItem
import com.example.model.HskGrammarVault
import com.example.model.LearnedWordEntry
import com.example.model.TopicCategory
import com.example.model.VocabWord
import com.example.model.WordStrength
import com.example.model.WordStrengthFilter
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import java.text.NumberFormat
import java.util.Locale

enum class VaultSubScreen {
    HUB,
    WORDS_SELECTOR,
    GRAMMAR
}

@Composable
fun AlreadyLearnedVaultScreen(
    viewModel: PandaViewModel,
    initialSubScreen: VaultSubScreen = VaultSubScreen.HUB,
    initialTopicModeAllWords: Boolean = false,
    onBack: () -> Unit
) {
    var subScreen by remember { mutableStateOf(initialSubScreen) }
    var reviewOnlyLearned by remember { mutableStateOf(!initialTopicModeAllWords) }

    val learnedEntries by viewModel.learnedWordEntries.collectAsStateWithLifecycle()
    val totalCatalogCount by viewModel.totalVocabCount.collectAsStateWithLifecycle()
    val topics by viewModel.hskTopics.collectAsStateWithLifecycle()
    val allTopicCounts by viewModel.learnAllTopicCounts.collectAsStateWithLifecycle()
    val profileState by viewModel.profileState.collectAsStateWithLifecycle()

    BackHandler {
        if (subScreen != VaultSubScreen.HUB && initialSubScreen == VaultSubScreen.HUB) {
            subScreen = VaultSubScreen.HUB
        } else {
            onBack()
        }
    }

    when (subScreen) {
        VaultSubScreen.HUB -> {
            val weakCount = remember(learnedEntries) { learnedEntries.count { it.strength == WordStrength.WEAK } }
            val mediumCount = remember(learnedEntries) { learnedEntries.count { it.strength == WordStrength.MEDIUM } }
            val strongCount = remember(learnedEntries) { learnedEntries.count { it.strength == WordStrength.STRONG } }
            val dueCount = remember(learnedEntries) { learnedEntries.count { it.isDue } }
            val completedCoreCount = (profileState as? com.example.model.UiState.Success)
                ?.data?.completedUnitIds?.size ?: 0
            val unlockedGrammarCount = (completedCoreCount * 2 + 2).coerceAtMost(HskGrammarVault.items.size)

            AlreadyLearnedHubView(
                learnedWordsCount = learnedEntries.size,
                totalCatalogWords = totalCatalogCount,
                weakCount = weakCount,
                mediumCount = mediumCount,
                strongCount = strongCount,
                dueCount = dueCount,
                unlockedGrammarCount = unlockedGrammarCount,
                totalGrammarCount = HskGrammarVault.items.size,
                onOpenWords = {
                    reviewOnlyLearned = true
                    subScreen = VaultSubScreen.WORDS_SELECTOR
                },
                onOpenAllTopicsAnki = {
                    reviewOnlyLearned = false
                    subScreen = VaultSubScreen.WORDS_SELECTOR
                },
                onOpenGrammar = { subScreen = VaultSubScreen.GRAMMAR },
                onStartDueAnki = { viewModel.startDueAnkiReview() },
                onBack = onBack
            )
        }

        VaultSubScreen.WORDS_SELECTOR -> {
            LearnedWordsTopicSelectorView(
                learnedEntries = learnedEntries,
                topics = topics,
                allTopicCounts = allTopicCounts,
                reviewOnlyLearned = reviewOnlyLearned,
                onToggleOnlyLearned = { reviewOnlyLearned = it },
                onSpeak = { viewModel.speakChinese(it) },
                onOpenWordBreakdown = { viewModel.openWordBreakdown(it) },
                onStartAnkiReview = { selectedTopicIds, strengthFilter ->
                    val label = if (selectedTopicIds.size == topics.size) {
                        if (reviewOnlyLearned) "All Learned Words (${strengthFilter.label})" else "All 14 Topics Anki Review"
                    } else {
                        val names = topics.filter { it.id in selectedTopicIds }.take(2).joinToString(" & ") { it.nameEn }
                        val extra = if (selectedTopicIds.size > 2) " +${selectedTopicIds.size - 2}" else ""
                        "$names$extra"
                    }
                    viewModel.startTopicAnkiReview(
                        topicIds = selectedTopicIds.toList(),
                        title = label,
                        onlyLearned = reviewOnlyLearned,
                        strengthFilter = strengthFilter
                    )
                },
                onBack = {
                    if (initialSubScreen == VaultSubScreen.HUB) {
                        subScreen = VaultSubScreen.HUB
                    } else {
                        onBack()
                    }
                }
            )
        }

        VaultSubScreen.GRAMMAR -> {
            LearnedGrammarVaultView(
                items = HskGrammarVault.items,
                onSpeak = { viewModel.speakChinese(it) },
                onPracticeWithAi = { item ->
                    viewModel.setTutorHskLevel(item.hskLevel)
                    viewModel.selectNavTab(4)
                    viewModel.sendTeacherMessage(
                        "Teach me the HSK ${item.hskLevel} grammar pattern '${item.pattern}' (${item.title}). " +
                            "Give me 3 everyday sentences and 1 fill-in-the-blank quiz question."
                    )
                    onBack()
                },
                onBack = {
                    if (initialSubScreen == VaultSubScreen.HUB) {
                        subScreen = VaultSubScreen.HUB
                    } else {
                        onBack()
                    }
                }
            )
        }
    }
}

@Composable
private fun AlreadyLearnedHubView(
    learnedWordsCount: Int,
    totalCatalogWords: Int,
    weakCount: Int,
    mediumCount: Int,
    strongCount: Int,
    dueCount: Int,
    unlockedGrammarCount: Int,
    totalGrammarCount: Int,
    onOpenWords: () -> Unit,
    onOpenAllTopicsAnki: () -> Unit,
    onOpenGrammar: () -> Unit,
    onStartDueAnki: () -> Unit,
    onBack: () -> Unit
) {
    val fmt = remember { NumberFormat.getIntegerInstance(Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screenshot 4 Clean Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("vault_back_button")
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Already learned",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Learned So Far Summary + Instant Anki Review Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "LEARNED SO FAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = JadePrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StrengthSummaryPill("Weak", weakCount, CoralVermilion, Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StrengthSummaryPill("Medium", mediumCount, GoldAccent, Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            StrengthSummaryPill("Strong", strongCount, JadePrimary, Modifier.weight(1f))
                        }
                        PandaActionButton(
                            text = if (dueCount > 0) "Start Anki Review ($dueCount due)" else "Start Anki Review",
                            onClick = onStartDueAnki,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("vault_start_due_anki_button"),
                            leadingIcon = {
                                Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        )
                    }
                }
            }

            // Screenshot 4 Card 1: Words (learned / total >)
            item {
                VaultCategoryRowCard(
                    title = "Words",
                    subtitle = "Filter All / Weak / Medium / Strong & review Anki-style",
                    counter = "${fmt.format(learnedWordsCount)}/${fmt.format(totalCatalogWords)}",
                    badgeColor = JadePrimary,
                    icon = Icons.Rounded.MenuBook,
                    testTag = "vault_words_card",
                    onClick = onOpenWords
                )
            }

            // Screenshot 4 Card 2: Grammar (learned / total >)
            item {
                VaultCategoryRowCard(
                    title = "Grammar",
                    subtitle = "HSK 1–6 essential sentence structures & examples",
                    counter = "$unlockedGrammarCount/$totalGrammarCount",
                    badgeColor = Color(0xFFEC4899),
                    icon = Icons.Rounded.FormatQuote,
                    testTag = "vault_grammar_card",
                    onClick = onOpenGrammar
                )
            }

            // Card 3: Topic-by-Topic Multi-Select Anki Review (all 14 topics)
            item {
                VaultCategoryRowCard(
                    title = "Topic-by-Topic Anki Review",
                    subtitle = "Select any combination of the 14 topics to drill in Anki style",
                    counter = "14 topics",
                    badgeColor = IndigoAccent,
                    icon = Icons.Rounded.Topic,
                    testTag = "vault_topic_anki_card",
                    onClick = onOpenAllTopicsAnki
                )
            }
        }
    }
}

@Composable
private fun StrengthSummaryPill(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun VaultCategoryRowCard(
    title: String,
    subtitle: String,
    counter: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = counter,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LearnedWordsTopicSelectorView(
    learnedEntries: List<LearnedWordEntry>,
    topics: List<TopicCategory>,
    allTopicCounts: Map<Int, Int>,
    reviewOnlyLearned: Boolean,
    onToggleOnlyLearned: (Boolean) -> Unit,
    onSpeak: (String) -> Unit,
    onOpenWordBreakdown: (VocabWord) -> Unit,
    onStartAnkiReview: (selectedTopicIds: Set<Int>, strengthFilter: WordStrengthFilter) -> Unit,
    onBack: () -> Unit
) {
    var strengthFilter by remember { mutableStateOf(WordStrengthFilter.ALL) }
    var selectedTopicIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var expandedTopicIds by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val filteredLearned = remember(learnedEntries, strengthFilter) {
        when (strengthFilter) {
            WordStrengthFilter.ALL -> learnedEntries
            WordStrengthFilter.WEAK -> learnedEntries.filter { it.strength == WordStrength.WEAK }
            WordStrengthFilter.MEDIUM -> learnedEntries.filter { it.strength == WordStrength.MEDIUM }
            WordStrengthFilter.STRONG -> learnedEntries.filter { it.strength == WordStrength.STRONG }
        }
    }

    val learnedByTopic = remember(filteredLearned) {
        filteredLearned.groupBy { it.word.topicId }
    }

    val visibleTopics = remember(topics, learnedByTopic, reviewOnlyLearned) {
        if (reviewOnlyLearned) {
            val withLearned = topics.filter { (learnedByTopic[it.id]?.size ?: 0) > 0 }
            withLearned.ifEmpty { topics }
        } else {
            topics
        }
    }

    val allSelected = visibleTopics.isNotEmpty() && visibleTopics.all { it.id in selectedTopicIds }
    val selectedWordEstimate = remember(selectedTopicIds, reviewOnlyLearned, learnedByTopic, allTopicCounts) {
        if (reviewOnlyLearned) {
            selectedTopicIds.sumOf { learnedByTopic[it]?.size ?: 0 }
        } else {
            selectedTopicIds.sumOf { allTopicCounts[it] ?: 0 }.coerceAtMost(30)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Screenshot 1 Top Header: <  Words
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Words",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
        }

        // Scope Switcher: Learned So Far vs All 14 Topics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = reviewOnlyLearned,
                onClick = {
                    onToggleOnlyLearned(true)
                    selectedTopicIds = emptySet()
                },
                label = { Text("Learned So Far (${learnedEntries.size})") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("words_scope_learned"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = JadePrimary,
                    selectedLabelColor = Color.White
                )
            )
            FilterChip(
                selected = !reviewOnlyLearned,
                onClick = {
                    onToggleOnlyLearned(false)
                    selectedTopicIds = emptySet()
                },
                label = { Text("All 14 Topics") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("words_scope_all_topics"),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = IndigoAccent,
                    selectedLabelColor = Color.White
                )
            )
        }

        // Screenshot 1 Strength Filter Pills: All | Weak | Medium | Strong
        if (reviewOnlyLearned) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WordStrengthFilter.entries) { filter ->
                    val selected = filter == strengthFilter
                    val count = when (filter) {
                        WordStrengthFilter.ALL -> learnedEntries.size
                        WordStrengthFilter.WEAK -> learnedEntries.count { it.strength == WordStrength.WEAK }
                        WordStrengthFilter.MEDIUM -> learnedEntries.count { it.strength == WordStrength.MEDIUM }
                        WordStrengthFilter.STRONG -> learnedEntries.count { it.strength == WordStrength.STRONG }
                    }
                    Surface(
                        onClick = { strengthFilter = filter },
                        shape = RoundedCornerShape(50),
                        color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.testTag("strength_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = "${filter.label} ($count)",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Screenshot 1 "Select units    All ○" Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Select topics to review",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable {
                        selectedTopicIds = if (allSelected) {
                            emptySet()
                        } else {
                            visibleTopics.map { it.id }.toSet()
                        }
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("select_all_topics_toggle"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "All",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (allSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = "Select all topics",
                    tint = if (allSelected) JadePrimary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Expandable Topic / Unit Selection List (Screenshot 1)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(visibleTopics, key = { it.id }) { topic ->
                val topicEntries = learnedByTopic[topic.id].orEmpty()
                val totalTopicWords = allTopicCounts[topic.id] ?: 0
                val isSelected = topic.id in selectedTopicIds
                val isExpanded = topic.id in expandedTopicIds
                val badgeColors = listOf(
                    Color(0xFFEC4899),
                    JadePrimary,
                    GoldAccent,
                    IndigoAccent,
                    CoralVermilion,
                    Color(0xFF0EA5E9)
                )
                val badgeColor = badgeColors[(topic.id - 1) % badgeColors.size]

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("topic_review_unit_${topic.id}"),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = if (isSelected) 3.dp else 1.dp,
                    border = BorderStroke(
                        if (isSelected) 1.8.dp else 1.dp,
                        if (isSelected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTopicIds = if (isSelected) {
                                        selectedTopicIds - topic.id
                                    } else {
                                        selectedTopicIds + topic.id
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic.nameZh.take(1),
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topic.nameEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (reviewOnlyLearned) {
                                        "${topicEntries.size} learned word(s) · ${topic.nameZh}"
                                    } else {
                                        "$totalTopicWords total words · ${topic.nameZh}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (topicEntries.isNotEmpty()) {
                                Surface(
                                    onClick = {
                                        expandedTopicIds = if (isExpanded) {
                                            expandedTopicIds - topic.id
                                        } else {
                                            expandedTopicIds + topic.id
                                        }
                                    },
                                    shape = CircleShape,
                                    color = JadeLightContainer
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                        contentDescription = "Expand topic words",
                                        tint = JadePrimary,
                                        modifier = Modifier.padding(6.dp).size(18.dp)
                                    )
                                }
                            }

                            Icon(
                                imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = if (isSelected) "Selected" else "Not selected",
                                tint = if (isSelected) JadePrimary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        AnimatedVisibility(visible = isExpanded && topicEntries.isNotEmpty()) {
                            Column(
                                modifier = Modifier.padding(top = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                topicEntries.take(25).forEach { entry ->
                                    val dotColor = when (entry.strength) {
                                        WordStrength.WEAK -> CoralVermilion
                                        WordStrength.MEDIUM -> GoldAccent
                                        WordStrength.STRONG -> JadePrimary
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(dotColor)
                                            )
                                            Text(
                                                text = entry.word.hanzi,
                                                fontFamily = HanziSerifFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = entry.word.pinyin,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = JadePrimary
                                                )
                                                Text(
                                                    text = entry.word.english,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            IconButton(
                                                onClick = { onSpeak(entry.word.hanzi) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen", modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { onOpenWordBreakdown(entry.word) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(Icons.Rounded.AutoStories, contentDescription = "Story & Radicals", tint = JadePrimary, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Screenshot 1 Bottom Full-Width PRACTICE CTA Bar
        val canPractice = selectedTopicIds.isNotEmpty()
        Surface(
            onClick = {
                if (canPractice) {
                    onStartAnkiReview(selectedTopicIds, strengthFilter)
                }
            },
            enabled = canPractice,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .testTag("vault_practice_anki_button"),
            color = if (canPractice) JadePrimary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (canPractice) {
                        if (selectedWordEstimate > 0) "ANKI REVIEW ($selectedWordEstimate CARDS)" else "ANKI REVIEW"
                    } else {
                        "SELECT TOPICS TO PRACTICE"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (canPractice) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.6.sp
                )
            }
        }
    }
}

@Composable
private fun LearnedGrammarVaultView(
    items: List<GrammarVaultItem>,
    onSpeak: (String) -> Unit,
    onPracticeWithAi: (GrammarVaultItem) -> Unit,
    onBack: () -> Unit
) {
    var selectedLevel by remember { mutableStateOf<Int?>(null) }
    val filtered = remember(items, selectedLevel) {
        if (selectedLevel == null) items else items.filter { it.hskLevel == selectedLevel }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Grammar Vault",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedLevel == null,
                    onClick = { selectedLevel = null },
                    label = { Text("All Levels") }
                )
            }
            items((1..6).toList()) { lvl ->
                FilterChip(
                    selected = selectedLevel == lvl,
                    onClick = { selectedLevel = lvl },
                    label = { Text("HSK $lvl") }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { item ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "HSK ${item.hskLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = item.pattern,
                                style = MaterialTheme.typography.titleSmall,
                                color = JadePrimary,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.explanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(item.exampleHanzi, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                Text(item.examplePinyin, style = MaterialTheme.typography.bodySmall, color = JadePrimary)
                                Text(item.exampleEnglish, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onSpeak(item.exampleHanzi) },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Listen")
                            }
                            OutlinedButton(
                                onClick = { onPracticeWithAi(item) },
                                shape = RoundedCornerShape(50)
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Drill with Panda Laoshi")
                            }
                        }
                    }
                }
            }
        }
    }
}
