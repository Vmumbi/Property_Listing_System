package com.example.property_listing_system.data.network.models

import com.google.gson.annotations.SerializedName

data class PropertyValuationRequest(
    @SerializedName("property_type")
    val propertyType: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("bedrooms")
    val bedrooms: Int,

    @SerializedName("bathrooms")
    val bathrooms: Int,

    @SerializedName("square_footage")
    val squareFootage: Double,

    @SerializedName("has_garden")
    val hasGarden: Boolean = false,

    @SerializedName("has_swimming_pool")
    val hasSwimmingPool: Boolean = false,

    @SerializedName("has_parking")
    val hasParking: Boolean = true,

    @SerializedName("year_built")
    val yearBuilt: Int? = null
)
