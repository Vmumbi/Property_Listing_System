package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private lateinit var propertyViewModel: PropertyViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize PropertyViewModel directly using requireActivity()
        val database = AppDatabase.getDatabase(requireContext().applicationContext)
        val localDataSource = PropertyLocalDataSourceImpl(database.propertyDao())
        val repository = PropertyRepositoryImpl(localDataSource)
        val factory = PropertyViewModelFactory(repository)

        propertyViewModel = ViewModelProvider(requireActivity(), factory)[PropertyViewModel::class.java]

        val tvWelcome = view.findViewById<TextView>(R.id.tvWelcomeUser)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        tvWelcome.text = "Dashboard Connected Successfully to PropertyViewModel!"

        btnLogout.setOnClickListener {
            findNavController().navigate(R.id.loginFragment)
        }
    }
}