# Property Listing System

A modern, offline-first Android application for property discovery, valuation, and management built with Jetpack Compose, Kotlin Coroutines, Flow, Room Database, and WorkManager.

## Group Project Execution Plan

| Member / Role | Stage | Waits For | Tasks & Specifications | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Member 1: Project Lead & GitHub Manager** | 1 & 6 | — | • Create shared GitHub repo (ui/data packages).<br>• Review code & approve Pull Requests.<br>• Compile signed APK & write README. | Initial Setup Complete |
| **Member 2: Database Engineer** | 3 | Member 1 | • Build Room database architecture.<br>• Create Property Entity and DAO.<br>• Implement CRUD SQL queries. | **Completed (Branch: `feature/member-2-database-engineer`)** |
| **Member 3: State & Business Logic** | 2 | Member 1 | • Build PropertyViewModel with LiveData.<br>• Set up Repository class (UI → ViewModel → Repo → Data). | **Completed (Branch: `feature/member3-viewmodel`)** |
| **Member 4: Frontend Developer A** | 4 | Members 1 & 3 | • Develop UI for Login, Registration, Dashboard.<br>• Implement navigation graph and back navigation. | **Completed (Branch: `feature/screens`)** |
| **Member 5: Frontend Developer B** | 4 | Members 1 & 3 | • Build dynamic data display (RecyclerView) for properties.<br>• Create interactive input forms (Add/Edit Property). | **Completed (`26c74fa`)** |
| **Member 6: Network & Background** | Any | Member 1 | • Integrate external API (e.g., maps, location, mock valuation).<br>• Implement WorkManager for background sync/reminders. | **Completed (Branch: `feature/member-6-network-background`)** |
| **Member 7: Integration, Validation & QA** | 5 | Members 2, 3, 4, 5, 6 | • Ensure properties correctly save to Room via ViewModels.<br>• Implement input validation and error states.<br>• Coordinate final presentation and demo. | **Completed (Branch: `feature/member-7-integration-qa`)** |

---

## Member 7: Integration, Validation & QA Deliverables
**Author:** AlvisKamau (`alvis.kamau@strathmore.edu`)  
**Branch:** `feature/member-7-integration-qa`

1. **Unified Navigation Graph:** Connected Member 4 (Auth & Dashboard), Member 5 (RecyclerView & Forms), Member 6 (Valuation & WorkManager), and Member 2 (Room DB) into a unified navigation architecture (`nav_graph.xml`).
2. **Comprehensive Validation Engine:** Implemented `PropertyValidator` and `UserValidator` to enforce business logic (title length, price boundaries, Kenyan phone formats, email regex) with real-time UI error indicators.
3. **End-to-End ViewModel & Room Persistence:** Ensured interactive forms save seamlessly through `PropertyViewModel` to the underlying Room Database with reactive StateFlow updates.
4. **Automated QA Test Suite:** Authored `Member7IntegrationQATest.kt` covering cross-component validation, authentication checks, and database repository flows.
5. **Presentation Runbook:** Authored a step-by-step demonstration runbook in `MEMBER_7_DOCUMENTATION.md` for the final CAT 2 presentation.

---

## Member 2: Database Engineer Deliverables

Member 2 has completed all assigned specifications on branch `feature/member-2-database-engineer`:
1. **Room Database Architecture:** Built thread-safe Singleton `AppDatabase` with pre-population callback and custom `TypeConverters`.
2. **Property Entity & DAO:** Defined `PropertyEntity` indexed for high-performance querying and `PropertyDao` exposing complete CRUD operations.
3. **Reactive Data Flow:** Utilized Kotlin Coroutines `Flow` for real-time observable queries and `suspend` functions for asynchronous write operations.
4. **Data Validation & Repository:** Implemented `PropertyValidator` and `PropertyRepository` (UI → ViewModel → Repository → Data).
5. **Interactive CRUD Demo Screen:** Developed `Member2DatabaseDemoScreen` in Jetpack Compose to demonstrate live Create, Read, Search, Filter, Update, and Delete operations.
6. **Unit & Instrumented Testing:** Implemented automated test suites in `PropertyValidatorTest`, `PropertyRepositoryTest`, and `PropertyDaoInstrumentedTest`.

For full architecture and technical defence details, see [MEMBER_2_DOCUMENTATION.md](MEMBER_2_DOCUMENTATION.md).

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

---

## Running Tests
```bash
./gradlew testDebugUnitTest
```
