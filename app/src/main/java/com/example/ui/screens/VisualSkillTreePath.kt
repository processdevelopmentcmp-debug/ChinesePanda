package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.DirectionsSubway
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveShortcutQuiz
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldBright
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.ui.theme.JadeVibrant

data class SkillTreeNodeSpec(
    val id: String,
    val title: String,
    val subtitleZh: String,
    val icon: ImageVector,
    val accentColor: Color,
    val topicId: Int, // -1 means Stories milestone node
    val minHsk: Int,
    val maxHsk: Int
)

sealed interface SkillTreeRowItem {
    data class SingleNode(val node: SkillTreeNodeSpec) : SkillTreeRowItem
    data class PairNodes(val left: SkillTreeNodeSpec, val right: SkillTreeNodeSpec) : SkillTreeRowItem
    data class TripleNodes(val left: SkillTreeNodeSpec, val center: SkillTreeNodeSpec, val right: SkillTreeNodeSpec) : SkillTreeRowItem
    data class ShortcutGate(val targetHskLevel: Int, val label: String) : SkillTreeRowItem
}

val VISUAL_SKILL_TREE_ROWS: List<SkillTreeRowItem> = listOf(
    SkillTreeRowItem.SingleNode(
        SkillTreeNodeSpec("greetings_1", "Personal Info & Greetings", "问候", Icons.Rounded.Badge, JadePrimary, 1, 1, 1)
    ),
    SkillTreeRowItem.PairNodes(
        SkillTreeNodeSpec("time_1", "Time", "时间", Icons.Rounded.Schedule, Color(0xFF0EA5E9), 3, 1, 1),
        SkillTreeNodeSpec("hometown_1", "Family & Home", "家庭", Icons.Rounded.Home, Color(0xFFEC4899), 4, 1, 1)
    ),
    SkillTreeRowItem.SingleNode(
        SkillTreeNodeSpec("stories_1", "Stories", "分级故事", Icons.AutoMirrored.Rounded.MenuBook, IndigoAccent, -1, 1, 2)
    ),
    SkillTreeRowItem.PairNodes(
        SkillTreeNodeSpec("numbers_1", "Numbers & Counters", "数字", Icons.Rounded.Numbers, GoldAccent, 2, 1, 1),
        SkillTreeNodeSpec("taste_1", "Food & Taste", "饮食", Icons.Rounded.Fastfood, CoralVermilion, 5, 1, 1)
    ),
    SkillTreeRowItem.ShortcutGate(targetHskLevel = 2, label = "Take a shortcut to HSK 2"),
    SkillTreeRowItem.SingleNode(
        SkillTreeNodeSpec("restaurants_2", "Restaurants", "点菜", Icons.Rounded.Restaurant, Color(0xFFF97316), 5, 2, 3)
    ),
    SkillTreeRowItem.PairNodes(
        SkillTreeNodeSpec("daily_schedule_2", "Daily Schedule", "日常", Icons.AutoMirrored.Rounded.DirectionsRun, JadeVibrant, 6, 2, 3),
        SkillTreeNodeSpec("leisure_2", "Leisure & Feelings", "休闲", Icons.Rounded.SportsEsports, Color(0xFF8B5CF6), 13, 2, 3)
    ),
    SkillTreeRowItem.SingleNode(
        SkillTreeNodeSpec("stories_2", "Stories", "情景阅读", Icons.AutoMirrored.Rounded.MenuBook, IndigoAccent, -1, 2, 4)
    ),
    SkillTreeRowItem.PairNodes(
        SkillTreeNodeSpec("places_2", "Places & Directions", "方位", Icons.Rounded.Place, Color(0xFF0EA5E9), 7, 2, 3),
        SkillTreeNodeSpec("travel_2", "Travel & Rail", "交通", Icons.Rounded.DirectionsSubway, JadePrimary, 8, 2, 4)
    ),
    SkillTreeRowItem.ShortcutGate(targetHskLevel = 4, label = "Take a shortcut to HSK 4"),
    SkillTreeRowItem.SingleNode(
        SkillTreeNodeSpec("career_4", "Study & Work", "学习工作", Icons.Rounded.Work, IndigoAccent, 9, 3, 5)
    ),
    SkillTreeRowItem.TripleNodes(
        SkillTreeNodeSpec("shopping_4", "Shopping & Tech", "购物科技", Icons.Rounded.ShoppingBag, Color(0xFFEC4899), 10, 3, 5),
        SkillTreeNodeSpec("weather_4", "Weather & Nature", "自然", Icons.Rounded.Cloud, Color(0xFF06B6D4), 12, 3, 5),
        SkillTreeNodeSpec("objects_4", "Everyday Life", "物品", Icons.Rounded.Groups, GoldAccent, 11, 3, 5)
    ),
    SkillTreeRowItem.ShortcutGate(targetHskLevel = 6, label = "Take a shortcut to HSK 6+"),
    SkillTreeRowItem.PairNodes(
        SkillTreeNodeSpec("grammar_6", "Advanced Connectors", "高级语法", Icons.Rounded.AccountTree, JadeDeep, 14, 4, 6),
        SkillTreeNodeSpec("mastery_7", "HSK 7–9 Mastery", "成语与精通", Icons.Rounded.EmojiEmotions, CoralVermilion, 6, 7, 9)
    )
)

