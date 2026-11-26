package com.example.viewmodel

import android.content.SharedPreferences
import android.util.Log
import com.example.audio.ChineseAudioManager
import com.example.audio.LiveVoiceTutorManager
import com.example.data.AppDisplayPrefs
import com.example.data.DailyLearningTrackerRepository
import com.example.data.HanziEtymologyRepository
import com.example.data.HskCurriculumData
import com.example.data.HskMockExamGenerator
import com.example.data.HskVocabularyRepository
import com.example.data.PandaAiRepository
import com.example.data.PandaFirestoreRepository
import com.example.data.local.CharacterAnatomy
import com.example.data.local.DailyTrackerDateUtil
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.DailyWordGoalCalculator
import com.example.data.local.DailyWordGoalUnit
import com.example.data.local.HskCatalogSyncState
import com.example.data.local.HskLevelSummary
import com.example.data.local.HskVocabularyFilter
import com.example.data.local.WordEtymologyStory
import android.graphics.Bitmap
import com.example.model.ActiveAnkiSession
import com.example.model.ActiveMockExamSession
import com.example.model.ActiveShortcutQuiz
import com.example.model.AiChatMessage
import com.example.model.AiSentenceGrade
import com.example.model.CameraPointLearnResult
import com.example.model.CustomWordDoc
import com.example.model.GradedStory
import com.example.model.GrammarTransformResult
import com.example.model.ShadowingGradeResult
import com.example.model.GroundedSource
import com.example.model.HskBadgeCalculator
import com.example.model.HskMockExamMode
import com.example.model.HskMockLevel
import com.example.model.HskProfileShowcase
import com.example.model.LearnedWordEntry
import com.example.model.LessonQuestion
import com.example.model.LiveVoiceState
import com.example.model.MockExamMistakeItem
import com.example.model.MockExamReport
import com.example.model.MockExamSection
import com.example.model.MockQuestionAnswer
import com.example.model.MockQuestionKind
import com.example.model.PastMockExamRecord
import com.example.model.SavedStoryDoc
import com.example.model.ShortcutQuizQuestion
import com.example.model.Sm2Calculator
import com.example.model.SrsCardDoc
import com.example.model.SynonymBattleItem
import com.example.model.TopicCategory
import com.example.model.TutorMode
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VocabWord
import com.example.model.WordStrength
import com.example.model.WordStrengthFilter
import com.google.firebase.firestore.FirebaseFirestoreException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

private const val VM_TAG = "PandaViewModel"
private const val STOP_TIMEOUT_MS = 5000L

enum class PracticeStudioTab(val label: String) {
    HUB("Hub"),
    WRITE("Write"),
    SPEAK("Speak"),
    LISTENING("Listening"),
    SENTENCES("Sentences"),
    CLOZE("Cloze"),
    RADICALS("Radicals"),
    SCANNER("Scan"),
    MOCK_EXAM("Mock Exam"),
    MEASURE_SYNONYMS("Measure & PK")
}

enum class LearnTabMode(val label: String) {
    HSK_BY_HSK("HSK by HSK"),
    TOPIC_BY_TOPIC("Topic by Topic"),
    SKILL_TREE("Skill Tree"),
    CORE_PATH("Core Path")
}

data class ActiveVocabLesson(
    val lessonId: String,
    val title: String,
    val subtitle: String,
    val hskBadge: String,
    val hskLevel: Int,
    val words: List<VocabWord>,
    val distractors: List<VocabWord>,
    val xpReward: Int = 45
)

private data class LearnScopeQuery(
    val levelFilter: String,
    val topicId: Int?,
    val chapterIndex: Int
)

