package com.danylom73.weatherapp.network

import android.content.Context
import android.widget.Toast
import com.danylom73.weatherapp.Constants
import com.danylom73.weatherapp.models.WeatherResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRepository(private val context: Context) {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create<WeatherService>(WeatherService::class.java)

    fun getWeatherData(
        latitude: Double,
        longitude: Double,
        onSuccess: (WeatherResponse) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (!Constants.isNetworkAvailable(context)) {
            Toast.makeText(context, "No Internet Connection", Toast.LENGTH_SHORT).show()
            return
        }

        service.getWeather(latitude, longitude, Constants.METRIC_UNIT, Constants.APP_ID)
            .enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    response.body()?.let { onSuccess(it) }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                    onFailure(t)
                }
            })
    }
}
