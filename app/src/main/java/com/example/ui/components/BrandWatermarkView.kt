package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.model.BrandProfile
import com.example.model.WatermarkPosition
import com.example.model.WatermarkSize

@Composable
fun BoxScope.BrandWatermarkView(
    brandProfile: BrandProfile,
    modifier: Modifier = Modifier,
    containerBaseDp: Dp = 320.dp
) {
    if (!brandProfile.showLogo) return

    val alignment = when (brandProfile.watermarkPosition) {
        WatermarkPosition.TOP_RIGHT -> Alignment.TopEnd
        WatermarkPosition.TOP_LEFT -> Alignment.TopStart
        WatermarkPosition.BOTTOM_RIGHT -> Alignment.BottomEnd
        WatermarkPosition.BOTTOM_LEFT -> Alignment.BottomStart
        WatermarkPosition.CENTER -> Alignment.Center
    }

    val logoSizeDp = when (brandProfile.watermarkSize) {
        WatermarkSize.SMALL -> containerBaseDp * 0.16f
        WatermarkSize.MEDIUM -> containerBaseDp * 0.22f
        WatermarkSize.LARGE -> containerBaseDp * 0.30f
        WatermarkSize.CUSTOM -> containerBaseDp * 0.25f
    }.coerceIn(48.dp, 120.dp)

    Box(
        modifier = modifier
            .align(alignment)
            .padding(14.dp)
            .alpha(brandProfile.watermarkOpacity.coerceIn(0f, 1f))
            .testTag("brand_watermark_view")
    ) {
        if (brandProfile.customLogoUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(brandProfile.customLogoUri))
                    .crossfade(true)
                    .build(),
                contentDescription = "Official Brand Watermark",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(logoSizeDp)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.ic_official_brand_logo),
                contentDescription = "Official Brand Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(logoSizeDp)
            )
        }
    }
}
