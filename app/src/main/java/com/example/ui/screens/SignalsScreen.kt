package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TradeSignal
import com.example.ui.TradingViewModel
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun SignalsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val signals by viewModel.signalsList.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val errorMsg by viewModel.uiError.collectAsState()

    val df = remember { DecimalFormat("$#,##0.00") }
    
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 84.dp)
        ) {
            // AI Service Connection Header Status Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (viewModel.isApiKeyConfgured()) EmeraldGreen else Color.Yellow)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (viewModel.isApiKeyConfgured()) "Gemini-3.5-Flash Active" else "API Demonstration Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "Risk Style: ${settings.riskProfile} | Target Conf: >=${settings.minSignalAccuracy}%",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyberCyan,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (!viewModel.isApiKeyConfgured()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Yellow.copy(alpha = 0.08f))
                                    .border(1.dp, Color.Yellow.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "To run live neural analysis on markets, add your GEMINI_API_KEY inside the Secrets panel in Google AI Studio. Currently simulating highly dynamic technical indicators.",
                                    fontSize = 10.sp,
                                    color = Color(0xFFFFD54F),
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        if (errorMsg != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CrimsonRed.copy(alpha = 0.1f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = errorMsg!!,
                                    fontSize = 10.sp,
                                    color = CrimsonRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Technical Signal Cards Header
            item {
                Text(
                    text = "AI Quantitative Buy & Sell Signals",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (signals.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassEmpty,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                    text = "No signals generated yet",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                            )
                            Text(
                                text = "Tap the refresh button below to run automated AI market analysis",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            // Signals list items
            items(signals, key = { it.symbol }) { signal ->
                val badgeColor = when (signal.type) {
                    "BUY" -> EmeraldGreen
                    "SELL" -> CrimsonRed
                    else -> Color.Gray
                }

                val indicatorRsi = when {
                    signal.rsi < 35 -> "Oversold"
                    signal.rsi > 65 -> "Overbought"
                    else -> "Neutral"
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(badgeColor.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = signal.symbol,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = signal.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = df.format(signal.price),
                                        fontSize = 12.sp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                            // Trigger Action indicator badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(badgeColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = signal.type,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = badgeColor,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Confidence bar
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Confidence Matrix",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.width(100.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(signal.accuracy / 100f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(CyberCyan, badgeColor)
                                            )
                                        )
                                )
                            }
                            
                            Text(
                                text = "${signal.accuracy}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                        }

                        // Technical readings
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // RSI Indicator
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceCardVariant)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("RSI (14)", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = signal.rsi.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "($indicatorRsi)",
                                            fontSize = 9.sp,
                                            color = if (indicatorRsi == "Oversold") EmeraldGreen else if (indicatorRsi == "Overbought") CrimsonRed else Color.LightGray,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // MACD Indicator
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceCardVariant)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text("MACD TREND", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = signal.macd,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (signal.macd == "BULLISH") EmeraldGreen else if (signal.macd == "BEARISH") CrimsonRed else Color.White
                                    )
                                }
                            }
                        }

                        // AI Advice / automated analysis explanation
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "AI Automated Analysis (Hindi-Eng)",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = signal.reason,
                                    fontSize = 11.5.sp,
                                    color = Color.LightGray,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to refresh signals
        FloatingActionButton(
            onClick = { viewModel.refreshSignals() },
            containerColor = CyberCyan,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 84.dp, end = 16.dp)
                .testTag("refresh_signals_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh Market Signals",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
