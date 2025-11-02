package com.android.converter.ui

import com.android.converter.data.model.Currency

data class AppState(
    val ready: Boolean = false,
    val refreshing: Boolean = false,
    val currencies: List<Currency> = emptyList(),
    val fromCurrency: Currency? = null,
    val toCurrency: Currency? = null,
    val fromAmount: String = "0",
    val toAmount: String = "0"
)
