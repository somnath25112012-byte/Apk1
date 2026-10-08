package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudioScreen
import com.example.ui.StudioViewModel
import com.example.ui.components.StudioBottomBar
import com.example.ui.screens.AiGeneratorScreen
import com.example.ui.screens.BrandSettingsScreen
import com.example.ui.screens.MyVideosScreen
import com.example.ui.screens.ScriptVoiceScreen
import com.example.ui.screens.UserMediaScreen
import com.example.ui.theme.AnwesharStudioTheme
import com.example.ui.theme.HeritageGold
import com.example.ui.theme.RoyalBurgundy

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnwesharStudioTheme {
                StudioAppRoot(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioAppRoot(
    viewModel: StudioViewModel
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val brandProfile by viewModel.brandProfile.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != StudioScreen.AI_GENERATOR) {
        viewModel.setScreen(StudioScreen.AI_GENERATOR)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        containerColor = Color(0xFF121014),
        topBar = {
            StudioTopBar(
                businessNameBn = brandProfile.businessNameBn,
                taglineBn = brandProfile.taglineBn
            )
        },
        bottomBar = {
            StudioBottomBar(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentScreen,
                label = "ScreenCrossfade"
            ) { screen ->
                when (screen) {
                    StudioScreen.AI_GENERATOR -> AiGeneratorScreen(viewModel = viewModel)
                    StudioScreen.USER_MEDIA -> UserMediaScreen(viewModel = viewModel)
                    StudioScreen.SCRIPT_VOICE -> ScriptVoiceScreen(viewModel = viewModel)
                    StudioScreen.MY_VIDEOS -> MyVideosScreen(viewModel = viewModel)
                    StudioScreen.BRAND_SETTINGS -> BrandSettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun StudioTopBar(
    businessNameBn: String,
    taglineBn: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .testTag("studio_top_bar"),
        color = Color(0xFF19151E),
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E2535))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Official Brand Logo Emblem in Header
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2130))
                    .border(1.5.dp, HeritageGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_official_brand_logo),
                    contentDescription = "Brand Emblem",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "অন্বেষার ভিডিও স্টুডিও",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$businessNameBn • প্রিমিয়াম এআই ভিডিও স্টুডিও",
                    color = HeritageGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
