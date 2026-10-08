package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudioScreen
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy

@Composable
fun StudioBottomBar(
    currentScreen: StudioScreen,
    onScreenSelected: (StudioScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .border(1.dp, Color(0xFF2E2633), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .testTag("studio_bottom_nav_bar"),
        containerColor = Color(0xFF141216),
        tonalElevation = 8.dp
    ) {
        val items = listOf(
            Triple(StudioScreen.AI_GENERATOR, Icons.Default.AutoAwesome, "AI ভিডিও"),
            Triple(StudioScreen.USER_MEDIA, Icons.Default.PermMedia, "মিডিয়া"),
            Triple(StudioScreen.SCRIPT_VOICE, Icons.Default.RecordVoiceOver, "স্ক্রিপ্ট/ভয়েস"),
            Triple(StudioScreen.MY_VIDEOS, Icons.Default.VideoLibrary, "আমার ভিডিও"),
            Triple(StudioScreen.BRAND_SETTINGS, Icons.Default.Storefront, "ব্র্যান্ড")
        )

        items.forEach { (screen, icon, label) ->
            val isSelected = currentScreen == screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    selectedTextColor = HeritageGold,
                    indicatorColor = HeritageGold,
                    unselectedIconColor = Color(0xFFA5978E),
                    unselectedTextColor = Color(0xFFA5978E)
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
