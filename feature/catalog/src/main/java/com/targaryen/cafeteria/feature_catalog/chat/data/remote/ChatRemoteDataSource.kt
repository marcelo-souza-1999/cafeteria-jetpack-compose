package com.targaryen.cafeteria.feature_catalog.chat.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.targaryen.cafeteria.coredatabase.model.ChatMessageEntity
import com.targaryen.cafeteria.coredatabase.model.ChatSessionEntity
import kotlinx.coroutines.tasks.await
import org.koin.core.annotation.Single

@Single
class ChatRemoteDataSource(
    private val firestore: FirebaseFirestore,
) {
    suspend fun saveSession(
        userId: String,
        session: ChatSessionEntity,
    ) {
        try {
            val sessionData =
                hashMapOf(
                    FIELD_ID to session.id,
                    FIELD_TITLE to session.title,
                    FIELD_LAST_UPDATED to session.lastUpdated,
                )
            firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SESSIONS)
                .document(session.id)
                .set(sessionData)
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, ERROR_SAVE_SESSION, e)
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
                    FIELD_ID to message.id,
                    FIELD_TEXT to message.text,
                    FIELD_SENDER to message.sender,
                    FIELD_TIMESTAMP to message.timestamp,
                )
            firestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_SESSIONS)
                .document(sessionId)
                .collection(COLLECTION_MESSAGES)
                .document(message.id)
                .set(messageData)
                .await()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, ERROR_SAVE_MESSAGE, e)
        }
    }

    suspend fun fetchSessions(userId: String): List<ChatSessionEntity> {
        return try {
            val snapshot =
                firestore
                    .collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SESSIONS)
                    .get()
                    .await()

            snapshot.documents.mapNotNull { document ->
                val id = document.getString(FIELD_ID) ?: return@mapNotNull null
                val title = document.getString(FIELD_TITLE) ?: DEFAULT_CHAT_TITLE
                val lastUpdated = document.getLong(FIELD_LAST_UPDATED) ?: 0L
                ChatSessionEntity(
                    id = id,
                    title = title,
                    userId = userId,
                    lastUpdated = lastUpdated,
                )
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, ERROR_FETCH_SESSIONS, e)
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
                    .collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SESSIONS)
                    .document(sessionId)
                    .collection(COLLECTION_MESSAGES)
                    .get()
                    .await()

            snapshot.documents.mapNotNull { document ->
                val id = document.getString(FIELD_ID) ?: return@mapNotNull null
                val text = document.getString(FIELD_TEXT) ?: ""
                val sender = document.getString(FIELD_SENDER) ?: DEFAULT_SENDER_USER
                val timestamp = document.getLong(FIELD_TIMESTAMP) ?: 0L
                ChatMessageEntity(
                    id = id,
                    sessionId = sessionId,
                    text = text,
                    sender = sender,
                    timestamp = timestamp,
                )
            }
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, ERROR_FETCH_MESSAGES, e)
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
                    .collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SESSIONS)
                    .document(sessionId)
                    .collection(COLLECTION_MESSAGES)
                    .get()
                    .await()

            val batch = firestore.batch()
            messagesSnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            val sessionRef =
                firestore
                    .collection(COLLECTION_USERS)
                    .document(userId)
                    .collection(COLLECTION_SESSIONS)
                    .document(sessionId)
            batch.delete(sessionRef)
            batch.commit().await()
        } catch (e: FirebaseFirestoreException) {
            Log.e(TAG, ERROR_DELETE_SESSION, e)
        }
    }

    companion object {
        private const val TAG = "ChatRemoteDataSource"

        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_SESSIONS = "chat_sessions"
        private const val COLLECTION_MESSAGES = "messages"

        private const val FIELD_ID = "id"
        private const val FIELD_TITLE = "title"
        private const val FIELD_LAST_UPDATED = "lastUpdated"
        private const val FIELD_TEXT = "text"
        private const val FIELD_SENDER = "sender"
        private const val FIELD_TIMESTAMP = "timestamp"

        private const val ERROR_SAVE_SESSION = "Failed to save session to firestore"
        private const val ERROR_SAVE_MESSAGE = "Failed to save message to firestore"
        private const val ERROR_FETCH_SESSIONS = "Failed to fetch sessions from firestore"
        private const val ERROR_FETCH_MESSAGES = "Failed to fetch messages from firestore"
        private const val ERROR_DELETE_SESSION = "Failed to delete session from firestore"

        private const val DEFAULT_CHAT_TITLE = "Chat Imperial"
        private const val DEFAULT_SENDER_USER = "USER"
    }
}
