package com.example.oficiolocal.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.data.repository.FavoriteRepository
import com.example.oficiolocal.data.repository.ProviderRepository
import com.example.oficiolocal.domain.Provider
import com.example.oficiolocal.ui.components.FavoriteButton

@Composable
fun FavoritesScreen(
    favoriteRepository: FavoriteRepository,
    providerRepository: ProviderRepository,
    onProviderClick: (String) -> Unit = {}
) {
    val factory = FavoritesViewModelFactory(
        favoriteRepository = favoriteRepository,
        providerRepository = providerRepository
    )

    val viewModel: FavoritesViewModel = viewModel(factory = factory)
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis favoritos",
            style = MaterialTheme.typography.headlineSmall
        )

        if (favorites.isEmpty()) {
            Text(
                text = "Aún no tienes prestadores favoritos.",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { provider ->
                    FavoriteProviderCard(
                        provider = provider,
                        onFavoriteClick = {
                            viewModel.toggleFavorite(provider.id)
                        },
                        onProviderClick = {
                            onProviderClick(provider.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteProviderCard(
    provider: Provider,
    onFavoriteClick: () -> Unit,
    onProviderClick: () -> Unit
) {
    Card(
        onClick = onProviderClick,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = provider.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = provider.category,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = provider.location,
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = " ${provider.rating}",
                style = MaterialTheme.typography.bodyMedium
            )

            FavoriteButton(
                isFavorite = true,
                onClick = onFavoriteClick
            )
        }
    }
}