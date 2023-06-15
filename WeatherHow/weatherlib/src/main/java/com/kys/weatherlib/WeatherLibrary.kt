package com.kys.weatherlib

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kys.weatherlib.model.WeatherData
import com.kys.weatherlib.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherLibrary(private val context: Context) {

    private val apiKey = Constants.API_KEY
    private val baseUrl = Constants.BASE_URL
    private val iconBaseUrl = Constants.ICON_BASE_URL
    private val sharedPrefsKey = Constants.SHARED_PREFS_KEY

    private val retrofit: Retrofit
    private val weatherService: WeatherService
    private val sharedPreferences: SharedPreferences

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    init {
        val httpClient = OkHttpClient.Builder().build()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherService = retrofit.create(WeatherService::class.java)
        sharedPreferences = context.getSharedPreferences("WeatherPrefs", Context.MODE_PRIVATE)
    }

    fun fetchWeatherData(city: String) {
        GlobalScope.launch {
            val response = weatherService.getWeatherDataByCity(city, apiKey, Constants.UNIT)
            if (response.isSuccessful) {

                saveLastSearchedCity(city)

                val weatherData = response.body()
                println("API Data: ${weatherData.toString()}")
                weatherData?.let {
                    fetchWeatherIcon(weatherData.weatherList?.get(0)?.icon) { iconUrl ->
                        weatherData.iconUrl = iconUrl
                        println("Icon Url : ${weatherData.iconUrl}")
                        _weatherData.postValue(weatherData)
                    }
                }
            } else {
                println("API Data failed : ${response.message()}")
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private suspend fun fetchWeatherIcon(iconCode: String?, callback: (String?) -> Unit) {
        withContext(Dispatchers.IO) {
            iconCode?.let {
                val iconUrl = "$iconBaseUrl$iconCode@2x.png"
                println("Icon Url :: $iconUrl")
                callback(iconUrl)
            } ?: callback(null)
        }
    }

    fun fetchWeatherDataForCurrentLocation(latitude: Double, longitude: Double) {
        GlobalScope.launch {
            val response = weatherService.getWeatherDataByLatLon(
                latitude,
                longitude,
                Constants.LIMIT,
                apiKey,
                Constants.UNIT
            )
            if (response.isSuccessful) {
                val weatherData = response.body()
                println("API Data LAT LON: ${weatherData.toString()}")
                weatherData?.let {
                    fetchWeatherIcon(weatherData?.get(0)?.name) {
                        val name = weatherData?.get(0)?.name
                        if (name != null) {
                            fetchWeatherData(name)
                        }
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, response.message(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveLastSearchedCity(city: String) {
        sharedPreferences.edit().putString(sharedPrefsKey, city).apply()
    }

    fun getLastSearchedCity(): String? {
        return sharedPreferences.getString(sharedPrefsKey, null)
    }
}
