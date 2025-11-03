package com.android.converter.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.converter.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    private val state = MutableStateFlow(AppState())
    val uiState = state.asStateFlow()

    init { viewModelScope.launch {
        initCurrencies()
    } }

    private suspend fun initCurrencies() {
        val fromCurrencyCode = sharedPreferences.getString("from-currency", "USD") ?: "USD"
        val toCurrencyCode = sharedPreferences.getString("to-currency", "EUR") ?: "EUR"

        currencyRepository.init()
        currencyRepository.getCurrencies().collect { currencies ->
            state.update { it.copy(
                ready = true,
                currencies = currencies,
                fromCurrency = currencies.find { currency -> currency.code == fromCurrencyCode },
                toCurrency = currencies.find { currency -> currency.code == toCurrencyCode }
            ) }
        }
    }
}
