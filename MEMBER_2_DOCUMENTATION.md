# Member 2: Database Engineer - Technical Documentation & Defence Guide

## Role Information
- **Role:** Database Engineer (Member 2)
- **Component Focus:** Data Persistence / Database (4 marks), CRUD Functionality (4 marks), Android Architecture & Data Flow (4 marks), Testing & Validation (2 marks), GitHub Collaboration (2 marks), Presentation & Technical Defence (2 marks).
- **Project:** Property Listing System (Android Mobile Application)

---

## 1. Executive Summary & Architecture Overview
As Member 2 (Database Engineer), my primary responsibility was architecting, implementing, and validating the local persistence engine of the Property Listing System using Google's **Android Room Persistence Library** built over SQLite.

The data layer follows Google's Recommended App Architecture:
```
[ UI Layer / Compose ] 
       │
       ▼
[ ViewModel Layer (Member 3 / Member 2 Demo) ]
       │
       ▼
[ Repository Pattern (PropertyRepository) ]
       │
       ▼
[ Local Data Source (PropertyLocalDataSource) ]
       │
       ▼
[ Data Access Object (PropertyDao) ]
       │
       ▼
[ SQLite Database via Room (AppDatabase) ]
```

---

## 2. Room Database Architecture Components

### A. Database Entity (`PropertyEntity.kt`)
Annotated with `@Entity(tableName = "properties")`.
- **Primary Key:** `@PrimaryKey(autoGenerate = true) val id: Long = 0L` ensures unique identity and auto-incrementing surrogate keys managed natively by SQLite.
- **Database Indexes:**
  - `remoteId`: Accelerated lookups during remote synchronization (Member 6 sync).
  - `propertyType`: Index for categorized filtering (Apartment, Villa, Commercial, etc.).
  - `price`: B-tree indexed range scans for budget filtering.
  - `status`: Index for lifecycle queries (AVAILABLE, RENTED, SOLD).
  - `isFavorite`: Indexed boolean column for instant bookmark filtering.
- **Real-World Attributes:** Title, description, price, currency (KES), address, city, latitude, longitude, bedrooms, bathrooms, square footage, image URL, contact phone, contact email, featured flag, favorite flag, lifecycle status, creation timestamp, and remote ID.

### B. Data Access Object (`PropertyDao.kt`)
The DAO is an abstract interface where Room generates type-safe SQLite implementation code at compile time using Kotlin Symbol Processing (KSP).

#### 1. Create (Insert)
```kotlin
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertProperty(property: PropertyEntity): Long

@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insertProperties(properties: List<PropertyEntity>): List<Long>
```
- **Rationale:** Returns the generated row ID; `OnConflictStrategy.REPLACE` handles idempotent upsert operations seamlessly during batch synchronization.

#### 2. Read (Query)
- **`getAllProperties(): Flow<List<PropertyEntity>>`**:
  Observes all properties sorted newest first. Emits real-time reactive updates whenever SQLite table contents change.
- **`getPropertyById(id: Long): Flow<PropertyEntity?>`**:
  Reactive lookup for detailed property screens.
- **`getPropertyByIdOnce(id: Long): PropertyEntity?`**:
  One-shot suspend query for background workers or immediate validation.
- **`searchProperties(query: String): Flow<List<PropertyEntity>>`**:
  Performs SQL `LIKE` wildcard matching across `title`, `address`, `description`, and `city`.
- **`getPropertiesByType(propertyType: String): Flow<List<PropertyEntity>>`**:
  Categorical filtering sorted by ascending price.
- **`getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>>`**:
  Range evaluation between minimum and maximum bounds.
- **`getFavoriteProperties(): Flow<List<PropertyEntity>>`**:
  Retrieves bookmarked properties (`isFavorite = 1`).
- **`getPropertyCount(): Flow<Int>` & `getAvailablePropertyCount(): Flow<Int>`**:
  Statistical aggregations via SQL `COUNT(*)`.

#### 3. Update
```kotlin
@Update
suspend fun updateProperty(property: PropertyEntity): Int

@Query("UPDATE properties SET isFavorite = :isFavorite WHERE id = :id")
suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Int

@Query("UPDATE properties SET status = :status WHERE id = :id")
suspend fun updatePropertyStatus(id: Long, status: String): Int

@Query("UPDATE properties SET price = :newPrice WHERE id = :id")
suspend fun updatePropertyPrice(id: Long, newPrice: Double): Int
```
- **Rationale:** Fine-grained SQL updates mutate only targeted columns without transferring or rewriting unneeded fields. Returns rows affected.

#### 4. Delete
```kotlin
@Delete
suspend fun deleteProperty(property: PropertyEntity): Int

@Query("DELETE FROM properties WHERE id = :id")
suspend fun deletePropertyById(id: Long): Int

@Query("DELETE FROM properties")
suspend fun deleteAllProperties(): Int
```
- **Rationale:** Supports entity removal, ID-based targeted deletion, and full cache invalidation.

---

### C. Database Class (`AppDatabase.kt`)
- Inherits from `RoomDatabase()`.
- Implements thread-safe **Double-Checked Locking Singleton Pattern** to prevent multiple expensive database instances from opening concurrent SQLite connections.
- **Pre-Population Callback (`RoomDatabase.Callback()`):**
  When the database is created for the first time on device, `onCreate(db)` fires asynchronously on `Dispatchers.IO` to populate 6 realistic Kenyan real estate listings (Westlands, Karen, Kilimani, Lavington, Upper Hill, Nyali Mombasa).
  - *Compliance Rule:* The project avoids hard-coded UI data while guaranteeing a rich user experience on initial launch via true SQLite persistence.

