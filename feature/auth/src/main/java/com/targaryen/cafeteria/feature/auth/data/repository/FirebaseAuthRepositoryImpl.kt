package com.targaryen.cafeteria.feature.auth.data.repository

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Single

@Single
class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {
    override fun signInWithEmail(email: String, pass: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
            }
            .addOnFailureListener { exception ->
                val authError = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
                    is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
                    is FirebaseNetworkException -> AuthError.NetworkError
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            "ERROR_TOO_MANY_REQUESTS" -> AuthError.TooManyRequests
                            else -> AuthError.Unknown(exception.message)
                        }
                    }
                    else -> AuthError.Unknown(exception.message)
                }
                trySend(Resource.Error(authError))
            }
        awaitClose { }
    }
}
