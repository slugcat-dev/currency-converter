package com.android.converter.data.network

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class ExchangeRatesResponse(
    val rates: Map<String, Double>
)

interface ExchangeRatesService {
    @GET("latest.json")
    suspend fun getLatest(): ExchangeRatesResponse
}
