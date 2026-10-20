package com.targaryen.cafeteria.feature_checkout.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * Informações e status do último pedido realizado pelo usuário imperial.
 *
 * @property status O status atual do pedido no sistema (ex: "Pendente", "Preparando", "Finalizado", "Cancelado").
 * @property summary O resumo textual legível dos produtos inclusos no pedido.
 * @property totalPrice O valor total cobrado pelo pedido em Dragões de Ouro.
 * @property dateMillis O timestamp em milissegundos da realização do pedido.
 * @property message Mensagem legível resumida com as informações do pedido.
 */
@AppFunctionSerializable
data class OrderStatusResponse(
    val status: String,
    val summary: String,
    val totalPrice: Double,
    val dateMillis: Long,
    val message: String,
)

/**
 * Conjunto de funções para consulta de finalização de compras e pedidos da Cafeteria Targaryen.
 */
class CheckoutAppFunctions(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    /**
     * Recupera o status e os detalhes do último pedido realizado pelo usuário imperial autenticado.
     */
    @AppFunction
    suspend fun getLastOrderStatus(context: AppFunctionContext): OrderStatusResponse {
        context.hashCode()
        val userId =
            firebaseAuth.currentUser?.uid ?: return OrderStatusResponse(
                status = "Não Autenticado",
                summary = "",
                totalPrice = 0.0,
                dateMillis = 0L,
                message = "Nenhum usuário imperial autenticado no momento. Faça login no aplicativo.",
            )

        return try {
            val querySnapshot =
                firestore
                    .collection("orders")
                    .whereEqualTo("userId", userId)
                    .orderBy("dateMillis", Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .await()

            if (!querySnapshot.isEmpty) {
                val document = querySnapshot.documents[0]
                val status = document.getString("status") ?: "Pendente"
                val summary = document.getString("itemsSummary") ?: ""
                val totalPrice = document.getDouble("totalPrice") ?: 0.0
                val dateMillis = document.getLong("dateMillis") ?: 0L

                OrderStatusResponse(
                    status = status,
                    summary = summary,
                    totalPrice = totalPrice,
                    dateMillis = dateMillis,
                    message =
                        "Último pedido encontrado: $summary com status " +
                            "'$status' no valor de $totalPrice Dragões de Ouro.",
                )
            } else {
                OrderStatusResponse(
                    status = "Nenhum Pedido",
                    summary = "",
                    totalPrice = 0.0,
                    dateMillis = 0L,
                    message = "Você ainda não realizou nenhum pedido na Cafeteria Targaryen.",
                )
            }
        } catch (e: com.google.firebase.FirebaseException) {
            OrderStatusResponse(
                status = "Erro",
                summary = "",
                totalPrice = 0.0,
                dateMillis = 0L,
                message = "Erro ao buscar último pedido: ${e.localizedMessage}",
            )
        }
    }
}
