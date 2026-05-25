package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiGenerationConfig
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.RetrofitClient
import com.squareup.moshi.Types
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TradingRepository(private val database: TradingDatabase) {

    private val portfolioDao = database.portfolioDao()
    private val tradeLogDao = database.tradeLogDao()
    private val tradeSignalDao = database.tradeSignalDao()
    private val userSettingsDao = database.userSettingsDao()

    val portfolio: Flow<List<UserPortfolio>> = portfolioDao.getPortfolioFlow()
    val signals: Flow<List<TradeSignal>> = tradeSignalDao.getSignalsFlow()
    val logs: Flow<List<TradeLog>> = tradeLogDao.getTradeLogsFlow()
    val settings: Flow<UserSettings?> = userSettingsDao.getSettingsFlow()

    suspend fun getSettings(): UserSettings? = withContext(Dispatchers.IO) {
        userSettingsDao.getSettings()
    }

    suspend fun saveSettings(userSettings: UserSettings) = withContext(Dispatchers.IO) {
        userSettingsDao.saveSettings(userSettings)
    }

    suspend fun clearLogs() = withContext(Dispatchers.IO) {
        tradeLogDao.clearLogs()
    }

    suspend fun insertManualTrade(symbol: String, name: String, type: String, amount: Double, price: Double) = withContext(Dispatchers.IO) {
        val asset = portfolioDao.getAssetBySymbol(symbol)
        if (type == "BUY") {
            if (asset != null) {
                val newAmount = asset.amount + amount
                val totalCost = (asset.amount * asset.averageBuyPrice) + (amount * price)
                val newAvgPrice = totalCost / newAmount
                portfolioDao.insertAsset(
                    asset.copy(
                        amount = newAmount,
                        averageBuyPrice = newAvgPrice,
                        currentPrice = price
                    )
                )
            } else {
                portfolioDao.insertAsset(
                    UserPortfolio(
                        symbol = symbol,
                        name = name,
                        amount = amount,
                        averageBuyPrice = price,
                        currentPrice = price,
                        assetType = if (symbol == "AAPL" || symbol == "TSLA") "STOCK" else "CRYPTO"
                    )
                )
            }
            tradeLogDao.insertLog(
                TradeLog(
                    symbol = symbol,
                    type = "BUY",
                    amount = amount,
                    price = price,
                    totalValue = amount * price,
                    executedBy = "MANUAL"
                )
            )
        } else { // SELL
            if (asset != null && asset.amount >= amount) {
                val newAmount = asset.amount - amount
                if (newAmount <= 0.0001) {
                    portfolioDao.deleteAsset(symbol)
                } else {
                    portfolioDao.insertAsset(
                        asset.copy(
                            amount = newAmount,
                            currentPrice = price
                        )
                    )
                }
                tradeLogDao.insertLog(
                    TradeLog(
                        symbol = symbol,
                        type = "SELL",
                        amount = amount,
                        price = price,
                        totalValue = amount * price,
                        executedBy = "MANUAL"
                    )
                )
            }
        }
    }

    suspend fun fetchSignalsAndAnalyze(riskProfile: String, preferredMarkets: String): List<TradeSignal> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isMockMode = apiKey.isBlank() || 
                         apiKey == "MY_GEMINI_API_KEY" || 
                         apiKey.contains("placeholder", ignoreCase = true)

        if (isMockMode) {
            Log.d("TradingRepository", "Mock mode: Generating automated market signals...")
            val simulated = generateSimulatedSignals(riskProfile, preferredMarkets)
            tradeSignalDao.insertSignals(simulated)
            return@withContext simulated
        }

        try {
            Log.d("TradingRepository", "Calling Gemini API for technical signal analysis...")
            val prompt = """
                Analyze the financial markets for these specific tickers/symbols: $preferredMarkets.
                The user has selected a [$riskProfile] risk tolerance trading style.
                We have roughly these current markets:
                - BTC is around $67,500
                - ETH is around $3,450
                - SOL is around $165.0
                - AAPL is around $189.0
                - TSLA is around $215.0

                Return a RAW valid JSON array matching the Trading schema. Your response must have NO markdown blocks, NO backticks (Do NOT write ```json or ```). Return ONLY the raw valid JSON payload.
                Write the "reason" field in high-quality Hinglish (Hindi + English) as the user is an Indian retail trader (e.g., "RSI is now 32 which is oversold range. Support standard levels par strong bounce support mil raha hai, isliye safe BUY trigger generate hua hai.").
                
                The JSON schema to follow:
                [
                  {
                    "symbol": "BTC",
                    "name": "Bitcoin",
                    "type": "BUY", // Must be either "BUY", "SELL", or "HOLD"
                    "price": 67350.5,
                    "accuracy": 89, // confidence rating percentage 40 to 99
                    "rsi": 62.4, // standard 14-period rsi value
                    "macd": "BULLISH", // "BULLISH", "BEARISH", "NEUTRAL"
                    "reason": "MACD golden crossover and horizontal support volume indicate bullish momentum starting."
                  }
                ]
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.5
                )
            )

            val rawResponse = RetrofitClient.geminiApi.generateContent(apiKey, request)
            val textOutput = rawResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            if (!textOutput.isNullOrBlank()) {
                var cleanedText = textOutput.trim()
                if (cleanedText.startsWith("```json")) {
                    cleanedText = cleanedText.removePrefix("```json").trim()
                }
                if (cleanedText.startsWith("```")) {
                    cleanedText = cleanedText.removePrefix("```").trim()
                }
                if (cleanedText.endsWith("```")) {
                    cleanedText = cleanedText.removeSuffix("```").trim()
                }

                val type = Types.newParameterizedType(List::class.java, TradeSignal::class.java)
                val adapter = RetrofitClient.getMoshi().adapter<List<TradeSignal>>(type)
                val parsed = adapter.fromJson(cleanedText)

                if (!parsed.isNullOrEmpty()) {
                    tradeSignalDao.insertSignals(parsed)
                    return@withContext parsed
                }
            }
            throw IllegalStateException("Received empty or corrupt response from Gemini")

        } catch (e: Exception) {
            Log.e("TradingRepository", "Gemini API call failed, falling back to rich simulator: ${e.message}")
            val simulated = generateSimulatedSignals(riskProfile, preferredMarkets)
            tradeSignalDao.insertSignals(simulated)
            return@withContext simulated
        }
    }

    suspend fun executeAutoTrade(signal: TradeSignal, minAccuracy: Int) = withContext(Dispatchers.IO) {
        if (signal.type == "HOLD") return@withContext
        if (signal.accuracy < minAccuracy) {
            Log.d("TradingRepository", "AI AutoTrade bypassed: signal accuracy (${signal.accuracy}%) is below minimum threshold ($minAccuracy%)")
            return@withContext
        }

        val asset = portfolioDao.getAssetBySymbol(signal.symbol)
        
        // Dynamic amount to trade based on ticker
        val amountToTrade = when(signal.symbol) {
            "BTC" -> 0.05
            "ETH" -> 0.5
            "SOL" -> 5.0
            "AAPL" -> 10.0
            "TSLA" -> 12.0
            else -> 8.0
        }

        if (signal.type == "BUY") {
            if (asset != null) {
                val newAmount = asset.amount + amountToTrade
                val totalCost = (asset.amount * asset.averageBuyPrice) + (amountToTrade * signal.price)
                val newAvgPrice = totalCost / newAmount
                portfolioDao.insertAsset(
                    asset.copy(
                        amount = newAmount,
                        averageBuyPrice = newAvgPrice,
                        currentPrice = signal.price
                    )
                )
            } else {
                portfolioDao.insertAsset(
                    UserPortfolio(
                        symbol = signal.symbol,
                        name = signal.name,
                        amount = amountToTrade,
                        averageBuyPrice = signal.price,
                        currentPrice = signal.price,
                        assetType = if (signal.symbol == "AAPL" || signal.symbol == "TSLA") "STOCK" else "CRYPTO"
                    )
                )
            }
            tradeLogDao.insertLog(
                TradeLog(
                    symbol = signal.symbol,
                    type = "BUY",
                    amount = amountToTrade,
                    price = signal.price,
                    totalValue = amountToTrade * signal.price,
                    executedBy = "AI AUTO"
                )
            )
            Log.d("TradingRepository", "AI AutoTrade Executed: BOUGHT $amountToTrade ${signal.symbol} at ${signal.price}")

        } else if (signal.type == "SELL") {
            if (asset != null && asset.amount >= amountToTrade) {
                val newAmount = asset.amount - amountToTrade
                if (newAmount <= 0.0001) {
                    portfolioDao.deleteAsset(signal.symbol)
                } else {
                    portfolioDao.insertAsset(
                        asset.copy(
                            amount = newAmount,
                            currentPrice = signal.price
                        )
                    )
                }
                tradeLogDao.insertLog(
                    TradeLog(
                        symbol = signal.symbol,
                        type = "SELL",
                        amount = amountToTrade,
                        price = signal.price,
                        totalValue = amountToTrade * signal.price,
                        executedBy = "AI AUTO"
                    )
                )
                Log.d("TradingRepository", "AI AutoTrade Executed: SOLD $amountToTrade ${signal.symbol} at ${signal.price}")
            } else {
                Log.d("TradingRepository", "AI AutoTrade Sell bypassed: User owns insufficient holdings of ${signal.symbol} to sell.")
            }
        }
    }

    private fun generateSimulatedSignals(riskProfile: String, marketsString: String): List<TradeSignal> {
        val markets = marketsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val list = mutableListOf<TradeSignal>()
        val timeFactor = System.currentTimeMillis() / 60000 % 60 // Increments every minute to create slow fluctuations

        for (symbol in markets) {
            val (name, basePrice) = when (symbol) {
                "BTC" -> Pair("Bitcoin", 67200.0)
                "ETH" -> Pair("Ethereum", 3450.0)
                "SOL" -> Pair("Solana", 166.5)
                "AAPL" -> Pair("Apple Inc.", 189.2)
                "TSLA" -> Pair("Tesla Inc.", 214.5)
                else -> Pair(symbol, 125.0)
            }

            val seed = symbol.hashCode().toDouble()
            // Periodic wave function based on current time
            val fluctuation = Math.sin(timeFactor.toDouble() + seed) * (basePrice * 0.025)
            val finalPrice = Math.round((basePrice + fluctuation) * 100.0) / 100.0

            val rsiSeed = 28.0 + (Math.cos(timeFactor.toDouble() + seed) + 1.0) * 26.0 // oscillates between 28 and 80
            val rsi = Math.round(rsiSeed * 10.0) / 10.0
            val macd = when {
                rsi > 68 -> "BEARISH"
                rsi < 38 -> "BULLISH"
                else -> if (seed.toInt() % 2 == 0) "BULLISH" else "NEUTRAL"
            }

            val type = when {
                rsi < 36 -> "BUY"
                rsi > 64 -> "SELL"
                macd == "BULLISH" && riskProfile == "AGGRESSIVE" && rsi < 55 -> "BUY"
                macd == "BEARISH" && riskProfile != "CONSERVATIVE" -> "SELL"
                else -> if (seed.toInt() % 3 == 0) "BUY" else if (seed.toInt() % 3 == 1) "SELL" else "HOLD"
            }

            val accuracy = when (riskProfile) {
                "CONSERVATIVE" -> 82 + (seed.toInt() % 14)
                "BALANCED" -> 74 + (seed.toInt() % 21)
                else -> 65 + (seed.toInt() % 28) // Aggressive is more volatile
            }

            val reason = when (type) {
                "BUY" -> {
                    when {
                        rsi < 36 -> "RSI index $rsi level par oversold boundary me aa chuka hai. Is strong support band se reversal volume confirm ho raha hai, safe BUY trigger hai."
                        macd == "BULLISH" -> "Moving average convergence divergence (MACD) gold crossover complete kar chuka hai. Momentum profile $riskProfile strategies ke liye optimized buying suggest karta hai."
                        else -> "Support zone solid lag raha hai, continuous buying pressure accumulate ho rhi h local charts par. Risk profile criteria match hone par early entry signal."
                    }
                }
                "SELL" -> {
                    when {
                        rsi > 64 -> "RSI $rsi range me enter ho chuka h, meaning local market overbought level exceed kar gya h. Profit booking lock in karne ka optimal time h."
                        macd == "BEARISH" -> "MACD local baseline support ko breakdown karke downwards face kar rha h. Heavy correction risk se bachne ke liye position liquidating signals."
                        else -> "Local resistance targets par heavy supply order blocks dikh rahe hain. Sell-off momentum badhne se pehle safe exit recommend kya jata hai."
                    }
                }
                else -> {
                    "Horizontal consolidation channel me trade kar raha h price. Technical indicators (RSI: $rsi) neutral trigger create kar rahe h, entry ke liye breakthrough check karein."
                }
            }

            list.add(
                TradeSignal(
                    symbol = symbol,
                    name = name,
                    type = type,
                    price = finalPrice,
                    accuracy = accuracy,
                    rsi = rsi,
                    macd = macd,
                    reason = reason,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
        return list
    }
}
