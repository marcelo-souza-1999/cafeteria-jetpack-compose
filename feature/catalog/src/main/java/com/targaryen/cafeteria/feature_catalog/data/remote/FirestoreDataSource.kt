package com.targaryen.cafeteria.feature_catalog.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.annotation.Single

@Single
class FirestoreDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("products")

    fun streamProducts(): Flow<QuerySnapshot> = callbackFlow {
        val listenerRegistration = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(snapshot)
            }
        }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}
