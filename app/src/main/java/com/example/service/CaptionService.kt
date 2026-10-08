package com.example.service

import com.example.model.ScenePlan

enum class CaptionPosition(val titleBn: String) {
    BOTTOM("নিচে (সাবটাইটেল)"),
    CENTER("মাঝখানে (হাইলাইট)"),
    TOP("উপরে (শিরোনাম)")
}

enum class CaptionAnimation(val titleBn: String) {
    FADE_IN("মসৃণ ফেড ইন"),
    POP_UP("পপ আপ টাইপ"),
    TYPEWRITER("টাইপরাইটার স্টাইল"),
    STATIC("স্থির টেক্সট")
}

data class CaptionStyleConfig(
    val fontSizeSp: Int = 22,
    val position: CaptionPosition = CaptionPosition.BOTTOM,
    val animation: CaptionAnimation = CaptionAnimation.FADE_IN,
    val isTitleEnabled: Boolean = true,
    val isSubtitleEnabled: Boolean = true,
    val isCtaEnabled: Boolean = true,
    val ctaTextBn: String = "অর্ডার করতে কল বা হোয়াটসঅ্যাপ করুন: ৮৫০৯৩৬০৩২৭"
)

interface CaptionService {
    fun generateCaptions(scenes: List<ScenePlan>): List<String>
    fun formatBengaliCta(phone: String, businessName: String): String
}

class DefaultCaptionService : CaptionService {
    override fun generateCaptions(scenes: List<ScenePlan>): List<String> {
        return scenes.map { it.onScreenCaptionBn }
    }

    override fun formatBengaliCta(phone: String, businessName: String): String {
        return "$businessName • কল বা হোয়াটসঅ্যাপ করুন: $phone"
    }
}
