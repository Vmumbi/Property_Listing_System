package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory
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

        val tvDatabaseStats = view.findViewById<TextView>(R.id.tvDatabaseStats)
        val tvSubStats = view.findViewById<TextView>(R.id.tvSubStats)
        val btnBrowseProperties = view.findViewById<Button>(R.id.btnBrowseProperties)
        val btnAddProperty = view.findViewById<Button>(R.id.btnAddProperty)
        val btnValuationSync = view.findViewById<Button>(R.id.btnValuationSync)
        val btnDatabaseConsole = view.findViewById<Button>(R.id.btnDatabaseConsole)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Member 7: Observe Room DB count and live state through PropertyViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                propertyViewModel.allProperties.collect { properties ->
                    val total = properties.size
                    val available = properties.count { it.status.equals("Available", ignoreCase = true) }
                    tvDatabaseStats.text = "Room DB: $total Listings Cached ($available Available)"
                    tvSubStats.text = "StateFlow reactive updates active • Verified by Member 7"
                }
            }
        }

        // Navigation Actions
        btnBrowseProperties.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_propertyListFragment)
        }

        btnAddProperty.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addEditPropertyFragment)
        }

        btnValuationSync.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_member6Fragment)
        }

        btnDatabaseConsole.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_member2Fragment)
        }

        btnLogout.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
        }
    }
}