@Composable
fun VisualSkillTreeSection(
    completedLessonIds: Set<String>,
    learnedWordsCount: Int,
    onTapSkillNode: (SkillTreeNodeSpec) -> Unit,
    onTapStoriesMilestone: () -> Unit,
    onTapShortcut: (targetHskLevel: Int) -> Unit,
    onOpenLearnedVault: () -> Unit
) {
    val allNodes = remember {
        VISUAL_SKILL_TREE_ROWS.flatMap { row ->
            when (row) {
                is SkillTreeRowItem.SingleNode -> listOf(row.node)
                is SkillTreeRowItem.PairNodes -> listOf(row.left, row.right)
                is SkillTreeRowItem.TripleNodes -> listOf(row.left, row.center, row.right)
                is SkillTreeRowItem.ShortcutGate -> emptyList()
            }
        }.filter { it.topicId > 0 }
    }
    val completedCount = remember(completedLessonIds, allNodes) {
        allNodes.count { "tree_${it.id}" in completedLessonIds }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Warm Golden-Jade Visual Skill Tree Banner (inspired by Screenshots 2 & 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFF6C644), Color(0xFFFDE68A), Color(0xFFD6F5EC))
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "VISUAL SKILL TREE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF5A3E00),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tap any circle to master 10 real HSK 3.0 words",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color(0xFF1F2937),
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$completedCount / ${allNodes.size} skill nodes completed · $learnedWordsCount words learned so far",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF374151)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    onClick = onOpenLearnedVault,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.92f),
                    modifier = Modifier.testTag("skill_tree_learned_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, tint = JadePrimary, modifier = Modifier.size(16.dp))
                        Text(
                            text = "$learnedWordsCount",
                            style = MaterialTheme.typography.labelLarge,
                            color = JadeDeep,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Branching Skill Tree Rows
        VISUAL_SKILL_TREE_ROWS.forEachIndexed { rowIdx, row ->
            when (row) {
                is SkillTreeRowItem.SingleNode -> {
                    val isDone = "tree_${row.node.id}" in completedLessonIds
                    val isCurrent = !isDone && rowIdx == 0
                    SkillTreeCircleNode(
                        node = row.node,
                        completed = isDone,
                        highlighted = isCurrent,
                        onClick = {
                            if (row.node.topicId < 0) onTapStoriesMilestone() else onTapSkillNode(row.node)
                        }
                    )
                }

                is SkillTreeRowItem.PairNodes -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        SkillTreeCircleNode(
                            node = row.left,
                            completed = "tree_${row.left.id}" in completedLessonIds,
                            highlighted = false,
                            modifier = Modifier.weight(1f),
                            onClick = { onTapSkillNode(row.left) }
                        )
                        SkillTreeCircleNode(
                            node = row.right,
                            completed = "tree_${row.right.id}" in completedLessonIds,
                            highlighted = false,
                            modifier = Modifier.weight(1f),
                            onClick = { onTapSkillNode(row.right) }
                        )
                    }
                }

                is SkillTreeRowItem.TripleNodes -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Top
                    ) {
                        listOf(row.left, row.center, row.right).forEach { node ->
                            SkillTreeCircleNode(
                                node = node,
                                completed = "tree_${node.id}" in completedLessonIds,
                                highlighted = false,
                                compact = true,
                                modifier = Modifier.weight(1f),
                                onClick = { onTapSkillNode(node) }
                            )
                        }
                    }
                }

                is SkillTreeRowItem.ShortcutGate -> {
                    val unlocked = "shortcut_hsk_${row.targetHskLevel}" in completedLessonIds
                    Surface(
                        onClick = { onTapShortcut(row.targetHskLevel) },
                        shape = RoundedCornerShape(50),
                        color = if (unlocked) JadePrimary else GoldBright,
                        shadowElevation = 3.dp,
                        modifier = Modifier.testTag("shortcut_gate_hsk_${row.targetHskLevel}")
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 8.dp, end = 18.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (unlocked) Icons.Rounded.CheckCircle else Icons.Rounded.Key,
                                    contentDescription = null,
                                    tint = if (unlocked) JadePrimary else GoldAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = if (unlocked) "HSK ${row.targetHskLevel} Shortcut Unlocked" else row.label,
                                style = MaterialTheme.typography.titleSmall,
                                color = if (unlocked) Color.White else Color(0xFF3B2600),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(52.dp))
    }
}

