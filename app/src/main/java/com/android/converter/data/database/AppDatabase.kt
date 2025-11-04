package com.android.converter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.converter.data.model.Currency

@Database([Currency::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}
