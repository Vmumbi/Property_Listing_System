package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.auth.AuthManager
import com.example.property_listing_system.data.auth.UserRole
import com.example.property_listing_system.data.validation.UserValidator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthManager.init(requireContext())

        val tilRegName = view.findViewById<TextInputLayout>(R.id.tilRegName)
        val tilRegEmail = view.findViewById<TextInputLayout>(R.id.tilRegEmail)
        val tilRegPassword = view.findViewById<TextInputLayout>(R.id.tilRegPassword)
        val tilRegConfirmPassword = view.findViewById<TextInputLayout>(R.id.tilRegConfirmPassword)

        val etRegName = view.findViewById<TextInputEditText>(R.id.etRegName)
        val etRegEmail = view.findViewById<TextInputEditText>(R.id.etRegEmail)
        val etRegPassword = view.findViewById<TextInputEditText>(R.id.etRegPassword)
        val etRegConfirmPassword = view.findViewById<TextInputEditText>(R.id.etRegConfirmPassword)
        val spinnerRole = view.findViewById<Spinner>(R.id.spinnerRole)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLoginRedirect = view.findViewById<TextView>(R.id.tvLoginRedirect)

        val roles = listOf(UserRole.BUYER, UserRole.AGENT)
        val roleNames = roles.map { it.displayName }

        spinnerRole.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            roleNames
        )

        btnRegister.setOnClickListener {
            val name = etRegName.text.toString().trim()
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString()
            val confirmPass = etRegConfirmPassword.text.toString()

            tilRegName.error = null
            tilRegEmail.error = null
            tilRegPassword.error = null
            tilRegConfirmPassword.error = null

            val validation = UserValidator.validateRegistration(name, email, password, confirmPass)
            if (!validation.isValid) {
                val err = validation.errorMessage ?: "Validation error"
                when {
                    name.length < 2 -> tilRegName.error = err
                    !email.contains("@") -> tilRegEmail.error = err
                    password.length < 6 -> tilRegPassword.error = err
                    password != confirmPass -> tilRegConfirmPassword.error = err
                }
                Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRole = roles[spinnerRole.selectedItemPosition.coerceIn(0, roles.lastIndex)]
            val result = AuthManager.register(name, email, password, selectedRole)

            result.onSuccess { account ->
                Toast.makeText(
                    requireContext(),
                    "Account registered! Welcome, ${account.name} (${account.role.displayName})",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
            }.onFailure { error ->
                tilRegEmail.error = error.message
                Toast.makeText(requireContext(), error.message, Toast.LENGTH_LONG).show()
            }
        }

        tvLoginRedirect.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}
