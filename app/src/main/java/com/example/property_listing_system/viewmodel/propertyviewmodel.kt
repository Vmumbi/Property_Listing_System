package com.example.property_listing_system.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn

class PropertyViewModel(
    private val repository: PropertyRepository
) : ViewModel() {

    // Observe all properties
    val allProperties: StateFlow<List<PropertyEntity>> =
        repository.getAllProperties().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe favorite properties
    val favoriteProperties: StateFlow<List<PropertyEntity>> =
        repository.getFavoriteProperties().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Observe featured properties
    val featuredProperties: StateFlow<List<PropertyEntity>> =
        repository.getFeaturedProperties().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Search properties
    fun searchProperties(query: String): Flow<List<PropertyEntity>> {
        return repository.searchProperties(query)
    }

    // Get property by ID
    fun getPropertyById(id: Long): Flow<PropertyEntity?> {
        return repository.getPropertyById(id)
    }

    // Filter by property type
    fun getPropertiesByType(type: String): Flow<List<PropertyEntity>> {
        return repository.getPropertiesByType(type)
    }

    // Filter by price range
    fun getPropertiesByPriceRange(
        minPrice: Double,
        maxPrice: Double
    ): Flow<List<PropertyEntity>> {
        return repository.getPropertiesByPriceRange(minPrice, maxPrice)
    }

    // Filter by status
    fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>> {
        return repository.getPropertiesByStatus(status)
    }

    // Get property types
    fun getAllPropertyTypes(): Flow<List<String>> {
        return repository.getAllPropertyTypes()
    }

    // Add a property
    suspend fun addProperty(property: PropertyEntity): Result<Long> {
        return repository.addProperty(property)
    }

    // Update a property
    suspend fun updateProperty(property: PropertyEntity): Result<Unit> {
        return repository.updateProperty(property)
    }

    // Toggle favorite status
    suspend fun toggleFavorite(
        id: Long,
        isFavorite: Boolean
    ): Result<Unit> {
        return repository.toggleFavorite(id, isFavorite)
    }

    // Update property status
    suspend fun updateStatus(
        id: Long,
        status: String
    ): Result<Unit> {
        return repository.updateStatus(id, status)
    }

    // Update property price
    suspend fun updatePrice(
        id: Long,
        newPrice: Double
    ): Result<Unit> {
        return repository.updatePrice(id, newPrice)
    }

    // Delete a property
    suspend fun deleteProperty(property: PropertyEntity): Result<Unit> {
        return repository.deleteProperty(property)
    }

    // Delete property by ID
    suspend fun deletePropertyById(id: Long): Result<Unit> {
        return repository.deletePropertyById(id)
    }

    // Delete all properties
    suspend fun clearAllProperties(): Result<Unit> {
        return repository.clearAllProperties()
    }
}