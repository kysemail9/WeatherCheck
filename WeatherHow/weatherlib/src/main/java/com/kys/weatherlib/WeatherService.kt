package com.kys.weatherlib

import com.kys.weatherlib.model.WeatherData
import com.kys.weatherlib.model.WeatherDataByLatLon
import com.kys.weatherlib.utils.Constants
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET(Constants.END_URL_CITY)
    suspend fun getWeatherDataByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<WeatherData>

    @GET(Constants.END_URL_LAT_LON)
    suspend fun getWeatherDataByLatLon(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String,
        @Query("units") units: String
    ): Response<WeatherDataByLatLon>
}