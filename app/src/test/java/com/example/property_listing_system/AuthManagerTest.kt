package com.example.property_listing_system

import com.example.property_listing_system.data.auth.AuthManager
import com.example.property_listing_system.data.auth.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthManagerTest {

    @Before
    fun setup() {
        AuthManager.logout()
    }

    @Test
    fun demoAccounts_arePreConfigured() {
        val accounts = AuthManager.getAllSavedAccounts()
        assertTrue(accounts.size >= 3)

        val buyer = accounts.find { it.email == "buyer@gmail.com" }
        assertNotNull(buyer)
        assertEquals(UserRole.BUYER, buyer?.role)

        val agent = accounts.find { it.email == "agent@realtor.co.ke" }
        assertNotNull(agent)
        assertEquals(UserRole.AGENT, agent?.role)

        val admin = accounts.find { it.email == "admin@strathmore.edu" }
        assertNotNull(admin)
        assertEquals(UserRole.ADMIN, admin?.role)
    }

    @Test
    fun login_withPreSavedAccounts_succeeds() {
        val buyerResult = AuthManager.login("buyer@gmail.com", "password123")
        assertTrue(buyerResult.isSuccess)
        assertEquals("Brian Omondi", buyerResult.getOrNull()?.name)
        assertEquals(UserRole.BUYER, buyerResult.getOrNull()?.role)

        val agentResult = AuthManager.login("agent@realtor.co.ke", "password123")
        assertTrue(agentResult.isSuccess)
        assertEquals(UserRole.AGENT, agentResult.getOrNull()?.role)

        val adminResult = AuthManager.login("admin@strathmore.edu", "admin123")
        assertTrue(adminResult.isSuccess)
        assertEquals(UserRole.ADMIN, adminResult.getOrNull()?.role)
    }

    @Test
    fun login_withUnsavedAccount_fails() {
        val unknownResult = AuthManager.login("random@unknown.com", "password123")
        assertFalse(unknownResult.isSuccess)
        assertTrue(unknownResult.exceptionOrNull()?.message?.contains("No account found") == true)
    }

    @Test
    fun login_withIncorrectPassword_fails() {
        val wrongPassResult = AuthManager.login("buyer@gmail.com", "wrongPassword")
        assertFalse(wrongPassResult.isSuccess)
        assertTrue(wrongPassResult.exceptionOrNull()?.message?.contains("Incorrect password") == true)
    }

    @Test
    fun rolePermissions_enforceMultiUserSpecifications() {
        // 1. Buyer: can browse/evaluate, cannot add property or access admin console
        AuthManager.login("buyer@gmail.com", "password123")
        assertFalse(AuthManager.canAddProperty())
        assertFalse(AuthManager.canAccessAdminConsole())

        // 2. Agent: can add property, cannot access admin console
        AuthManager.login("agent@realtor.co.ke", "password123")
        assertTrue(AuthManager.canAddProperty())
        assertFalse(AuthManager.canAccessAdminConsole())

        // 3. Admin: can add property and access admin console
        AuthManager.login("admin@strathmore.edu", "admin123")
        assertTrue(AuthManager.canAddProperty())
        assertTrue(AuthManager.canAccessAdminConsole())
    }
}
