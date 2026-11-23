# Member 6: Network & Background Services Documentation

**Role:** Network & Background Engineer  
**Branch:** `feature/member-6-network-background`  
**CAT 2 Stage:** Any (Waits for Member 1)  

---

## 1. Overview & Assigned Tasks

Member 6 is responsible for building the networking, remote API, external geolocation/maps services, and WorkManager background job infrastructure for the **Property Listing System**:

- **External API Integration:**
  - Remote Property Valuation & Rental Yield Estimator API.
  - Property listings remote synchronization endpoint.
  - Geocoding and Location services.
  - Retrofit HTTP client with OkHttp logging and offline-ready mock engine.
- **WorkManager Background Processing:**
  - `PropertySyncWorker`: Scheduled background sync updating local properties from cloud repository.
  - `PropertyReminderWorker`: Scheduled inspection & viewing appointment alerts.
  - Notification Channels & high-priority alerts with Android 13+ permission support.
- **Location & Maps Integration:**
  - GPS device coordinates detection via Google Play Services Location (`FusedLocationProviderClient`).
  - Deep-link intent launcher opening Google Maps with pin coordinates and turn-by-turn navigation.

---

## 2. Architecture & File Structure

```
app/src/main/java/com/example/property_listing_system/
├── MainActivity.kt                                 # Hosts Member 6 demo & requests runtime permissions
├── background/
│   ├── notifications/
│   │   └── PropertyNotificationManager.kt          # Manages notification channels (Sync & Reminders)
│   ├── scheduler/
│   │   └── PropertyWorkManagerScheduler.kt         # Enqueues periodic sync, one-time sync & reminders
│   └── workers/
│       ├── PropertyReminderWorker.kt               # Worker executing scheduled viewing reminders
│       └── PropertySyncWorker.kt                   # Worker executing background cloud sync
├── data/
│   ├── location/
│   │   ├── MapIntentHelper.kt                      # Launches Google Maps / navigation intents
│   │   └── PropertyLocationManager.kt              # GPS location & geocoder helper
│   └── network/
│       ├── api/
│       │   ├── MockPropertyApiInterceptor.kt       # Algorithmic valuation engine & mock sync server
│       │   └── PropertyApiService.kt               # Retrofit API interface
│       ├── client/
│       │   ├── NetworkResult.kt                    # Sealed class (Success, Error, Loading)
│       │   └── RetrofitClient.kt                   # Retrofit & OkHttp singleton configuration
│       ├── models/
│       │   ├── LocationCoordinatesDto.kt           # Geocoding DTO
│       │   ├── PropertyValuationRequest.kt         # Valuation input parameters
│       │   ├── PropertyValuationResponse.kt        # Valuation calculation result DTO
│       │   └── RemotePropertyDto.kt                # Remote property model for sync
│       └── repository/
│           └── PropertyNetworkRepository.kt        # Coroutine-based network repository
└── ui/
    └── member6/
        ├── Member6DemoScreen.kt                    # Interactive Compose UI for CAT presentation
        └── Member6ViewModel.kt                     # StateFlow ViewModel managing Member 6 operations
```

---

## 3. Detailed Component Breakdown

### A. External API Integration (Property Valuation)
- **Interface:** `PropertyApiService.kt`
- **Client:** `RetrofitClient.kt`
- **Interceptor:** `MockPropertyApiInterceptor.kt`
  - Calculates realistic market valuation based on location rates (e.g. Westlands, Karen, Kilimani, Runda), square footage, bedroom/bathroom counts, and amenities (pool, garden, parking).
  - Calculates monthly rental yield estimates and market demand classification.
  - Emulates true HTTP network pipeline over OkHttp, allowing 100% offline testing and presentation without external server downtime.

### B. WorkManager Background Processing
- **Periodic Sync:** `PropertyWorkManagerScheduler.schedulePeriodicSync(24)`
  - Constraints: `NetworkType.CONNECTED`, `requiresBatteryNotLow(true)`.
  - Runs in the background to fetch updated property listings.
- **Immediate One-Time Sync:** `PropertyWorkManagerScheduler.scheduleImmediateSync()`
  - Triggers on-demand synchronization and fires a notification upon completion.
- **Viewing Reminders:** `PropertyWorkManagerScheduler.schedulePropertyReminder(...)`
  - Schedules delayed alerts for client visits or inspections with custom delay (e.g., 10 seconds for demo).

### C. Location & Maps Integration
- **`PropertyLocationManager`**: Detects current GPS coordinates and performs forward/reverse geocoding.
- **`MapIntentHelper`**: Creates `geo:` and `google.navigation:` intents to open Google Maps directly to the property's coordinates.

---

## 4. Integration Guide for Other Group Members

### For Member 2 (Database Engineer) & Member 3 (State & Business Logic):
When saving remote properties to Room:
```kotlin
// In PropertySyncWorker or PropertyRepository:
val networkRepo = PropertyNetworkRepository()
val result = networkRepo.fetchRemoteProperties()
if (result is NetworkResult.Success) {
    val remoteList: List<RemotePropertyDto> = result.data
    // Map RemotePropertyDto to Room PropertyEntity and insert via DAO:
    // val entities = remoteList.map { it.toEntity() }
    // propertyDao.insertAll(entities)
}
```

### For Member 4 & 5 (Frontend Developers):
To calculate property valuations or open maps from any property detail screen:
```kotlin
// Launch map for property:
MapIntentHelper.openPropertyInMaps(context, property.latitude, property.longitude, property.title)

// Schedule viewing reminder:
val scheduler = PropertyWorkManagerScheduler(context)
scheduler.schedulePropertyReminder(
    propertyId = property.id,
    propertyTitle = property.title,
    message = "Inspection starts in 30 minutes",
    delaySeconds = 1800
)
```

---

## 5. How to Test & Demo for CAT 2

1. Open project in **Android Studio**.
2. Sync Gradle files (dependencies added in `libs.versions.toml` and `app/build.gradle.kts`).
3. Run app on an Android Emulator or physical device.
4. On launch, the **Member 6 Demo Screen** displays 4 interactive cards:
   - **Card 1 (Valuation API):** Select property type, bedrooms, enter location -> Tap **Calculate Valuation Estimate** to see real-time algorithmic valuation, monthly rent, and market demand.
   - **Card 2 (Background Sync):** Tap **Sync Now** -> Observe WorkManager state transition (`ENQUEUED` -> `RUNNING` -> `SUCCEEDED`) and see the sync notification.
   - **Card 3 (Viewing Reminder):** Tap **Schedule Alert** (default 10s delay) -> Wait 10 seconds to see the system heads-up notification trigger.
   - **Card 4 (Location & Maps):** Tap **Open in Maps** -> Automatically opens Google Maps centered on the property coordinates with a title pin.
5. Run unit tests in `Member6NetworkAndBackgroundTest.kt` to verify serialization and API interceptor accuracy.
