package com.targaryen.cafeteria.feature.auth.appfunctions

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository

/**
 * Informações de sessão e status de autenticação do usuário.
 *
 * @property isLoggedIn Indica se o usuário está atualmente autenticado e ativo no sistema.
 * @property userUid O identificador único de usuário (UID) do Firebase Auth, ou nulo se não logado.
 * @property message Mensagem descritiva detalhando o status atual da autenticação.
 */
@AppFunctionSerializable
data class AuthSessionResponse(
    val isLoggedIn: Boolean,
    val userUid: String?,
    val message: String,
)

/**
 * Conjunto de funções de autenticação e controle de sessão imperial da Cafeteria.
 */
class AuthAppFunctions(
    private val authRepository: AuthRepository,
) {
    /**
     * Verifica o status de autenticação da sessão do usuário imperial.
     * Retorna se o usuário está logado e, em caso positivo, o UID correspondente.
     */
    @AppFunction
    fun checkAuthSession(context: AppFunctionContext): AuthSessionResponse {
        context.hashCode()
        val isLoggedIn = authRepository.isUserLoggedIn()
        val uid = authRepository.getCurrentUserUid()
        val msg =
            if (isLoggedIn) {
                "Usuário imperial está autenticado com UID: $uid."
            } else {
                "Nenhum usuário imperial autenticado no momento."
            }
        return AuthSessionResponse(
            isLoggedIn = isLoggedIn,
            userUid = uid,
            message = msg,
        )
    }
}
