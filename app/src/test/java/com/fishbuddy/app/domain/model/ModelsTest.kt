package com.fishbuddy.app.domain.model

import org.junit.Assert.*
import org.junit.Test

class ModelsTest {

    // === WaterBodyType.from() ===

    @Test
    fun `WaterBodyType from Chinese names`() {
        assertEquals(WaterBodyType.RIVER, WaterBodyType.from("河流"))
        assertEquals(WaterBodyType.LAKE, WaterBodyType.from("湖泊"))
        assertEquals(WaterBodyType.POND, WaterBodyType.from("池塘"))
        assertEquals(WaterBodyType.RESERVOIR, WaterBodyType.from("水库"))
        assertEquals(WaterBodyType.STREAM, WaterBodyType.from("溪流"))
    }

    @Test
    fun `WaterBodyType from English names`() {
        assertEquals(WaterBodyType.RIVER, WaterBodyType.from("river"))
        assertEquals(WaterBodyType.LAKE, WaterBodyType.from("lake"))
        assertEquals(WaterBodyType.POND, WaterBodyType.from("pond"))
        assertEquals(WaterBodyType.RESERVOIR, WaterBodyType.from("reservoir"))
        assertEquals(WaterBodyType.STREAM, WaterBodyType.from("stream"))
    }

    @Test
    fun `WaterBodyType unknown for unrecognized input`() {
        assertEquals(WaterBodyType.UNKNOWN, WaterBodyType.from(""))
        assertEquals(WaterBodyType.UNKNOWN, WaterBodyType.from("ocean"))
        assertEquals(WaterBodyType.UNKNOWN, WaterBodyType.from("大海"))
    }

    @Test
    fun `WaterBodyType Chinese names are correct`() {
        assertEquals("河流", WaterBodyType.RIVER.chineseName)
        assertEquals("湖泊", WaterBodyType.LAKE.chineseName)
        assertEquals("池塘", WaterBodyType.POND.chineseName)
        assertEquals("水库", WaterBodyType.RESERVOIR.chineseName)
        assertEquals("溪流", WaterBodyType.STREAM.chineseName)
        assertEquals("未知", WaterBodyType.UNKNOWN.chineseName)
    }

    // === WeatherData ===

    @Test
    fun `WeatherData isFishingWeather good conditions`() {
        val good = WeatherData(
            temperature = 22.0, apparentTemperature = 23.0,
            humidity = 60, surfacePressure = 1013.0,
            windSpeed = 10.0, windDirection = 180, weatherCode = 1
        )
        assertTrue(good.isFishingWeather)
        assertEquals("多云", good.weatherDescription) // code 1 = 多云
    }

    @Test
    fun `WeatherData isFishingWeather cold temperature`() {
        val cold = WeatherData(
            temperature = 2.0, apparentTemperature = 1.0,
            humidity = 40, surfacePressure = 1015.0,
            windSpeed = 5.0, windDirection = 90, weatherCode = 0
        )
        assertFalse(cold.isFishingWeather)
        assertTrue(cold.fishingSummary.contains("水温偏低"))
    }

    @Test
    fun `WeatherData isFishingWeather thunderstorm`() {
        val storm = WeatherData(
            temperature = 25.0, apparentTemperature = 26.0,
            humidity = 80, surfacePressure = 1005.0,
            windSpeed = 12.0, windDirection = 270, weatherCode = 95
        )
        assertFalse(storm.isFishingWeather)
        assertEquals("雷暴", storm.weatherDescription)
    }

    @Test
    fun `WeatherData fishingSummary contains pressure hint`() {
        val highPressure = WeatherData(
            temperature = 20.0, apparentTemperature = 21.0,
            humidity = 50, surfacePressure = 1030.0,
            windSpeed = 8.0, windDirection = 0, weatherCode = 2
        )
        assertTrue(highPressure.fishingSummary.contains("气压偏高"))

        val lowPressure = WeatherData(
            temperature = 20.0, apparentTemperature = 21.0,
            humidity = 50, surfacePressure = 995.0,
            windSpeed = 8.0, windDirection = 0, weatherCode = 2
        )
        assertTrue(lowPressure.fishingSummary.contains("气压偏低"))
    }

    // === FishingRecommendation ===

    @Test
    fun `FishingRecommendation construction`() {
        val rec = FishingRecommendation(
            method = "台钓", bait = "商品饵", technique = "调四钓二",
            bestSeason = "春秋", bestTimeOfDay = "清晨", difficulty = "初级"
        )
        assertEquals("台钓", rec.method)
        assertEquals("春秋", rec.bestSeason)
    }

    // === UserLocation ===

    @Test
    fun `UserLocation default name is null`() {
        val loc = UserLocation(39.904, 116.407)
        assertEquals(39.904, loc.latitude, 0.001)
        assertEquals(116.407, loc.longitude, 0.001)
        assertNull(loc.name)
    }

    @Test
    fun `UserLocation with name`() {
        val loc = UserLocation(39.904, 116.407, "北京")
        assertEquals("北京", loc.name)
    }
}
