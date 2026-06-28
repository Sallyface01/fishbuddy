package com.fishbuddy.app.domain.model

/** Fish species returned from database queries. */
data class FishSpecies(
    val commonName: String,
    val scientificName: String,
    val description: String,
    val confidence: Double,
    val typicalMethods: List<String>
)

/** Fishing method recommendation. */
data class FishingRecommendation(
    val method: String,
    val bait: String,
    val technique: String,
    val bestSeason: String,
    val bestTimeOfDay: String,
    val difficulty: String
)

/** Water body types. */
enum class WaterBodyType(val chineseName: String) {
    RIVER("河流"), LAKE("湖泊"), POND("池塘"),
    RESERVOIR("水库"), STREAM("溪流"), UNKNOWN("未知");

    companion object {
        fun from(string: String): WaterBodyType = when (string) {
            "河流", "river" -> RIVER
            "湖泊", "lake" -> LAKE
            "池塘", "pond" -> POND
            "水库", "reservoir" -> RESERVOIR
            "溪流", "stream" -> STREAM
            else -> UNKNOWN
        }
    }
}

/** Weather data from Open-Meteo API. */
data class WeatherData(
    val temperature: Double,
    val apparentTemperature: Double,
    val humidity: Int,
    val surfacePressure: Double,
    val windSpeed: Double,
    val windDirection: Int,
    val weatherCode: Int
) {
    val weatherDescription: String get() = when (weatherCode) {
        0 -> "晴天"; 1, 2, 3 -> "多云"
        45, 48 -> "雾"; 51, 53, 55 -> "毛毛雨"
        61, 63, 65 -> "雨"; 71, 73, 75 -> "雪"
        80, 81, 82 -> "阵雨"; 95, 96, 99 -> "雷暴"
        else -> "未知"
    }

    val isFishingWeather: Boolean get() =
        temperature in 5.0..35.0 && surfacePressure in 990.0..1030.0
                && weatherCode < 95 && windSpeed < 30.0

    val fishingSummary: String get() = buildString {
        if (isFishingWeather) append("天气适合钓鱼")
        else {
            append("天气条件一般")
            if (temperature <= 5) append("，水温偏低")
            if (temperature >= 35) append("，水温偏高")
            if (windSpeed >= 30) append("，风力较大")
            if (weatherCode >= 95) append("，有雷暴风险")
        }
        if (surfacePressure > 1025) append("，气压偏高鱼口轻")
        else if (surfacePressure < 1000) append("，气压偏低鱼上浮")
    }
}

/** GPS location wrapper. */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)
