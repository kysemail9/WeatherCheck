package com.kys.weatherlib.model

import com.google.gson.annotations.SerializedName

data class Main(
    @SerializedName("temp") val temperature: Double?,
    @SerializedName("humidity") val humidity: Int?,
    val feels_like: Double,
    val pressure: Int,
    val temp_max: Double,
    val temp_min: Double
)