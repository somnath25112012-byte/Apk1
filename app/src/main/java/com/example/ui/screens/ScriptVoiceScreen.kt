package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.ExportedVideo
import com.example.model.VoiceEmotionPreset
import com.example.model.VoiceStyle
import com.example.ui.StudioScreen
import com.example.ui.StudioViewModel
import com.example.ui.components.RealMp4Player
import com.example.ui.components.VideoPreviewPlayer
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.HeritageGoldLight
import com.example.ui.theme.PeacockEmerald
import com.example.ui.theme.RoyalBurgundy
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptVoiceScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val project by viewModel.currentProject.collectAsState()
    val brandProfile by viewModel.brandProfile.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPositionSeconds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val volumeLevel by viewModel.volumeLevel.collectAsState()

    val isExporting by viewModel.isExporting.collectAsState()
    val exportPercent by viewModel.exportProgressPercent.collectAsState()
    val exportStatus by viewModel.exportStatusText.collectAsState()
    val exportError by viewModel.exportError.collectAsState()
    val lastExportedVideo by viewModel.lastExportedVideo.collectAsState()

    val voicePreviewStatus by viewModel.voicePreviewStatus.collectAsState()
    val isSpeaking by viewModel.voiceService.isSpeakingFlow.collectAsState()

    var showRealMp4Dialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("script_voice_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        if (project == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "কোনো অ্যাক্টিভ প্রজেক্ট নেই। অনুগ্রহ করে প্রথমে AI ভিডিও তৈরি করুন।", color = Color.White)
            }
            return
        }

        // Section I: Interactive Video Preview Player
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ভিডিও প্রিভিউ (লোগো ও ক্যাপশন সহ)",
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (lastExportedVideo != null) {
                OutlinedButton(
                    onClick = { showRealMp4Dialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PeacockEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PeacockEmerald),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Movie, contentDescription = "MP4", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "রিয়েল MP4 চালান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        VideoPreviewPlayer(
            project = project!!,
            brandProfile = brandProfile,
            isPlaying = isPlaying,
            playbackPositionSeconds = playbackPosition,
            isMuted = isMuted,
            volumeLevel = volumeLevel,
            onTogglePlayPause = { viewModel.togglePlayPause() },
            onSeek = { viewModel.seekTo(it) },
            onToggleMute = { viewModel.toggleMute() },
            onVolumeChange = { viewModel.setVolume(it) },
            activeScene = viewModel.getCurrentActiveScene()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Watermark & Logo status badge (Section M1)
        Surface(
            color = Color(0xFF1C1822),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382F3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.BrandingWatermark,
                    contentDescription = "Logo",
                    tint = HeritageGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (brandProfile.showLogo) "অফিসিয়াল ব্র্যান্ড লোগো সক্রিয় (${brandProfile.watermarkPosition.titleBn})" else "লোগো বন্ধ আছে",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "আকার: ${brandProfile.watermarkSize.titleBn} • স্বচ্ছতা: ${(brandProfile.watermarkOpacity * 100).toInt()}% • আসল MP4-তে এম্বেড হবে",
                        color = Color(0xFFC7BBAE),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice-over selection & preview (PART 5 & 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1620)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3244))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = HeritageGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "বাংলা এআই ভয়েসওভার",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.previewActiveVoiceover() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold),
                        modifier = Modifier.testTag("btn_preview_voiceover")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Play", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isSpeaking) "প্লে হচ্ছে..." else "ভয়েস শুনুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (voicePreviewStatus != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = voicePreviewStatus ?: "", color = HeritageGoldLight, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Emotion Presets
                Text(
                    text = "কণ্ঠের মেজাজ ও প্রকাশভঙ্গি (Voice Emotion):",
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VoiceEmotionPreset.values().forEach { preset ->
                        val isSel = project?.voicePreset == preset
                        Surface(
                            color = if (isSel) PeacockEmerald else Color(0xFF26202C),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) HeritageGold else Color(0xFF3F3547)
                            ),
                            modifier = Modifier.clickable {
                                project?.let {
                                    viewModel.generateAiVideo() // keeps preset in sync
                                }
                            }
                        ) {
                            Text(
                                text = preset.titleBn,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Styles
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val voices = listOf(
                        VoiceStyle.BENGALI_FEMALE_NATURAL,
                        VoiceStyle.BENGALI_FEMALE_PROMOTIONAL,
                        VoiceStyle.BENGALI_MALE_NATURAL,
                        VoiceStyle.BENGALI_MALE_PROMOTIONAL
                    )
                    voices.forEach { vs ->
                        val isSel = project?.voiceStyle == vs
                        Surface(
                            color = if (isSel) RoyalBurgundy else Color(0xFF26202C),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) HeritageGold else Color(0xFF3F3547)
                            ),
                            modifier = Modifier.clickable { viewModel.setProjectVoiceStyle(vs) }
                        ) {
                            Text(
                                text = vs.titleBn,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bengali Script by Scene (Section E & G)
        Text(
            text = "দৃশ্য ও বাংলা স্ক্রিপ্ট সম্পাদনা (${project?.scenes?.size ?: 0}টি দৃশ্য)",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        project?.scenes?.forEach { scene ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1720)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF342B3C))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "দৃশ্য ${scene.order}: ${scene.titleBn}",
                            color = HeritageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = RoyalBurgundy.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${scene.durationSeconds} সেকেন্ড",
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ভয়েসওভার স্ক্রিপ্ট (Bengali Narration):",
                        color = Color(0xFFC7BBAE),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scene.narrationScriptBn,
                        onValueChange = { viewModel.updateSceneNarration(scene.id, it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = studioTextFieldColors(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "অন-স্ক্রিন বাংলা ক্যাপশন (On-Screen Text):",
                        color = Color(0xFFC7BBAE),
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = scene.onScreenCaptionBn,
                        onValueChange = { viewModel.updateSceneCaption(scene.id, it) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = studioTextFieldColors(),
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Export Progress Card
        if (isExporting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D2C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = HeritageGold,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "MP4 ভিডিও এক্সপোর্ট হচ্ছে... ($exportPercent%)",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = exportStatus,
                                color = Color(0xFFD4C8B8),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { exportPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = HeritageGold,
                        trackColor = Color(0xFF42374A)
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (exportError != null) {
            Surface(
                color = Color(0xFF381418),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exportError ?: "",
                    color = Color(0xFFFFB4AB),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Action Button: "ভিডিও তৈরি করুন ও MP4 সংরক্ষণ করুন"
        Button(
            onClick = { viewModel.exportCurrentProject() },
            enabled = !isExporting,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_export_mp4"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HeritageGold,
                contentColor = Color.Black
            )
        ) {
            Icon(
                imageVector = Icons.Default.MovieCreation,
                contentDescription = "Export",
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "ভিডিও তৈরি করুন ও MP4 সংরক্ষণ করুন",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Real MP4 Preview Dialog
    if (lastExportedVideo != null && showRealMp4Dialog) {
        val exported = lastExportedVideo!!
        val file = File(exported.filePath)

        AlertDialog(
            onDismissRequest = { showRealMp4Dialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "OK", tint = PeacockEmerald, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "রিয়েল MP4 প্রিভিউ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    RealMp4Player(videoFile = file, brandProfile = brandProfile, autoPlay = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "${exported.titleBn} (${exported.durationSeconds} সে.)", color = HeritageGold, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { shareExportedVideo(context, exported) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার")
                    }
                    Button(
                        onClick = {
                            showRealMp4Dialog = false
                            viewModel.setScreen(StudioScreen.MY_VIDEOS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                    ) {
                        Text("আমার ভিডিওতে দেখুন")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRealMp4Dialog = false }) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1724)
        )
    }
}

private fun shareExportedVideo(context: Context, video: ExportedVideo) {
    val file = File(video.filePath)
    if (!file.exists()) return

    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_SUBJECT, video.titleBn)
            putExtra(Intent.EXTRA_TEXT, "অন্বেষার রসনা বিলাস - অফিসিয়াল ভিডিও: ${video.titleBn}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "ভিডিও শেয়ার করুন")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {}
}
