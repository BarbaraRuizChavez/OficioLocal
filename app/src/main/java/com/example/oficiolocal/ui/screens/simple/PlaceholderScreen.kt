package com.example.oficiolocal.ui.screens.simple

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.R
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.ui.components.FavoriteButton
import com.example.oficiolocal.ui.components.ReviewsSection
import kotlinx.coroutines.launch

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

@Composable
private fun localizedTradeName(category: String): String = when (category.trim().lowercase()) {
    "plomería", "plomeria", "plumbing" -> stringResource(R.string.trade_plumbing)
    "electricidad", "electrical", "electrician" -> stringResource(R.string.trade_electricity)
    "carpintería", "carpinteria", "carpentry" -> stringResource(R.string.trade_carpentry)
    "técnico", "tecnico", "soporte técnico", "soporte tecnico", "technician", "tech support" ->
        stringResource(R.string.trade_tech)
    else -> category
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreen(
    onProviderClick: (String) -> Unit,
    viewModel: DirectoryViewModel = viewModel()
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.directory_title)) }) }
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
                placeholder = { Text(stringResource(R.string.directory_search_hint)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = stringResource(R.string.directory_search)) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(viewModel.categories) { category ->
                    FilterChip(
                        selected = viewModel.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        label = { Text(localizedTradeName(category)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.providersList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.directory_empty))
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
                                    text = "${localizedTradeName(provider.category)} • ${provider.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.provider_price_label, provider.priceRange),
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
    onBackClick: () -> Unit,
    onRequestClick: () -> Unit
) {
    val provider = remember(providerId) { ServiceLocator.providerRepository.getProviderById(providerId) }
    val favoriteFlow = remember(providerId) {
        ServiceLocator.favoriteRepository.observeIsFavorite(providerId)
    }
    val isFavorite by favoriteFlow.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(provider?.name ?: stringResource(R.string.provider_detail_title))
                },
                actions = {
                    if (provider != null) {
                        FavoriteButton(isFavorite = isFavorite) {
                            coroutineScope.launch {
                                ServiceLocator.favoriteRepository.toggleFavorite(providerId)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (provider == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.provider_not_found))
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
                Text(text = "${localizedTradeName(provider.category)} • ${provider.location}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(
                            R.string.provider_rating_reviews,
                            provider.rating.toString(),
                            provider.reviewCount.toString()
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text(text = stringResource(R.string.provider_description_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.provider_schedule_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.schedule, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.provider_price_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = provider.priceRange, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.provider_services_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    provider.services.forEach { service ->
                        Text(text = "• $service", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 2.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                ReviewsSection(providerId = provider.id)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val phoneUri = Uri.fromParts("tel", provider.phone, null)
                        context.startActivity(Intent(Intent.ACTION_DIAL, phoneUri))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = provider.phone.isNotBlank()
                ) {
                    Icon(Icons.Default.Call, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.provider_call, provider.phone))
                }

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRequestClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.request_service))
                }
            }
        }
    }
}
