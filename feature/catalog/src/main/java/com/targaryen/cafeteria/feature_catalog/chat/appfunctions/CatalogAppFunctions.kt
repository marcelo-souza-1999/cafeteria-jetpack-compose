package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.catalog.domain.model.Product
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first
import java.text.Normalizer
import java.util.Locale

private const val MAX_SUGGESTIONS_COUNT = 5
private val DIACRITICS_REGEX = Regex("\\p{InCombiningDiacriticalMarks}+")
private val COFFEE_SEARCH_ALIASES = setOf("cafe", "cafes", "coffee", "expresso", "espresso")
private val COFFEE_PRODUCT_KEYWORDS =
    listOf(
        "cafe",
        "expresso",
        "mocha",
        "capuccino",
        "cappuccino",
        "macchiato",
        "latte",
        "affogato",
        "cold brew",
    )

internal fun String.normalizeForSearch(): String {
    val decomposed = Normalizer.normalize(this.trim(), Normalizer.Form.NFD)
    return DIACRITICS_REGEX.replace(decomposed, "").lowercase(Locale.ROOT)
}

internal fun List<Product>.findBestMatchingProduct(rawQuery: String): Product? {
    val normalizedQuery = rawQuery.normalizeForSearch()
    if (normalizedQuery.isBlank()) return null

    return find { prod -> prod.name.normalizeForSearch() == normalizedQuery }
        ?: find { prod -> prod.name.normalizeForSearch().contains(normalizedQuery) }
        ?: find { prod -> normalizedQuery.contains(prod.name.normalizeForSearch()) }
}

internal fun Product.isCoffeeBeverage(): Boolean {
    val normalizedName = name.normalizeForSearch()
    val normalizedDesc = description.normalizeForSearch()
    return COFFEE_PRODUCT_KEYWORDS.any { keyword ->
        normalizedName.contains(keyword) || normalizedDesc.contains(keyword)
    }
}

/**
 * Requisição para pesquisar produtos no cardápio da cafeteria.
 *
 * @property query Termo ou palavra-chave de busca (ex: "café", "expresso", "mocha", "doce").
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
 * @property category Categoria de classificação do produto (ex: "bebidas", "elixires", "comidas").
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
 * @property message Mensagem informativa com os produtos encontrados ou sugestões do cardápio.
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
        val normalizedQuery = request.query.normalizeForSearch()
        val isGenericCoffeeSearch = normalizedQuery in COFFEE_SEARCH_ALIASES

        val matchedProducts =
            allProducts.filter { prod ->
                val normName = prod.name.normalizeForSearch()
                val normDesc = prod.description.normalizeForSearch()
                val normCat = prod.category.normalizeForSearch()
                val directMatch =
                    normName.contains(normalizedQuery) ||
                        normDesc.contains(normalizedQuery) ||
                        normCat.contains(normalizedQuery)
                directMatch || (isGenericCoffeeSearch && prod.isCoffeeBeverage())
            }

        val mappedItems =
            matchedProducts.map { item ->
                CatalogProductItem(
                    id = item.id,
                    name = item.name,
                    description = item.description,
                    price = item.price,
                    category = item.category,
                )
            }

        val formattedMessage =
            if (mappedItems.isNotEmpty()) {
                val itemsListText =
                    mappedItems.joinToString(separator = "\n") { prod ->
                        "• ${prod.name} (${prod.price} Dragões de Ouro)"
                    }
                "Encontrado(s) ${mappedItems.size} produto(s) para '${request.query}':\n$itemsListText"
            } else {
                val suggestions =
                    allProducts
                        .filter { prod -> prod.isCoffeeBeverage() }
                        .ifEmpty { allProducts }
                        .take(MAX_SUGGESTIONS_COUNT)
                val suggestionsText =
                    suggestions.joinToString(separator = "\n") { prod ->
                        "• ${prod.name} (${prod.price} Dragões de Ouro)"
                    }
                "Nenhum item exato para '${request.query}'. Sugestões imperiais disponíveis:\n$suggestionsText"
            }

        return SearchCatalogResponse(
            products = mappedItems,
            message = formattedMessage,
        )
    }
}
