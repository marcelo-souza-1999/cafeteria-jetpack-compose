package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.first

@AppFunctionSerializable
data class ChatQueryRequest(
    val query: String,
)

@AppFunctionSerializable
data class ChatQueryResponse(
    val reply: String,
)

class ChatAppFunctions(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) {
    @AppFunction
    suspend fun askRhaenyra(
        context: AppFunctionContext,
        request: ChatQueryRequest,
    ): ChatQueryResponse {
        context.hashCode()
        val result = sendChatMessageUseCase(request.query, emptyList()).first()
        return ChatQueryResponse(reply = result)
    }
}
