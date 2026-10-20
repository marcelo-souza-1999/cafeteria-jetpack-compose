package com.targaryen.cafeteria.feature_catalog.chat.domain.usecase

import com.targaryen.cafeteria.coredatabase.address.AddressProvider
import com.targaryen.cafeteria.coredatabase.address.DeliveryAddress
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatPromptConstants
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Factory

private const val MAX_CATALOG_PROMPT_LIMIT = 15

@Factory
class SendChatMessageUseCase(
    private val catalogRepository: CatalogRepository,
    private val chatRepository: ChatRepository,
    private val addressProvider: AddressProvider,
) {
    operator fun invoke(
        prompt: String,
        history: List<ChatMessage>,
    ): Flow<String> =
        flow {
            val products = catalogRepository.getProducts().first().take(MAX_CATALOG_PROMPT_LIMIT)
            val catalogString =
                products.joinToString(separator = "\n") { product ->
                    "- ${product.name}: ${product.price} ${ChatPromptConstants.PRICE_SUFFIX}"
                }

            val systemInstruction =
                """
                ${ChatPromptConstants.SYSTEM_INSTRUCTION.trimIndent()}
                
                ${ChatPromptConstants.REAL_CATALOG_CONTEXT_HEADER}
                $catalogString
                """.trimIndent()

            val formattedPrompt =
                buildString {
                    history.takeLast(ChatPromptConstants.MAX_HISTORY_LIMIT).forEach { message ->
                        val speaker =
                            if (message.sender == ChatSender.USER) {
                                ChatPromptConstants.SPEAKER_USER
                            } else {
                                ChatPromptConstants.SPEAKER_AI
                            }
                        append("$speaker: ${message.text}\n")
                    }
                    append("${ChatPromptConstants.SPEAKER_USER}: $prompt\n")
                    append("${ChatPromptConstants.SPEAKER_AI}: ")
                }

            chatRepository
                .sendMessage(
                    prompt = formattedPrompt,
                    systemInstruction = systemInstruction,
                    onFunctionCall = { name, arguments ->
                        handleFunctionCall(name, arguments)
                    },
                ).collect { reply ->
                    emit(reply)
                }
        }

    private suspend fun handleFunctionCall(
        name: String,
        arguments: Map<String, Any?>,
    ): String =
        when (name) {
            "addProductToCart" -> handleAddProductToCart(arguments)
            "saveDeliveryAddress" -> handleSaveDeliveryAddress(arguments)
            "navigateToCheckout" -> "[NAVIGATE_TO_CHECKOUT]"
            else -> "Operação desconhecida."
        }

    private suspend fun handleAddProductToCart(arguments: Map<String, Any?>): String {
        val productName = arguments["productName"] as? String ?: ""
        val quantity =
            (arguments["quantity"] as? Double)?.toInt()
                ?: (arguments["quantity"] as? Int)
                ?: (arguments["quantity"] as? String)?.toIntOrNull()
                ?: 1
        val products = catalogRepository.getProducts().first()
        val product =
            products.find { prod ->
                prod.name.equals(productName, ignoreCase = true) ||
                    prod.name.contains(productName, ignoreCase = true)
            }
        return if (product != null) {
            val newTotalQuantity = product.quantityInCart + quantity
            catalogRepository.updateProductQuantity(product.id, newTotalQuantity)
            "[ADDED_ITEM]:${quantity}x ${product.name}"
        } else {
            "[NOT_FOUND_ITEM]:$productName"
        }
    }

    private fun handleSaveDeliveryAddress(arguments: Map<String, Any?>): String {
        val cep = extractFirstMatching(arguments, listOf("cep", "zipCode", "postalCode")).filter { it.isDigit() }
        val street = extractFirstMatching(arguments, listOf("street", "logradouro", "rua"))
        val city = extractFirstMatching(arguments, listOf("city", "cidade"))
        val state = extractFirstMatching(arguments, listOf("state", "uf", "estado"))
        val rawNumber = extractFirstMatching(arguments, listOf("number", "numero", "houseNumber"))
        val number = (rawNumber.toDoubleOrNull()?.toInt()?.toString() ?: rawNumber).trim()
        val referencePoint =
            extractFirstMatching(arguments, listOf("referencePoint", "complement", "complemento", "reference"))
        val recipientName = extractFirstMatching(arguments, listOf("recipientName", "name", "nome", "recipient"))

        addressProvider.saveAddress(
            DeliveryAddress(
                cep = cep,
                street = street,
                city = city,
                state = state,
                number = number,
                referencePoint = referencePoint,
                recipientName = recipientName,
            ),
        )
        return "[NAVIGATE_TO_CHECKOUT]"
    }

    private fun extractFirstMatching(
        arguments: Map<String, Any?>,
        keys: List<String>,
    ): String {
        for (key in keys) {
            val value = arguments[key]?.toString()
            if (!value.isNullOrBlank()) {
                return value
            }
        }
        return ""
    }
}
