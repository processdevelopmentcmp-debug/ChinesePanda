package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CharacterAnatomy
import com.example.data.local.ComponentRole
import com.example.data.local.WordEtymologyStory
import com.example.model.UiState
import com.example.model.VocabWord
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant

/**
 * CharacterPop + HanziCraft-inspired interactive breakdown sheet for ANY HSK 3.0 word:
 * - Instant offline character decomposition tree (Kangxi Radical, IDS Structure, Semantic & Phonetic cues)
 * - Word-Making Formula (how characters combine to build the compound word)
 * - CharacterPop-style visual memory story (cached permanently in Room SQLite)
 * - HanziCraft / Shuowen-style historical etymology & related character family
 * - Direct 1-tap external links to HanziCraft.com and CharacterPop.com
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WordEtymologySheet(
    word: VocabWord,
    anatomyList: List<CharacterAnatomy>,
    storyState: UiState<WordEtymologyStory>?,
    onSpeak: (String, Float?) -> Unit,
    onAddSrs: () -> Unit,
    onPracticeWrite: () -> Unit,
    onRetryStory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val uriHandler = LocalUriHandler.current
    var selectedCharIndex by remember(word.hanzi) { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.linearGradient(listOf(JadeDeep, JadePrimary, Color(0xFF007D69))))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.White.copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = "RADICALS · STORY · WORD HISTORY · HSK ${word.hskLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldBright,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = word.hanzi,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = word.pinyin,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldBright,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Text(
                                text = word.english,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("etymology_close_button")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close breakdown", tint = Color.White)
                        }
                    }

                    // Audio & Quick Study Actions
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HeaderMiniPill(
                            label = "Listen",
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            onClick = { onSpeak(word.hanzi, 1.0f) }
                        )
                        HeaderMiniPill(
                            label = "Slow",
                            icon = Icons.Rounded.SlowMotionVideo,
                            onClick = { onSpeak(word.hanzi, 0.65f) }
                        )
                        HeaderMiniPill(
                            label = "Save SM-2",
                            icon = Icons.Rounded.BookmarkAdd,
                            onClick = onAddSrs
                        )
                        if (word.writable) {
                            HeaderMiniPill(
                                label = "Write",
                                icon = Icons.Rounded.Brush,
                                onClick = onPracticeWrite
                            )
                        }
                    }
                }
            }

            // Section 1: Instant Offline Character Decomposition Tree (HanziCraft / CharacterPop Style)
            if (anatomyList.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Rounded.AccountTree, contentDescription = null, tint = JadePrimary, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "Character Decomposition & Radicals",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "HanziCraft-style structural tree · semantic & phonetic parts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Multi-character selector chips
                        if (anatomyList.size > 1) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                anatomyList.forEachIndexed { idx, ch ->
                                    val selected = idx == selectedCharIndex
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            selectedCharIndex = idx
                                            onSpeak(ch.character, 1.0f)
                                        },
                                        label = {
                                            Text(
                                                text = "${ch.character} ${ch.pinyin.ifBlank { "" }}".trim(),
                                                fontWeight = FontWeight.Bold
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = JadePrimary,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }

                        val activeChar = anatomyList[selectedCharIndex.coerceIn(0, anatomyList.lastIndex)]
                        CharacterAnatomyCard(
                            anatomy = activeChar,
                            aiNote = (storyState as? UiState.Success)?.data?.characterNotes
                                ?.firstOrNull { it.character == activeChar.character },
                            onTapChar = { onSpeak(it, 1.0f) }
                        )
                    }
                }
            }

            // Section 2: CharacterPop Memory Story, Word-Making System & Historical Origin
            when (storyState) {
                null, is UiState.Loading -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = JadePrimary,
                                strokeWidth = 2.5.dp
                            )
                            Column {
                                Text(
                                    text = "Building CharacterPop Story & Word History...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Will be cached permanently offline on your device.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                is UiState.Error -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = storyState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            TextButton(onClick = onRetryStory) {
                                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Retry Story & Etymology")
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val story = storyState.data

                    // Word-Making Formula Card
                    if (story.wordMakingLogic.isNotBlank()) {
                        EtymologySectionCard(
                            title = "How This Word Is Made (Word-Building System)",
                            icon = Icons.Rounded.AccountTree,
                            accentColor = JadePrimary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            body = story.wordMakingLogic
                        )
                    }

                    // CharacterPop Memory Story Card
                    if (story.characterPopStory.isNotBlank()) {
                        EtymologySectionCard(
                            title = "CharacterPop Memory Story",
                            icon = Icons.Rounded.AutoStories,
                            accentColor = GoldAccent,
                            containerColor = GoldContainer.copy(alpha = 0.55f),
                            body = story.characterPopStory
                        )
                    }

                    // Historical Origin & Evolution Card
                    if (story.historicalOrigin.isNotBlank()) {
                        EtymologySectionCard(
                            title = "Historical Origin & Script Evolution",
                            icon = Icons.Rounded.HistoryEdu,
                            accentColor = IndigoAccent,
                            containerColor = IndigoContainer.copy(alpha = 0.5f),
                            body = story.historicalOrigin
                        )
                    }

                    // Related Character Family
                    if (story.relatedWords.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Same Radical / Character Family (Tap to hear)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    story.relatedWords.forEach { rel ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .clickable { onSpeak(rel.hanzi, 1.0f) }
                                        ) {
                                            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = rel.hanzi,
                                                        fontFamily = HanziSerifFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                    Text(
                                                        text = rel.pinyin,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = JadePrimary
                                                    )
                                                }
                                                Text(
                                                    text = rel.english,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Refresh Story Option
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = onRetryStory) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Generate Alternate Story")
                        }
                    }
                }
            }

            // Section 3: Direct Reference Links to HanziCraft & CharacterPop for the active character
            val activeCharGlyph = anatomyList.getOrNull(selectedCharIndex)?.character
                ?: word.hanzi.firstOrNull()?.toString()
                ?: word.hanzi
            val encodedChar = Uri.encode(activeCharGlyph)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://hanzicraft.com/character/$encodedChar") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("HanziCraft ($activeCharGlyph)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open on HanziCraft", modifier = Modifier.size(15.dp))
                }
                OutlinedButton(
                    onClick = { uriHandler.openUri("https://characterpop.com/characters/$encodedChar") },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("CharacterPop ($activeCharGlyph)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Open on CharacterPop", modifier = Modifier.size(15.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacterAnatomyCard(
    anatomy: CharacterAnatomy,
    aiNote: com.example.data.local.AiCharacterNote?,
    onTapChar: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = JadePrimary.copy(alpha = 0.14f),
                    border = BorderStroke(1.5.dp, JadePrimary),
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onTapChar(anatomy.character) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = anatomy.character,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (anatomy.pinyin.isNotBlank()) {
                            Text(
                                text = anatomy.pinyin,
                                style = MaterialTheme.typography.titleSmall,
                                color = JadeVibrant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GoldContainer
                        ) {
                            Text(
                                text = "${anatomy.formationType} · ${anatomy.formationBadgeZh}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldOnContainer,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (anatomy.definition.isNotBlank()) {
                        Text(
                            text = anatomy.definition,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Radical: ${anatomy.radical}${if (anatomy.radicalMeaning.isNotBlank()) " (${anatomy.radicalMeaning})" else ""} · ${anatomy.structureLabel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Visual Component Building Blocks (Semantic vs Phonetic vs Radical)
            if (anatomy.components.isNotEmpty()) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    anatomy.components.forEach { comp ->
                        val roleBg = when (comp.role) {
                            ComponentRole.SEMANTIC -> JadePrimary.copy(alpha = 0.16f)
                            ComponentRole.PHONETIC -> GoldBright.copy(alpha = 0.25f)
                            ComponentRole.RADICAL -> IndigoAccent.copy(alpha = 0.15f)
                            ComponentRole.BUILDING_BLOCK -> MaterialTheme.colorScheme.surface
                        }
                        val roleFg = when (comp.role) {
                            ComponentRole.SEMANTIC -> JadePrimary
                            ComponentRole.PHONETIC -> GoldAccent
                            ComponentRole.RADICAL -> IndigoAccent
                            ComponentRole.BUILDING_BLOCK -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = roleBg,
                            border = BorderStroke(1.dp, roleFg.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTapChar(comp.glyph) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = comp.glyph,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Column {
                                    Text(
                                        text = comp.role.label.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = roleFg,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    )
                                    val detail = listOf(comp.pinyin, comp.meaning)
                                        .filter { it.isNotBlank() }
                                        .joinToString(" · ")
                                        .ifBlank { "Component" }
                                    Text(
                                        text = detail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (anatomy.etymologyHint.isNotBlank()) {
                Text(
                    text = "Etymology Cue: ${anatomy.etymologyHint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (aiNote != null) {
                if (aiNote.formationExplanation.isNotBlank()) {
                    Text(
                        text = aiNote.formationExplanation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (aiNote.miniStory.isNotBlank()) {
                    Text(
                        text = "Story hook: ${aiNote.miniStory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EtymologySectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    containerColor: Color,
    body: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(19.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun HeaderMiniPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color.White.copy(alpha = 0.18f),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