@Composable
private fun SkillTreeCircleNode(
    node: SkillTreeNodeSpec,
    completed: Boolean,
    highlighted: Boolean,
    compact: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val circleSize = if (compact) 74.dp else 88.dp
    val ringColor = when {
        completed -> JadePrimary
        highlighted -> GoldAccent
        node.topicId < 0 -> IndigoAccent
        else -> node.accentColor.copy(alpha = 0.65f)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("skill_node_${node.id}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier.size(circleSize),
            contentAlignment = Alignment.Center
        ) {
            // Outer Progress Ring
            Canvas(modifier = Modifier.size(circleSize)) {
                val stroke = 4.5.dp.toPx()
                val inset = stroke / 2f
                val arcSize = Size(size.width - stroke, size.height - stroke)
                drawArc(
                    color = ringColor.copy(alpha = 0.22f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = if (completed) 360f else if (highlighted) 120f else 45f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            // Inner Emblem Circle
            Box(
                modifier = Modifier
                    .size(circleSize - 14.dp)
                    .clip(CircleShape)
                    .background(
                        if (completed) JadePrimary.copy(alpha = 0.16f)
                        else node.accentColor.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = node.icon,
                    contentDescription = node.title,
                    tint = if (completed) JadePrimary else node.accentColor,
                    modifier = Modifier.size(if (compact) 28.dp else 34.dp)
                )
            }

            // Completion Checkmark Badge
            if (completed) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(JadePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        contentDescription = "Mastered",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Text(
            text = node.title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = if (node.topicId < 0) node.subtitleZh else "${node.subtitleZh} · HSK ${node.minHsk}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
fun ShortcutPlacementQuizDialog(
    quiz: ActiveShortcutQuiz,
    onSpeak: (String) -> Unit,
    onFinish: (passed: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var index by remember(quiz.targetHskLevel) { mutableIntStateOf(0) }
    var correctCount by remember(quiz.targetHskLevel) { mutableIntStateOf(0) }
    var selectedOption by remember(index) { mutableStateOf<Int?>(null) }

    val question = quiz.questions[index]
    val answered = selectedOption != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(quiz.title, fontWeight = FontWeight.ExtraBold)
                LinearProgressIndicator(
                    progress = { (index + if (answered) 1 else 0).toFloat() / quiz.questions.size.coerceAtLeast(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = GoldAccent
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Question ${index + 1}/${quiz.questions.size} · Score 4/5 to unlock",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = question.word.hanzi,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = question.word.pinyin,
                    style = MaterialTheme.typography.titleSmall,
                    color = JadePrimary
                )
                OutlinedButton(
                    onClick = { onSpeak(question.word.hanzi) },
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Listen")
                }

                question.options.forEachIndexed { idx, opt ->
                    val isRight = answered && idx == question.correctIndex
                    val isWrong = answered && selectedOption == idx && idx != question.correctIndex
                    Surface(
                        onClick = {
                            if (!answered) {
                                selectedOption = idx
                                if (idx == question.correctIndex) correctCount++
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            isRight -> JadePrimary.copy(alpha = 0.18f)
                            isWrong -> CoralVermilion.copy(alpha = 0.18f)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        },
                        border = BorderStroke(
                            1.2.dp,
                            when {
                                isRight -> JadePrimary
                                isWrong -> CoralVermilion
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            }
                        )
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (answered) {
                PandaActionButton(
                    text = if (index == quiz.questions.lastIndex) "Finish ($correctCount/${quiz.questions.size})" else "Next",
                    onClick = {
                        if (index == quiz.questions.lastIndex) {
                            onFinish(correctCount >= 4)
                        } else {
                            index++
                        }
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
