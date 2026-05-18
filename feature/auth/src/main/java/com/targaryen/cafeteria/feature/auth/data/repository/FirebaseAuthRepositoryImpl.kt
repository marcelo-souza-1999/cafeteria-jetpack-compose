package com.targaryen.cafeteria.feature.auth.data.repository

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
import com.targaryen.cafeteria.core_database.dao.UserDao
import com.targaryen.cafeteria.core_database.model.UserEntity
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

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    override fun signUpWithEmail(name: String, email: String, pass: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()

                    user.updateProfile(profileUpdates)
                        .addOnCompleteListener {
                            saveUserToFirestoreAndRoom(
                                uid = user.uid,
                                name = name,
                                email = email,
                                photoUrl = null,
                                onSuccess = { trySend(Resource.Success(Unit)) },
                                onFailure = { e -> trySend(Resource.Error(AuthError.Unknown(e.message))) }
                            )
                        }
                } else {
                    trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                }
            }
            .addOnFailureListener { exception ->
                val authError = when (exception) {
                    is FirebaseAuthUserCollisionException -> AuthError.EmailAlreadyInUse
                    is FirebaseNetworkException -> AuthError.NetworkError
                    else -> AuthError.Unknown(exception.message)
                }
                trySend(Resource.Error(authError))
            }
        awaitClose { }
    }

    override fun signInWithGoogle(idToken: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                val isNewUser = result.additionalUserInfo?.isNewUser ?: false

                if (user != null) {
                    trySend(Resource.Success(Unit))

                    if (isNewUser) {
                        saveUserToFirestoreAndRoom(
                            uid = user.uid,
                            name = user.displayName ?: "",
                            email = user.email ?: "",
                            photoUrl = user.photoUrl?.toString(),
                            onSuccess = { },
                            onFailure = { }
                        )
                    } else {
                        // Veterano: Sincroniza Firestore -> Room (Oportunista)
                        firestore.collection(COLLECTION_USERS).document(user.uid).get()
                            .addOnSuccessListener { document ->
                                if (document.exists()) {
                                    val name = document.getString(KEY_NAME) ?: ""
                                    val emailFromDb = document.getString(KEY_EMAIL) ?: user.email ?: ""
                                    val photoUrl = document.getString(KEY_PHOTO_URL)
                                    repositoryScope.launch {
                                        try {
                                            userDao.insertUser(
                                                UserEntity(
                                                    uid = user.uid,
                                                    name = name,
                                                    email = emailFromDb,
                                                    photoUrl = photoUrl
                                                )
                                            )
                                        } catch (e: Exception) {
                                            // Silencioso
                                        }
                                    }
                                }
                            }
                    }
                } else {
                    trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                }
            }
            .addOnFailureListener { exception ->
                val authError = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
                    is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
                    is FirebaseNetworkException -> AuthError.NetworkError
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            ERROR_TOO_MANY_REQUESTS -> AuthError.TooManyRequests
                            else -> AuthError.Unknown(exception.message)
                        }
                    }
                    else -> AuthError.Unknown(exception.message)
                }
                trySend(Resource.Error(authError))
            }
        awaitClose { }
    }

    override fun signInWithEmail(email: String, pass: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    trySend(Resource.Success(Unit))
                    // Sincroniza Firestore -> Room ao logar (Oportunista)
                    firestore.collection(COLLECTION_USERS).document(user.uid).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val name = document.getString(KEY_NAME) ?: ""
                                val photoUrl = document.getString(KEY_PHOTO_URL)
                                repositoryScope.launch {
                                    try {
                                        userDao.insertUser(
                                            UserEntity(
                                                uid = user.uid,
                                                name = name,
                                                email = email,
                                                photoUrl = photoUrl
                                            )
                                        )
                                    } catch (e: Exception) {
                                        // Silencioso, falha local não deve derrubar o app
                                    }
                                }
                            } else {
                                // Se não houver documento no Firestore (caso raro), criamos um básico
                                saveUserToFirestoreAndRoom(
                                    uid = user.uid,
                                    name = user.displayName ?: "Aliado",
                                    email = email,
                                    photoUrl = user.photoUrl?.toString(),
                                    onSuccess = { },
                                    onFailure = { }
                                )
                            }
                        }
                } else {
                    trySend(Resource.Error(AuthError.Unknown(MSG_USER_RETRIEVAL_FAILED)))
                }
            }
            .addOnFailureListener { exception ->
                val authError = when (exception) {
                    is FirebaseAuthInvalidCredentialsException -> AuthError.InvalidCredentials
                    is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
                    is FirebaseNetworkException -> AuthError.NetworkError
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            ERROR_TOO_MANY_REQUESTS -> AuthError.TooManyRequests
                            else -> AuthError.Unknown(exception.message)
                        }
                    }
                    else -> AuthError.Unknown(exception.message)
                }
                trySend(Resource.Error(authError))
            }
        awaitClose { }
    }

    override fun sendPasswordResetEmail(email: String): Flow<Resource<Unit, AuthError>> = callbackFlow {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                trySend(Resource.Success(Unit))
            }
            .addOnFailureListener { exception ->
                val authError = when (exception) {
                    is FirebaseAuthInvalidUserException -> AuthError.UserNotFound
                    is FirebaseNetworkException -> AuthError.NetworkError
                    is FirebaseAuthException -> {
                        when (exception.errorCode) {
                            ERROR_TOO_MANY_REQUESTS -> AuthError.TooManyRequests
                            else -> AuthError.Unknown(exception.message)
                        }
                    }
                    else -> AuthError.Unknown(exception.message)
                }
                trySend(Resource.Error(authError))
            }
        awaitClose { }
    }

    private fun saveUserToFirestoreAndRoom(
        uid: String,
        name: String,
        email: String,
        photoUrl: String?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val userData = hashMapOf<String, Any>(
            KEY_UID to uid,
            KEY_NAME to name,
            KEY_EMAIL to email,
            KEY_CREATED_AT to Timestamp.now()
        )
        photoUrl?.let { userData[KEY_PHOTO_URL] = it }

        // 1. Salva na Nuvem (Firestore)
        firestore.collection(COLLECTION_USERS).document(uid)
            .set(userData)
            .addOnSuccessListener {
                // 2. Salva no Banco Local (Room) - Doutrina Offline-First
                repositoryScope.launch {
                    try {
                        val userEntity = UserEntity(
                            uid = uid,
                            name = name,
                            email = email,
                            photoUrl = photoUrl
                        )
                        userDao.insertUser(userEntity)
                        onSuccess()
                    } catch (e: Exception) {
                        onFailure(e)
                    }
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    companion object {
        private const val ERROR_TOO_MANY_REQUESTS = "ERROR_TOO_MANY_REQUESTS"
        private const val COLLECTION_USERS = "users"
        private const val KEY_UID = "uid"
        private const val KEY_NAME = "name"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHOTO_URL = "photoUrl"
        private const val KEY_CREATED_AT = "createdAt"
        private const val MSG_USER_RETRIEVAL_FAILED = "Falha ao recuperar usuário criado"
    }
}
