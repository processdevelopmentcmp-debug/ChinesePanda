package com.example.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.TouchApp
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
import com.example.data.MandarinDrillsData
import com.example.model.VocabWord
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClozeContextStudioSection(viewModel: PandaViewModel) {
    val cards = MandarinDrillsData.clozeContextCards
    var cardIdx by remember { mutableIntStateOf(0) }
    var typeMode by remember { mutableStateOf(false) }
    var typedAnswer by remember(cardIdx) { mutableStateOf("") }
    var selectedAnswer by remember(cardIdx) { mutableStateOf<String?>(null) }
    var streak by remember { mutableIntStateOf(0) }

    val card = cards[cardIdx % cards.size]
    val shuffledOptions = remember(card.id) { card.options.shuffled() }
    val answered = selectedAnswer != null
    val isCorrect = selectedAnswer == card.missingWord

    fun submitAnswer(answer: String) {
        val clean = answer.trim()
        if (clean.isEmpty()) return
        selectedAnswer = clean
        if (clean == card.missingWord) {
            streak++
            viewModel.awardXp(15L, null, "Cloze context card mastered: ${card.missingWord}")
            viewModel.speakChinese(card.fullSentence)
        } else {
            streak = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Mode Switcher (Tap vs Type)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Cloze-Deletion Context Cards", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Learn vocabulary inside real Mandarin grammar patterns.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(shape = RoundedCornerShape(50), color = JadeLightContainer) {
                        Text(
                            text = "Streak $streak",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = JadePrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !typeMode,
                        onClick = { typeMode = false },
                        label = { Text("Tap Word") },
                        leadingIcon = { Icon(Icons.Rounded.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JadePrimary, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = typeMode,
                        onClick = { typeMode = true },
                        label = { Text("Type Answer") },
                        leadingIcon = { Icon(Icons.Rounded.Keyboard, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JadePrimary, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Main Cloze Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cloze_card_container"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.65f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        HskLevelPill(level = card.hskLevel)
                        Text("Card ${cardIdx + 1}/${cards.size}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(
                        onClick = { viewModel.speakChinese(card.fullSentence) },
                        modifier = Modifier.testTag("cloze_audio_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen full sentence", tint = JadePrimary)
                    }
                }

                // Sentence with Blank or Tone-Colored Reveal
                if (!answered) {
                    Text(
                        text = card.sentenceWithBlank,
                        fontFamily = HanziSerifFontFamily,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "\"${card.englishTranslation}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                } else {
                    ToneColoredHanziPinyinBlock(
                        hanzi = card.fullSentence,
                        pinyin = card.fullPinyin,
                        hanziFontSize = 25.sp
                    )
                    Text(
                        text = "\"${card.englishTranslation}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    ToneColorLegendRow()
                }

                // Input UI: Tap Options OR Type Field
                if (!typeMode) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        shuffledOptions.forEachIndexed { idx, opt ->
                            val isRightOpt = opt == card.missingWord
                            val isPicked = selectedAnswer == opt
                            val bg = when {
                                answered && isRightOpt -> JadePrimary.copy(alpha = 0.16f)
                                answered && isPicked && !isRightOpt -> CoralVermilion.copy(alpha = 0.16f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            }
                            val border = when {
                                answered && isRightOpt -> JadePrimary
                                answered && isPicked && !isRightOpt -> CoralVermilion
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(enabled = !answered) { submitAnswer(opt) }
                                    .testTag("cloze_option_$idx"),
                                shape = RoundedCornerShape(14.dp),
                                color = bg,
                                border = BorderStroke(if (answered && (isRightOpt || isPicked)) 2.dp else 1.dp, border)
                            ) {
                                Text(
                                    text = opt,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                } else if (!answered) {
                    OutlinedTextField(
                        value = typedAnswer,
                        onValueChange = { typedAnswer = it },
                        label = { Text("Type the missing Chinese word") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cloze_type_input")
                    )
                    PandaActionButton(
                        text = "Check Missing Word",
                        onClick = { submitAnswer(typedAnswer) },
                        enabled = typedAnswer.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Grammar feedback + Next + Save to SM-2
                if (answered) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCorrect) JadeLightContainer else MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isCorrect) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = JadePrimary, modifier = Modifier.size(18.dp))
                                }
                                Text(
                                    text = if (isCorrect) {
                                        "Spot on! ${card.missingWord} (${card.missingPinyin})"
                                    } else {
                                        "Correct word: ${card.missingWord} (${card.missingPinyin})"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCorrect) JadePrimary else MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Text(
                                text = card.grammarNote,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCorrect) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.addWordsBatchToSrs(
                                    listOf(
                                        VocabWord(
                                            key = "cloze_${card.missingWord}",
                                            hanzi = card.missingWord,
                                            pinyin = card.missingPinyin,
                                            english = card.englishTranslation,
                                            hskLevel = card.hskLevel,
                                            topicId = 1,
                                            writable = true,
                                            exampleHanzi = card.fullSentence,
                                            examplePinyin = card.fullPinyin,
                                            exampleEnglish = card.englishTranslation
                                        )
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("+ SM-2")
                        }
                        PandaActionButton(
                            text = "Next Card →",
                            onClick = { cardIdx = (cardIdx + 1) % cards.size },
                            containerColor = IndigoAccent,
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("cloze_next_card_button")
                        )
                    }
                }
            }
        }
    }
}
