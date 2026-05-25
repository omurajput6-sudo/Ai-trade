package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TradingViewModel(
    application: Application,
    private val repository: TradingRepository
) : AndroidViewModel(application) {

    val portfolioList: StateFlow<List<UserPortfolio>> = repository.portfolio
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val signalsList: StateFlow<List<TradeSignal>> = repository.signals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tradeLogsList: StateFlow<List<TradeLog>> = repository.logs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<UserSettings> = repository.settings
        .filterNotNull()
        .stateIn(
            viewModelScope, 
            SharingStarted.WhileSubscribed(5000), 
            UserSettings()
        )

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiError = MutableStateFlow<String?>(null)
    val uiError: StateFlow<String?> = _uiError.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        // Automatically fetch signals on boot to populate the board
        refreshSignals()
        
        // If biometric security is disabled by default, we can bypass the unlock
        viewModelScope.launch {
            repository.settings.collect { currentSettings ->
                if (currentSettings != null && !currentSettings.biometricsEnabled) {
                    _isUnlocked.value = true
                }
            }
        }
    }

    fun setUnlocked(unlocked: Boolean) {
        _isUnlocked.value = unlocked
    }

    fun refreshSignals() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _uiError.value = null
            try {
                val currentSettings = settings.value
                val fetched = repository.fetchSignalsAndAnalyze(
                    riskProfile = currentSettings.riskProfile,
                    preferredMarkets = currentSettings.preferredMarkets
                )
                
                // If AI Auto trade is enabled, run auto buy/sell evaluation
                if (currentSettings.autoTradeEnabled) {
                    var tradeCount = 0
                    fetched.forEach { signal ->
                        if (signal.type == "BUY" || signal.type == "SELL") {
                            val originalPortfolio = repository.portfolio.first()
                            repository.executeAutoTrade(signal, currentSettings.minSignalAccuracy)
                            val newPortfolio = repository.portfolio.first()
                            if (originalPortfolio != newPortfolio) {
                                tradeCount++
                            }
                        }
                    }
                    if (tradeCount > 0) {
                        _statusMessage.value = "AI Bot automatically executed $tradeCount trades based on signal accuracy!"
                    }
                }
            } catch (e: Exception) {
                _uiError.value = "Failed to update signals: ${e.message}"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun submitManualTrade(symbol: String, name: String, type: String, amount: Double, price: Double) {
        viewModelScope.launch {
            repository.insertManualTrade(symbol, name, type, amount, price)
            _statusMessage.value = "Manually executed $type for $amount $symbol"
        }
    }

    fun clearTradingLogs() {
        viewModelScope.launch {
            repository.clearLogs()
            _statusMessage.value = "All trading logs successfully cleared"
        }
    }

    fun updateRiskProfile(profile: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(riskProfile = profile)
            repository.saveSettings(updated)
            _statusMessage.value = "Risk profile changed to: $profile"
            refreshSignals() // Re-analyze with new risk parameters!
        }
    }

    fun toggleAutoTrade(enabled: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(autoTradeEnabled = enabled)
            repository.saveSettings(updated)
            _statusMessage.value = if (enabled) "AI Automated Auto-Trading Enabled" else "AI Auto-Trading Disabled"
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            val updated = settings.value.copy(biometricsEnabled = enabled)
            repository.saveSettings(updated)
            // If turning off biometric, unlock immediately
            if (!enabled) {
                _isUnlocked.value = true
            }
            _statusMessage.value = if (enabled) "Biometric Lock Protection Activated" else "Security lock disabled"
        }
    }

    fun updateMinSignalAccuracy(accuracy: Int) {
        viewModelScope.launch {
            val updated = settings.value.copy(minSignalAccuracy = accuracy)
            repository.saveSettings(updated)
        }
    }

    fun updatePreferredMarkets(markets: String) {
        viewModelScope.launch {
            val updated = settings.value.copy(preferredMarkets = markets)
            repository.saveSettings(updated)
            _statusMessage.value = "Tracked markets updated"
            refreshSignals() // Fetch new market list
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // Helper calculation for portfolio total value
    val totalPortfolioValueUSD: Flow<Double> = portfolioList.map { list ->
        list.sumOf { it.amount * it.currentPrice }
    }

    // Helper calculation for total investment cost
    val totalInvestmentCostUSD: Flow<Double> = portfolioList.map { list ->
        list.sumOf { it.amount * it.averageBuyPrice }
    }

    fun isApiKeyConfgured(): Boolean {
        val apiKey = BuildConfig.GEMINI_API_KEY
        return apiKey.isNotBlank() && 
               apiKey != "MY_GEMINI_API_KEY" && 
               !apiKey.contains("placeholder", ignoreCase = true)
    }

    class Factory(
        private val application: Application,
        private val repository: TradingRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TradingViewModel::class.java)) {
                return TradingViewModel(application, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
