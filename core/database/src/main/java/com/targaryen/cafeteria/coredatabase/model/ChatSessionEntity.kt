package com.targaryen.cafeteria.coredatabase.model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

/**
 * Entidade de persistência local para sessões de conversa do assistente.
 *
 * @property id Identificador único da sessão (UUID).
 * @property title Título semântico do tópico da conversa.
 * @property userId Identificador do usuário autenticado no Firebase. Permanece `null` quando a
 * sessão é iniciada em Modo Visitante (Guest/Anônimo), sendo sincronizada com a nuvem após o login.
 * @property lastUpdated Timestamp da última mensagem ou modificação da sessão.
 */
@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val userId: String?,
    val lastUpdated: Long,
)
