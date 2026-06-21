package com.targaryen.cafeteria.feature_catalog.chat.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single
class ChatRemoteDataSource {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun saveSession(
        userId: String,
        session: ChatSessionEntity,
    ) {
        try {
            val sessionData =
                hashMapOf(
                    "id" to session.id,
                    "title" to session.title,
                    "lastUpdated" to session.lastUpdated,
                )
            firestore
                .collection("users")
                .document(userId)
                .collection("chat_sessions")
                .document(session.id)
                .set(sessionData)
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e("ChatRemoteDataSource", "Failed to save session to firestore", e)
        }
    }

    suspend fun saveMessage(
        userId: String,
        sessionId: String,
        message: ChatMessageEntity,
    ) {
        try {
            val messageData =
                hashMapOf(
                    "id" to message.id,
                    "text" to message.text,
                    "sender" to message.sender,
                    "timestamp" to message.timestamp,
                )
            firestore
                .collection("users")
                .document(userId)
                .collection("chat_sessions")
                .document(sessionId)
                .collection("messages")
                .document(message.id)
                .set(messageData)
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e("ChatRemoteDataSource", "Failed to save message to firestore", e)
        }
    }

    suspend fun fetchSessions(userId: String): List<ChatSessionEntity> {
        return try {
            val snapshot =
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("chat_sessions")
                    .get()
                    .await()

            snapshot.documents.mapNotNull { document ->
                val id = document.getString("id") ?: return@mapNotNull null
                val title = document.getString("title") ?: "Chat Imperial"
                val lastUpdated = document.getLong("lastUpdated") ?: 0L
                ChatSessionEntity(
                    id = id,
                    title = title,
                    userId = userId,
                    lastUpdated = lastUpdated,
                )
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e("ChatRemoteDataSource", "Failed to fetch sessions from firestore", e)
            emptyList()
        }
    }

    suspend fun fetchMessagesForSession(
        userId: String,
        sessionId: String,
    ): List<ChatMessageEntity> {
        return try {
            val snapshot =
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("chat_sessions")
                    .document(sessionId)
                    .collection("messages")
                    .get()
                    .await()

            snapshot.documents.mapNotNull { document ->
                val id = document.getString("id") ?: return@mapNotNull null
                val text = document.getString("text") ?: ""
                val sender = document.getString("sender") ?: "USER"
                val timestamp = document.getLong("timestamp") ?: 0L
                ChatMessageEntity(
                    id = id,
                    sessionId = sessionId,
                    text = text,
                    sender = sender,
                    timestamp = timestamp,
                )
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e("ChatRemoteDataSource", "Failed to fetch messages from firestore", e)
            emptyList()
        }
    }

    suspend fun deleteSession(
        userId: String,
        sessionId: String,
    ) {
        try {
            val messagesSnapshot =
                firestore
                    .collection("users")
                    .document(userId)
                    .collection("chat_sessions")
                    .document(sessionId)
                    .collection("messages")
                    .get()
                    .await()

            messagesSnapshot.documents.forEach { doc ->
                doc.reference.delete().await()
            }

            firestore
                .collection("users")
                .document(userId)
                .collection("chat_sessions")
                .document(sessionId)
                .delete()
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e("ChatRemoteDataSource", "Failed to delete session from firestore", e)
        }
    }
}
