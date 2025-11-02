package com.android.converter.di

import android.content.Context
import android.content.SharedPreferences
import com.android.converter.data.database.CurrencyDao
import com.android.converter.data.network.ExchangeRatesService
import com.android.converter.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideCurrencyRepository(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        currencyDao: CurrencyDao,
        exchangeRatesService: ExchangeRatesService
    ) = CurrencyRepository(context, sharedPreferences, currencyDao, exchangeRatesService)
}
