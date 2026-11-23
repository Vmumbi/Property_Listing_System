package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegisterRedirect = view.findViewById<TextView>(R.id.tvRegisterRedirect)

        btnLogin.setOnClickListener {
            // Navigate to Dashboard and clear backstack so user can't go back to login
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        tvRegisterRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}