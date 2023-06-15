package com.kys.weatherhow.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kys.weatherlib.WeatherLibrary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherViewModel constructor(private val weatherLibrary: WeatherLibrary) : ViewModel() {

    val toastMessage = MutableLiveData<String>()

    fun fetchWeatherData(city: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    weatherLibrary.fetchWeatherData(city)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage.value = "Something went wrong..."
            }
        }
    }

    fun fetchWeatherDataForCurrentLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    weatherLibrary.fetchWeatherDataForCurrentLocation(latitude, longitude)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                toastMessage.value = "Something went wrong..."
            }
        }
    }

    internal fun getLastSearchedCity() {
        try {
            val lastSearchedCity = weatherLibrary.getLastSearchedCity()
            if (!lastSearchedCity.isNullOrEmpty()) {
                weatherLibrary.fetchWeatherData(lastSearchedCity.trim())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastMessage.value = "Something went wrong..."
        }
    }
}