package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BrandingWatermark
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.model.ExportedVideo
import com.example.ui.StudioScreen
import com.example.ui.StudioViewModel
import com.example.ui.UserMediaItem
import com.example.ui.components.RealMp4Player
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.PeacockEmerald
import com.example.ui.theme.RoyalBurgundy
import java.io.File

@Composable
fun UserMediaScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaList by viewModel.userMediaList.collectAsState()
    val brandProfile by viewModel.brandProfile.collectAsState()
    val targetDuration by viewModel.userMediaTargetDuration.collectAsState()
    val previewItem by viewModel.previewMediaItem.collectAsState()

    val isExporting by viewModel.isExporting.collectAsState()
    val exportPercent by viewModel.exportProgressPercent.collectAsState()
    val exportStatus by viewModel.exportStatusText.collectAsState()
    val exportError by viewModel.exportError.collectAsState()
    val lastExportedVideo by viewModel.lastExportedVideo.collectAsState()

    var showExportSuccessDialog by remember { mutableStateOf(false) }

    // Android Photo Picker (zero-permission, fully Play-policy compliant)
    // 1. Mixed Images and Videos picker
    val mixedPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val mime = try { context.contentResolver.getType(uri) } catch (e: Exception) { null }
            val isVideo = mime?.startsWith("video") == true
            viewModel.addUserMediaWithMetadata(context, uri, isVideo)
        }
    }

    // 2. Images-only picker
    val imageOnlyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addUserMediaWithMetadata(context, uri, isVideo = false)
        }
    }

    // 3. Videos-only picker
    val videoOnlyPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            viewModel.addUserMediaWithMetadata(context, uri, isVideo = true)
        }
    }

    val totalDurationSeconds = mediaList.sumOf { it.durationSeconds }.coerceAtLeast(30)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .testTag("user_media_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Header Card with Media Pickers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1822)),
            border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "নিজের ছবি ও ভিডিও দিয়ে MP4 তৈরি",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "গ্যালারি থেকে ছবি ও ভিডিও ক্লিপ যোগ করে ৯:১৬ ভার্টিক্যাল রিল/শর্টস তৈরি করুন",
                    fontSize = 12.sp,
                    color = Color(0xFFD4C8B8)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Three Media Picker Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Mixed
                    Button(
                        onClick = {
                            mixedPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier
                            .weight(1.1f)
                            .testTag("btn_pick_mixed_media"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HeritageGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Add Mixed",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ছবি ও ভিডিও", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Images only
                    OutlinedButton(
                        onClick = {
                            imageOnlyPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(0.95f)
                            .testTag("btn_pick_images"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Add Images",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ছবি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Videos only
                    OutlinedButton(
                        onClick = {
                            videoOnlyPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        modifier = Modifier
                            .weight(0.95f)
                            .testTag("btn_pick_videos"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6))
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoFile,
                            contentDescription = "Add Videos",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ভিডিও", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Target Duration Selector: 30s, 45s, 60s, 90s
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Duration",
                    tint = HeritageGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "টার্গেট সময়কাল:",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(30, 45, 60, 90).forEach { sec ->
                    val isSelected = targetDuration == sec
                    Surface(
                        color = if (isSelected) HeritageGold else Color(0xFF241D29),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) HeritageGold else Color(0xFF43374D)
                        ),
                        modifier = Modifier
                            .clickable { viewModel.setTargetDurationSeconds(sec) }
                            .testTag("duration_chip_$sec")
                    ) {
                        Text(
                            text = "$sec সে.",
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Official Logo Switch Row
        Surface(
            color = Color(0xFF1B1621),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF382F3E)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                            text = "ভিডিওতে লোগো দেখান",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${brandProfile.businessNameBn} (${brandProfile.watermarkPosition.titleBn})",
                            color = Color(0xFFC7BBAE),
                            fontSize = 10.sp
                        )
                    }
                }

                Switch(
                    checked = brandProfile.showLogo,
                    onCheckedChange = { viewModel.toggleShowLogo(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HeritageGold,
                        checkedTrackColor = RoyalBurgundy
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timeline header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "টাইমলাইন দৃশ্য (${mediaList.size}টি দৃশ্য)",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "মোট সময়কাল: $totalDurationSeconds সে. (মিনিমাম ৩০ সে.)",
                color = HeritageGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Media items timeline list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(mediaList) { index, item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("media_item_card_$index"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1822)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF352B3C))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Thumbnail with tap to preview
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable { viewModel.setPreviewItem(item) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.uri != Uri.EMPTY) {
                                AsyncImage(
                                    model = item.uri,
                                    contentDescription = "Media thumb",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = item.drawableResId ?: R.drawable.scene_thali_delight),
                                    contentDescription = "Media thumb",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            if (item.isVideo) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Video",
                                        tint = HeritageGold,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${index + 1}. ${item.titleBn}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = if (item.isVideo) Color(0xFF1E3A5F) else RoyalBurgundy,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (item.isVideo) "ভিডিও" else "ছবি",
                                        color = if (item.isVideo) Color(0xFF90CAF9) else HeritageGold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))

                                // Duration adjustments (+ / -)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.durationSeconds} সে.",
                                        color = Color(0xFFD4C8B8),
                                        fontSize = 11.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        color = Color(0xFF2C2333),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable {
                                            if (item.durationSeconds > 2) {
                                                viewModel.updateUserMediaDuration(index, item.durationSeconds - 1)
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "-",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Surface(
                                        color = Color(0xFF2C2333),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable {
                                            viewModel.updateUserMediaDuration(index, item.durationSeconds + 1)
                                        }
                                    ) {
                                        Text(
                                            text = "+",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Reorder, Preview, and Delete
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.setPreviewItem(item) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "Preview Item",
                                    tint = HeritageGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            if (index > 0) {
                                IconButton(
                                    onClick = { viewModel.moveUserMedia(index, index - 1) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Move Up",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            if (index < mediaList.size - 1) {
                                IconButton(
                                    onClick = { viewModel.moveUserMedia(index, index + 1) },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Move Down",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeUserMedia(index) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFE57373),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Progress bar during export
        if (isExporting) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF261D2C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            color = HeritageGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "MP4 ভিডিও এনকোড হচ্ছে ($exportPercent%)...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = exportStatus,
                                color = Color(0xFFD4C8B8),
                                fontSize = 11.sp
                            )
                        }
                    }
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
            Spacer(modifier = Modifier.height(10.dp))
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
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Two Primary Action Buttons:
        // 1. "রিয়েল MP4 তৈরি করুন" (Direct real MP4 export)
        // 2. "স্ক্রিপ্ট ও ভয়েসওভার সহ সাজান" (Take to Script & Voiceover)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.createProjectFromUserMedia() },
                enabled = mediaList.isNotEmpty() && !isExporting,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("btn_proceed_to_script"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
            ) {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Script",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "স্ক্রিপ্ট ও প্রিভিউ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    viewModel.exportUserMediaDirectly()
                    showExportSuccessDialog = true
                },
                enabled = mediaList.isNotEmpty() && !isExporting,
                modifier = Modifier
                    .weight(1.3f)
                    .height(52.dp)
                    .testTag("btn_create_real_mp4"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HeritageGold,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    imageVector = Icons.Default.MovieCreation,
                    contentDescription = "Create MP4",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "রিয়েল MP4 তৈরি করুন",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Media Item Preview Dialog (Single Item Image or Video)
    if (previewItem != null) {
        val item = previewItem!!
        AlertDialog(
            onDismissRequest = { viewModel.setPreviewItem(null) },
            title = {
                Text(
                    text = item.titleBn,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(9f / 16f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        if (item.isVideo && item.uri != Uri.EMPTY) {
                            AndroidView(
                                factory = { ctx ->
                                    VideoView(ctx).apply {
                                        setVideoURI(item.uri)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            mp.start()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (item.uri != Uri.EMPTY) {
                            AsyncImage(
                                model = item.uri,
                                contentDescription = "Item Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Image(
                                painter = painterResource(id = item.drawableResId ?: R.drawable.scene_thali_delight),
                                contentDescription = "Item Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (item.isVideo) "ভিডিও দৃশ্য (${item.durationSeconds} সেকেন্ড)" else "ফটোগ্রাফিক দৃশ্য (${item.durationSeconds} সেকেন্ড)",
                        color = Color(0xFFC7BBAE),
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setPreviewItem(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                ) {
                    Text("ঠিক আছে")
                }
            },
            containerColor = Color(0xFF221A26)
        )
    }

    // Exported Video Player Dialog (Actual MP4 Player with Play, Pause, Seek, Mute, Volume)
    if (lastExportedVideo != null && showExportSuccessDialog && !isExporting) {
        val exported = lastExportedVideo!!
        val file = File(exported.filePath)

        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = PeacockEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MP4 সফলভাবে তৈরি হয়েছে!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Actual REAL MP4 Player View
                    RealMp4Player(
                        videoFile = file,
                        brandProfile = brandProfile,
                        autoPlay = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${exported.resolutionLabel} • ${exported.durationSeconds} সে. • সংরক্ষিত হয়েছে",
                        color = HeritageGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            shareVideoFile(context, exported)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64B5F6)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64B5F6))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার")
                    }

                    Button(
                        onClick = {
                            showExportSuccessDialog = false
                            viewModel.setScreen(StudioScreen.MY_VIDEOS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                    ) {
                        Text("আমার ভিডিওতে দেখুন")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportSuccessDialog = false }) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1724)
        )
    }
}

private fun shareVideoFile(context: Context, video: ExportedVideo) {
    val file = File(video.filePath)
    if (!file.exists()) return

    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_SUBJECT, video.titleBn)
            putExtra(Intent.EXTRA_TEXT, "অন্বেষার রসনা বিলাস - অফিসিয়াল ভিডিও: ${video.titleBn}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "ভিডিও শেয়ার করুন (WhatsApp / Facebook / Instagram)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}
