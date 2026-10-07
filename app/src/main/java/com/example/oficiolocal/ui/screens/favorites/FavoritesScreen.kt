package com.example.oficiolocal.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oficiolocal.R
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

    // Refresca la lista cada vez que se abre la pantalla (por si se marcó un favorito en el Detalle).
    LaunchedEffect(Unit) { viewModel.loadFavorites() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.favorites_title),
            style = MaterialTheme.typography.headlineSmall
        )

        if (favorites.isEmpty()) {
            Text(
                text = stringResource(R.string.favorites_empty),
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
                        onFavoriteClick = { viewModel.toggleFavorite(provider.id) },
                        onProviderClick = { onProviderClick(provider.id) }
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = provider.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${provider.category} · ${provider.location}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "★ ${provider.rating}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            FavoriteButton(
                isFavorite = true,
                onClick = onFavoriteClick
            )
        }
    }
}
