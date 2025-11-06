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
import java.text.DecimalFormatSymbols
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
        convertToAmount()
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
        convertToAmount()
    }

    // Save the selected currency pair to the preferences
    private fun saveCurrencyPreferences() = sharedPreferences.edit {
        putString("from-currency", state.value.fromCurrency!!.code)
        putString("to-currency", state.value.toCurrency!!.code)
    }

    // Handle keyboard input
    fun onKeyPress(key: String) {
        val fromCurrency = requireNotNull(state.value.fromCurrency)
        val fromAmount = state.value.fromAmount

        val formatSymbols = DecimalFormatSymbols.getInstance(state.value.locale)
        val decimalSeparator = formatSymbols.decimalSeparator

        val intPart = fromAmount.substringBefore(decimalSeparator)
        val decPart = fromAmount.substringAfter(decimalSeparator)
        val hasDecSeparator = fromAmount.contains(decimalSeparator)

        val separatorAllowed = !hasDecSeparator && fromCurrency.decimals > 0

        val digitAllowed = when {
            hasDecSeparator -> decPart.length < fromCurrency.decimals
            else -> intPart.length < 15
        }

        // Update the amount based on the pressed key
        val updatedAmount = when {
            // Backspace
            key == "C" -> "0"
            key == "<" -> fromAmount.dropLast(1)

            // Separator
            key == "." -> {
                if (separatorAllowed) fromAmount + decimalSeparator
                else fromAmount
            }

            // Digits
            fromAmount == "0" -> key
            digitAllowed -> fromAmount + key

            // Key not allowed
            else -> fromAmount
        }

        // Update the state with the formatted amount
        state.update { it.copy(
            fromAmount = formatFromAmount(updatedAmount, decimalSeparator)
        ) }

        convertToAmount()
    }

    // Format the entered amount to include grouping separators
    private fun formatFromAmount(fromAmount: String, decimalSeparator: Char): String {
        if (fromAmount.isEmpty())
            return "0"

        val intPart = fromAmount.substringBefore(decimalSeparator)
        val decPart = fromAmount.substringAfter(decimalSeparator)
        val hasDecSeparator = fromAmount.contains(decimalSeparator)

        // Format the integer part
        val numberFormat = NumberFormat.getNumberInstance(state.value.locale)

        val intValue = numberFormat.parse(intPart).toLong()
        val formattedIntPart = numberFormat.format(intValue)

        // Reassemble the parts into the final value
        return buildString {
            append(formattedIntPart)

            if (hasDecSeparator) {
                append(decimalSeparator)
                append(decPart)
            }
        }
    }

    // Convert the entered amount with the conversion rate of the currency pair
    private fun convertToAmount() {
        val fromCurrency = requireNotNull(state.value.fromCurrency)
        val toCurrency = requireNotNull(state.value.toCurrency)
        val fromAmount = state.value.fromAmount

        if (fromCurrency.rate == 0.0)
            throw IllegalStateException("Rate may not be zero")

        // Calculate the conversion rate of the selected currency pair
        val rate = toCurrency.rate / fromCurrency.rate

        // Convert the entered amount
        val numberFormat = NumberFormat.getNumberInstance(state.value.locale)

        val fromAmountValue = numberFormat.parse(fromAmount).toDouble()
        val toAmountValue = fromAmountValue * rate

        // Drop the decimals if the amount is more than one thousand
        numberFormat.setMaximumFractionDigits(if (toAmountValue < 1000) toCurrency.decimals else 0)

        // Update the state with the formatted amount
        state.update { it.copy(
            toAmount = numberFormat.format(toAmountValue)
        ) }
    }
}