class PandaViewModel(
    private val firestoreRepo: PandaFirestoreRepository,
    private val vocabRepo: HskVocabularyRepository,
    private val etymologyRepo: HanziEtymologyRepository,
    private val trackerRepo: DailyLearningTrackerRepository,
    val displayPrefs: AppDisplayPrefs,
    private val aiRepo: PandaAiRepository,
    val audioManager: ChineseAudioManager,
    private val currentUserId: String,
    initialDisplayName: String,
    private val progressPrefs: SharedPreferences? = null
) : ViewModel() {

    // --- REACTIVE FIRESTORE STREAMS WITH TWO-TIER ERROR UNWRAPPING ---

    val profileState: StateFlow<UiState<UserProfile>> = firestoreRepo.observeProfile(currentUserId)
        .map<UserProfile?, UiState<UserProfile>> { profile ->
            UiState.Success(
                profile ?: UserProfile(
                    id = currentUserId,
                    userId = currentUserId,
                    displayName = initialDisplayName.ifBlank { "Panda Learner" },
                    xp = 100L,
                    streak = 1L,
                    hearts = 5L,
                    currentHskLevel = 1L
                )
            )
        }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(VM_TAG, "Error observing profile", root)
            emit(UiState.Error(root.message ?: "Failed to load profile"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private val customWordsFlow: StateFlow<List<CustomWordDoc>> = firestoreRepo.observeCustomWords(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing custom words", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val catalogSyncState: StateFlow<HskCatalogSyncState> = vocabRepo.syncState

    private val customVocabListFlow: StateFlow<List<VocabWord>> = customWordsFlow
        .map { customDocs ->
            customDocs.map { doc ->
                VocabWord(
                    key = "custom_${doc.id}",
                    hanzi = doc.hanzi,
                    pinyin = doc.pinyin,
                    english = doc.english,
                    hskLevel = doc.hskLevel.toInt().coerceIn(1, 9),
                    topicId = doc.topicId.toInt().coerceIn(1, 14),
                    writable = doc.writable,
                    exampleHanzi = doc.exampleHanzi,
                    examplePinyin = doc.examplePinyin,
                    exampleEnglish = doc.exampleEnglish,
                    customDocId = doc.id,
                    partOfSpeech = "Custom"
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val featuredLocalVocabFlow: StateFlow<List<VocabWord>> = vocabRepo.featuredVocabulary
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing featured local HSK vocabulary", error)
            emit(HskCurriculumData.builtInVocab)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInVocab)

    val hskTopics: StateFlow<List<TopicCategory>> = vocabRepo.allTopics
        .map { topics -> topics.ifEmpty { HskCurriculumData.topics } }
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing local HSK topics", error)
            emit(HskCurriculumData.topics)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.topics)

    // Lightweight curated + custom list used by Writing & Sentence studios
    val allVocab: StateFlow<List<VocabWord>> = combine(featuredLocalVocabFlow, customVocabListFlow) { localVocab, customVocab ->
        localVocab.ifEmpty { HskCurriculumData.builtInVocab } + customVocab
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInVocab)

    val totalVocabCount: StateFlow<Int> = combine(
        vocabRepo.totalLocalVocabularyCount,
        customVocabListFlow
    ) { localCount, customList ->
        localCount.coerceAtLeast(HskCurriculumData.builtInVocab.size) + customList.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInVocab.size)

    // --- DAILY LEARNING TRACKER, FOCUS STUDY TIMER & WORD OF THE DAY ---

    val dailyTracker: StateFlow<DailyTrackerSnapshot> = trackerRepo.observeTrackerSnapshot(currentUserId)
        .catch { error ->
            if (error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing daily learning tracker", error)
            emit(DailyTrackerSnapshot())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), DailyTrackerSnapshot())

    val wordOfTheDay: StateFlow<VocabWord> = combine(allVocab, profileState) { vocabList, profileUi ->
        val source = vocabList.ifEmpty { HskCurriculumData.builtInVocab }
        val targetLvl = (profileUi as? UiState.Success)?.data?.currentHskLevel?.toInt()?.coerceIn(1, 9) ?: 1
        val preferred = source.filter { it.hskLevel == targetLvl && it.exampleHanzi.isNotBlank() }
            .ifEmpty { source.filter { it.hskLevel == targetLvl } }
            .ifEmpty { source }
        val seed = DailyTrackerDateUtil.dayOfYearSeed()
        preferred[seed % preferred.size]
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        HskCurriculumData.builtInVocab.first()
    )

    private val _focusTimerDurationMin = MutableStateFlow(10)
    val focusTimerDurationMin: StateFlow<Int> = _focusTimerDurationMin.asStateFlow()

    private val _focusTimerRemainingSec = MutableStateFlow(10 * 60)
    val focusTimerRemainingSec: StateFlow<Int> = _focusTimerRemainingSec.asStateFlow()

    private val _focusTimerRunning = MutableStateFlow(false)
    val focusTimerRunning: StateFlow<Boolean> = _focusTimerRunning.asStateFlow()

    private var focusTimerJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val hskLevelSummaries: StateFlow<List<HskLevelSummary>> = customVocabListFlow
        .flatMapLatest { customWords -> vocabRepo.observeLevelSummariesWithCustom(customWords) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            vocabRepo.buildLevelSummaries(HskCurriculumData.builtInVocab)
        )

    private val _vocabFilter = MutableStateFlow(HskVocabularyFilter())
    val vocabFilter: StateFlow<HskVocabularyFilter> = _vocabFilter.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val topicWordCounts: StateFlow<Map<Int, Int>> = combine(_vocabFilter, customVocabListFlow) { filter, custom ->
        filter to custom
    }.flatMapLatest { (filter, custom) ->
        vocabRepo.observeTopicCounts(filter, custom)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    private val filteredPageAndTotal: StateFlow<Pair<List<VocabWord>, Int>> = combine(
        _vocabFilter,
        customVocabListFlow
    ) { filter, custom ->
        filter to custom
    }.flatMapLatest { (filter, custom) ->
        vocabRepo.observeFilteredPageWithTotal(filter, custom)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        HskCurriculumData.builtInVocab to HskCurriculumData.builtInVocab.size
    )

    val filteredVocab: StateFlow<List<VocabWord>> = filteredPageAndTotal
        .map { it.first }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInVocab)

    val filteredTotalCount: StateFlow<Int> = filteredPageAndTotal
        .map { it.second }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInVocab.size)

    // --- RICH LEARN TAB: HSK-BY-HSK & TOPIC-BY-TOPIC LESSON COURSE ---

    private val progressPrefKey = "completed_vocab_lessons_$currentUserId"
    private val learnedWordsPrefKey = "learned_word_hanzi_$currentUserId"

    private val _learnMode = MutableStateFlow(LearnTabMode.HSK_BY_HSK)
    val learnMode: StateFlow<LearnTabMode> = _learnMode.asStateFlow()

    private val _learnHskLevel = MutableStateFlow("1")
    val learnHskLevel: StateFlow<String> = _learnHskLevel.asStateFlow()

    private val _learnTopicId = MutableStateFlow<Int?>(null)
    val learnTopicId: StateFlow<Int?> = _learnTopicId.asStateFlow()

    private val _learnChapterIndex = MutableStateFlow(0)
    val learnChapterIndex: StateFlow<Int> = _learnChapterIndex.asStateFlow()

    private val _completedVocabLessonIds = MutableStateFlow(
        progressPrefs?.getStringSet(progressPrefKey, emptySet())?.toSet() ?: emptySet()
    )
    val completedVocabLessonIds: StateFlow<Set<String>> = _completedVocabLessonIds.asStateFlow()

    private val _learnedWordHanzi = MutableStateFlow(
        progressPrefs?.getStringSet(learnedWordsPrefKey, emptySet())?.toSet() ?: emptySet()
    )
    val learnedWordHanzi: StateFlow<Set<String>> = _learnedWordHanzi.asStateFlow()

    private val _activeVocabLesson = MutableStateFlow<ActiveVocabLesson?>(null)
    val activeVocabLesson: StateFlow<ActiveVocabLesson?> = _activeVocabLesson.asStateFlow()

    private val _activeAnkiSession = MutableStateFlow<ActiveAnkiSession?>(null)
    val activeAnkiSession: StateFlow<ActiveAnkiSession?> = _activeAnkiSession.asStateFlow()

    private val _activeShortcutQuiz = MutableStateFlow<ActiveShortcutQuiz?>(null)
    val activeShortcutQuiz: StateFlow<ActiveShortcutQuiz?> = _activeShortcutQuiz.asStateFlow()

    // --- HSK 3.0 MOCK EXAM SIMULATOR STATE (persisted in SharedPreferences — zero Room changes) ---
    private val mockExamHistoryPrefKey = "hsk_mock_exam_history_${currentUserId}"
    private val _mockExamSession = MutableStateFlow<ActiveMockExamSession?>(null)
    val mockExamSession: StateFlow<ActiveMockExamSession?> = _mockExamSession.asStateFlow()

    private val _mockExamReport = MutableStateFlow<MockExamReport?>(null)
    val mockExamReport: StateFlow<MockExamReport?> = _mockExamReport.asStateFlow()

    private val _isGradingMockExam = MutableStateFlow(false)
    val isGradingMockExam: StateFlow<Boolean> = _isGradingMockExam.asStateFlow()

    private val _pastMockExams = MutableStateFlow(
        HskMockExamGenerator.deserializeHistory(progressPrefs?.getString(mockExamHistoryPrefKey, null))
    )
    val pastMockExams: StateFlow<List<PastMockExamRecord>> = _pastMockExams.asStateFlow()

    // Instant offline character decomposition map (hanzi -> CharacterAnatomy list) for active lesson cards
    private val _activeLessonWordAnatomy = MutableStateFlow<Map<String, List<CharacterAnatomy>>>(emptyMap())
    val activeLessonWordAnatomy: StateFlow<Map<String, List<CharacterAnatomy>>> = _activeLessonWordAnatomy.asStateFlow()

    // Global CharacterPop + HanziCraft Word Breakdown Sheet state (usable from Learn & HSK Topics)
    private val _inspectedWord = MutableStateFlow<VocabWord?>(null)
    val inspectedWord: StateFlow<VocabWord?> = _inspectedWord.asStateFlow()

    private val _inspectedWordAnatomy = MutableStateFlow<List<CharacterAnatomy>>(emptyList())
    val inspectedWordAnatomy: StateFlow<List<CharacterAnatomy>> = _inspectedWordAnatomy.asStateFlow()

    private val _inspectedWordStoryState = MutableStateFlow<UiState<WordEtymologyStory>?>(null)
    val inspectedWordStoryState: StateFlow<UiState<WordEtymologyStory>?> = _inspectedWordStoryState.asStateFlow()

    private var etymologyStoryJob: Job? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    val learnAllTopicCounts: StateFlow<Map<Int, Int>> = customVocabListFlow
        .flatMapLatest { customWords -> vocabRepo.observeAllTopicCounts(customWords) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyMap())

    @OptIn(ExperimentalCoroutinesApi::class)
    val learnTopicLevelSummaries: StateFlow<List<HskLevelSummary>> = _learnTopicId
        .flatMapLatest { topicId ->
            if (topicId == null) {
                vocabRepo.levelSummaries
            } else {
                vocabRepo.observeTopicLevelSummaries(topicId)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            vocabRepo.buildLevelSummaries(HskCurriculumData.builtInVocab)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val learnScopeTotalWords: StateFlow<Int> = combine(_learnHskLevel, _learnTopicId) { lvl, topic ->
        lvl to topic
    }.flatMapLatest { (lvl, topic) ->
        vocabRepo.observeScopeWordCount(lvl, topic)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val learnChapterWords: StateFlow<List<VocabWord>> = combine(
        _learnHskLevel,
        _learnTopicId,
        _learnChapterIndex
    ) { lvl, topic, chapter ->
        LearnScopeQuery(lvl, topic, chapter)
    }.flatMapLatest { query ->
        vocabRepo.observeChapterWords(
            levelFilter = query.levelFilter,
            topicId = query.topicId,
            chapterIndex = query.chapterIndex,
            chapterSize = 100
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val srsCardsState: StateFlow<UiState<List<SrsCardDoc>>> = firestoreRepo.observeSrsCards(currentUserId)
        .map<List<SrsCardDoc>, UiState<List<SrsCardDoc>>> { UiState.Success(it) }
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            val root = firestoreError ?: error
            Log.w(VM_TAG, "Error observing SRS cards", root)
            emit(UiState.Error(root.message ?: "Failed to load SRS flashcards"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private val savedStoriesFlow: StateFlow<List<SavedStoryDoc>> = firestoreRepo.observeSavedStories(currentUserId)
        .catch { error ->
            val firestoreError = generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(VM_TAG, "Error observing stories", firestoreError ?: error)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val allStories: StateFlow<List<GradedStory>> = savedStoriesFlow
        .map { saved ->
            val aiStories = saved.map { doc ->
                GradedStory(
                    id = doc.id,
                    titleZh = doc.titleZh,
                    titleEn = doc.titleEn,
                    hskLevel = doc.hskLevel.toInt().coerceIn(1, 9),
                    topicName = doc.topicName,
                    sentences = doc.sentences,
                    quizQuestion = doc.quizQuestion,
                    quizOptions = doc.quizOptions,
                    quizCorrectIndex = doc.quizCorrectIndex.toInt(),
                    aiGenerated = true
                )
            }
            aiStories + HskCurriculumData.builtInStories
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskCurriculumData.builtInStories)

    // Combined "Learned So Far" word list (SRS cards + completed lesson words) with Weak/Medium/Strong strength
    @OptIn(ExperimentalCoroutinesApi::class)
    val learnedWordEntries: StateFlow<List<LearnedWordEntry>> = combine(
        srsCardsState,
        _learnedWordHanzi,
        allVocab
    ) { srsUi, localLearnedSet, featured ->
        Triple((srsUi as? UiState.Success)?.data.orEmpty(), localLearnedSet, featured)
    }.flatMapLatest { (srsCards, localLearnedSet, featured) ->
        kotlinx.coroutines.flow.flow {
            val nowMs = System.currentTimeMillis()
            val featuredByHanzi = featured.associateBy { it.hanzi }
            val srsByHanzi = srsCards.associateBy { it.hanzi }
            val allHanzi = (srsByHanzi.keys + localLearnedSet).toSet()
            val missingFromFeatured = allHanzi.filterNot { it in featuredByHanzi }.toSet()
            val dbLookup = if (missingFromFeatured.isNotEmpty()) {
                vocabRepo.getWordsByHanziList(missingFromFeatured).associateBy { it.hanzi }
            } else emptyMap()

            val entries = allHanzi.mapNotNull { hanzi ->
                val srs = srsByHanzi[hanzi]
                val vocab = featuredByHanzi[hanzi]
                    ?: dbLookup[hanzi]
                    ?: srs?.let {
                        VocabWord(
                            key = it.wordKey,
                            hanzi = it.hanzi,
                            pinyin = it.pinyin,
                            english = it.english,
                            hskLevel = it.hskLevel.toInt().coerceIn(1, 9),
                            topicId = 1,
                            writable = true,
                            exampleHanzi = it.exampleHanzi
                        )
                    }
                if (vocab == null) return@mapNotNull null
                val strength = classifyWordStrength(srs)
                val isDue = srs == null || srs.nextReviewEpochMs <= nowMs
                LearnedWordEntry(
                    word = vocab,
                    srsCard = srs,
                    strength = strength,
                    isDue = isDue
                )
            }.sortedWith(
                compareBy<LearnedWordEntry> { it.strength.ordinal }
                    .thenBy { it.word.hskLevel }
                    .thenBy { it.word.topicId }
            )
            emit(entries)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    val hskProfileShowcase: StateFlow<HskProfileShowcase> = combine(
        profileState,
        learnedWordEntries,
        hskLevelSummaries,
        dailyTracker,
        _completedVocabLessonIds
    ) { profileUi, learnedEntries, summaries, tracker, completedLessonIds ->
        val resolvedProfile = (profileUi as? UiState.Success)?.data ?: UserProfile(
            id = currentUserId,
            userId = currentUserId,
            displayName = initialDisplayName
        )
        HskBadgeCalculator.buildShowcase(
            profile = resolvedProfile,
            learnedEntries = learnedEntries,
            levelSummaries = summaries,
            dailyTracker = tracker,
            completedVocabLessonIds = completedLessonIds
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), HskProfileShowcase())

    fun jumpToHskLevelInLearn(levelKey: String) {
        _learnMode.value = LearnTabMode.HSK_BY_HSK
        _learnHskLevel.value = levelKey
        _learnTopicId.value = null
        _learnChapterIndex.value = 0
        _selectedNavIndex.value = 0
    }

    // --- UI STATE & NAVIGATION HANDOFFS ---

    private val _selectedNavIndex = MutableStateFlow(0)
    val selectedNavIndex: StateFlow<Int> = _selectedNavIndex.asStateFlow()

    private val _activePracticeStudio = MutableStateFlow(PracticeStudioTab.HUB)
    val activePracticeStudio: StateFlow<PracticeStudioTab> = _activePracticeStudio.asStateFlow()

    private val _activeWriteWord = MutableStateFlow(
        HskCurriculumData.builtInVocab.first { it.hanzi == "扫码" }
    )
    val activeWriteWord: StateFlow<VocabWord> = _activeWriteWord.asStateFlow()

    private val _audioSpeed = MutableStateFlow(1.0f)
    val audioSpeed: StateFlow<Float> = _audioSpeed.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // --- AI TEACHER CHAT, GOOGLE SEARCH GROUNDING & LIVE VOICE STATE ---

    private val liveVoiceManager = LiveVoiceTutorManager()
    val liveVoiceState: StateFlow<LiveVoiceState> = liveVoiceManager.state
    private var liveVoiceJob: Job? = null

    private val _selectedLiveVoice = MutableStateFlow(LiveVoiceTutorManager.AVAILABLE_VOICES.first())
    val selectedLiveVoice: StateFlow<String> = _selectedLiveVoice.asStateFlow()

    private val _webSearchEnabled = MutableStateFlow(true)
    val webSearchEnabled: StateFlow<Boolean> = _webSearchEnabled.asStateFlow()

    private val _tutorMode = MutableStateFlow(TutorMode.CONVERSATION)
    val tutorMode: StateFlow<TutorMode> = _tutorMode.asStateFlow()

    private val _tutorHskLevel = MutableStateFlow(1)
    val tutorHskLevel: StateFlow<Int> = _tutorHskLevel.asStateFlow()

    private var teacherStreamJob: Job? = null

    private val _chatMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "welcome",
                fromUser = false,
                text = "你好！(Nǐ hǎo!) I'm **Panda Laoshi (熊猫老师)**, your supportive Mandarin tutor.\n" +
                    "Chat with **Google Search Grounding** for up-to-date China culture & HSK 3.0 news, switch coaching modes (Chat, Grammar, Roleplay, Tones), or tap **Live Voice** for a real-time spoken Mandarin conversation!",
                isWelcome = true
            )
        )
    )
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _isTeacherStreaming = MutableStateFlow(false)
    val isTeacherStreaming: StateFlow<Boolean> = _isTeacherStreaming.asStateFlow()

    // --- AI GENERATORS & GRADING STATES ---

    private val _isGeneratingWordPack = MutableStateFlow(false)
    val isGeneratingWordPack: StateFlow<Boolean> = _isGeneratingWordPack.asStateFlow()

    private val _sentenceGradeState = MutableStateFlow<UiState<AiSentenceGrade>?>(null)
    val sentenceGradeState: StateFlow<UiState<AiSentenceGrade>?> = _sentenceGradeState.asStateFlow()

    private val _isGeneratingStory = MutableStateFlow(false)
    val isGeneratingStory: StateFlow<Boolean> = _isGeneratingStory.asStateFlow()

    private val _isGeneratingMnemonic = MutableStateFlow(false)
    val isGeneratingMnemonic: StateFlow<Boolean> = _isGeneratingMnemonic.asStateFlow()

    private val _bonusExerciseState = MutableStateFlow<UiState<LessonQuestion>?>(null)
    val bonusExerciseState: StateFlow<UiState<LessonQuestion>?> = _bonusExerciseState.asStateFlow()

    private val _cameraScanState = MutableStateFlow<UiState<CameraPointLearnResult>?>(null)
    val cameraScanState: StateFlow<UiState<CameraPointLearnResult>?> = _cameraScanState.asStateFlow()

    private val _grammarTransformState = MutableStateFlow<UiState<GrammarTransformResult>?>(null)
    val grammarTransformState: StateFlow<UiState<GrammarTransformResult>?> = _grammarTransformState.asStateFlow()

    private val _socraticHintState = MutableStateFlow<UiState<String>?>(null)
    val socraticHintState: StateFlow<UiState<String>?> = _socraticHintState.asStateFlow()

    private val _shadowingGradeState = MutableStateFlow<UiState<ShadowingGradeResult>?>(null)
    val shadowingGradeState: StateFlow<UiState<ShadowingGradeResult>?> = _shadowingGradeState.asStateFlow()

    init {
        viewModelScope.launch {
            vocabRepo.ensureSeeded()
            firestoreRepo.ensureUserProfile(currentUserId, initialDisplayName)
            // Seed starter SM-2 cards if user's deck is completely empty on first login
            seedStarterSrsIfEmpty()
            // Automatically download & cache all ~11,092 official HSK 3.0 words into local Room SQLite
            launch { vocabRepo.syncFullHsk30Catalog(force = false) }
            // Automatically download & cache all ~9,574 character decompositions (Make Me a Hanzi) offline
            launch { etymologyRepo.ensureCharacterDictionarySynced() }
        }
    }

    fun syncFullHsk30Catalog(force: Boolean = false) {
        viewModelScope.launch {
            val result = vocabRepo.syncFullHsk30Catalog(force = force)
            result.onSuccess { total ->
                _snackbarMessage.value = "Synced $total HSK 3.0 words offline!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Failed syncing HSK 3.0 dictionary."
            }
        }
    }

    private suspend fun seedStarterSrsIfEmpty() {
        val current = srsCardsState.value
        if (current is UiState.Success && current.data.isNotEmpty()) return
        // Seed 6 essential HSK 1-3 cards (idempotent by cardId)
        val starters = listOf("w101", "w501", "w604", "w1002", "w505", "w1401")
        starters.forEach { key ->
            val word = vocabRepo.getWordByKey(key)
                ?: HskCurriculumData.builtInVocab.firstOrNull { it.key == key }
            if (word != null) {
                firestoreRepo.upsertSrsCard(currentUserId, word)
            }
        }
    }

    fun setVocabLevelFilter(levelFilter: String) {
        _vocabFilter.value = _vocabFilter.value.copy(levelFilter = levelFilter, pageSize = 60)
    }

    fun setVocabTopicFilter(topicId: Int?) {
        _vocabFilter.value = _vocabFilter.value.copy(topicId = topicId, pageSize = 60)
    }

    fun setVocabSearchQuery(query: String) {
        _vocabFilter.value = _vocabFilter.value.copy(searchQuery = query, pageSize = 60)
    }

    fun setVocabWritableOnly(writableOnly: Boolean) {
        _vocabFilter.value = _vocabFilter.value.copy(writableOnly = writableOnly, pageSize = 60)
    }

    fun setVocabHsk30NewOnly(hsk30NewOnly: Boolean) {
        _vocabFilter.value = _vocabFilter.value.copy(hsk30NewOnly = hsk30NewOnly, pageSize = 60)
    }

    fun loadMoreFilteredVocab() {
        _vocabFilter.value = _vocabFilter.value.copy(pageSize = _vocabFilter.value.pageSize + 60)
    }

    fun resetVocabFilters() {
        _vocabFilter.value = HskVocabularyFilter()
    }

    fun setLearnMode(mode: LearnTabMode) {
        _learnMode.value = mode
        _learnChapterIndex.value = 0
        when (mode) {
            LearnTabMode.HSK_BY_HSK -> {
                if (_learnHskLevel.value == "ALL") _learnHskLevel.value = "1"
                _learnTopicId.value = null
            }
            LearnTabMode.TOPIC_BY_TOPIC -> {
                _learnTopicId.value = null
                _learnHskLevel.value = "ALL"
            }
            LearnTabMode.SKILL_TREE -> Unit
            LearnTabMode.CORE_PATH -> Unit
        }
    }

    fun setLearnHskLevel(level: String) {
        _learnHskLevel.value = level
        _learnChapterIndex.value = 0
    }

    fun setLearnTopicId(topicId: Int?) {
        _learnTopicId.value = topicId
        _learnChapterIndex.value = 0
    }

    fun openTopicLearningPath(topicId: Int) {
        _learnTopicId.value = topicId
        _learnHskLevel.value = "ALL"
        _learnChapterIndex.value = 0
    }

    fun setLearnChapterIndex(chapterIndex: Int) {
        _learnChapterIndex.value = chapterIndex.coerceAtLeast(0)
    }

    fun startVocabLesson(
        lessonId: String,
        title: String,
        subtitle: String,
        hskBadge: String,
        hskLevel: Int,
        words: List<VocabWord>
    ) {
        if (words.isEmpty()) return
        viewModelScope.launch {
            val distractors = vocabRepo.getDistractorWords(
                hskLevel = hskLevel,
                excludeHanzi = words.map { it.hanzi },
                limit = 18
            )
            // Preload instant offline HanziCraft/CharacterPop character anatomy for every word in this lesson
            val anatomyMap = words.associate { w ->
                w.hanzi to etymologyRepo.getWordAnatomy(w.hanzi)
            }
            _activeLessonWordAnatomy.value = anatomyMap
            _activeVocabLesson.value = ActiveVocabLesson(
                lessonId = lessonId,
                title = title,
                subtitle = subtitle,
                hskBadge = hskBadge,
                hskLevel = hskLevel,
                words = words,
                distractors = distractors,
                xpReward = (25 + words.size * 2).coerceAtMost(50)
            )
        }
    }

    fun openWordBreakdown(word: VocabWord, forceRefresh: Boolean = false) {
        etymologyStoryJob?.cancel()
        _inspectedWord.value = word
        _inspectedWordStoryState.value = UiState.Loading
        if (!forceRefresh) {
            recordDailyActivity(storiesDelta = 1, minutesDelta = 1)
        }
        etymologyStoryJob = viewModelScope.launch {
            // Ensure character dictionary is synced if not yet populated
            etymologyRepo.ensureCharacterDictionarySynced()
            val anatomy = etymologyRepo.getWordAnatomy(word.hanzi)
            _inspectedWordAnatomy.value = anatomy
            val result = etymologyRepo.getOrGenerateWordStory(
                word = word,
                anatomy = anatomy,
                forceRefresh = forceRefresh
            )
            result.onSuccess { story ->
                _inspectedWordStoryState.value = UiState.Success(story)
            }.onFailure { err ->
                _inspectedWordStoryState.value = UiState.Error(
                    err.localizedMessage ?: "Could not load word story & history right now."
                )
            }
        }
    }

    fun regenerateWordBreakdownStory() {
        val current = _inspectedWord.value ?: return
        openWordBreakdown(current, forceRefresh = true)
    }

    fun dismissWordBreakdown() {
        etymologyStoryJob?.cancel()
        etymologyStoryJob = null
        _inspectedWord.value = null
        _inspectedWordAnatomy.value = emptyList()
        _inspectedWordStoryState.value = null
    }

    fun completeVocabLesson(lessonId: String, title: String, earnedXp: Int) {
        val activeWords = _activeVocabLesson.value?.words.orEmpty()
        val updated = _completedVocabLessonIds.value + lessonId
        _completedVocabLessonIds.value = updated
        progressPrefs?.edit()?.putStringSet(progressPrefKey, updated)?.apply()
        val newlyAcquired = markWordsAsLearned(activeWords.map { it.hanzi })
        _activeVocabLesson.value = null
        recordDailyActivity(
            lessonsDelta = 1,
            minutesDelta = 3
        )
        val newSummary = if (newlyAcquired.isNotEmpty()) {
            " · +${newlyAcquired.size} new HSK word(s) today!"
        } else {
            ""
        }
        // Award XP in Firestore without overflowing the 50-item Core Path completedUnitIds cap
        awardXp(earnedXp.toLong(), completedUnitId = null, reason = "Completed $title$newSummary")
    }

    fun dismissVocabLesson() {
        _activeVocabLesson.value = null
    }

    // --- LEARNED SO FAR, ANKI-STYLE REVIEW & VISUAL SKILL TREE ---

    private fun classifyWordStrength(srs: SrsCardDoc?): WordStrength {
        if (srs == null) return WordStrength.MEDIUM
        return when {
            srs.lapses >= 1L || srs.repetitions == 0L || srs.easeFactor < 2.15 -> WordStrength.WEAK
            srs.repetitions >= 3L && srs.intervalDays >= 6L -> WordStrength.STRONG
            else -> WordStrength.MEDIUM
        }
    }

    private fun markWordsAsLearned(hanziList: Collection<String>): List<String> {
        val clean = hanziList.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (clean.isEmpty()) return emptyList()
        val previous = _learnedWordHanzi.value
        val newlyAdded = clean.filter { it !in previous }
        if (newlyAdded.isNotEmpty()) {
            val updated = previous + newlyAdded
            _learnedWordHanzi.value = updated
            progressPrefs?.edit()?.putStringSet(learnedWordsPrefKey, updated)?.apply()
            viewModelScope.launch {
                runCatching {
                    trackerRepo.recordNewWordsAcquired(currentUserId, newlyAdded)
                }.onFailure { err ->
                    Log.w(VM_TAG, "Failed to log newly acquired words", err)
                }
            }
        }
        return newlyAdded
    }

    fun startAnkiReview(title: String, subtitle: String, entries: List<LearnedWordEntry>) {
        if (entries.isEmpty()) {
            _snackbarMessage.value = "No words match this selection yet — complete a lesson or select topics!"
            return
        }
        _activeAnkiSession.value = ActiveAnkiSession(
            id = "anki_${System.currentTimeMillis()}",
            title = title,
            subtitle = subtitle,
            entries = entries
        )
    }

    fun startDueAnkiReview() {
        val entries = learnedWordEntries.value
        val due = entries.filter { it.isDue }.ifEmpty { entries }
        if (due.isEmpty()) {
            val starterEntries = allVocab.value.take(10).map {
                LearnedWordEntry(word = it, srsCard = null, strength = WordStrength.WEAK, isDue = true)
            }
            startAnkiReview(
                title = "Anki Quick Review",
                subtitle = "Starter HSK words · Spaced Repetition",
                entries = starterEntries
            )
        } else {
            startAnkiReview(
                title = "Anki Due Review",
                subtitle = "${due.size} card(s) ready for spaced repetition",
                entries = due
            )
        }
    }

    fun startTopicAnkiReview(
        topicIds: List<Int>,
        title: String,
        onlyLearned: Boolean,
        strengthFilter: WordStrengthFilter = WordStrengthFilter.ALL
    ) {
        if (topicIds.isEmpty()) {
            _snackbarMessage.value = "Select at least one topic to review."
            return
        }
        viewModelScope.launch {
            val srsByHanzi = ((srsCardsState.value as? UiState.Success)?.data.orEmpty()).associateBy { it.hanzi }
            val nowMs = System.currentTimeMillis()

            val entries = if (onlyLearned) {
                learnedWordEntries.value
                    .filter { it.word.topicId in topicIds }
                    .filter { entry ->
                        when (strengthFilter) {
                            WordStrengthFilter.ALL -> true
                            WordStrengthFilter.WEAK -> entry.strength == WordStrength.WEAK
                            WordStrengthFilter.MEDIUM -> entry.strength == WordStrength.MEDIUM
                            WordStrengthFilter.STRONG -> entry.strength == WordStrength.STRONG
                        }
                    }
            } else {
                val words = vocabRepo.getWordsForTopics(topicIds, limit = 30)
                words.map { w ->
                    val srs = srsByHanzi[w.hanzi]
                    LearnedWordEntry(
                        word = w,
                        srsCard = srs,
                        strength = if (srs != null) classifyWordStrength(srs) else WordStrength.WEAK,
                        isDue = srs == null || srs.nextReviewEpochMs <= nowMs
                    )
                }
            }

            if (entries.isEmpty()) {
                _snackbarMessage.value = if (onlyLearned) {
                    "No learned words in selected topics with this filter yet. Switch to 'All Topic Words' to practice!"
                } else {
                    "No words found for selected topics."
                }
            } else {
                startAnkiReview(
                    title = title,
                    subtitle = "${entries.size} card(s) · Anki SM-2 Spaced Repetition",
                    entries = entries
                )
            }
        }
    }

    fun rateAnkiCard(entry: LearnedWordEntry, quality: Int) {
        if (quality >= 3) {
            markWordsAsLearned(listOf(entry.word.hanzi))
        }
        viewModelScope.launch {
            val existingCards = (srsCardsState.value as? UiState.Success)?.data.orEmpty()
            val matchedCard = entry.srsCard ?: existingCards.firstOrNull { it.hanzi == entry.word.hanzi }
            if (matchedCard != null) {
                submitSrsReview(matchedCard, quality)
            } else {
                // Upsert the word into SM-2 deck first, then apply the Anki rating
                val upsertRes = firestoreRepo.upsertSrsCard(currentUserId, entry.word)
                val sm2 = Sm2Calculator.calculate(
                    quality = quality,
                    currentReps = 0,
                    currentInterval = 1,
                    currentEase = 2.5,
                    currentLapses = 0
                )
                if (upsertRes.isSuccess) {
                    val safeCardId = firestoreRepo.buildSafeSrsDocId(entry.word.key)
                    firestoreRepo.updateSrsReview(currentUserId, safeCardId, sm2, null)
                }
                recordDailyActivity(
                    xpDelta = if (quality >= 3) 15 else 0,
                    srsDelta = 1,
                    minutesDelta = 1
                )
                if (quality >= 3) {
                    firestoreRepo.awardXpAndUnit(currentUserId, 15L)
                }
            }
        }
    }

    fun dismissAnkiSession() {
        _activeAnkiSession.value = null
    }

    fun startSkillTreeNodeLesson(
        nodeId: String,
        title: String,
        topicId: Int,
        minHsk: Int,
        maxHsk: Int
    ) {
        viewModelScope.launch {
            val words = vocabRepo.getWordsForTopicNode(
                topicId = topicId,
                minHsk = minHsk,
                maxHsk = maxHsk,
                limit = 10
            )
            if (words.isEmpty()) {
                _snackbarMessage.value = "Syncing vocabulary for $title — try again in a moment."
                return@launch
            }
            val badge = if (minHsk == maxHsk) "HSK $minHsk" else "HSK $minHsk–$maxHsk"
            startVocabLesson(
                lessonId = "tree_$nodeId",
                title = title,
                subtitle = words.take(3).joinToString(" · ") { "${it.hanzi} (${it.english.substringBefore(";")})" },
                hskBadge = badge,
                hskLevel = minHsk,
                words = words
            )
        }
    }

    fun startShortcutPlacementQuiz(targetHskLevel: Int) {
        viewModelScope.launch {
            val pool = vocabRepo.getShortcutWordsForLevel(targetHskLevel, limit = 20)
            if (pool.size < 4) {
                _snackbarMessage.value = "Not enough words cached yet for HSK $targetHskLevel shortcut."
                return@launch
            }
            val targets = pool.take(5)
            val questions = targets.map { word ->
                val wrong = pool.filter { it.hanzi != word.hanzi }.shuffled().take(3)
                    .map { it.english.substringBefore(";").trim() }
                val correctEn = word.english.substringBefore(";").trim()
                val options = (wrong + correctEn).distinct().shuffled()
                val correctIdx = options.indexOf(correctEn).coerceAtLeast(0)
                ShortcutQuizQuestion(word = word, options = options, correctIndex = correctIdx)
            }
            _activeShortcutQuiz.value = ActiveShortcutQuiz(
                targetHskLevel = targetHskLevel,
                title = "HSK $targetHskLevel Shortcut Challenge",
                questions = questions
            )
        }
    }

    fun completeShortcutQuiz(targetHskLevel: Int, passed: Boolean) {
        _activeShortcutQuiz.value = null
        if (passed) {
            updateTargetHskLevel(targetHskLevel)
            val levelKey = if (targetHskLevel >= 7) "7+" else targetHskLevel.toString()
            setLearnHskLevel(levelKey)
            val updated = _completedVocabLessonIds.value + "shortcut_hsk_$targetHskLevel"
            _completedVocabLessonIds.value = updated
            progressPrefs?.edit()?.putStringSet(progressPrefKey, updated)?.apply()
            awardXp(60L, null, "Shortcut unlocked! Jumped to HSK $targetHskLevel")
        } else {
            _snackbarMessage.value = "Score 4/5 or higher to unlock the shortcut. Keep practicing!"
        }
    }

    fun dismissShortcutQuiz() {
        _activeShortcutQuiz.value = null
    }

    fun addWordsBatchToSrs(words: List<VocabWord>) {
        if (words.isEmpty()) return
        viewModelScope.launch {
            val res = firestoreRepo.upsertSrsCardsBatch(currentUserId, words)
            res.onSuccess { count ->
                _snackbarMessage.value = "Saved $count word(s) to SM-2 Review deck!"
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not save words to review."
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun selectNavTab(index: Int) {
        _selectedNavIndex.value = index.coerceIn(0, 4)
    }

    fun selectPracticeStudio(studio: PracticeStudioTab) {
        _activePracticeStudio.value = studio
    }

    fun setAudioSpeed(speed: Float) {
        _audioSpeed.value = speed
    }

    fun speakChinese(text: String, overrideSpeed: Float? = null) {
        audioManager.speak(text, overrideSpeed ?: _audioSpeed.value)
    }

    fun jumpToWriteWord(word: VocabWord) {
        _activeWriteWord.value = word
        _activePracticeStudio.value = PracticeStudioTab.WRITE
        _selectedNavIndex.value = 2
    }

    fun jumpToWriteHanzi(hanzi: String) {
        val trimmed = hanzi.trim()
        if (trimmed.isEmpty()) return
        val localMatch = allVocab.value.firstOrNull { it.hanzi == trimmed }
        if (localMatch != null) {
            jumpToWriteWord(localMatch)
            return
        }
        viewModelScope.launch {
            val dbMatch = vocabRepo.getWordByHanzi(trimmed)
            val resolved = dbMatch ?: VocabWord(
                key = "custom_write_$trimmed",
                hanzi = trimmed,
                pinyin = "",
                english = "Custom writing practice",
                hskLevel = 1,
                topicId = 1,
                writable = true
            )
            jumpToWriteWord(resolved)
        }
    }

    fun recordWritingPracticeComplete(char: String, accuracyPercent: Int, isQuizMode: Boolean) {
        val xpReward = if (isQuizMode) 25L else 15L
        val modeLabel = if (isQuizMode) "Quiz" else "Guided"
        recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
        awardXp(
            amount = xpReward,
            completedUnitId = null,
            reason = "Mastered '$char' ($modeLabel · $accuracyPercent% accuracy)"
        )
        speakChinese(char)
    }

    fun recordPronunciationPracticeComplete(hanzi: String, pitchScore: Int) {
        recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
        if (pitchScore >= 70) {
            val xpReward = if (pitchScore >= 85) 25L else 15L
            awardXp(
                amount = xpReward,
                completedUnitId = null,
                reason = "Pitch match on '$hanzi' ($pitchScore%)"
            )
        }
    }

    fun launchTopicLessonInAiTeacher(topicName: String, hskLevel: Int) {
        _tutorHskLevel.value = hskLevel.coerceIn(1, 9)
        _selectedNavIndex.value = 4
        sendTeacherMessage(
            "Create a structured HSK 3.0 Level $hskLevel masterclass for the topic '$topicName'. " +
                "Include: (1) 5 core words with Hanzi, Pinyin, and English, (2) 1 key grammar formula, " +
                "(3) a short practical dialogue, and (4) 1 quick check question for me."
        )
    }

    fun setTutorMode(mode: TutorMode) {
        _tutorMode.value = mode
    }

    fun setTutorHskLevel(level: Int) {
        _tutorHskLevel.value = level.coerceIn(1, 9)
    }

    fun setWebSearchEnabled(enabled: Boolean) {
        _webSearchEnabled.value = enabled
    }

    fun setSelectedLiveVoice(voiceName: String) {
        _selectedLiveVoice.value = voiceName
    }

    fun startLiveVoiceConversation() {
        audioManager.stopListening()
        liveVoiceJob?.cancel()
        val level = _tutorHskLevel.value
        val voice = _selectedLiveVoice.value
        recordDailyActivity(practiceDelta = 1, minutesDelta = 2)
        liveVoiceJob = viewModelScope.launch {
            liveVoiceManager.startConversation(hskLevel = level, voiceName = voice)
        }
    }

    fun stopLiveVoiceConversation() {
        liveVoiceJob?.cancel()
        liveVoiceJob = null
        viewModelScope.launch {
            liveVoiceManager.stopConversation()
        }
    }

    fun jumpToListeningPractice() {
        _activePracticeStudio.value = PracticeStudioTab.LISTENING
        _selectedNavIndex.value = 2
    }

    fun jumpToSentencePractice() {
        _activePracticeStudio.value = PracticeStudioTab.SENTENCES
        _selectedNavIndex.value = 2
    }

    // --- DAILY TRACKER HELPERS & FOCUS STUDY TIMER ---

    fun setDailyXpGoal(xpGoal: Int) {
        displayPrefs.setDailyXpGoal(xpGoal)
    }

    fun setDailyWordsGoal(wordsGoal: Int) {
        displayPrefs.setDailyWordsGoal(wordsGoal)
    }

    fun setDailyWordGoalUnit(unit: DailyWordGoalUnit) {
        displayPrefs.setDailyWordGoalUnit(unit)
    }

    fun setDailyWordGoalHskLevel(levelKey: String) {
        displayPrefs.setDailyWordGoalHskLevel(levelKey)
    }

    fun setDailyWordGoalConfig(wordsGoal: Int, unit: DailyWordGoalUnit, levelKey: String) {
        displayPrefs.setDailyWordGoalConfig(wordsGoal, unit, levelKey)
        val scope = DailyWordGoalCalculator.formatHskScopeLabel(levelKey)
        _snackbarMessage.value = "Daily goal updated: $wordsGoal ${unit.shortUnit}/day ($scope)"
    }

    fun markWordAcquiredToday(word: VocabWord) {
        val newlyAdded = markWordsAsLearned(listOf(word.hanzi))
        if (newlyAdded.isNotEmpty()) {
            _snackbarMessage.value = "Acquired '${word.hanzi}' toward today's daily goal!"
        } else {
            _snackbarMessage.value = "'${word.hanzi}' is already in your learned vault."
        }
    }

    fun openAcquiredWordBreakdown(hanzi: String) {
        viewModelScope.launch {
            val matched = vocabRepo.getWordByHanzi(hanzi)
                ?: allVocab.value.firstOrNull { it.hanzi == hanzi }
            if (matched != null) {
                openWordBreakdown(matched)
            } else {
                speakChinese(hanzi)
            }
        }
    }

    fun startDailyWordGoalSession() {
        viewModelScope.launch {
            val snapshot = dailyTracker.value
            val levelKey = snapshot.wordGoalHskLevel
            val desiredCount = when {
                snapshot.remainingWordsToday > 0 -> snapshot.remainingWordsToday.coerceIn(5, 12)
                else -> snapshot.wordsGoal.coerceIn(5, 12)
            }
            val unlearned = vocabRepo.getNextUnlearnedWordsForLevel(
                levelFilter = levelKey,
                alreadyLearnedHanzi = _learnedWordHanzi.value,
                limit = desiredCount
            )
            if (unlearned.isEmpty()) {
                _snackbarMessage.value = "Syncing HSK 3.0 vocabulary — try again in a moment."
                return@launch
            }
            val scopeLabel = DailyWordGoalCalculator.formatHskScopeLabel(levelKey)
            val primaryLevel = unlearned.first().hskLevel
            val lessonId = "daily_goal_${snapshot.todayDateKey}_${System.currentTimeMillis()}"
            val subtitle = unlearned.take(3).joinToString(" · ") {
                "${it.hanzi} (${it.english.substringBefore(";")})"
            }
            _activeAnkiSession.value = null
            _selectedNavIndex.value = 0
            startVocabLesson(
                lessonId = lessonId,
                title = "Daily Goal Pack · $scopeLabel",
                subtitle = subtitle,
                hskBadge = scopeLabel,
                hskLevel = primaryLevel,
                words = unlearned
            )
        }
    }

    fun setFocusTimerMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 60)
        focusTimerJob?.cancel()
        focusTimerJob = null
        _focusTimerRunning.value = false
        _focusTimerDurationMin.value = clamped
        _focusTimerRemainingSec.value = clamped * 60
    }

    fun toggleFocusTimer() {
        if (_focusTimerRunning.value) {
            focusTimerJob?.cancel()
            focusTimerJob = null
            _focusTimerRunning.value = false
        } else {
            if (_focusTimerRemainingSec.value <= 0) {
                _focusTimerRemainingSec.value = _focusTimerDurationMin.value * 60
            }
            _focusTimerRunning.value = true
            focusTimerJob?.cancel()
            focusTimerJob = viewModelScope.launch {
                while (_focusTimerRemainingSec.value > 0 && _focusTimerRunning.value) {
                    delay(1_000L)
                    _focusTimerRemainingSec.value = (_focusTimerRemainingSec.value - 1).coerceAtLeast(0)
                }
                if (_focusTimerRemainingSec.value == 0 && _focusTimerRunning.value) {
                    _focusTimerRunning.value = false
                    val completedMinutes = _focusTimerDurationMin.value
                    recordDailyActivity(practiceDelta = 1, minutesDelta = completedMinutes)
                    awardXp(
                        amount = (completedMinutes * 2L).coerceIn(10L, 50L),
                        completedUnitId = null,
                        reason = "Completed ${completedMinutes}m Focus Study Session!"
                    )
                }
            }
        }
    }

    fun resetFocusTimer() {
        focusTimerJob?.cancel()
        focusTimerJob = null
        _focusTimerRunning.value = false
        _focusTimerRemainingSec.value = _focusTimerDurationMin.value * 60
    }

    private fun recordDailyActivity(
        xpDelta: Int = 0,
        wordsDelta: Int = 0,
        lessonsDelta: Int = 0,
        srsDelta: Int = 0,
        storiesDelta: Int = 0,
        practiceDelta: Int = 0,
        minutesDelta: Int = 0
    ) {
        viewModelScope.launch {
            runCatching {
                trackerRepo.recordActivity(
                    userId = currentUserId,
                    xpDelta = xpDelta,
                    wordsDelta = wordsDelta,
                    lessonsDelta = lessonsDelta,
                    srsDelta = srsDelta,
                    storiesDelta = storiesDelta,
                    practiceDelta = practiceDelta,
                    minutesDelta = minutesDelta
                )
            }.onFailure { err ->
                Log.w(VM_TAG, "Failed to log daily activity", err)
            }
        }
    }

    // --- GAMIFICATION & PROGRESS ---

    fun awardXp(amount: Long, completedUnitId: String? = null, reason: String? = null) {
        if (completedUnitId != null) {
            val unitKeyHanzi = HskCurriculumData.learningUnits
                .firstOrNull { it.id == completedUnitId }
                ?.keyHanzi
                .orEmpty()
            if (unitKeyHanzi.isNotEmpty()) {
                markWordsAsLearned(unitKeyHanzi)
            }
        }
        if (amount > 0L || completedUnitId != null) {
            recordDailyActivity(
                xpDelta = amount.toInt().coerceAtLeast(0),
                lessonsDelta = if (completedUnitId != null) 1 else 0,
                minutesDelta = if (completedUnitId != null) 3 else 0
            )
        }
        viewModelScope.launch {
            val res = firestoreRepo.awardXpAndUnit(currentUserId, amount, completedUnitId)
            if (res.isSuccess && !reason.isNullOrBlank()) {
                _snackbarMessage.value = "+$amount XP · $reason"
            } else if (res.isFailure) {
                _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Could not save XP."
            }
        }
    }

    fun updateTargetHskLevel(level: Int) {
        viewModelScope.launch {
            firestoreRepo.awardXpAndUnit(currentUserId, 0L, null, level.coerceIn(1, 9).toLong())
        }
    }

    fun deductHeart() {
        val current = (profileState.value as? UiState.Success)?.data?.hearts ?: 5L
        if (current > 0L) {
            viewModelScope.launch {
                firestoreRepo.updateHearts(currentUserId, current - 1L)
            }
        }
    }

    fun refillHearts() {
        viewModelScope.launch {
            firestoreRepo.updateHearts(currentUserId, 5L)
            _snackbarMessage.value = "Hearts restored to 5/5!"
        }
    }

    // --- SRS SPACED REPETITION ---

    fun addWordToSrs(word: VocabWord) {
        viewModelScope.launch {
            val res = firestoreRepo.upsertSrsCard(currentUserId, word)
            if (res.isSuccess) {
                _snackbarMessage.value = "Added '${word.hanzi}' to SM-2 Review deck."
            } else {
                _snackbarMessage.value = res.exceptionOrNull()?.localizedMessage ?: "Failed to add card."
            }
        }
    }

    fun addMissedWordsToSrs(wordKeys: List<String>) {
        if (wordKeys.isEmpty()) return
        viewModelScope.launch {
            var count = 0
            wordKeys.distinct().forEach { key ->
                val word = allVocab.value.firstOrNull { it.key == key }
                if (word != null) {
                    val res = firestoreRepo.upsertSrsCard(currentUserId, word)
                    if (res.isSuccess) count++
                }
            }
            if (count > 0) {
                _snackbarMessage.value = "Sent $count missed word(s) to SM-2 Review!"
            }
        }
    }

    fun submitSrsReview(card: SrsCardDoc, quality: Int) {
        viewModelScope.launch {
            val sm2 = Sm2Calculator.calculate(
                quality = quality,
                currentReps = card.repetitions.toInt(),
                currentInterval = card.intervalDays.toInt(),
                currentEase = card.easeFactor,
                currentLapses = card.lapses.toInt()
            )
            var mnemonic: String? = card.mnemonicHint
            // 3-Strike AI Mnemonic Rescue
            if (quality < 3 && sm2.lapses >= 3 && mnemonic.isNullOrBlank()) {
                _isGeneratingMnemonic.value = true
                val aiRes = aiRepo.generateMnemonicRescue(card.hanzi, card.pinyin, card.english)
                _isGeneratingMnemonic.value = false
                if (aiRes.isSuccess) {
                    mnemonic = aiRes.getOrNull()
                    _snackbarMessage.value = "3-Strike AI Mnemonic Rescue unlocked for ${card.hanzi}!"
                }
            }
            val saveRes = firestoreRepo.updateSrsReview(currentUserId, card.id, sm2, mnemonic)
            if (saveRes.isSuccess) {
                if (quality >= 3) {
                    markWordsAsLearned(listOf(card.hanzi))
                }
                recordDailyActivity(
                    xpDelta = if (quality >= 3) 15 else 0,
                    srsDelta = 1,
                    minutesDelta = 1
                )
                if (quality >= 3) {
                    firestoreRepo.awardXpAndUnit(currentUserId, 15L)
                    _snackbarMessage.value = "+15 XP · Next review in ${sm2.intervalDays} day(s)"
                } else {
                    _snackbarMessage.value = "Card queued for quick re-study."
                }
            } else {
                _snackbarMessage.value = saveRes.exceptionOrNull()?.localizedMessage ?: "Failed to update card."
            }
        }
    }

    fun requestMnemonicForCard(card: SrsCardDoc) {
        viewModelScope.launch {
            _isGeneratingMnemonic.value = true
            val aiRes = aiRepo.generateMnemonicRescue(card.hanzi, card.pinyin, card.english)
            _isGeneratingMnemonic.value = false
            aiRes.onSuccess { hint ->
                val unchanged = com.example.model.Sm2Result(
                    repetitions = card.repetitions.toInt(),
                    intervalDays = card.intervalDays.toInt(),
                    easeFactor = card.easeFactor,
                    lapses = card.lapses.toInt(),
                    nextReviewEpochMs = card.nextReviewEpochMs
                )
                firestoreRepo.updateSrsReview(currentUserId, card.id, unchanged, hint)
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Could not generate mnemonic."
            }
        }
    }

    // --- CUSTOM WORDS: AI GENERATOR & BULK CSV IMPORTER ---

    fun generateAiWordPack(hskLevel: Int, topicId: Int, topicPrompt: String, onDone: () -> Unit) {
        if (topicPrompt.isBlank()) {
            _snackbarMessage.value = "Enter a topic theme first."
            return
        }
        viewModelScope.launch {
            _isGeneratingWordPack.value = true
            val aiRes = aiRepo.generateTopicWordPack(hskLevel, topicId, topicPrompt)
            aiRes.onSuccess { words ->
                val saveRes = firestoreRepo.addCustomWords(currentUserId, words)
                saveRes.onSuccess { count ->
                    _snackbarMessage.value = "Generated & saved $count HSK $hskLevel words!"
                    firestoreRepo.awardXpAndUnit(currentUserId, 25L)
                    onDone()
                }.onFailure { err ->
                    _snackbarMessage.value = err.localizedMessage ?: "Failed saving generated words."
                }
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "AI word pack generation failed."
            }
            _isGeneratingWordPack.value = false
        }
    }

    fun importCsvWords(csvText: String, onDone: () -> Unit) {
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }
        if (lines.isEmpty()) {
            _snackbarMessage.value = "Paste CSV lines in format: hanzi,pinyin,english,level,topicId"
            return
        }
        val parsed = lines.mapIndexedNotNull { idx, line ->
            val parts = line.split(",").map { it.trim() }
            if (parts.size < 3) return@mapIndexedNotNull null
            val hanzi = parts[0]
            val pinyin = parts[1]
            val english = parts[2]
            val level = parts.getOrNull(3)?.toIntOrNull()?.coerceIn(1, 9) ?: 1
            val topicId = parts.getOrNull(4)?.toIntOrNull()?.coerceIn(1, 14) ?: 1
            if (hanzi.isBlank() || pinyin.isBlank() || english.isBlank()) return@mapIndexedNotNull null
            VocabWord(
                key = "csv_${System.currentTimeMillis()}_$idx",
                hanzi = hanzi,
                pinyin = pinyin,
                english = english,
                hskLevel = level,
                topicId = topicId,
                writable = true
            )
        }
        if (parsed.isEmpty()) {
            _snackbarMessage.value = "No valid rows found. Use: hanzi,pinyin,english,level,topicId"
            return
        }
        viewModelScope.launch {
            val res = firestoreRepo.addCustomWords(currentUserId, parsed)
            res.onSuccess { count ->
                _snackbarMessage.value = "Imported $count vocabulary words into your cloud library!"
                onDone()
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Import failed."
            }
        }
    }

    // --- AI SENTENCE GRADING ---

    fun gradeFreeSentence(targetWord: VocabWord, sentence: String) {
        if (sentence.isBlank()) {
            _snackbarMessage.value = "Type or dictate a Chinese sentence first."
            return
        }
        viewModelScope.launch {
            _sentenceGradeState.value = UiState.Loading
            val res = aiRepo.gradeFreeSentence(targetWord, sentence.trim())
            res.onSuccess { grade ->
                _sentenceGradeState.value = UiState.Success(grade)
                recordDailyActivity(
                    xpDelta = if (grade.score >= 70) 20 else 0,
                    practiceDelta = 1,
                    minutesDelta = 1
                )
                if (grade.score >= 70) {
                    firestoreRepo.awardXpAndUnit(currentUserId, 20L)
                }
            }.onFailure { err ->
                _sentenceGradeState.value = UiState.Error(
                    err.localizedMessage ?: "Could not grade sentence right now."
                )
            }
        }
    }

    fun clearSentenceGrade() {
        _sentenceGradeState.value = null
    }

    // --- AI GRADED STORY GENERATOR ---

    fun generateAndSaveAiStory(
        hskLevel: Int,
        topicName: String,
        useUnlockedVocabOnly: Boolean = false,
        onCreated: (GradedStory) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingStory.value = true
            val unlocked = if (useUnlockedVocabOnly) {
                learnedWordEntries.value.map { it.word.hanzi }.distinct()
            } else emptyList()
            val res = aiRepo.generateGradedStory(hskLevel, topicName, unlocked)
            res.onSuccess { story ->
                firestoreRepo.saveGeneratedStory(currentUserId, story)
                firestoreRepo.awardXpAndUnit(currentUserId, 25L)
                recordDailyActivity(xpDelta = 25, storiesDelta = 1, minutesDelta = 2)
                val suffix = if (unlocked.isNotEmpty()) " (built from your unlocked vocabulary)" else ""
                _snackbarMessage.value = "New HSK $hskLevel story '${story.titleZh}' created!$suffix"
                onCreated(story)
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Story generation failed."
            }
            _isGeneratingStory.value = false
        }
    }

    // --- CAMERA POINT & LEARN, GRAMMAR TRANSFORMER, SOCRATIC HINT & SHADOWING GRADER ---

    fun analyzeCameraPhoto(bitmap: Bitmap) {
        viewModelScope.launch {
            _cameraScanState.value = UiState.Loading
            val targetLvl = (profileState.value as? UiState.Success)?.data?.currentHskLevel?.toInt()?.coerceIn(1, 9) ?: 1
            val res = aiRepo.analyzePhotoPointAndLearn(bitmap, targetLvl)
            res.onSuccess { result ->
                _cameraScanState.value = UiState.Success(result)
                recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
                awardXp(15L, null, "Scanned '${result.hanzi}' with Camera Point & Learn")
                speakChinese(result.hanzi)
            }.onFailure { err ->
                _cameraScanState.value = UiState.Error(err.localizedMessage ?: "Could not analyze photo right now.")
            }
        }
    }

    fun clearCameraScan() {
        _cameraScanState.value = null
    }

    fun transformGrammarPattern(baseSentence: String, label: String, instruction: String) {
        if (baseSentence.isBlank()) return
        viewModelScope.launch {
            _grammarTransformState.value = UiState.Loading
            val res = aiRepo.transformGrammarPattern(baseSentence.trim(), label, instruction)
            res.onSuccess { result ->
                _grammarTransformState.value = UiState.Success(result)
                recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
                awardXp(10L, null, "Transformed sentence ($label)")
                speakChinese(result.transformedHanzi)
            }.onFailure { err ->
                _grammarTransformState.value = UiState.Error(err.localizedMessage ?: "Grammar transformation failed.")
            }
        }
    }

    fun clearGrammarTransform() {
        _grammarTransformState.value = null
    }

    fun requestSocraticHint(promptEn: String, userOrderHanzi: String, expectedHanzi: String) {
        viewModelScope.launch {
            _socraticHintState.value = UiState.Loading
            val res = aiRepo.generateSocraticMistakeHint(promptEn, userOrderHanzi, expectedHanzi)
            res.onSuccess { hint ->
                _socraticHintState.value = UiState.Success(hint)
            }.onFailure { err ->
                _socraticHintState.value = UiState.Error(err.localizedMessage ?: "Could not load hint.")
            }
        }
    }

    fun clearSocraticHint() {
        _socraticHintState.value = null
    }

    fun gradeShadowingDialogue(targetScript: String, targetPinyin: String, recognizedSpeech: String, hskLevel: Int) {
        if (recognizedSpeech.isBlank()) return
        viewModelScope.launch {
            _shadowingGradeState.value = UiState.Loading
            val res = aiRepo.gradeShadowingAttempt(targetScript, targetPinyin, recognizedSpeech, hskLevel)
            res.onSuccess { grade ->
                _shadowingGradeState.value = UiState.Success(grade)
                recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
                if (grade.fluencyScore >= 70) {
                    awardXp(20L, null, "Shadowing score ${grade.fluencyScore}%")
                }
            }.onFailure { err ->
                _shadowingGradeState.value = UiState.Error(err.localizedMessage ?: "Shadowing analysis failed.")
            }
        }
    }

    fun clearShadowingGrade() {
        _shadowingGradeState.value = null
    }

    // --- AI BONUS EXERCISE ---

    fun generateAiBonusExercise(hskLevel: Int) {
        viewModelScope.launch {
            _bonusExerciseState.value = UiState.Loading
            val res = aiRepo.generateBonusExercise(hskLevel)
            res.onSuccess { q ->
                _bonusExerciseState.value = UiState.Success(q)
            }.onFailure { err ->
                _bonusExerciseState.value = UiState.Error(err.localizedMessage ?: "Failed to generate exercise.")
            }
        }
    }

    fun dismissBonusExercise() {
        _bonusExerciseState.value = null
    }

    // --- AI TEACHER STREAMING CHAT ---

    fun sendTeacherMessage(prompt: String, appendUserBubble: Boolean = true) {
        val clean = prompt.trim()
        if (clean.isEmpty() || _isTeacherStreaming.value) return

        val historySnapshot = _chatMessages.value
        val activeLevel = _tutorHskLevel.value
        val activeMode = _tutorMode.value
        val useWebSearch = _webSearchEnabled.value
        val assistantId = UUID.randomUUID().toString()
        val modeBadge = buildString {
            append("HSK $activeLevel · ${activeMode.label}")
            if (useWebSearch) append(" · Google Search")
        }
        val placeholder = AiChatMessage(
            id = assistantId,
            fromUser = false,
            text = "",
            streaming = true,
            modeLabel = modeBadge
        )

        _chatMessages.value = if (appendUserBubble) {
            val userMsg = AiChatMessage(
                id = UUID.randomUUID().toString(),
                fromUser = true,
                text = clean
            )
            _chatMessages.value + userMsg + placeholder
        } else {
            _chatMessages.value + placeholder
        }
        _isTeacherStreaming.value = true
        if (appendUserBubble) {
            recordDailyActivity(practiceDelta = 1, minutesDelta = 1)
        }

        teacherStreamJob = viewModelScope.launch {
            val builder = StringBuilder()
            val accumulatedSources = linkedMapOf<String, GroundedSource>()
            val accumulatedQueries = linkedSetOf<String>()
            var entryPointHtml: String? = null
            try {
                aiRepo.streamTeacherReply(
                    history = historySnapshot,
                    userPrompt = clean,
                    hskLevel = activeLevel,
                    mode = activeMode,
                    useGoogleSearch = useWebSearch
                ).collect { chunk ->
                    if (chunk.textDelta.isNotEmpty()) {
                        builder.append(chunk.textDelta)
                    }
                    chunk.sources.forEach { src -> accumulatedSources[src.uri] = src }
                    accumulatedQueries.addAll(chunk.searchQueries)
                    if (!chunk.searchEntryPointHtml.isNullOrBlank()) {
                        entryPointHtml = chunk.searchEntryPointHtml
                    }

                    val currentText = builder.toString()
                    val currentSources = accumulatedSources.values.toList()
                    val currentQueries = accumulatedQueries.toList()
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == assistantId) {
                            msg.copy(
                                text = currentText,
                                streaming = true,
                                sources = currentSources,
                                searchQueries = currentQueries,
                                searchEntryPointHtml = entryPointHtml
                            )
                        } else msg
                    }
                }
                val finalText = builder.toString().trim()
                val finalSources = accumulatedSources.values.toList()
                val finalQueries = accumulatedQueries.toList()
                if (finalText.isNotEmpty()) {
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == assistantId) {
                            msg.copy(
                                text = finalText,
                                streaming = false,
                                sources = finalSources,
                                searchQueries = finalQueries,
                                searchEntryPointHtml = entryPointHtml
                            )
                        } else msg
                    }
                } else {
                    _chatMessages.value = _chatMessages.value.map { msg ->
                        if (msg.id == assistantId) {
                            msg.copy(
                                text = "Panda Laoshi returned an empty reply. Tap Retry to ask again.",
                                streaming = false,
                                isError = true,
                                failedPrompt = clean
                            )
                        } else msg
                    }
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(VM_TAG, "Teacher stream timed out", e)
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == assistantId) {
                        msg.copy(
                            text = "Panda Laoshi timed out after 60 seconds. Check your connection and tap Retry.",
                            streaming = false,
                            isError = true,
                            failedPrompt = clean
                        )
                    } else msg
                }
            } catch (e: CancellationException) {
                val partial = builder.toString().trim()
                _chatMessages.value = _chatMessages.value.mapNotNull { msg ->
                    if (msg.id != assistantId) msg
                    else if (partial.isNotEmpty()) msg.copy(text = partial, streaming = false)
                    else null
                }
                throw e
            } catch (e: Exception) {
                Log.e(VM_TAG, "Teacher stream failed", e)
                val detail = e.localizedMessage?.takeIf { it.isNotBlank() } ?: "Unable to reach Panda Laoshi right now."
                _chatMessages.value = _chatMessages.value.map { msg ->
                    if (msg.id == assistantId) {
                        msg.copy(
                            text = detail,
                            streaming = false,
                            isError = true,
                            failedPrompt = clean
                        )
                    } else msg
                }
            } finally {
                _isTeacherStreaming.value = false
                teacherStreamJob = null
            }
        }
    }

    fun stopTeacherStream() {
        teacherStreamJob?.cancel()
        teacherStreamJob = null
        _isTeacherStreaming.value = false
    }

    fun retryTeacherMessage(errorMessageId: String, failedPrompt: String) {
        if (_isTeacherStreaming.value) return
        _chatMessages.value = _chatMessages.value.filterNot { it.id == errorMessageId }
        sendTeacherMessage(prompt = failedPrompt, appendUserBubble = false)
    }

    fun clearTeacherChat() {
        stopTeacherStream()
        _chatMessages.value = listOf(
            AiChatMessage(
                id = "welcome_reset_${System.currentTimeMillis()}",
                fromUser = false,
                text = "你好！(Nǐ hǎo!) Fresh conversation started at **HSK ${_tutorHskLevel.value} (${_tutorMode.value.label})**.\n" +
                    "Write in English or Chinese, toggle **Web Search** for up-to-date answers, or tap **Live Voice** to speak in real time!",
                isWelcome = true
            )
        )
    }

    // --- HSK 3.0 MOCK EXAM SIMULATOR ACTIONS ---

    fun startHskMockExam(level: HskMockLevel, mode: HskMockExamMode) {
        viewModelScope.launch {
            _mockExamReport.value = null
            _isGradingMockExam.value = false
            val targetLimit = (mode.totalQuestions * 2).coerceAtLeast(40)
            val levelWords = runCatching {
                vocabRepo.getShortcutWordsForLevel(level.targetNumericLevel, limit = targetLimit)
            }.getOrDefault(emptyList())
            val distractors = runCatching {
                vocabRepo.getDistractorWords(level.targetNumericLevel, emptyList(), limit = 40)
            }.getOrDefault(emptyList())

            _mockExamSession.value = HskMockExamGenerator.generateExam(
                level = level,
                mode = mode,
                levelWords = levelWords,
                distractorPool = distractors
            )
        }
    }

    fun submitHskMockExam(
        answers: Map<String, MockQuestionAnswer>,
        elapsedSeconds: Int
    ) {
        val session = _mockExamSession.value ?: return
        if (_isGradingMockExam.value) return

        viewModelScope.launch {
            _isGradingMockExam.value = true
            try {
                var listeningEarned = 0.0
                var listeningMax = 0
                var readingEarned = 0.0
                var readingMax = 0
                var writingEarned = 0.0
                var writingMax = 0

                val mistakes = mutableListOf<MockExamMistakeItem>()
                val missedWords = linkedMapOf<String, VocabWord>()

                session.questions.forEachIndexed { idx, q ->
                    val ans = answers[q.id] ?: MockQuestionAnswer()
                    val qNum = idx + 1

                    when (q.kind) {
                        MockQuestionKind.LISTENING_AUDIO_MCQ -> {
                            listeningMax++
                            val correct = ans.selectedOptionIndex == q.correctOptionIndex
                            if (correct) {
                                listeningEarned += 1.0
                            } else {
                                val userText = ans.selectedOptionIndex?.let { q.options.getOrNull(it) } ?: "No answer selected"
                                val correctText = q.options.getOrNull(q.correctOptionIndex).orEmpty()
                                q.targetWords.forEach { missedWords[it.hanzi] = it }
                                mistakes.add(
                                    MockExamMistakeItem(
                                        questionNumber = qNum,
                                        section = MockExamSection.LISTENING,
                                        promptSummary = q.promptText,
                                        userAnswerDisplay = userText,
                                        correctHanzi = q.hanziPrompt,
                                        correctPinyin = q.pinyinPrompt,
                                        correctEnglish = correctText,
                                        feedbackNote = q.explanation,
                                        missedWords = q.targetWords
                                    )
                                )
                            }
                        }

                        MockQuestionKind.READING_HANZI_MEANING,
                        MockQuestionKind.READING_COMPREHENSION,
                        MockQuestionKind.READING_CLOZE -> {
                            readingMax++
                            val correct = ans.selectedOptionIndex == q.correctOptionIndex
                            if (correct) {
                                readingEarned += 1.0
                            } else {
                                val userText = ans.selectedOptionIndex?.let { q.options.getOrNull(it) } ?: "No answer selected"
                                val correctOpt = q.options.getOrNull(q.correctOptionIndex).orEmpty()
                                val primaryWord = q.targetWords.firstOrNull()
                                q.targetWords.forEach { missedWords[it.hanzi] = it }
                                mistakes.add(
                                    MockExamMistakeItem(
                                        questionNumber = qNum,
                                        section = MockExamSection.READING,
                                        promptSummary = q.hanziPrompt,
                                        userAnswerDisplay = userText,
                                        correctHanzi = primaryWord?.hanzi ?: correctOpt,
                                        correctPinyin = primaryWord?.pinyin ?: q.pinyinPrompt,
                                        correctEnglish = if (q.kind == MockQuestionKind.READING_CLOZE) {
                                            "Missing word: $correctOpt (${primaryWord?.english.orEmpty()})"
                                        } else correctOpt,
                                        feedbackNote = q.explanation,
                                        missedWords = q.targetWords
                                    )
                                )
                            }
                        }

                        MockQuestionKind.WRITING_TILE_ORDER -> {
                            writingMax++
                            val assembled = ans.assembledTileIndices
                                .mapNotNull { q.scrambledTiles.getOrNull(it)?.first }
                                .joinToString("")
                            val correct = assembled == q.expectedSentenceHanzi
                            if (correct) {
                                writingEarned += 1.0
                            } else {
                                q.targetWords.forEach { missedWords[it.hanzi] = it }
                                mistakes.add(
                                    MockExamMistakeItem(
                                        questionNumber = qNum,
                                        section = MockExamSection.WRITING,
                                        promptSummary = q.hanziPrompt,
                                        userAnswerDisplay = assembled.ifBlank { "No tiles placed" },
                                        correctHanzi = q.expectedSentenceHanzi,
                                        correctPinyin = q.expectedSentencePinyin,
                                        correctEnglish = q.hanziPrompt,
                                        feedbackNote = q.explanation,
                                        missedWords = q.targetWords
                                    )
                                )
                            }
                        }

                        MockQuestionKind.WRITING_FREE_SENTENCE -> {
                            writingMax++
                            val userText = ans.typedSentence.trim()
                            if (userText.isBlank()) {
                                q.targetWords.forEach { missedWords[it.hanzi] = it }
                                mistakes.add(
                                    MockExamMistakeItem(
                                        questionNumber = qNum,
                                        section = MockExamSection.WRITING,
                                        promptSummary = "Target words: ${q.targetWords.joinToString(" + ") { it.hanzi }}",
                                        userAnswerDisplay = "No sentence written",
                                        correctHanzi = q.targetWords.joinToString(" · ") { it.hanzi },
                                        correctPinyin = q.targetWords.joinToString(" · ") { it.pinyin },
                                        correctEnglish = q.targetWords.joinToString(" / ") { it.english },
                                        feedbackNote = q.explanation,
                                        missedWords = q.targetWords
                                    )
                                )
                            } else {
                                // Try Gemini AI grader via existing PandaAiRepository; fall back cleanly offline
                                val compositeTarget = VocabWord(
                                    key = q.id,
                                    hanzi = q.targetWords.joinToString(" & ") { it.hanzi },
                                    pinyin = q.targetWords.joinToString(" / ") { it.pinyin },
                                    english = q.targetWords.joinToString("; ") { it.english },
                                    hskLevel = session.level.targetNumericLevel,
                                    topicId = q.targetWords.firstOrNull()?.topicId ?: 1
                                )
                                val aiRes = aiRepo.gradeFreeSentence(compositeTarget, userText)
                                val (score100, correctedZh, correctedPy, note) = if (aiRes.isSuccess) {
                                    val g = aiRes.getOrThrow()
                                    val containsBothBonus = if (q.targetWords.all { userText.contains(it.hanzi) }) 0 else -15
                                    val adj = (g.score + containsBothBonus).coerceIn(0, 100)
                                    listOf(
                                        adj,
                                        g.correctedHanzi.ifBlank { userText },
                                        g.correctedPinyin,
                                        "${g.verdict} (${adj}/100): ${g.grammarAnalysis}"
                                    )
                                } else {
                                    val (offlineScore, offlineNote) = HskMockExamGenerator.scoreFreeSentenceOffline(
                                        userSentence = userText,
                                        targetWords = q.targetWords
                                    )
                                    listOf(
                                        offlineScore,
                                        q.targetWords.joinToString(" · ") { it.hanzi },
                                        q.targetWords.joinToString(" · ") { it.pinyin },
                                        offlineNote
                                    )
                                }

                                val numericScore = score100 as Int
                                writingEarned += (numericScore / 100.0)

                                if (numericScore < 75) {
                                    q.targetWords.forEach { missedWords[it.hanzi] = it }
                                    mistakes.add(
                                        MockExamMistakeItem(
                                            questionNumber = qNum,
                                            section = MockExamSection.WRITING,
                                            promptSummary = "Target words: ${q.targetWords.joinToString(" + ") { it.hanzi }}",
                                            userAnswerDisplay = userText,
                                            correctHanzi = correctedZh as String,
                                            correctPinyin = correctedPy as String,
                                            correctEnglish = q.targetWords.joinToString(" / ") { "${it.hanzi}: ${it.english}" },
                                            feedbackNote = note as String,
                                            missedWords = q.targetWords
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                val lScore = if (listeningMax > 0) ((listeningEarned / listeningMax) * 100.0).toInt().coerceIn(0, 100) else 0
                val rScore = if (readingMax > 0) ((readingEarned / readingMax) * 100.0).toInt().coerceIn(0, 100) else 0
                val wScore = if (writingMax > 0) ((writingEarned / writingMax) * 100.0).toInt().coerceIn(0, 100) else 0
                val totalScore = (lScore + rScore + wScore).coerceIn(0, 300)
                val passed = totalScore >= 180

                val minutesLogged = ((elapsedSeconds + 59) / 60).coerceAtLeast(1)
                val xpEarned = if (passed) {
                    if (session.mode == HskMockExamMode.FULL) 120 else 65
                } else {
                    if (session.mode == HskMockExamMode.FULL) 50 else 30
                }
                val now = System.currentTimeMillis()

                // Persist to SharedPreferences history (zero Room DB modifications!)
                val record = PastMockExamRecord(
                    id = session.examId,
                    levelLabel = session.level.label,
                    modeLabel = session.mode.label,
                    dateLabel = HskMockExamGenerator.formatExamDate(now),
                    totalScore = totalScore,
                    listeningScore = lScore,
                    readingScore = rScore,
                    writingScore = wScore,
                    passed = passed,
                    timestampEpochMs = now
                )
                val updatedHistory = (listOf(record) + _pastMockExams.value).take(25)
                _pastMockExams.value = updatedHistory
                progressPrefs?.edit()
                    ?.putString(mockExamHistoryPrefKey, HskMockExamGenerator.serializeHistory(updatedHistory))
                    ?.apply()

                // Log XP & study minutes to Daily Learning Tracker + profile XP
                recordDailyActivity(
                    xpDelta = xpEarned,
                    practiceDelta = 1,
                    minutesDelta = minutesLogged
                )
                firestoreRepo.awardXpAndUnit(currentUserId, xpEarned.toLong())

                _mockExamReport.value = MockExamReport(
                    examId = session.examId,
                    level = session.level,
                    mode = session.mode,
                    timestampEpochMs = now,
                    listeningScore = lScore,
                    readingScore = rScore,
                    writingScore = wScore,
                    totalScore = totalScore,
                    passed = passed,
                    elapsedSeconds = elapsedSeconds,
                    mistakes = mistakes,
                    missedVocabWords = missedWords.values.toList(),
                    xpEarned = xpEarned,
                    minutesLogged = minutesLogged
                )
                _mockExamSession.value = null
            } finally {
                _isGradingMockExam.value = false
            }
        }
    }

    fun addMockExamMissedWordsToAnki(words: List<VocabWord>) {
        if (words.isEmpty()) return
        viewModelScope.launch {
            var added = 0
            words.distinctBy { it.hanzi }.forEach { word ->
                val res = firestoreRepo.upsertSrsCard(currentUserId, word)
                if (res.isSuccess) added++
            }
            if (added > 0) {
                recordDailyActivity(srsDelta = added)
                _snackbarMessage.value = "Added $added missed mock exam word(s) to Anki SM-2 Review!"
            } else {
                _snackbarMessage.value = "Could not add missed words right now."
            }
        }
    }

    fun exitHskMockExam() {
        _mockExamSession.value = null
        _mockExamReport.value = null
        _isGradingMockExam.value = false
    }

    // --- MEASURE WORDS & NEAR-SYNONYMS BATTLE LAB ---
    private val _synonymExtraExamples = MutableStateFlow<Map<String, UiState<String>>>(emptyMap())
    val synonymExtraExamples: StateFlow<Map<String, UiState<String>>> = _synonymExtraExamples.asStateFlow()

    fun requestSynonymExtraExamples(battle: SynonymBattleItem) {
        val existing = _synonymExtraExamples.value[battle.id]
        if (existing is UiState.Loading) return
        _synonymExtraExamples.value = _synonymExtraExamples.value + (battle.id to UiState.Loading)
        viewModelScope.launch {
            try {
                val prompt = buildString {
                    append("Compare the Mandarin near-synonyms ${battle.title} (HSK ${battle.hskLevel}). ")
                    append("Provide EXACTLY 2 short, practical extra contrast example sentences. ")
                    append("Format each line cleanly as: 1) Hanzi (Pinyin) — English [Short grammar note]. ")
                    append("No intro or outro text.")
                }
                val builder = StringBuilder()
                withTimeout(25_000L) {
                    aiRepo.streamTeacherReply(
                        history = emptyList(),
                        userPrompt = prompt,
                        hskLevel = battle.hskLevel,
                        mode = TutorMode.GRAMMAR,
                        useGoogleSearch = false
                    ).collect { chunk ->
                        builder.append(chunk.textDelta)
                    }
                }
                val generated = builder.toString().trim()
                val finalContent = if (generated.isNotBlank()) {
                    generated
                } else {
                    "Offline Panda Laoshi Backup:\n${battle.offlineExtraExamples}"
                }
                _synonymExtraExamples.value = _synonymExtraExamples.value +
                    (battle.id to UiState.Success(finalContent))
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                Log.w(VM_TAG, "Using offline fallback synonym examples for ${battle.id}", e)
                _synonymExtraExamples.value = _synonymExtraExamples.value +
                    (battle.id to UiState.Success("Offline Panda Laoshi Backup:\n${battle.offlineExtraExamples}"))
            }
        }
    }

    fun completeMeasureOrSynonymRound(
        modeLabel: String,
        correctCount: Int,
        totalCount: Int,
        minutesStudied: Int = 2
    ) {
        val xpEarned = (correctCount * 4 + 10).coerceIn(10, 60)
        recordDailyActivity(
            xpDelta = xpEarned,
            practiceDelta = 1,
            minutesDelta = minutesStudied.coerceAtLeast(1)
        )
        viewModelScope.launch {
            firestoreRepo.awardXpAndUnit(currentUserId, xpEarned.toLong())
            _snackbarMessage.value = "+$xpEarned XP · $modeLabel ($correctCount/$totalCount)"
        }
    }

    fun saveMeasureOrSynonymItemsToAnki(words: List<VocabWord>, sourceLabel: String = "Measure & Synonyms") {
        if (words.isEmpty()) return
        viewModelScope.launch {
            var added = 0
            words.distinctBy { it.hanzi }.forEach { word ->
                val res = firestoreRepo.upsertSrsCard(currentUserId, word)
                if (res.isSuccess) added++
            }
            if (added > 0) {
                recordDailyActivity(srsDelta = added)
                _snackbarMessage.value = "Saved $added $sourceLabel item(s) to Anki SM-2 Review!"
            } else {
                _snackbarMessage.value = "Could not save items to Anki right now."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        focusTimerJob?.cancel()
        liveVoiceManager.closeSync()
        audioManager.release()
    }
}
