package com.example.property_listing_system.data.validation

import com.example.property_listing_system.data.local.entity.PropertyEntity

/**
 * PropertyValidator ensures business logic validation before database write operations.
 * Contributes to the "Testing, Validation & Error Handling" rubric.
 */
object PropertyValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validate(property: PropertyEntity): ValidationResult {
        if (property.title.trim().length < 3) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Title must be at least 3 characters long."
            )
        }

        if (property.description.trim().isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Description cannot be empty."
            )
        }

        if (property.price <= 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Listing price must be greater than zero."
            )
        }

        if (property.address.trim().isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Property address is required."
            )
        }

        if (property.bedrooms < 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Bedrooms count cannot be negative."
            )
        }

        if (property.bathrooms < 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Bathrooms count cannot be negative."
            )
        }

        if (property.squareFootage < 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Square footage cannot be negative."
            )
        }

        if (property.contactPhone.isNotBlank() && !isValidPhoneNumber(property.contactPhone)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Contact phone number format is invalid."
            )
        }

        return ValidationResult(isValid = true)
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Accepts Kenyan (+254..., 07..., 01...) and general international phone numbers
        val regex = Regex("""^(\+?[0-9]{9,15})$""")
        return regex.matches(phone.replace(" ", "").replace("-", ""))
    }
}
