package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrandingWatermark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.BrandProfile
import com.example.model.WatermarkPosition
import com.example.model.WatermarkSize
import com.example.ui.StudioViewModel
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BrandSettingsScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val brandProfile by viewModel.brandProfile.collectAsState()

    var nameBn by remember(brandProfile) { mutableStateOf(brandProfile.businessNameBn) }
    var nameEn by remember(brandProfile) { mutableStateOf(brandProfile.businessNameEn) }
    var tagline by remember(brandProfile) { mutableStateOf(brandProfile.taglineBn) }
    var location by remember(brandProfile) { mutableStateOf(brandProfile.location) }
    var phone by remember(brandProfile) { mutableStateOf(brandProfile.phone) }
    var website by remember(brandProfile) { mutableStateOf(brandProfile.website) }

    var isSavedSnackbar by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.updateBrandProfile(brandProfile.copy(customLogoUri = uri.toString()))
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121014))
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
            .testTag("brand_settings_screen")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Official Brand Header Card
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
                        .background(HeritageGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = "Brand",
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "অফিসিয়াল ব্র্যান্ড ও লোগো সেটিংস",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ভিডিওর ওয়াটারমার্ক ও বিজনেস আইডেন্টিটি কনফিগারেশন",
                        fontSize = 12.sp,
                        color = Color(0xFFD4C8B8)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // OFFICIAL LOGO DISPLAY & CONTROLS (Section M, M1, M2)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1720)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3C3246))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "অফিসিয়াল ব্র্যান্ড লোগো (Authoritative Asset)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "আপনার আপলোড করা মূল লোগো যা অ্যাপ আইকন ও সব ভিডিওর ওয়াটারমার্কে ব্যবহৃত হচ্ছে",
                    color = Color(0xFFC7BBAE),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Logo Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF28202E))
                        .border(1.dp, HeritageGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (brandProfile.customLogoUri != null) {
                        AsyncImage(
                            model = brandProfile.customLogoUri,
                            contentDescription = "Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(130.dp)
                                .alpha(brandProfile.watermarkOpacity)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_official_brand_logo),
                            contentDescription = "Official Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(130.dp)
                                .alpha(brandProfile.watermarkOpacity)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Logo Actions: Replace or Reset to Official
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { logoPickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HeritageGold),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HeritageGold)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = "Replace", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "লোগো পরিবর্তন", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetLogoToOfficial() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF584C62))
                    ) {
                        Icon(imageVector = Icons.Default.RestartAlt, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "মূল লোগো ফিরিয়ে আনুন", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle: "ভিডিওতে লোগো দেখান" (Default: ON - Section M1)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ভিডিওতে লোগো দেখান",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ডিফল্ট: চালু (ভিডিওতে স্বয়ংক্রিয় ওয়াটারমার্ক)",
                            color = Color(0xFFAFA296),
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = brandProfile.showLogo,
                        onCheckedChange = { viewModel.toggleShowLogo(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = HeritageGold,
                            uncheckedTrackColor = Color(0xFF382F3E)
                        ),
                        modifier = Modifier.testTag("switch_show_logo")
                    )
                }

                if (brandProfile.showLogo) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Watermark Position (Section M1: Top-left, Top-right default, Bottom-left, Bottom-right, Center)
                    Text(
                        text = "লোগোর অবস্থান (Position - ডিফল্ট: উপরে ডানে)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WatermarkPosition.values().forEach { pos ->
                            val isSel = brandProfile.watermarkPosition == pos
                            Surface(
                                color = if (isSel) HeritageGold else Color(0xFF26202C),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) HeritageGold else Color(0xFF3E3446)
                                ),
                                modifier = Modifier.clickable { viewModel.setWatermarkPosition(pos) }
                            ) {
                                Text(
                                    text = pos.titleBn,
                                    color = if (isSel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watermark Size (Small, Medium default, Large, Custom)
                    Text(
                        text = "লোগোর আকার (Size - ডিফল্ট: মাঝারি)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WatermarkSize.values().forEach { sz ->
                            val isSel = brandProfile.watermarkSize == sz
                            Surface(
                                color = if (isSel) RoyalBurgundy else Color(0xFF26202C),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSel) HeritageGold else Color(0xFF3E3446)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setWatermarkSize(sz) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = sz.titleBn.split(" ")[0],
                                        color = if (isSel) HeritageGold else Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watermark Opacity (0–100%, default: 90%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লোগোর স্বচ্ছতা (Opacity)",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(brandProfile.watermarkOpacity * 100).toInt()}% (ডিফল্ট: ৯০%)",
                            color = HeritageGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = brandProfile.watermarkOpacity,
                        onValueChange = { viewModel.setWatermarkOpacity(it) },
                        valueRange = 0.1f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = HeritageGold,
                            activeTrackColor = HeritageGold,
                            inactiveTrackColor = Color(0xFF3E3446)
                        ),
                        modifier = Modifier.testTag("slider_watermark_opacity")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BUSINESS DETAILS FORM (Section L)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1720)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3C3246))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ব্যবসায়িক তথ্য (Business Details)",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "ব্যবসার নাম (বাংলা)", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nameBn,
                    onValueChange = { nameBn = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "Business Name (English)", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = nameEn,
                    onValueChange = { nameEn = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ট্যাগলাইন (Tagline)", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ঠিকানা / অবস্থান", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ফোন / হোয়াটসঅ্যাপ নম্বর", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = "ওয়েবসাইট", color = Color(0xFFC7BBAE), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = studioTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updated = brandProfile.copy(
                            businessNameBn = nameBn,
                            businessNameEn = nameEn,
                            taglineBn = tagline,
                            location = location,
                            phone = phone,
                            website = website
                        )
                        viewModel.updateBrandProfile(updated)
                        isSavedSnackbar = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("btn_save_brand_settings"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HeritageGold, contentColor = Color.Black)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "ব্র্যান্ড সেটিংস সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }

                if (isSavedSnackbar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ ব্র্যান্ড তথ্য সফলভাবে ডিভাইসে সংরক্ষিত হয়েছে!",
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
