package com.fishbuddy.app.service

import android.content.Context
import com.fishbuddy.app.domain.model.FishSpecies
import com.fishbuddy.app.domain.model.FishingRecommendation
import com.fishbuddy.app.domain.model.UserLocation
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*

/** Loads fish_database.json and provides GPS→city→species queries. */
class FishDatabaseService(private val context: Context) {

    private var db: FishDatabaseJSON? = null

    /** All region names for the guide filter. */
    val allRegions: List<String> get() = db?.regions?.keys?.sorted() ?: emptyList()

    /** All species details for the guide. */
    val allSpeciesDetails: List<SpeciesDetailJSON>
        get() = db?.species?.values?.sortedBy { it.commonName } ?: emptyList()

    init {
        loadDatabase()
    }

    private fun loadDatabase() {
        try {
            val json = context.assets.open("data/fish_database.json")
                .bufferedReader().use { it.readText() }
            db = Gson().fromJson(json, FishDatabaseJSON::class.java)
        } catch (e: Exception) {
            db = null
        }
    }

    /** Main query: GPS → city → species + recommendations. */
    fun query(waterType: String, location: UserLocation?): Pair<List<FishSpecies>, List<FishingRecommendation>> {
        val d = db ?: return Pair(emptyList(), emptyList())

        val context = resolveCityContext(d, location)
        val speciesNames = resolveSpeciesNames(d, context, waterType)

        val species = speciesNames.mapNotNull { name ->
            d.species[name]?.let { detail ->
                FishSpecies(
                    commonName = detail.commonName,
                    scientificName = detail.scientificName,
                    description = detail.description,
                    confidence = when (context.level) {
                        CityLevel.CITY -> 0.85
                        CityLevel.PROVINCE -> 0.70
                        CityLevel.REGION -> 0.60
                        else -> 0.50
                    },
                    typicalMethods = detail.typicalMethods,
                    imageUrl = detail.imageUrl
                )
            }
        }.sortedByDescending { it.confidence }.take(8)

        val recommendations = resolveRecommendations(species.map { it.commonName }, waterType)
        return Pair(species, recommendations)
    }

    fun resolveLocationName(location: UserLocation?): String? {
        if (location == null || db == null) return null
        val ctx = resolveCityContext(db!!, location)
        return ctx.displayName
    }

    fun speciesExistsInWaterType(speciesName: String, waterType: String): Boolean {
        val d = db ?: return false
        for (region in d.regions.values)
            for (province in region.provinces.values)
                for (city in province.cities.values)
                    if (city.waterTypes[waterType]?.contains(speciesName) == true)
                        return true
        return false
    }

    fun speciesExistsInRegion(speciesName: String, region: String): Boolean {
        val d = db ?: return false
        val regionData = d.regions[region] ?: return false
        for (province in regionData.provinces.values)
            for (city in province.cities.values)
                for (species in city.waterTypes.values)
                    if (species.contains(speciesName)) return true
        return false
    }

    // --- Internal ---

    private enum class CityLevel { CITY, PROVINCE, REGION, FALLBACK }

    private data class CityContext(
        val level: CityLevel, val displayName: String,
        val regionName: String?, val provinceName: String?, val cityName: String?,
        val cityData: CityJSON?
    )

    private fun resolveCityContext(db: FishDatabaseJSON, loc: UserLocation?): CityContext {
        if (loc == null) return CityContext(CityLevel.FALLBACK, "未知位置", null, null, null, null)

        var nearest: Triple<String, String, CityJSON>? = null
        var nearestDist = Double.MAX_VALUE

        for ((regionName, region) in db.regions)
            for ((provinceName, province) in region.provinces)
                for ((cityName, city) in province.cities) {
                    val d = haversine(loc.latitude, loc.longitude, city.center[0], city.center[1])
                    if (d < 80.0 && d < nearestDist) {
                        nearest = Triple(cityName, provinceName, city)
                        nearestDist = d
                    }
                }

        if (nearest != null)
            return CityContext(CityLevel.CITY, "${nearest.first} · ${nearest.second}",
                null, nearest.second, nearest.first, nearest.third)

        // Fallback to nearest province
        var closestProv: String? = null; var closestReg: String? = null; var closestDist = Double.MAX_VALUE
        for ((rname, region) in db.regions)
            for ((pname, province) in region.provinces)
                for ((_, city) in province.cities) {
                    val d = haversine(loc.latitude, loc.longitude, city.center[0], city.center[1])
                    if (d < closestDist) { closestDist = d; closestProv = pname; closestReg = rname }
                }

        if (closestProv != null)
            return CityContext(CityLevel.REGION, "$closestProv（最近匹配）", closestReg, closestProv, null, null)

        return CityContext(CityLevel.FALLBACK, "未知位置", null, null, null, null)
    }

