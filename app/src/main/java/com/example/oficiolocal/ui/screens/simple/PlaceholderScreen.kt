package com.example.oficiolocal.ui.screens.simple

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.Provider

class DirectoryViewModel : ViewModel() {
    private val repository = ServiceLocator.providerRepository

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<String?>(null)
        private set

    var providersList by mutableStateOf<List<Provider>>(emptyList())
        private set

    val categories = listOf("Plomería", "Electricidad", "Carpintería")

    init {
        fetchProviders()
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        fetchProviders()
    }

    fun onCategorySelected(category: String) {
        selectedCategory = if (selectedCategory == category) null else category
        fetchProviders()
    }

    private fun fetchProviders() {
        providersList = repository.getProviders(searchQuery, selectedCategory)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(
    onProviderClick: (String) -> Unit,
    viewModel: DirectoryViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Directorio de Prestadores") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por nombre o ubicación...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categories) { category ->
                    FilterChip(
                        selected = viewModel.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.providersList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron prestadores.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(viewModel.providersList) { provider ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProviderClick(provider.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = provider.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "${provider.rating} (${provider.reviewCount})")
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${provider.category} • ${provider.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Precio aprox: ${provider.priceRange}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(
    providerId: String,
    onBackClick: () -> Unit
) {
    val provider = remember(providerId) { ServiceLocator.providerRepository.getProviderById(providerId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(provider?.name ?: "Detalle del Prestador") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (provider == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Prestador no encontrado")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(text = provider.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text(text = "${provider.category} • ${provider.location}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${provider.rating} (${provider.reviewCount} reseñas)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(text = "Descripción", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Horario de Atención", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.schedule, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Precio Aproximado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.priceRange, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Servicios Ofrecidos", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    provider.services.forEach { service ->
                        Text(text = "• $service", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = { }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Llamar (${provider.phone})")
                }
            }
        }
    }
}