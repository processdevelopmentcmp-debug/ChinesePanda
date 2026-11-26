package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.audio.ChineseAudioManager
import com.example.data.AppDisplayPrefs
import com.example.data.DailyLearningTrackerRepository
import com.example.data.HanziEtymologyRepository
import com.example.data.HskVocabularyRepository
import com.example.data.PandaAiRepository
import com.example.data.PandaFirestoreRepository
import com.example.data.local.HskVocabularyDatabase
import com.example.firebase.authStateFlow
import com.example.firebase.signOutUser
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.components.AuthScreen
import com.example.ui.components.PandaMascotAvatar
import com.example.ui.components.StatBadgePill
import com.example.ui.screens.practice.AlreadyLearnedVaultScreen
import com.example.ui.screens.practice.AnkiReviewSessionView
import com.example.ui.screens.practice.VaultSubScreen
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

private data class NavDestination(
    val index: Int,
    val labelRes: Int,
    val icon: ImageVector,
    val testTag: String
)

private val NAV_DESTINATIONS = listOf(
    NavDestination(0, R.string.nav_learn, Icons.Rounded.School, "nav_learn"),
    NavDestination(1, R.string.nav_topics, Icons.Rounded.MenuBook, "nav_topics"),
    NavDestination(2, R.string.nav_practice, Icons.Rounded.Brush, "nav_practice"),
    NavDestination(3, R.string.nav_review, Icons.Rounded.Psychology, "nav_review"),
    NavDestination(4, R.string.nav_teacher, Icons.Rounded.AutoAwesome, "nav_teacher"),
    NavDestination(5, R.string.nav_write_stroke, Icons.Rounded.Draw, "nav_write_stroke")
)

@Composable
fun PandaAppRoot(
    displayPrefs: AppDisplayPrefs,
    auth: FirebaseAuth = Firebase.auth
) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            AuthenticatedPandaShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "Panda Learner",
                userEmail = user.email ?: "",
                displayPrefs = displayPrefs
            )
        }
    }
}

