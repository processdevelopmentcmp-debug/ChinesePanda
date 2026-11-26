package com.example.ui.screens.practice

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.HanziSerifFontFamily
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@Composable
fun WriteStudioSection(viewModel: PandaViewModel) {
    val activeWord by viewModel.activeWriteWord.collectAsStateWithLifecycle()
    val allVocab by viewModel.allVocab.collectAsStateWithLifecycle()

    val chars = remember(activeWord.hanzi) {
        activeWord.hanzi.filter { !it.isWhitespace() }.map { it.toString() }.ifEmpty { listOf("好") }
    }
    var activeCharIdx by remember(activeWord.hanzi) { mutableIntStateOf(0) }
    val activeChar = chars.getOrElse(activeCharIdx) { chars.first() }

    var showOutline by remember { mutableStateOf(true) }
    var pulseGuide by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var feedbackText by remember(activeChar) { mutableStateOf<String?>(null) }

    val strokes = remember(activeChar) { mutableStateListOf<List<Offset>>() }
    var activeStroke by remember(activeChar) { mutableStateOf<List<Offset>>(emptyList()) }

    val guideAlpha = if (pulseGuide) {
        val transition = rememberInfiniteTransition(label = "guide_pulse")
        val a by transition.animateFloat(
            initialValue = 0.14f,
            targetValue = 0.42f,
            animationSpec = infiniteRepeatable(
                animation = tween(900, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "guide_alpha"
        )
        a
    } else 0.22f

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
                placeholder = { Text("Type any Hanzi (e.g. 扫码 / 森林)") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            PandaActionButton(
                text = "Load",
                onClick = {
                    if (customInput.isNotBlank()) {
                        viewModel.jumpToWriteHanzi(customInput)
                        customInput = ""
                    }
                }
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = activeWord.hanzi,
                            fontFamily = HanziSerifFontFamily,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = activeWord.pinyin,
                            style = MaterialTheme.typography.titleSmall,
                            color = JadePrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        HskLevelPill(level = activeWord.hskLevel)
                        IconButton(onClick = { viewModel.speakChinese(activeWord.hanzi) }) {
                            Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen", tint = JadePrimary)
                        }
                    }
                }
                Text(
                    text = activeWord.english,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // Multi-character selector pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chars.forEachIndexed { idx, ch ->
                        val selected = idx == activeCharIdx
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { activeCharIdx = idx },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) JadePrimary else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = ch,
                                fontFamily = HanziSerifFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Mi Zi Ge Calligraphy Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 310.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .testTag("mizige_writing_canvas"),
            contentAlignment = Alignment.Center
        ) {
            // Faint Reference Character Outline
            if (showOutline) {
                Text(
                    text = activeChar,
                    fontFamily = HanziSerifFontFamily,
                    fontSize = 190.sp,
                    color = JadeDeep,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.alpha(guideAlpha)
                )
            }

            // Mi Zi Ge grid + User Ink Strokes
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeChar) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                activeStroke = listOf(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                activeStroke = activeStroke + change.position
                            },
                            onDragEnd = {
                                if (activeStroke.size > 1) {
                                    strokes.add(activeStroke)
                                }
                                activeStroke = emptyList()
                            },
                            onDragCancel = {
                                activeStroke = emptyList()
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                val dash = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                val gridColor = CoralVermilion.copy(alpha = 0.35f)

                // Outer Vermilion Border
                drawRect(
                    color = CoralVermilion,
                    style = Stroke(width = 6f)
                )
                // Crosshairs
                drawLine(gridColor, Offset(w / 2f, 0f), Offset(w / 2f, h), strokeWidth = 2.5f, pathEffect = dash)
                drawLine(gridColor, Offset(0f, h / 2f), Offset(w, h / 2f), strokeWidth = 2.5f, pathEffect = dash)
                // Diagonals (Mi Zi Ge 米)
                drawLine(gridColor.copy(alpha = 0.25f), Offset(0f, 0f), Offset(w, h), strokeWidth = 2f, pathEffect = dash)
                drawLine(gridColor.copy(alpha = 0.25f), Offset(w, 0f), Offset(0f, h), strokeWidth = 2f, pathEffect = dash)

                // Draw committed strokes with calligraphy brush feel
                val allPaths = strokes + listOfNotNull(activeStroke.takeIf { it.size > 1 })
                allPaths.forEach { pts ->
                    val path = Path().apply {
                        moveTo(pts.first().x, pts.first().y)
                        for (i in 1 until pts.size) {
                            lineTo(pts[i].x, pts[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF0F2B24),
                        style = Stroke(
                            width = 20f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // Stroke Counter Badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp),
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            ) {
                Text(
                    text = "${strokes.size} strokes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Calligraphy Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { showOutline = !showOutline },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Icon(
                    if (showOutline) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = "Toggle outline",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (showOutline) "Hide Guide" else "Guide", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = { pulseGuide = !pulseGuide },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Text(if (pulseGuide) "Stop Pulse" else "Trace Pulse", style = MaterialTheme.typography.labelMedium)
            }

            IconButton(
                onClick = { if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex) },
                enabled = strokes.isNotEmpty()
            ) {
                Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo stroke")
            }

            IconButton(
                onClick = {
                    strokes.clear()
                    feedbackText = null
                }
            ) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Clear canvas")
            }
        }

        PandaActionButton(
            text = "Check Calligraphy (+15 XP)",
            onClick = {
                if (strokes.isEmpty()) {
                    feedbackText = "Draw '$activeChar' inside the red Mi Zi Ge grid first!"
                } else {
                    feedbackText = "Great brush balance on '$activeChar' (${strokes.size} strokes)! Keep strokes top-to-bottom and left-to-right."
                    viewModel.awardXp(15L, null, "Calligraphy practice: $activeChar")
                    viewModel.speakChinese(activeChar)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("check_calligraphy_button"),
            leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )

        if (feedbackText != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = feedbackText!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Quick Pick Writable HSK Words
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Quick Pick Writable HSK 3.0 Words",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allVocab.filter { it.writable }.take(18).forEach { w ->
                    val selected = w.hanzi == activeWord.hanzi
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.jumpToWriteWord(w) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
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
                                color = if (selected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
