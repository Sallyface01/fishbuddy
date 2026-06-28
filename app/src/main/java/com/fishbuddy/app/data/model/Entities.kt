package com.fishbuddy.app.data.model

import androidx.room.*
import java.util.UUID

/** Analysis record — equivalent to iOS SwiftData AnalysisRecord. */
@Entity(tableName = "analysis_records")
data class AnalysisRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "photo_data") val photoData: ByteArray? = null,
    @ColumnInfo(name = "thumbnail_data") val thumbnailData: ByteArray? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    @ColumnInfo(name = "water_body_type") val waterBodyType: String? = null,
    @ColumnInfo(name = "water_conditions") val waterConditions: String? = null,
    @ColumnInfo(name = "fish_species_json") val fishSpeciesJSON: String = "[]",
    @ColumnInfo(name = "recommendations_json") val recommendationsJSON: String = "[]",
    @ColumnInfo(name = "weather_json") val weatherJSON: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

/** Fishing spot — equivalent to iOS FishingSpot. */
@Entity(tableName = "fishing_spots")
data class FishingSpotEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "water_body_type") val waterBodyType: String? = null,
    val notes: String? = null,
    val rating: Int = 3,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

/** Catch log — tied to a fish species name. */
@Entity(tableName = "catch_logs")
data class CatchLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "species_name") val speciesName: String,
    @ColumnInfo(name = "photo_data") val photoData: ByteArray? = null,
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "location_name") val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
