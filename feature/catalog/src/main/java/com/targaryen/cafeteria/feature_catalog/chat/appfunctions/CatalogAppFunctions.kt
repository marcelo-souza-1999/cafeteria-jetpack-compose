package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first

/**
 * Requisição para pesquisar produtos no cardápio da cafeteria.
 *
 * @property query Termo ou palavra-chave de busca (ex: "latte", "espresso", "bolo", "doce").
 */
@AppFunctionSerializable
data class SearchCatalogRequest(
    val query: String,
)

/**
 * Representação de um produto encontrado no cardápio imperial.
 *
 * @property id Identificador único e interno do produto.
 * @property name Nome oficial do item de cardápio.
 * @property description Descrição contendo ingredientes e características do produto.
 * @property price Preço do item em Dragões de Ouro.
 * @property category Categoria de classificação do produto (ex: "Cafés", "Bebidas Geladas", "Sobremesas").
 */
@AppFunctionSerializable
data class CatalogProductItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val category: String,
)

/**
 * Resposta contendo o resultado da pesquisa de produtos no cardápio.
 *
 * @property products Lista de produtos que correspondem ao termo pesquisado.
 * @property message Mensagem informativa com a quantidade de produtos encontrados.
 */
@AppFunctionSerializable
data class SearchCatalogResponse(
    val products: List<CatalogProductItem>,
    val message: String,
)

/**
 * Conjunto de funções para busca e listagem de produtos do catálogo Cafeteria Targaryen.
 */
class CatalogAppFunctions(
    private val catalogRepository: CatalogRepository,
) {
    /**
     * Pesquisa itens de cardápio por nome, descrição ou categoria correspondentes ao termo de busca.
     *
     * @param request Parâmetros de pesquisa contendo a palavra-chave.
     */
    @AppFunction
    suspend fun searchCatalog(
        context: AppFunctionContext,
        request: SearchCatalogRequest,
    ): SearchCatalogResponse {
        context.hashCode()
        val allProducts = catalogRepository.getProducts().first()
        val filtered =
            allProducts
                .filter { prod ->
                    prod.name.contains(request.query, ignoreCase = true) ||
                        prod.description.contains(request.query, ignoreCase = true) ||
                        prod.category.contains(request.query, ignoreCase = true)
                }.map { item ->
                    CatalogProductItem(
                        id = item.id,
                        name = item.name,
                        description = item.description,
                        price = item.price,
                        category = item.category,
                    )
                }
        return SearchCatalogResponse(
            products = filtered,
            message =
                "Encontrado ${filtered.size} produto(s) correspondente(s) à " +
                    "busca '${request.query}' no cardápio imperial.",
        )
    }
}
