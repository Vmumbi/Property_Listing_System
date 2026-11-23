package com.example.property_listing_system.data.network.repository

import com.example.property_listing_system.data.network.api.PropertyApiService
import com.example.property_listing_system.data.network.client.NetworkResult
import com.example.property_listing_system.data.network.client.RetrofitClient
import com.example.property_listing_system.data.network.models.LocationCoordinatesDto
import com.example.property_listing_system.data.network.models.PropertyValuationRequest
import com.example.property_listing_system.data.network.models.PropertyValuationResponse
import com.example.property_listing_system.data.network.models.RemotePropertyDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PropertyNetworkRepository(
    private val apiService: PropertyApiService = RetrofitClient.apiService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Submits property specs to remote valuation API and returns estimated value & rent
     */
    suspend fun getPropertyValuation(
        request: PropertyValuationRequest
    ): NetworkResult<PropertyValuationResponse> = withContext(ioDispatcher) {
        try {
            val response = apiService.getValuationEstimate(request)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to fetch valuation estimate",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.localizedMessage ?: "Network connection error",
                cause = e
            )
        }
    }

    /**
     * Fetches remote property listings for background synchronization (WorkManager)
     */
    suspend fun fetchRemoteProperties(
        lastSyncTimestamp: Long = 0
    ): NetworkResult<List<RemotePropertyDto>> = withContext(ioDispatcher) {
        try {
            val response = apiService.getRemoteProperties(lastSyncTimestamp)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Failed to synchronize remote properties",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.localizedMessage ?: "Background sync network error",
                cause = e
            )
        }
    }

    /**
     * Geocodes an address via external API
     */
    suspend fun geocodeAddress(
        address: String
    ): NetworkResult<LocationCoordinatesDto> = withContext(ioDispatcher) {
        try {
            val response = apiService.geocodeAddress(address)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(
                    message = response.errorBody()?.string() ?: "Geocoding failed for address: $address",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.localizedMessage ?: "Geocoding network error",
                cause = e
            )
        }
    }
}
