package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BrandProfile
import com.example.model.ScenePlan
import com.example.model.VideoProject
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy

@Composable
fun VideoPreviewPlayer(
    project: VideoProject,
    brandProfile: BrandProfile,
    isPlaying: Boolean,
    playbackPositionSeconds: Float,
    isMuted: Boolean,
    volumeLevel: Float,
    onTogglePlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    activeScene: ScenePlan?,
    modifier: Modifier = Modifier
) {
    val totalSeconds = project.totalCalculatedDurationSeconds.toFloat()
    val currentScene = activeScene ?: project.scenes.firstOrNull()

    // Smooth Ken Burns zoom effect based on progress within current scene
    val sceneProgress = if (currentScene != null && currentScene.durationSeconds > 0) {
        val sceneStart = project.scenes.takeWhile { it.id != currentScene.id }.sumOf { it.durationSeconds }
        ((playbackPositionSeconds - sceneStart) / currentScene.durationSeconds).coerceIn(0f, 1f)
    } else 0f

    val zoomScale by animateFloatAsState(
        targetValue = 1.0f + (sceneProgress * 0.08f),
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "KenBurnsZoom"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_preview_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF161418)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Video Screen Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(project.aspectRatio.ratio)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .border(1.dp, Color(0xFF382E3A), RoundedCornerShape(16.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onTogglePlayPause() },
                contentAlignment = Alignment.Center
            ) {
                // Background visual asset
                if (currentScene?.mediaUri != null) {
                    AsyncImage(
                        model = currentScene.mediaUri,
                        contentDescription = "User Scene Media",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(zoomScale)
                    )
                } else {
                    val drawableRes = currentScene?.drawableResId ?: R.drawable.scene_thali_delight
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = "Scene Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .scale(zoomScale)
                    )
                }

                // Vignette gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Top Bar Badges: 1080p Full HD & Scene Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "1080p • ${project.aspectRatio.label}",
                            color = HeritageGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (currentScene != null) {
                        Surface(
                            color = RoyalBurgundy.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "সিন ${currentScene.order}/${project.scenes.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // CRITICAL: Official Brand Logo Watermark Overlay
                BrandWatermarkView(brandProfile = brandProfile)

                // Center Play/Pause button on pause
                if (!isPlaying) {
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

                // Bottom Bengali Caption & CTA Banner
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentScene != null) {
                        Surface(
                            color = Color(0xD91F1116),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentScene.onScreenCaptionBn,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${brandProfile.businessNameBn} • ${brandProfile.phone}",
                                    color = HeritageGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Seek Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatSeconds(playbackPositionSeconds.toInt()),
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = playbackPositionSeconds,
                    onValueChange = onSeek,
                    valueRange = 0f..totalSeconds.coerceAtLeast(1f),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .testTag("video_seek_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = HeritageGold,
                        activeTrackColor = HeritageGold,
                        inactiveTrackColor = Color(0xFF382E3A)
                    )
                )

                Text(
                    text = formatSeconds(totalSeconds.toInt()),
                    color = Color(0xFFD4C8B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Player Controls Row (Play/Pause, Rewind, FastForward, Mute, Volume)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val target = (playbackPositionSeconds - 5f).coerceAtLeast(0f)
                            onSeek(target)
                        },
                        modifier = Modifier.testTag("preview_rewind_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 5s",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(46.dp)
                            .background(HeritageGold, CircleShape)
                            .testTag("preview_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black
                        )
                    }

                    IconButton(
                        onClick = {
                            val target = (playbackPositionSeconds + 5f).coerceAtMost(totalSeconds)
                            onSeek(target)
                        },
                        modifier = Modifier.testTag("preview_forward_button")
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
                        onClick = onToggleMute,
                        modifier = Modifier.testTag("preview_mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = if (isMuted) Color.Red else HeritageGold
                        )
                    }

                    Slider(
                        value = if (isMuted) 0f else volumeLevel,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .width(80.dp)
                            .testTag("preview_volume_slider"),
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
