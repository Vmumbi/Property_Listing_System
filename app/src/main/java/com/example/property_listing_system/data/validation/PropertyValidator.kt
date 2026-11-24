package com.example.property_listing_system.data.validation

import com.example.property_listing_system.data.local.entity.PropertyEntity
import java.util.regex.Pattern

/**
 * PropertyValidator ensures data integrity and business rules before Room database operations.
 */
object PropertyValidator {

    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" +
        "\\." +
        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
        ")+"
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val fieldErrors: Map<String, String> = emptyMap()
    )

    /**
     * Comprehensive validation of PropertyEntity before persistence
     */
    fun validate(property: PropertyEntity): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (property.title.trim().length < 3) {
            errors["title"] = "Title must be at least 3 characters long."
        }

        if (property.description.trim().isEmpty()) {
            errors["description"] = "Description cannot be empty."
        }

        if (property.price <= 0) {
            errors["price"] = "Listing price must be greater than zero."
        } else if (property.price > 10_000_000_000.0) {
            errors["price"] = "Listing price exceeds maximum allowable threshold."
        }

        if (property.address.trim().isEmpty()) {
            errors["address"] = "Property address is required."
        }

        if (property.bedrooms < 0) {
            errors["bedrooms"] = "Bedrooms count cannot be negative."
        }

        if (property.bathrooms < 0) {
            errors["bathrooms"] = "Bathrooms count cannot be negative."
        }

        if (property.squareFootage < 0) {
            errors["squareFootage"] = "Square footage cannot be negative."
        }

        if (property.contactPhone.isNotBlank()) {
            if (!isValidKenyanPhoneNumber(property.contactPhone)) {
                errors["contactPhone"] = "Phone number must be a valid Kenyan number: 10 digits (07... or 01...) or 12 digits (+254...)."
            }
        } else {
            errors["contactPhone"] = "Contact phone number is required."
        }

        if (property.contactEmail.isNotBlank() && !isValidEmail(property.contactEmail)) {
            errors["contactEmail"] = "Contact email format is invalid."
        }

        return if (errors.isEmpty()) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                errorMessage = errors.values.first(),
                fieldErrors = errors
            )
        }
    }

    /**
     * Strict Kenyan phone number validation enforcing exact digit count:
     * - 10 digits starting with 07 or 01 (e.g., 0712345678, 0112345678)
     * - 12 digits starting with 254 (e.g., 254712345678)
     * - 13 characters starting with +254 (e.g., +254712345678)
     */
    fun isValidKenyanPhoneNumber(phone: String): Boolean {
        val sanitized = phone.replace(" ", "").replace("-", "").trim()
        // Accept 07xxxxxxxx / 01xxxxxxxx (10 digits) or +2547xxxxxxxx / 2547xxxxxxxx (12/13 chars)
        val regex = Regex("""^(\+254\d{9}|254\d{9}|0\d{9})$""")
        return regex.matches(sanitized)
    }

    // Retained for backward-compatibility with other tests
    fun isValidPhoneNumber(phone: String): Boolean = isValidKenyanPhoneNumber(phone)

    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email.trim()).matches()
    }
}

/**
 * UserValidator provides validation for Login & Registration workflows.
 */
object UserValidator {

    data class UserValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    fun validateLogin(email: String, password: String): UserValidationResult {
        if (!PropertyValidator.isValidEmail(email)) {
            return UserValidationResult(false, "Please enter a valid email address.")
        }
        if (password.length < 6) {
            return UserValidationResult(false, "Password must be at least 6 characters.")
        }
        return UserValidationResult(true)
    }

    fun validateRegistration(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): UserValidationResult {
        if (name.trim().length < 2) {
            return UserValidationResult(false, "Full name must be at least 2 characters.")
        }
        if (!PropertyValidator.isValidEmail(email)) {
            return UserValidationResult(false, "Please enter a valid email address.")
        }
        if (password.length < 6) {
            return UserValidationResult(false, "Password must be at least 6 characters long.")
        }
        if (password != confirmPassword) {
            return UserValidationResult(false, "Passwords do not match.")
        }
        return UserValidationResult(true)
    }
}
