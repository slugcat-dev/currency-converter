package com.android.converter

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType

object Utils {
    private val jsonContentType = "application/json".toMediaType()

    val json = Json { ignoreUnknownKeys = true }
    val jsonConverterFactory = json.asConverterFactory(contentType = jsonContentType)

    fun readAsset(context: Context, fileName: String) =
        context.assets.open(fileName).bufferedReader().use { it.readText() }
}
