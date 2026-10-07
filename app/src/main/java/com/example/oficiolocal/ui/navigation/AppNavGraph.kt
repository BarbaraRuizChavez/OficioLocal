package com.example.oficiolocal.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.oficiolocal.R
import com.example.oficiolocal.ui.screens.simple.PlaceholderScreen

@Composable
fun AppNavGraph() {
    val nav = rememberNavController()
    val current = nav.currentBackStackEntryAsState().value?.destination?.route

    // NavigationSuiteScaffold elige solo: barra inferior (compacto),
    // riel (mediano) o panel lateral (expandido).
    NavigationSuiteScaffold(
        navigationSuiteItems = {
            Destination.entries.forEach { d ->
                item(
                    selected = current == d.route,
                    onClick = {
                        nav.navigate(d.route) {
                            popUpTo(Destination.HOME.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(d.icon, contentDescription = stringResource(d.label)) },
                    label = { Text(stringResource(d.label)) }
                )
            }
        }
    ) {
        NavHost(nav, startDestination = Destination.HOME.route) {
            composable(Destination.HOME.route) { PlaceholderScreen(R.string.nav_home) }
            composable(Destination.REQUESTS.route) { PlaceholderScreen(R.string.nav_requests) }
            composable(Destination.FAVORITES.route) { PlaceholderScreen(R.string.nav_favorites) }
            composable(Destination.PROFILE.route) { PlaceholderScreen(R.string.nav_profile) }
            composable("new_request") { PlaceholderScreen(R.string.request_service) }
        }
    }
}