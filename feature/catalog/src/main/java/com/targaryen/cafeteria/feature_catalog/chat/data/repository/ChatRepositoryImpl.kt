package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.FunctionCallPart
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ThinkingLevel
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.firebase.ai.type.thinkingConfig
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import org.koin.core.annotation.Single
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private const val CHAT_TEMPERATURE = 0.2f

@Single
class ChatRepositoryImpl : ChatRepository {
    private val addProductToCartTool =
        FunctionDeclaration(
            name = FN_ADD_PRODUCT_TO_CART,
            description = FN_ADD_PRODUCT_TO_CART_DESC,
            parameters =
                mapOf(
                    PARAM_PRODUCT_NAME to Schema.string(PARAM_PRODUCT_NAME_DESC),
                    PARAM_QUANTITY to Schema.integer(PARAM_QUANTITY_DESC),
                ),
            optionalParameters = emptyList(),
        )

    private val saveDeliveryAddressTool =
        FunctionDeclaration(
            name = FN_SAVE_DELIVERY_ADDRESS,
            description = FN_SAVE_DELIVERY_ADDRESS_DESC,
            parameters =
                mapOf(
                    PARAM_CEP to Schema.string(PARAM_CEP_DESC),
                    PARAM_STREET to Schema.string(PARAM_STREET_DESC),
                    PARAM_CITY to Schema.string(PARAM_CITY_DESC),
                    PARAM_STATE to Schema.string(PARAM_STATE_DESC),
                    PARAM_NUMBER to Schema.string(PARAM_NUMBER_DESC),
                    PARAM_REFERENCE_POINT to Schema.string(PARAM_REFERENCE_POINT_DESC),
                    PARAM_RECIPIENT_NAME to Schema.string(PARAM_RECIPIENT_NAME_DESC),
                ),
            optionalParameters = listOf(PARAM_STREET, PARAM_CITY, PARAM_STATE),
        )

    private val navigateToCheckoutTool =
        FunctionDeclaration(
            name = FN_NAVIGATE_TO_CHECKOUT,
            description = FN_NAVIGATE_TO_CHECKOUT_DESC,
            parameters = emptyMap(),
            optionalParameters = emptyList(),
        )

    private val modelCache = ConcurrentHashMap<String, GenerativeModel>()

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
            val primaryModel = getOrCreateGenerativeModel(PRIMARY_MODEL_NAME, systemInstruction)
            try {
                emit(generateContentWithModel(primaryModel, prompt, onFunctionCall))
            } catch (primaryException: FirebaseAIException) {
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
        val fallbackModel = getOrCreateGenerativeModel(FALLBACK_MODEL_NAME, systemInstruction)
        return try {
            generateContentWithModel(fallbackModel, prompt, onFunctionCall)
        } catch (fallbackException: FirebaseAIException) {
            handleModelException(fallbackException)
        } catch (fallbackException: SerializationException) {
            handleModelException(fallbackException)
        } catch (fallbackException: IOException) {
            handleModelException(fallbackException)
        }
    }

    private fun getOrCreateGenerativeModel(
        modelName: String,
        systemInstruction: String,
    ): GenerativeModel {
        val cacheKey = "$modelName:${systemInstruction.hashCode()}"
        return modelCache.computeIfAbsent(cacheKey) {
            Firebase.ai.generativeModel(
                modelName = modelName,
                generationConfig =
                    generationConfig {
                        temperature = CHAT_TEMPERATURE
                        thinkingConfig =
                            thinkingConfig {
                                thinkingLevel = ThinkingLevel.MINIMAL
                            }
                    },
                systemInstruction =
                    content {
                        text(systemInstruction)
                    },
                tools =
                    listOf(
                        Tool.functionDeclarations(
                            listOf(addProductToCartTool, saveDeliveryAddressTool, navigateToCheckoutTool),
                        ),
                    ),
            )
        }
    }

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
            is FirebaseAIException -> throw IllegalStateException(
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
        calls: List<FunctionCallPart>,
        onFunctionCall: suspend (name: String, arguments: Map<String, Any?>) -> String,
    ): String {
        val addedItems = mutableListOf<String>()
        val notFoundItems = mutableListOf<String>()
        val otherReplies = mutableListOf<String>()
        var shouldNavigateToCheckout = false

        for (call in calls) {
            val convertedArgs = call.args.mapValues { entry -> entry.value.toAnyValue() }
            val reply = onFunctionCall(call.name, convertedArgs)
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

    private fun JsonElement.toAnyValue(): Any? =
        when (this) {
            is JsonPrimitive ->
                when {
                    isString -> content
                    booleanOrNull != null -> booleanOrNull
                    intOrNull != null -> intOrNull
                    doubleOrNull != null -> doubleOrNull
                    else -> content
                }
            else -> toString()
        }
}
