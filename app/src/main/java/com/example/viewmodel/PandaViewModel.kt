package com.example.viewmodel

import android.content.SharedPreferences
import android.util.Log
import com.example.audio.ChineseAudioManager
import com.example.audio.LiveVoiceTutorManager
import com.example.data.AppDisplayPrefs
import com.example.data.DailyLearningTrackerRepository
import com.example.data.HanziEtymologyRepository
import com.example.data.HskCurriculumData
import com.example.data.HskVocabularyRepository
import com.example.data.PandaAiRepository
import com.example.data.PandaFirestoreRepository
import com.example.data.local.CharacterAnatomy
import com.example.data.local.DailyTrackerDateUtil
import com.example.data.local.DailyTrackerSnapshot
import com.example.data.local.HskCatalogSyncState
import com.example.data.local.HskLevelSummary
import com.example.data.local.HskVocabularyFilter
import com.example.data.local.WordEtymologyStory
import com.example.model.AiChatMessage
import com.example.model.AiSentenceGrade
import com.example.model.CustomWordDoc
import com.example.model.GradedStory
import com.example.model.GroundedSource
import com.example.model.LessonQuestion
import com.example.model.LiveVoiceState
import com.example.model.SavedStoryDoc
import com.example.model.Sm2Calculator
import com.example.model.SrsCardDoc
import com.example.model.TopicCategory
import com.example.model.TutorMode
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VocabWord
import com.google.firebase.firestore.FirebaseFirestoreException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
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
    WRITE("Write"),
    SPEAK("Speak"),
    LISTENING("Listening"),
    SENTENCES("Sentences"),
    RADICALS("Radicals")
}

enum class LearnTabMode(val label: String) {
    HSK_BY_HSK("HSK by HSK"),
    TOPIC_BY_TOPIC("Topic by Topic"),
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

    private val _activeVocabLesson = MutableStateFlow<ActiveVocabLesson?>(null)
    val activeVocabLesson: StateFlow<ActiveVocabLesson?> = _activeVocabLesson.asStateFlow()

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

    // --- UI STATE & NAVIGATION HANDOFFS ---

    private val _selectedNavIndex = MutableStateFlow(0)
    val selectedNavIndex: StateFlow<Int> = _selectedNavIndex.asStateFlow()

    private val _activePracticeStudio = MutableStateFlow(PracticeStudioTab.WRITE)
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
        val wordsCount = _activeVocabLesson.value?.words?.size ?: 10
        val updated = _completedVocabLessonIds.value + lessonId
        _completedVocabLessonIds.value = updated
        progressPrefs?.edit()?.putStringSet(progressPrefKey, updated)?.apply()
        _activeVocabLesson.value = null
        recordDailyActivity(
            wordsDelta = wordsCount,
            lessonsDelta = 1,
            minutesDelta = 3
        )
        // Award XP in Firestore without overflowing the 50-item Core Path completedUnitIds cap
        awardXp(earnedXp.toLong(), completedUnitId = null, reason = "Completed $title")
    }

    fun dismissVocabLesson() {
        _activeVocabLesson.value = null
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
        val match = allVocab.value.firstOrNull { it.hanzi == trimmed }
            ?: VocabWord(
                key = "custom_write_$trimmed",
                hanzi = trimmed,
                pinyin = "",
                english = "Custom writing practice",
                hskLevel = 1,
                topicId = 1,
                writable = true
            )
        jumpToWriteWord(match)
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
        if (amount > 0L || completedUnitId != null) {
            recordDailyActivity(
                xpDelta = amount.toInt().coerceAtLeast(0),
                lessonsDelta = if (completedUnitId != null) 1 else 0,
                wordsDelta = if (completedUnitId != null) 6 else 0,
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
                recordDailyActivity(
                    xpDelta = if (quality >= 3) 15 else 0,
                    wordsDelta = if (quality >= 3) 1 else 0,
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

    fun generateAndSaveAiStory(hskLevel: Int, topicName: String, onCreated: (GradedStory) -> Unit) {
        viewModelScope.launch {
            _isGeneratingStory.value = true
            val res = aiRepo.generateGradedStory(hskLevel, topicName)
            res.onSuccess { story ->
                firestoreRepo.saveGeneratedStory(currentUserId, story)
                firestoreRepo.awardXpAndUnit(currentUserId, 25L)
                recordDailyActivity(xpDelta = 25, storiesDelta = 1, minutesDelta = 2)
                _snackbarMessage.value = "New HSK $hskLevel story '${story.titleZh}' created!"
                onCreated(story)
            }.onFailure { err ->
                _snackbarMessage.value = err.localizedMessage ?: "Story generation failed."
            }
            _isGeneratingStory.value = false
        }
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

    override fun onCleared() {
        super.onCleared()
        focusTimerJob?.cancel()
        liveVoiceManager.closeSync()
        audioManager.release()
    }
}
