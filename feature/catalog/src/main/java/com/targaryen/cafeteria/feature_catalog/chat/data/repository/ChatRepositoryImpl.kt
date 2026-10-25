package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.GoogleGenerativeAIException
import com.google.ai.client.generativeai.type.Schema
import com.google.ai.client.generativeai.type.Tool
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.defineFunction
import com.targaryen.cafeteria.feature_catalog.BuildConfig
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class ChatRepositoryImpl : ChatRepository {
    private val addProductToCartTool =
        defineFunction(
            name = FN_ADD_PRODUCT_TO_CART,
            description = FN_ADD_PRODUCT_TO_CART_DESC,
            parameters =
                listOf(
                    Schema.str(PARAM_PRODUCT_NAME, PARAM_PRODUCT_NAME_DESC),
                    Schema.int(PARAM_QUANTITY, PARAM_QUANTITY_DESC),
                ),
            requiredParameters = listOf(PARAM_PRODUCT_NAME, PARAM_QUANTITY),
        )

    private val saveDeliveryAddressTool =
        defineFunction(
            name = FN_SAVE_DELIVERY_ADDRESS,
            description = FN_SAVE_DELIVERY_ADDRESS_DESC,
            parameters =
                listOf(
                    Schema.str(PARAM_CEP, PARAM_CEP_DESC),
                    Schema.str(PARAM_STREET, PARAM_STREET_DESC),
                    Schema.str(PARAM_CITY, PARAM_CITY_DESC),
                    Schema.str(PARAM_STATE, PARAM_STATE_DESC),
                    Schema.str(PARAM_NUMBER, PARAM_NUMBER_DESC),
                    Schema.str(PARAM_REFERENCE_POINT, PARAM_REFERENCE_POINT_DESC),
                    Schema.str(PARAM_RECIPIENT_NAME, PARAM_RECIPIENT_NAME_DESC),
                ),
            requiredParameters = listOf(PARAM_CEP, PARAM_NUMBER, PARAM_REFERENCE_POINT, PARAM_RECIPIENT_NAME),
        )

    private val navigateToCheckoutTool =
        defineFunction(
            name = FN_NAVIGATE_TO_CHECKOUT,
            description = FN_NAVIGATE_TO_CHECKOUT_DESC,
        )

    override fun sendMessage(
        prompt: String,
        systemInstruction: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): Flow<String> =
        flow {
            if (prompt.isBlank()) {
                emit("")
                return@flow
            }
            val primaryModel = createGenerativeModel(PRIMARY_MODEL_NAME, systemInstruction)
            try {
                emit(generateContentWithModel(primaryModel, prompt, onFunctionCall))
            } catch (primaryException: GoogleGenerativeAIException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            } catch (primaryException: SerializationException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            } catch (primaryException: IOException) {
                emit(tryFallbackOrThrow(primaryException, systemInstruction, prompt, onFunctionCall))
            }
        }

    private suspend fun tryFallbackOrThrow(
        primaryException: Exception,
        systemInstruction: String,
        prompt: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): String {
        if (!shouldFallback(primaryException)) {
            handleModelException(primaryException)
        }
        val fallbackModel = createGenerativeModel(FALLBACK_MODEL_NAME, systemInstruction)
        return try {
            generateContentWithModel(fallbackModel, prompt, onFunctionCall)
        } catch (fallbackException: GoogleGenerativeAIException) {
            handleModelException(fallbackException)
        } catch (fallbackException: SerializationException) {
            handleModelException(fallbackException)
        } catch (fallbackException: IOException) {
            handleModelException(fallbackException)
        }
    }

    private fun createGenerativeModel(
        modelName: String,
        systemInstruction: String,
    ): GenerativeModel =
        GenerativeModel(
            modelName = modelName,
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction =
                content {
                    text(systemInstruction)
                },
            tools =
                listOf(
                    Tool(listOf(addProductToCartTool, saveDeliveryAddressTool, navigateToCheckoutTool)),
                ),
        )

    private suspend fun generateContentWithModel(
        model: GenerativeModel,
        prompt: String,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): String {
        val response = model.generateContent(prompt)
        val calls = response.functionCalls
        return if (calls.isNotEmpty()) {
            processFunctionCalls(calls, onFunctionCall)
        } else {
            response.text ?: ""
        }
    }

    private fun shouldFallback(exception: Exception): Boolean {
        val msg = exception.message.orEmpty()
        val causeMsg = exception.cause?.message.orEmpty()
        return msg.contains("503") ||
            msg.contains("high demand", ignoreCase = true) ||
            msg.contains("UNAVAILABLE", ignoreCase = true) ||
            causeMsg.contains("503") ||
            causeMsg.contains("high demand", ignoreCase = true) ||
            exception is SerializationException
    }

    private fun handleModelException(exception: Exception): Nothing =
        when (exception) {
            is GoogleGenerativeAIException -> throw IllegalStateException(
                exception.message ?: ERROR_GENERIC_MODEL,
                exception,
            )
            is SerializationException -> throw IllegalStateException(
                "$ERROR_SERIALIZATION_MODEL: ${exception.message}",
                exception,
            )
            is IOException -> throw IllegalStateException(ERROR_NETWORK_MODEL, exception)
            else -> throw exception
        }

    private suspend fun processFunctionCalls(
        calls: List<com.google.ai.client.generativeai.type.FunctionCallPart>,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): String {
        val addedItems = mutableListOf<String>()
        val notFoundItems = mutableListOf<String>()
        val otherReplies = mutableListOf<String>()
        var shouldNavigateToCheckout = false

        for (call in calls) {
            val reply = onFunctionCall(call.name, call.args)
            when {
                reply == "[NAVIGATE_TO_CHECKOUT]" -> {
                    shouldNavigateToCheckout = true
                }
                reply.startsWith("[ADDED_ITEM]:") -> {
                    addedItems.add(reply.removePrefix("[ADDED_ITEM]:"))
                }
                reply.startsWith("[NOT_FOUND_ITEM]:") -> {
                    notFoundItems.add(reply.removePrefix("[NOT_FOUND_ITEM]:"))
                }
                else -> {
                    otherReplies.add(reply)
                }
            }
        }

        if (shouldNavigateToCheckout) {
            return "[NAVIGATE_TO_CHECKOUT]"
        }

        val messageParts = mutableListOf<String>()
        if (addedItems.isNotEmpty()) {
            val formattedItems = formatListToNaturalLanguage(addedItems)
            messageParts.add(
                "Com prazer, nobre cliente! Adicionei $formattedItems ao seu carrinho imperial.",
            )
        }
        if (notFoundItems.isNotEmpty()) {
            val formattedMissing = formatListToNaturalLanguage(notFoundItems)
            messageParts.add(
                "Peço desculpas, mas não encontrei $formattedMissing em nosso cardápio imperial.",
            )
        }
        messageParts.addAll(otherReplies)

        return messageParts.joinToString(separator = "\n")
    }

    private fun formatListToNaturalLanguage(items: List<String>): String =
        when (items.size) {
            0 -> ""
            1 -> items.first()
            else -> items.dropLast(1).joinToString(", ") + " e " + items.last()
        }
}
