package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first

/**
 * Requisição para alternar o status de favorito de um produto.
 *
 * @property productName O nome do café ou iguaria (ex: "Café Expresso Valíria").
 */
@AppFunctionSerializable
data class ToggleFavoriteRequest(
    val productName: String,
)

/**
 * Resposta resultante da alteração de favorito.
 *
 * @property success Indica se a operação foi concluída com sucesso.
 * @property isFavorite Novo estado de favorito do produto.
 * @property message Mensagem descritiva detalhando o resultado da operação.
 */
@AppFunctionSerializable
data class ToggleFavoriteResponse(
    val success: Boolean,
    val isFavorite: Boolean,
    val message: String,
)

/**
 * Representa um item favorito da cafeteria.
 *
 * @property name Nome oficial do produto favorito.
 * @property price Preço do produto em Dragões de Ouro.
 * @property category Categoria do produto.
 */
@AppFunctionSerializable
data class FavoriteItem(
    val name: String,
    val price: Double,
    val category: String,
)

/**
 * Resposta contendo a lista de banquetes e cafés favoritos do usuário.
 *
 * @property favorites Lista de itens atualmente marcados como favoritos.
 * @property message Mensagem resumida com a contagem de favoritos.
 */
@AppFunctionSerializable
data class FavoritesListResponse(
    val favorites: List<FavoriteItem>,
    val message: String,
)

/**
 * Conjunto de funções para gerenciamento das iguarias favoritas da Cafeteria Targaryen.
 */
class FavoritesAppFunctions(
    private val catalogRepository: CatalogRepository,
) {
    /**
     * Alterna o status de favorito de um produto (marca como favorito ou remove dos favoritos).
     *
     * @param request Parâmetros contendo o nome do produto.
     */
    @AppFunction
    suspend fun toggleFavoriteProduct(
        context: AppFunctionContext,
        request: ToggleFavoriteRequest,
    ): ToggleFavoriteResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val product = products.findBestMatchingProduct(request.productName)

        return if (product != null) {
            catalogRepository.toggleFavorite(product.id)
            val newFavoriteState = !product.isFavorite
            val action = if (newFavoriteState) "adicionado aos" else "removido dos"
            ToggleFavoriteResponse(
                success = true,
                isFavorite = newFavoriteState,
                message = "'${product.name}' foi $action seus banquetes favoritos.",
            )
        } else {
            ToggleFavoriteResponse(
                success = false,
                isFavorite = false,
                message = "Erro: Produto '${request.productName}' não encontrado no cardápio.",
            )
        }
    }

    /**
     * Recupera a lista com todos os cafés e elixires marcados como favoritos pelo usuário.
     */
    @AppFunction
    suspend fun getFavoriteProducts(context: AppFunctionContext): FavoritesListResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val favorites =
            products.filter { prod -> prod.isFavorite }.map { item ->
                FavoriteItem(
                    name = item.name,
                    price = item.price,
                    category = item.category,
                )
            }

        val msg =
            if (favorites.isEmpty()) {
                "Nenhum banquete ou café marcado como favorito no momento."
            } else {
                "Você possui ${favorites.size} banquete(s) favorito(s):\n" +
                    favorites.joinToString("\n") { fav -> "• ${fav.name} (${fav.price} Dragões de Ouro)" }
            }

        return FavoritesListResponse(
            favorites = favorites,
            message = msg,
        )
    }
}
