package com.kys.weatherlib.model

data class WeatherDataByLatLonItem(
    val country: String,
    val lat: Double,
    val local_names: LocalNames,
    val lon: Double,
    val name: String,
    val state: String,

    var iconUrl: String? = null,
)