package com.android.converter.ui

import android.content.SharedPreferences
import android.icu.text.NumberFormat
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.converter.data.model.Currency
import com.android.converter.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject
import kotlin.system.measureTimeMillis

@HiltViewModel
class AppViewModel @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    private val state = MutableStateFlow(AppState())
    val uiState = state.asStateFlow()

    init { viewModelScope.launch {
        // Load the last used currencies from the preferences
        val fromCurrencyCode = sharedPreferences.getString("from-currency", "USD") ?: "USD"
        val toCurrencyCode = sharedPreferences.getString("to-currency", "EUR") ?: "EUR"

        // Init the currency repo and app state
        currencyRepository.init()
        currencyRepository.getCurrencies().collect { currencies ->
            state.update { it.copy(
                ready = true,
                currencies = currencies,
                fromCurrency = currencies.find { currency -> currency.code == fromCurrencyCode },
                toCurrency = currencies.find { currency -> currency.code == toCurrencyCode }
            ) }
        }

        refreshCurrencies()
    } }

    // Fetch current exchange rates
    suspend fun refreshCurrencies() {
        state.update { it.copy(refreshing = true) }

        // Add an artificial delay if the refresh takes less than a second to
        // avoid quick changes in the UI
        val timeElapsed = measureTimeMillis {
            currencyRepository.refreshCurrencies()
        }

        if (timeElapsed < 1000L)
            delay(1000L - timeElapsed)

        state.update { it.copy(refreshing = false) }
    }

    // Set the locale used for number formatting
    fun setLocale(locale: Locale) {
        state.update { it.copy(locale = locale) }
    }

    // Update the specified currency
    fun setCurrency(type: String, currency: Currency) {
        val from = type == "from"

        val otherCurrency = when {
            from -> state.value.fromCurrency
            else -> state.value.toCurrency
        }

        // If both currencies would end up being the same, swap them instead
        if (currency == otherCurrency)
            return swapCurrencies()

        state.update { when {
            from -> it.copy(fromCurrency = currency)
            else -> it.copy(toCurrency = currency)
        } }

        saveCurrencyPreferences()
        convert()
    }

    // Swap the selected currencies and amounts
    fun swapCurrencies() {
        val currentState = state.value

        state.update { it.copy(
            fromCurrency = currentState.toCurrency,
            toCurrency = currentState.fromCurrency,
            fromAmount = currentState.toAmount
        ) }

        saveCurrencyPreferences()
        convert()
    }

    // Save the selected currency pair to the preferences
    private fun saveCurrencyPreferences() = sharedPreferences.edit {
        putString("from-currency", state.value.fromCurrency!!.code)
        putString("to-currency", state.value.toCurrency!!.code)
    }

    // Convert the entered amount with the conversion rate of the currency pair
    private fun convert() {
        val fromCurrency = requireNotNull(state.value.fromCurrency)
        val toCurrency = requireNotNull(state.value.toCurrency)
        val fromAmount = state.value.fromAmount

        if (fromCurrency.rate == 0.0)
            throw IllegalStateException("Rate may not be zero")

        val number = NumberFormat.getNumberInstance(state.value.locale)

        // Calculate the conversion rate of the selected currency pair
        val rate = toCurrency.rate / fromCurrency.rate

        // Convert the entered amount
        val fromAmountValue = number.parse(fromAmount).toDouble()
        val toAmountValue = fromAmountValue * rate

        // Drop the decimals if the amount is more than one thousand
        number.setMaximumFractionDigits(if (toAmountValue < 1000) toCurrency.decimals else 0)

        // Update the state with the formatted value
        state.update { it.copy(
            toAmount = number.format(toAmountValue)
        ) }
    }
}
