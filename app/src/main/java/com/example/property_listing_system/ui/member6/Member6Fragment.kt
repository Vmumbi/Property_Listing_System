package com.example.property_listing_system.ui.member6

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.ui.theme.Property_Listing_SystemTheme

/**
 * Member 7 Integration: Fragment bridge hosting Member 6's Compose Valuation screen
 */
class Member6Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Property_Listing_SystemTheme {
                    Member6DemoScreen(
                        onNavigateBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}
