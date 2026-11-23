package com.example.property_listing_system.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.property_listing_system.data.repository.PropertyRepository

class PropertyViewModelFactory(
    private val repository: PropertyRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PropertyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PropertyViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}