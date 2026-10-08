package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AspectRatioType
import com.example.model.MusicTrack
import com.example.model.VisualSourceMode
import com.example.model.VoiceEmotionPreset
import com.example.model.VoiceStyle
import com.example.ui.StudioViewModel
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.HeritageGoldLight
import com.example.ui.theme.PeacockEmerald
import com.example.ui.theme.RoyalBurgundy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiGeneratorScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val topic by viewModel.aiTopic.collectAsState()
    val business by viewModel.aiBusiness.collectAsState()
    val purpose by viewModel.aiPurpose.collectAsState()
    val style by viewModel.aiStyle.collectAsState()
    val durationSeconds by viewModel.aiDurationSeconds.collectAsState()
    val voiceStyle by viewModel.aiVoiceStyle.collectAsState()
    val voicePreset by viewModel.aiVoicePreset.collectAsState()
    val voiceSpeed by viewModel.aiVoiceSpeed.collectAsState()
    val visualSourceMode by viewModel.aiVisualSourceMode.collectAsState()
    val musicTrack by viewModel.aiMusicTrack.collectAsState()
    val isMusicEnabled by viewModel.aiIsMusicEnabled.collectAsState()
    val isCaptionsEnabled by viewModel.aiIsCaptionsEnabled.collectAsState()
    val captionFontSize by viewModel.aiCaptionFontSize.collectAsState()
    val aspectRatio by viewModel.aiAspectRatio.collectAsState()
    val instructions by viewModel.aiInstructions.collectAsState()
    val brandProfile by viewModel.brandProfile.collectAsState()

    val isGeneratingScript by viewModel.isGeneratingScript.collectAsState()
    val isWorkflowRunning by viewModel.isWorkflowRunning.collectAsState()
    val workflowStatus by viewModel.workflowStatusMessage.collectAsState()
    val voicePreviewStatus by viewModel.voicePreviewStatus.collectAsState()
    val isSpeaking by viewModel.voiceService.isSpeakingFlow.collectAsState()
    val isFallbackTtsEnabled by viewModel.voiceService.isFallbackEnabledFlow.collectAsState()

    val isExporting by viewModel.isExporting.collectAsState()
    val exportPercent by viewModel.exportProgressPercent.collectAsState()
    val exportStatus by viewModel.exportStatusText.collectAsState()
    val exportError by viewModel.exportError.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("ai_generator_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Screen Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1822)),
            border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(HeritageGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI ভিডিও স্টুডিও",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "টেক্সট প্রম্পট থেকে সরাসরি পূর্ণাঙ্গ বাংলা সিনেমাটিক MP4 ভিডিও",
                            fontSize = 12.sp,
                            color = Color(0xFFD4C8B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Engine & API Status Banner
                val isNeuralConnected = viewModel.voiceService.isRealNeuralApiConnected
                Surface(
                    color = if (isNeuralConnected) Color(0xFF14271B) else Color(0xFF2B2117),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isNeuralConnected) Color(0xFF2E7D32) else Color(0xFF6B4E2A)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isNeuralConnected) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "API Status",
                            tint = if (isNeuralConnected) Color(0xFF81C784) else HeritageGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isNeuralConnected)
                                "Gemini AI ক্লাউড কানেক্টেড (স্ক্রিপ্ট + নিউরাল ভয়েস + ভিজ্যুয়াল)"
                            else
                                "লোকাল ইঞ্জিন সক্রিয় • AI ক্লাউডের জন্য Secrets-এ GEMINI_API_KEY দিন",
                            fontSize = 11.sp,
                            color = if (isNeuralConnected) Color(0xFFA5D6A7) else HeritageGoldLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Topic / Prompt Field
        Text(
            text = "ভিডিওর বিষয় / প্রম্পট (Topic)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = topic,
            onValueChange = { viewModel.aiTopic.value = it },
            placeholder = { Text("যেমন: ঘরোয়া খাঁটি বাঙালি রসনা বিলাস ও আজকের স্পেশাল মেনু") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ai_topic"),
            shape = RoundedCornerShape(12.dp),
            colors = studioTextFieldColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Business Name
        Text(
            text = "ব্যবসা / রেস্তোরাঁর নাম",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = business,
            onValueChange = { viewModel.aiBusiness.value = it },
            placeholder = { Text("অন্বেষার রসনা বিলাস") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ai_business"),
            shape = RoundedCornerShape(12.dp),
            colors = studioTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Visual Source Mode (AI Generated, User Media, Mixed)
        Text(
            text = "ভিজ্যুয়াল সোর্স (Visual Mode)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VisualSourceMode.values().forEach { mode ->
                val isSel = visualSourceMode == mode
                Surface(
                    color = if (isSel) RoyalBurgundy else Color(0xFF221C26),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.aiVisualSourceMode.value = mode }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                VisualSourceMode.AI_GENERATED -> Icons.Default.AutoAwesome
                                VisualSourceMode.USER_MEDIA -> Icons.Default.Image
                                VisualSourceMode.MIXED -> Icons.Default.MovieCreation
                            },
                            contentDescription = mode.titleBn,
                            tint = if (isSel) HeritageGold else Color(0xFFB0A294),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.titleBn,
                            color = if (isSel) Color.White else Color(0xFFD4C8B8),
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Duration Options (30s, 45s, 60s, 90s - Minimum 30s)
        Text(
            text = "সময়কাল (Duration - ন্যূনতম ৩০ সেকেন্ড)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(30, 45, 60, 90).forEach { sec ->
                val isSel = durationSeconds == sec
                Surface(
                    color = if (isSel) HeritageGold else Color(0xFF221C26),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.aiDurationSeconds.value = sec }
                        .testTag("duration_option_$sec")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$sec সে.",
                            color = if (isSel) Color.Black else Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expressive Bengali Voice Section (PART 5 & 6)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1622)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3246))
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
                            text = "বাঙালি এআই ভয়েসওভার",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { viewModel.previewSelectedVoice() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold),
                        modifier = Modifier.testTag("btn_preview_voice_sample")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Listen",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isSpeaking) "প্লে হচ্ছে..." else "ভয়েস শুনুন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (voicePreviewStatus != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = voicePreviewStatus ?: "",
                        color = HeritageGoldLight,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Voice Emotion & Style Presets
                Text(
                    text = "কণ্ঠের মেজাজ ও স্টাইল (Emotion Preset):",
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VoiceEmotionPreset.values().forEach { pr ->
                        val isSel = voicePreset == pr
                        Surface(
                            color = if (isSel) PeacockEmerald else Color(0xFF261F2E),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) HeritageGold else Color(0xFF3F3547)
                            ),
                            modifier = Modifier.clickable { viewModel.aiVoicePreset.value = pr }
                        ) {
                            Text(
                                text = pr.titleBn,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gender & Voice Type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val voices = listOf(
                        Pair(VoiceStyle.BENGALI_FEMALE_NATURAL, "বাঙালি নারী (স্বাভাবিক)"),
                        Pair(VoiceStyle.BENGALI_FEMALE_PROMOTIONAL, "বাঙালি নারী (বিজ্ঞাপনী)"),
                        Pair(VoiceStyle.BENGALI_MALE_NATURAL, "বাঙালি পুরুষ (স্বাভাবিক)")
                    )
                    voices.forEach { (vs, label) ->
                        val isSel = voiceStyle == vs
                        Surface(
                            color = if (isSel) RoyalBurgundy else Color(0xFF261F2E),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSel) HeritageGold else Color(0xFF3F3547)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { viewModel.aiVoiceStyle.value = vs }
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color.White else Color(0xFFC7BBAE),
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Speaking Speed Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গতি: ${String.format("%.1f", voiceSpeed)}x",
                        color = Color(0xFFD4C8B8),
                        fontSize = 11.sp,
                        modifier = Modifier.width(68.dp)
                    )
                    Slider(
                        value = voiceSpeed,
                        onValueChange = { viewModel.aiVoiceSpeed.value = it },
                        valueRange = 0.8f..1.3f,
                        steps = 4,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = HeritageGold,
                            activeTrackColor = HeritageGold,
                            inactiveTrackColor = Color(0xFF382E3A)
                        )
                    )
                }

                // Explicit Fallback Switch (Clearly labelled as requested in PART 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ডিভাইস লোকাল TTS ব্যাকআপ (ঐচ্ছিক)",
                        color = Color(0xFFAAA095),
                        fontSize = 11.sp
                    )
                    Switch(
                        checked = isFallbackTtsEnabled,
                        onCheckedChange = { viewModel.voiceService.setFallbackEnabled(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = HeritageGold,
                            checkedTrackColor = RoyalBurgundy
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Background Music and Bengali Captions Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Music Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1622)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382F3E))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "মিউজিক",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isMusicEnabled,
                            onCheckedChange = { viewModel.aiIsMusicEnabled.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = HeritageGold)
                        )
                    }
                    Text(
                        text = musicTrack.titleBn,
                        color = HeritageGold,
                        fontSize = 10.sp
                    )
                }
            }

            // Captions Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1622)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382F3E))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "বাংলা ক্যাপশন",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isCaptionsEnabled,
                            onCheckedChange = { viewModel.aiIsCaptionsEnabled.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = HeritageGold)
                        )
                    }
                    Text(
                        text = if (isCaptionsEnabled) "অন-স্ক্রিন সাবটাইটেল সক্রিয়" else "ক্যাপশন বন্ধ",
                        color = if (isCaptionsEnabled) PeacockEmerald else Color(0xFF9E9285),
                        fontSize = 10.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Official Brand Logo status
        Surface(
            color = Color(0xFF1C1824),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3C3345)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.BrandingWatermark,
                        contentDescription = "Logo",
                        tint = HeritageGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "অফিসিয়াল লোগো: ${brandProfile.businessNameBn}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পজিশন: ${brandProfile.watermarkPosition.titleBn} • MP4-তে এম্বেড হবে",
                            color = Color(0xFFC7BBAE),
                            fontSize = 10.sp
                        )
                    }
                }
                Switch(
                    checked = brandProfile.showLogo,
                    onCheckedChange = { viewModel.toggleShowLogo(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = HeritageGold)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Workflow / Export Progress Banner
        if (isWorkflowRunning || isExporting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D2C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = HeritageGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isWorkflowRunning) "AI ভিডিও তৈরি হচ্ছে..." else "MP4 এক্সপোর্ট হচ্ছে ($exportPercent%)...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isWorkflowRunning) workflowStatus else exportStatus,
                                color = Color(0xFFD4C8B8),
                                fontSize = 11.sp
                            )
                        }
                    }
                    if (isExporting) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { exportPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = HeritageGold,
                            trackColor = Color(0xFF42374A)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        if (exportError != null) {
            Surface(
                color = Color(0xFF381418),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = exportError ?: "",
                    color = Color(0xFFFFB4AB),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Two Action Buttons:
        // 1. "স্ক্রিপ্ট তৈরি ও সম্পাদনা" (Generate Script and Review/Edit scenes)
        // 2. "সরাসরি সম্পূর্ণ MP4 ভিডিও তৈরি করুন" (One-Click end-to-end video creation)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.generateAiVideo() },
                enabled = !isWorkflowRunning && !isGeneratingScript && !isExporting,
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("btn_generate_script_edit"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
            ) {
                Icon(
                    imageVector = Icons.Default.EditNote,
                    contentDescription = "Script",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "স্ক্রিপ্ট সম্পাদনা",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.generateCompleteAiVideoWorkflow() },
                enabled = !isWorkflowRunning && !isGeneratingScript && !isExporting,
                modifier = Modifier
                    .weight(1.3f)
                    .height(54.dp)
                    .testTag("btn_generate_complete_video"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeritageGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = "Complete Video",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সম্পূর্ণ MP4 তৈরি",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun studioTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF1E1A22),
    unfocusedContainerColor = Color(0xFF1A161E),
    focusedBorderColor = HeritageGold,
    unfocusedBorderColor = Color(0xFF3B3242),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color(0xFFE8E0D5),
    focusedPlaceholderColor = Color(0xFF8A7F75),
    unfocusedPlaceholderColor = Color(0xFF8A7F75)
)
