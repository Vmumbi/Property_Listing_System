package com.example.property_listing_system.Member4

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.auth.AuthManager
import com.example.property_listing_system.data.auth.UserRole
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private val propertyViewModel: PropertyViewModel by activityViewModels {
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val localDataSource = PropertyLocalDataSourceImpl(database.propertyDao())
        val repository = PropertyRepositoryImpl(localDataSource)
        PropertyViewModelFactory(repository)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthManager.init(requireContext())
        val user = AuthManager.currentUser ?: AuthManager.DEMO_BUYER

        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = view.findViewById<TextView>(R.id.tvUserEmail)
        val tvRoleBadge = view.findViewById<TextView>(R.id.tvRoleBadge)
        val tvRoleDescription = view.findViewById<TextView>(R.id.tvRoleDescription)
        val tvDatabaseStats = view.findViewById<TextView>(R.id.tvDatabaseStats)
        val tvSubStats = view.findViewById<TextView>(R.id.tvSubStats)

        val btnBrowseProperties = view.findViewById<Button>(R.id.btnBrowseProperties)
        val btnAddProperty = view.findViewById<Button>(R.id.btnAddProperty)
        val btnValuationSync = view.findViewById<Button>(R.id.btnValuationSync)
        val btnDatabaseConsole = view.findViewById<Button>(R.id.btnDatabaseConsole)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Display user details & role specifications
        tvUserName.text = user.name
        tvUserEmail.text = user.email
        tvRoleBadge.text = user.role.displayName
        tvRoleDescription.text = user.role.description

        // Configure Dark Mode Switch
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        switchDarkMode.isChecked = isNightMode

        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Live Room Database stats
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                propertyViewModel.allProperties.collect { properties ->
                    val total = properties.size
                    val available = properties.count { it.status.equals("Available", ignoreCase = true) }
                    tvDatabaseStats.text = "$total Listings Cached in SQLite ($available Available)"
                    tvSubStats.text = "Reactive Flow state active • Role: ${user.role.displayName}"
                }
            }
        }

        // 1. Browse Properties (Available to all roles)
        btnBrowseProperties.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment)
        }

        // 2. Add Property (Specification: Agent & Admin only)
        btnAddProperty.setOnClickListener {
            if (AuthManager.canAddProperty()) {
                findNavController().navigate(R.id.action_dashboardFragment_to_addEditPropertyFragment)
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Permission Restricted")
                    .setMessage("You are logged in as a Buyer / Tenant (view-only access).\n\nTo publish or edit listings, please sign in with an Agent account (agent@realtor.co.ke) or Admin.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // 3. AI Valuation & WorkManager Sync (Available to all roles)
        btnValuationSync.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_member6Fragment)
        }

        // 4. Database Console (Specification: Admin only)
        btnDatabaseConsole.setOnClickListener {
            if (AuthManager.canAccessAdminConsole()) {
                findNavController().navigate(R.id.action_dashboardFragment_to_member2Fragment)
            } else {
                AlertDialog.Builder(requireContext())
                    .setTitle("Admin Clearance Required")
                    .setMessage("Raw Room Database Console & SQL management is restricted to System Administrators.\n\nPlease log in as admin@strathmore.edu to access this panel.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        // 5. Logout
        btnLogout.setOnClickListener {
            AuthManager.logout()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }
    }
}
