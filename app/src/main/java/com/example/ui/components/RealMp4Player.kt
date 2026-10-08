package com.example.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BrandProfile
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File

@Composable
fun RealMp4Player(
    videoFile: File,
    modifier: Modifier = Modifier,
    brandProfile: BrandProfile? = null,
    autoPlay: Boolean = true
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isPrepared by remember { mutableStateOf(false) }
    var currentPositionMs by remember { mutableIntStateOf(0) }
    var durationMs by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var volumeLevel by remember { mutableFloatStateOf(0.9f) }
    var mediaPlayerRef by remember { mutableStateOf<MediaPlayer?>(null) }
    var isError by remember { mutableStateOf(false) }

    // Position tracking loop
    LaunchedEffect(isPlaying, isPrepared) {
        while (isActive && isPrepared && isPlaying) {
            delay(150)
            try {
                mediaPlayerRef?.let { mp ->
                    if (mp.isPlaying) {
                        currentPositionMs = mp.currentPosition
                    }
                }
            } catch (e: Exception) {
                // Ignore transient state
            }
        }
    }

    // Volume sync
    LaunchedEffect(isMuted, volumeLevel, mediaPlayerRef) {
        mediaPlayerRef?.let { mp ->
            try {
                val vol = if (isMuted) 0f else volumeLevel
                mp.setVolume(vol, vol)
            } catch (e: Exception) {}
        }
    }

    DisposableEffect(videoFile.absolutePath) {
        onDispose {
            try {
                mediaPlayerRef?.stop()
                mediaPlayerRef?.release()
                mediaPlayerRef = null
            } catch (e: Exception) {}
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("real_mp4_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161418)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // 9:16 Vertical Video Frame Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF382E3A), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        mediaPlayerRef?.let { mp ->
                            try {
                                if (mp.isPlaying) {
                                    mp.pause()
                                    isPlaying = false
                                } else {
                                    mp.start()
                                    isPlaying = true
                                }
                            } catch (e: Exception) {}
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (!videoFile.exists()) {
                    Text(
                        text = "ভিডিও ফাইলটি পাওয়া যায়নি",
                        color = Color(0xFFFFB4AB),
                        fontSize = 14.sp
                    )
                } else if (isError) {
                    Text(
                        text = "ভিডিও প্লেব্যাক ব্যর্থ হয়েছে",
                        color = Color(0xFFFFB4AB),
                        fontSize = 14.sp
                    )
                } else {
                    // Actual TextureView + MediaPlayer for MP4 playback
                    AndroidView(
                        factory = { ctx ->
                            val frameLayout = FrameLayout(ctx)
                            val textureView = TextureView(ctx)
                            textureView.layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )

                            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                                override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                                    try {
                                        val surface = Surface(st)
                                        val mp = MediaPlayer().apply {
                                            setSurface(surface)
                                            setDataSource(videoFile.absolutePath)
                                            isLooping = true
                                            setOnPreparedListener { preparedMp ->
                                                isPrepared = true
                                                durationMs = preparedMp.duration
                                                val vol = if (isMuted) 0f else volumeLevel
                                                preparedMp.setVolume(vol, vol)
                                                if (autoPlay) {
                                                    preparedMp.start()
                                                    isPlaying = true
                                                }
                                            }
                                            setOnErrorListener { _, _, _ ->
                                                isError = true
                                                false
                                            }
                                            prepareAsync()
                                        }
                                        mediaPlayerRef = mp
                                    } catch (e: Exception) {
                                        Log.e("RealMp4Player", "TextureView prepare error: ${e.message}")
                                        isError = true
                                    }
                                }

                                override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {}

                                override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                                    try {
                                        mediaPlayerRef?.stop()
                                        mediaPlayerRef?.release()
                                        mediaPlayerRef = null
                                    } catch (e: Exception) {}
                                    return true
                                }

                                override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
                            }

                            frameLayout.addView(textureView)
                            frameLayout
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Loading Spinner when preparing
                if (!isPrepared && !isError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = HeritageGold, modifier = Modifier.size(36.dp))
                    }
                }

                // Top Badge: Actual MP4 Indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "REAL MP4 • 9:16",
                            color = HeritageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = RoyalBurgundy.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${formatSeconds(durationMs / 1000)} সে.",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Center Play Button when paused
                if (!isPlaying && isPrepared) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(64.dp)
                            .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                            .border(2.dp, HeritageGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = HeritageGold,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Seek Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSeconds(currentPositionMs / 1000),
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = currentPositionMs.toFloat(),
                    onValueChange = { newPos ->
                        currentPositionMs = newPos.toInt()
                        mediaPlayerRef?.let { mp ->
                            try {
                                mp.seekTo(newPos.toInt())
                            } catch (e: Exception) {}
                        }
                    },
                    valueRange = 0f..(durationMs.toFloat().coerceAtLeast(1f)),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("mp4_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = HeritageGold,
                        activeTrackColor = HeritageGold,
                        inactiveTrackColor = Color(0xFF382E3A)
                    )
                )

                Text(
                    text = formatSeconds(durationMs / 1000),
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player Controls: Play, Pause, Seek 5s, Mute, Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            mediaPlayerRef?.let { mp ->
                                try {
                                    val target = (mp.currentPosition - 5000).coerceAtLeast(0)
                                    mp.seekTo(target)
                                    currentPositionMs = target
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.testTag("mp4_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 5s",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = {
                            mediaPlayerRef?.let { mp ->
                                try {
                                    if (mp.isPlaying) {
                                        mp.pause()
                                        isPlaying = false
                                    } else {
                                        mp.start()
                                        isPlaying = true
                                    }
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .background(HeritageGold, CircleShape)
                            .testTag("mp4_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black
                        )
                    }

                    IconButton(
                        onClick = {
                            mediaPlayerRef?.let { mp ->
                                try {
                                    val target = (mp.currentPosition + 5000).coerceAtMost(durationMs)
                                    mp.seekTo(target)
                                    currentPositionMs = target
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.testTag("mp4_forward_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 5s",
                            tint = Color.White
                        )
                    }
                }

                // Volume and Mute Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            mediaPlayerRef?.let { mp ->
                                try {
                                    val vol = if (isMuted) 0f else volumeLevel
                                    mp.setVolume(vol, vol)
                                } catch (e: Exception) {}
                            }
                        },
                        modifier = Modifier.testTag("mp4_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) Color.Red else HeritageGold
                        )
                    }

                    Slider(
                        value = if (isMuted) 0f else volumeLevel,
                        onValueChange = { newVol ->
                            volumeLevel = newVol
                            isMuted = false
                            mediaPlayerRef?.let { mp ->
                                try {
                                    mp.setVolume(newVol, newVol)
                                } catch (e: Exception) {}
                            }
                        },
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("mp4_volume_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = HeritageGold,
                            activeTrackColor = HeritageGold,
                            inactiveTrackColor = Color(0xFF382E3A)
                        )
                    )
                }
            }
        }
    }
}

private fun formatSeconds(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
