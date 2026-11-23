package com.example.property_listing_system.data.repository

import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSource
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.validation.PropertyValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * PropertyRepository serves as the single source of truth for property data.
 * Architecture layer: UI -> ViewModel -> Repository -> Room Database (Data)
 */
interface PropertyRepository {
    fun getAllProperties(): Flow<List<PropertyEntity>>
    fun getPropertyById(id: Long): Flow<PropertyEntity?>
    suspend fun getPropertyByIdOnce(id: Long): PropertyEntity?
    fun searchProperties(query: String): Flow<List<PropertyEntity>>
    fun getPropertiesByType(type: String): Flow<List<PropertyEntity>>
    fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>>
    fun getFavoriteProperties(): Flow<List<PropertyEntity>>
    fun getFeaturedProperties(): Flow<List<PropertyEntity>>
    fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>>
    fun getPropertyCount(): Flow<Int>
    fun getAvailablePropertyCount(): Flow<Int>
    fun getAllPropertyTypes(): Flow<List<String>>

    suspend fun addProperty(property: PropertyEntity): Result<Long>
    suspend fun addProperties(properties: List<PropertyEntity>): Result<List<Long>>
    suspend fun updateProperty(property: PropertyEntity): Result<Unit>
    suspend fun toggleFavorite(id: Long, isFavorite: Boolean): Result<Unit>
    suspend fun updateStatus(id: Long, status: String): Result<Unit>
    suspend fun updatePrice(id: Long, newPrice: Double): Result<Unit>
    suspend fun deleteProperty(property: PropertyEntity): Result<Unit>
    suspend fun deletePropertyById(id: Long): Result<Unit>
    suspend fun clearAllProperties(): Result<Unit>
}

class PropertyRepositoryImpl(
    private val localDataSource: PropertyLocalDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PropertyRepository {

    override fun getAllProperties(): Flow<List<PropertyEntity>> =
        localDataSource.getAllProperties()

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> =
        localDataSource.getPropertyById(id)

    override suspend fun getPropertyByIdOnce(id: Long): PropertyEntity? =
        withContext(ioDispatcher) {
            localDataSource.getPropertyByIdOnce(id)
        }

    override fun searchProperties(query: String): Flow<List<PropertyEntity>> =
        localDataSource.searchProperties(query)

    override fun getPropertiesByType(type: String): Flow<List<PropertyEntity>> =
        localDataSource.getPropertiesByType(type)

    override fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>> =
        localDataSource.getPropertiesByPriceRange(minPrice, maxPrice)

    override fun getFavoriteProperties(): Flow<List<PropertyEntity>> =
        localDataSource.getFavoriteProperties()

    override fun getFeaturedProperties(): Flow<List<PropertyEntity>> =
        localDataSource.getFeaturedProperties()

    override fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>> =
        localDataSource.getPropertiesByStatus(status)

    override fun getPropertyCount(): Flow<Int> =
        localDataSource.getPropertyCount()

    override fun getAvailablePropertyCount(): Flow<Int> =
        localDataSource.getAvailablePropertyCount()

    override fun getAllPropertyTypes(): Flow<List<String>> =
        localDataSource.getAllPropertyTypes()

    override suspend fun addProperty(property: PropertyEntity): Result<Long> =
        withContext(ioDispatcher) {
            val validation = PropertyValidator.validate(property)
            if (!validation.isValid) {
                return@withContext Result.failure(
                    IllegalArgumentException(validation.errorMessage ?: "Invalid property data.")
                )
            }
            try {
                val rowId = localDataSource.insertProperty(property)
                Result.success(rowId)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun addProperties(properties: List<PropertyEntity>): Result<List<Long>> =
        withContext(ioDispatcher) {
            try {
                val ids = localDataSource.insertProperties(properties)
                Result.success(ids)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateProperty(property: PropertyEntity): Result<Unit> =
        withContext(ioDispatcher) {
            val validation = PropertyValidator.validate(property)
            if (!validation.isValid) {
                return@withContext Result.failure(
                    IllegalArgumentException(validation.errorMessage ?: "Invalid property data.")
                )
            }
            try {
                val updated = localDataSource.updateProperty(property)
                if (updated > 0) Result.success(Unit)
                else Result.failure(NoSuchElementException("Property with ID ${property.id} not found."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun toggleFavorite(id: Long, isFavorite: Boolean): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val count = localDataSource.updateFavoriteStatus(id, isFavorite)
                if (count > 0) Result.success(Unit)
                else Result.failure(NoSuchElementException("Property with ID $id not found."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updateStatus(id: Long, status: String): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val count = localDataSource.updatePropertyStatus(id, status)
                if (count > 0) Result.success(Unit)
                else Result.failure(NoSuchElementException("Property with ID $id not found."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updatePrice(id: Long, newPrice: Double): Result<Unit> =
        withContext(ioDispatcher) {
            if (newPrice <= 0) {
                return@withContext Result.failure(
                    IllegalArgumentException("Listing price must be greater than zero.")
                )
            }
            try {
                val count = localDataSource.updatePropertyPrice(id, newPrice)
                if (count > 0) Result.success(Unit)
                else Result.failure(NoSuchElementException("Property with ID $id not found."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deleteProperty(property: PropertyEntity): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                localDataSource.deleteProperty(property)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun deletePropertyById(id: Long): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                val count = localDataSource.deletePropertyById(id)
                if (count > 0) Result.success(Unit)
                else Result.failure(NoSuchElementException("Property with ID $id not found."))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun clearAllProperties(): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                localDataSource.deleteAllProperties()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
