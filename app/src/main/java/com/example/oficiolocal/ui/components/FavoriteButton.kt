package com.example.oficiolocal.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.oficiolocal.R

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = if (isFavorite) {
                Icons.Filled.Favorite
            } else {
                Icons.Filled.FavoriteBorder
            },
            contentDescription = stringResource(
                if (isFavorite) {
                    R.string.favorite_remove
                } else {
                    R.string.favorite_add
                }
            ),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}