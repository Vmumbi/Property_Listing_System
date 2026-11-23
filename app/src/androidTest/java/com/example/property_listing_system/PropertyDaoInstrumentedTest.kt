package com.example.property_listing_system

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.property_listing_system.data.local.dao.PropertyDao
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.entity.PropertyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test verifying Room SQLite DAO operations using an in-memory database.
 */
@RunWith(AndroidJUnit4::class)
class PropertyDaoInstrumentedTest {

    private lateinit var propertyDao: PropertyDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        propertyDao = db.propertyDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveProperty() = runBlocking {
        val property = PropertyEntity(
            title = "Westlands Penthouse",
            description = "3 bedroom penthouse.",
            price = 22000000.0,
            currency = "KES",
            propertyType = PropertyEntity.TYPE_APARTMENT,
            address = "School Lane, Westlands",
            city = "Nairobi",
            bedrooms = 3,
            bathrooms = 2,
            contactPhone = "+254712345678"
        )
        val id = propertyDao.insertProperty(property)
        assertTrue(id > 0)

        val retrieved = propertyDao.getPropertyByIdOnce(id)
        assertNotNull(retrieved)
        assertEquals("Westlands Penthouse", retrieved!!.title)
        assertEquals(22000000.0, retrieved.price, 0.001)
    }

    @Test
    fun searchAndFilterProperties() = runBlocking {
        val prop1 = PropertyEntity(
            title = "Karen Luxury Villa",
            description = "Garden villa.",
            price = 55000000.0,
            propertyType = PropertyEntity.TYPE_VILLA,
            address = "Karen Road",
            city = "Nairobi"
        )
        val prop2 = PropertyEntity(
            title = "Kilimani Modern Apartment",
            description = "Apartment near mall.",
            price = 15000000.0,
            propertyType = PropertyEntity.TYPE_APARTMENT,
            address = "Kilimani",
            city = "Nairobi"
        )
        propertyDao.insertProperties(listOf(prop1, prop2))

        val searchResult = propertyDao.searchProperties("Karen").first()
        assertEquals(1, searchResult.size)
        assertEquals("Karen Luxury Villa", searchResult[0].title)

        val apartmentResult = propertyDao.getPropertiesByType(PropertyEntity.TYPE_APARTMENT).first()
        assertEquals(1, apartmentResult.size)
        assertEquals("Kilimani Modern Apartment", apartmentResult[0].title)

        val priceFiltered = propertyDao.getPropertiesByPriceRange(10000000.0, 20000000.0).first()
        assertEquals(1, priceFiltered.size)
        assertEquals("Kilimani Modern Apartment", priceFiltered[0].title)
    }

    @Test
    fun updateFavoriteAndStatus() = runBlocking {
        val property = PropertyEntity(
            title = "Townhouse in Lavington",
            description = "4 bed townhouse.",
            price = 32000000.0,
            propertyType = PropertyEntity.TYPE_TOWNHOUSE,
            address = "Lavington",
            city = "Nairobi",
            status = PropertyEntity.STATUS_AVAILABLE,
            isFavorite = false
        )
        val id = propertyDao.insertProperty(property)

        // Toggle Favorite
        val favUpdated = propertyDao.updateFavoriteStatus(id, true)
        assertEquals(1, favUpdated)
        val favs = propertyDao.getFavoriteProperties().first()
        assertEquals(1, favs.size)

        // Update Status
        val statusUpdated = propertyDao.updatePropertyStatus(id, PropertyEntity.STATUS_RENTED)
        assertEquals(1, statusUpdated)
        val updated = propertyDao.getPropertyByIdOnce(id)
        assertEquals(PropertyEntity.STATUS_RENTED, updated!!.status)
    }

    @Test
    fun deletePropertyById() = runBlocking {
        val property = PropertyEntity(
            title = "Office Space Upper Hill",
            description = "Commercial space.",
            price = 40000000.0,
            propertyType = PropertyEntity.TYPE_COMMERCIAL,
            address = "Upper Hill",
            city = "Nairobi"
        )
        val id = propertyDao.insertProperty(property)
        assertEquals(1, propertyDao.getAllProperties().first().size)

        val rowsDeleted = propertyDao.deletePropertyById(id)
        assertEquals(1, rowsDeleted)
        assertEquals(0, propertyDao.getAllProperties().first().size)
    }
}
