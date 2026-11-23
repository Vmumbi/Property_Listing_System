package com.example.property_listing_system.ui.member2

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepository
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.data.validation.PropertyValidator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DatabaseUiState(
    val searchQuery: String = "",
    val selectedTypeFilter: String = "All",
    val statusMessage: String? = null,
    val isError: Boolean = false,
    val isAddDialogOpen: Boolean = false
)

class Member2DatabaseViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: PropertyRepository

    private val _uiState = MutableStateFlow(DatabaseUiState())
    val uiState: StateFlow<DatabaseUiState> = _uiState.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        val dataSource = PropertyLocalDataSourceImpl(database.propertyDao())
        repository = PropertyRepositoryImpl(dataSource)
    }

    // Reactive streams from Room DAO
    val totalCount: StateFlow<Int> = repository.getPropertyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val availableCount: StateFlow<Int> = repository.getAvailablePropertyCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoriteProperties: StateFlow<List<PropertyEntity>> = repository.getFavoriteProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val properties: StateFlow<List<PropertyEntity>> = combine(
        _uiState
    ) { stateArray ->
        val state = stateArray[0]
        Pair(state.searchQuery, state.selectedTypeFilter)
    }.flatMapLatest { (query, filter) ->
        when {
            query.isNotBlank() -> repository.searchProperties(query)
            filter != "All" -> repository.getPropertiesByType(filter)
            else -> repository.getAllProperties()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChanged(newQuery: String) {
        _uiState.value = _uiState.value.copy(searchQuery = newQuery)
    }

    fun onTypeFilterSelected(type: String) {
        _uiState.value = _uiState.value.copy(selectedTypeFilter = type)
    }

    fun openAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = true)
    }

    fun closeAddDialog() {
        _uiState.value = _uiState.value.copy(isAddDialogOpen = false)
    }

    fun clearStatusMessage() {
        _uiState.value = _uiState.value.copy(statusMessage = null)
    }

    // CRUD: CREATE
    fun addProperty(
        title: String,
        description: String,
        price: Double,
        propertyType: String,
        address: String,
        city: String,
        bedrooms: Int,
        bathrooms: Int,
        phone: String
    ) {
        viewModelScope.launch {
            val newProperty = PropertyEntity(
                title = title,
                description = description,
                price = price,
                propertyType = propertyType,
                address = address,
                city = city,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                contactPhone = phone,
                status = PropertyEntity.STATUS_AVAILABLE
            )

            val validation = PropertyValidator.validate(newProperty)
            if (!validation.isValid) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = validation.errorMessage ?: "Validation failed.",
                    isError = true
                )
                return@launch
            }

            val result = repository.addProperty(newProperty)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Property added successfully (ID: ${result.getOrNull()}).",
                    isError = false,
                    isAddDialogOpen = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Error adding property: ${result.exceptionOrNull()?.message}",
                    isError = true
                )
            }
        }
    }

    // CRUD: UPDATE (Toggle Favorite)
    fun toggleFavorite(property: PropertyEntity) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(property.id, !property.isFavorite)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = if (!property.isFavorite) "Added to favorites!" else "Removed from favorites.",
                    isError = false
                )
            }
        }
    }

    // CRUD: UPDATE (Status)
    fun updateStatus(propertyId: Long, newStatus: String) {
        viewModelScope.launch {
            val result = repository.updateStatus(propertyId, newStatus)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Status updated to $newStatus.",
                    isError = false
                )
            }
        }
    }

    // CRUD: DELETE
    fun deleteProperty(property: PropertyEntity) {
        viewModelScope.launch {
            val result = repository.deleteProperty(property)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Deleted '${property.title}'.",
                    isError = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Failed to delete: ${result.exceptionOrNull()?.message}",
                    isError = true
                )
            }
        }
    }

    // Reset / Pre-populate Database
    fun seedSampleData() {
        viewModelScope.launch {
            val sampleProperties = AppDatabase.getSampleInitialProperties()
            val result = repository.addProperties(sampleProperties)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "Database seeded with ${sampleProperties.size} realistic listings.",
                    isError = false
                )
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            val result = repository.clearAllProperties()
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    statusMessage = "All properties cleared from SQLite database.",
                    isError = false
                )
            }
        }
    }
}
