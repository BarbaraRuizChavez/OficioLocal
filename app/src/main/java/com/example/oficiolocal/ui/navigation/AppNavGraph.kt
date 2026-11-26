package com.example.oficiolocal.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.oficiolocal.R
import com.example.oficiolocal.di.ServiceLocator
import com.example.oficiolocal.data.notifications.NotificationsScreen
import com.example.oficiolocal.ui.screens.auth.LoginScreen
import com.example.oficiolocal.ui.screens.favorites.FavoritesScreen
import com.example.oficiolocal.ui.screens.profile.ProfileScreen
import com.example.oficiolocal.ui.screens.providerprofile.ProviderProfileScreen
import com.example.oficiolocal.ui.screens.requests.MyRequestsScreen
import com.example.oficiolocal.ui.screens.simple.ProviderDetailScreen
import com.example.oficiolocal.ui.screens.simple.ProviderListScreen
import com.example.oficiolocal.ui.screens.request.NewRequestScreen
import com.example.oficiolocal.ui.screens.request.NewRequestViewModel
import com.example.oficiolocal.ui.screens.requestdetail.RequestDetailScreen

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
fun AppNavGraph(
    navController: NavHostController,
    widthSizeClass: WindowWidthSizeClass,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showMenu = navItems.any { it.route == currentRoute }
    // Pantallas anchas (tableta): riel lateral. Pantallas compactas: barra inferior.
    val isWide = widthSizeClass != WindowWidthSizeClass.Compact
    val offline by ServiceLocator.settingsRepository.offline.collectAsState()

    // Estado del formulario de Solicitudes en pantalla ancha (vive aquí para sobrevivir a la rotación).
    var requestsFormOpen by rememberSaveable { mutableStateOf(false) }
    // Cambia cada vez que se abre, para que el formulario empiece vacío.
    var requestsFormKey by rememberSaveable { mutableIntStateOf(0) }

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
                            widthSizeClass = widthSizeClass,
                            onProviderClick = { providerId ->
                                navController.navigate(Routes.buildProviderDetailRoute(providerId))
                            },
                            onRequestClick = { providerId ->
                                navController.navigate(Routes.buildNewRequestRoute(providerId))
                            },
                            onNotificationsClick = {
                                navController.navigate(Routes.NOTIFICATIONS)
                            }
                        )
                    }

                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.REQUESTS) {
                        // Si el formulario estaba abierto a la derecha y se gira a vertical,
                        // se muestra como pantalla completa.
                        LaunchedEffect(isWide, requestsFormOpen) {
                            if (!isWide && requestsFormOpen) {
                                requestsFormOpen = false
                                navController.navigate(Routes.NEW_REQUEST)
                            }
                        }

                        if (isWide && requestsFormOpen) {
                            // Riel (izquierda) | Mis solicitudes (centro) | Formulario (derecha)
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MyRequestsScreen(
                                        onNewRequest = { },
                                        onRequestClick = { requestId ->
                                            navController.navigate(Routes.buildRequestDetailRoute(requestId))
                                        }
                                    )
                                }
                                VerticalDivider()
                                Box(modifier = Modifier.weight(1f)) {
                                    val formViewModel: NewRequestViewModel =
                                        viewModel(key = "new_request_pane_$requestsFormKey")
                                    NewRequestScreen(
                                        onBackClick = { requestsFormOpen = false },
                                        viewModel = formViewModel
                                    )
                                }
                            }
                        } else {
                            MyRequestsScreen(
                                onNewRequest = {
                                    if (isWide) {
                                        requestsFormKey++
                                        requestsFormOpen = true
                                    } else {
                                        navController.navigate(Routes.NEW_REQUEST)
                                    }
                                },
                                onRequestClick = { requestId ->
                                    navController.navigate(Routes.buildRequestDetailRoute(requestId))
                                }
                            )
                        }
                    }

                    composable(
                        route = Routes.REQUEST_DETAIL,
                        arguments = listOf(navArgument("requestId") { type = NavType.StringType })
                    ) { entry ->
                        RequestDetailScreen(
                            requestId = entry.arguments?.getString("requestId").orEmpty(),
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.NEW_REQUEST) {
                        if (isWide) {
                            // Se giró a horizontal con el formulario abierto: regresar a Solicitudes
                            // y mostrarlo en el panel derecho.
                            LaunchedEffect(Unit) {
                                requestsFormKey++
                                requestsFormOpen = true
                                navController.popBackStack()
                            }
                        } else {
                            NewRequestScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                    }

                    composable(
                        route = Routes.NEW_REQUEST_PROVIDER,
                        arguments = listOf(
                            navArgument("providerId") {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        NewRequestScreen(
                            onBackClick = { navController.popBackStack() }
                        )
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
                            onEditProviderProfile = {
                                navController.navigate(Routes.PROVIDER_PROFILE)
                            },
                            onLogout = {
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(Routes.PROVIDER_PROFILE) {
                        ProviderProfileScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = Routes.PROVIDER_DETAIL,
                        arguments = listOf(navArgument("providerId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val providerId = backStackEntry.arguments?.getString("providerId") ?: ""
                        ProviderDetailScreen(
                            providerId = providerId,
                            onBackClick = { navController.popBackStack() },
                            onRequestClick = {
                                navController.navigate(
                                    Routes.buildNewRequestRoute(providerId)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}