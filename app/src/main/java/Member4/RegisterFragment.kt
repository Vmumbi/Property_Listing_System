package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.validation.UserValidator

class RegisterFragment : Fragment(R.layout.fragment_register) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etRegEmail = view.findViewById<EditText>(R.id.etRegEmail)
        val etRegPassword = view.findViewById<EditText>(R.id.etRegPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString()

            val validation = UserValidator.validateLogin(email, password)
            if (!validation.isValid) {
                if (email.isEmpty() || !email.contains("@")) {
                    etRegEmail.error = validation.errorMessage
                    etRegEmail.requestFocus()
                } else {
                    etRegPassword.error = validation.errorMessage
                    etRegPassword.requestFocus()
                }
                return@setOnClickListener
            }

            // Navigate to Dashboard after successful registration
            findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
        }
    }
}
