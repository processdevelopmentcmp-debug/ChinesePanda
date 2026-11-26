package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.audio.LiveVoiceTutorManager
import com.example.model.LiveVoiceState
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralOnContainer
import com.example.ui.theme.CoralVermilion
import com.example.ui.theme.GoldBright
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary

@Composable
fun LiveVoiceTutorDialog(
    liveState: LiveVoiceState,
    hskLevel: Int,
    selectedVoice: String,
    onSelectVoice: (String) -> Unit,
    onSelectHskLevel: (Int) -> Unit,
    onStartLive: () -> Unit,
    onStopLive: () -> Unit,
    onDismiss: () -> Unit
) {
    val isActive = liveState is LiveVoiceState.Active
    val isConnecting = liveState is LiveVoiceState.Connecting

    val transition = rememberInfiniteTransition(label = "live_voice_halo")
    val haloScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isActive) 1.24f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "halo_scale"
    )

    Dialog(
        onDismissRequest = {
            onStopLive()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isActive) CoralVermilion else JadeLightContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.GraphicEq,
                                    contentDescription = null,
                                    tint = if (isActive) Color.White else JadeDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isActive) "LIVE · Gemini 3.8 Live" else "Gemini Live API",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isActive) Color.White else JadeDeep,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            onStopLive()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_live_voice_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                }

                // Animated Panda Laoshi Voice Orb
                Box(
                    modifier = Modifier.size(118.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isActive || isConnecting) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(haloScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            JadePrimary.copy(alpha = 0.32f),
                                            GoldBright.copy(alpha = 0.14f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    PandaMascotAvatar(
                        size = 82.dp,
                        animated = isActive || isConnecting,
                        showScholarHat = true
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.live_voice_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = when (liveState) {
                            is LiveVoiceState.Idle ->
                                "Speak Mandarin or English in real time — Panda Laoshi listens and replies aloud instantly."
                            is LiveVoiceState.Connecting ->
                                "Connecting to Gemini Live real-time audio..."
                            is LiveVoiceState.Active ->
                                "Listening live at HSK ${liveState.hskLevel} (${liveState.voiceName}) · Speak anytime!"
                            is LiveVoiceState.Error ->
                                "Real-time voice session paused."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                // Voice & Level Pickers (when idle or error)
                if (liveState is LiveVoiceState.Idle || liveState is LiveVoiceState.Error) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Tutor Voice",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            LiveVoiceTutorManager.AVAILABLE_VOICES.forEach { voice ->
                                FilterChip(
                                    selected = selectedVoice == voice,
                                    onClick = { onSelectVoice(voice) },
                                    label = { Text(voice) }
                                )
                            }
                        }

                        Text(
                            text = "HSK 3.0 Speaking Level",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            (1..9).forEach { lvl ->
                                FilterChip(
                                    selected = hskLevel == lvl,
                                    onClick = { onSelectHskLevel(lvl) },
                                    label = { Text("HSK $lvl") }
                                )
                            }
                        }
                    }
                }

                // Connecting Spinner
                if (isConnecting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = JadePrimary
                        )
                        Text(
                            text = "Opening real-time audio stream...",
                            style = MaterialTheme.typography.bodySmall,
                            color = JadeDeep
                        )
                    }
                }

                // Real-time Transcripts Feed
                if (liveState is LiveVoiceState.Active) {
                    val listState = rememberLazyListState()
                    LaunchedEffect(liveState.transcripts.size, liveState.transcripts.lastOrNull()?.text?.length) {
                        if (liveState.transcripts.isNotEmpty()) {
                            listState.animateScrollToItem(liveState.transcripts.lastIndex)
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 220.dp),
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    ) {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(liveState.transcripts, key = { it.id }) { line ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (line.fromUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (line.fromUser) JadePrimary else MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth(0.88f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                                            Text(
                                                text = if (line.fromUser) "You" else "Panda Laoshi",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (line.fromUser) Color.White.copy(alpha = 0.8f) else JadePrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = line.text,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (line.fromUser) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Transparent Error Card
                if (liveState is LiveVoiceState.Error) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = CoralContainer,
                        border = BorderStroke(1.dp, CoralVermilion.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = CoralVermilion,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = liveState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = CoralOnContainer
                            )
                        }
                    }
                }

                // Primary Call Control Button
                when (liveState) {
                    is LiveVoiceState.Active, is LiveVoiceState.Connecting -> {
                        PandaActionButton(
                            text = stringResource(R.string.live_voice_end),
                            onClick = onStopLive,
                            containerColor = CoralVermilion,
                            contentColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_voice_end_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.CallEnd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    is LiveVoiceState.Error -> {
                        PandaActionButton(
                            text = "Retry Live Voice",
                            onClick = onStartLive,
                            containerColor = JadePrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_voice_retry_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }

                    is LiveVoiceState.Idle -> {
                        PandaActionButton(
                            text = stringResource(R.string.live_voice_start),
                            onClick = onStartLive,
                            containerColor = JadePrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_voice_start_button"),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
