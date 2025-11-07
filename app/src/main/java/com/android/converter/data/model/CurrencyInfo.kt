package com.android.converter.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyInfo(
    val code: String,
    val name: String,
    val category: String,
    val icon: String,
    val decimals: Int
)
