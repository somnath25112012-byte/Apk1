package com.example.model

enum class WatermarkPosition(val titleBn: String, val titleEn: String) {
    TOP_RIGHT("উপরে ডানে", "Top-Right"),
    TOP_LEFT("উপরে বাঁয়ে", "Top-Left"),
    BOTTOM_RIGHT("নিচে ডানে", "Bottom-Right"),
    BOTTOM_LEFT("নিচে বাঁয়ে", "Bottom-Left"),
    CENTER("মাঝখানে", "Center")
}

enum class WatermarkSize(val titleBn: String, val scaleFactor: Float) {
    SMALL("ছোট (১৫%)", 0.15f),
    MEDIUM("মাঝারি (২২%)", 0.22f),
    LARGE("বড় (৩০%)", 0.30f),
    CUSTOM("কাস্টম", 0.25f)
}

data class BrandProfile(
    val businessNameBn: String = "অন্বেষার রসনা বিলাস",
    val businessNameEn: String = "ANWESHAR ROSHONA BILAS",
    val taglineBn: String = "বাঙালিয়ানার চেনা স্বাদ, ঘরের রান্নায় সেরা স্বাদ।",
    val location: String = "বহরমপুর, মুর্শিদাবাদ, পশ্চিমবঙ্গ",
    val phone: String = "8509360327",
    val website: String = "anweshar-roshona-bilas.ai.studio",
    val customLogoUri: String? = null,
    val showLogo: Boolean = true,
    val watermarkPosition: WatermarkPosition = WatermarkPosition.TOP_RIGHT,
    val watermarkSize: WatermarkSize = WatermarkSize.MEDIUM,
    val watermarkOpacity: Float = 0.90f // 90% default as specified
)
