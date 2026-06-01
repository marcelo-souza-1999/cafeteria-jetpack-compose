package com.targaryen.cafeteria.feature_catalog.profile.data.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.targaryen.cafeteria.core_network.Resource
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
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class ProfileRepositoryImpl(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : ProfileRepository {

    override fun getProfile(): Flow<Resource<UserEntity, ProfileError>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error(ProfileError.UserNotFound))
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(currentUser.uid)
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

                    val entity = UserEntity(
                        uid = currentUser.uid,
                        name = name,
                        email = email,
                        photoUrl = photoUrl
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

            firestore.collection("users").document(currentUser.uid)
                .update("photoUrl", photoUrl)
                .addOnSuccessListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                        if (localUser != null) {
                            userDao.insertUser(localUser.copy(photoUrl = photoUrl))
                        }
                        trySend(Resource.Success(Unit))
                    }
                }
                .addOnFailureListener { exception ->
                    trySend(Resource.Error(ProfileError.Unknown(exception.message)))
                }

            awaitClose { }
        }

    override fun uploadProfilePhoto(uri: Uri): Flow<Resource<String, ProfileError>> = callbackFlow {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            trySend(Resource.Error(ProfileError.UserNotFound))
            close()
            return@callbackFlow
        }

        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = firebaseStorage.reference.child("users/${currentUser.uid}/$fileName")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    trySend(Resource.Success(downloadUri.toString()))
                }.addOnFailureListener {
                    trySend(Resource.Error(ProfileError.Unknown(it.message)))
                }
            }
            .addOnFailureListener {
                trySend(Resource.Error(ProfileError.Unknown(it.message)))
            }

        awaitClose { }
    }

    override fun updateProfileName(name: String): Flow<Resource<Unit, ProfileError>> =
        callbackFlow {
            val currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                trySend(Resource.Error(ProfileError.UserNotFound))
                close()
                return@callbackFlow
            }

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            currentUser.updateProfile(profileUpdates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firestore.collection("users").document(currentUser.uid)
                        .update("name", name)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.IO).launch {
                                val localUser = userDao.getUserByUid(currentUser.uid).firstOrNull()
                                if (localUser != null) {
                                    userDao.insertUser(localUser.copy(name = name))
                                }
                                trySend(Resource.Success(Unit))
                            }
                        }
                        .addOnFailureListener { trySend(Resource.Error(ProfileError.Unknown(it.message))) }
                } else {
                    trySend(Resource.Error(ProfileError.Unknown(task.exception?.message)))
                }
            }
            awaitClose { }
        }

    override fun updateEmail(email: String): Flow<Resource<Unit, ProfileError>> = callbackFlow {
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

            val listener = firestore.collection("orders")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(ProfileError.NetworkError))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { doc ->
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
                                status = status
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
        newPass: String
    ): Flow<Resource<Unit, ProfileError>> = callbackFlow {
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

    override fun deleteAccount(): Flow<Resource<Unit, ProfileError>> = callbackFlow {
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
