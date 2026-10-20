package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.first

/**
 * Requisição para adicionar um produto e sua respectiva quantidade ao carrinho.
 *
 * @property productName O nome do café, doce ou produto a ser adicionado (ex: "Targaryen Blood Blend", "Torta de Limão").
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
 * Conjunto de funções para gerenciamento do carrinho de compras local do app Cafeteria Targaryen.
 */
class CartAppFunctions(
    private val catalogRepository: CatalogRepository,
) {
    /**
     * Adiciona uma quantidade específica de um produto ao carrinho de compras imperial.
     *
     * @param request Parâmetros contendo o nome exato do produto e a quantidade desejada.
     */
    @AppFunction
    suspend fun addProductToCart(
        context: AppFunctionContext,
        request: AddProductRequest,
    ): AddProductResponse {
        context.hashCode()
        val products = catalogRepository.getProducts().first()
        val product = products.find { prod -> prod.name.equals(request.productName, ignoreCase = true) }
        return if (product != null) {
            catalogRepository.updateProductQuantity(product.id, request.quantity)
            AddProductResponse(
                success = true,
                message = "Adicionado ${request.quantity} de '${product.name}' ao carrinho imperial.",
            )
        } else {
            AddProductResponse(
                success = false,
                message = "Produto '${request.productName}' não encontrado no cardápio.",
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
        return CartSummaryResponse(
            items = cartItems,
            totalPrice = total,
            message =
                "Carrinho imperial contém ${cartItems.size} tipo(s) de produto(s) " +
                    "com valor total de $total Dragões de Ouro.",
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
}
