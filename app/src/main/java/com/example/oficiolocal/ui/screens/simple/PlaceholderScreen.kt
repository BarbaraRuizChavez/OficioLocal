package com.example.oficiolocal.ui.screens.simple

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.example.oficiolocal.ui.components.ProviderAvatar
import com.example.oficiolocal.ui.components.categoryStyle
import com.example.oficiolocal.ui.components.ReviewsSection
import com.example.oficiolocal.ui.components.WorkGallery
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
    // El directorio siempre ocupa toda la pantalla. Al elegir un prestador se navega a su detalle
    // (en horizontal, AppNavGraph mantiene el menú lateral visible).
    DirectoryList(
        viewModel = viewModel,
        onProviderClick = onProviderClick,
        onNotificationsClick = onNotificationsClick
    )
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
        // Todo (buscador, filtros y prestadores) va dentro de la misma cuadrícula,
        // así que toda la pantalla se desplaza junta.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
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
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(viewModel.categories) { category ->
                        FilterChip(
                            selected = viewModel.selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(localizedTradeName(category)) }
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
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
            }

            if (viewModel.providersList.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.directory_empty))
                    }
                }
            } else {
                gridItems(viewModel.providersList) { provider ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProviderClick(provider.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProviderAvatar(category = provider.category, size = 60.dp, photoUrl = provider.photoUrl)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
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
    var provider by remember(providerId) {
        mutableStateOf(ServiceLocator.providerRepository.getProviderById(providerId))
    }
    LaunchedEffect(providerId) {
        provider = ServiceLocator.providerRepository.refreshOne(providerId) ?: provider
    }
    // Captura el valor delegado para que Kotlin pueda hacer smart cast de forma segura.
    val currentProvider = provider
    val viewer by ServiceLocator.authRepository.currentUser.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentProvider?.name ?: stringResource(R.string.provider_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    currentProvider?.let { selectedProvider ->
                        FavoriteButton(
                            isFavorite = isFavorite,
                            onClick = {
                                scope.launch {
                                    ServiceLocator.favoriteRepository.toggleFavorite(selectedProvider.id)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentProvider == null) {
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
                    val style = categoryStyle(currentProvider.category)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(style.color, style.color.copy(alpha = 0.6f))))
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ProviderAvatar(
                                category = currentProvider.category,
                                photoUrl = currentProvider.photoUrl,
                                size = 72.dp,
                                modifier = Modifier.border(3.dp, Color.White.copy(alpha = 0.7f), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = currentProvider.name, style = MaterialTheme.typography.titleLarge, color = Color.White)
                                Text(
                                    text = "${localizedTradeName(currentProvider.category)} · ${currentProvider.location}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFE08A), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.provider_rating_reviews, currentProvider.rating, currentProvider.reviewCount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                    Text(
                        text = stringResource(R.string.provider_description_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = currentProvider.description, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.provider_schedule_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = currentProvider.schedule, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = stringResource(R.string.provider_price_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = currentProvider.priceRange, style = MaterialTheme.typography.bodyMedium)
                    if (currentProvider.services.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.provider_services_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        currentProvider.services.forEach { service ->
                            Text(text = "• $service", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    if (currentProvider.gallery.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.provider_work_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        WorkGallery(photos = currentProvider.gallery)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentProvider.phone}"))
                            openExternal(context, intent, noApp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentProvider.phone.isNotBlank()
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.provider_call, currentProvider.phone))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val query = Uri.encode("${currentProvider.name}, ${currentProvider.location}, Uriangato, Guanajuato")
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
                            val number = "52" + currentProvider.phone.filter { it.isDigit() }
                            openExternal(
                                context,
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$number")),
                                noApp
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentProvider.phone.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.provider_whatsapp))
                    }
                    // Solo los clientes solicitan servicios; un prestador no se contrata a sí mismo.
                    if (viewer?.isProvider != true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.request_service))
                        }
                    }
                    HorizontalDivider()
                    ReviewsSection(providerId = currentProvider.id)
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