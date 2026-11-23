package com.example.property_listing_system.data.mapper

import com.example.property_listing_system.data.local.entity.PropertyEntity

/**
 * PropertyMapper provides bidirectional conversion between local Room entity
 * models and remote network DTO models.
 */
object PropertyMapper {

    /**
     * Converts a PropertyEntity to a generic Map or remote-ready representation.
     */
    fun entityToSummaryString(entity: PropertyEntity): String {
        return "${entity.title} - ${entity.currency} ${entity.price} (${entity.propertyType}) in ${entity.city}"
    }

    /**
     * Converts an external representation to PropertyEntity.
     */
    fun createEntity(
        id: Long = 0L,
        title: String,
        description: String,
        price: Double,
        currency: String = "KES",
        propertyType: String = PropertyEntity.TYPE_APARTMENT,
        address: String,
        city: String = "Nairobi",
        latitude: Double = -1.286389,
        longitude: Double = 36.817223,
        bedrooms: Int = 1,
        bathrooms: Int = 1,
        squareFootage: Double = 0.0,
        imageUrl: String = "",
        contactPhone: String = "",
        contactEmail: String = "",
        isFeatured: Boolean = false,
        isFavorite: Boolean = false,
        status: String = PropertyEntity.STATUS_AVAILABLE,
        remoteId: String? = null
    ): PropertyEntity {
        return PropertyEntity(
            id = id,
            title = title,
            description = description,
            price = price,
            currency = currency,
            propertyType = propertyType,
            address = address,
            city = city,
            latitude = latitude,
            longitude = longitude,
            bedrooms = bedrooms,
            bathrooms = bathrooms,
            squareFootage = squareFootage,
            imageUrl = imageUrl,
            contactPhone = contactPhone,
            contactEmail = contactEmail,
            isFeatured = isFeatured,
            isFavorite = isFavorite,
            status = status,
            remoteId = remoteId
        )
    }
}
