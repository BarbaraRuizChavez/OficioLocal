package com.example.oficiolocal.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val REQUESTS = "requests"
    const val NEW_REQUEST = "new_request"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val PROVIDER_DETAIL = "provider_detail/{providerId}"

    const val NEW_REQUEST_PROVIDER = "new_request/{providerId}"

    fun buildProviderDetailRoute(providerId: String) = "provider_detail/$providerId"
    fun buildNewRequestRoute(providerId: String) =
        "new_request/$providerId"
}
