package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first

private const val MAX_CART_SUGGESTIONS = 3

/**
 * Requisição para adicionar um produto e sua respectiva quantidade ao carrinho.
 *
 * @property productName O nome do café, doce ou produto a ser adicionado (ex: "Café Expresso Valíria", "Mocha Fogo de Dragão").
 * @property quantity A quantidade do produto selecionado a ser inserida no carrinho (ex: 1, 2, 5).
 */
@AppFunctionSerializable
data class AddProductRequest(
    val productName: String,
    val quantity: Int,
)

/**
 * Resposta resultante da ação de adicionar produto ao carrinho.
 *
 * @property success Indica se o produto foi adicionado com sucesso ao carrinho.
 * @property message Mensagem textual informativa detalhando o resultado da operação.
 */
@AppFunctionSerializable
data class AddProductResponse(
    val success: Boolean,
    val message: String,
)

/**
 * Representa um item e seus detalhes atualmente contido no carrinho de compras.
 *
 * @property name Nome oficial do produto no carrinho.
 * @property quantity Quantidade do produto selecionada no carrinho.
 * @property price Preço unitário do produto em Dragões de Ouro.
 */
@AppFunctionSerializable
data class CartItem(
    val name: String,
    val quantity: Int,
    val price: Double,
)

/**
 * Resumo atualizado dos produtos e valores do carrinho de compras.
 *
 * @property items Lista contendo cada um dos itens adicionados ao carrinho.
 * @property totalPrice Preço total somado de todos os itens em Dragões de Ouro.
 * @property message Resumo formatado e descritivo do carrinho para exibição.
 */
@AppFunctionSerializable
data class CartSummaryResponse(
    val items: List<CartItem>,
    val totalPrice: Double,
    val message: String,
)

/**
 * Resposta resultante da ação de limpar o carrinho.
 *
 * @property success Indica se a limpeza do carrinho foi executada com sucesso.
 * @property message Mensagem descritiva do resultado da limpeza.
 */
@AppFunctionSerializable
data class ClearCartResponse(
    val success: Boolean,
    val message: String,
)

/**
 * Requisição para remover ou diminuir a quantidade de um produto no carrinho.
 *
 * @property productName O nome do café ou iguaria a ser removido (ex: "Café Expresso Valíria").
 * @property quantity Quantidade a ser removida (se omitido ou maior/igual ao atual, remove o item).
 */
@AppFunctionSerializable
data class RemoveProductRequest(
    val productName: String,
    val quantity: Int = 1,
)

/**
 * Resposta resultante da ação de remover produto do carrinho.
 *
 * @property success Indica se a remoção ou redução foi executada com sucesso.
 * @property message Mensagem descritiva detalhando o resultado da operação.
 */
@AppFunctionSerializable
data class RemoveProductResponse(
    val success: Boolean,
    val message: String,
)

/**
 * Conjunto de funções para gerenciamento do carrinho de compras local do app Cafeteria Targaryen.
 */
