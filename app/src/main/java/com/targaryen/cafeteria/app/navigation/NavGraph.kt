package com.targaryen.cafeteria.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

@Serializable
data object LoginRoute : NavKey

@Serializable
data object RegisterRoute : NavKey

@Serializable
data object CatalogRoute : NavKey

@Serializable
data object CheckoutRoute : NavKey
