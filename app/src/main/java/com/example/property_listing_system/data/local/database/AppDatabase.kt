package com.example.property_listing_system.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.property_listing_system.data.local.dao.PropertyDao
import com.example.property_listing_system.data.local.entity.PropertyEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * AppDatabase is the main Room Database class for the Property Listing System.
 */
@Database(
    entities = [PropertyEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun propertyDao(): PropertyDao

    companion object {
        const val DATABASE_NAME = "property_listing_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(PropertyDatabaseCallback(coroutineScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class PropertyDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialProperties(database.propertyDao())
                    }
                }
            }
        }

        suspend fun populateInitialProperties(dao: PropertyDao) {
            val initialListings = getSampleInitialProperties()
            dao.insertProperties(initialListings)
        }

        fun getSampleInitialProperties(): List<PropertyEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                PropertyEntity(
                    title = "Luxury 3-Bedroom Penthouse with Panoramic Views",
                    description = "Executive penthouse offering unmatched skyline views, rooftop heated pool, gym, high-speed elevators, and backup generator.",
                    price = 180000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_MONTHLY,
                    propertyType = PropertyEntity.TYPE_APARTMENT,
                    address = "Westlands, Rhapta Road",
                    city = "Nairobi",
                    latitude = -1.2678,
                    longitude = 36.8094,
                    bedrooms = 3,
                    bathrooms = 3,
                    squareFootage = 2200.0,
                    imageUrl = "https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254712345678",
                    contactEmail = "agent@realtor.co.ke",
                    agentEmail = "agent@realtor.co.ke",
                    isFeatured = true,
                    isFavorite = true,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now,
                    remoteId = "REMOTE-001"
                ),
                PropertyEntity(
                    title = "Charming 4-Bedroom Colonial Villa on 0.5 Acre",
                    description = "Magnificent villa set in a serene mature garden, features hardwood floors, grand fireplace, private DSQ for 2, and electric fencing.",
                    price = 78000000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_TOTAL,
                    propertyType = PropertyEntity.TYPE_VILLA,
                    address = "Karen, Mbagathi Ridge",
                    city = "Nairobi",
                    latitude = -1.3197,
                    longitude = 36.7065,
                    bedrooms = 4,
                    bathrooms = 4,
                    squareFootage = 4500.0,
                    imageUrl = "https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254722987654",
                    contactEmail = "agent@realtor.co.ke",
                    agentEmail = "agent@realtor.co.ke",
                    isFeatured = true,
                    isFavorite = false,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now - 3600000,
                    remoteId = "REMOTE-002"
                ),
                PropertyEntity(
                    title = "Executive 2-Bedroom Apartment Near Yaya Centre",
                    description = "Spacious modern apartment with open plan kitchen, ensuite bedrooms, borehole water supply, intercom, and perimeter wall.",
                    price = 95000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_MONTHLY,
                    propertyType = PropertyEntity.TYPE_APARTMENT,
                    address = "Kilimani, Argwings Kodhek",
                    city = "Nairobi",
                    latitude = -1.2921,
                    longitude = 36.7856,
                    bedrooms = 2,
                    bathrooms = 2,
                    squareFootage = 1350.0,
                    imageUrl = "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254733112233",
                    contactEmail = "agent@realtor.co.ke",
                    agentEmail = "agent@realtor.co.ke",
                    isFeatured = false,
                    isFavorite = false,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now - 7200000,
                    remoteId = "REMOTE-003"
                ),
                PropertyEntity(
                    title = "Modern 4-Bedroom Townhouse in Gated Estate",
                    description = "Family-friendly gated community with clubhouse, kids play area, private DSQ, solar water heating, and perimeter electric fence.",
                    price = 36000000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_TOTAL,
                    propertyType = PropertyEntity.TYPE_TOWNHOUSE,
                    address = "Lavington, James Gichuru",
                    city = "Nairobi",
                    latitude = -1.2789,
                    longitude = 36.7689,
                    bedrooms = 4,
                    bathrooms = 3,
                    squareFootage = 3100.0,
                    imageUrl = "https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254701445566",
                    contactEmail = "admin@strathmore.edu",
                    agentEmail = "admin@strathmore.edu",
                    isFeatured = false,
                    isFavorite = false,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now - 10800000,
                    remoteId = "REMOTE-004"
                ),
                PropertyEntity(
                    title = "Prime Commercial Office Suite on 5th Floor",
                    description = "Fitted corporate office space with high-speed fiber internet, dedicated basement parking slots, and round-the-clock security.",
                    price = 1200000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_YEARLY,
                    propertyType = PropertyEntity.TYPE_COMMERCIAL,
                    address = "Upper Hill, Hospital Road",
                    city = "Nairobi",
                    latitude = -1.2989,
                    longitude = 36.8156,
                    bedrooms = 0,
                    bathrooms = 2,
                    squareFootage = 1800.0,
                    imageUrl = "https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254711223344",
                    contactEmail = "admin@strathmore.edu",
                    agentEmail = "admin@strathmore.edu",
                    isFeatured = true,
                    isFavorite = false,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now - 14400000,
                    remoteId = "REMOTE-005"
                ),
                PropertyEntity(
                    title = "0.25 Acre Residential Plot with Clean Title Deed",
                    description = "Ready-to-build red soil plot in a developed secure neighborhood. Water and electricity on site. Ready freehold title.",
                    price = 12500000.0,
                    currency = "KES",
                    pricePeriod = PropertyEntity.PERIOD_TOTAL,
                    propertyType = PropertyEntity.TYPE_LAND,
                    address = "Runda, Mimosa Drive",
                    city = "Nairobi",
                    latitude = -1.2186,
                    longitude = 36.8184,
                    bedrooms = 0,
                    bathrooms = 0,
                    squareFootage = 10890.0,
                    imageUrl = "https://images.unsplash.com/photo-1500382017468-9049fed747ef?auto=format&fit=crop&w=600&q=80",
                    contactPhone = "+254788990011",
                    contactEmail = "agent@realtor.co.ke",
                    agentEmail = "agent@realtor.co.ke",
                    isFeatured = false,
                    isFavorite = false,
                    status = PropertyEntity.STATUS_AVAILABLE,
                    createdTimestamp = now - 18000000,
                    remoteId = "REMOTE-006"
                )
            )
        }
    }
}
