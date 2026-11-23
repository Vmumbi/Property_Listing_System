package com.example.property_listing_system

import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSource
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepository
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PropertyRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeDataSource: FakePropertyLocalDataSource
    private lateinit var repository: PropertyRepository

    private val sampleProperty = PropertyEntity(
        id = 1L,
        title = "Karen Villa with Pool",
        description = "4 bedroom luxury home on half acre garden.",
        price = 65000000.0,
        currency = "KES",
        propertyType = PropertyEntity.TYPE_VILLA,
        address = "Mbagathi Way, Karen",
        city = "Nairobi",
        bedrooms = 4,
        bathrooms = 4,
        squareFootage = 3800.0,
        contactPhone = "+254722112233",
        status = PropertyEntity.STATUS_AVAILABLE
    )

    @Before
    fun setup() {
        fakeDataSource = FakePropertyLocalDataSource()
        repository = PropertyRepositoryImpl(fakeDataSource, testDispatcher)
    }

    @Test
    fun addProperty_withValidData_succeedsAndInsertsIntoDatabase() = runTest(testDispatcher) {
        val result = repository.addProperty(sampleProperty)
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())

        val allProperties = repository.getAllProperties().first()
        assertEquals(1, allProperties.size)
        assertEquals("Karen Villa with Pool", allProperties[0].title)
    }

    @Test
    fun addProperty_withInvalidData_failsWithValidationException() = runTest(testDispatcher) {
        val invalidProperty = sampleProperty.copy(price = -100.0)
        val result = repository.addProperty(invalidProperty)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun toggleFavorite_updatesFavoriteState() = runTest(testDispatcher) {
        repository.addProperty(sampleProperty)

        val toggleResult = repository.toggleFavorite(1L, true)
        assertTrue(toggleResult.isSuccess)

        val updated = repository.getPropertyById(1L).first()
        assertTrue(updated!!.isFavorite)
    }

    @Test
    fun updateStatus_modifiesPropertyStatus() = runTest(testDispatcher) {
        repository.addProperty(sampleProperty)

        val updateResult = repository.updateStatus(1L, PropertyEntity.STATUS_SOLD)
        assertTrue(updateResult.isSuccess)

        val updated = repository.getPropertyById(1L).first()
        assertEquals(PropertyEntity.STATUS_SOLD, updated!!.status)
    }

    @Test
    fun deleteProperty_removesItemFromDatabase() = runTest(testDispatcher) {
        repository.addProperty(sampleProperty)
        assertEquals(1, repository.getAllProperties().first().size)

        val deleteResult = repository.deletePropertyById(1L)
        assertTrue(deleteResult.isSuccess)

        assertEquals(0, repository.getAllProperties().first().size)
    }

    // In-memory fake local data source
    private class FakePropertyLocalDataSource : PropertyLocalDataSource {
        private val list = mutableListOf<PropertyEntity>()

        override fun getAllProperties(): Flow<List<PropertyEntity>> = flowOf(list.toList())
        override fun getPropertyById(id: Long): Flow<PropertyEntity?> = flowOf(list.find { it.id == id })
        override suspend fun getPropertyByIdOnce(id: Long): PropertyEntity? = list.find { it.id == id }
        override suspend fun getPropertyByRemoteId(remoteId: String): PropertyEntity? = list.find { it.remoteId == remoteId }
        override fun searchProperties(query: String): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.title.contains(query, ignoreCase = true) })
        override fun getPropertiesByType(type: String): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.propertyType.equals(type, ignoreCase = true) })
        override fun getPropertiesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.price in minPrice..maxPrice })
        override fun getFavoriteProperties(): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.isFavorite })
        override fun getFeaturedProperties(): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.isFeatured })
        override fun getPropertiesByStatus(status: String): Flow<List<PropertyEntity>> =
            flowOf(list.filter { it.status == status })
        override fun getPropertyCount(): Flow<Int> = flowOf(list.size)
        override fun getAvailablePropertyCount(): Flow<Int> =
            flowOf(list.count { it.status == PropertyEntity.STATUS_AVAILABLE })
        override fun getAllPropertyTypes(): Flow<List<String>> =
            flowOf(list.map { it.propertyType }.distinct())

        override suspend fun insertProperty(property: PropertyEntity): Long {
            list.removeAll { it.id == property.id }
            list.add(property)
            return property.id
        }

        override suspend fun insertProperties(properties: List<PropertyEntity>): List<Long> {
            properties.forEach { insertProperty(it) }
            return properties.map { it.id }
        }

        override suspend fun updateProperty(property: PropertyEntity): Int {
            val idx = list.indexOfFirst { it.id == property.id }
            return if (idx >= 0) {
                list[idx] = property
                1
            } else 0
        }

        override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean): Int {
            val prop = list.find { it.id == id } ?: return 0
            val idx = list.indexOf(prop)
            list[idx] = prop.copy(isFavorite = isFavorite)
            return 1
        }

        override suspend fun updatePropertyStatus(id: Long, status: String): Int {
            val prop = list.find { it.id == id } ?: return 0
            val idx = list.indexOf(prop)
            list[idx] = prop.copy(status = status)
            return 1
        }

        override suspend fun updatePropertyPrice(id: Long, newPrice: Double): Int {
            val prop = list.find { it.id == id } ?: return 0
            val idx = list.indexOf(prop)
            list[idx] = prop.copy(price = newPrice)
            return 1
        }

        override suspend fun deleteProperty(property: PropertyEntity): Int {
            return if (list.removeIf { it.id == property.id }) 1 else 0
        }

        override suspend fun deletePropertyById(id: Long): Int {
            return if (list.removeIf { it.id == id }) 1 else 0
        }

        override suspend fun deleteAllProperties(): Int {
            val count = list.size
            list.clear()
            return count
        }
    }
}
