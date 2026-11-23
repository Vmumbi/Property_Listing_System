# Property_Listing_System
This project follows the development of a property listing system.

## Group Project Execution Plan

| Member / Role | Stage | Waits For | Tasks & Specifications | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Member 1: Project Lead & GitHub Manager** | 1 & 6 | — | • Create shared GitHub repo (ui/data packages).<br>• Review code & approve Pull Requests.<br>• Compile signed APK & write README. | Initial Setup Complete |
| **Member 2: Database Engineer** | 3 | Member 1 | • Build Room database architecture.<br>• Create Property Entity and DAO.<br>• Implement CRUD SQL queries. | Pending |
| **Member 3: State & Business Logic** | 2 | Member 1 | • Build PropertyViewModel with LiveData.<br>• Set up Repository class (UI → ViewModel → Repo → Data). | Pending |
| **Member 4: Frontend Developer A** | 4 | Members 1 & 3 | • Develop UI for Login, Registration, Dashboard.<br>• Implement navigation graph and back navigation. | Pending |
| **Member 5: Frontend Developer B** | 4 | Members 1 & 3 | • Build dynamic data display (RecyclerView) for properties.<br>• Create interactive input forms (Add/Edit Property). | Pending |
| **Member 6: Network & Background** | Any | Member 1 | • Integrate external API (e.g., maps, location, mock valuation).<br>• Implement WorkManager for background sync/reminders. | **Completed (Branch: `feature/member-6-network-background`)** |
| **Member 7: Integration, Validation & QA** | 5 | Members 2, 3, 4, 5, 6 | • Ensure properties correctly save to Room via ViewModels.<br>• Implement input validation and error states.<br>• Coordinate final presentation and demo. | Pending |

---

## Member 6: Completed Features

Member 6 has completed all assigned specifications on branch `feature/member-6-network-background`:
1. **External API Integration**:
   - Algorithmic Property Valuation & Rental Rate Estimator using Retrofit and OkHttp.
   - Remote property listings synchronization endpoint.
   - Geocoding API integration.
2. **WorkManager Background Processing**:
   - `PropertySyncWorker`: Background periodic (24h) and immediate cloud listing synchronization with battery and network constraints.
   - `PropertyReminderWorker`: Delayed notifications for property viewing appointments, inspections, and client follow-ups.
   - Dual-channel notification system (`CHANNEL_SYNC`, `CHANNEL_REMINDERS`) compatible with Android 13+ runtime permissions.
3. **Location & Maps**:
   - `PropertyLocationManager`: Device coordinates detection via Google Play Services Location.
   - `MapIntentHelper`: One-click deep link to Google Maps with property pins and turn-by-turn navigation.
4. **Interactive Demo Screen & Unit Tests**:
   - Full Jetpack Compose demonstration screen (`Member6DemoScreen`) integrated in `MainActivity.kt`.
   - Comprehensive unit test suite in `Member6NetworkAndBackgroundTest.kt`.

For full architecture and integration details, see [MEMBER_6_DOCUMENTATION.md](MEMBER_6_DOCUMENTATION.md).
