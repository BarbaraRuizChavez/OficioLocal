package com.example.oficiolocal.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.oficiolocal.R

enum class Destination(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    HOME("home", R.string.nav_home, Icons.Filled.Home),
    REQUESTS("requests", R.string.nav_requests, Icons.AutoMirrored.Filled.Assignment),
    FAVORITES("favorites", R.string.nav_favorites, Icons.Filled.Favorite),
    PROFILE("profile", R.string.nav_profile, Icons.Filled.Person)
}