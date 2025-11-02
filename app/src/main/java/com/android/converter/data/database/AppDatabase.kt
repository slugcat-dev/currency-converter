package com.android.converter.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.converter.data.model.Currency

@Database(entities = [Currency::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
}