    private fun resolveSpeciesNames(db: FishDatabaseJSON, ctx: CityContext, waterType: String): List<String> {
        ctx.cityData?.waterTypes?.get(waterType)?.let { if (it.isNotEmpty()) return it }

        if (ctx.provinceName != null && ctx.regionName != null) {
            val all = mutableSetOf<String>()
            db.regions[ctx.regionName]?.provinces?.get(ctx.provinceName)?.cities?.values?.forEach {
                it.waterTypes[waterType]?.let { s -> all.addAll(s) }
            }
            if (all.isNotEmpty()) return all.sorted()
        }

        if (ctx.regionName != null) {
            val all = mutableSetOf<String>()
            db.regions[ctx.regionName]?.provinces?.values?.forEach { prov ->
                prov.cities.values.forEach { it.waterTypes[waterType]?.let { s -> all.addAll(s) } }
            }
            if (all.isNotEmpty()) return all.sorted()
        }

        return db.species.keys.sorted()
    }

    private fun resolveRecommendations(speciesNames: List<String>, waterType: String): List<FishingRecommendation> {
        val methods = FishingMethodsService(context).loadMethods() ?: return emptyList()
        val seen = mutableSetOf<String>()
        return methods.methods.values.filter { m ->
            m.suitableSpecies.any { speciesNames.contains(it) } &&
            m.suitableWater.contains(waterType) &&
            seen.add(m.name)
        }.map { m ->
            FishingRecommendation(
                method = m.name, bait = m.bait.joinToString("、"),
                technique = m.technique,
                bestSeason = m.bestSeason ?: "全年",
                bestTimeOfDay = m.bestTime ?: "全天",
                difficulty = m.difficulty
            )
        }
    }

    private fun haversine(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        return r * 2 * atan2(sqrt(a), sqrt(1 - a))
    }
}

/** Loads fishing_methods.json. */
class FishingMethodsService(private val context: Context) {
    fun loadMethods(): FishingMethodsJSON? {
        return try {
            val json = context.assets.open("data/fishing_methods.json")
                .bufferedReader().use { it.readText() }
            Gson().fromJson(json, FishingMethodsJSON::class.java)
        } catch (e: Exception) { null }
    }
}

// --- JSON Model Types ---

data class FishDatabaseJSON(
    val version: String,
    val regions: Map<String, RegionJSON>,
    val species: Map<String, SpeciesDetailJSON>
)
data class RegionJSON(val provinces: Map<String, ProvinceJSON>)
data class ProvinceJSON(val cities: Map<String, CityJSON>)
data class CityJSON(val center: List<Double>, val description: String, val waterTypes: Map<String, List<String>>)
data class SpeciesDetailJSON(
    val commonName: String, val englishName: String, val scientificName: String,
    val description: String, val typicalMethods: List<String>,
    val bestBait: List<String>, val bestSeason: String, val bestTime: String, val avgSize: String,
    val imageUrl: String? = null
)
data class FishingMethodsJSON(val version: String, val methods: Map<String, MethodDetailJSON>)
data class MethodDetailJSON(
    val name: String, val suitableSpecies: List<String>, val suitableWater: List<String>,
    val bait: List<String>, val technique: String, val difficulty: String,
    val bestSeason: String? = null, val bestTime: String? = null, val equipment: String = ""
)
