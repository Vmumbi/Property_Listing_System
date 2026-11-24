package com.example.property_listing_system.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * PropertyEntity represents the persistent data model for properties in SQLite via Room.
 */
@Entity(
    tableName = "properties",
    indices = [
        Index(value = ["remoteId"], unique = false),
        Index(value = ["propertyType"]),
        Index(value = ["price"]),
        Index(value = ["status"]),
        Index(value = ["isFavorite"]),
        Index(value = ["agentEmail"])
    ]
)
data class PropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val title: String,
    val description: String,
    val price: Double,
    val currency: String = "KES",
    val propertyType: String, // e.g. "Apartment", "Villa", "Townhouse", "Mansionette", "Commercial", "Land"
    val address: String,
    val city: String = "Nairobi",
    val latitude: Double = -1.286389,
    val longitude: Double = 36.817223,
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val squareFootage: Double = 0.0,
    val imageUrl: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val isFeatured: Boolean = false,
    val isFavorite: Boolean = false,
    val status: String = STATUS_AVAILABLE, // "AVAILABLE", "RENTED", "SOLD", "UNDER_OFFER"
    val createdTimestamp: Long = System.currentTimeMillis(),
    val remoteId: String? = null,
    val pricePeriod: String = PERIOD_MONTHLY, // "Monthly", "Yearly", "Total"
    val agentEmail: String = "" // Tracks owner agent for "My Listed Properties"
) {
    companion object {
        const val STATUS_AVAILABLE = "AVAILABLE"
        const val STATUS_RENTED = "RENTED"
        const val STATUS_SOLD = "SOLD"
        const val STATUS_UNDER_OFFER = "UNDER_OFFER"

        const val TYPE_APARTMENT = "Apartment"
        const val TYPE_VILLA = "Villa"
        const val TYPE_TOWNHOUSE = "Townhouse"
        const val TYPE_MANSIONETTE = "Mansionette"
        const val TYPE_COMMERCIAL = "Commercial"
        const val TYPE_LAND = "Land"

        const val PERIOD_MONTHLY = "Monthly"
        const val PERIOD_YEARLY = "Yearly"
        const val PERIOD_TOTAL = "Total"
    }
}
