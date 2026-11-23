package com.example.property_listing_system.data.network.api

import com.example.property_listing_system.data.network.models.LocationCoordinatesDto
import com.example.property_listing_system.data.network.models.PropertyValuationRequest
import com.example.property_listing_system.data.network.models.PropertyValuationResponse
import com.example.property_listing_system.data.network.models.RemotePropertyDto
import com.google.gson.Gson
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class MockPropertyApiInterceptor(
    private val gson: Gson = Gson(),
    private val enabled: Boolean = true
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (!enabled) {
            return chain.proceed(request)
        }

        val url = request.url.toString()
        val path = request.url.encodedPath

        // 1. Property Valuation Endpoint
        if (path.contains("api/v1/properties/valuation")) {
            val buffer = okio.Buffer()
            request.body?.writeTo(buffer)
            val requestBodyString = buffer.readUtf8()
            val valuationRequest = runCatching {
                gson.fromJson(requestBodyString, PropertyValuationRequest::class.java)
            }.getOrNull()

            val responseObj = calculateValuation(valuationRequest)
            val json = gson.toJson(responseObj)

            return Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()
        }

        // 2. Property Sync Endpoint
        if (path.contains("api/v1/properties/sync")) {
            val mockList = generateMockRemoteProperties()
            val json = gson.toJson(mockList)

            return Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()
        }

        // 3. Geocode Endpoint
        if (path.contains("api/v1/location/geocode")) {
            val addressQuery = request.url.queryParameter("address") ?: "Nairobi"
            val geocodeResult = generateGeocodeResult(addressQuery)
            val json = gson.toJson(geocodeResult)

            return Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()
        }

        return chain.proceed(request)
    }

    private fun calculateValuation(req: PropertyValuationRequest?): PropertyValuationResponse {
        val location = req?.location?.trim() ?: "Nairobi, Kenya"
        val sqft = if (req != null && req.squareFootage > 0) req.squareFootage else 1200.0
        val beds = req?.bedrooms ?: 2
        val baths = req?.bathrooms ?: 2
        val type = req?.propertyType ?: "Apartment"

        // Base price per square foot (KES) based on location
        val baseRatePerSqFt = when {
            location.contains("Karen", ignoreCase = true) -> 16500.0
            location.contains("Runda", ignoreCase = true) -> 17000.0
            location.contains("Westlands", ignoreCase = true) -> 15000.0
            location.contains("Kilimani", ignoreCase = true) -> 13500.0
            location.contains("Kileleshwa", ignoreCase = true) -> 13000.0
            location.contains("Lavington", ignoreCase = true) -> 15500.0
            location.contains("CBD", ignoreCase = true) -> 14000.0
            location.contains("Mombasa", ignoreCase = true) -> 12000.0
            location.contains("Kisumu", ignoreCase = true) -> 10000.0
            else -> 11000.0
        }

        // Base calculation
        var estimated = sqft * baseRatePerSqFt
        estimated += (beds * 600000.0)
        estimated += (baths * 350000.0)

        // Amenity adjustments
        if (req?.hasSwimmingPool == true) estimated *= 1.12
        if (req?.hasGarden == true) estimated *= 1.08
        if (req?.hasParking == true) estimated *= 1.04

        // Property type multiplier
        estimated = when (type.lowercase(Locale.ROOT)) {
            "villa" -> estimated * 1.25
            "mansionette" -> estimated * 1.15
            "penthouse" -> estimated * 1.30
            "commercial" -> estimated * 1.35
            "studio" -> estimated * 0.90
            else -> estimated
        }

        // Round to nearest 50,000
        val roundedEstimated = ((estimated / 50000.0).roundToInt() * 50000.0)
        val lowRange = (roundedEstimated * 0.93)
        val highRange = (roundedEstimated * 1.07)
        val monthlyRent = ((roundedEstimated * 0.007) / 1000.0).roundToInt() * 1000.0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date())

        val demand = when {
            location.contains("Westlands", true) || location.contains("Kilimani", true) -> "Very High Demand"
            location.contains("Karen", true) || location.contains("Runda", true) -> "Stable Premium"
            else -> "Growing Demand"
        }

        val summary = "Algorithmic valuation based on ${sqft.toInt()} sq ft in $location with $beds beds, $baths baths and selected amenities."

        return PropertyValuationResponse(
            propertyType = type,
            location = location,
            estimatedValue = roundedEstimated,
            currency = "KES",
            valueRangeLow = lowRange,
            valueRangeHigh = highRange,
            estimatedMonthlyRent = monthlyRent,
            confidenceScore = 0.94,
            marketDemand = demand,
            valuationDate = dateStr,
            valuationSummary = summary
        )
    }

    private fun generateMockRemoteProperties(): List<RemotePropertyDto> {
        val now = System.currentTimeMillis()
        return listOf(
            RemotePropertyDto(
                id = "REMOTE-001",
                title = "Luxury 3-Bedroom Penthouse with Panoramic Views",
                description = "Modern penthouse with master ensuite, floor-to-ceiling windows, rooftop terrace, gym, and heated pool access.",
                price = 24500000.0,
                currency = "KES",
                propertyType = "Apartment",
                address = "Westlands, Nairobi, Kenya",
                latitude = -1.2678,
                longitude = 36.8094,
                bedrooms = 3,
                bathrooms = 3,
                squareFootage = 2200.0,
                imageUrl = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=600&q=80",
                contactPhone = "+254712345678",
                isFeatured = true,
                lastUpdatedTimestamp = now
            ),
            RemotePropertyDto(
                id = "REMOTE-002",
                title = "Charming 4-Bedroom Villa on 0.5 Acre",
                description = "Spacious colonial-style villa featuring lush landscaped gardens, high ceilings, fireplace, and private swimming pool.",
                price = 78000000.0,
                currency = "KES",
                propertyType = "Villa",
                address = "Karen, Nairobi, Kenya",
                latitude = -1.3197,
                longitude = 36.7065,
                bedrooms = 4,
                bathrooms = 4,
                squareFootage = 4500.0,
                imageUrl = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=600&q=80",
                contactPhone = "+254722987654",
                isFeatured = true,
                lastUpdatedTimestamp = now
            ),
            RemotePropertyDto(
                id = "REMOTE-003",
                title = "Executive 2-Bedroom Apartment Near Yaya",
                description = "Ideal investment property with high rental yields. Includes backup generator, borehole, 24/7 CCTV, and fitted open-plan kitchen.",
                price = 14500000.0,
                currency = "KES",
                propertyType = "Apartment",
                address = "Kilimani, Nairobi, Kenya",
                latitude = -1.2921,
                longitude = 36.7856,
                bedrooms = 2,
                bathrooms = 2,
                squareFootage = 1350.0,
                imageUrl = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=600&q=80",
                contactPhone = "+254733112233",
                isFeatured = false,
                lastUpdatedTimestamp = now
            ),
            RemotePropertyDto(
                id = "REMOTE-004",
                title = "Modern 4-Bedroom Townhouse in Gated Estate",
                description = "Family-friendly gated community with clubhouse, kids play area, private DSQ, solar water heating, and perimeter electric fence.",
                price = 36000000.0,
                currency = "KES",
                propertyType = "Mansionette",
                address = "Lavington, Nairobi, Kenya",
                latitude = -1.2789,
                longitude = 36.7689,
                bedrooms = 4,
                bathrooms = 3,
                squareFootage = 3100.0,
                imageUrl = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=600&q=80",
                contactPhone = "+254701445566",
                isFeatured = false,
                lastUpdatedTimestamp = now
            )
        )
    }

    private fun generateGeocodeResult(query: String): LocationCoordinatesDto {
        val q = query.lowercase(Locale.ROOT)
        return when {
            q.contains("karen") -> LocationCoordinatesDto(query, -1.3197, 36.7065, "Karen, Nairobi", "Nairobi", "Kenya")
            q.contains("westlands") -> LocationCoordinatesDto(query, -1.2678, 36.8094, "Westlands, Nairobi", "Nairobi", "Kenya")
            q.contains("kilimani") -> LocationCoordinatesDto(query, -1.2921, 36.7856, "Kilimani, Nairobi", "Nairobi", "Kenya")
            q.contains("lavington") -> LocationCoordinatesDto(query, -1.2789, 36.7689, "Lavington, Nairobi", "Nairobi", "Kenya")
            q.contains("runda") -> LocationCoordinatesDto(query, -1.2186, 36.8184, "Runda, Nairobi", "Nairobi", "Kenya")
            q.contains("mombasa") -> LocationCoordinatesDto(query, -4.0435, 39.6682, "Mombasa City", "Mombasa", "Kenya")
            q.contains("kisumu") -> LocationCoordinatesDto(query, -0.0917, 34.7680, "Kisumu City", "Kisumu", "Kenya")
            else -> LocationCoordinatesDto(query, -1.286389, 36.817223, "$query, Nairobi", "Nairobi", "Kenya")
        }
    }
}
