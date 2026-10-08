package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.model.ExportedVideo
import com.example.ui.StudioViewModel
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyVideosScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.exportedVideos.collectAsState()
    val context = LocalContext.current

    var videoToRename by remember { mutableStateOf<ExportedVideo?>(null) }
    var renameInput by remember { mutableStateOf("") }

    var videoToPlay by remember { mutableStateOf<ExportedVideo?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .testTag("my_videos_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E1822)
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(RoyalBurgundy, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Videos",
                        tint = HeritageGold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "আমার ভিডিও লাইব্রেরি",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ডিভাইসে সংরক্ষিত ভিডিও (${videos.size}টি MP4 প্রস্তুত)",
                        fontSize = 12.sp,
                        color = Color(0xFFD4C8B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Empty",
                        tint = Color(0xFF5D5164),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "এখনও কোনো ভিডিও তৈরি করা হয়নি",
                        color = Color(0xFFC7BBAE),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("video_card_${video.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1720)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B3144))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Thumbnail with play trigger
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black)
                                        .clickable { videoToPlay = video },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = video.thumbnailResId ?: R.drawable.scene_thali_delight),
                                        contentDescription = "Thumb",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.Black.copy(alpha = 0.65f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = HeritageGold,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = video.titleBn,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = RoyalBurgundy,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = video.resolutionLabel,
                                                color = HeritageGold,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${video.durationSeconds} সে. • ${formatFileSize(video.fileSizeBytes)}",
                                            color = Color(0xFFC7BBAE),
                                            fontSize = 11.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = formatTimestamp(video.timestamp),
                                        color = Color(0xFF8E8278),
                                        fontSize = 10.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: Preview, Rename, Share, Delete (Section K)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { videoToPlay = video },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Preview",
                                        tint = HeritageGold
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        videoToRename = video
                                        renameInput = video.titleBn
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename",
                                        tint = Color(0xFFD4C8B8)
                                    )
                                }

                                IconButton(
                                    onClick = { shareVideo(context, video) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color(0xFF64B5F6)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.deleteVideo(video.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFE57373)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Rename Dialog
    if (videoToRename != null) {
        AlertDialog(
            onDismissRequest = { videoToRename = null },
            title = { Text(text = "ভিডিওর নাম পরিবর্তন", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    label = { Text("নতুন নাম") },
                    colors = studioTextFieldColors(),
                    shape = RoundedCornerShape(10.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            viewModel.renameVideo(videoToRename!!.id, renameInput)
                        }
                        videoToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToRename = null }) {
                    Text("বাতিল", color = Color.White)
                }
            },
            containerColor = Color(0xFF241D29)
        )
    }

    // Playback Preview Dialog
    if (videoToPlay != null) {
        val playFile = File(videoToPlay!!.filePath)
        AlertDialog(
            onDismissRequest = { videoToPlay = null },
            title = { Text(text = videoToPlay!!.titleBn, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (playFile.exists() && playFile.length() > 0) {
                        com.example.ui.components.RealMp4Player(
                            videoFile = playFile,
                            autoPlay = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = videoToPlay!!.thumbnailResId ?: R.drawable.scene_thali_delight),
                                contentDescription = "Playback",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "ফাইল সাইজ: ${formatFileSize(videoToPlay!!.fileSizeBytes)} • রেজোলিউশন: ${videoToPlay!!.resolutionLabel}",
                        color = Color(0xFFAFA296),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        shareVideo(context, videoToPlay!!)
                        videoToPlay = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("হোয়াটসঅ্যাপ / সোশ্যাল মিডিয়ায় শেয়ার")
                }
            },
            dismissButton = {
                TextButton(onClick = { videoToPlay = null }) {
                    Text("বন্ধ করুন", color = Color.White)
                }
            },
            containerColor = Color(0xFF221A26)
        )
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format("%.1f MB", mb)
    } else {
        String.format("%.0f KB", kb)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.forLanguageTag("bn-IN"))
    return sdf.format(Date(timestamp))
}

private fun shareVideo(context: Context, video: ExportedVideo) {
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
        val chooser = Intent.createChooser(sendIntent, "ভিডিও শেয়ার করুন (Facebook / WhatsApp / Instagram)")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        // Fallback
    }
}
