package com.example.oficiolocal.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.oficiolocal.ui.screens.auth.LoginScreen
import com.example.oficiolocal.ui.screens.simple.ProviderDetailScreen
import com.example.oficiolocal.ui.screens.simple.ProviderListScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas donde debe aparecer la barra inferior
    val bottomBarRoutes = listOf(Routes.HOME, Routes.REQUESTS, Routes.FAVORITES, Routes.PROFILE)
    val shouldShowBottomBar = currentRoute in bottomBarRoutes

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Routes.HOME,
                        onClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Assignment, contentDescription = "Requests") },
                        label = { Text("Requests") },
                        selected = currentRoute == Routes.REQUESTS,
                        onClick = {
                            navController.navigate(Routes.REQUESTS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = currentRoute == Routes.FAVORITES,
                        onClick = {
                            navController.navigate(Routes.FAVORITES) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == Routes.PROFILE,
                        onClick = {
                            navController.navigate(Routes.PROFILE) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LOGIN,
            modifier = Modifier.padding(innerPadding)
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pantalla de Solicitudes (Requests)")
                }
            }

            composable(Routes.FAVORITES) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pantalla de Favoritos (Favorites)")
                }
            }

            composable(Routes.PROFILE) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pantalla de Perfil (Profile)")
                }
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