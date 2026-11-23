package com.example.property_listing_system.data.network.api

import com.example.property_listing_system.data.network.models.LocationCoordinatesDto
import com.example.property_listing_system.data.network.models.PropertyValuationRequest
import com.example.property_listing_system.data.network.models.PropertyValuationResponse
import com.example.property_listing_system.data.network.models.RemotePropertyDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PropertyApiService {

    /**
     * Request an algorithmic property valuation and market rental estimate
     */
    @POST("api/v1/properties/valuation")
    suspend fun getValuationEstimate(
        @Body request: PropertyValuationRequest
    ): Response<PropertyValuationResponse>

    /**
     * Fetch latest property listings from the remote server for background sync
     */
    @GET("api/v1/properties/sync")
    suspend fun getRemoteProperties(
        @Query("last_sync") lastSync: Long = 0
    ): Response<List<RemotePropertyDto>>

    /**
     * Geocode an address to latitude and longitude
     */
    @GET("api/v1/location/geocode")
    suspend fun geocodeAddress(
        @Query("address") address: String
    ): Response<LocationCoordinatesDto>
}
