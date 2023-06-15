package com.kys.weatherlib.model

import com.google.gson.annotations.SerializedName

data class WeatherData(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    @SerializedName("main") val main: Main?,
    @SerializedName("name") val cityName: String?,
    val sys: Sys,
    val timezone: Int,
    val visibility: Int,
    @SerializedName("weather") val weatherList: List<Weather>?,
    val wind: Wind,

    var iconUrl: String? = null,
)