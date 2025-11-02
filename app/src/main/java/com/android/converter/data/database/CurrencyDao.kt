package com.android.converter.data.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.android.converter.data.model.Currency
import kotlinx.coroutines.flow.Flow

@Dao
interface CurrencyDao {
    @Upsert
    suspend fun upsertAll(currencyEntities: List<Currency>)

    @Query("SELECT * FROM currencies")
    fun getCurrencies(): Flow<List<Currency>>
}
