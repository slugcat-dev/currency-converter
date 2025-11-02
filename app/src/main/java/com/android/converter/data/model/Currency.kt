package com.android.converter.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "currencies")
data class Currency (
    @PrimaryKey
    val code: String,
    val name: String,
    val icon: String,
    val rate: Double,
    val decimals: Int
)