### D. Type Converters (`Converters.kt`)
- Converts `java.util.Date` ↔ `Long` (Unix Epoch timestamp).
- Converts `List<String>` ↔ comma-separated `String` for amenity tags.

### E. Validation Layer (`PropertyValidator.kt`)
- Validates data integrity before writing to Room:
  - Title minimum length (>= 3 chars).
  - Description non-empty.
  - Price strictly positive (> 0).
  - Address required.
  - Bedrooms/bathrooms non-negative.
  - Phone number validation (Kenyan mobile formats: `+254...`, `07...`, `01...`).

---

## 3. Concurrency, Coroutines & Reactive Data Flow
1. **Why `suspend`?**
   Room forbids database operations on the Main/UI thread because disk I/O operations can block frame rendering (causing ANR - Application Not Responding). Every one-shot operation is marked `suspend` and runs on `Dispatchers.IO`.
2. **Why Kotlin `Flow` for queries?**
   Room automatically integrates with SQLite table invalidation trackers. Whenever an `INSERT`, `UPDATE`, or `DELETE` is committed on the `properties` table, Room notifies active `Flow` collectors, automatically emitting new data to the UI without manual refresh calls.

---

## 4. Technical Defence & Oral Examination Q&A

### Q1: What is Room, and why did you choose it over SQLiteOpenHelper?
**Answer:** Room is an abstraction layer over SQLite that provides compile-time verification of SQL queries, eliminates boilerplate cursor mapping, and natively integrates with Kotlin Coroutines and Flow for reactive stream emission. Direct SQLiteOpenHelper lacks compile-time syntax checks and requires manual schema migration and cursor parsing.

### Q2: Why are some DAO methods marked `suspend` while query methods return `Flow` without `suspend`?
**Answer:** Write operations (`INSERT`, `UPDATE`, `DELETE`) are one-shot tasks that execute once and suspend execution until disk I/O completes off the main thread. Query methods returning `Flow` are cold asynchronous data streams; they do not perform work until collected, and Room internally manages dispatching queries to a background thread pool and re-emitting whenever the underlying table changes.

### Q3: How did you ensure thread safety when instantiating the database?
**Answer:** I used the Double-Checked Locking Singleton pattern with `@Volatile private var INSTANCE: AppDatabase? = null`. The `@Volatile` annotation guarantees memory visibility across threads, and `synchronized(this)` ensures only one thread can initialize the database instance if multiple concurrent threads request access simultaneously.

### Q4: How does your database implementation adhere to the rule against "hard-coded data"?
**Answer:** Instead of hard-coding static lists in UI Composables or Activities, all initial records are pre-populated into the SQLite database file via `RoomDatabase.Callback().onCreate()`. They are queried and mutated as true persistent database records, supporting full CRUD, search, and deletion.

### Q5: How do indexes improve database performance in your schema?
**Answer:** Without indexes, SQLite performs a full table scan ($O(N)$) for every query filter. By defining indexes on `price`, `propertyType`, and `status`, SQLite maintains B-tree lookup structures ($O(\log N)$), significantly accelerating range queries, sorting, and category filters.

### Q6: How does Member 2's database layer connect to Member 3 (ViewModel) and Member 6 (Network/Sync)?
**Answer:** Through the Repository pattern (`PropertyRepository`). Member 3's `PropertyViewModel` consumes clean `Flow<List<PropertyEntity>>` streams and calls suspend functions. Member 6's sync worker maps incoming `RemotePropertyDto` objects to `PropertyEntity` via `PropertyMapper` and calls `insertProperties()`, which immediately updates the UI via reactive Flow emissions.

### Q7: What testing was performed on the database?
**Answer:** 
1. `PropertyValidatorTest` verifies that invalid inputs (negative prices, invalid phone numbers, empty titles) are rejected before database commits.
2. `PropertyRepositoryTest` verifies repository CRUD operations and reactive flow emissions using coroutines test dispatchers.
3. `PropertyDaoInstrumentedTest` runs an in-memory SQLite database (`Room.inMemoryDatabaseBuilder`) to verify raw SQL queries, search queries, filtering, and cascade updates.

---

## 5. Artifacts and Files Delivered by Member 2
- `data/local/entity/PropertyEntity.kt` - Room Entity with indexes and status constants
- `data/local/dao/PropertyDao.kt` - Complete SQL CRUD queries with reactive Flow and suspend functions
- `data/local/database/AppDatabase.kt` - Singleton RoomDatabase with pre-population callback
- `data/local/database/Converters.kt` - Room TypeConverters
- `data/local/datasource/PropertyLocalDataSource.kt` - Local data source abstraction
- `data/repository/PropertyRepository.kt` - Repository pattern interface & implementation
- `data/validation/PropertyValidator.kt` - Input validation and integrity rules
- `data/mapper/PropertyMapper.kt` - Entity-to-DTO mapper helper
- `ui/member2/Member2DatabaseViewModel.kt` - State management and coroutine orchestration
- `ui/member2/Member2DatabaseDemoScreen.kt` - Interactive Compose UI to test and demonstrate live CRUD
- `test/PropertyValidatorTest.kt` - Unit tests for validation
- `test/PropertyRepositoryTest.kt` - Unit tests for repository
- `androidTest/PropertyDaoInstrumentedTest.kt` - SQLite in-memory instrumented DAO tests
- `MEMBER_2_DOCUMENTATION.md` - Complete technical defence and architecture guide
