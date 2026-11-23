package com.example.property_listing_system

import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.validation.PropertyValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Member 2 database data validation and integrity rules.
 */
class PropertyValidatorTest {

    private fun createValidProperty(): PropertyEntity {
        return PropertyEntity(
            id = 1L,
            title = "Modern Westlands Penthouse",
            description = "Spacious penthouse with skyline view and swimming pool.",
            price = 25000000.0,
            currency = "KES",
            propertyType = PropertyEntity.TYPE_APARTMENT,
            address = "Rhapta Road, Westlands",
            city = "Nairobi",
            bedrooms = 3,
            bathrooms = 2,
            squareFootage = 2100.0,
            contactPhone = "+254712345678",
            contactEmail = "realtor@westlands.co.ke",
            status = PropertyEntity.STATUS_AVAILABLE
        )
    }

    @Test
    fun validProperty_passesValidation() {
        val property = createValidProperty()
        val result = PropertyValidator.validate(property)
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun titleTooShort_failsValidation() {
        val property = createValidProperty().copy(title = "AB")
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Title must be at least 3 characters"))
    }

    @Test
    fun emptyDescription_failsValidation() {
        val property = createValidProperty().copy(description = "   ")
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Description cannot be empty"))
    }

    @Test
    fun zeroOrNegativePrice_failsValidation() {
        val propertyZero = createValidProperty().copy(price = 0.0)
        val resultZero = PropertyValidator.validate(propertyZero)
        assertFalse(resultZero.isValid)
        assertTrue(resultZero.errorMessage!!.contains("greater than zero"))

        val propertyNegative = createValidProperty().copy(price = -5000.0)
        val resultNegative = PropertyValidator.validate(propertyNegative)
        assertFalse(resultNegative.isValid)
    }

    @Test
    fun emptyAddress_failsValidation() {
        val property = createValidProperty().copy(address = "")
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("address is required"))
    }

    @Test
    fun negativeBedrooms_failsValidation() {
        val property = createValidProperty().copy(bedrooms = -1)
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Bedrooms count cannot be negative"))
    }

    @Test
    fun negativeBathrooms_failsValidation() {
        val property = createValidProperty().copy(bathrooms = -1)
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Bathrooms count cannot be negative"))
    }

    @Test
    fun invalidPhoneNumber_failsValidation() {
        val property = createValidProperty().copy(contactPhone = "abc-123-phone")
        val result = PropertyValidator.validate(property)
        assertFalse(result.isValid)
        assertTrue(result.errorMessage!!.contains("Contact phone number format is invalid"))
    }

    @Test
    fun validKenyanPhoneFormats_passValidation() {
        val phoneNumbers = listOf("+254712345678", "0712345678", "+254112345678", "0112345678")
        for (phone in phoneNumbers) {
            val property = createValidProperty().copy(contactPhone = phone)
            val result = PropertyValidator.validate(property)
            assertTrue("Phone $phone should be valid", result.isValid)
        }
    }
}
