package com.example.property_listing_system

import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSource
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepository
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.data.validation.PropertyValidator
import com.example.property_listing_system.data.validation.UserValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Member 7: Integration, Validation & QA Automated Test Suite
 * Validates cross-component data flow, business rules, and error states.
 */
class Member7IntegrationQATest {

    private lateinit var fakeDataSource: FakePropertyLocalDataSource
    private lateinit var repository: PropertyRepository

    @Before
    fun setup() {
        fakeDataSource = FakePropertyLocalDataSource()
        repository = PropertyRepositoryImpl(fakeDataSource)
    }

    private fun buildSampleProperty(
        title: String = "Kilimani Executive Apartments",
        price: Double = 18500000.0,
        phone: String = "+254712345678",
        email: String = "sales@kilimanilistings.co.ke"
    ): PropertyEntity {
        return PropertyEntity(
            id = 1L,
            title = title,
            description = "High-end luxury 3-bedroom apartment with borehole, backup generator, and gym.",
            price = price,
            currency = "KES",
            propertyType = PropertyEntity.TYPE_APARTMENT,
            address = "Argwings Kodhek Road, Kilimani",
            city = "Nairobi",
            bedrooms = 3,
            bathrooms = 2,
            squareFootage = 1600.0,
            contactPhone = phone,
            contactEmail = email,
            status = PropertyEntity.STATUS_AVAILABLE
        )
    }

    // ----------------------------------------------------
    // Test Category 1: Property Input Validation & Error States
    // ----------------------------------------------------

    @Test
    fun validProperty_passesCompleteValidation() {
        val property = buildSampleProperty()
        val result = PropertyValidator.validate(property)
        assertTrue("Valid property should pass validation", result.isValid)
        assertEquals(null, result.errorMessage)
        assertTrue(result.fieldErrors.isEmpty())
    }

    @Test
    fun invalidProperty_titleValidation_triggersFieldError() {
        val shortTitleProp = buildSampleProperty(title = "No")
        val result = PropertyValidator.validate(shortTitleProp)
        assertFalse(result.isValid)
        assertTrue(result.fieldErrors.containsKey("title"))
        assertTrue(result.fieldErrors["title"]!!.contains("at least 3 characters"))
    }

    @Test
    fun invalidProperty_priceValidation_zeroOrNegative_triggersFieldError() {
        val zeroPriceProp = buildSampleProperty(price = 0.0)
        val resultZero = PropertyValidator.validate(zeroPriceProp)
        assertFalse(resultZero.isValid)
        assertTrue(resultZero.fieldErrors.containsKey("price"))

        val negativePriceProp = buildSampleProperty(price = -1000.0)
        val resultNegative = PropertyValidator.validate(negativePriceProp)
        assertFalse(resultNegative.isValid)
        assertTrue(resultNegative.fieldErrors.containsKey("price"))
    }

    @Test
    fun invalidProperty_kenyanPhoneValidation() {
        val invalidPhoneProp = buildSampleProperty(phone = "12345notaphone")
        val resultInvalid = PropertyValidator.validate(invalidPhoneProp)
        assertFalse(resultInvalid.isValid)
        assertTrue(resultInvalid.fieldErrors.containsKey("contactPhone"))

        val validKenyanNumbers = listOf("+254712345678", "0722000000", "0110123456", "+254112345678")
        for (num in validKenyanNumbers) {
            val validPhoneProp = buildSampleProperty(phone = num)
            assertTrue("Phone $num should be recognized as valid", PropertyValidator.validate(validPhoneProp).isValid)
        }
    }

    @Test
    fun invalidProperty_emailValidation() {
        val badEmailProp = buildSampleProperty(email = "invalid-email-format")
        val resultBad = PropertyValidator.validate(badEmailProp)
        assertFalse(resultBad.isValid)
        assertTrue(resultBad.fieldErrors.containsKey("contactEmail"))
    }

    // ----------------------------------------------------
    // Test Category 2: User Authentication Validation (Member 4)
    // ----------------------------------------------------

    @Test
    fun userLoginValidation_rejectsInvalidInputs() {
        val emptyResult = UserValidator.validateLogin("", "")
        assertFalse(emptyResult.isValid)

        val badEmailResult = UserValidator.validateLogin("notanemail", "password123")
        assertFalse(badEmailResult.isValid)

        val shortPassResult = UserValidator.validateLogin("user@domain.com", "123")
        assertFalse(shortPassResult.isValid)

        val validResult = UserValidator.validateLogin("user@strathmore.edu", "securePass123")
        assertTrue(validResult.isValid)
    }

