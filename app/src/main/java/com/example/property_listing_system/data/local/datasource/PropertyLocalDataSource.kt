package com.example.property_listing_system.data.local.datasource

import com.example.property_listing_system.data.local.dao.PropertyDao
import com.example.property_listing_system.data.local.entity.PropertyEntity
import kotlinx.coroutines.flow.Flow

/**
 * PropertyLocalDataSource encapsulates direct access to the Room DAO layer.
 */
interface PropertyLocalDataSource {
    fun getAllProperties(): Flow<List<PropertyEntity>>
    fun getPropertyById(id: Long): Flow<PropertyEntity?>
    suspend fun getPropertyByIdOnce(id: Long): PropertyEntity?
    suspend fun getPropertyByRemoteId(remoteId: String): PropertyEntity?
    fun searchProperties(query: String): Flow<List<PropertyEntity>>
    fun getPropertiesByType(type: String): Flow<List<PropertyEntity>>
    fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>>
    fun getFavoriteProperties(): Flow<List<PropertyEntity>>
    fun getFeaturedProperties(): Flow<List<PropertyEntity>>
    fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>>
    fun getPropertyCount(): Flow<Int>
    fun getAvailablePropertyCount(): Flow<Int>
    fun getAllPropertyTypes(): Flow<List<String>>

    suspend fun insertProperty(property: PropertyEntity): Long
    suspend fun insertProperties(properties: List<PropertyEntity>): List<Long>
    suspend fun updateProperty(property: PropertyEntity): Int
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Int
    suspend fun updatePropertyStatus(id: Long, status: String): Int
    suspend fun updatePropertyPrice(id: Long, newPrice: Double): Int
    suspend fun deleteProperty(property: PropertyEntity): Int
    suspend fun deletePropertyById(id: Long): Int
    suspend fun deleteAllProperties(): Int
}

class PropertyLocalDataSourceImpl(
    private val propertyDao: PropertyDao
) : PropertyLocalDataSource {

    override fun getAllProperties(): Flow<List<PropertyEntity>> =
        propertyDao.getAllProperties()

    override fun getPropertyById(id: Long): Flow<PropertyEntity?> =
        propertyDao.getPropertyById(id)

    override suspend fun getPropertyByIdOnce(id: Long): PropertyEntity? =
        propertyDao.getPropertyByIdOnce(id)

    override suspend fun getPropertyByRemoteId(remoteId: String): PropertyEntity? =
        propertyDao.getPropertyByRemoteId(remoteId)

    override fun searchProperties(query: String): Flow<List<PropertyEntity>> =
        propertyDao.searchProperties(query)

    override fun getPropertiesByType(type: String): Flow<List<PropertyEntity>> =
        propertyDao.getPropertiesByType(type)

    override fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>> =
        propertyDao.getPropertiesByPriceRange(minPrice, maxPrice)

    override fun getFavoriteProperties(): Flow<List<PropertyEntity>> =
        propertyDao.getFavoriteProperties()

    override fun getFeaturedProperties(): Flow<List<PropertyEntity>> =
        propertyDao.getFeaturedProperties()

    override fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>> =
        propertyDao.getPropertiesByStatus(status)

    override fun getPropertyCount(): Flow<Int> =
        propertyDao.getPropertyCount()

    override fun getAvailablePropertyCount(): Flow<Int> =
        propertyDao.getAvailablePropertyCount()

    override fun getAllPropertyTypes(): Flow<List<String>> =
        propertyDao.getAllPropertyTypes()

    override suspend fun insertProperty(property: PropertyEntity): Long =
        propertyDao.insertProperty(property)

    override suspend fun insertProperties(properties: List<PropertyEntity>): List<Long> =
        propertyDao.insertProperties(properties)

    override suspend fun updateProperty(property: PropertyEntity): Int =
        propertyDao.updateProperty(property)

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Int =
        propertyDao.updateFavoriteStatus(id, isFavorite)

    override suspend fun updatePropertyStatus(id: Long, status: String): Int =
        propertyDao.updatePropertyStatus(id, status)

    override suspend fun updatePropertyPrice(id: Long, newPrice: Double): Int =
        propertyDao.updatePropertyPrice(id, newPrice)

    override suspend fun deleteProperty(property: PropertyEntity): Int =
        propertyDao.deleteProperty(property)

    override suspend fun deletePropertyById(id: Long): Int =
        propertyDao.deletePropertyById(id)

    override suspend fun deleteAllProperties(): Int =
        propertyDao.deleteAllProperties()
}
