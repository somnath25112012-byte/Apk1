package com.example.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaCodecList
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.BrandProfile
import com.example.model.ExportedVideo
import com.example.model.VideoProject
import com.example.model.VideoTransition
import com.example.model.WatermarkPosition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.coroutines.coroutineContext

sealed class ExportProgress {
    data class Progress(val percentage: Int, val currentStepBn: String) : ExportProgress()
    data class Success(val video: ExportedVideo) : ExportProgress()
    data class Error(val messageBn: String) : ExportProgress()
}

interface MediaCompositionService {
    fun exportProjectToMp4(
        context: Context,
        project: VideoProject,
        brandProfile: BrandProfile,
        captionConfig: CaptionStyleConfig
    ): Flow<ExportProgress>
}

class DefaultMediaCompositionService : MediaCompositionService {
    private val TAG = "MediaCompositionService"

    override fun exportProjectToMp4(
        context: Context,
        project: VideoProject,
        brandProfile: BrandProfile,
        captionConfig: CaptionStyleConfig
    ): Flow<ExportProgress> = flow {
        emit(ExportProgress.Progress(2, "স্টোরেজ ও মিডিয়া ফাইল পরীক্ষা করা হচ্ছে..."))

        // Storage check: ensure at least 25 MB available
        val freeBytes = context.filesDir.usableSpace
        if (freeBytes < 25 * 1024 * 1024) {
            emit(ExportProgress.Error("ডিভাইসে পর্যাপ্ত মেমোরি খালি নেই (কমপক্ষে ২৫ মেগাবাইট প্রয়োজন)।"))
            return@flow
        }

        val outputDir = File(context.filesDir, "exported_videos").apply { mkdirs() }
        val fileName = "Anweshar_${System.currentTimeMillis()}.mp4"
        val outputFile = File(outputDir, fileName)

        // Select resolution: prefer 1080x1920 (Reels/Shorts 9:16), fallback to 720x1280
        val (targetWidth, targetHeight) = selectOptimalVideoResolution()
        val frameRate = 25
        val bitRate = if (targetWidth >= 1080) 4_500_000 else 2_500_000

        var muxer: MediaMuxer? = null
        var encoder: MediaCodec? = null
        var inputSurface: android.view.Surface? = null
        var isCompletedSuccessfully = false

        try {
            emit(ExportProgress.Progress(8, "ভিডিও এনকোডার কনফিগার করা হচ্ছে (${targetWidth}×${targetHeight})..."))

            val logoBitmap = if (brandProfile.showLogo) {
                loadBrandLogoBitmap(context, brandProfile)
            } else null

            val bengaliTypeface = try {
                ResourcesCompat.getFont(context, R.font.noto_serif_bengali) ?: Typeface.DEFAULT_BOLD
            } catch (e: Exception) {
                Typeface.DEFAULT_BOLD
            }

            // Create H.264 / AVC video format
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, targetWidth, targetHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = try {
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
                    configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }
            } catch (ex: Exception) {
                Log.w(TAG, "1080x1920 encoder configure failed, falling back to 720x1280", ex)
                val fallbackFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280).apply {
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                    setInteger(MediaFormat.KEY_BIT_RATE, 2_500_000)
                    setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                }
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).apply {
                    configure(fallbackFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
                }
            }

            inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            val scenes = project.scenes
            if (scenes.isEmpty()) {
                emit(ExportProgress.Error("ভিডিও তৈরির জন্য কোনো দৃশ্য বা মিডিয়া নির্বাচন করা হয়নি।"))
                return@flow
            }

            val totalScenes = scenes.size
            val totalSeconds = project.totalCalculatedDurationSeconds.coerceAtLeast(30)
            val totalFrames = totalSeconds * frameRate

            // Calculate frames per scene to match target duration
            val sceneFrameCounts = IntArray(totalScenes)
            var accumulatedFrames = 0
            for (i in 0 until totalScenes) {
                val sceneSec = scenes[i].durationSeconds.coerceAtLeast(2)
                val frames = (sceneSec.toFloat() / scenes.sumOf { it.durationSeconds }.coerceAtLeast(1) * totalFrames).toInt()
                    .coerceAtLeast(frameRate * 2)
                sceneFrameCounts[i] = frames
                accumulatedFrames += frames
            }
            if (accumulatedFrames < totalFrames && totalScenes > 0) {
                sceneFrameCounts[totalScenes - 1] += (totalFrames - accumulatedFrames)
            }