    @Test
    fun userRegistrationValidation_verifiesPasswordMatch() {
        val mismatchResult = UserValidator.validateRegistration(
            name = "Alvis Kamau",
            email = "alvis.kamau@strathmore.edu",
            password = "password123",
            confirmPassword = "differentPassword"
        )
        assertFalse(mismatchResult.isValid)
        assertEquals("Passwords do not match.", mismatchResult.errorMessage)

        val successResult = UserValidator.validateRegistration(
            name = "Alvis Kamau",
            email = "alvis.kamau@strathmore.edu",
            password = "password123",
            confirmPassword = "password123"
        )
        assertTrue(successResult.isValid)
    }

    // ----------------------------------------------------
    // Test Category 3: End-to-End Room Save via Repository (Member 2, 3 & 7)
    // ----------------------------------------------------

    @Test
    fun repository_savesAndRetrievesValidatedProperty() = runBlocking {
        val property = buildSampleProperty()
        val validation = PropertyValidator.validate(property)
        assertTrue(validation.isValid)

        val insertResult = repository.insertProperty(property)
        assertTrue("Insert should succeed", insertResult.isSuccess)

        val all = repository.getAllProperties().first()
        assertEquals(1, all.size)
        assertEquals("Kilimani Executive Apartments", all[0].title)
        assertEquals(18500000.0, all[0].price, 0.01)
    }

    @Test
    fun repository_updateAndToggleFavorite_reflectedInState() = runBlocking {
        val property = buildSampleProperty()
        repository.insertProperty(property)

        val updated = property.copy(price = 19000000.0, isFavorite = true)
        val updateResult = repository.updateProperty(updated)
        assertTrue(updateResult.isSuccess)

        val fetched = repository.getPropertyById(1L).first()
        assertNotNull(fetched)
        assertEquals(19000000.0, fetched!!.price, 0.01)
        assertTrue(fetched.isFavorite)
    }

    // ----------------------------------------------------
    // Test Double (In-Memory Fake Data Source for QA)
    // ----------------------------------------------------

    class FakePropertyLocalDataSource : PropertyLocalDataSource {
        private val list = mutableListOf<PropertyEntity>()
        private val flow = MutableStateFlow<List<PropertyEntity>>(emptyList())

        override fun getAllProperties(): Flow<List<PropertyEntity>> = flow

        override fun getPropertyById(propertyId: Long): Flow<PropertyEntity?> {
            val itemFlow = MutableStateFlow(list.find { it.id == propertyId })
            return itemFlow
        }

        override fun searchProperties(query: String): Flow<List<PropertyEntity>> {
            val filtered = list.filter { it.title.contains(query, true) || it.city.contains(query, true) }
            return MutableStateFlow(filtered)
        }

        override fun filterProperties(
            type: String?,
            minPrice: Double?,
            maxPrice: Double?,
            city: String?,
            bedrooms: Int?,
            status: String?
        ): Flow<List<PropertyEntity>> = flow

        override fun getFavoriteProperties(): Flow<List<PropertyEntity>> {
            return MutableStateFlow(list.filter { it.isFavorite })
        }

        override fun getFeaturedProperties(): Flow<List<PropertyEntity>> {
            return MutableStateFlow(list.filter { it.isFeatured })
        }

        override suspend fun insertProperty(property: PropertyEntity): Long {
            list.removeAll { it.id == property.id }
            list.add(property)
            flow.value = list.toList()
            return property.id
        }

        override suspend fun insertProperties(properties: List<PropertyEntity>): List<Long> {
            properties.forEach { insertProperty(it) }
            return properties.map { it.id }
        }

        override suspend fun updateProperty(property: PropertyEntity): Int {
            val index = list.indexOfFirst { it.id == property.id }
            return if (index != -1) {
                list[index] = property
                flow.value = list.toList()
                1
            } else 0
        }

        override suspend fun deleteProperty(property: PropertyEntity): Int {
            val removed = list.removeAll { it.id == property.id }
            flow.value = list.toList()
            return if (removed) 1 else 0
        }

        override suspend fun deletePropertyById(propertyId: Long): Int {
            val removed = list.removeAll { it.id == propertyId }
            flow.value = list.toList()
            return if (removed) 1 else 0
        }

        override suspend fun setFavoriteStatus(propertyId: Long, isFavorite: Boolean): Int {
            val item = list.find { it.id == propertyId }
            return if (item != null) {
                updateProperty(item.copy(isFavorite = isFavorite))
                1
            } else 0
        }

        override suspend fun getPropertyCount(): Int = list.size

        override suspend fun clearAllProperties(): Int {
            val count = list.size
            list.clear()
            flow.value = emptyList()
            return count
        }
    }
}
