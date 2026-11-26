package com.example.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HskCurriculumData
import com.example.model.LearningUnit
import com.example.model.LessonQuestion
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.HskBadgesMiniStripCard
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant
import com.example.viewmodel.LearnTabMode
import com.example.viewmodel.PandaViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LearnPathScreen(
    viewModel: PandaViewModel,
    profile: UserProfile,
    onOpenDailyTracker: () -> Unit = {},
    onOpenWordGoalSetter: () -> Unit = {},
    onOpenLearnedVault: () -> Unit = {},
    onOpenProfilePage: () -> Unit = {}
) {
    val bonusState by viewModel.bonusExerciseState.collectAsStateWithLifecycle()
    val learnMode by viewModel.learnMode.collectAsStateWithLifecycle()
    val selectedHskLevel by viewModel.learnHskLevel.collectAsStateWithLifecycle()
    val selectedTopicId by viewModel.learnTopicId.collectAsStateWithLifecycle()
    val selectedChapterIndex by viewModel.learnChapterIndex.collectAsStateWithLifecycle()
    val levelSummaries by viewModel.hskLevelSummaries.collectAsStateWithLifecycle()
    val topicLevelSummaries by viewModel.learnTopicLevelSummaries.collectAsStateWithLifecycle()
    val topics by viewModel.hskTopics.collectAsStateWithLifecycle()
    val allTopicCounts by viewModel.learnAllTopicCounts.collectAsStateWithLifecycle()
    val scopeTotalWords by viewModel.learnScopeTotalWords.collectAsStateWithLifecycle()
    val chapterWords by viewModel.learnChapterWords.collectAsStateWithLifecycle()
    val totalCatalogWords by viewModel.totalVocabCount.collectAsStateWithLifecycle()
    val syncState by viewModel.catalogSyncState.collectAsStateWithLifecycle()
    val completedVocabLessons by viewModel.completedVocabLessonIds.collectAsStateWithLifecycle()
    val activeVocabLesson by viewModel.activeVocabLesson.collectAsStateWithLifecycle()
    val activeLessonAnatomy by viewModel.activeLessonWordAnatomy.collectAsStateWithLifecycle()
    val dailyTracker by viewModel.dailyTracker.collectAsStateWithLifecycle()
    val hskProfileShowcase by viewModel.hskProfileShowcase.collectAsStateWithLifecycle()
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val focusTimerRunning by viewModel.focusTimerRunning.collectAsStateWithLifecycle()
    val focusTimerRemainingSec by viewModel.focusTimerRemainingSec.collectAsStateWithLifecycle()
    val learnedEntries by viewModel.learnedWordEntries.collectAsStateWithLifecycle()
    val activeShortcutQuiz by viewModel.activeShortcutQuiz.collectAsStateWithLifecycle()

    var activeCoreUnit by remember { mutableStateOf<LearningUnit?>(null) }
    val dueAnkiCount = remember(learnedEntries) { learnedEntries.count { it.isDue } }

    // 1. Active 4-Stage Interactive Vocabulary Lesson Runner
    if (activeVocabLesson != null) {
        val currentLesson = activeVocabLesson!!
        BackHandler { viewModel.dismissVocabLesson() }
        VocabLessonRunnerView(
            lesson = currentLesson,
            wordAnatomyMap = activeLessonAnatomy,
            onSpeak = { text, speed -> viewModel.speakChinese(text, speed) },
            onOpenWordBreakdown = { word -> viewModel.openWordBreakdown(word) },
            onSaveWordToSrs = { word -> viewModel.addWordToSrs(word) },
            onSaveAllToSrs = { batch -> viewModel.addWordsBatchToSrs(batch) },
            onPracticeWrite = { word ->
                viewModel.dismissVocabLesson()
                viewModel.jumpToWriteWord(word)
            },
            onWrongAnswer = { viewModel.deductHeart() },
            onComplete = { xp ->
                viewModel.completeVocabLesson(currentLesson.lessonId, currentLesson.title, xp)
            },
            onExit = { viewModel.dismissVocabLesson() }
        )
        return
    }

    // 2. Active Core Path Grammar/Pinyin/Chengyu Lesson Runner
    if (activeCoreUnit != null) {
        BackHandler { activeCoreUnit = null }
        LessonRunnerView(
            unit = activeCoreUnit!!,
            onSpeak = { viewModel.speakChinese(it) },
            onWrongAnswer = { viewModel.deductHeart() },
            onComplete = { earnedXp ->
                viewModel.awardXp(earnedXp.toLong(), activeCoreUnit!!.id, "Completed ${activeCoreUnit!!.title}")
                activeCoreUnit = null
            },
            onExit = { activeCoreUnit = null }
        )
        return
    }

    // Back handler when drilling into a single Topic inside Topic-by-Topic mode
    if (learnMode == LearnTabMode.TOPIC_BY_TOPIC && selectedTopicId != null) {
        BackHandler { viewModel.setLearnTopicId(null) }
    }

    val completedCoreIds = profile.completedUnitIds.toSet()
    val lessonBatches = remember(chapterWords) { chapterWords.chunked(10) }
    val selectedTopicObj = remember(topics, selectedTopicId) {
        topics.firstOrNull { it.id == selectedTopicId }
    }

    val scopePrefix = remember(learnMode, selectedHskLevel, selectedTopicId) {
        when {
            learnMode == LearnTabMode.TOPIC_BY_TOPIC && selectedTopicId != null ->
                "topic_t${selectedTopicId}_l${selectedHskLevel}_"
            selectedTopicId != null ->
                "hsk_l${selectedHskLevel}_t${selectedTopicId}_"
            else ->
                "hsk_l${selectedHskLevel}_"
        }
    }

    val nextUnfinishedBatchIndex = remember(lessonBatches, completedVocabLessons, scopePrefix, selectedChapterIndex) {
        lessonBatches.indices.firstOrNull { idx ->
            val globalLessonNum = selectedChapterIndex * 10 + idx + 1
            "${scopePrefix}u$globalLessonNum" !in completedVocabLessons
        } ?: 0
    }

    fun launchBatchLesson(batchIdx: Int) {
        val batch = lessonBatches.getOrNull(batchIdx) ?: return
        val globalLessonNum = selectedChapterIndex * 10 + batchIdx + 1
        val lessonId = "${scopePrefix}u$globalLessonNum"
        val hskBadgeText = when (selectedHskLevel) {
            "ALL" -> "HSK ${batch.first().hskLevel}"
            "7+" -> "HSK 7–9"
            else -> "HSK $selectedHskLevel"
        }
        val titleText = if (selectedTopicObj != null) {
            "${selectedTopicObj.nameEn} · Lesson $globalLessonNum"
        } else {
            "$hskBadgeText · Lesson $globalLessonNum"
        }
        val subtitleText = batch.take(3).joinToString(" · ") { "${it.hanzi} (${it.english.substringBefore(";")})" }
        viewModel.startVocabLesson(
            lessonId = lessonId,
            title = titleText,
            subtitle = subtitleText,
            hskBadge = hskBadgeText,
            hskLevel = batch.first().hskLevel,
            words = batch
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rich Mastery Hero Dashboard
        item(key = "hero") {
            LearnMasteryHeroCard(
                profile = profile,
                totalCatalogWords = totalCatalogWords,
                syncState = syncState,
                completedVocabLessonsCount = completedVocabLessons.size,
                completedCoreUnitsCount = completedCoreIds.size,
                totalCoreUnitsCount = HskCurriculumData.learningUnits.size,
                hasQuickStartLesson = lessonBatches.isNotEmpty() && learnMode != LearnTabMode.CORE_PATH,
                onContinueNextLesson = {
                    if (learnMode == LearnTabMode.TOPIC_BY_TOPIC && selectedTopicId == null) {
                        val firstTopicId = topics.firstOrNull()?.id ?: 1
                        viewModel.openTopicLearningPath(firstTopicId)
                    } else {
                        launchBatchLesson(nextUnfinishedBatchIndex)
                    }
                },
                onAiBonusDrill = {
                    val targetLvl = selectedHskLevel.toIntOrNull()
                        ?: if (selectedHskLevel == "7+") 7 else profile.currentHskLevel.toInt()
                    viewModel.generateAiBonusExercise(targetLvl)
                },
                onSyncCatalog = { viewModel.syncFullHsk30Catalog(force = true) },
                onRefillHearts = { viewModel.refillHearts() }
            )
        }

        // Daily Learning Tracker Compact Dashboard Card
        item(key = "daily_tracker_card") {
            DailyTrackerCompactCard(
                snapshot = dailyTracker,
                focusTimerRunning = focusTimerRunning,
                focusTimerRemainingSec = focusTimerRemainingSec,
                onOpenFullTracker = onOpenDailyTracker,
                onOpenWordGoalSetter = onOpenWordGoalSetter,
                onStartDailyGoalLesson = { viewModel.startDailyWordGoalSession() }
            )
        }

        // Visual HSK 3.0 Progress Badges Mini Strip
        item(key = "hsk_badges_strip") {
            HskBadgesMiniStripCard(
                showcase = hskProfileShowcase,
                onOpenProfileBadges = onOpenProfilePage,
                onSelectBadgeLevel = { levelKey ->
                    viewModel.jumpToHskLevelInLearn(levelKey)
                }
            )
        }

        // Daily Word of the Day Spotlight Card
        item(key = "word_of_the_day") {
            WordOfTheDaySpotlightCard(
                word = wordOfTheDay,
                onSpeak = { text, speed -> viewModel.speakChinese(text, speed) },
                onOpenBreakdown = { word -> viewModel.openWordBreakdown(word) },
                onAddSrs = { word -> viewModel.addWordToSrs(word) },
                onWriteWord = { word -> viewModel.jumpToWriteWord(word) },
                onMarkLearned = { word -> viewModel.markWordAcquiredToday(word) }
            )
        }

        // 3-Mode Course Switcher (HSK by HSK | Topic by Topic | Core Path)
        item(key = "mode_switcher") {
            LearnModeSegmentedBar(
                selectedMode = learnMode,
                onSelectMode = { viewModel.setLearnMode(it) }
            )
        }

        when (learnMode) {
            LearnTabMode.HSK_BY_HSK -> {
                // HSK 1 → HSK 7–9 Horizontal Mastery Cards
                item(key = "hsk_selector") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "CHOOSE YOUR HSK 3.0 LEVEL (1 → 9)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        HskLevelCourseSelectorRow(
                            summaries = levelSummaries,
                            selectedLevel = selectedHskLevel,
                            includeAllOption = false,
                            completedLessonIds = completedVocabLessons,
                            lessonIdPrefixBuilder = { lvlKey -> "hsk_l${lvlKey}_" },
                            onSelectLevel = { viewModel.setLearnHskLevel(it) }
                        )
                    }
                }

                // Optional Topic Filter Pill Strip inside the chosen HSK Level
                item(key = "hsk_topic_filter") {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "FILTER BY TOPIC INSIDE HSK ${if (selectedHskLevel == "7+") "7–9" else selectedHskLevel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item(key = "all_topics") {
                                FilterChip(
                                    selected = selectedTopicId == null,
                                    onClick = { viewModel.setLearnTopicId(null) },
                                    label = { Text("All Topics") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JadePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                            items(topics, key = { it.id }) { topic ->
                                FilterChip(
                                    selected = selectedTopicId == topic.id,
                                    onClick = { viewModel.setLearnTopicId(topic.id) },
                                    label = { Text("${topic.nameZh} ${topic.nameEn}") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JadePrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Chapter Selector (100 words / 10 lessons per chapter)
                item(key = "hsk_chapters") {
                    LearnChapterSelectorRow(
                        totalWords = scopeTotalWords,
                        selectedChapterIndex = selectedChapterIndex,
                        onSelectChapter = { viewModel.setLearnChapterIndex(it) }
                    )
                }

                // Section Header
                item(key = "hsk_lessons_header") {
                    val totalLessons = ((scopeTotalWords + 9) / 10).coerceAtLeast(if (scopeTotalWords > 0) 1 else 0)
                    val levelTitle = if (selectedHskLevel == "7+") "HSK 7–9" else "HSK $selectedHskLevel"
                    val formattedWords = NumberFormat.getIntegerInstance(Locale.US).format(scopeTotalWords)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (selectedTopicObj != null) "$levelTitle · ${selectedTopicObj.nameEn}" else "$levelTitle Complete Word Path",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "$formattedWords words · $totalLessons bite-sized 10-word lessons",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (lessonBatches.isEmpty()) {
                    item(key = "hsk_empty") {
                        EmptyScopeCard(
                            message = "No words found for this filter yet. Try selecting All Topics or sync the 11,000+ HSK 3.0 dictionary."
                        )
                    }
                } else {
                    itemsIndexed(lessonBatches, key = { idx, _ -> "hsk_${selectedChapterIndex}_$idx" }) { idx, batch ->
                        val globalLessonNum = selectedChapterIndex * 10 + idx + 1
                        val startWord = (globalLessonNum - 1) * 10 + 1
                        val endWord = startWord + batch.size - 1
                        val lessonId = "${scopePrefix}u$globalLessonNum"
                        val isDone = lessonId in completedVocabLessons
                        val isNext = idx == nextUnfinishedBatchIndex && !isDone
                        val startPad = when (idx % 3) {
                            1 -> 16.dp
                            2 -> 6.dp
                            else -> 0.dp
                        }
                        val endPad = when (idx % 3) {
                            1 -> 0.dp
                            2 -> 16.dp
                            else -> 6.dp
                        }
                        val badge = if (selectedHskLevel == "7+") "HSK 7–9" else "HSK $selectedHskLevel"
                        val previewSubtitle = batch.take(3).joinToString(" · ") { it.english.substringBefore(";") }

                        VocabLessonSteppingCard(
                            lessonNumber = globalLessonNum,
                            wordStartNumber = startWord,
                            wordEndNumber = endWord,
                            hskBadge = badge,
                            topicSubtitle = previewSubtitle,
                            words = batch,
                            completed = isDone,
                            highlighted = isNext,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = startPad, end = endPad),
                            onStartLesson = { launchBatchLesson(idx) },
                            onTapHanzi = { hanzi -> viewModel.speakChinese(hanzi) }
                        )
                    }
                }
            }

            LearnTabMode.TOPIC_BY_TOPIC -> {
                if (selectedTopicObj == null) {
                    item(key = "topics_header") {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "14 Real-Life Mastery Topics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Pick any topic to learn all its HSK 1–9 words step by step in 10-word interactive lessons.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(topics, key = { it.id }) { topic ->
                        val count = allTopicCounts[topic.id] ?: 0
                        val doneForTopic = completedVocabLessons.count { it.startsWith("topic_t${topic.id}_") }
                        TopicCourseCard(
                            topic = topic,
                            wordCount = count,
                            completedCount = doneForTopic,
                            onOpenTopic = { viewModel.openTopicLearningPath(topic.id) }
                        )
                    }
                } else {
                    // Topic Detail Learning Path Header + HSK-by-HSK Sub-selector
                    item(key = "topic_detail_header") {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.45f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.setLearnTopicId(null) },
                                        modifier = Modifier.testTag("learn_topic_back_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to topics")
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${selectedTopicObj.nameZh} · ${selectedTopicObj.nameEn}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "${NumberFormat.getIntegerInstance(Locale.US).format(scopeTotalWords)} words in current HSK filter",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    TextButton(
                                        onClick = {
                                            val lvl = selectedHskLevel.toIntOrNull()
                                                ?: if (selectedHskLevel == "7+") 7 else selectedTopicObj.minHsk
                                            viewModel.launchTopicLessonInAiTeacher(selectedTopicObj.nameEn, lvl)
                                        }
                                    ) {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("AI Class")
                                    }
                                }

                                HskLevelCourseSelectorRow(
                                    summaries = topicLevelSummaries,
                                    selectedLevel = selectedHskLevel,
                                    includeAllOption = true,
                                    completedLessonIds = completedVocabLessons,
                                    lessonIdPrefixBuilder = { lvlKey -> "topic_t${selectedTopicObj.id}_l${lvlKey}_" },
                                    onSelectLevel = { viewModel.setLearnHskLevel(it) }
                                )
                            }
                        }
                    }

                    item(key = "topic_chapters") {
                        LearnChapterSelectorRow(
                            totalWords = scopeTotalWords,
                            selectedChapterIndex = selectedChapterIndex,
                            onSelectChapter = { viewModel.setLearnChapterIndex(it) }
                        )
                    }

                    if (lessonBatches.isEmpty()) {
                        item(key = "topic_empty") {
                            EmptyScopeCard(
                                message = "No words in this HSK level for ${selectedTopicObj.nameEn}. Tap 'All HSK' to see all words in this topic."
                            )
                        }
                    } else {
                        itemsIndexed(lessonBatches, key = { idx, _ -> "topic_${selectedTopicObj.id}_${selectedChapterIndex}_$idx" }) { idx, batch ->
                            val globalLessonNum = selectedChapterIndex * 10 + idx + 1
                            val startWord = (globalLessonNum - 1) * 10 + 1
                            val endWord = startWord + batch.size - 1
                            val lessonId = "${scopePrefix}u$globalLessonNum"
                            val isDone = lessonId in completedVocabLessons
                            val isNext = idx == nextUnfinishedBatchIndex && !isDone
                            val badge = when (selectedHskLevel) {
                                "ALL" -> "HSK ${batch.first().hskLevel}"
                                "7+" -> "HSK 7–9"
                                else -> "HSK $selectedHskLevel"
                            }
                            val previewSubtitle = batch.take(3).joinToString(" · ") { it.english.substringBefore(";") }

                            VocabLessonSteppingCard(
                                lessonNumber = globalLessonNum,
                                wordStartNumber = startWord,
                                wordEndNumber = endWord,
                                hskBadge = badge,
                                topicSubtitle = previewSubtitle,
                                words = batch,
                                completed = isDone,
                                highlighted = isNext,
                                modifier = Modifier.fillMaxWidth(),
                                onStartLesson = { launchBatchLesson(idx) },
                                onTapHanzi = { hanzi -> viewModel.speakChinese(hanzi) }
                            )
                        }
                    }
                }
            }

            LearnTabMode.SKILL_TREE -> {
                item(key = "visual_skill_tree") {
                    VisualSkillTreeSection(
                        completedLessonIds = completedVocabLessons,
                        learnedWordsCount = learnedEntries.size,
                        onTapSkillNode = { node ->
                            viewModel.startSkillTreeNodeLesson(
                                nodeId = node.id,
                                title = node.title,
                                topicId = node.topicId,
                                minHsk = node.minHsk,
                                maxHsk = node.maxHsk
                            )
                        },
                        onTapStoriesMilestone = { viewModel.selectNavTab(3) },
                        onTapShortcut = { targetLevel ->
                            viewModel.startShortcutPlacementQuiz(targetLevel)
                        },
                        onOpenLearnedVault = onOpenLearnedVault
                    )
                }
            }

            LearnTabMode.CORE_PATH -> {
                item(key = "core_header") {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Guided Grammar, Pinyin & Chengyu Path",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Structured milestone units from Pinyin & Tone Sandhi through HSK 2–6 Grammar and HSK 7–9 Idioms.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                itemsIndexed(HskCurriculumData.learningUnits, key = { _, u -> u.id }) { idx, unit ->
                    val isCompleted = completedCoreIds.contains(unit.id)
                    val isNextUp = !isCompleted && (idx == 0 || completedCoreIds.contains(HskCurriculumData.learningUnits[idx - 1].id))
                    val startPad = when (idx % 3) {
                        1 -> 18.dp
                        2 -> 6.dp
                        else -> 0.dp
                    }
                    val endPad = when (idx % 3) {
                        1 -> 0.dp
                        2 -> 18.dp
                        else -> 6.dp
                    }

                    UnitPathCard(
                        unit = unit,
                        completed = isCompleted,
                        highlighted = isNextUp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = startPad, end = endPad),
                        onStart = { activeCoreUnit = unit },
                        onTapHanzi = { char -> viewModel.speakChinese(char) }
                    )
                }
            }
        }
    }

    // HelloChinese-inspired Floating Anki Review Bubble with live due count badge
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 16.dp)
    ) {
        Surface(
            onClick = { viewModel.startDueAnkiReview() },
            shape = CircleShape,
            color = GoldBright,
            shadowElevation = 6.dp,
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier
                .size(58.dp)
                .testTag("floating_anki_review_bubble")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = "Quick Anki review",
                    tint = Color(0xFF4A3100),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        if (dueAnkiCount > 0) {
            Surface(
                shape = CircleShape,
                color = CoralVermilion,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (dueAnkiCount > 99) "99" else "$dueAnkiCount",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
    }

    if (activeShortcutQuiz != null) {
        ShortcutPlacementQuizDialog(
            quiz = activeShortcutQuiz!!,
            onSpeak = { viewModel.speakChinese(it) },
            onFinish = { passed ->
                viewModel.completeShortcutQuiz(activeShortcutQuiz!!.targetHskLevel, passed)
            },
            onDismiss = { viewModel.dismissShortcutQuiz() }
        )
    }

    // AI Bonus Exercise Dialog
    if (bonusState != null) {
        AiBonusExerciseDialog(
            state = bonusState!!,
            onSpeak = { viewModel.speakChinese(it) },
            onCorrect = { viewModel.awardXp(30L, null, "AI Bonus Drill mastered") },
            onRetry = {
                val targetLvl = selectedHskLevel.toIntOrNull()
                    ?: if (selectedHskLevel == "7+") 7 else profile.currentHskLevel.toInt()
                viewModel.generateAiBonusExercise(targetLvl)
            },
            onDismiss = { viewModel.dismissBonusExercise() }
        )
    }
}

@Composable
private fun EmptyScopeCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
private fun UnitPathCard(
    unit: LearningUnit,
    completed: Boolean,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
    onStart: () -> Unit,
    onTapHanzi: (String) -> Unit
) {
    val borderColor = when {
        completed -> JadePrimary
        highlighted -> GoldAccent
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    }
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onStart() }
            .testTag("unit_card_${unit.id}"),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (highlighted) 3.dp else 1.dp,
        border = BorderStroke(if (highlighted || completed) 2.dp else 1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                completed -> JadePrimary
                                highlighted -> GoldBright
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (completed) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Completed", tint = Color.White)
                    } else {
                        Text(
                            text = "${unit.order}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (highlighted) Color(0xFF2B1C00) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = unit.hskBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "+${unit.xpReward} XP · ${unit.questions.size} drills",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = unit.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = unit.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    unit.keyHanzi.forEach { hanzi ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onTapHanzi(hanzi) }
                        ) {
                            Text(
                                text = hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (completed) MaterialTheme.colorScheme.primaryContainer else JadePrimary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = if (completed) JadeDeep else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (completed) "Practice" else "Start",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (completed) JadeDeep else Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LessonRunnerView(
    unit: LearningUnit,
    onSpeak: (String) -> Unit,
    onWrongAnswer: () -> Unit,
    onComplete: (Int) -> Unit,
    onExit: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var correctCount by remember { mutableIntStateOf(0) }

    val question = unit.questions[currentIndex]
    val answered = selectedOption != null
    val isCorrect = selectedOption == question.correctIndex

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to path")
            }
            LinearProgressIndicator(
                progress = { (currentIndex + if (answered) 1 else 0).toFloat() / unit.questions.size },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = JadeVibrant,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Text(
                text = "${currentIndex + 1}/${unit.questions.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${unit.hskBadge} · ${question.kind}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                if (question.hanziHighlight.isNotBlank()) {
                    Text(
                        text = question.hanziHighlight,
                        fontFamily = HanziSerifFontFamily,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = JadeDeep,
                        textAlign = TextAlign.Center
                    )
                }

                if (question.pinyinHint.isNotBlank()) {
                    Text(
                        text = question.pinyinHint,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!question.audioText.isNullOrBlank()) {
                    OutlinedButton(
                        onClick = { onSpeak(question.audioText) },
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen")
                        Spacer(Modifier.width(6.dp))
                        Text("Listen Native Audio")
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            question.options.forEachIndexed { idx, opt ->
                val isSelected = selectedOption == idx
                val showRight = answered && idx == question.correctIndex
                val showWrong = answered && isSelected && !isCorrect

                val bgColor = when {
                    showRight -> JadePrimary.copy(alpha = 0.14f)
                    showWrong -> CoralVermilion.copy(alpha = 0.14f)
                    else -> MaterialTheme.colorScheme.surface
                }
                val strokeColor = when {
                    showRight -> JadePrimary
                    showWrong -> CoralVermilion
                    else -> MaterialTheme.colorScheme.outline
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(enabled = !answered) {
                            selectedOption = idx
                            if (idx == question.correctIndex) {
                                correctCount++
                            } else {
                                onWrongAnswer()
                            }
                            if (!question.audioText.isNullOrBlank()) {
                                onSpeak(question.audioText)
                            }
                        }
                        .testTag("lesson_option_$idx"),
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    border = BorderStroke(if (showRight || showWrong) 2.dp else 1.dp, strokeColor)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + idx)}.",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = answered) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = if (isCorrect) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (isCorrect) "Correct! 太棒了 (Tài bàng le)!" else "Not quite — keep going!",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = question.explanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCorrect) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                    PandaActionButton(
                        text = if (currentIndex == unit.questions.lastIndex) "Finish Lesson (+${unit.xpReward} XP)" else "Next Question",
                        onClick = {
                            if (currentIndex == unit.questions.lastIndex) {
                                onComplete(unit.xpReward)
                            } else {
                                currentIndex++
                                selectedOption = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_next_button"),
                        containerColor = if (isCorrect) JadePrimary else CoralVermilion
                    )
                }
            }
        }
    }
}

@Composable
private fun AiBonusExerciseDialog(
    state: UiState<LessonQuestion>,
    onSpeak: (String) -> Unit,
    onCorrect: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember(state) { mutableStateOf<Int?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.School, contentDescription = null, tint = JadePrimary) },
        title = { Text("Panda Laoshi AI Bonus Drill") },
        text = {
            when (state) {
                is UiState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = JadePrimary)
                        Text("Crafting a fresh HSK 3.0 drill...", style = MaterialTheme.typography.bodySmall)
                    }
                }
                is UiState.Error -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onRetry) { Text("Try Again") }
                    }
                }
                is UiState.Success -> {
                    val q = state.data
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(q.prompt, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        if (q.hanziHighlight.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = q.hanziHighlight,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JadeDeep,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!q.audioText.isNullOrBlank()) {
                                    IconButton(onClick = { onSpeak(q.audioText) }) {
                                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen")
                                    }
                                }
                            }
                        }
                        q.options.forEachIndexed { idx, opt ->
                            val isAns = selected != null
                            val isRight = idx == q.correctIndex
                            val isChosen = selected == idx
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = !isAns) {
                                        selected = idx
                                        if (isRight) onCorrect()
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    isAns && isRight -> JadePrimary.copy(alpha = 0.16f)
                                    isAns && isChosen && !isRight -> CoralVermilion.copy(alpha = 0.16f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = "${('A' + idx)}. $opt",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        if (selected != null) {
                            Text(
                                text = q.explanation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
