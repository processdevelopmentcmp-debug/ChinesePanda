package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.practice.ListeningStudioSection
import com.example.ui.screens.practice.RadicalsStudioSection
import com.example.ui.screens.practice.SentenceStudioSection
import com.example.ui.screens.practice.SpeakStudioSection
import com.example.ui.screens.practice.WriteStudioSection
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel
import com.example.viewmodel.PracticeStudioTab

@Composable
fun PracticeHubScreen(viewModel: PandaViewModel) {
    val activeStudio by viewModel.activePracticeStudio.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontal Studio Selector Ribbon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PracticeStudioTab.entries.forEach { tab ->
                val selected = tab == activeStudio
                val icon = when (tab) {
                    PracticeStudioTab.WRITE -> Icons.Rounded.Brush
                    PracticeStudioTab.SPEAK -> Icons.Rounded.Mic
                    PracticeStudioTab.LISTENING -> Icons.Rounded.Headphones
                    PracticeStudioTab.SENTENCES -> Icons.Rounded.Extension
                    PracticeStudioTab.RADICALS -> Icons.Rounded.GridView
                }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { viewModel.selectPracticeStudio(tab) }
                        .testTag("practice_tab_${tab.name.lowercase()}"),
                    shape = RoundedCornerShape(50),
                    color = if (selected) JadePrimary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (selected) JadePrimary else MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Studio Content
        when (activeStudio) {
            PracticeStudioTab.WRITE -> WriteStudioSection(viewModel)
            PracticeStudioTab.SPEAK -> SpeakStudioSection(viewModel)
            PracticeStudioTab.LISTENING -> ListeningStudioSection(viewModel)
            PracticeStudioTab.SENTENCES -> SentenceStudioSection(viewModel)
            PracticeStudioTab.RADICALS -> RadicalsStudioSection(viewModel)
        }
    }
}
