package com.example.property_listing_system.ui.member2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.property_listing_system.data.local.entity.PropertyEntity
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Member2DatabaseDemoScreen(
    viewModel: Member2DatabaseViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val properties by viewModel.properties.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val availableCount by viewModel.availableCount.collectAsState()
    val favorites by viewModel.favoriteProperties.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Room Database Engine", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Member 2: Persistence & CRUD", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.seedSampleData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Seed Sample Data")
                    }
                    IconButton(onClick = { viewModel.clearAllData() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear All")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Property")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Database Statistics Banner
            DatabaseStatsBanner(
                total = totalCount,
                available = availableCount,
                favorites = favorites.size
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Search Field
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by title, location, type...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Property Type Filter Chips
            val filterTypes = listOf("All", "Apartment", "Villa", "Townhouse", "Commercial")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filterTypes) { type ->
                    FilterChip(
                        selected = uiState.selectedTypeFilter == type,
                        onClick = { viewModel.onTypeFilterSelected(type) },
                        label = { Text(type) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Properties List
            if (properties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No properties found in SQLite database.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.seedSampleData() }) {
                            Text("Populate Sample Properties")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(properties, key = { it.id }) { property ->
                        PropertyCard(
                            property = property,
                            onToggleFavorite = { viewModel.toggleFavorite(property) },
                            onStatusChange = { newStatus -> viewModel.updateStatus(property.id, newStatus) },
                            onDelete = { viewModel.deleteProperty(property) }
                        )
                    }
                }
            }
        }
    }

    // Add Property Dialog
    if (uiState.isAddDialogOpen) {
        AddPropertyDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { title, desc, price, type, address, city, beds, baths, phone ->
                viewModel.addProperty(title, desc, price, type, address, city, beds, baths, phone)
            }
        )
    }
}

@Composable
fun DatabaseStatsBanner(total: Int, available: Int, favorites: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(label = "Total SQLite", value = total.toString())
            StatItem(label = "Available", value = available.toString())
            StatItem(label = "Favorites", value = favorites.toString())
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
    }
}

@Composable
fun PropertyCard(
    property: PropertyEntity,
    onToggleFavorite: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember { NumberFormat.getNumberInstance(Locale.US) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = property.propertyType,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = when (property.status) {
                            PropertyEntity.STATUS_AVAILABLE -> Color(0xFF2E7D32)
                            PropertyEntity.STATUS_RENTED -> Color(0xFFE65100)
                            else -> Color(0xFFC62828)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = property.status,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            if (property.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (property.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = property.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Text(
                text = "${property.currency} ${formatter.format(property.price)}",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${property.address}, ${property.city}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${property.bedrooms} Beds • ${property.bathrooms} Baths • ${property.squareFootage.toInt()} sq ft",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${property.id} | Remote: ${property.remoteId ?: "Local"}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.weight(1f)
                )

                // Quick Status Switch Buttons
                if (property.status == PropertyEntity.STATUS_AVAILABLE) {
                    TextButton(onClick = { onStatusChange(PropertyEntity.STATUS_RENTED) }) {
                        Text("Mark Rented", fontSize = 11.sp)
                    }
                    TextButton(onClick = { onStatusChange(PropertyEntity.STATUS_SOLD) }) {
                        Text("Mark Sold", fontSize = 11.sp)
                    }
                } else {
                    TextButton(onClick = { onStatusChange(PropertyEntity.STATUS_AVAILABLE) }) {
                        Text("Make Available", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddPropertyDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        description: String,
        price: Double,
        type: String,
        address: String,
        city: String,
        bedrooms: Int,
        bathrooms: Int,
        phone: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Nairobi") }
    var selectedType by remember { mutableStateOf("Apartment") }
    var bedroomsText by remember { mutableStateOf("2") }
    var bathroomsText by remember { mutableStateOf("2") }
    var phone by remember { mutableStateOf("+254712345678") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add New Listing to SQLite",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price (KES) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address *") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = bedroomsText,
                        onValueChange = { bedroomsText = it },
                        label = { Text("Beds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bathroomsText,
                        onValueChange = { bathroomsText = it },
                        label = { Text("Baths") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            val beds = bedroomsText.toIntOrNull() ?: 1
                            val baths = bathroomsText.toIntOrNull() ?: 1
                            onConfirm(title, description, price, selectedType, address, city, beds, baths, phone)
                        }
                    ) {
                        Text("Save to Room")
                    }
                }
            }
        }
    }
}
