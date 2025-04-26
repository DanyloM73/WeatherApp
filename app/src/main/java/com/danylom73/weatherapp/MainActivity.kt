package com.danylom73.weatherapp

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.danylom73.weatherapp.databinding.ActivityMainBinding
import com.danylom73.weatherapp.models.WeatherResponse
import com.danylom73.weatherapp.network.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date
import java.util.Locale
import androidx.core.content.edit
import com.danylom73.weatherapp.helpers.LocationHelper
import com.danylom73.weatherapp.helpers.WeatherUIRenderer
import com.danylom73.weatherapp.network.WeatherRepository

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationHelper: LocationHelper
    private lateinit var weatherRepository: WeatherRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationHelper = LocationHelper(this, fusedLocationClient)
        weatherRepository = WeatherRepository(this)
        sharedPreferences = getSharedPreferences(
            Constants.PREFERENCE_NAME, MODE_PRIVATE
        )

        val stringResponse = sharedPreferences.getString(
            Constants.WEATHER_RESPONSE_DATA, ""
        )

        if (!stringResponse.isNullOrEmpty()) {
            WeatherUIRenderer.render(
                this,
                binding,
                Gson().fromJson(
                    stringResponse, WeatherResponse::class.java
                ),
                getUnit()
            )
        }

        checkLocationAndRequestData()

        binding.ibRefresh.setOnClickListener {
            checkLocationAndRequestData()
        }
    }

    private fun checkLocationAndRequestData() {
        if (!locationHelper.isLocationEnabled()) {
            Toast.makeText(this, "Location provider off", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            return
        }

        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        requestWeatherUpdate()
                    } else if (report.isAnyPermissionPermanentlyDenied) {
                        showDialogForPermissions()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showDialogForPermissions()
                }
            }).check()
    }

    @RequiresPermission(allOf = [
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ])
    private fun requestWeatherUpdate() {
        locationHelper.requestSingleUpdate { lat, lon ->
            weatherRepository.getWeatherData(
                lat, lon,
                onSuccess = {
                    sharedPreferences.edit {
                        putString(Constants.WEATHER_RESPONSE_DATA, Gson().toJson(it))
                    }
                    WeatherUIRenderer.render(this, binding, it, getUnit())
                },
                onFailure = {
                    Log.e("WeatherError", it.message ?: "Unknown error")
                }
            )
        }
    }

    private fun getUnit(): String {
        val config = application.resources.configuration.locales[0].country
        return if (config == "US" || config == "LR" || config == "MM") "°F" else "°C"
    }

    private fun showDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("You disabled location permissions")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
