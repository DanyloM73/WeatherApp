package com.danylom73.weatherapp.helpers

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import com.danylom73.weatherapp.R
import com.danylom73.weatherapp.databinding.ActivityMainBinding
import com.danylom73.weatherapp.models.WeatherResponse
import java.util.Date
import java.util.Locale

object WeatherUIRenderer {
    fun render(
        context: Context,
        binding: ActivityMainBinding,
        response: WeatherResponse,
        unit: String
    ) {
        val weather = response.weather.firstOrNull() ?: return

        binding.tvMain.text = weather.main
        binding.tvMainDescription.text = weather.description
        binding.tvTemp.text = context.getString(
            R.string.temperature,
            response.main.temp.toString(),
            unit
        )
        binding.tvHumidity.text = context.getString(
            R.string.humidity,
            response.main.humidity.toInt()
        )
        binding.tvMin.text = context.getString(
            R.string.temperature,
            response.main.temp_min.toString(),
            unit
        )
        binding.tvMax.text = context.getString(
            R.string.temperature,
            response.main.temp_max.toString(),
            unit
        )
        binding.tvSpeed.text = response.wind.speed.toString()
        binding.tvName.text = response.name
        binding.tvCountry.text = response.sys.country
        binding.tvSunriseTime.text = unixTime(response.sys.sunrise)
        binding.tvSunsetTime.text = unixTime(response.sys.sunset)

        val icon = when (weather.icon) {
            "01d" -> R.drawable.sunny
            "02d", "03d", "04d", "04n", "01n", "02n", "03n", "10n" -> R.drawable.cloud
            "10d", "11n" -> R.drawable.rain
            "11d" -> R.drawable.storm
            "13d", "13n" -> R.drawable.snowflake
            else -> R.drawable.ic_launcher_background
        }
        binding.ivMain.setImageResource(icon)
    }

    private fun unixTime(time: Long): String {
        val date = Date(time * 1000L)
        val sdf = SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}
