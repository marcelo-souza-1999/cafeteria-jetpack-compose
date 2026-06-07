package com.targaryen.cafeteria.feature_catalog.profile.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.core_network.remote.BackBlazeB2DataSource
import com.targaryen.cafeteria.core_network.util.Resource
import com.targaryen.cafeteria.coredatabase.dao.UserDao
import com.targaryen.cafeteria.coredatabase.model.UserEntity
import com.targaryen.cafeteria.feature_catalog.profile.domain.model.PurchaseHistoryItem
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileError
import com.targaryen.cafeteria.feature_catalog.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.io.IOException

@Single
class ProfileRepositoryImpl(
    private val context: Context,
    private val userDao: UserDao,
    private val backblazeB2DataSource: BackBlazeB2DataSource,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : ProfileRepository {
    override fun getProfile(): Flow<Resource<UserEntity, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val listener =
                firestore
                    .collection("users")
                    .document(currentUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                                if (localUser != null) {
                                    trySend(Resource.Success(localUser))
                                } else {
                                    trySend(Resource.Error(ProfileError.NetworkError))
                                }
                            }
                            return@addSnapshotListener
                        }

                        if (snapshot != null && snapshot.exists()) {
                            val name =
                                snapshot.getString("name") ?: currentUser.displayName ?: "Usuário Targaryen"
                            val email = snapshot.getString("email") ?: currentUser.email ?: ""
                            val photoUrl = snapshot.getString("photoUrl")

                            val entity =
                                UserEntity(
                                    uid = currentUser.uid,
                                    name = name,
                                    email = email,
                                    photoUrl = photoUrl,
                                )

                            CoroutineScope(Dispatchers.IO).launch {
                                userDao.insertUser(entity)
                                trySend(Resource.Success(entity))
                            }
                        } else {
                            CoroutineScope(Dispatchers.IO).launch {
                                val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                                if (localUser != null) {
                                    trySend(Resource.Success(localUser))
                                } else {
                                    trySend(Resource.Error(ProfileError.UserNotFound))
                                }
                            }
                        }
                    }

            awaitClose { listener.remove() }
        }

    override fun updateProfilePhoto(photoUrl: String): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            firestore
                .collection("users")
                .document(currentUser.uid)
                .update("photoUrl", photoUrl)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                        if (localUser != null) {
                            userDao.insertUser(localUser.copy(photoUrl = photoUrl))
                        }
                        trySend(Resource.Success(Unit))
                    }
                }.addOnFailureListener { exception ->
                    trySend(Resource.Error(ProfileError.Unknown(exception.message)))
                }

            awaitClose { }
        }

    override fun uploadProfilePhoto(uri: Uri): Flow<Resource<String, ProfileError>> =
        flow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                emit(Resource.Error(ProfileError.UserNotFound))
                return@flow
            }

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    emit(Resource.Error(ProfileError.Unknown("Não foi possível ler a imagem")))
                    return@flow
                }
                val fileBytes = inputStream.use { stream -> stream.readBytes() }
                val fileName = "profiles/${currentUser.uid}/profile_photo.jpg"

                val downloadUrl =
                    backblazeB2DataSource.uploadFile(
                        fileName = fileName,
                        fileBytes = fileBytes,
                        contentType = "image/jpeg",
                    )

                emit(Resource.Success("$downloadUrl?t=${System.currentTimeMillis()}"))
            } catch (e: IOException) {
                emit(Resource.Error(ProfileError.Unknown(e.message)))
            } catch (e: FirebaseAuthException) {
                emit(Resource.Error(ProfileError.Unknown(e.message)))
            }
        }

    override fun updateProfileName(name: String): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val profileUpdates =
                UserProfileChangeRequest
                    .Builder()
                    .setDisplayName(name)
                    .build()

            currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore
                        .collection("users")
                        .document(currentUser.uid)
                        .update("name", name)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                                if (localUser != null) {
                                    userDao.insertUser(localUser.copy(name = name))
                                }
                                trySend(Resource.Success(Unit))
                            }
                        }.addOnFailureListener { trySend(Resource.Error(ProfileError.Unknown(it.message))) }
                } else {
                    trySend(Resource.Error(ProfileError.Unknown(task.exception?.message)))
                }
            }
            awaitClose { }
        }

    override fun updateEmail(email: String): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            currentUser.verifyBeforeUpdateEmail(email).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Resource.Success(Unit))
                } else {
                    val ex = task.exception
                    if (ex is FirebaseAuthRecentLoginRequiredException) {
                        trySend(Resource.Error(ProfileError.InvalidPassword))
                    } else {
                        trySend(Resource.Error(ProfileError.Unknown(ex?.message)))
                    }
                }
            }
            awaitClose { }
        }

    override fun getPurchaseHistory(): Flow<Resource<List<PurchaseHistoryItem>, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val listener =
                firestore
                    .collection("orders")
                    .whereEqualTo("userId", currentUser.uid)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            trySend(Resource.Error(ProfileError.NetworkError))
                            return@addSnapshotListener
                        }

                        if (snapshot != null) {
                            val orders =
                                snapshot.documents
                                    .mapNotNull { doc ->
                                        val id = doc.id
                                        val dateMillis = doc.getLong("dateMillis") ?: 0L
                                        val totalPrice = doc.getDouble("totalPrice") ?: 0.0
                                        val itemsSummary = doc.getString("itemsSummary") ?: ""
                                        val status = doc.getString("status") ?: "Aprovado"

                                        PurchaseHistoryItem(
                                            id = id,
                                            dateMillis = dateMillis,
                                            totalPrice = totalPrice,
                                            itemsSummary = itemsSummary,
                                            status = status,
                                        )
                                    }.sortedByDescending { order -> order.dateMillis }
                            trySend(Resource.Success(orders))
                        } else {
                            trySend(Resource.Success(emptyList()))
                        }
                    }

            awaitClose { listener.remove() }
        }

    override fun updatePassword(
        currentPass: String,
        newPass: String,
    ): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val credential = EmailAuthProvider.getCredential(currentUser.email ?: "", currentPass)
            currentUser.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    currentUser.updatePassword(newPass).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            trySend(Resource.Success(Unit))
                        } else {
                            trySend(Resource.Error(ProfileError.Unknown(updateTask.exception?.message)))
                        }
                    }
                } else {
                    trySend(Resource.Error(ProfileError.InvalidPassword))
                }
            }
            awaitClose { }
        }

    override fun deleteAccount(): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val uid = currentUser.uid
            firestore.collection("users").document(uid).delete().addOnCompleteListener {
                currentUser.delete().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            userDao.deleteUserByUid(uid)
                            trySend(Resource.Success(Unit))
                        }
                    } else {
                        val ex = task.exception
                        if (ex is FirebaseAuthRecentLoginRequiredException) {
                            trySend(Resource.Error(ProfileError.InvalidPassword))
                        } else {
                            trySend(Resource.Error(ProfileError.Unknown(ex?.message)))
                        }
                    }
                }
            }

            awaitClose { }
        }
}
