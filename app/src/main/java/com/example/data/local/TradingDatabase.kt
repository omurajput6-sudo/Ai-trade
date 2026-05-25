package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserPortfolio::class, TradeLog::class, TradeSignal::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
abstract class TradingDatabase : RoomDatabase() {
    abstract fun portfolioDao(): PortfolioDao
    abstract fun tradeLogDao(): TradeLogDao
    abstract fun tradeSignalDao(): TradeSignalDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: TradingDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): TradingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TradingDatabase::class.java,
                    "ai_trading_database"
                )
                    .addCallback(DatabasePrepopulationCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulationCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    // 1. Insert Initial Customization Settings
                    database.userSettingsDao().saveSettings(
                        UserSettings(
                            id = 1,
                            riskProfile = "BALANCED",
                            autoTradeEnabled = false,
                            biometricsEnabled = true,
                            minSignalAccuracy = 75,
                            preferredMarkets = "BTC,ETH,SOL,AAPL,TSLA"
                        )
                    )

                    // 2. Insert Initial Portfolio Holdings (user starts with some holdings)
                    val initialPortfolio = listOf(
                        UserPortfolio("BTC", "Bitcoin", 0.42, 64200.0, 67250.0, "CRYPTO"),
                        UserPortfolio("ETH", "Ethereum", 3.5, 3100.0, 3450.0, "CRYPTO"),
                        UserPortfolio("TSLA", "Tesla Inc.", 15.0, 180.0, 215.4, "STOCK"),
                        UserPortfolio("AAPL", "Apple Inc.", 10.0, 168.0, 189.2, "STOCK")
                    )
                    initialPortfolio.forEach {
                        database.portfolioDao().insertAsset(it)
                    }

                    // 3. Insert Initial Signals in history
                    val initialSignals = listOf(
                        TradeSignal(
                            symbol = "BTC",
                            name = "Bitcoin",
                            type = "BUY",
                            price = 67250.0,
                            accuracy = 88,
                            rsi = 62.4,
                            macd = "BULLISH",
                            reason = "AI MACD crossover standard signal with strong pattern support. Perfect buying opportunity.",
                            timestamp = System.currentTimeMillis() - 3600000
                        ),
                        TradeSignal(
                            symbol = "ETH",
                            name = "Ethereum",
                            type = "BUY",
                            price = 3450.0,
                            accuracy = 82,
                            rsi = 58.0,
                            macd = "BULLISH",
                            reason = "EMA-20 crossed above EMA-50 showing strong bullish momentum.",
                            timestamp = System.currentTimeMillis() - 7200000
                        ),
                        TradeSignal(
                            symbol = "TSLA",
                            name = "Tesla Inc.",
                            type = "SELL",
                            price = 215.4,
                            accuracy = 79,
                            rsi = 74.2,
                            macd = "BEARISH",
                            reason = "RSI index suggests overbought territory with local distribution visible.",
                            timestamp = System.currentTimeMillis() - 10800000
                        )
                    )
                    database.tradeSignalDao().insertSignals(initialSignals)

                    // 4. Insert Initial Trade Logs
                    val initialLogs = listOf(
                        TradeLog(
                            symbol = "BTC",
                            type = "BUY",
                            amount = 0.05,
                            price = 64200.0,
                            totalValue = 3210.0,
                            timestamp = System.currentTimeMillis() - 43200000,
                            executedBy = "MANUAL"
                        ),
                        TradeLog(
                            symbol = "ETH",
                            type = "BUY",
                            amount = 1.2,
                            price = 3050.0,
                            totalValue = 3660.0,
                            timestamp = System.currentTimeMillis() - 86400000,
                            executedBy = "AI AUTO"
                        )
                    )
                    initialLogs.forEach {
                        database.tradeLogDao().insertLog(it)
                    }
                }
            }
        }
    }
}
