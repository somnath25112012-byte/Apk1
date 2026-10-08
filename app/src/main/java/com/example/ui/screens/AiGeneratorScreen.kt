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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
    val musicTrack by viewModel.aiMusicTrack.collectAsState()
    val aspectRatio by viewModel.aiAspectRatio.collectAsState()
    val instructions by viewModel.aiInstructions.collectAsState()
    val isGenerating by viewModel.isGeneratingScript.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("ai_generator_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Screen Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1822)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            text = "AI দিয়ে ভিডিও তৈরি করুন",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "কোনো ছবি ছাড়া শুধু টেক্সট প্রম্পট থেকে পূর্ণাঙ্গ ভিডিও",
                            fontSize = 12.sp,
                            color = Color(0xFFD4C8B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // API Transparency Banner (As required in Section C)
                Surface(
                    color = Color(0xFF28201A),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6B4E2A))
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = HeritageGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ইঞ্জিন: ${viewModel.aiScriptService.providerName} • অফলাইন প্রস্তুত",
                            fontSize = 11.sp,
                            color = HeritageGoldLight
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Fields
        Text(
            text = "ভিডিওর বিষয় (Topic)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = topic,
            onValueChange = { viewModel.aiTopic.value = it },
            placeholder = { Text("যেমন: স্পেশাল মটন কষা ও খাঁটি বাঙালি ভোজ") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_ai_topic"),
            shape = RoundedCornerShape(12.dp),
            colors = studioTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "ব্যবসা / সেবা / ব্র্যান্ডের নাম",
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

        // Purpose Selector Chips
        Text(
            text = "ভিডিওর উদ্দেশ্য (Purpose)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val purposeOptions = listOf(
            "ফেসবুক ও রিল প্রচার",
            "ইউটিউব শর্টস",
            "হোয়াটসঅ্যাপ স্ট্যাটাস ও অফার",
            "উৎসব ও পুজো স্পেশাল",
            "রেস্তোরাঁ ও খাদ্য মেনু প্রচার"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            purposeOptions.forEach { opt ->
                val isSel = purpose == opt
                Surface(
                    color = if (isSel) HeritageGold else Color(0xFF221C26),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier.clickable { viewModel.aiPurpose.value = opt }
                ) {
                    Text(
                        text = opt,
                        color = if (isSel) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duration Options (30s, 45s, 60s, 90s, Custom - Minimum 30s as specified)
        Text(
            text = "সময়কাল (Duration - ন্যূনতম ৩০ সেকেন্ড)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val durationOptions = listOf(30, 45, 60, 90)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            durationOptions.forEach { sec ->
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

        // Aspect Ratio Options (Reels 9:16, Square 1:1, Landscape 16:9)
        Text(
            text = "ভিডিওর আকার (Aspect Ratio)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AspectRatioType.values().forEach { ar ->
                val isSel = aspectRatio == ar
                Surface(
                    color = if (isSel) RoyalBurgundy else Color(0xFF221C26),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.aiAspectRatio.value = ar }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = ar.label,
                            color = HeritageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ar.titleBn.split(" ")[0],
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Voice Style Selection
        Text(
            text = "ভয়েসওভার ধরণ (Default: বাঙালি নারী)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val topVoices = listOf(
            VoiceStyle.BENGALI_FEMALE_NATURAL,
            VoiceStyle.BENGALI_FEMALE_PROMOTIONAL,
            VoiceStyle.BENGALI_MALE_NATURAL,
            VoiceStyle.BENGALI_MALE_PROMOTIONAL
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            topVoices.forEach { vs ->
                val isSel = voiceStyle == vs
                Surface(
                    color = if (isSel) PeacockEmerald else Color(0xFF221C26),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier.clickable { viewModel.aiVoiceStyle.value = vs }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice",
                            tint = if (isSel) Color.White else HeritageGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = vs.titleBn,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Music
        Text(
            text = "ব্যাকগ্রাউন্ড মিউজিক",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        val musics = listOf(
            MusicTrack.BENGALI_FLUTE,
            MusicTrack.RABINDRA_MELODY,
            MusicTrack.BAUL_FUSION,
            MusicTrack.MODERN_COMMERCIAL
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            musics.forEach { track ->
                val isSel = musicTrack == track
                Surface(
                    color = if (isSel) Color(0xFF5E2B37) else Color(0xFF221C26),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSel) HeritageGold else Color(0xFF42374A)
                    ),
                    modifier = Modifier.clickable { viewModel.aiMusicTrack.value = track }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Music",
                            tint = HeritageGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = track.titleBn,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Additional Instructions
        Text(
            text = "অতিরিক্ত নির্দেশাবলী (ঐচ্ছিক)",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = instructions,
            onValueChange = { viewModel.aiInstructions.value = it },
            placeholder = { Text("বিশেষ কোনো অফার বা সংলাপ যোগ করতে চান?") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = studioTextFieldColors(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Primary Generate Button
        Button(
            onClick = { viewModel.generateAiVideo() },
            enabled = !isGenerating,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("btn_generate_ai_video"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HeritageGold,
                contentColor = Color.Black
            )
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    color = Color.Black,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "বাংলা স্ক্রিপ্ট ও দৃশ্য তৈরি হচ্ছে...",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Generate",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "AI দিয়ে ভিডিও তৈরি করুন",
                    fontSize = 16.sp,
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
