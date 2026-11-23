package com.example.property_listing_system.data.location

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object MapIntentHelper {

    /**
     * Opens an external Maps application to view the property location pin.
     */
    fun openPropertyInMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        propertyTitle: String = "Property Location"
    ) {
        val encodedTitle = Uri.encode(propertyTitle)
        val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedTitle)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to web browser Google Maps
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(webIntent)
            } catch (err: Exception) {
                Toast.makeText(context, "No map application or browser available", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Launches turn-by-turn navigation to the property.
     */
    fun openDirectionsToProperty(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        val navUri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            openPropertyInMaps(context, latitude, longitude, "Property Destination")
        }
    }
}
