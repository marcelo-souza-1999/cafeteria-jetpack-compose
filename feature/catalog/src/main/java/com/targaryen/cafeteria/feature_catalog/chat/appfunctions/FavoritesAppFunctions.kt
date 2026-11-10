package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionSerializable
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first

/**
 * Requisição para alternar o status de favorito de um produto.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ToggleFavoriteRequest(
    /** O nome do café ou iguaria (ex: "Café Expresso Valíria"). */
    val productName: String,
)

/**
 * Resposta resultante da alteração de favorito.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ToggleFavoriteResponse(
    /** Indica se a operação foi concluída com sucesso. */
    val success: Boolean,
    /** Novo estado de favorito do produto. */
    val isFavorite: Boolean,
    /** Mensagem descritiva detalhando o resultado da operação. */
    val message: String,
)

/**
 * Representa um item favorito da cafeteria.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class FavoriteItem(
    /** Nome oficial do produto favorito. */
    val name: String,
    /** Preço do produto em Dragões de Ouro. */
    val price: Double,
    /** Categoria do produto. */
    val category: String,
)

/**
 * Resposta contendo a lista de banquetes e cafés favoritos do usuário.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class FavoritesListResponse(
    /** Lista de itens atualmente marcados como favoritos. */
    val favorites: List<FavoriteItem>,
    /** Mensagem resumida com a contagem de favoritos. */
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
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun toggleFavoriteProduct(request: ToggleFavoriteRequest): ToggleFavoriteResponse {
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
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun getFavoriteProducts(): FavoritesListResponse {
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
