package com.kys.weatherhow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kys.weatherlib.WeatherLibrary

class WeatherViewModelFactory(private val weatherLibrary: WeatherLibrary) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WeatherViewModel::class.java)) {
            return WeatherViewModel(weatherLibrary) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
