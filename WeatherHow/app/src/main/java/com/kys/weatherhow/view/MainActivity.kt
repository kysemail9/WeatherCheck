package com.kys.weatherhow.view

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.kys.weatherhow.R
import com.kys.weatherhow.databinding.ActivityMainBinding
import com.kys.weatherhow.viewmodel.WeatherViewModel
import com.kys.weatherhow.viewmodel.WeatherViewModelFactory
import com.kys.weatherlib.WeatherLibrary
import com.kys.weatherlib.model.WeatherData
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: WeatherViewModel

    lateinit var weatherLibrary: WeatherLibrary

    lateinit var textViewCityWeather: TextView
    lateinit var editCity: EditText
    private lateinit var imageViewWeatherIcon: ImageView

    private var isPermissionRequestInProgress = false

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        weatherLibrary = WeatherLibrary(this) //<-- initialize lib module here

        viewModel = ViewModelProvider(
            this,
            WeatherViewModelFactory(weatherLibrary)
        )[WeatherViewModel::class.java]

        // <--  Bind UI elements -->
        textViewCityWeather = findViewById(R.id.textViewCityWeather)
        editCity = findViewById(R.id.editCity)
        imageViewWeatherIcon = findViewById(R.id.imageViewWeatherIcon)

        viewModel.toastMessage.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        checkLiveWeatherUpdates()
        viewModel.getLastSearchedCity()
        checkIfLocation()
    }

    fun checkIfLocation() {
        try {
            if (checkLocationPermission()) {
                println(" location permission ==  YES ")
                retrieveLocationAndFetchWeatherData()
            } else {
                println(" location permission ==  NO ")
                requestLocationPermission()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    fun onSearchButtonClicked(view: View) {
        try {
            val city = editCity.text.toString()
            if (!city.isNullOrEmpty()) {
                if (isInternetConnected()) {
                    viewModel.fetchWeatherData(city.trim())
                } else {
                    Toast.makeText(this, "Please check Internet", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLiveWeatherUpdates() {
        weatherLibrary.weatherData.observe(this) { weatherData ->
            updateUI(weatherData)
        }
    }

    fun updateUI(weatherData: WeatherData) {
        try {
            val city = weatherData.cityName
            val temperature = weatherData.main?.temperature
            textViewCityWeather.text = "City : $city _  Temperature: $temperature °C"

            val imageUrl = weatherData.iconUrl
            /** Load the image with Glide and enable caching */
            Glide.with(this)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageViewWeatherIcon)
            imageViewWeatherIcon.visibility = View.VISIBLE

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Something went wrong...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("location permission given")

            //<--  Set the flag to indicate that the permission request is completed -->
            isPermissionRequestInProgress = false

            retrieveLocationAndFetchWeatherData()

        } else {
            println("DO Nothing")
        }
    }

    fun checkLocationPermission(): Boolean {

        println("CHECK LOC ")

        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission() {
        if (!isPermissionRequestInProgress) {
            isPermissionRequestInProgress = true
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            val requestCode = 1 // <-- any unique code for the request
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    fun retrieveLocationAndFetchWeatherData() {
        lifecycleScope.launch {
            try {
                if (isInternetConnected()) {
                    val location = getCurrentLocation()
                    if (location != null) {
                        val latitude = location.first
                        val longitude = location.second
                        println("{latitude : $latitude + longitude : $longitude}")

                        viewModel.fetchWeatherDataForCurrentLocation(latitude, longitude)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return suspendCoroutine { continuation ->
            if (checkLocationPermission()) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            val latitude = location.latitude
                            val longitude = location.longitude
                            continuation.resume(Pair(latitude, longitude))
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener { exception ->
                        continuation.resumeWithException(exception)
                    }
            } else {
                continuation.resume(null)
            }
        }
    }

    fun isInternetConnected(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun showToast(anyString: String?) {
        Toast.makeText(this, anyString, Toast.LENGTH_SHORT).show()
    }
}