package com.example.service

import com.example.model.ScenePlan
import com.example.R

interface AiVideoService {
    val isRealApiConnected: Boolean
    val providerName: String

    suspend fun planAiVisualsForPrompt(
        prompt: String,
        style: String,
        sceneCount: Int
    ): List<Int>
}

class DefaultAiVideoService : AiVideoService {
    override val isRealApiConnected: Boolean = false
    override val providerName: String = "Studio Local Visual Library (Food & Brand Aesthetics)"

    private val availableVisuals = listOf(
        R.drawable.scene_thali_delight,
        R.drawable.scene_kasha_mangsho,
        R.drawable.scene_ilish_paturi,
        R.drawable.scene_rosogolla,
        R.drawable.scene_kitchen_flame
    )

    override suspend fun planAiVisualsForPrompt(
        prompt: String,
        style: String,
        sceneCount: Int
    ): List<Int> {
        val result = mutableListOf<Int>()
        for (i in 0 until sceneCount) {
            result.add(availableVisuals[i % availableVisuals.size])
        }
        return result
    }
}