@Composable
private fun AuthenticatedPandaShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    displayPrefs: AppDisplayPrefs
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: PandaViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val localVocabDb = HskVocabularyDatabase.getInstance(app.applicationContext)
                val aiRepo = PandaAiRepository()
                val progressPrefs = app.applicationContext.getSharedPreferences(
                    "panda_learn_progress",
                    android.content.Context.MODE_PRIVATE
                )
                PandaViewModel(
                    firestoreRepo = PandaFirestoreRepository(db),
                    vocabRepo = HskVocabularyRepository(localVocabDb.hskVocabularyDao()),
                    etymologyRepo = HanziEtymologyRepository(localVocabDb.hanziEtymologyDao(), aiRepo),
                    trackerRepo = DailyLearningTrackerRepository(localVocabDb.dailyStudyLogDao(), displayPrefs),
                    displayPrefs = displayPrefs,
                    aiRepo = aiRepo,
                    audioManager = ChineseAudioManager(app.applicationContext),
                    currentUserId = currentUserId,
                    initialDisplayName = userDisplayName,
                    progressPrefs = progressPrefs
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val srsState by viewModel.srsCardsState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedNavIndex.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val inspectedWord by viewModel.inspectedWord.collectAsStateWithLifecycle()
    val inspectedAnatomy by viewModel.inspectedWordAnatomy.collectAsStateWithLifecycle()
    val inspectedStoryState by viewModel.inspectedWordStoryState.collectAsStateWithLifecycle()
    val dailyTracker by viewModel.dailyTracker.collectAsStateWithLifecycle()
    val hskLevelSummaries by viewModel.hskLevelSummaries.collectAsStateWithLifecycle()
    val totalCatalogWords by viewModel.totalVocabCount.collectAsStateWithLifecycle()
    val learnedWordHanzi by viewModel.learnedWordHanzi.collectAsStateWithLifecycle()
    val hskProfileShowcase by viewModel.hskProfileShowcase.collectAsStateWithLifecycle()
    val focusDurationMin by viewModel.focusTimerDurationMin.collectAsStateWithLifecycle()
    val focusRemainingSec by viewModel.focusTimerRemainingSec.collectAsStateWithLifecycle()
    val focusRunning by viewModel.focusTimerRunning.collectAsStateWithLifecycle()
    val activeAnkiSession by viewModel.activeAnkiSession.collectAsStateWithLifecycle()
    val appFontScale by displayPrefs.fontScale.collectAsStateWithLifecycle()
    val themeMode by displayPrefs.themeMode.collectAsStateWithLifecycle()
    val audioSpeed by viewModel.audioSpeed.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showProfilePage by remember { mutableStateOf(false) }
    var showFontSheet by remember { mutableStateOf(false) }
    var showDailyTrackerSheet by remember { mutableStateOf(false) }
    var showWordGoalSheet by remember { mutableStateOf(false) }
    // null = closed; Pair(initialSubScreen, allTopicsMode)
    var learnedVaultLaunch by remember { mutableStateOf<Pair<VaultSubScreen, Boolean>?>(null) }

    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val profile = (profileState as? UiState.Success)?.data ?: UserProfile(
        id = currentUserId,
        userId = currentUserId,
        displayName = userDisplayName,
        xp = 100L,
        streak = 1L,
        hearts = 5L,
        currentHskLevel = 1L
    )

    val dueCount = remember(srsState) {
        val now = System.currentTimeMillis()
        (srsState as? UiState.Success)?.data?.count { it.nextReviewEpochMs <= now } ?: 0
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 680.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                PandaTopGamificationBar(
                    profile = profile,
                    dueCount = dueCount,
                    dailyStreakDisplay = maxOf(profile.streak.toInt(), dailyTracker.currentStreakDays),
                    onDueClick = {
                        showProfilePage = false
                        learnedVaultLaunch = null
                        viewModel.dismissAnkiSession()
                        viewModel.selectNavTab(3)
                    },
                    onDailyTrackerClick = { showDailyTrackerSheet = true },
                    onFontResizeClick = { showFontSheet = true },
                    onProfileClick = {
                        learnedVaultLaunch = null
                        viewModel.dismissAnkiSession()
                        showProfilePage = true
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        NAV_DESTINATIONS.forEach { dest ->
                            NavigationBarItem(
                                selected = selectedTab == dest.index && !showProfilePage && learnedVaultLaunch == null && activeAnkiSession == null,
                                onClick = {
                                    showProfilePage = false
                                    learnedVaultLaunch = null
                                    viewModel.dismissAnkiSession()
                                    viewModel.selectNavTab(dest.index)
                                },
                                icon = {
                                    Icon(dest.icon, contentDescription = stringResource(dest.labelRes))
                                },
                                label = {
                                    Text(stringResource(dest.labelRes), maxLines = 1)
                                },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        NAV_DESTINATIONS.forEach { dest ->
                            NavigationRailItem(
                                selected = selectedTab == dest.index && !showProfilePage && learnedVaultLaunch == null && activeAnkiSession == null,
                                onClick = {
                                    showProfilePage = false
                                    learnedVaultLaunch = null
                                    viewModel.dismissAnkiSession()
                                    viewModel.selectNavTab(dest.index)
                                },
                                icon = {
                                    Icon(dest.icon, contentDescription = stringResource(dest.labelRes))
                                },
                                label = { Text(stringResource(dest.labelRes)) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when {
                        activeAnkiSession != null -> {
                            AnkiReviewSessionView(
                                session = activeAnkiSession!!,
                                onSpeak = { text, speed -> viewModel.speakChinese(text, speed) },
                                onRateCard = { entry, quality -> viewModel.rateAnkiCard(entry, quality) },
                                onOpenWordBreakdown = { word -> viewModel.openWordBreakdown(word) },
                                onPracticeWrite = { word ->
                                    viewModel.dismissAnkiSession()
                                    learnedVaultLaunch = null
                                    showProfilePage = false
                                    viewModel.jumpToWriteWord(word)
                                },
                                onDismiss = { viewModel.dismissAnkiSession() }
                            )
                        }
                        showProfilePage -> {
                            ProfileAchievementsScreen(
                                profile = profile,
                                userEmail = userEmail,
                                dailyTracker = dailyTracker,
                                showcase = hskProfileShowcase,
                                onUpdateTargetHskLevel = { lvl -> viewModel.updateTargetHskLevel(lvl) },
                                onPracticeHskLevel = { levelKey -> viewModel.jumpToHskLevelInLearn(levelKey) },
                                onOpenWordGoalSetter = { showWordGoalSheet = true },
                                onOpenDailyTracker = { showDailyTrackerSheet = true },
                                onOpenFontSettings = { showFontSheet = true },
                                onSignOut = {
                                    showProfilePage = false
                                    signOutUser(credentialManager, scope)
                                },
                                onBack = { showProfilePage = false }
                            )
                        }
                        learnedVaultLaunch != null -> {
                            AlreadyLearnedVaultScreen(
                                viewModel = viewModel,
                                initialSubScreen = learnedVaultLaunch!!.first,
                                initialTopicModeAllWords = learnedVaultLaunch!!.second,
                                onBack = { learnedVaultLaunch = null }
                            )
                        }
                        else -> {
                            when (selectedTab) {
                                0 -> LearnPathScreen(
                                    viewModel = viewModel,
                                    profile = profile,
                                    onOpenDailyTracker = { showDailyTrackerSheet = true },
                                    onOpenWordGoalSetter = { showWordGoalSheet = true },
                                    onOpenLearnedVault = { learnedVaultLaunch = VaultSubScreen.HUB to false },
                                    onOpenProfilePage = { showProfilePage = true }
                                )
                                1 -> TopicsScreen(viewModel = viewModel)
                                2 -> PracticeHubScreen(
                                    viewModel = viewModel,
                                    onOpenLearnedVault = { learnedVaultLaunch = VaultSubScreen.HUB to false },
                                    onOpenTopicAnkiSelector = { learnedVaultLaunch = VaultSubScreen.WORDS_SELECTOR to true }
                                )
                                3 -> ReviewAndStoriesScreen(
                                    viewModel = viewModel,
                                    onOpenLearnedVault = { learnedVaultLaunch = VaultSubScreen.HUB to false },
                                    onOpenTopicAnkiSelector = { learnedVaultLaunch = VaultSubScreen.WORDS_SELECTOR to true }
                                )
                                4 -> AiTeacherScreen(viewModel = viewModel)
                                5 -> StrokePracticeScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }

    if (inspectedWord != null) {
        WordEtymologySheet(
            word = inspectedWord!!,
            anatomyList = inspectedAnatomy,
            storyState = inspectedStoryState,
            onSpeak = { text, speed -> viewModel.speakChinese(text, speed) },
            onAddSrs = { viewModel.addWordToSrs(inspectedWord!!) },
            onPracticeWrite = {
                val target = inspectedWord!!
                viewModel.dismissWordBreakdown()
                viewModel.jumpToWriteWord(target)
            },
            onRetryStory = { viewModel.regenerateWordBreakdownStory() },
            onDismiss = { viewModel.dismissWordBreakdown() }
        )
    }

    if (showFontSheet) {
        FontAndDisplaySheet(
            currentScale = appFontScale,
            themeMode = themeMode,
            audioSpeed = audioSpeed,
            onScaleChange = { displayPrefs.setFontScale(it) },
            onStepScale = { displayPrefs.stepFontScale(it) },
            onResetScale = { displayPrefs.resetFontScale() },
            onThemeModeChange = { displayPrefs.setThemeMode(it) },
            onAudioSpeedChange = { viewModel.setAudioSpeed(it) },
            onTestAudio = { speed -> viewModel.speakChinese("每天学习中文，越来越棒！", speed) },
            onDismiss = { showFontSheet = false }
        )
    }

    if (showDailyTrackerSheet) {
        DailyLearningTrackerSheet(
            snapshot = dailyTracker,
            focusDurationMin = focusDurationMin,
            focusRemainingSec = focusRemainingSec,
            focusRunning = focusRunning,
            onSetFocusMinutes = { viewModel.setFocusTimerMinutes(it) },
            onToggleFocusTimer = { viewModel.toggleFocusTimer() },
            onResetFocusTimer = { viewModel.resetFocusTimer() },
            onSetXpGoal = { viewModel.setDailyXpGoal(it) },
            onSetWordsGoal = { viewModel.setDailyWordsGoal(it) },
            onOpenWordGoalSetter = {
                showDailyTrackerSheet = false
                showWordGoalSheet = true
            },
            onStartDailyGoalLesson = {
                learnedVaultLaunch = null
                viewModel.startDailyWordGoalSession()
            },
            onTapAcquiredWord = { hanzi -> viewModel.openAcquiredWordBreakdown(hanzi) },
            onNavigateToTab = { tabIndex -> viewModel.selectNavTab(tabIndex) },
            onDismiss = { showDailyTrackerSheet = false }
        )
    }

    if (showWordGoalSheet) {
        DailyWordAcquisitionGoalSheet(
            snapshot = dailyTracker,
            levelSummaries = hskLevelSummaries,
            totalCatalogWords = totalCatalogWords,
            totalLearnedWords = learnedWordHanzi.size,
            onSaveGoal = { targetCount, unit, hskLevelKey ->
                viewModel.setDailyWordGoalConfig(targetCount, unit, hskLevelKey)
            },
            onSaveAndStartPack = { targetCount, unit, hskLevelKey ->
                viewModel.setDailyWordGoalConfig(targetCount, unit, hskLevelKey)
                showProfilePage = false
                learnedVaultLaunch = null
                viewModel.startDailyWordGoalSession()
            },
            onDismiss = { showWordGoalSheet = false }
        )
    }
}

@Composable
private fun PandaTopGamificationBar(
    profile: UserProfile,
    dueCount: Int,
    dailyStreakDisplay: Int,
    onDueClick: () -> Unit,
    onDailyTrackerClick: () -> Unit,
    onFontResizeClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onProfileClick,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("profile_avatar_button")
                ) {
                    PandaMascotAvatar(size = 38.dp, animated = false)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "PandaChinese",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "HSK 3.0 · L1–L9",
                        style = MaterialTheme.typography.labelSmall,
                        color = JadePrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Overall Font Size Resizer Button ("Aa")
                Surface(
                    onClick = onFontResizeClick,
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("font_size_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            Icons.Rounded.FormatSize,
                            contentDescription = stringResource(R.string.cd_resize_fonts),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Aa",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                // Daily Learning Tracker & Streak Button
                Surface(
                    onClick = onDailyTrackerClick,
                    shape = MaterialTheme.shapes.large,
                    color = GoldContainer,
                    modifier = Modifier.testTag("daily_tracker_top_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Rounded.LocalFireDepartment,
                            contentDescription = stringResource(R.string.cd_daily_tracker),
                            tint = GoldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$dailyStreakDisplay",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                StatBadgePill(
                    label = "${profile.xp}",
                    bgColor = JadeLightContainer,
                    fgColor = JadePrimary,
                    icon = {
                        Icon(Icons.Rounded.Bolt, contentDescription = "XP", tint = JadePrimary, modifier = Modifier.size(14.dp))
                    }
                )
                StatBadgePill(
                    label = "${profile.hearts}",
                    bgColor = CoralContainer,
                    fgColor = CoralVermilion,
                    icon = {
                        Icon(Icons.Rounded.Favorite, contentDescription = "Hearts", tint = CoralVermilion, modifier = Modifier.size(14.dp))
                    }
                )
                if (dueCount > 0) {
                    Surface(
                        onClick = onDueClick,
                        shape = MaterialTheme.shapes.large,
                        color = IndigoContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(Icons.Rounded.Psychology, contentDescription = "Due SRS", tint = IndigoAccent, modifier = Modifier.size(14.dp))
                            Text(
                                text = "$dueCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoAccent,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