class CartAppFunctions(
    private val catalogRepository: CatalogRepository,
) {
    /**
     * Adiciona uma quantidade específica de um produto ao carrinho de compras imperial.
     *
     * @param request Parâmetros contendo o nome do produto e a quantidade desejada.
     */
    @AppFunction
    suspend fun addProductToCart(
        context: AppFunctionContext,
        request: AddProductRequest,
    ): AddProductResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val product = products.findBestMatchingProduct(request.productName)
        return if (product != null) {
            val safeQuantity = request.quantity.coerceAtLeast(1)
            val updatedQuantity = product.quantityInCart + safeQuantity
            catalogRepository.updateProductQuantity(product.id, updatedQuantity)
            AddProductResponse(
                success = true,
                message =
                    "Adicionado $safeQuantity de '${product.name}' ao carrinho imperial " +
                        "(Total: $updatedQuantity).",
            )
        } else {
            val suggestions =
                products
                    .filter { prod -> prod.isCoffeeBeverage() }
                    .ifEmpty { products }
                    .take(MAX_CART_SUGGESTIONS)
                    .joinToString(", ") { prod -> prod.name }
            AddProductResponse(
                success = false,
                message =
                    "Erro: Produto '${request.productName}' não encontrado no cardápio. " +
                        "Experimente pedir: $suggestions.",
            )
        }
    }

    /**
     * Retorna o resumo completo de todos os produtos do carrinho de compras e seu valor total.
     */
    @AppFunction
    suspend fun getCartSummary(context: AppFunctionContext): CartSummaryResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val cartItems =
            products.filter { prod -> prod.quantityInCart > 0 }.map { item ->
                CartItem(
                    name = item.name,
                    quantity = item.quantityInCart,
                    price = item.price,
                )
            }
        val total = cartItems.sumOf { item -> item.price * item.quantity }
        val details =
            if (cartItems.isEmpty()) {
                "Seu carrinho imperial está vazio no momento."
            } else {
                val itemsFormatted =
                    cartItems.joinToString("\n") { item ->
                        "• ${item.quantity}x ${item.name} (${item.price} Dragões de Ouro)"
                    }
                "Carrinho imperial contém ${cartItems.size} tipo(s) de produto(s):\n$itemsFormatted"
            }
        return CartSummaryResponse(
            items = cartItems,
            totalPrice = total,
            message = details,
        )
    }

    /**
     * Limpa completamente todos os itens que estão atualmente no carrinho de compras.
     */
    @AppFunction
    suspend fun clearCart(context: AppFunctionContext): ClearCartResponse {
        context.hashCode()
        catalogRepository.clearCart()
        return ClearCartResponse(
            success = true,
            message = "Carrinho imperial limpo com sucesso.",
        )
    }

    /**
     * Remove ou diminui a quantidade de um produto específico do carrinho de compras.
     *
     * @param request Parâmetros contendo o nome do produto e a quantidade a diminuir.
     */
    @AppFunction
    suspend fun removeProductFromCart(
        context: AppFunctionContext,
        request: RemoveProductRequest,
    ): RemoveProductResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val productsInCart = products.filter { prod -> prod.quantityInCart > 0 }
        if (productsInCart.isEmpty()) {
            return RemoveProductResponse(
                success = false,
                message = "Erro: Seu carrinho imperial está vazio. Nenhum item para remover.",
            )
        }

        val normalizedQuery = request.productName.normalizeForSearch()
        val isGenericCoffeeTarget = normalizedQuery == "cafe" || normalizedQuery == "cafes"
        val productInCart =
            productsInCart.findBestMatchingProduct(request.productName)
                ?: if (isGenericCoffeeTarget) {
                    productsInCart.firstOrNull { prod -> prod.isCoffeeBeverage() } ?: productsInCart.firstOrNull()
                } else {
                    null
                }

        val targetProduct = productInCart ?: products.findBestMatchingProduct(request.productName)

        return when {
            targetProduct == null -> {
                val currentCartNames = productsInCart.joinToString(", ") { prod -> prod.name }
                RemoveProductResponse(
                    success = false,
                    message =
                        "Erro: '${request.productName}' não está no carrinho. " +
                            "Itens atuais no carrinho: $currentCartNames.",
                )
            }
            targetProduct.quantityInCart == 0 -> {
                val currentCartNames = productsInCart.joinToString(", ") { prod -> prod.name }
                RemoveProductResponse(
                    success = false,
                    message =
                        "Erro: '${targetProduct.name}' não está no seu carrinho imperial. " +
                            "Itens atuais no carrinho: $currentCartNames.",
                )
            }
            else -> {
                val newQty = (targetProduct.quantityInCart - request.quantity.coerceAtLeast(1)).coerceAtLeast(0)
                catalogRepository.updateProductQuantity(targetProduct.id, newQty)
                val msg =
                    if (newQty == 0) {
                        "'${targetProduct.name}' removido completamente do carrinho."
                    } else {
                        "Quantidade de '${targetProduct.name}' reduzida para $newQty."
                    }
                RemoveProductResponse(success = true, message = msg)
            }
        }
    }
}
