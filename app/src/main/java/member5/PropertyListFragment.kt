package com.example.property_listing_system.member5

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class PropertyListFragment : Fragment(R.layout.fragment_property_list) {

    private val viewModel: PropertyViewModel by activityViewModels {
        val dao = AppDatabase.getDatabase(requireContext()).propertyDao()
        val localDataSource = PropertyLocalDataSourceImpl(dao)
        val repository = PropertyRepositoryImpl(localDataSource)
        PropertyViewModelFactory(repository)
    }

    private lateinit var adapter: PropertyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewProperties)
        val emptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val fab = view.findViewById<FloatingActionButton>(R.id.fabAddProperty)

        adapter = PropertyAdapter { property ->
            val bundle = Bundle().apply { putLong("propertyId", property.id) }
            findNavController().navigate(R.id.action_to_addEditProperty, bundle)
        }
        recyclerView.adapter = adapter

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_to_addEditProperty)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allProperties.collect { properties ->
                    adapter.submitList(properties)
                    emptyState.visibility = if (properties.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
}