import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.kys.weatherhow.view.MainActivity
import com.kys.weatherhow.viewmodel.WeatherViewModel
import com.kys.weatherlib.WeatherLibrary
import com.kys.weatherlib.model.Clouds
import com.kys.weatherlib.model.Coord
import com.kys.weatherlib.model.Main
import com.kys.weatherlib.model.Sys
import com.kys.weatherlib.model.Weather
import com.kys.weatherlib.model.WeatherData
import com.kys.weatherlib.model.Wind
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

class MainActivityTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var weatherLibrary: WeatherLibrary

    @Mock
    private lateinit var viewModel: WeatherViewModel

    @Mock
    private lateinit var weatherDataObserver: Observer<WeatherData>

    private lateinit var mainActivity: MainActivity

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        mainActivity = MainActivity()
        mainActivity.weatherLibrary = weatherLibrary
        mainActivity.viewModel = viewModel

        mainActivity.weatherLibrary.weatherData.observeForever(weatherDataObserver)
    }

    @Test
    fun `updateUI should update UI elements with WeatherData`() {

        val main = Main(25.0, 0, 25.0, 0, 25.0, 25.0)
        val name = "Dubai"
        val weatherList: List<Weather> = listOf(Weather("ETC", "01n", 0, ""))

        val weatherData = WeatherData(
            "", Clouds(0),
            0, Coord(0.0, 0.0), 0, 0, main, name,
            Sys("", 0, 0, 0, 0), 0, 0,
            weatherList, Wind(0, 0.0), ""
        )

        mainActivity.updateUI(weatherData)
        assertEquals(
            "City : $name _  Temperature: ${main.temperature} °C",
            mainActivity.textViewCityWeather.text.toString()
        )
        verify(weatherLibrary, never()).fetchWeatherData(anyString())
    }

    @Test
    fun `onSearchButtonClicked should fetch weather data when city is provided and internet is connected`() {
        val cityName = "Dubai"
        mainActivity.editCity.setText(cityName)

        `when`(mainActivity.isInternetConnected()).thenReturn(true)

        mainActivity.onSearchButtonClicked(mock(View::class.java))

        verify(viewModel).fetchWeatherData(cityName.trim())
        verify(mainActivity, never()).showToast(anyString())
    }

    @Test
    fun `onSearchButtonClicked should show toast message when city is not provided`() {
        mainActivity.editCity.text = null

        mainActivity.onSearchButtonClicked(mock(View::class.java))

        verify(mainActivity).showToast("Please enter a city name")
        verify(viewModel, never()).fetchWeatherData(anyString())
    }

    @Test
    fun `onSearchButtonClicked should show toast message when internet is not connected`() {
        val cityName = "Test City"
        mainActivity.editCity.setText(cityName)

        `when`(mainActivity.isInternetConnected()).thenReturn(false)

        mainActivity.onSearchButtonClicked(mock(View::class.java))

        verify(mainActivity).showToast("Please check Internet")
        verify(viewModel, never()).fetchWeatherData(anyString())
    }

    @Test
    fun `checkIfLocation should request location permission when not granted`() {
        `when`(mainActivity.checkLocationPermission()).thenReturn(false)

        mainActivity.checkIfLocation()

        verify(mainActivity).requestLocationPermission()
        verify(mainActivity, never()).retrieveLocationAndFetchWeatherData()
    }

    @Test
    fun `checkIfLocation should retrieve location and fetch weather data when location permission is granted`() {
        `when`(mainActivity.checkLocationPermission()).thenReturn(true)
        `when`(mainActivity.isInternetConnected()).thenReturn(true)

        mainActivity.checkIfLocation()

        verify(mainActivity).retrieveLocationAndFetchWeatherData()
        verify(mainActivity, never()).requestLocationPermission()
    }
}
