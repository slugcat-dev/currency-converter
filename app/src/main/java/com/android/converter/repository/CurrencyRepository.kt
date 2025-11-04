package com.android.converter.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.android.converter.Utils.json
import com.android.converter.Utils.readAsset
import com.android.converter.data.database.CurrencyDao
import com.android.converter.data.model.Currency
import com.android.converter.data.network.ExchangeRatesResponse
import com.android.converter.data.network.ExchangeRatesService
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

class CurrencyRepository(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val currencyDao: CurrencyDao,
    private val exchangeRatesService: ExchangeRatesService
) {
    // Init the database when the user opens the app for the first time
    suspend fun init() {
        if (currencyDao.getCurrencies().first().isEmpty())
            currencyDao.upsertAll(loadDefaultCurrencyData())
    }

    // Get all currencies in the database
    fun getCurrencies() = currencyDao.getCurrencies()

    // Fetch current exchange rates if the last update was more than an
    // hour ago
    suspend fun refreshCurrencies(): Boolean {
        val lastUpdate = sharedPreferences.getLong("last-update", 0)

        if (System.currentTimeMillis() - lastUpdate < 3600000L)
            return true

        try {
            val rates = exchangeRatesService.getLatest().rates
            val currencies = currencyDao.getCurrencies().first()

            val updatedCurrencies = currencies.map { currency ->
                currency.copy(rate = rates[currency.code] ?: currency.rate)
            }

            currencyDao.upsertAll(updatedCurrencies)
            sharedPreferences.edit { putLong("last-update", System.currentTimeMillis()) }

            return true
        } catch (exception: Exception) {
            exception.printStackTrace()
        }

        return false
    }

    // Load the currency list and exchange rates from the asset directory
    private fun loadDefaultCurrencyData(): List<Currency> {
        @Serializable
        data class CurrencyInfo(val code: String, val name: String, val icon: String, val decimals: Int)

        val currenciesJson = readAsset(context, "currencies.json")
        val ratesJson = readAsset(context, "rates.json")

        val currencies = json.decodeFromString<List<CurrencyInfo>>(currenciesJson)
        val rates = json.decodeFromString<ExchangeRatesResponse>(ratesJson).rates

        return currencies.map { currency ->
            Currency(
                code = currency.code,
                name = currency.name,
                icon = currency.icon,
                rate = rates[currency.code] ?: 1.0,
                decimals = currency.decimals
            )
        }
    }
}
