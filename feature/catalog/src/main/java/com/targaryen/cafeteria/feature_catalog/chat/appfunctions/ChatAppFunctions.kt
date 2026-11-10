package com.targaryen.cafeteria.feature_catalog.chat.appfunctions

import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionSerializable
import com.targaryen.cafeteria.feature_catalog.chat.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.first

/**
 * Requisição contendo uma mensagem de conversação informal.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ChatQueryRequest(
    /**
     * Mensagem de diálogo ou pergunta geral (ex: "Quem é você?", "Me conte uma história").
     * Não envie comandos de carrinho ou compras neste campo.
     */
    val query: String,
)

/**
 * Resposta gerada pela assistente de inteligência artificial.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class ChatQueryResponse(
    /** A resposta textual elaborada pela inteligência artificial Rhaenyra. */
    val reply: String,
)

/**
 * Funções exclusivas de bate-papo e conversação informal com a assistente virtual Rhaenyra.
 */
class ChatAppFunctions(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
) {
    /**
     * Realiza uma conversa informal ou bate-papo com a assistente.
     * ATENÇÃO: Esta função serve APENAS para conversação e diálogos gerais.
     * Não a invoque para adicionar produtos, gerenciar o carrinho,
     * listar cardápio ou consultar status de pedidos.
     *
     * @param request A mensagem contendo a pergunta ou diálogo informal.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun askRhaenyra(request: ChatQueryRequest): ChatQueryResponse {
        val result = sendChatMessageUseCase(request.query, emptyList()).first()
        return ChatQueryResponse(reply = result)
    }
}
