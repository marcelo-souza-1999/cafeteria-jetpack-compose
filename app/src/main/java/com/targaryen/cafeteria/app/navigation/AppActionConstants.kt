package com.targaryen.cafeteria.app.navigation

object AppActionConstants {
    const val SCHEME = "cafeteria"
    const val GMS_SEARCH_ACTION = "com.google.android.gms.actions.SEARCH_ACTION"

    object Host {
        const val CHECKOUT = "checkout"
        const val ACTION = "action"
    }

    object Action {
        const val ORDER = "order"
        const val CLEAR_CART = "clear_cart"
        const val CART_SUMMARY = "cart_summary"
        const val CART = "cart"
        const val FAVORITES = "favorites"
        const val CHAT = "chat"
        const val STATUS = "status"
        const val SEARCH = "search"
        const val FEATURE = "feature"
    }

    object Param {
        const val PRODUCT_NAME = "productName"
        const val QUANTITY = "quantity"
        const val FEATURE = "feature"
        const val FEATURE_LEGACY = "name"
        const val QUERY = "query"
    }

    object Extra {
        const val ACTION = "action"
        const val PRODUCT_NAME = "productName"
        const val QUANTITY = "quantity"
        const val FEATURE = "feature"
        const val FEATURE_LEGACY = "name"
        const val QUERY = "query"
    }

    object CheckoutStatus {
        const val SUCCESS = "success"
        const val FAILURE = "failure"
        const val PENDING = "pending"
    }

    object FeatureName {
        const val CARRINHO = "carrinho"
        const val CART = "cart"
        const val FAVORITOS = "favoritos"
        const val FAVORITES = "favorites"
        const val CHAT = "chat"
        const val ASSISTENTE = "assistente"
        const val RHAENYRA = "rhaenyra"
        const val PERFIL = "perfil"
        const val PROFILE = "profile"
    }
}
