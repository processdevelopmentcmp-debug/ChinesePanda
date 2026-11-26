package com.example.ui.screens.practice

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Lightbulb
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.HskCurriculumData
import com.example.data.MandarinDrillsData
import com.example.model.RadicalItem
import com.example.ui.components.HskLevelPill
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldContainer
import com.example.ui.theme.GoldOnContainer
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

private enum class RadicalSubMode(val label: String) {
    BUILDER("Builder"),
    FAMILY_TREES("Family Trees"),
    CORE_18("18 Radicals")
}

@Composable
fun RadicalsStudioSection(viewModel: PandaViewModel) {
    var subMode by remember { mutableStateOf(RadicalSubMode.BUILDER) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 3-Mode Sub-tabs (keeps existing 18 Radicals intact + adds Interactive Builder & Family Trees)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadicalSubMode.entries.forEach { mode ->
                val selected = subMode == mode
                val icon = when (mode) {
                    RadicalSubMode.BUILDER -> Icons.Rounded.AutoFixHigh
                    RadicalSubMode.FAMILY_TREES -> Icons.Rounded.AccountTree
                    RadicalSubMode.CORE_18 -> Icons.Rounded.GridView
                }
                FilterChip(
                    selected = selected,
                    onClick = { subMode = mode },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JadePrimary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("radicals_mode_${mode.name.lowercase()}")
                )
            }
        }

        when (subMode) {
            RadicalSubMode.BUILDER -> InteractiveRadicalBuilderPane(viewModel)
            RadicalSubMode.FAMILY_TREES -> CharacterFamilyTreesPane(viewModel)
            RadicalSubMode.CORE_18 -> Core18RadicalsList(viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InteractiveRadicalBuilderPane(viewModel: PandaViewModel) {
    var selectedLeft by remember { mutableStateOf("氵") }
    var selectedRight by remember { mutableStateOf("青") }
    val discoveredChars = remember { mutableStateListOf("清") }

    val matchingRecipe = remember(selectedLeft, selectedRight) {
        MandarinDrillsData.radicalFusionRecipes.firstOrNull {
            it.leftRadical == selectedLeft && it.rightComponent == selectedRight
        }
    }
    val compatibleRightGlyphs = remember(selectedLeft) {
        MandarinDrillsData.radicalFusionRecipes
            .filter { it.leftRadical == selectedLeft }
            .map { it.rightComponent }
            .toSet()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Live Fusion Anvil Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("radical_builder_forge_card"),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.5.dp, if (matchingRecipe != null) JadePrimary else MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Interactive Radical Forge (${discoveredChars.size}/${MandarinDrillsData.radicalFusionRecipes.size} unlocked)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = JadePrimary
                    )

                    // Equation row: [Left Radical] + [Right Component] = [Result]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ForgeSlotChip(
                            glyph = selectedLeft,
                            caption = MandarinDrillsData.semanticRadicals.firstOrNull { it.glyph == selectedLeft }?.name ?: "Radical",
                            accent = JadePrimary
                        )
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        ForgeSlotChip(
                            glyph = selectedRight,
                            caption = MandarinDrillsData.combinableComponents.firstOrNull { it.glyph == selectedRight }?.name ?: "Part",
                            accent = IndigoAccent
                        )
                        Text(
                            text = "=",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = if (matchingRecipe != null) JadeLightContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(2.dp, if (matchingRecipe != null) JadePrimary else MaterialTheme.colorScheme.outline)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (matchingRecipe != null) {
                                    ToneColoredHanziPinyinBlock(
                                        hanzi = matchingRecipe.resultChar,
                                        pinyin = matchingRecipe.pinyin,
                                        hanziFontSize = 36.sp,
                                        showPinyin = false
                                    )
                                } else {
                                    Text("?", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    if (matchingRecipe != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HskLevelPill(level = matchingRecipe.hskLevel)
                            ToneColoredHanziPinyinBlock(
                                hanzi = matchingRecipe.resultChar,
                                pinyin = matchingRecipe.pinyin,
                                hanziFontSize = 18.sp
                            )
                            Text("— ${matchingRecipe.english}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.speakChinese(matchingRecipe.resultChar) }) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen formed char", tint = JadePrimary)
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = GoldContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                Text(
                                    text = matchingRecipe.mnemonic,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GoldOnContainer
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.jumpToWriteHanzi(matchingRecipe.resultChar) },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Practice Writing 「${matchingRecipe.resultChar}」")
                        }
                    } else {
                        Text(
                            text = "No standard HSK combo for $selectedLeft + $selectedRight. Pick one of the highlighted green components below!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Step 1: Pick Semantic Radical (Left)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("1. Pick Meaning Radical (Left)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MandarinDrillsData.semanticRadicals.forEach { rad ->
                        val selected = selectedLeft == rad.glyph
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedLeft = rad.glyph
                                    // Snap to a valid partner if current right doesn't match
                                    val valid = MandarinDrillsData.radicalFusionRecipes.firstOrNull {
                                        it.leftRadical == rad.glyph && it.rightComponent == selectedRight
                                    } ?: MandarinDrillsData.radicalFusionRecipes.firstOrNull { it.leftRadical == rad.glyph }
                                    if (valid != null) {
                                        selectedRight = valid.rightComponent
                                        if (valid.resultChar !in discoveredChars) {
                                            discoveredChars.add(valid.resultChar)
                                            viewModel.awardXp(5L, null, "Forged ${valid.resultChar} (${valid.english})")
                                        }
                                        viewModel.speakChinese(valid.resultChar)
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = rad.glyph,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = rad.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Step 2: Pick Sound/Meaning Component (Right)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("2. Combine with Component (Highlighted = Valid Character)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MandarinDrillsData.combinableComponents.forEach { comp ->
                        val selected = selectedRight == comp.glyph
                        val isCompatible = comp.glyph in compatibleRightGlyphs
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedRight = comp.glyph
                                    val formed = MandarinDrillsData.radicalFusionRecipes.firstOrNull {
                                        it.leftRadical == selectedLeft && it.rightComponent == comp.glyph
                                    }
                                    if (formed != null) {
                                        if (formed.resultChar !in discoveredChars) {
                                            discoveredChars.add(formed.resultChar)
                                            viewModel.awardXp(5L, null, "Forged ${formed.resultChar} (${formed.english})")
                                        }
                                        viewModel.speakChinese(formed.resultChar)
                                    }
                                },
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                selected -> IndigoAccent
                                isCompatible -> JadeLightContainer
                                else -> MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (selected || isCompatible) 1.5.dp else 1.dp,
                                color = when {
                                    selected -> IndigoAccent
                                    isCompatible -> JadePrimary
                                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = comp.glyph,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = comp.name,
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
}

@Composable
private fun ForgeSlotChip(glyph: String, caption: String, accent: Color) {
    Surface(
        modifier = Modifier.size(66.dp),
        shape = RoundedCornerShape(16.dp),
        color = accent.copy(alpha = 0.12f),
        border = BorderStroke(1.5.dp, accent)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(glyph, fontFamily = HanziSerifFontFamily, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = accent)
            Text(caption, style = MaterialTheme.typography.labelSmall, color = accent, maxLines = 1)
        }
    }
}

@Composable
private fun CharacterFamilyTreesPane(viewModel: PandaViewModel) {
    var selectedTreeIdx by remember { mutableIntStateOf(0) }
    val trees = MandarinDrillsData.characterFamilyTrees
    val activeTree = trees[selectedTreeIdx.coerceIn(0, trees.lastIndex)]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Phonetic Root Selector Strip
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Character Family Trees (Sound + Meaning)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = JadeDeep
                )
                ToneColorLegendRow()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trees.forEachIndexed { idx, tree ->
                        val selected = idx == selectedTreeIdx
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { selectedTreeIdx = idx }
                                .testTag("family_tree_stem_$idx"),
                            shape = RoundedCornerShape(14.dp),
                            color = if (selected) IndigoAccent else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, if (selected) IndigoAccent else MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = tree.phoneticStem,
                                    fontFamily = HanziSerifFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = tree.stemPinyin,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White.copy(alpha = 0.88f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Root Sound Stem Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = IndigoAccent.copy(alpha = 0.12f),
                border = BorderStroke(1.5.dp, IndigoAccent.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(IndigoAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeTree.phoneticStem,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Shared Sound Stem: ${activeTree.phoneticStem} (${activeTree.stemPinyin}) · ${activeTree.stemMeaning}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = IndigoAccent
                        )
                        Text(
                            text = activeTree.soundCueNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.speakChinese(activeTree.members.joinToString("，") { it.char }) }) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Hear whole family", tint = IndigoAccent)
                    }
                }
            }
        }

        // Branching Sibling Members
        items(activeTree.members, key = { it.char }) { member ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Semantic Radical + Phonetic Formula Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = JadeLightContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${member.semanticRadical} + ${activeTree.phoneticStem}",
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = JadeDeep
                            )
                            Text(
                                text = member.semanticMeaning,
                                style = MaterialTheme.typography.labelSmall,
                                color = JadePrimary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ToneColoredHanziPinyinBlock(
                                hanzi = member.char,
                                pinyin = member.pinyin,
                                hanziFontSize = 24.sp
                            )
                            HskLevelPill(level = member.hskLevel)
                        }
                        Text(
                            text = member.english,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = member.mnemonic,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = { viewModel.speakChinese(member.char) }) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen ${member.char}", tint = JadePrimary)
                        }
                        IconButton(onClick = { viewModel.jumpToWriteHanzi(member.char) }) {
                            Icon(Icons.Rounded.Brush, contentDescription = "Write ${member.char}", tint = IndigoAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Core18RadicalsList(viewModel: PandaViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("18 Core HSK Radicals (部首)", style = MaterialTheme.typography.titleMedium, color = JadeDeep)
                    Text(
                        "These 18 semantic building blocks unlock the meaning of 80%+ of HSK 3.0 characters. Tap any example character to practice writing it!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        items(HskCurriculumData.radicals, key = { it.char }) { radical ->
            RadicalCard(
                radical = radical,
                onSpeak = { viewModel.speakChinese(it) },
                onPracticeWrite = { viewModel.jumpToWriteHanzi(it) }
            )
        }
    }
}

@Composable
private fun RadicalCard(
    radical: RadicalItem,
    onSpeak: (String) -> Unit,
    onPracticeWrite: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = radical.char,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = JadeDeep
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(radical.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(radical.pinyin, style = MaterialTheme.typography.labelMedium, color = JadePrimary)
                    }
                    Text(radical.meaning, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                    Text(radical.origin, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Examples:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                radical.examples.forEach { ex ->
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                onSpeak(ex)
                                onPracticeWrite(ex)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(ex, fontFamily = HanziSerifFontFamily, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Rounded.Brush, contentDescription = "Write $ex", modifier = Modifier.size(12.dp), tint = JadePrimary)
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { onSpeak(radical.examples.joinToString("，")) }) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Hear examples", tint = JadePrimary)
                }
            }
        }
    }
}
