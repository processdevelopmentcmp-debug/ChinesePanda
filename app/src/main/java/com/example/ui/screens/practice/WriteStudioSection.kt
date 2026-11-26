package com.example.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WritingPracticeMode
import com.example.ui.components.HskLevelPill
import com.example.ui.components.InteractiveHanziWriter
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

private val WriteLevelFilters = listOf(
    "ALL" to "All HSK",
    "1" to "HSK 1",
    "2" to "HSK 2",
    "3" to "HSK 3",
    "4" to "HSK 4",
    "5+" to "HSK 5+"
)

@Composable
fun WriteStudioSection(viewModel: PandaViewModel) {
    val activeWord by viewModel.activeWriteWord.collectAsStateWithLifecycle()
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()

    val chars = remember(activeWord.hanzi) {
        activeWord.hanzi
            .filter { ch -> !ch.isWhitespace() && Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.HAN }
            .map { it.toString() }
            .ifEmpty {
                activeWord.hanzi.filter { !it.isWhitespace() }.map { it.toString() }.ifEmpty { listOf("好") }
            }
    }
    var activeCharIdx by remember(activeWord.hanzi) { mutableIntStateOf(0) }
    val activeChar = chars.getOrElse(activeCharIdx) { chars.first() }
    val completedChars = remember(activeWord.hanzi) { mutableStateMapOf<String, Int>() }

    var customInput by remember { mutableStateOf("") }
    var selectedLevelFilter by remember { mutableStateOf("ALL") }

    val quickPickWords = remember(allVocab, selectedLevelFilter) {
        val base = allVocab.filter { it.writable }.ifEmpty { allVocab }
        val filtered = when (selectedLevelFilter) {
            "ALL" -> base
            "5+" -> base.filter { it.hskLevel >= 5 }
            else -> {
                val lvl = selectedLevelFilter.toIntOrNull()
                if (lvl != null) base.filter { it.hskLevel == lvl } else base
            }
        }
        filtered.ifEmpty { base }.take(24)
    }

    val allCharsMastered = chars.isNotEmpty() && chars.all { it in completedChars }
    val nextUnfinishedIdx = chars.indices.firstOrNull { idx -> idx != activeCharIdx && chars[idx] !in completedChars }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Custom Hanzi Loader Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it },
                placeholder = { Text("Type any Hanzi (e.g. 扫码 / 森林 / 喜欢)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("write_custom_hanzi_input")
            )
            PandaActionButton(
                text = "Load",
                onClick = {
                    if (customInput.isNotBlank()) {
                        viewModel.jumpToWriteHanzi(customInput)
                        customInput = ""
                    }
                },
                modifier = Modifier.testTag("write_load_hanzi_button")
            )
        }

        // Active Word Info Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = activeWord.hanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (activeWord.pinyin.isNotBlank()) {
                            Text(
                                text = activeWord.pinyin,
                                style = MaterialTheme.typography.titleSmall,
                                color = JadePrimary
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HskLevelPill(level = activeWord.hskLevel)
                        IconButton(
                            onClick = { viewModel.speakChinese(activeWord.hanzi) },
                            modifier = Modifier.testTag("write_speak_word_button")
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Listen to pronunciation",
                                tint = JadePrimary
                            )
                        }
                    }
                }

                Text(
                    text = activeWord.english,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Multi-character selector pills with mastery checkmarks + Story shortcut
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        chars.forEachIndexed { idx, ch ->
                            val selected = idx == activeCharIdx
                            val score = completedChars[ch]
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { activeCharIdx = idx }
                                    .testTag("write_char_pill_$idx"),
                                shape = RoundedCornerShape(12.dp),
                                color = when {
                                    selected -> JadePrimary
                                    score != null -> JadeLightContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                border = if (score != null && !selected) {
                                    BorderStroke(1.dp, JadePrimary.copy(alpha = 0.5f))
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = ch,
                                        fontFamily = HanziSerifFontFamily,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            selected -> Color.White
                                            score != null -> JadePrimary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (score != null) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = "Character completed",
                                            tint = if (selected) GoldAccent else JadePrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "$score%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (selected) Color.White.copy(alpha = 0.9f) else JadePrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(
                        onClick = { viewModel.openWordBreakdown(activeWord) },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("write_word_breakdown_button")
                    ) {
                        Icon(
                            Icons.Rounded.AutoStories,
                            contentDescription = "Word story and radicals",
                            modifier = Modifier.size(16.dp),
                            tint = JadePrimary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Radicals", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Interactive Animated Stroke-Order & Canvas Feedback Component
        InteractiveHanziWriter(
            character = activeChar,
            modifier = Modifier.fillMaxWidth(),
            initialMode = WritingPracticeMode.GUIDED,
            onCharacterCompleted = { completedChar, accuracy, isQuiz ->
                val prev = completedChars[completedChar] ?: 0
                completedChars[completedChar] = maxOf(prev, accuracy)
                viewModel.recordWritingPracticeComplete(
                    char = completedChar,
                    accuracyPercent = accuracy,
                    isQuizMode = isQuiz
                )
            }
        )

        // Multi-character flow banner: move to next character or celebrate full word mastery
        if (chars.size > 1 && activeChar in completedChars) {
            if (nextUnfinishedIdx != null) {
                FilledTonalButton(
                    onClick = { activeCharIdx = nextUnfinishedIdx },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("write_next_char_button")
                ) {
                    Text(
                        text = "Practice Next Character: ${chars[nextUnfinishedIdx]}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (allCharsMastered) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = JadeLightContainer,
                    border = BorderStroke(1.dp, JadePrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = JadePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "All ${chars.size} characters of 「${activeWord.hanzi}」 mastered! Pick another HSK word below.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = JadePrimary
                        )
                    }
                }
            }
        }

        // Quick Pick HSK 3.0 Vocabulary with Level Filter Chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Quick Pick HSK 3.0 Vocabulary",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                WriteLevelFilters.forEach { (key, label) ->
                    val isSelected = selectedLevelFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLevelFilter = key },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JadePrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("write_level_filter_$key")
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickPickWords.forEach { w ->
                    val selected = w.hanzi == activeWord.hanzi
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.jumpToWriteWord(w) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (selected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = w.hanzi,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = w.pinyin,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
