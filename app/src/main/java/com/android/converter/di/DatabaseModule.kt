package com.android.converter.di

import android.content.Context
import androidx.room.Room
import com.android.converter.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = Room
        .databaseBuilder(context, AppDatabase::class.java, "currency-database")
        .build()

    @Provides
    @Singleton
    fun provideCurrencyDao(db: AppDatabase) = db.currencyDao()
}
