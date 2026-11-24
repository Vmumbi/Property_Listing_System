package com.example.property_listing_system.Member4

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.R
import com.example.property_listing_system.data.auth.AuthManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginFragment : Fragment(R.layout.fragment_login) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AuthManager.init(requireContext())

        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPassword = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = view.findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegisterRedirect = view.findViewById<TextView>(R.id.tvRegisterRedirect)

        val btnQuickBuyer = view.findViewById<Button>(R.id.btnQuickBuyer)
        val btnQuickAgent = view.findViewById<Button>(R.id.btnQuickAgent)
        val btnQuickAdmin = view.findViewById<Button>(R.id.btnQuickAdmin)
        val tvQuickAccountHint = view.findViewById<TextView>(R.id.tvQuickAccountHint)

        // Quick Demo Fill Listeners
        btnQuickBuyer.setOnClickListener {
            etEmail.setText(AuthManager.DEMO_BUYER.email)
            etPassword.setText(AuthManager.DEMO_BUYER.password)
            tilEmail.error = null
            tilPassword.error = null
            tvQuickAccountHint.text = "Filled: ${AuthManager.DEMO_BUYER.name} (Buyer / Tenant)"
        }

        btnQuickAgent.setOnClickListener {
            etEmail.setText(AuthManager.DEMO_AGENT.email)
            etPassword.setText(AuthManager.DEMO_AGENT.password)
            tilEmail.error = null
            tilPassword.error = null
            tvQuickAccountHint.text = "Filled: ${AuthManager.DEMO_AGENT.name} (Real Estate Agent)"
        }

        btnQuickAdmin.setOnClickListener {
            etEmail.setText(AuthManager.DEMO_ADMIN.email)
            etPassword.setText(AuthManager.DEMO_ADMIN.password)
            tilEmail.error = null
            tilPassword.error = null
            tvQuickAccountHint.text = "Filled: ${AuthManager.DEMO_ADMIN.name} (Administrator)"
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            tilEmail.error = null
            tilPassword.error = null

            if (email.isEmpty()) {
                tilEmail.error = "Email address is required."
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                tilPassword.error = "Password is required."
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Strictly authenticate against saved accounts
            val loginResult = AuthManager.login(email, password)
            loginResult.onSuccess { account ->
                Toast.makeText(
                    requireContext(),
                    "Welcome, ${account.name} (${account.role.displayName})",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
            }.onFailure { exception ->
                val msg = exception.message ?: "Authentication failed."
                if (msg.contains("password", ignoreCase = true)) {
                    tilPassword.error = msg
                    etPassword.requestFocus()
                } else {
                    tilEmail.error = msg
                    etEmail.requestFocus()
                }
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            }
        }

        tvRegisterRedirect.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