            // Paints setup
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = true }
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = if (targetWidth >= 1080) 42f else 32f
                typeface = bengaliTypeface
                textAlign = Paint.Align.CENTER
                setShadowLayer(8f, 2f, 2f, Color.BLACK)
            }
            val brandBannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E8C784")
                textSize = if (targetWidth >= 1080) 34f else 26f
                typeface = bengaliTypeface
                textAlign = Paint.Align.CENTER
            }
            val overlayBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#C0120C14")
            }

            emit(ExportProgress.Progress(15, "দৃশ্য ও ফ্রেম প্রসেসিং শুরু হচ্ছে..."))

            var globalFrameIndex = 0

            // Process each scene individually (DO NOT load all media simultaneously)
            for (sIndex in scenes.indices) {
                if (!coroutineContext.isActive) {
                    emit(ExportProgress.Error("ব্যবহারকারী দ্বারা এক্সপোর্ট বাতিল করা হয়েছে।"))
                    return@flow
                }

                val scene = scenes[sIndex]
                val framesForScene = sceneFrameCounts[sIndex]
                val isVideo = scene.isVideoMedia && !scene.mediaUri.isNullOrBlank()

                var videoRetriever: MediaMetadataRetriever? = null
                var videoDurationUs: Long = 0L
                var sceneImageBitmap: Bitmap? = null

                try {
                    if (isVideo) {
                        try {
                            videoRetriever = MediaMetadataRetriever().apply {
                                val uri = Uri.parse(scene.mediaUri)
                                setDataSource(context, uri)
                                val durStr = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                videoDurationUs = (durStr?.toLongOrNull() ?: 5000L) * 1000L
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to load video retriever: ${e.message}")
                            videoRetriever = null
                        }
                    } else {
                        // Load image with downsampling
                        sceneImageBitmap = loadSampledSceneBitmap(context, scene.mediaUri, scene.drawableResId, targetWidth, targetHeight)
                    }

                    var lastCachedVideoFrame: Bitmap? = null

                    for (f in 0 until framesForScene) {
                        if (!coroutineContext.isActive) {
                            emit(ExportProgress.Error("এক্সপোর্ট বাতিল করা হয়েছে।"))
                            return@flow
                        }

                        // Progress update
                        val progressPercent = 15 + ((globalFrameIndex.toFloat() / totalFrames) * 75).toInt()
                        if (globalFrameIndex % 25 == 0) {
                            emit(ExportProgress.Progress(
                                progressPercent.coerceIn(15, 90),
                                "দৃশ্য ${sIndex + 1}/$totalScenes রেন্ডার হচ্ছে (${scene.titleBn})..."
                            ))
                        }

                        val canvas = inputSurface.lockCanvas(null)
                        try {
                            canvas.drawColor(Color.parseColor("#121014"))

                            // 1. Draw media content (video frame or image)
                            if (isVideo && videoRetriever != null) {
                                val targetTimeUs = if (framesForScene > 1) {
                                    ((f.toFloat() / framesForScene) * videoDurationUs).toLong()
                                } else 0L

                                val frameBitmap = try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                        videoRetriever.getScaledFrameAtTime(
                                            targetTimeUs,
                                            MediaMetadataRetriever.OPTION_CLOSEST,
                                            targetWidth,
                                            targetHeight
                                        )
                                    } else {
                                        videoRetriever.getFrameAtTime(targetTimeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                                    }
                                } catch (e: Exception) {
                                    null
                                }

                                if (frameBitmap != null) {
                                    lastCachedVideoFrame?.recycle()
                                    lastCachedVideoFrame = frameBitmap
                                }

                                val toDraw = lastCachedVideoFrame
                                if (toDraw != null && !toDraw.isRecycled) {
                                    drawBitmapCenterCrop(canvas, toDraw, targetWidth, targetHeight, paint)
                                } else {
                                    canvas.drawColor(Color.parseColor("#1B1218"))
                                }
                            } else if (sceneImageBitmap != null && !sceneImageBitmap.isRecycled) {
                                // Ken Burns subtle zoom effect and transitions
                                val zoomScale = when (scene.transition) {
                                    VideoTransition.CROSS_ZOOM -> 1.0f + (f.toFloat() / framesForScene) * 0.08f
                                    else -> 1.0f + (f.toFloat() / framesForScene) * 0.04f
                                }

                                canvas.save()
                                canvas.scale(zoomScale, zoomScale, targetWidth / 2f, targetHeight / 2f)

                                // Fade transition for the first 12 frames
                                if (scene.transition == VideoTransition.FADE && f < 12) {
                                    paint.alpha = ((f / 12f) * 255).toInt()
                                } else {
                                    paint.alpha = 255
                                }

                                drawBitmapCenterCrop(canvas, sceneImageBitmap, targetWidth, targetHeight, paint)
                                canvas.restore()
                            } else {
                                canvas.drawColor(Color.parseColor("#25151F"))
                            }

                            // 2. Draw Vignette & Dimmer Overlay
                            val bottomBannerHeight = if (targetHeight >= 1920) 280f else 220f
                            val bannerRect = RectF(
                                32f,
                                targetHeight - bottomBannerHeight - 80f,
                                targetWidth - 32f,
                                targetHeight - 80f
                            )
                            canvas.drawRoundRect(bannerRect, 28f, 28f, overlayBgPaint)

                            // 3. Draw Bengali on-screen captions & Narration subtitle
                            val captionText = scene.onScreenCaptionBn.ifBlank { scene.titleBn }
                            canvas.drawText(
                                captionText,
                                targetWidth / 2f,
                                targetHeight - bottomBannerHeight + 10f,
                                textPaint
                            )

                            val subText = "${brandProfile.businessNameBn} • ${brandProfile.phone}"
                            canvas.drawText(
                                subText,
                                targetWidth / 2f,
                                targetHeight - bottomBannerHeight + (if (targetHeight >= 1920) 80f else 65f),
                                brandBannerPaint
                            )

                            // 4. CRITICAL: Official Brand Logo Watermark Overlay
                            if (brandProfile.showLogo && logoBitmap != null && !logoBitmap.isRecycled) {
                                drawLogoWatermarkOnCanvas(canvas, targetWidth, targetHeight, logoBitmap, brandProfile)
                            }

                        } finally {
                            inputSurface.unlockCanvasAndPost(canvas)
                        }

                        // Drain encoded video output buffers
                        drainEncoder(encoder, muxer, bufferInfo, globalFrameIndex, frameRate) { trackIdx ->
                            videoTrackIndex = trackIdx
                            muxerStarted = true
                        }

                        globalFrameIndex++
                    }

                    lastCachedVideoFrame?.recycle()
                } finally {
                    try {
                        videoRetriever?.release()
                    } catch (ignored: Exception) {}
                    sceneImageBitmap?.recycle()
                }
            }

            emit(ExportProgress.Progress(92, "ভিডিও মেটাডাটা ও কনটেইনার সংকলন করা হচ্ছে..."))

            // Signal end of input stream to encoder
            encoder.signalEndOfInputStream()

            // Drain remaining buffers
            var eos = false
            while (!eos) {
                val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 10000)
                if (outputBufferId >= 0) {
                    val encodedData = encoder.getOutputBuffer(outputBufferId)
                    if (encodedData != null && muxerStarted && bufferInfo.size > 0) {
                        encodedData.position(bufferInfo.offset)
                        encodedData.limit(bufferInfo.offset + bufferInfo.size)
                        muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(outputBufferId, false)
                    if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        eos = true
                    }
                } else if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    break
                }
            }

            isCompletedSuccessfully = true
            emit(ExportProgress.Progress(98, "MP4 ফাইল সফলভাবে প্রস্তুত হয়েছে!"))

            val finalSize = outputFile.length()
            val exportedVideo = ExportedVideo(
                id = UUID.randomUUID().toString(),
                titleBn = project.titleBn,
                filePath = outputFile.absolutePath,
                durationSeconds = totalSeconds,
                timestamp = System.currentTimeMillis(),
                fileSizeBytes = finalSize.coerceAtLeast(1024),
                resolutionLabel = "${targetWidth}×${targetHeight} (MP4 / 9:16)",
                thumbnailResId = project.scenes.firstOrNull()?.drawableResId ?: R.drawable.scene_thali_delight,
                isWatermarked = brandProfile.showLogo
            )

            emit(ExportProgress.Progress(100, "সম্পন্ন!"))
            emit(ExportProgress.Success(exportedVideo))

        } catch (e: Exception) {
            Log.e(TAG, "Video export failed: ${e.message}", e)
            emit(ExportProgress.Error("ভিডিও তৈরিতে সমস্যা হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}"))
        } finally {
            // Clean up resources cleanly
            try {
                inputSurface?.release()
            } catch (ignored: Exception) {}
            try {
                encoder?.stop()
                encoder?.release()
            } catch (ignored: Exception) {}
            try {
                muxer?.stop()
                muxer?.release()
            } catch (ignored: Exception) {}

            // Clean up corrupted file if failed
            if (!isCompletedSuccessfully && outputFile.exists()) {
                try {
                    outputFile.delete()
                } catch (ignored: Exception) {}
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun drainEncoder(
        encoder: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        frameIndex: Int,
        frameRate: Int,
        onMuxerStarted: (Int) -> Unit
    ) {
        var trackIdx = -1
        while (true) {
            val outputBufferId = encoder.dequeueOutputBuffer(bufferInfo, 0)
            if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break
            } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                val newFormat = encoder.outputFormat
                trackIdx = muxer.addTrack(newFormat)
                muxer.start()
                onMuxerStarted(trackIdx)
            } else if (outputBufferId >= 0) {
                val encodedData = encoder.getOutputBuffer(outputBufferId)
                if (encodedData != null && bufferInfo.size > 0) {
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    bufferInfo.presentationTimeUs = (frameIndex * 1_000_000L / frameRate)
                    try {
                        muxer.writeSampleData(trackIdx.coerceAtLeast(0), encodedData, bufferInfo)
                    } catch (e: Exception) {
                        Log.w(TAG, "Writing sample data exception: ${e.message}")
                    }
                }
                encoder.releaseOutputBuffer(outputBufferId, false)
            }
        }
    }

    private fun selectOptimalVideoResolution(): Pair<Int, Int> {
        try {
            val codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val caps = codec.codecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC)
            val videoCaps = caps.videoCapabilities
            val supports1080 = videoCaps != null && videoCaps.isSizeSupported(1080, 1920)
            codec.release()
            if (supports1080) {
                return Pair(1080, 1920)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Probe 1080x1920 resolution exception: ${e.message}")
        }
        return Pair(720, 1280)
    }

    private fun loadSampledSceneBitmap(
        context: Context,
        uriString: String?,
        drawableResId: Int?,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        return try {
            if (!uriString.isNullOrBlank()) {
                val uri = Uri.parse(uriString)
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }

                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
            } else {
                val res = drawableResId ?: R.drawable.scene_thali_delight
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeResource(context.resources, res, options)
                options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight)
                options.inJustDecodeBounds = false
                BitmapFactory.decodeResource(context.resources, res, options)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading scene bitmap: ${e.message}")
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun drawBitmapCenterCrop(
        canvas: Canvas,
        bitmap: Bitmap,
        destWidth: Int,
        destHeight: Int,
        paint: Paint
    ) {
        val srcWidth = bitmap.width.toFloat()
        val srcHeight = bitmap.height.toFloat()

        val scale = Math.max(destWidth / srcWidth, destHeight / srcHeight)
        val scaledWidth = srcWidth * scale
        val scaledHeight = srcHeight * scale

        val left = (destWidth - scaledWidth) / 2f
        val top = (destHeight - scaledHeight) / 2f

        val destRect = RectF(left, top, left + scaledWidth, top + scaledHeight)
        canvas.drawBitmap(bitmap, null, destRect, paint)
    }

    private fun loadBrandLogoBitmap(context: Context, brandProfile: BrandProfile): Bitmap? {
        return try {
            if (brandProfile.customLogoUri != null) {
                val uri = Uri.parse(brandProfile.customLogoUri)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.ic_official_brand_logo)
            }
        } catch (e: Exception) {
            BitmapFactory.decodeResource(context.resources, R.drawable.ic_brand_logo)
        }
    }

    private fun drawLogoWatermarkOnCanvas(
        canvas: Canvas,
        canvasWidth: Int,
        canvasHeight: Int,
        logo: Bitmap,
        brandProfile: BrandProfile
    ) {
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            alpha = (brandProfile.watermarkOpacity.coerceIn(0f, 1f) * 255).toInt()
        }

        val logoSizePx = (canvasWidth * brandProfile.watermarkSize.scaleFactor).toInt().coerceAtLeast(80)
        val marginPx = if (canvasWidth >= 1080) 50 else 36

        val (left, top) = when (brandProfile.watermarkPosition) {
            WatermarkPosition.TOP_RIGHT -> Pair(canvasWidth - logoSizePx - marginPx, marginPx + 40)
            WatermarkPosition.TOP_LEFT -> Pair(marginPx, marginPx + 40)
            WatermarkPosition.BOTTOM_RIGHT -> Pair(canvasWidth - logoSizePx - marginPx, canvasHeight - logoSizePx - marginPx - 260)
            WatermarkPosition.BOTTOM_LEFT -> Pair(marginPx, canvasHeight - logoSizePx - marginPx - 260)
            WatermarkPosition.CENTER -> Pair((canvasWidth - logoSizePx) / 2, (canvasHeight - logoSizePx) / 2)
        }

        val destRect = Rect(left, top, left + logoSizePx, top + logoSizePx)
        canvas.drawBitmap(logo, null, destRect, watermarkPaint)
    }
}
