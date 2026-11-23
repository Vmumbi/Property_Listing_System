package com.example.property_listing_system.data.network.models

import com.google.gson.annotations.SerializedName

data class PropertyValuationResponse(
    @SerializedName("property_type")
    val propertyType: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("estimated_value")
    val estimatedValue: Double,

    @SerializedName("currency")
    val currency: String = "KES",

    @SerializedName("value_range_low")
    val valueRangeLow: Double,

    @SerializedName("value_range_high")
    val valueRangeHigh: Double,

    @SerializedName("estimated_monthly_rent")
    val estimatedMonthlyRent: Double,

    @SerializedName("confidence_score")
    val confidenceScore: Double,

    @SerializedName("market_demand")
    val marketDemand: String,

    @SerializedName("valuation_date")
    val valuationDate: String,

    @SerializedName("valuation_summary")
    val valuationSummary: String
)
