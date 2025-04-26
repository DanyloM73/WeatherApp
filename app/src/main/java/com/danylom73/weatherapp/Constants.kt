package com.danylom73.weatherapp

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {
    const val APP_ID = "ff6c15e95e8e926ce9186c884f1f881c"
    const val BASE_URL = "https://api.openweathermap.org/data/"
    const val METRIC_UNIT = "metric"
    const val PREFERENCE_NAME = "WeatherAppPreference"
    const val WEATHER_RESPONSE_DATA = "weather_response_data"

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network)
            ?: return false

        return when {
            activeNetwork
                .hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork
                .hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork
                .hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
}