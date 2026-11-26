package com.example.ui.screens.practice

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.UiState
import com.example.model.VocabWord
import com.example.ui.components.HskLevelPill
import com.example.ui.components.PandaActionButton
import com.example.ui.components.ToneColorLegendRow
import com.example.ui.components.ToneColoredHanziPinyinBlock
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IndigoAccent
import com.example.ui.theme.JadeDeep
import com.example.ui.theme.JadeLightContainer
import com.example.ui.theme.JadePrimary
import com.example.viewmodel.PandaViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraScannerStudioSection(viewModel: PandaViewModel) {
    val context = LocalContext.current
    val scanState by viewModel.cameraScanState.collectAsStateWithLifecycle()

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            localError = null
            previewBitmap = bitmap
            viewModel.analyzeCameraPhoto(bitmap)
        }
    }

    val cameraPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            localError = null
            cameraLauncher.launch(null)
        } else {
            localError = "Camera permission is needed to snap photos. You can still pick any image from your Photo Library!"
        }
    }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bmp = runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val src = ImageDecoder.createSource(context.contentResolver, uri)
                    ImageDecoder.decodeBitmap(src) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }
            }.getOrNull()
            if (bmp != null) {
                localError = null
                previewBitmap = bmp
                viewModel.analyzeCameraPhoto(bmp)
            } else {
                localError = "Could not load the selected image."
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.horizontalGradient(listOf(JadePrimary, IndigoAccent)))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Text(
                        text = "Camera \"Point & Learn\" Scanner",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
                Text(
                    text = "Snap a photo of a coffee cup, street sign, book, or menu—Gemini Vision returns the Chinese word, tone colors, radical tip, and an HSK example sentence.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }
        }

        // Action Buttons: Snap Photo + Pick Photo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PandaActionButton(
                text = "Snap Photo",
                onClick = {
                    val hasCameraPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasCameraPerm) {
                        cameraLauncher.launch(null)
                    } else {
                        cameraPermLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                containerColor = JadePrimary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("camera_snap_photo_button"),
                leadingIcon = { Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )

            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("camera_pick_photo_button")
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Gallery", style = MaterialTheme.typography.labelLarge)
            }
        }

        // Photo Preview Frame
        previewBitmap?.let { bmp ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.5f))
            ) {
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Scanned object photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        if (localError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.errorContainer
            ) {
                Text(
                    text = localError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Gemini Vision Result Area
        when (val state = scanState) {
            null -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = JadePrimary,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = "Point your camera at the world around you",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Every scanned object gives you instant tone-colored Hanzi, native audio, a radical memory hook, and 1-tap flashcard saving.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        ToneColorLegendRow()
                    }
                }
            }
            is UiState.Loading -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = JadePrimary)
                        Spacer(Modifier.width(12.dp))
                        Text("Gemini Vision is identifying Chinese vocabulary...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            is UiState.Error -> {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            is UiState.Success -> {
                val res = state.data
                val asVocab = VocabWord(
                    key = "scan_${res.hanzi}",
                    hanzi = res.hanzi,
                    pinyin = res.pinyin,
                    english = res.english,
                    hskLevel = res.hskLevel,
                    topicId = 1,
                    writable = true,
                    exampleHanzi = res.exampleHanzi,
                    examplePinyin = res.examplePinyin,
                    exampleEnglish = res.exampleEnglish
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_scan_result_card"),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    border = BorderStroke(1.5.dp, JadePrimary.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                HskLevelPill(level = res.hskLevel)
                                ToneColoredHanziPinyinBlock(
                                    hanzi = res.hanzi,
                                    pinyin = res.pinyin,
                                    hanziFontSize = 36.sp
                                )
                                Text(
                                    text = res.english,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakChinese(res.hanzi) },
                                modifier = Modifier.testTag("camera_speak_result_button")
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Listen word", tint = JadePrimary, modifier = Modifier.size(28.dp))
                            }
                        }

                        ToneColorLegendRow()

                        if (res.radicalTip.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = res.radicalTip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        // HSK Example Sentence
                        if (res.exampleHanzi.isNotBlank()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { viewModel.speakChinese(res.exampleHanzi) },
                                shape = RoundedCornerShape(14.dp),
                                color = JadeLightContainer.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("HSK ${res.hskLevel} Example Sentence", style = MaterialTheme.typography.labelSmall, color = JadeDeep, fontWeight = FontWeight.Bold)
                                        ToneColoredHanziPinyinBlock(
                                            hanzi = res.exampleHanzi,
                                            pinyin = res.examplePinyin,
                                            hanziFontSize = 18.sp
                                        )
                                        Text(res.exampleEnglish, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = "Hear example", tint = JadePrimary, modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Quick actions: Practice Writing & Save to SRS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.jumpToWriteWord(asVocab) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Rounded.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Write")
                            }
                            PandaActionButton(
                                text = "+ Save to SM-2",
                                onClick = { viewModel.addWordsBatchToSrs(listOf(asVocab)) },
                                containerColor = IndigoAccent,
                                modifier = Modifier.weight(1.3f),
                                leadingIcon = { Icon(Icons.Rounded.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        // Secondary visible objects
                        if (res.relatedObjects.isNotEmpty()) {
                            Text("Also spotted in photo:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                res.relatedObjects.forEach { obj ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.speakChinese(obj.hanzi) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                                    ) {
                                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                            ToneColoredHanziPinyinBlock(
                                                hanzi = obj.hanzi,
                                                pinyin = obj.pinyin,
                                                hanziFontSize = 16.sp
                                            )
                                            Text(obj.english, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
