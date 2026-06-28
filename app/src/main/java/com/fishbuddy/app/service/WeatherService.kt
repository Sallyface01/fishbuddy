package com.fishbuddy.app.service

import com.fishbuddy.app.domain.model.WeatherData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/** Fetches weather from free Open-Meteo API. No API key needed. */
class WeatherService {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchWeather(latitude: Double, longitude: Double): WeatherData? {
        return withContext(Dispatchers.IO) {
            try {
                val url = "https://api.open-meteo.com/v1/forecast?" +
                        "latitude=$latitude&longitude=$longitude" +
                        "&current=temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m" +
                        "&timezone=auto"
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: return@withContext null

                val json = gson.fromJson(body, Map::class.java)
                val current = json["current"] as? Map<*, *> ?: return@withContext null

                WeatherData(
                    temperature = (current["temperature_2m"] as? Number)?.toDouble() ?: 0.0,
                    apparentTemperature = (current["apparent_temperature"] as? Number)?.toDouble() ?: 0.0,
                    humidity = (current["relative_humidity_2m"] as? Number)?.toInt() ?: 0,
                    surfacePressure = (current["surface_pressure"] as? Number)?.toDouble() ?: 1013.0,
                    windSpeed = (current["wind_speed_10m"] as? Number)?.toDouble() ?: 0.0,
                    windDirection = (current["wind_direction_10m"] as? Number)?.toInt() ?: 0,
                    weatherCode = (current["weather_code"] as? Number)?.toInt() ?: 0
                )
            } catch (e: Exception) { null }
        }
    }
}
