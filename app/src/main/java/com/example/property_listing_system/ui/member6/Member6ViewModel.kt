package com.example.property_listing_system.ui.member6

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.example.property_listing_system.background.scheduler.PropertyWorkManagerScheduler
import com.example.property_listing_system.data.location.MapIntentHelper
import com.example.property_listing_system.data.location.PropertyLocationManager
import com.example.property_listing_system.data.network.client.NetworkResult
import com.example.property_listing_system.data.network.models.PropertyValuationRequest
import com.example.property_listing_system.data.network.models.PropertyValuationResponse
import com.example.property_listing_system.data.network.repository.PropertyNetworkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class Member6UiState(
    val isValuationLoading: Boolean = false,
    val valuationResult: PropertyValuationResponse? = null,
    val valuationError: String? = null,

    val syncState: String = "IDLE",
    val syncDetail: String? = null,

    val reminderStatus: String? = null,
    val detectedLocation: String? = null,
    val isDetectingLocation: Boolean = false
)

class Member6ViewModel(application: Application) : AndroidViewModel(application) {

    private val networkRepository = PropertyNetworkRepository()
    private val workScheduler = PropertyWorkManagerScheduler(application)
    private val locationManager = PropertyLocationManager(application)

    private val _uiState = MutableStateFlow(Member6UiState())
    val uiState: StateFlow<Member6UiState> = _uiState.asStateFlow()

    init {
        observeSyncWork()
    }

    private fun observeSyncWork() {
        viewModelScope.launch {
            workScheduler.getSyncWorkInfoFlow().collect { workInfoList ->
                val latest = workInfoList.firstOrNull()
                if (latest != null) {
                    val stateStr = latest.state.name
                    val message = latest.outputData.getString("sync_message")
                    _uiState.update {
                        it.copy(
                            syncState = stateStr,
                            syncDetail = message ?: "Worker state: $stateStr"
                        )
                    }
                }
            }
        }
    }

    /**
     * Member 6 Task: External API Integration (Property Valuation Estimate)
     */
    fun calculateValuation(
        propertyType: String,
        location: String,
        bedrooms: Int,
        bathrooms: Int,
        sqft: Double,
        hasPool: Boolean,
        hasGarden: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isValuationLoading = true, valuationError = null) }

            val request = PropertyValuationRequest(
                propertyType = propertyType,
                location = location,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                squareFootage = sqft,
                hasSwimmingPool = hasPool,
                hasGarden = hasGarden
            )

            when (val result = networkRepository.getPropertyValuation(request)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isValuationLoading = false,
                            valuationResult = result.data,
                            valuationError = null
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isValuationLoading = false,
                            valuationError = result.message
                        )
                    }
                }
                is NetworkResult.Loading -> {
                    _uiState.update { it.copy(isValuationLoading = true) }
                }
            }
        }
    }

    /**
     * Member 6 Task: Background Sync with WorkManager
     */
    fun triggerImmediateSync() {
        _uiState.update {
            it.copy(syncState = "ENQUEUED", syncDetail = "Enqueued immediate sync with remote API...")
        }
        workScheduler.scheduleImmediateSync()
    }

    /**
     * Schedule Periodic Daily WorkManager Sync
     */
    fun schedulePeriodicSync() {
        workScheduler.schedulePeriodicSync(24)
        _uiState.update {
            it.copy(syncDetail = "Periodic sync scheduled: Every 24 hours on Wi-Fi/Cellular")
        }
    }

    /**
     * Member 6 Task: Scheduled Inspection & Viewing Reminders with WorkManager
     */
    fun scheduleViewingReminder(
        propertyTitle: String,
        delaySeconds: Long = 10,
        reminderType: String = "Viewing Appointment"
    ) {
        workScheduler.schedulePropertyReminder(
            propertyId = "DEMO-PROP-101",
            propertyTitle = propertyTitle,
            message = "Your scheduled viewing for $propertyTitle is starting soon. Tap to view property details.",
            delaySeconds = delaySeconds,
            reminderType = reminderType
        )
        _uiState.update {
            it.copy(
                reminderStatus = "Reminder scheduled! Notification will fire in $delaySeconds seconds."
            )
        }
    }

    /**
     * Member 6 Task: Location & Maps Integration
     */
    fun detectCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true) }
            val location = locationManager.getCurrentLocation()
            if (location != null) {
                val address = locationManager.getAddressFromCoordinates(location.latitude, location.longitude)
                val text = address ?: "Lat: ${location.latitude}, Lng: ${location.longitude}"
                _uiState.update {
                    it.copy(isDetectingLocation = false, detectedLocation = text)
                }
            } else {
                _uiState.update {
                    it.copy(
                        isDetectingLocation = false,
                        detectedLocation = "Nairobi, Kenya (GPS fallback)"
                    )
                }
            }
        }
    }

    fun openPropertyMap(title: String, lat: Double, lng: Double) {
        MapIntentHelper.openPropertyInMaps(
            context = getApplication(),
            latitude = lat,
            longitude = lng,
            propertyTitle = title
        )
    }
}
