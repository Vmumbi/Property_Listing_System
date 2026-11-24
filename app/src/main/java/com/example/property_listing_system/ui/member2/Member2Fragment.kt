package com.example.property_listing_system.ui.member2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.property_listing_system.ui.theme.Property_Listing_SystemTheme

/**
 * Member 7 Integration: Fragment bridge hosting Member 2's Compose Database Console screen
 */
class Member2Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Property_Listing_SystemTheme {
                    Member2DatabaseDemoScreen(
                        onNavigateBack = { findNavController().popBackStack() }
                    )
                }
            }
        }
    }
}
