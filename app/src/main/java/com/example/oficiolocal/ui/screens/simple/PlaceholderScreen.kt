package com.example.oficiolocal.ui.screens.simple

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.ui.components.FavoriteButton
import com.example.oficiolocal.ui.components.ReviewsSection
import com.example.oficiolocal.ui.components.isAvailableToday
import com.example.oficiolocal.ui.components.maxPriceOf
import kotlinx.coroutines.launch

class DirectoryViewModel : ViewModel() {
    private val repository = ServiceLocator.providerRepository

    var searchQuery by mutableStateOf("")
        private set

    var selectedCategory by mutableStateOf<String?>(null)
        private set

    var onlyToday by mutableStateOf(false)
        private set

    var maxPrice by mutableStateOf<Int?>(null)
        private set

    var providersList by mutableStateOf<List<Provider>>(emptyList())
        private set

    val categories = listOf("Plomería", "Electricidad", "Carpintería", "Técnico")

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

    fun onToggleToday() {
        onlyToday = !onlyToday
        fetchProviders()
    }

    fun onMaxPriceSelected(price: Int) {
        maxPrice = if (maxPrice == price) null else price
        fetchProviders()
    }

    private fun fetchProviders() {
        providersList = repository.getProviders(searchQuery, selectedCategory)
            .filter { !onlyToday || isAvailableToday(it.schedule) }
            .filter { provider ->
                maxPrice?.let { max ->
                    maxPriceOf(provider.priceRange)?.let { price -> price <= max } ?: false
                } ?: true
            }
    }
}

@Composable
internal fun localizedTradeName(category: String): String = when (category.trim().lowercase()) {
    "plomería", "plomeria", "plumbing" -> stringResource(R.string.trade_plumbing)
    "electricidad", "electrical", "electrician" -> stringResource(R.string.trade_electricity)
    "carpintería", "carpinteria", "carpentry" -> stringResource(R.string.trade_carpentry)
    "técnico", "tecnico", "soporte técnico", "soporte tecnico", "technician", "tech support" ->
        stringResource(R.string.trade_tech)
    else -> category
}

@Composable
fun ProviderListScreen(
    widthSizeClass: WindowWidthSizeClass,
    onProviderClick: (String) -> Unit,
    onRequestClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: DirectoryViewModel = viewModel()
) {
    val twoPane = widthSizeClass == WindowWidthSizeClass.Expanded
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(if (twoPane) 0.4f else 1f)) {
            DirectoryList(
                viewModel = viewModel,
                onProviderClick = { id -> if (twoPane) selectedId = id else onProviderClick(id) },
                onNotificationsClick = onNotificationsClick
            )
        }
        if (twoPane) {
            Box(modifier = Modifier.weight(0.6f)) {
                val id = selectedId
                if (id == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.directory_select_hint))
                    }
                } else {
                    key(id) {
                        ProviderDetailScreen(
                            providerId = id,
                            onBackClick = { selectedId = null },
                            onRequestClick = { onRequestClick(id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DirectoryList(
    onProviderClick: (String) -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: DirectoryViewModel
) {
    val unreadCount by ServiceLocator.notificationRepository.unreadCount.collectAsState(initial = 0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.directory_title)) },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text(unreadCount.toString()) }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = stringResource(R.string.notif_title)
                            )
                        }
                    }
                }
            )
        }
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
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = stringResource(R.string.directory_search)
                    )
                },
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

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = viewModel.onlyToday,
                        onClick = { viewModel.onToggleToday() },
                        label = { Text(stringResource(R.string.filter_today)) }
                    )
                }
                item {
                    FilterChip(
                        selected = viewModel.maxPrice == 500,
                        onClick = { viewModel.onMaxPriceSelected(500) },
                        label = { Text(stringResource(R.string.filter_price_500)) }
                    )
                }
                item {
                    FilterChip(
                        selected = viewModel.maxPrice == 1000,
                        onClick = { viewModel.onMaxPriceSelected(1000) },
                        label = { Text(stringResource(R.string.filter_price_1000)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.providersList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.directory_empty))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    gridItems(viewModel.providersList) { provider ->
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
                                    text = "${localizedTradeName(provider.category)} · ${provider.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.provider_price_label, provider.priceRange),
                                    style = MaterialTheme.typography.bodySmall
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
    val context = LocalContext.current
    val noApp = stringResource(R.string.provider_no_app)
    val scope = rememberCoroutineScope()
    val isFavorite by ServiceLocator.favoriteRepository
        .observeIsFavorite(providerId)
        .collectAsState(initial = false)
    val provider = remember(providerId) {
        ServiceLocator.providerRepository.getProviderById(providerId)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(provider?.name ?: stringResource(R.string.provider_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (provider != null) {
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onClick = { scope.launch { ServiceLocator.favoriteRepository.toggleFavorite(provider.id) } }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (provider == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.provider_not_found))
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${localizedTradeName(provider.category)} · ${provider.location}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.provider_rating_reviews, provider.rating, provider.reviewCount),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.provider_description_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = provider.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.provider_schedule_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = provider.schedule, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.provider_price_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = provider.priceRange, style = MaterialTheme.typography.bodyMedium)
                    if (provider.services.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.provider_services_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        provider.services.forEach { service ->
                            Text(text = "• $service", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                            openExternal(context, intent, noApp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = provider.phone.isNotBlank()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.provider_call, provider.phone))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val query = Uri.encode("${provider.name}, ${provider.location}, Uriangato, Guanajuato")
                            openExternal(context, Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$query")), noApp)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.provider_map))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val number = "52" + provider.phone.filter { it.isDigit() }
                            openExternal(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")),
                                noApp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = provider.phone.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.provider_whatsapp))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onRequestClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.request_service))
                    }
                    HorizontalDivider()
                    ReviewsSection(providerId = provider.id)
                }
            }
        }
    }
}
private fun openExternal(context: Context, intent: Intent, errorMessage: String) {
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
