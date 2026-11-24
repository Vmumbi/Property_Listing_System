package com.example.property_listing_system.member5

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.auth.AuthManager
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.local.entity.PropertyEntity
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.data.validation.PropertyValidator
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

    private val pricePeriods = listOf(
        PropertyEntity.PERIOD_MONTHLY,
        PropertyEntity.PERIOD_YEARLY,
        PropertyEntity.PERIOD_TOTAL
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthManager.init(requireContext())
        editingPropertyId = arguments?.getLong("propertyId", -1L) ?: -1L

        val btnBackFromAddEdit = view.findViewById<ImageButton>(R.id.btnBackFromAddEdit)
        val tvFormTitle = view.findViewById<TextView>(R.id.tvFormTitle)
        val etTitle = view.findViewById<EditText>(R.id.etTitle)
        val etDescription = view.findViewById<EditText>(R.id.etDescription)
        val etPrice = view.findViewById<EditText>(R.id.etPrice)
        val spinnerPricePeriod = view.findViewById<Spinner>(R.id.spinnerPricePeriod)
        val spinnerType = view.findViewById<Spinner>(R.id.spinnerPropertyType)
        val etImageUrl = view.findViewById<EditText>(R.id.etImageUrl)
        val btnPresetPenthouse = view.findViewById<Button>(R.id.btnPresetPenthouse)
        val btnPresetVilla = view.findViewById<Button>(R.id.btnPresetVilla)
        val btnPresetApartment = view.findViewById<Button>(R.id.btnPresetApartment)

        val etAddress = view.findViewById<EditText>(R.id.etAddress)
        val etCity = view.findViewById<EditText>(R.id.etCity)
        val etBedrooms = view.findViewById<EditText>(R.id.etBedrooms)
        val etBathrooms = view.findViewById<EditText>(R.id.etBathrooms)
        val etSquareFootage = view.findViewById<EditText>(R.id.etSquareFootage)
        val etContactPhone = view.findViewById<EditText>(R.id.etContactPhone)
        val etContactEmail = view.findViewById<EditText>(R.id.etContactEmail)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        // Back button navigation
        btnBackFromAddEdit.setOnClickListener {
            findNavController().popBackStack()
        }

        // Setup Spinners
        spinnerPricePeriod.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            pricePeriods
        )

        spinnerType.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            propertyTypes
        )

        // Preset Sample Image URLs (Agent & Admin can add images with 1 tap)
        btnPresetPenthouse.setOnClickListener {
            etImageUrl.setText("https://images.unsplash.com/photo-1545324418-cc1a3fa10c00?auto=format&fit=crop&w=600&q=80")
            Toast.makeText(requireContext(), "Penthouse image attached", Toast.LENGTH_SHORT).show()
        }
        btnPresetVilla.setOnClickListener {
            etImageUrl.setText("https://images.unsplash.com/photo-1580587771525-78b9dba3b914?auto=format&fit=crop&w=600&q=80")
            Toast.makeText(requireContext(), "Villa image attached", Toast.LENGTH_SHORT).show()
        }
        btnPresetApartment.setOnClickListener {
            etImageUrl.setText("https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=600&q=80")
            Toast.makeText(requireContext(), "Apartment image attached", Toast.LENGTH_SHORT).show()
        }

        // Pre-fill user contact email if empty
        val currentEmail = AuthManager.currentUser?.email.orEmpty()
        if (etContactEmail.text.isBlank() && currentEmail.isNotBlank()) {
            etContactEmail.setText(currentEmail)
        }

        if (editingPropertyId != -1L) {
            tvFormTitle.text = "Edit Property"
            btnSave.text = "Update Property"
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getPropertyById(editingPropertyId).collect { property ->
                    if (property != null) {
                        existingProperty = property
                        etTitle.setText(property.title)
                        etDescription.setText(property.description)
                        etPrice.setText(property.price.toString())
                        spinnerPricePeriod.setSelection(pricePeriods.indexOf(property.pricePeriod).coerceAtLeast(0))
                        spinnerType.setSelection(propertyTypes.indexOf(property.propertyType).coerceAtLeast(0))
                        etImageUrl.setText(property.imageUrl)
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
            val description = etDescription.text.toString().trim()
            val price = etPrice.text.toString().trim().toDoubleOrNull() ?: 0.0
            val pricePeriod = pricePeriods[spinnerPricePeriod.selectedItemPosition.coerceIn(0, pricePeriods.lastIndex)]
            val selectedType = propertyTypes[spinnerType.selectedItemPosition.coerceIn(0, propertyTypes.lastIndex)]
            val imageUrl = etImageUrl.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val city = etCity.text.toString().trim().ifEmpty { "Nairobi" }
            val bedrooms = etBedrooms.text.toString().toIntOrNull() ?: 1
            val bathrooms = etBathrooms.text.toString().toIntOrNull() ?: 1
            val sqft = etSquareFootage.text.toString().toDoubleOrNull() ?: 0.0
            val phone = etContactPhone.text.toString().trim()
            val email = etContactEmail.text.toString().trim()

            val agentEmail = existingProperty?.agentEmail?.ifBlank { currentEmail } ?: currentEmail

            val propertyCandidate = PropertyEntity(
                id = existingProperty?.id ?: 0L,
                title = title,
                description = description,
                price = price,
                currency = existingProperty?.currency ?: "KES",
                propertyType = selectedType,
                address = address,
                city = city,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                squareFootage = sqft,
                imageUrl = imageUrl,
                contactPhone = phone,
                contactEmail = email,
                status = existingProperty?.status ?: PropertyEntity.STATUS_AVAILABLE,
                isFeatured = existingProperty?.isFeatured ?: false,
                isFavorite = existingProperty?.isFavorite ?: false,
                pricePeriod = pricePeriod,
                agentEmail = agentEmail
            )

            // Strict Validation including Kenyan phone digit count
            val validationResult = PropertyValidator.validate(propertyCandidate)
            if (!validationResult.isValid) {
                validationResult.fieldErrors.forEach { (field, err) ->
                    when (field) {
                        "title" -> etTitle.error = err
                        "description" -> etDescription.error = err
                        "price" -> etPrice.error = err
                        "address" -> etAddress.error = err
                        "bedrooms" -> etBedrooms.error = err
                        "bathrooms" -> etBathrooms.error = err
                        "squareFootage" -> etSquareFootage.error = err
                        "contactPhone" -> etContactPhone.error = err
                        "contactEmail" -> etContactEmail.error = err
                    }
                }
                Toast.makeText(
                    requireContext(),
                    validationResult.errorMessage ?: "Please correct form errors",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val result = if (existingProperty != null) {
                    viewModel.updateProperty(propertyCandidate)
                } else {
                    viewModel.addProperty(propertyCandidate)
                }

                result.onSuccess {
                    Toast.makeText(
                        requireContext(),
                        if (existingProperty != null) "Property updated successfully!" else "Property saved to database!",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }.onFailure { error ->
                    Toast.makeText(
                        requireContext(),
                        "Save failed: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
