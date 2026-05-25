package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    fun getPortfolioFlow(): Flow<List<UserPortfolio>>

    @Query("SELECT * FROM portfolio ORDER BY symbol ASC")
    suspend fun getPortfolioList(): List<UserPortfolio>

    @Query("SELECT * FROM portfolio WHERE symbol = :symbol LIMIT 1")
    suspend fun getAssetBySymbol(symbol: String): UserPortfolio?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: UserPortfolio)

    @Query("DELETE FROM portfolio WHERE symbol = :symbol")
    suspend fun deleteAsset(symbol: String)
}

@Dao
interface TradeLogDao {
    @Query("SELECT * FROM trade_log ORDER BY timestamp DESC")
    fun getTradeLogsFlow(): Flow<List<TradeLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TradeLog)

    @Query("DELETE FROM trade_log")
    suspend fun clearLogs()
}

@Dao
interface TradeSignalDao {
    @Query("SELECT * FROM trade_signal ORDER BY timestamp DESC")
    fun getSignalsFlow(): Flow<List<TradeSignal>>

    @Query("SELECT * FROM trade_signal ORDER BY timestamp DESC")
    suspend fun getSignalsList(): List<TradeSignal>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignals(signals: List<TradeSignal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignal(signal: TradeSignal)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettings?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettings)
}
