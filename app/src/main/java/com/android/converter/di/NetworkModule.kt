package com.android.converter.di

import com.android.converter.data.network.ExchangeRatesService
import com.android.converter.Utils.jsonConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl("https://cdn.moneyconvert.net/api/")
        .addConverterFactory(jsonConverterFactory)
        .build()

    @Provides
    @Singleton
    fun provideExchangeRatesService(retrofit: Retrofit): ExchangeRatesService =
        retrofit.create(ExchangeRatesService::class.java)
}
