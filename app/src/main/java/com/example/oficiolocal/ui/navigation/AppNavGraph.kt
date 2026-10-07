package com.example.oficiolocal.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.ui.screens.auth.LoginScreen
import com.example.oficiolocal.ui.screens.favorites.FavoritesScreen
import com.example.oficiolocal.ui.screens.profile.ProfileScreen
import com.example.oficiolocal.ui.screens.requests.MyRequestsScreen
import com.example.oficiolocal.ui.screens.requests.NewRequestScreen
import com.example.oficiolocal.ui.screens.simple.ProviderDetailScreen
import com.example.oficiolocal.ui.screens.simple.ProviderListScreen

private data class NavItem(
    @StringRes val label: Int,
    val route: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem(R.string.nav_home, Routes.HOME, Icons.Filled.Home),
    NavItem(R.string.nav_requests, Routes.REQUESTS, Icons.AutoMirrored.Filled.Assignment),
    NavItem(R.string.nav_favorites, Routes.FAVORITES, Icons.Filled.Favorite),
    NavItem(R.string.nav_profile, Routes.PROFILE, Icons.Filled.Person)
)

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showMenu = navItems.any { it.route == currentRoute }
    // Pantallas anchas (tableta): riel lateral. Pantallas compactas: barra inferior.
    val isWide = LocalConfiguration.current.screenWidthDp >= 600
    val offline by ServiceLocator.settingsRepository.offline.collectAsState()

    fun goTo(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (showMenu && !isWide) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = { goTo(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (showMenu && isWide) {
                NavigationRail {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route,
                            onClick = { goTo(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                if (offline) {
                    Text(
                        text = stringResource(R.string.offline_banner),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(8.dp)
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.HOME) {
                        ProviderListScreen(
                            onProviderClick = { providerId ->
                                navController.navigate(Routes.buildProviderDetailRoute(providerId))
                            }
                        )
                    }

                    composable(Routes.REQUESTS) {
                        MyRequestsScreen(
                            onNewRequest = { navController.navigate(Routes.NEW_REQUEST) }
                        )
                    }

                    composable(Routes.NEW_REQUEST) {
                        NewRequestScreen(onDone = { navController.popBackStack() })
                    }

                    composable(Routes.FAVORITES) {
                        FavoritesScreen(
                            favoriteRepository = ServiceLocator.favoriteRepository,
                            providerRepository = ServiceLocator.providerRepository,
                            onProviderClick = { providerId ->
                                navController.navigate(Routes.buildProviderDetailRoute(providerId))
                            }
                        )
                    }

                    composable(Routes.PROFILE) {
                        ProfileScreen(
                            onLogout = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = Routes.PROVIDER_DETAIL,
                        arguments = listOf(navArgument("providerId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                        ProviderDetailScreen(
                            providerId = providerId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
