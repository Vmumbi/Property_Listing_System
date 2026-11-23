package com.example.property_listing_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.property_listing_system.ui.member2.Member2DatabaseDemoScreen
import com.example.property_listing_system.ui.theme.Property_Listing_SystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Property_Listing_SystemTheme {
                Member2DatabaseDemoScreen()
            }
        }
    }
}
