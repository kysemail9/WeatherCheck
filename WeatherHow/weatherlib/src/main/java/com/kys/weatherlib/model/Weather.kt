package com.kys.weatherlib.model

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("description") val description: String?,
    @SerializedName("icon") val icon: String?,
    val id: Int,
    val main: String
)