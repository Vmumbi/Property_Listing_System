package com.example.property_listing_system.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class UserRole(val displayName: String, val description: String) {
    BUYER("Buyer / Tenant", "Browse properties, calculate AI valuations, schedule viewings, and save favorites."),
    AGENT("Real Estate Agent", "List, edit, and manage properties, set prices, and run valuation analyses."),
    ADMIN("System Administrator", "Full system access: Room database console, sync management, and user oversight.")
}

data class UserAccount(
    val name: String,
    val email: String,
    val password: String,
    val role: UserRole
)

object AuthManager {

    private const val PREFS_NAME = "property_auth_prefs"
    private const val KEY_CUSTOM_USERS = "custom_users"
    private const val KEY_CURRENT_USER = "current_user"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    // Pre-seeded generic accounts for the 3 user specifications
    val DEMO_BUYER = UserAccount(
        name = "Brian Omondi",
        email = "buyer@gmail.com",
        password = "password123",
        role = UserRole.BUYER
    )

    val DEMO_AGENT = UserAccount(
        name = "Alice Wanjiku",
        email = "agent@realtor.co.ke",
        password = "password123",
        role = UserRole.AGENT
    )

    val DEMO_ADMIN = UserAccount(
        name = "System Administrator",
        email = "admin@strathmore.edu",
        password = "admin123",
        role = UserRole.ADMIN
    )

    private val predefinedAccounts = listOf(DEMO_BUYER, DEMO_AGENT, DEMO_ADMIN)

    var currentUser: UserAccount? = null
        private set

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }

    private fun getCustomUsers(): MutableList<UserAccount> {
        val json = prefs?.getString(KEY_CUSTOM_USERS, null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<UserAccount>>() {}.type
        return runCatching { gson.fromJson<MutableList<UserAccount>>(json, type) }.getOrNull() ?: mutableListOf()
    }

    private fun saveCustomUsers(users: List<UserAccount>) {
        prefs?.edit()?.putString(KEY_CUSTOM_USERS, gson.toJson(users))?.apply()
    }

    fun getAllSavedAccounts(): List<UserAccount> {
        val custom = getCustomUsers()
        return predefinedAccounts + custom.filter { c -> predefinedAccounts.none { it.email.equals(c.email, true) } }
    }

    /**
     * Authenticates exclusively against saved accounts.
     */
    fun login(emailInput: String, passwordInput: String): Result<UserAccount> {
        val email = emailInput.trim().lowercase()
        val password = passwordInput.trim()

        val allAccounts = getAllSavedAccounts()
        val match = allAccounts.find { it.email.lowercase() == email }

        if (match == null) {
            return Result.failure(
                IllegalArgumentException("No account found for $email. Please register or use a demo account.")
            )
        }

        if (match.password != password) {
            return Result.failure(
                IllegalArgumentException("Incorrect password for ${match.email}.")
            )
        }

        currentUser = match
        saveSession(match)
        return Result.success(match)
    }

    /**
     * Registers a new account and saves it permanently to local storage.
     */
    fun register(name: String, emailInput: String, password: String, role: UserRole): Result<UserAccount> {
        val email = emailInput.trim().lowercase()
        val allAccounts = getAllSavedAccounts()

        if (allAccounts.any { it.email.lowercase() == email }) {
            return Result.failure(
                IllegalArgumentException("An account with email $email already exists.")
            )
        }

        val newAccount = UserAccount(
            name = name.trim().ifEmpty { "User" },
            email = email,
            password = password.trim(),
            role = role
        )

        val custom = getCustomUsers()
        custom.add(newAccount)
        saveCustomUsers(custom)

        currentUser = newAccount
        saveSession(newAccount)
        return Result.success(newAccount)
    }

    fun logout() {
        currentUser = null
        prefs?.edit()?.remove(KEY_CURRENT_USER)?.apply()
    }

    private fun saveSession(account: UserAccount) {
        prefs?.edit()?.putString(KEY_CURRENT_USER, gson.toJson(account))?.apply()
    }

    private fun loadSession() {
        val json = prefs?.getString(KEY_CURRENT_USER, null) ?: return
        currentUser = runCatching { gson.fromJson(json, UserAccount::class.java) }.getOrNull()
    }

    fun canAddProperty(): Boolean {
        val role = currentUser?.role ?: return false
        return role == UserRole.AGENT || role == UserRole.ADMIN
    }

    fun canAccessAdminConsole(): Boolean {
        return currentUser?.role == UserRole.ADMIN
    }
}
