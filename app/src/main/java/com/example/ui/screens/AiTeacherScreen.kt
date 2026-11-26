package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.LiveVoiceState
import com.example.model.TutorMode
import com.example.ui.components.LiveVoiceTutorDialog
import com.example.ui.components.PandaMascotAvatar
import com.example.ui.components.TutorMessageBubble
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@Composable
fun AiTeacherScreen(viewModel: PandaViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isStreaming by viewModel.isTeacherStreaming.collectAsStateWithLifecycle()
    val tutorMode by viewModel.tutorMode.collectAsStateWithLifecycle()
    val tutorHskLevel by viewModel.tutorHskLevel.collectAsStateWithLifecycle()
    val webSearchEnabled by viewModel.webSearchEnabled.collectAsStateWithLifecycle()
    val liveVoiceState by viewModel.liveVoiceState.collectAsStateWithLifecycle()
    val selectedLiveVoice by viewModel.selectedLiveVoice.collectAsStateWithLifecycle()
    val hskTopics by viewModel.hskTopics.collectAsStateWithLifecycle()
    val isListening by viewModel.audioManager.isListening.collectAsStateWithLifecycle()
    val speechStatus by viewModel.audioManager.speechStatus.collectAsStateWithLifecycle()

    var input by remember { mutableStateOf("") }
    var showLessonLauncher by remember { mutableStateOf(false) }
    var showLevelPicker by remember { mutableStateOf(false) }
    var showLiveVoiceDialog by remember { mutableStateOf(false) }
    var pendingLiveStart by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val startMandarinDictation: () -> Unit = {
        viewModel.audioManager.startChineseListening(
            onResult = { recognized ->
                input = if (input.isBlank()) recognized else "$input $recognized"
            },
            onError = { err ->
                viewModel.showMessage(err)
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (pendingLiveStart) {
                pendingLiveStart = false
                viewModel.startLiveVoiceConversation()
            } else {
                startMandarinDictation()
            }
        } else {
            pendingLiveStart = false
            viewModel.showMessage("Microphone permission is needed for Mandarin voice features.")
        }
    }

    val requestStartLiveVoice: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            viewModel.startLiveVoiceConversation()
        } else {
            pendingLiveStart = true
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    val submitCurrentInput: () -> Unit = {
        val clean = input.trim()
        if (!isStreaming && clean.isNotEmpty()) {
            input = ""
            viewModel.sendTeacherMessage(clean)
        }
    }

    LaunchedEffect(messages.size, messages.lastOrNull()?.text?.length) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Panda Laoshi Tutor Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PandaMascotAvatar(size = 44.dp, animated = isStreaming)
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Panda Laoshi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showLevelPicker = true }
                                    .testTag("tutor_hsk_selector"),
                                shape = RoundedCornerShape(50),
                                color = JadeLightContainer
                            ) {
                                Text(
                                    text = if (tutorHskLevel >= 7) "HSK 7–9" else "HSK $tutorHskLevel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JadeDeep,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isStreaming) {
                                "Crafting your Mandarin reply..."
                            } else {
                                "${tutorMode.subtitle} · Supportive AI Tutor"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isStreaming) JadePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLiveVoiceDialog = true }
                            .testTag("open_live_voice_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (liveVoiceState is LiveVoiceState.Active) CoralVermilion else JadePrimary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.GraphicEq,
                                contentDescription = stringResource(R.string.cd_live_voice_button),
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.live_voice_button),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLessonLauncher = true }
                            .testTag("launch_topic_lesson_button"),
                        shape = RoundedCornerShape(12.dp),
                        color = GoldBright
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Rounded.School,
                                contentDescription = null,
                                tint = GoldOnContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Lesson",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldOnContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.clearTeacherChat() },
                        modifier = Modifier.testTag("ai_teacher_clear_button")
                    ) {
                        Icon(
                            Icons.Rounded.DeleteSweep,
                            contentDescription = stringResource(R.string.tutor_new_chat)
                        )
                    }
                }

                // Tutor Coaching Mode & Google Search Grounding Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { viewModel.setWebSearchEnabled(!webSearchEnabled) }
                            .testTag("toggle_web_search_chip"),
                        shape = RoundedCornerShape(50),
                        color = if (webSearchEnabled) IndigoContainer else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(
                            1.dp,
                            if (webSearchEnabled) IndigoAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Public,
                                contentDescription = null,
                                tint = if (webSearchEnabled) IndigoAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (webSearchEnabled) "Search On" else stringResource(R.string.web_search_toggle),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (webSearchEnabled) IndigoAccent else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (webSearchEnabled) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }

                    TutorMode.entries.forEach { mode ->
                        val selected = mode == tutorMode
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { viewModel.setTutorMode(mode) }
                                .testTag("tutor_mode_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(50),
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected) {
                                null
                            } else {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = tutorModeIcon(mode),
                                    contentDescription = null,
                                    tint = if (selected) Color.White else JadeDeep,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contextual Quick Prompts Ribbon tailored to Active Tutor Mode, HSK Level & Search Grounding
        val quickPrompts = remember(tutorMode, tutorHskLevel, webSearchEnabled) {
            promptsForMode(tutorMode, tutorHskLevel, webSearchEnabled)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickPrompts.forEach { (label, prompt) ->
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !isStreaming) { viewModel.sendTeacherMessage(prompt) },
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = JadeDeep,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Streaming Chat Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                TutorMessageBubble(
                    message = msg,
                    onSpeakNormal = { text -> viewModel.speakChinese(text, 1.0f) },
                    onSpeakSlow = { text -> viewModel.speakChinese(text, 0.75f) },
                    onWriteHanzi = { hanzi -> viewModel.jumpToWriteHanzi(hanzi) },
                    onCopy = { fullText ->
                        clipboard.setText(AnnotatedString(fullText))
                        viewModel.showMessage("Copied Panda Laoshi's reply")
                    },
                    onRetry = { errId, failedPrompt ->
                        viewModel.retryTeacherMessage(errId, failedPrompt)
                    }
                )
            }
        }

        // Active Voice Dictation Status Strip
        AnimatedVisibility(visible = isListening) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(14.dp),
                color = CoralContainer,
                border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = CoralVermilion,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = speechStatus ?: "Listening in Mandarin (zh-CN)...",
                        style = MaterialTheme.typography.labelMedium,
                        color = CoralVermilion,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.audioManager.stopListening() }) {
                        Text("Stop", color = CoralVermilion)
                    }
                }
            }
        }

        // Multimodal Chat Composer Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            if (isListening) {
                                viewModel.audioManager.stopListening()
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    startMandarinDictation()
                                } else {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .testTag("ai_teacher_mic_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isListening) CoralVermilion else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = stringResource(R.string.cd_voice_input_chat),
                            tint = if (isListening) Color.White else JadeDeep
                        )
                    }
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.tutor_input_placeholder),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submitCurrentInput() }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_teacher_input")
                )

                if (isStreaming) {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { viewModel.stopTeacherStream() }
                            .testTag("ai_teacher_stop_button"),
                        shape = RoundedCornerShape(16.dp),
                        color = CoralVermilion
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Stop,
                                contentDescription = stringResource(R.string.cd_stop_response),
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(enabled = input.isNotBlank()) { submitCurrentInput() }
                            .testTag("ai_teacher_send_button"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (input.isNotBlank()) JadePrimary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = stringResource(R.string.cd_send_message),
                                tint = if (input.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLevelPicker) {
        AlertDialog(
            onDismissRequest = { showLevelPicker = false },
            icon = { PandaMascotAvatar(size = 44.dp, animated = false) },
            title = { Text("Panda Laoshi Difficulty") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose your HSK 3.0 target level so Panda Laoshi adapts vocabulary and sentence complexity:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..9).forEach { lvl ->
                            FilterChip(
                                selected = tutorHskLevel == lvl,
                                onClick = {
                                    viewModel.setTutorHskLevel(lvl)
                                    showLevelPicker = false
                                },
                                label = { Text("HSK $lvl") }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLevelPicker = false }) { Text("Done") }
            }
        )
    }

    if (showLessonLauncher) {
        var lvl by remember { mutableIntStateOf(tutorHskLevel) }
        var topicIdx by remember { mutableIntStateOf(0) }

        AlertDialog(
            onDismissRequest = { showLessonLauncher = false },
            icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = JadePrimary) },
            title = { Text("Structured HSK 3.0 Lesson") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose level and topic for Panda Laoshi's masterclass:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..9).forEach { level ->
                            FilterChip(
                                selected = lvl == level,
                                onClick = { lvl = level },
                                label = { Text("HSK $level") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hskTopics.forEachIndexed { idx, topic ->
                            FilterChip(
                                selected = topicIdx == idx,
                                onClick = { topicIdx = idx },
                                label = { Text(topic.nameEn) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val chosenTopic = hskTopics.getOrNull(topicIdx)?.nameEn ?: "Daily Life"
                    showLessonLauncher = false
                    viewModel.launchTopicLessonInAiTeacher(chosenTopic, lvl)
                }) { Text("Start Lesson") }
            },
            dismissButton = {
                TextButton(onClick = { showLessonLauncher = false }) { Text("Cancel") }
            }
        )
    }
    if (showLiveVoiceDialog) {
        LiveVoiceTutorDialog(
            liveState = liveVoiceState,
            hskLevel = tutorHskLevel,
            selectedVoice = selectedLiveVoice,
            onSelectVoice = { viewModel.setSelectedLiveVoice(it) },
            onSelectHskLevel = { viewModel.setTutorHskLevel(it) },
            onStartLive = requestStartLiveVoice,
            onStopLive = { viewModel.stopLiveVoiceConversation() },
            onDismiss = { showLiveVoiceDialog = false }
        )
    }
}

private fun tutorModeIcon(mode: TutorMode): ImageVector = when (mode) {
    TutorMode.CONVERSATION -> Icons.AutoMirrored.Rounded.Chat
    TutorMode.GRAMMAR -> Icons.Rounded.MenuBook
    TutorMode.ROLEPLAY -> Icons.Rounded.TheaterComedy
    TutorMode.PRONUNCIATION -> Icons.Rounded.RecordVoiceOver
}

private fun promptsForMode(
    mode: TutorMode,
    hskLevel: Int,
    webSearchEnabled: Boolean
): List<Pair<String, String>> {
    val base = when (mode) {
        TutorMode.CONVERSATION -> listOf(
            "Start a friendly chat" to "Start a warm, beginner-friendly Chinese conversation with me at HSK $hskLevel about my day.",
            "Correct my Chinese" to "Give me a short English sentence to translate into Chinese, then gently correct my attempt.",
            "Useful daily phrase" to "Teach me 1 super natural Mandarin phrase natives actually use daily at HSK $hskLevel.",
            "3-question quiz" to "Quiz me with 3 supportive HSK $hskLevel multiple-choice questions, one at a time."
        )
        TutorMode.GRAMMAR -> listOf(
            "Explain 了 vs 过" to "Explain the difference between 了 (le) and 过 (guò) with simple HSK $hskLevel examples.",
            "Chinese word order" to "Explain Chinese Time-Place-Action (S + Time + Place + V + O) word order clearly.",
            "Common HSK mistakes" to "What are 3 common grammar mistakes learners make around HSK $hskLevel and how to avoid them?",
            "Measure words" to "Teach me the 5 most essential Chinese measure words (量词) with examples."
        )
        TutorMode.ROLEPLAY -> listOf(
            "Order bubble tea" to "Let's roleplay! You are a barista at a Shanghai milk tea shop. Greet me in HSK $hskLevel Chinese and ask what I'd like to order.",
            "Beijing hotel check-in" to "Let's roleplay! You are a friendly hotel receptionist in Beijing. Welcome me in HSK $hskLevel Chinese.",
            "Take the subway" to "Let's roleplay asking for directions to the nearest subway station at HSK $hskLevel.",
            "Meet a new friend" to "Let's roleplay meeting a friendly local at a park in Chengdu at HSK $hskLevel."
        )
        TutorMode.PRONUNCIATION -> listOf(
            "3+3 Tone Sandhi" to "Explain Mandarin 3+3 tone sandhi with 4 everyday examples and tone-marked Pinyin.",
            "一 & 不 tone shifts" to "Explain how the tones of 一 (yī) and 不 (bù) change in real speech with examples.",
            "Tricky sound pairs" to "Coach me on pronouncing zh/ch/sh vs z/c/s or j/q/x with easy tongue-position tips.",
            "Radical memory tips" to "Break down the radicals and sounds of 休, 看, 明, and 扫码 in a memorable way."
        )
    }
    return if (webSearchEnabled) {
        listOf(
            "Latest HSK 3.0 news" to "Search the web for the latest official HSK 3.0 exam updates and summarize key takeaways for an HSK $hskLevel learner.",
            "Trending Chinese slang" to "Search the web for 3 trending modern Mandarin internet slang phrases used in China right now, with Hanzi, Pinyin, and examples."
        ) + base
    } else {
        base
    }
}
