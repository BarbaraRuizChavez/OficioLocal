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
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
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
import androidx.compose.ui.Alignment
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
import com.example.oficiolocal.ui.screens.notifications.NotificationsScreen
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

    // En pantallas medianas y expandidas el menú permanece disponible en el detalle.
    val onProviderDetail = currentRoute == Routes.PROVIDER_DETAIL
    val isExpanded = widthSizeClass == WindowWidthSizeClass.Expanded
    val showMenu = navItems.any { it.route == currentRoute } ||
        (widthSizeClass != WindowWidthSizeClass.Compact && onProviderDetail) ||
        (isExpanded && currentRoute == Routes.PROVIDER_PROFILE)
    val offline by ServiceLocator.settingsRepository.offline.collectAsState()
    var selectedProviderId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedRequestId by rememberSaveable { mutableStateOf<String?>(null) }

    // Estado del formulario de Solicitudes en pantalla expandida.
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
            if (showMenu && widthSizeClass == WindowWidthSizeClass.Compact) {
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
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

            AdaptiveNavigationContainer(
                widthSizeClass = widthSizeClass,
                showMenu = showMenu,
                currentRoute = currentRoute,
                onNavigate = ::goTo,
                modifier = Modifier.weight(1f)
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.LOGIN,
                    modifier = Modifier.fillMaxSize()
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
                        if (isExpanded) {
                            Row(Modifier.fillMaxSize()) {
                                Box(Modifier.weight(1f).fillMaxHeight()) {
                                    ProviderListScreen(
                                        widthSizeClass = widthSizeClass,
                                        onProviderClick = { selectedProviderId = it },
                                        onRequestClick = { providerId ->
                                            navController.navigate(Routes.buildNewRequestRoute(providerId))
                                        },
                                        onNotificationsClick = {
                                            navController.navigate(Routes.NOTIFICATIONS)
                                        }
                                    )
                                }
                                VerticalDivider()
                                Box(
                                    modifier = Modifier.weight(1.15f).fillMaxHeight(),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    val selectedId = selectedProviderId
                                    if (selectedId == null) {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(stringResource(R.string.directory_select_provider))
                                        }
                                    } else {
                                        ProviderDetailScreen(
                                            providerId = selectedId,
                                            onBackClick = { selectedProviderId = null },
                                            onRequestClick = {
                                                navController.navigate(
                                                    Routes.buildNewRequestRoute(selectedId)
                                                )
                                            },
                                            showBackButton = false
                                        )
                                    }
                                }
                            }
                        } else {
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
                    }

                    composable(Routes.NOTIFICATIONS) {
                        NotificationsScreen(
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(Routes.REQUESTS) {
                        // Si el formulario estaba abierto a la derecha y se gira a vertical,
                        // se muestra como pantalla completa.
                        LaunchedEffect(isExpanded, requestsFormOpen) {
                            if (!isExpanded && requestsFormOpen) {
                                requestsFormOpen = false
                                navController.navigate(Routes.NEW_REQUEST)
                            }
                        }

                        if (isExpanded) {
                            // Lista a la izquierda; detalle o formulario a la derecha.
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(1f)) {
                                    MyRequestsScreen(
                                        selectedRequestId = selectedRequestId,
                                        onNewRequest = {
                                            selectedRequestId = null
                                            requestsFormKey++
                                            requestsFormOpen = true
                                        },
                                        onRequestClick = { requestId ->
                                            selectedRequestId = requestId
                                            requestsFormOpen = false
                                        }
                                    )
                                }
                                VerticalDivider()
                                Box(modifier = Modifier.weight(1.15f)) {
                                    when {
                                        requestsFormOpen -> {
                                            val formViewModel: NewRequestViewModel =
                                                viewModel(key = "new_request_pane_$requestsFormKey")
                                            NewRequestScreen(
                                                onBackClick = { requestsFormOpen = false },
                                                viewModel = formViewModel
                                            )
                                        }
                                        selectedRequestId != null -> {
                                            RequestDetailScreen(
                                                requestId = selectedRequestId!!,
                                                onBackClick = { selectedRequestId = null },
                                                showBackButton = false
                                            )
                                        }
                                        else -> {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(stringResource(R.string.tracking_select_request))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            MyRequestsScreen(
                                onNewRequest = {
                                    if (isExpanded) {
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
                        if (isExpanded) {
                            // Se giró a horizontal con el formulario abierto: regresar a Solicitudes
                            // y mostrarlo en el panel derecho.
                            LaunchedEffect(Unit) {
                                selectedRequestId = null
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
                            widthSizeClass = widthSizeClass,
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
                        if (isExpanded) {
                            // Mantiene Perfil a la izquierda y abre su formulario a la derecha.
                            Row(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.weight(0.9f)) {
                                    ProfileScreen(
                                        widthSizeClass = WindowWidthSizeClass.Medium,
                                        showProviderEditButton = false,
                                        onEditProviderProfile = {},
                                        onLogout = {
                                            navController.navigate(Routes.LOGIN) {
                                                popUpTo(navController.graph.id) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                VerticalDivider()
                                Box(modifier = Modifier.weight(1.1f)) {
                                    ProviderProfileScreen(
                                        onBackClick = { navController.popBackStack() },
                                        widthSizeClass = WindowWidthSizeClass.Medium
                                    )
                                }
                            }
                        } else {
                            ProviderProfileScreen(
                                onBackClick = { navController.popBackStack() },
                                widthSizeClass = widthSizeClass
                            )
                        }
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

@Composable
private fun AdaptiveNavigationContainer(
    widthSizeClass: WindowWidthSizeClass,
    showMenu: Boolean,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when {
        widthSizeClass == WindowWidthSizeClass.Expanded && showMenu -> {
            PermanentNavigationDrawer(
                modifier = modifier,
                drawerContent = {
                    PermanentDrawerSheet {
                        navItems.forEach { item ->
                            NavigationDrawerItem(
                                label = { Text(stringResource(item.label)) },
                                selected = currentRoute == item.route ||
                                    (item.route == Routes.HOME && currentRoute == Routes.PROVIDER_DETAIL) ||
                                    (item.route == Routes.PROFILE && currentRoute == Routes.PROVIDER_PROFILE),
                                onClick = { onNavigate(item.route) },
                                icon = { Icon(item.icon, contentDescription = null) },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                },
                content = content
            )
        }
        widthSizeClass == WindowWidthSizeClass.Medium && showMenu -> {
            Row(modifier = modifier) {
                NavigationRail {
                    navItems.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route ||
                                (item.route == Routes.HOME && currentRoute == Routes.PROVIDER_DETAIL),
                            onClick = { onNavigate(item.route) },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(stringResource(item.label)) }
                        )
                    }
                }
                Box(Modifier.weight(1f).fillMaxHeight()) { content() }
            }
        }
        else -> Box(modifier = modifier) { content() }
    }
}
