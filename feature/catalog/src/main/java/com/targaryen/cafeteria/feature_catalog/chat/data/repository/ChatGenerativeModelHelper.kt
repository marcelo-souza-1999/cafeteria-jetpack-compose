package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.ai.type.FunctionCallPart
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import java.io.IOException

internal const val PRIMARY_MODEL_NAME = "gemini-3.5-flash-lite"
internal const val FALLBACK_MODEL_NAME = "gemini-3.6-flash"
internal const val SESSION_TITLE_MAX_LENGTH = 20

internal const val ERROR_GENERIC_MODEL = "Erro no modelo generativo"
internal const val ERROR_SERIALIZATION_MODEL = "Falha no processamento de resposta da IA"
internal const val ERROR_NETWORK_MODEL = "Falha de conexão com os servidores da IA"

internal const val FN_ADD_PRODUCT_TO_CART = "addProductToCart"
internal const val FN_ADD_PRODUCT_TO_CART_DESC = "Adiciona uma quantidade de um café ou bolo ao carrinho do cliente."
internal const val PARAM_PRODUCT_NAME = "productName"
internal const val PARAM_PRODUCT_NAME_DESC = "O nome aproximado ou exato do produto a ser adicionado."
internal const val PARAM_QUANTITY = "quantity"
internal const val PARAM_QUANTITY_DESC = "A quantidade do produto a ser inserida no carrinho."

internal const val FN_NAVIGATE_TO_CHECKOUT = "navigateToCheckout"
internal const val FN_NAVIGATE_TO_CHECKOUT_DESC = "Redireciona para a tela de finalização de compra (checkout)."

internal const val FN_SAVE_DELIVERY_ADDRESS = "saveDeliveryAddress"
internal const val FN_SAVE_DELIVERY_ADDRESS_DESC = "Salva as informações de endereço de entrega do cliente."
internal const val PARAM_CEP = "cep"
internal const val PARAM_CEP_DESC = "O CEP com 8 dígitos."
internal const val PARAM_STREET = "street"
internal const val PARAM_STREET_DESC = "O nome do logradouro ou rua. Opcional se CEP for fornecido."
internal const val PARAM_CITY = "city"
internal const val PARAM_CITY_DESC = "O nome da cidade. Opcional se CEP for fornecido."
internal const val PARAM_STATE = "state"
internal const val PARAM_STATE_DESC = "A sigla do estado (UF). Opcional se CEP for fornecido."
internal const val PARAM_NUMBER = "number"
internal const val PARAM_NUMBER_DESC = "O número da residência."
internal const val PARAM_REFERENCE_POINT = "referencePoint"
internal const val PARAM_REFERENCE_POINT_DESC = "Um ponto de referência próximo."
internal const val PARAM_RECIPIENT_NAME = "recipientName"
internal const val PARAM_RECIPIENT_NAME_DESC = "O nome de quem receberá o pedido."

internal suspend fun generateContentWithModel(
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

internal fun shouldFallback(exception: Exception): Boolean {
    val msg = exception.message.orEmpty()
    val causeMsg = exception.cause?.message.orEmpty()
    return msg.contains("503") ||
        msg.contains("high demand", ignoreCase = true) ||
        msg.contains("UNAVAILABLE", ignoreCase = true) ||
        causeMsg.contains("503") ||
        causeMsg.contains("high demand", ignoreCase = true) ||
        exception is SerializationException
}

internal fun handleModelException(exception: Exception): Nothing =
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

internal suspend fun processFunctionCalls(
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

internal fun formatListToNaturalLanguage(items: List<String>): String =
    when (items.size) {
        0 -> ""
        1 -> items.first()
        else -> items.dropLast(1).joinToString(", ") + " e " + items.last()
    }

internal fun JsonElement.toAnyValue(): Any? =
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
