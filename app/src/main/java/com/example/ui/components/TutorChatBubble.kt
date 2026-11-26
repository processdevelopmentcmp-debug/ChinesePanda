package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SlowMotionVideo
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.AiChatMessage
import com.example.model.GroundedSource
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralOnContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary

@Composable
fun TutorMessageBubble(
    message: AiChatMessage,
    onSpeakNormal: (String) -> Unit,
    onSpeakSlow: (String) -> Unit,
    onWriteHanzi: (String) -> Unit,
    onCopy: (String) -> Unit,
    onRetry: (errorId: String, prompt: String) -> Unit
) {
    val isUser = message.fromUser

    if (message.isError) {
        TutorErrorBubble(
            message = message,
            onRetry = onRetry
        )
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            PandaMascotAvatar(
                size = 32.dp,
                animated = message.streaming,
                showScholarHat = true,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.84f else 0.88f),
            shape = RoundedCornerShape(
                topStart = if (isUser) 20.dp else 6.dp,
                topEnd = if (isUser) 6.dp else 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = if (isUser) JadePrimary else MaterialTheme.colorScheme.surface,
            tonalElevation = if (isUser) 0.dp else 1.dp,
            border = if (isUser) {
                null
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
            }
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isUser && !message.modeLabel.isNullOrBlank()) {
                    Text(
                        text = message.modeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = JadePrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (message.streaming && message.text.isEmpty()) {
                    TypingDotsIndicator()
                } else {
                    val formatted = remember(message.text, isUser) {
                        formatTutorMarkdown(
                            raw = message.text,
                            accentColor = if (isUser) Color.White else JadeDeep
                        )
                    }
                    Text(
                        text = formatted,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }

                if (!isUser && !message.streaming && message.text.isNotBlank()) {
                    val chineseSpeechText = remember(message.text) {
                        extractSpeakableChinese(message.text)
                    }
                    val writeTarget = remember(message.text) {
                        extractPracticeHanzi(message.text)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (chineseSpeechText.isNotBlank()) {
                            BubbleActionChip(
                                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                                label = "Hear",
                                contentDesc = stringResource(R.string.cd_hear_chinese_normal),
                                onClick = { onSpeakNormal(chineseSpeechText) }
                            )
                            BubbleActionChip(
                                icon = Icons.Rounded.SlowMotionVideo,
                                label = "0.75x",
                                contentDesc = stringResource(R.string.cd_hear_chinese_slow),
                                onClick = { onSpeakSlow(chineseSpeechText) }
                            )
                        }
                        if (writeTarget.isNotBlank()) {
                            BubbleActionChip(
                                icon = Icons.Rounded.Brush,
                                label = "Write $writeTarget",
                                contentDesc = stringResource(R.string.cd_practice_writing),
                                onClick = { onWriteHanzi(writeTarget) }
                            )
                        }
                        BubbleActionChip(
                            icon = Icons.Rounded.ContentCopy,
                            label = "Copy",
                            contentDesc = stringResource(R.string.cd_copy_message),
                            onClick = { onCopy(message.text) }
                        )
                    }

                    if (message.sources.isNotEmpty() || message.searchQueries.isNotEmpty()) {
                        GroundedSourcesSection(
                            sources = message.sources,
                            queries = message.searchQueries
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroundedSourcesSection(
    sources: List<GroundedSource>,
    queries: List<String>
) {
    val uriHandler = LocalUriHandler.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = IndigoContainer.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = IndigoAccent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Grounded with Google Search",
                    style = MaterialTheme.typography.labelSmall,
                    color = IndigoAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            if (queries.isNotEmpty()) {
                Text(
                    text = "Searched: ${queries.distinct().take(3).joinToString(" · ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (sources.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    sources.distinctBy { it.uri }.take(5).forEach { source ->
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    runCatching { uriHandler.openUri(source.uri) }
                                },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, IndigoAccent.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = source.title.take(26),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoAccent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                                    contentDescription = stringResource(R.string.cd_open_source_link),
                                    tint = IndigoAccent,
                                    modifier = Modifier.size(12.dp)
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
private fun TutorErrorBubble(
    message: AiChatMessage,
    onRetry: (errorId: String, prompt: String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        PandaMascotAvatar(
            size = 32.dp,
            animated = false,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(
                topStart = 6.dp,
                topEnd = 20.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = CoralContainer,
            border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.45f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = CoralVermilion,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Panda Laoshi couldn't reply",
                        style = MaterialTheme.typography.titleSmall,
                        color = CoralOnContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = CoralOnContainer
                )
                val promptToRetry = message.failedPrompt
                if (!promptToRetry.isNullOrBlank()) {
                    FilledTonalButton(
                        onClick = { onRetry(message.id, promptToRetry) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("ai_teacher_retry_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = CoralVermilion,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = stringResource(R.string.tutor_retry))
                    }
                }
            }
        }
    }
}

@Composable
private fun BubbleActionChip(
    icon: ImageVector,
    label: String,
    contentDesc: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = JadeLightContainer.copy(alpha = 0.65f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                tint = JadeDeep,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = JadeDeep,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TypingDotsIndicator() {
    val transition = rememberInfiniteTransition(label = "tutor_typing")
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(0, 180, 360).forEach { delay ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, delayMillis = delay),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$delay"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(JadePrimary)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Panda Laoshi is typing...",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun extractSpeakableChinese(text: String): String {
    return text.filter { ch ->
        Character.UnicodeScript.of(ch.code) == Character.UnicodeScript.HAN ||
            ch == '，' || ch == '。' || ch == '？' || ch == '！'
    }.take(120)
}

private fun extractPracticeHanzi(text: String): String {
    val runs = Regex("[\\p{IsHan}]{1,4}")
        .findAll(text)
        .map { it.value }
        .filter { it != "熊猫老师" }
        .toList()
    return runs.firstOrNull { it.length in 2..4 } ?: runs.firstOrNull().orEmpty()
}

private fun formatTutorMarkdown(raw: String, accentColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = raw.lines()
        lines.forEachIndexed { lineIdx, originalLine ->
            val trimmed = originalLine.trimStart()
            val (lineContent, isHeader) = when {
                trimmed.startsWith("### ") -> trimmed.removePrefix("### ") to true
                trimmed.startsWith("## ") -> trimmed.removePrefix("## ") to true
                trimmed.startsWith("# ") -> trimmed.removePrefix("# ") to true
                trimmed.startsWith("- ") -> "• " + trimmed.removePrefix("- ") to false
                trimmed.startsWith("* ") -> "• " + trimmed.removePrefix("* ") to false
                else -> originalLine to false
            }

            if (isHeader) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = accentColor)) {
                    appendInlineMarkdown(lineContent)
                }
            } else {
                appendInlineMarkdown(lineContent)
            }

            if (lineIdx < lines.lastIndex) {
                append('\n')
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(line: String) {
    var i = 0
    while (i < line.length) {
        if (i + 1 < line.length && line[i] == '*' && line[i + 1] == '*') {
            val end = line.indexOf("**", startIndex = i + 2)
            if (end != -1) {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(line.substring(i + 2, end))
                }
                i = end + 2
                continue
            }
        }
        if (line[i] == '*') {
            val end = line.indexOf('*', startIndex = i + 1)
            if (end != -1 && end > i + 1) {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                    append(line.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }
        if (line[i] == '`') {
            val end = line.indexOf('`', startIndex = i + 1)
            if (end != -1 && end > i + 1) {
                withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                    append(line.substring(i + 1, end))
                }
                i = end + 1
                continue
            }
        }
        append(line[i])
        i++
    }
}
