package com.example.model

data class ExportedVideo(
    val id: String,
    val titleBn: String,
    val filePath: String,
    val durationSeconds: Int,
    val timestamp: Long,
    val fileSizeBytes: Long,
    val resolutionLabel: String = "1080p Full HD",
    val thumbnailResId: Int? = null,
    val isWatermarked: Boolean = true
)
