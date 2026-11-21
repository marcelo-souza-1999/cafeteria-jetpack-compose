package com.targaryen.cafeteria.app.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.targaryen.cafeteria.feature.auth.appfunctions.AuthAppFunctions
import com.targaryen.cafeteria.feature.auth.appfunctions.AuthSessionResponse
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.AddProductRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.AddProductResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CartAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CartSummaryResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CatalogAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.ClearCartResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.FavoritesAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.FavoritesListResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.RemoveProductRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.RemoveProductResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.SearchCatalogRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.SearchCatalogResponse
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.ToggleFavoriteRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.ToggleFavoriteResponse
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import com.targaryen.cafeteria.feature_checkout.appfunctions.CheckoutAppFunctions
import com.targaryen.cafeteria.feature_checkout.appfunctions.OrderStatusResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

private const val ANDROID_16_API_LEVEL = 36

/**
 * Ponto de entrada do serviço oficial de AppFunctions da Cafeteria Targaryen no Android 16.
 *
 * Expõe as ações e ferramentas de domínio da Cafeteria para o sistema operacional Android,
 * permitindo que agentes de IA e assistentes executem comandos em segundo plano.
 */
@RequiresApi(ANDROID_16_API_LEVEL)
@AppFunctionServiceEntryPoint(
    serviceName = "CafeteriaAppFunctionService",
    appFunctionXmlFileName = "cafeteria_app_function_service",
)
abstract class BaseCafeteriaAppFunctionService : AppFunctionService() {
    private val catalogRepository: CatalogRepository by inject()
    private val authRepository: AuthRepository by inject()
    private val sendChatMessageUseCase: SendChatMessageUseCase by inject()

    private val cartAppFunctions by lazy { CartAppFunctions(catalogRepository) }
    private val catalogAppFunctions by lazy { CatalogAppFunctions(catalogRepository) }
    private val favoritesAppFunctions by lazy { FavoritesAppFunctions(catalogRepository) }
    private val checkoutAppFunctions by lazy { CheckoutAppFunctions() }
    private val authAppFunctions by lazy { AuthAppFunctions(authRepository) }

    /**
     * Adiciona um produto e sua quantidade ao carrinho de compras imperial.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun addProductToCart(request: AddProductRequest): AddProductResponse =
        withContext(Dispatchers.IO) {
            cartAppFunctions.addProductToCart(request)
        }

    /**
     * Retorna o resumo completo com itens e valores do carrinho de compras.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getCartSummary(): CartSummaryResponse =
        withContext(Dispatchers.IO) {
            cartAppFunctions.getCartSummary()
        }

    /**
     * Limpa completamente todos os itens do carrinho de compras.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun clearCart(): ClearCartResponse =
        withContext(Dispatchers.IO) {
            cartAppFunctions.clearCart()
        }

    /**
     * Remove ou diminui a quantidade de um produto específico do carrinho de compras.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun removeProductFromCart(request: RemoveProductRequest): RemoveProductResponse =
        withContext(Dispatchers.IO) {
            cartAppFunctions.removeProductFromCart(request)
        }

    /**
     * Pesquisa produtos no cardápio da cafeteria por termo ou palavra-chave.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun searchCatalog(request: SearchCatalogRequest): SearchCatalogResponse =
        withContext(Dispatchers.IO) {
            catalogAppFunctions.searchCatalog(request)
        }

    /**
     * Alterna o status de favorito de um produto (adiciona ou remove dos favoritos).
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun toggleFavoriteProduct(request: ToggleFavoriteRequest): ToggleFavoriteResponse =
        withContext(Dispatchers.IO) {
            favoritesAppFunctions.toggleFavoriteProduct(request)
        }

    /**
     * Retorna a lista de banquetes e cafés favoritos do usuário.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getFavoriteProducts(): FavoritesListResponse =
        withContext(Dispatchers.IO) {
            favoritesAppFunctions.getFavoriteProducts()
        }

    /**
     * Recupera o status e informações do último pedido realizado pelo usuário.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getLastOrderStatus(): OrderStatusResponse =
        withContext(Dispatchers.IO) {
            checkoutAppFunctions.getLastOrderStatus()
        }

    /**
     * Verifica o status de autenticação da sessão do usuário na Cafeteria Targaryen.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun checkAuthSession(): AuthSessionResponse =
        withContext(Dispatchers.IO) {
            authAppFunctions.checkAuthSession()
        }

    /**
     * Realiza uma consulta ou diálogo de conversação informal com a assistente Rhaenyra.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun askRhaenyra(query: String): String =
        withContext(Dispatchers.IO) {
            sendChatMessageUseCase(query, emptyList()).first()
        }
}
