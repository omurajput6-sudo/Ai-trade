package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.data.local.TradingDatabase
import com.example.data.repository.TradingRepository
import com.example.ui.TradingViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize DB, Repo, & ViewModel on Startup
        val database = TradingDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = TradingRepository(database)
        val factory = TradingViewModel.Factory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[TradingViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val isUnlocked by viewModel.isUnlocked.collectAsState()
                val settings by viewModel.settings.collectAsState()

                // Decide whether we display biometric verification or the main terminal in absolute edge-to-edge layout
                Surface(
                    modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                    color = DeepBackground
                ) {
                    if (settings.biometricsEnabled && !isUnlocked) {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            LockScreen(
                                onUnlockSuccess = { viewModel.setUnlocked(true) }
                            )
                        }
                    } else {
                        AnimatedVisibility(
                            visible = true,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            MainTradingTerminal(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainTradingTerminal(viewModel: TradingViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val statusMessage by viewModel.statusMessage.collectAsState()
    val scope = rememberCoroutineScope()

    // Slide-down floating HUD toast manager
    LaunchedEffect(statusMessage) {
        if (statusMessage != null) {
            delay(3500) // Keep the capsule glowing for 3.5 seconds
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(DeepBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ALGO TRADE AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "QUANTITATIVE QUANT TERMINAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "BOT STATUS: LIVE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.2f), thickness = 1.dp)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceCard,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // Tab 0: Portfolio Dashboard
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Dashboard", modifier = Modifier.size(20.dp)) },
                    label = { Text("Portfolio", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_portfolio")
                )

                // Tab 1: AI Signals Board
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Signals", modifier = Modifier.size(20.dp)) },
                    label = { Text("AI Signals", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_signals")
                )

                // Tab 2: Bot Automates
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Memory, contentDescription = "Bot Log", modifier = Modifier.size(20.dp)) },
                    label = { Text("Bot Activity", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_bot_logs")
                )

                // Tab 3: Configuration
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Customizer", modifier = Modifier.size(20.dp)) },
                    label = { Text("Configure", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mounted Active Screens
            when (selectedTab) {
                0 -> DashboardScreen(viewModel = viewModel)
                1 -> SignalsScreen(viewModel = viewModel)
                2 -> AutoTradeScreen(viewModel = viewModel)
                3 -> SettingsScreen(viewModel = viewModel)
            }

            // High-Tech Cyber HUD Message Overlay (Sliding Toast Capsule)
            AnimatedVisibility(
                visible = statusMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 12.dp, end = 12.dp)
            ) {
                statusMessage?.let { text ->
                    Surface(
                        color = Color.Black.copy(alpha = 0.85f),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = CyberCyan)
                            .border(1.dp, CyberCyan, RoundedCornerShape(24.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = text,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}
