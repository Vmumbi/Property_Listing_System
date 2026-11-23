package com.example.property_listing_system.data.network.models

import com.google.gson.annotations.SerializedName

data class RemotePropertyDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("price")
    val price: Double,

    @SerializedName("currency")
    val currency: String = "KES",

    @SerializedName("property_type")
    val propertyType: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("bedrooms")
    val bedrooms: Int,

    @SerializedName("bathrooms")
    val bathrooms: Int,

    @SerializedName("square_footage")
    val squareFootage: Double,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("contact_phone")
    val contactPhone: String,

    @SerializedName("is_featured")
    val isFeatured: Boolean = false,

    @SerializedName("last_updated_timestamp")
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)
