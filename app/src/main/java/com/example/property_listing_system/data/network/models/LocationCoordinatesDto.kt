package com.example.property_listing_system.data.network.models

import com.google.gson.annotations.SerializedName

data class LocationCoordinatesDto(
    @SerializedName("query")
    val query: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("formatted_address")
    val formattedAddress: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("country")
    val country: String
)
