package com.example.property_listing_system.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.property_listing_system.data.local.entity.PropertyEntity
import kotlinx.coroutines.flow.Flow

/**
 * PropertyDao provides the Data Access Object (DAO) interface for Room.
 * It defines all SQL CRUD (Create, Read, Update, Delete) operations.
 *
 * Query execution rules:
 * - One-shot write/modify operations are marked 'suspend' and executed off the main thread.
 * - Read queries returning Flow<T> observe table changes reactively in real time.
 */
@Dao
interface PropertyDao {

    // ============================================================================
    // 1. CREATE (INSERT)
    // ============================================================================

    /**
     * Inserts a single property into the database.
     * Uses OnConflictStrategy.REPLACE to update if the primary key conflicts.
     *
     * @return The auto-generated row ID (Long).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyEntity): Long

    /**
     * Bulk inserts a list of properties (e.g. after sync from remote API).
     *
     * @return List of newly generated/affected row IDs.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<PropertyEntity>): List<Long>

    // ============================================================================
    // 2. READ (SELECT QUERIES)
    // ============================================================================

    /**
     * Observes all properties ordered by newest first.
     */
    @Query("SELECT * FROM properties ORDER BY createdTimestamp DESC")
    fun getAllProperties(): Flow<List<PropertyEntity>>

    /**
     * Observes a single property by its local primary key ID.
     */
    @Query("SELECT * FROM properties WHERE id = :id")
    fun getPropertyById(id: Long): Flow<PropertyEntity?>

    /**
     * Synchronous/one-shot fetch for a single property by ID.
     */
    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyByIdOnce(id: Long): PropertyEntity?

    /**
     * Look up a property by its remote sync ID (e.g. "REMOTE-001").
     */
    @Query("SELECT * FROM properties WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getPropertyByRemoteId(remoteId: String): PropertyEntity?

    /**
     * Full-text search across title, address, description, and city.
     */
    @Query("""
        SELECT * FROM properties 
        WHERE title LIKE '%' || :query || '%' 
           OR address LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%' 
           OR city LIKE '%' || :query || '%'
        ORDER BY createdTimestamp DESC
    """)
    fun searchProperties(query: String): Flow<List<PropertyEntity>>

    /**
     * Filters properties by type (e.g. "Apartment", "Villa", "Commercial").
     */
    @Query("SELECT * FROM properties WHERE propertyType = :propertyType ORDER BY price ASC")
    fun getPropertiesByType(propertyType: String): Flow<List<PropertyEntity>>

    /**
     * Filters properties within a price range (KES).
     */
    @Query("SELECT * FROM properties WHERE price BETWEEN :minPrice AND :maxPrice ORDER BY price ASC")
    fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>>

    /**
     * Observes all bookmarked/favorite properties.
     */
    @Query("SELECT * FROM properties WHERE isFavorite = 1 ORDER BY createdTimestamp DESC")
    fun getFavoriteProperties(): Flow<List<PropertyEntity>>

    /**
     * Observes all featured properties.
     */
    @Query("SELECT * FROM properties WHERE isFeatured = 1 ORDER BY createdTimestamp DESC")
    fun getFeaturedProperties(): Flow<List<PropertyEntity>>

    /**
     * Filters properties by availability status ("AVAILABLE", "RENTED", "SOLD", "UNDER_OFFER").
     */
    @Query("SELECT * FROM properties WHERE status = :status ORDER BY createdTimestamp DESC")
    fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>>

    /**
     * Count total number of properties stored in the local database.
     */
    @Query("SELECT COUNT(*) FROM properties")
    fun getPropertyCount(): Flow<Int>

    /**
     * Count available properties.
     */
    @Query("SELECT COUNT(*) FROM properties WHERE status = 'AVAILABLE'")
    fun getAvailablePropertyCount(): Flow<Int>

    /**
     * Returns a distinct list of property types currently in the database.
     */
    @Query("SELECT DISTINCT propertyType FROM properties ORDER BY propertyType ASC")
    fun getAllPropertyTypes(): Flow<List<String>>

    // ============================================================================
    // 3. UPDATE
    // ============================================================================

    /**
     * Updates an entire property record.
     *
     * @return Number of rows updated (1 if successful, 0 if not found).
     */
    @Update
    suspend fun updateProperty(property: PropertyEntity): Int

    /**
     * Toggles or updates the favorite status of a property.
     */
    @Query("UPDATE properties SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Int

    /**
     * Updates the lifecycle status ("AVAILABLE", "RENTED", "SOLD").
     */
    @Query("UPDATE properties SET status = :status WHERE id = :id")
    suspend fun updatePropertyStatus(id: Long, status: String): Int

    /**
     * Updates the listing price of a property.
     */
    @Query("UPDATE properties SET price = :newPrice WHERE id = :id")
    suspend fun updatePropertyPrice(id: Long, newPrice: Double): Int

    // ============================================================================
    // 4. DELETE
    // ============================================================================

    /**
     * Deletes a specific property entity.
     *
     * @return Number of rows deleted.
     */
    @Delete
    suspend fun deleteProperty(property: PropertyEntity): Int

    /**
     * Deletes a property by its primary key ID.
     */
    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun deletePropertyById(id: Long): Int

    /**
     * Clears all properties from the database (useful for resetting / fresh sync).
     */
    @Query("DELETE FROM properties")
    suspend fun deleteAllProperties(): Int
}
