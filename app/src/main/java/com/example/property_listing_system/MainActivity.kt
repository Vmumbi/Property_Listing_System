
package com.example.property_listing_system
import com.example.property_listing_system.ui.member3.Member3ViewModelDemoScreen
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.property_listing_system.ui.member2.Member2DatabaseDemoScreen
import com.example.property_listing_system.ui.member6.Member6DemoScreen
import com.example.property_listing_system.ui.theme.Property_Listing_SystemTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.property_listing_system.data.local.database.AppDatabase
import com.example.property_listing_system.data.local.datasource.PropertyLocalDataSourceImpl
import com.example.property_listing_system.data.repository.PropertyRepositoryImpl
import com.example.property_listing_system.viewmodel.PropertyViewModel
import com.example.property_listing_system.viewmodel.PropertyViewModelFactory
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestAppPermissions()

        setContent {
            Property_Listing_SystemTheme {
                var selectedTab by remember {
                    mutableIntStateOf(0)
                }
                val database = remember {
                    AppDatabase.getDatabase(applicationContext)
                }

                val localDataSource = remember {
                    PropertyLocalDataSourceImpl(database.propertyDao())
                }

                val repository = remember {
                    PropertyRepositoryImpl(localDataSource)
                }

                val factory = remember {
                    PropertyViewModelFactory(repository)
                }

                val propertyViewModel: PropertyViewModel = viewModel(
                    factory = factory
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        PrimaryTabRow(
                            selectedTabIndex = selectedTab,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Member 2: Database") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Member 3: State & Logic") }
                            )

                            Tab(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                text = { Text("Member 6: Network") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> Member2DatabaseDemoScreen()
                            1 -> Member3ViewModelDemoScreen(propertyViewModel)
                            2 -> Member6DemoScreen()
                        }
                    }
                }
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissions.toTypedArray())
    }
}
