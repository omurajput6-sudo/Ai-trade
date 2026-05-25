package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio")
data class UserPortfolio(
    @PrimaryKey val symbol: String,
    val name: String,
    val amount: Double,
    val averageBuyPrice: Double,
    val currentPrice: Double,
    val assetType: String // "CRYPTO" or "STOCK"
)

@Entity(tableName = "trade_log")
data class TradeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val type: String, // "BUY" or "SELL"
    val amount: Double,
    val price: Double,
    val totalValue: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val executedBy: String // "AI AUTO" or "MANUAL"
)

@Entity(tableName = "trade_signal")
data class TradeSignal(
    @PrimaryKey(autoGenerate = false) val symbol: String,
    val name: String,
    val type: String, // "BUY", "SELL", "HOLD"
    val price: Double,
    val accuracy: Int, // Percentage (e.g. 85%)
    val rsi: Double,
    val macd: String, // "BULLISH", "BEARISH", "NEUTRAL"
    val reason: String, // Dynamic AI reason
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val riskProfile: String = "BALANCED", // "CONSERVATIVE", "BALANCED", "AGGRESSIVE"
    val autoTradeEnabled: Boolean = false,
    val biometricsEnabled: Boolean = true,
    val minSignalAccuracy: Int = 75,
    val preferredMarkets: String = "BTC,ETH,SOL,AAPL,TSLA" // Comma separated symbols
)
