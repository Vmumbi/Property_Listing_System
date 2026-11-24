package com.example.property_listing_system.Member4

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.background.scheduler.PropertyWorkManagerScheduler
import com.example.property_listing_system.data.auth.AuthManager
import com.example.property_listing_system.data.auth.UserRole
import com.google.android.material.switchmaterial.SwitchMaterial

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthManager.init(requireContext())
        val user = AuthManager.currentUser ?: AuthManager.DEMO_BUYER

        val tvPortalTitle = view.findViewById<TextView>(R.id.tvPortalTitle)
        val tvUserWelcome = view.findViewById<TextView>(R.id.tvUserWelcome)
        val switchDarkMode = view.findViewById<SwitchMaterial>(R.id.switchDarkMode)

        val layoutBuyerActions = view.findViewById<LinearLayout>(R.id.layoutBuyerActions)
        val layoutAgentActions = view.findViewById<LinearLayout>(R.id.layoutAgentActions)
        val layoutAdminActions = view.findViewById<LinearLayout>(R.id.layoutAdminActions)

        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Dark Mode Switch
        val isNightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        switchDarkMode.isChecked = isNightMode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            AppCompatDelegate.setDefaultNightMode(mode)
        }

        // Setup Role-specific view
        when (user.role) {
            UserRole.BUYER -> {
                tvPortalTitle.text = "Buyer Portal"
                tvUserWelcome.text = "Signed in as: ${user.name} (Buyer)"
                layoutBuyerActions.visibility = View.VISIBLE
                layoutAgentActions.visibility = View.GONE
                layoutAdminActions.visibility = View.GONE
                setupBuyerActions(view)
            }
            UserRole.AGENT -> {
                tvPortalTitle.text = "Agent Portal"
                tvUserWelcome.text = "Signed in as: ${user.name} (Agent)"
                layoutBuyerActions.visibility = View.GONE
                layoutAgentActions.visibility = View.VISIBLE
                layoutAdminActions.visibility = View.GONE
                setupAgentActions(view)
            }
            UserRole.ADMIN -> {
                tvPortalTitle.text = "Administrator Portal"
                tvUserWelcome.text = "Signed in as: ${user.name} (Admin)"
                layoutBuyerActions.visibility = View.GONE
                layoutAgentActions.visibility = View.GONE
                layoutAdminActions.visibility = View.VISIBLE
                setupAdminActions(view)
            }
        }

        btnLogout.setOnClickListener {
            AuthManager.logout()
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }
    }

    private fun setupBuyerActions(view: View) {
        val btnBuyerBrowse = view.findViewById<Button>(R.id.btnBuyerBrowse)
        val btnBuyerValuation = view.findViewById<Button>(R.id.btnBuyerValuation)

        btnBuyerBrowse.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment)
        }

        btnBuyerValuation.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_member6Fragment)
        }
    }

    private fun setupAgentActions(view: View) {
        val btnAgentBrowseAll = view.findViewById<Button>(R.id.btnAgentBrowseAll)
        val btnAgentMyListings = view.findViewById<Button>(R.id.btnAgentMyListings)
        val btnAgentAddProperty = view.findViewById<Button>(R.id.btnAgentAddProperty)
        val btnAgentUploadOffline = view.findViewById<Button>(R.id.btnAgentUploadOffline)

        btnAgentBrowseAll.setOnClickListener {
            val bundle = Bundle().apply { putBoolean("filterMyListings", false) }
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment, bundle)
        }

        btnAgentMyListings.setOnClickListener {
            val bundle = Bundle().apply { putBoolean("filterMyListings", true) }
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment, bundle)
        }

        btnAgentAddProperty.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addEditPropertyFragment)
        }

        btnAgentUploadOffline.setOnClickListener {
            PropertyWorkManagerScheduler(requireContext()).scheduleImmediateSync()
            Toast.makeText(requireContext(), "Offline properties uploaded to cloud successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupAdminActions(view: View) {
        val btnAdminBrowseAll = view.findViewById<Button>(R.id.btnAdminBrowseAll)
        val btnAdminAddProperty = view.findViewById<Button>(R.id.btnAdminAddProperty)
        val btnAdminDatabaseConsole = view.findViewById<Button>(R.id.btnAdminDatabaseConsole)
        val btnAdminSyncCloud = view.findViewById<Button>(R.id.btnAdminSyncCloud)

        btnAdminBrowseAll.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment)
        }

        btnAdminAddProperty.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addEditPropertyFragment)
        }

        btnAdminDatabaseConsole.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_member2Fragment)
        }

        btnAdminSyncCloud.setOnClickListener {
            PropertyWorkManagerScheduler(requireContext()).scheduleImmediateSync()
            Toast.makeText(requireContext(), "Cloud synchronization initiated successfully!", Toast.LENGTH_SHORT).show()
        }
    }
}
