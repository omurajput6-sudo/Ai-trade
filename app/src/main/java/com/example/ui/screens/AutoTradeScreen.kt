package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.TradingViewModel
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AutoTradeScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val logs by viewModel.tradeLogsList.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    val df = remember { DecimalFormat("$#,##0.00") }
    val qf = remember { DecimalFormat("#,##0.0000") }
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Active Bot control block
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (settings.autoTradeEnabled) EmeraldGreen.copy(alpha = 0.3f) else Color.DarkGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
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
                        Surface(
                            shape = CircleShape,
                            color = if (settings.autoTradeEnabled) EmeraldGreen.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = if (settings.autoTradeEnabled) EmeraldGreen else Color.Gray,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Autonomous AI Trader",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (settings.autoTradeEnabled) "STATUS: ACTIVE & TRADING" else "STATUS: PAUSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (settings.autoTradeEnabled) EmeraldGreen else Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = settings.autoTradeEnabled,
                        onCheckedChange = { viewModel.toggleAutoTrade(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldGreen,
                            checkedTrackColor = EmeraldGreen.copy(alpha = 0.3f),
                            uncheckedThumbColor = Color.DarkGray,
                            uncheckedTrackColor = SurfaceCardVariant
                        ),
                        modifier = Modifier.testTag("auto_trade_toggle")
                    )
                }
            }
        }

        // Statistics row
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AI Executed Trades Log",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (logs.isNotEmpty()) {
                    TextButton(
                        onClick = { viewModel.clearTradingLogs() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray),
                        modifier = Modifier.testTag("clear_logs_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clear Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recorded trades",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Enable Autonomous AI Trader in settings and re-analyze or run standard simulated orders to test the system.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp).padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        // Main trade list item
        items(logs, key = { it.id }) { log ->
            val isBuy = log.type == "BUY"
            val badgeColor = if (isBuy) EmeraldGreen else CrimsonRed
            val executedByBot = log.executedBy == "AI AUTO"

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // BUY/SELL direction visual circular tag
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(badgeColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBuy) "B" else "S",
                                color = badgeColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${log.type} ${log.symbol}",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (executedByBot) CyberCyan.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.15f)
                                        )
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = log.executedBy,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (executedByBot) CyberCyan else Color.LightGray
                                    )
                                }
                            }
                            
                            Text(
                                text = dateFormat.format(Date(log.timestamp)),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = df.format(log.totalValue),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${qf.format(log.amount)} @ ${df.format(log.price)}",
                            fontSize = 11.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
