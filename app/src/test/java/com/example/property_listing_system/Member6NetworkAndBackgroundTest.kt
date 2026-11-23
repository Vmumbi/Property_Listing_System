package com.example.property_listing_system

import com.example.property_listing_system.data.network.api.MockPropertyApiInterceptor
import com.example.property_listing_system.data.network.client.NetworkResult
import com.example.property_listing_system.data.network.models.PropertyValuationRequest
import com.example.property_listing_system.data.network.models.PropertyValuationResponse
import com.example.property_listing_system.data.network.models.RemotePropertyDto
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Member6NetworkAndBackgroundTest {

    private val gson = Gson()

    @Test
    fun testPropertyValuationJsonSerialization() {
        val request = PropertyValuationRequest(
            propertyType = "Apartment",
            location = "Westlands, Nairobi",
            bedrooms = 3,
            bathrooms = 2,
            squareFootage = 1500.0,
            hasGarden = true,
            hasSwimmingPool = false
        )

        val json = gson.toJson(request)
        assertTrue(json.contains("Westlands, Nairobi"))
        assertTrue(json.contains("square_footage"))

        val parsed = gson.fromJson(json, PropertyValuationRequest::class.java)
        assertEquals("Apartment", parsed.propertyType)
        assertEquals(3, parsed.bedrooms)
        assertEquals(1500.0, parsed.squareFootage, 0.01)
    }

    @Test
    fun testMockValuationApiInterceptorCalculations() {
        val interceptor = MockPropertyApiInterceptor(gson = gson, enabled = true)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val reqBody = gson.toJson(
            PropertyValuationRequest(
                propertyType = "Villa",
                location = "Karen, Nairobi",
                bedrooms = 4,
                bathrooms = 4,
                squareFootage = 3500.0,
                hasGarden = true,
                hasSwimmingPool = true
            )
        )

        val request = Request.Builder()
            .url("https://api.propertylistingsystem.com/api/v1/properties/valuation")
            .post(reqBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val bodyString = response.body?.string()
        assertNotNull(bodyString)

        val valuation = gson.fromJson(bodyString, PropertyValuationResponse::class.java)
        assertEquals("Villa", valuation.propertyType)
        assertTrue("Estimated value should be positive", valuation.estimatedValue > 10_000_000.0)
        assertTrue("High range should be greater than low range", valuation.valueRangeHigh > valuation.valueRangeLow)
        assertTrue("Monthly rent should be calculated", valuation.estimatedMonthlyRent > 0.0)
        assertEquals("KES", valuation.currency)
    }

    @Test
    fun testMockSyncPropertiesEndpoint() {
        val interceptor = MockPropertyApiInterceptor(gson = gson, enabled = true)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val request = Request.Builder()
            .url("https://api.propertylistingsystem.com/api/v1/properties/sync?last_sync=0")
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)

        val bodyString = response.body?.string()
        assertNotNull(bodyString)

        val properties = gson.fromJson(bodyString, Array<RemotePropertyDto>::class.java)
        assertTrue("Sync should return at least 4 remote properties", properties.size >= 4)
        assertNotNull(properties[0].title)
        assertTrue(properties[0].price > 0.0)
    }

    @Test
    fun testGeocodingEndpoint() {
        val interceptor = MockPropertyApiInterceptor(gson = gson, enabled = true)
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val request = Request.Builder()
            .url("https://api.propertylistingsystem.com/api/v1/location/geocode?address=Westlands")
            .get()
            .build()

        val response = client.newCall(request).execute()
        assertEquals(200, response.code)
        val bodyString = response.body?.string()
        assertNotNull(bodyString)
        assertTrue(bodyString!!.contains("Westlands"))
    }

    @Test
    fun testNetworkResultSealedClass() {
        val success: NetworkResult<String> = NetworkResult.Success("Data")
        assertTrue(success.isSuccess)
        assertEquals("Data", success.getOrNull())

        val error: NetworkResult<String> = NetworkResult.Error("Network failure", 500)
        assertTrue(error.isError)
        assertEquals(null, error.getOrNull())
    }
}
