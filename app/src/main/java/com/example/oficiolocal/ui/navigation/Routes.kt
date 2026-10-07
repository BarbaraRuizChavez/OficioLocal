package com.example.oficiolocal.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val REQUESTS = "requests"
    const val NEW_REQUEST = "new_request"
    const val FAVORITES = "favorites"
    const val PROFILE = "profile"
    const val PROVIDER_DETAIL = "provider_detail/{providerId}"

    fun buildProviderDetailRoute(providerId: String) = "provider_detail/$providerId"
<<<<<<< HEAD
    const val NEW_REQUEST = "new_request?providerId={providerId}"

    fun buildNewRequestRoute(providerId: String? = null): String =
        if (providerId != null) "new_request?providerId=$providerId" else "new_request"
}
=======
}
>>>>>>> 6d60df0f5c4f05546e29261b3db698c48ec30c97
