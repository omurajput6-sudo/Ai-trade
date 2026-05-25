package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TradingViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var preferredMarketsText by remember { mutableStateOf(settings.preferredMarkets) }

    // Sync state if settings update externally
    LaunchedEffect(settings.preferredMarkets) {
        preferredMarketsText = settings.preferredMarkets
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Section Header
        item {
            Text(
                text = "Trading Engine Customization",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 1. RISK PROFILE SELECTOR
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Algorithm Risk Strategy",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Conservative uses higher safety filters; Aggressive pursues active trend breakouts.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    val riskOptions = listOf("CONSERVATIVE", "BALANCED", "AGGRESSIVE")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        riskOptions.forEach { risk ->
                            val isSelected = settings.riskProfile == risk
                            val activeColor = when (risk) {
                                "CONSERVATIVE" -> EmeraldGreen
                                "BALANCED" -> CyberCyan
                                else -> CrimsonRed
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) activeColor.copy(alpha = 0.15f) else SurfaceCardVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) activeColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateRiskProfile(risk) }
                                    .testTag("risk_$risk")
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = risk,
                                        color = if (isSelected) activeColor else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. BOT MINIMUM CONFIDENCE ACCURACY
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Min Signal Accuracy Threshold",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    
                    Text(
                        text = "AI signals must meet this match confidence before executing auto-trades.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Slider(
                            value = settings.minSignalAccuracy.toFloat(),
                            onValueChange = { viewModel.updateMinSignalAccuracy(it.toInt()) },
                            valueRange = 50f..95f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberCyan,
                                activeTrackColor = CyberCyan,
                                inactiveTrackColor = Color.DarkGray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("accuracy_slider")
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceCardVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${settings.minSignalAccuracy}%",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. SECURE ACCESS CONTROL (BIOMETRIC SECURITY CHECK)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Biometric Lock Security",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Requires screen locks on opening",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = settings.biometricsEnabled,
                        onCheckedChange = { viewModel.toggleBiometrics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyberCyan,
                            checkedTrackColor = CyberCyan.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.DarkGray,
                            uncheckedTrackColor = SurfaceCardVariant
                        ),
                        modifier = Modifier.testTag("biometrics_security_toggle")
                    )
                }
            }
        }

        // 4. PREFERRED TICKERS/MARKETS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tracked Market Portfolios",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Comma separated tickers for AI analysis support. Recommended: BTC,ETH,SOL,AAPL,TSLA",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = preferredMarketsText,
                        onValueChange = { preferredMarketsText = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color.DarkGray
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("markets_text_field"),
                        trailingIcon = {
                            if (preferredMarketsText != settings.preferredMarkets) {
                                IconButton(
                                    onClick = { viewModel.updatePreferredMarkets(preferredMarketsText) },
                                    modifier = Modifier.testTag("save_markets_button")
                                ) {
                                    Icon(Icons.Default.Save, contentDescription = "Save settings", tint = CyberCyan)
                                }
                            }
                        }
                    )
                }
            }
        }

        // 5. DIAGNOSTICS & SYSTEM AUDIT
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Troubleshoot, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Core Integration Check",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "App Environment", fontSize = 12.sp, color = Color.Gray)
                        Text(text = "Android API 36 (Emulator Supported)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "AI Neural Engine Provider", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            text = if (viewModel.isApiKeyConfgured()) "Google Gemini Pro API" else "Local High-Fidelity Simulator",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.isApiKeyConfgured()) EmeraldGreen else Color.Yellow
                        )
                    }
                }
            }
        }
    }
}
