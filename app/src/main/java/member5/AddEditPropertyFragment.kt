package com.example.property_listing_system.member5

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory
import kotlinx.coroutines.launch

class AddEditPropertyFragment : Fragment(R.layout.fragment_add_edit_property) {

    private val viewModel: PropertyViewModel by activityViewModels {
        val dao = AppDatabase.getDatabase(requireContext()).propertyDao()
        val localDataSource = PropertyLocalDataSourceImpl(dao)
        val repository = PropertyRepositoryImpl(localDataSource)
        PropertyViewModelFactory(repository)
    }

    private var editingPropertyId: Long = -1L
    private var existingProperty: PropertyEntity? = null

    private val propertyTypes = listOf(
        PropertyEntity.TYPE_APARTMENT,
        PropertyEntity.TYPE_VILLA,
        PropertyEntity.TYPE_TOWNHOUSE,
        PropertyEntity.TYPE_MANSIONETTE,
        PropertyEntity.TYPE_COMMERCIAL,
        PropertyEntity.TYPE_LAND
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editingPropertyId = arguments?.getLong("propertyId", -1L) ?: -1L

        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerPropertyType)
        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etCity = view.findViewById<EditText>(R.id.etCity)
        val etBedrooms = view.findViewById<EditText>(R.id.etBedrooms)
        val etBathrooms = view.findViewById<EditText>(R.id.etBathrooms)
        val etSquareFootage = view.findViewById<EditText>(R.id.etSquareFootage)
        val etContactPhone = view.findViewById<EditText>(R.id.etContactPhone)
        val etContactEmail = view.findViewById<EditText>(R.id.etContactEmail)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            propertyTypes
        )

        if (editingPropertyId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getPropertyById(editingPropertyId).collect { property ->
                    if (property != null) {
                        existingProperty = property
                        etTitle.setText(property.title)
                        etDescription.setText(property.description)
                        etPrice.setText(property.price.toString())
                        spinnerType.setSelection(propertyTypes.indexOf(property.propertyType).coerceAtLeast(0))
                        etAddress.setText(property.address)
                        etCity.setText(property.city)
                        etBedrooms.setText(property.bedrooms.toString())
                        etBathrooms.setText(property.bathrooms.toString())
                        etSquareFootage.setText(property.squareFootage.toString())
                        etContactPhone.setText(property.contactPhone)
                        etContactEmail.setText(property.contactEmail)
                    }
                }
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            val priceText = etPrice.text.toString().trim()
            val address = etAddress.text.toString().trim()

            var isValid = true
            if (title.isEmpty()) {
                etTitle.error = "Title is required"; isValid = false
            }
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0) {
                etPrice.error = "Enter a valid price"; isValid = false
            }
            if (address.isEmpty()) {
                etAddress.error = "Address is required"; isValid = false
            }
            val email = etContactEmail.text.toString().trim()
            if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etContactEmail.error = "Enter a valid email"; isValid = false
            }
            if (!isValid) return@setOnClickListener

            val property = PropertyEntity(
                id = existingProperty?.id ?: 0L,
                title = title,
                description = etDescription.text.toString().trim(),
                price = price!!,
                currency = existingProperty?.currency ?: "KES",
                propertyType = propertyTypes[spinnerType.selectedItemPosition],
                address = address,
                city = etCity.text.toString().trim().ifEmpty { "Nairobi" },
                bedrooms = etBedrooms.text.toString().toIntOrNull() ?: 1,
                bathrooms = etBathrooms.text.toString().toIntOrNull() ?: 1,
                squareFootage = etSquareFootage.text.toString().toDoubleOrNull() ?: 0.0,
                contactPhone = etContactPhone.text.toString().trim(),
                contactEmail = email,
                status = existingProperty?.status ?: PropertyEntity.STATUS_AVAILABLE,
                isFeatured = existingProperty?.isFeatured ?: false,
                isFavorite = existingProperty?.isFavorite ?: false
            )

            viewLifecycleOwner.lifecycleScope.launch {
                val result = if (existingProperty != null) {
                    viewModel.updateProperty(property)
                } else {
                    viewModel.addProperty(property)
                }

                result.onSuccess {
                    findNavController().popBackStack()
                }.onFailure { error ->
                    Toast.makeText(requireContext(), "Save failed: ${error.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}