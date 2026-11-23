# Property Listing System

A modern, offline-first Android application for property discovery, valuation, and management built with Jetpack Compose, Kotlin Coroutines, Flow, and Room Database.

## Member 2: Database Engineer Deliverables

### Responsibilities Completed:
- **Room Database Architecture:** Built thread-safe Singleton `AppDatabase` with pre-population callback and custom `TypeConverters`.
- **Property Entity & DAO:** Defined `PropertyEntity` indexed for high-performance querying and `PropertyDao` exposing complete CRUD operations.
- **Reactive Data Flow:** Utilized Kotlin Coroutines `Flow` for real-time observable queries and `suspend` functions for asynchronous write operations.
- **Data Validation & Repository:** Implemented `PropertyValidator` and `PropertyRepository` (UI → ViewModel → Repository → Data).
- **Interactive CRUD Demo Screen:** Developed `Member2DatabaseDemoScreen` in Jetpack Compose to demonstrate live Create, Read, Search, Filter, Update, and Delete operations.
- **Unit & Instrumented Testing:** Implemented automated test suites in `PropertyValidatorTest`, `PropertyRepositoryTest`, and `PropertyDaoInstrumentedTest`.
- **Documentation & Technical Defence:** Detailed documentation available in [MEMBER_2_DOCUMENTATION.md](file:///C:/Users/mahav/Desktop/Property%20Listing%20System/MEMBER_2_DOCUMENTATION.md).

### How to Run Tests:
```bash
./gradlew testDebugUnitTest
```

