package com.targaryen.cafeteria.feature.auth.data.repository

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.core_network.Resource
import com.targaryen.cafeteria.feature.auth.domain.model.AuthError
import com.targaryen.cafeteria.feature.auth.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class FirebaseAuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val userDao: UserDao
) : AuthRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    override fun isUserLoggedIn(): Boolean = firebaseAuth.currentUser != null

    override fun signUpWithEmail(name: String, email: String, pass: String): Flow<Resource<Unit, AuthError>> =
        callbackFlow {
            firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(name).build()
                        user.updateProfile(profileUpdates).addOnCompleteListener {
                            saveUserToFirestoreAndRoom(
                                UserEntity(user.uid, name, email, null),
                                { trySend(Resource.Success(Unit)) },
                                { e -> trySend(Resource.Error(AuthError.Unknown(e.message))) }
                            )
                        }
                    } else {
                        trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                    }
                }
                .addOnFailureListener { trySend(Resource.Error(mapAuthException(it))) }
            awaitClose { }
        }

    override fun signInWithGoogle(idToken: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    trySend(Resource.Success(Unit))
                    val entity = UserEntity(
                        user.uid, user.displayName ?: "", user.email ?: "", user.photoUrl?.toString()
                    )
                    if (result.additionalUserInfo?.isNewUser == true) {
                        saveUserToFirestoreAndRoom(entity, { }, { })
                    } else {
                        syncUserFromFirestore(entity.uid, entity.email)
                    }
                } else {
                    trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                }
            }
            .addOnFailureListener { trySend(Resource.Error(mapAuthException(it))) }
        awaitClose { }
    }

    override fun signInWithEmail(email: String, pass: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    trySend(Resource.Success(Unit))
                    syncUserFromFirestore(user.uid, email)
                } else {
                    trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                }
            }
            .addOnFailureListener { trySend(Resource.Error(mapAuthException(it))) }
        awaitClose { }
    }

    private fun syncUserFromFirestore(uid: String, email: String) {
        firestore.collection(COLLECTION_USERS).document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString(KEY_NAME) ?: ""
                    val emailFromDb = document.getString(KEY_EMAIL) ?: email
                    val photoUrl = document.getString(KEY_PHOTO_URL)
                    repositoryScope.launch {
                        try {
                            userDao.insertUser(UserEntity(uid, name, emailFromDb, photoUrl))
                        } catch (e: IllegalStateException) {
                            Log.e("AuthRepository", "Failed to insert user locally", e)
                        }
                    }
                } else {
                    val fallback = UserEntity(
                        uid, firebaseAuth.currentUser?.displayName ?: "Aliado",
                        email, firebaseAuth.currentUser?.photoUrl?.toString()
                    )
                    saveUserToFirestoreAndRoom(fallback, { }, { })
                }
            }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener { trySend(Resource.Success(Unit)) }
            .addOnFailureListener { trySend(Resource.Error(mapAuthException(it))) }
        awaitClose { }
    }

    private fun saveUserToFirestoreAndRoom(user: UserEntity, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val userData = hashMapOf<String, Any>(
            KEY_UID to user.uid, KEY_NAME to user.name, KEY_EMAIL to user.email, KEY_CREATED_AT to Timestamp.now()
        )
        user.photoUrl?.let { userData[KEY_PHOTO_URL] = it }
        firestore.collection(COLLECTION_USERS).document(user.uid).set(userData)
            .addOnSuccessListener {
                repositoryScope.launch {
                    try {
                        userDao.insertUser(user)
                        onSuccess()
                    } catch (e: IllegalStateException) {
                        onFailure(e)
                    }
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    private fun mapAuthException(exception: Exception): AuthError {
        return when (exception) {
            is FirebaseAuthUserCollisionException -> AuthError.EmailAlreadyInUse
            is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
            is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
            is FirebaseNetworkException -> AuthError.NetworkError
            is FirebaseAuthException -> {
                if (exception.errorCode == ERROR_TOO_MANY_REQUESTS) AuthError.TooManyRequests
                else AuthError.Unknown(exception.message)
            }
            else -> AuthError.Unknown(exception.message)
        }
    }

    companion object {
        private const val ERROR_TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS"
        private const val COLLECTION_USERS = "users"
        private const val KEY_UID = "uid"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHOTO_URL = "photoUrl"
        private const val KEY_CREATED_AT = "createdAt"
        private const val MSG_USER_RETRIEVAL_FAILED = "Falha ao recuperar usuário"
    }
}
