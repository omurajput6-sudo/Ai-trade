package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserPortfolio
import com.example.ui.TradingViewModel
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun DashboardScreen(
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    val portfolioList by viewModel.portfolioList.collectAsState()
    val totalVal by viewModel.totalPortfolioValueUSD.collectAsState(initial = 0.0)
    val totalCost by viewModel.totalInvestmentCostUSD.collectAsState(initial = 0.0)
    
    var showTradeDialog by remember { mutableStateOf(false) }
    val df = remember { DecimalFormat("$#,##0.00") }
    val pf = remember { DecimalFormat("+#,##0.00%;-#,##0.00%") }

    val profitLossValue = totalVal - totalCost
    val profitLossPercentage = if (totalCost > 0.0) profitLossValue / totalCost else 0.0
    val isProfit = profitLossValue >= 0.0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Balance glowing card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp), spotColor = CyberCyan)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "TOTAL PORTFOLIO NET WORTH",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = df.format(totalVal),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Invested Capital", fontSize = 11.sp, color = Color.Gray)
                            Text(
                                text = df.format(totalCost),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isProfit) EmeraldGreen.copy(alpha = 0.15f) else CrimsonRed.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = pf.format(profitLossPercentage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isProfit) EmeraldGreen else CrimsonRed
                            )
                        }
                    }
                }
            }
        }

        // Live Market mini trend chart
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timeline,
                                contentDescription = "Sparklines",
                                tint = CyberCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Market Analytics Graph (Live)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "BTC / USDT 1H",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Draw glowing financial candlesticks & grid lines in canvas
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        // Grid lines
                        val steps = 3
                        for (i in 0..steps) {
                            val y = (height / steps) * i
                            drawLine(
                                color = Color.White.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        // Simulated Candles
                        // 8 Candlesticks
                        val candleCount = 10
                        val spacing = width / candleCount
                        val candleWidth = spacing * 0.5f

                        val prices = listOf(
                            64200.0, 64600.0, 64100.0, 65300.0, 65000.0, 
                            65800.0, 66400.0, 65900.0, 67100.0, 67250.0
                        )
                        val opens = listOf(
                            64000.0, 64300.0, 64500.0, 64200.0, 65200.0, 
                            65100.0, 65700.0, 66300.0, 66000.0, 66800.0
                        )
                        val highs = listOf(
                            64500.0, 64800.0, 64600.0, 65500.0, 65400.0, 
                            66100.0, 66600.0, 66500.0, 67200.0, 67450.0
                        )
                        val lows = listOf(
                            63800.0, 64100.0, 63900.0, 64000.0, 64900.0, 
                            64800.0, 65500.0, 65700.0, 65800.0, 66600.0
                        )

                        val minPrice = 63000.0
                        val maxPrice = 68000.0
                        val range = maxPrice - minPrice

                        for (idx in 0 until candleCount) {
                            val x = (spacing * idx) + (spacing * 0.25f)
                            
                            val highY = height - (((highs[idx] - minPrice) / range) * height).toFloat()
                            val lowY = height - (((lows[idx] - minPrice) / range) * height).toFloat()
                            val openY = height - (((opens[idx] - minPrice) / range) * height).toFloat()
                            val closeY = height - (((prices[idx] - minPrice) / range) * height).toFloat()

                            val isBullish = prices[idx] >= opens[idx]
                            val candleColor = if (isBullish) EmeraldGreen else CrimsonRed

                            // Draw shadow wick
                            drawLine(
                                color = candleColor,
                                start = Offset(x + (candleWidth / 2f), highY),
                                end = Offset(x + (candleWidth / 2f), lowY),
                                strokeWidth = 2.dp.toPx()
                            )

                            // Draw candle body
                            drawRect(
                                color = candleColor,
                                topLeft = Offset(x, Math.min(openY, closeY)),
                                size = Size(candleWidth, Math.abs(openY - closeY).coerceAtLeast(4f))
                            )
                        }
                    }
                }
            }
        }

        // Section label
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Asset Holdings Allocation",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Button(
                    onClick = { showTradeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.15f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("execute_order_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Simulate Order", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Empty state
        if (portfolioList.isEmpty()) {
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
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No active holdings yet",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Simulate an order or enable auto-trading to populate assets",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Holdings List
        items(portfolioList, key = { it.symbol }) { holding ->
            val assetValue = holding.amount * holding.currentPrice
            val assetCost = holding.amount * holding.averageBuyPrice
            val assetProfit = assetValue - assetCost
            val assetProfitPct = if (assetCost > 0.0) assetProfit / assetCost else 0.0
            val isAssetGain = assetProfit >= 0.0

            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.DarkGray.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .clickable { 
                        // Show info or trigger a quick sell dialog if desired, or skip to keep interface clean
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Colored Circle Icon with First Symbol Letter
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (holding.assetType == "CRYPTO") CyberCyan.copy(alpha = 0.1f) else Color(0xFFFFD54F).copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = holding.symbol.take(2),
                                color = if (holding.assetType == "CRYPTO") CyberCyan else Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = holding.name,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${holding.amount} ${holding.symbol}",
                                    fontSize = 12.sp,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(SurfaceCardVariant)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = holding.assetType,
                                        fontSize = 9.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = df.format(assetValue),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Avg: ${df.format(holding.averageBuyPrice)}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = (if (isAssetGain) "+" else "") + pf.format(assetProfitPct),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAssetGain) EmeraldGreen else CrimsonRed
                            )
                        }
                    }
                }
            }
        }
    }

    // SIMULATED ORDER DOOR DIALOGUE
    if (showTradeDialog) {
        var symbolChoice by remember { mutableStateOf("BTC") }
        var typeChoice by remember { mutableStateOf("BUY") }
        var quantityString by remember { mutableStateOf("0.1") }
        var priceString by remember { mutableStateOf("") }
        var inputError by remember { mutableStateOf<String?>(null) }

        val choices = listOf("BTC", "ETH", "SOL", "AAPL", "TSLA")

        // Set default price based on asset choice
        LaunchedEffect(symbolChoice) {
            val basePrice = when (symbolChoice) {
                "BTC" -> 67250.0
                "ETH" -> 3450.0
                "SOL" -> 166.5
                "AAPL" -> 189.2
                else -> 214.5
            }
            priceString = basePrice.toString()
        }

        AlertDialog(
            onDismissRequest = { showTradeDialog = false },
            containerColor = SurfaceCard,
            title = {
                Text(
                    text = "AI Quantitative Order Form",
                    fontWeight = FontWeight.ExtraBold,
                    color = CyberCyan,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Symbol pick
                    Text("Select Ticker / Asset:", color = Color.Gray, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        choices.forEach { sym ->
                            val isSelected = sym == symbolChoice
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else SurfaceCardVariant)
                                    .border(1.dp, if (isSelected) CyberCyan else Color.Transparent, RoundedCornerShape(8.dp))
                                    .clickable { symbolChoice = sym }
                                    .testTag("select_$sym")
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sym,
                                    color = if (isSelected) CyberCyan else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Order Type
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCardVariant)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (typeChoice == "BUY") EmeraldGreen.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { typeChoice = "BUY" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("BUY (Long)", color = if (typeChoice == "BUY") EmeraldGreen else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (typeChoice == "SELL") CrimsonRed.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { typeChoice = "SELL" }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("SELL (Short)", color = if (typeChoice == "SELL") CrimsonRed else Color.Gray, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    // Inputs
                    OutlinedTextField(
                        value = quantityString,
                        onValueChange = { quantityString = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("quantity_input")
                    )

                    OutlinedTextField(
                        value = priceString,
                        onValueChange = { priceString = it },
                        label = { Text("Execution Price (USD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("price_input")
                    )

                    if (inputError != null) {
                        Text(
                            text = inputError!!,
                            color = CrimsonRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = quantityString.toDoubleOrNull()
                        val prc = priceString.toDoubleOrNull()
                        if (qty == null || qty <= 0.0) {
                            inputError = "Invalid quantity specified."
                            return@Button
                        }
                        if (prc == null || prc <= 0.0) {
                            inputError = "Invalid price specified."
                            return@Button
                        }
                        
                        val name = when (symbolChoice) {
                            "BTC" -> "Bitcoin"
                            "ETH" -> "Ethereum"
                            "SOL" -> "Solana"
                            "AAPL" -> "Apple Inc."
                            else -> "Tesla Inc."
                        }

                        // Submit
                        viewModel.submitManualTrade(symbolChoice, name, typeChoice, qty, prc)
                        showTradeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_order")
                ) {
                    Text("Execute Order", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTradeDialog = false }) {
                    Text("Dismiss", color = Color.Gray)
                }
            }
        )
    }
}
