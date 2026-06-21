package com.targaryen.cafeteria.feature_catalog.chat.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.targaryen.cafeteria.feature_catalog.BuildConfig
import com.targaryen.cafeteria.feature_catalog.chat.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class ChatRepositoryImpl : ChatRepository {
    override fun sendMessage(
        prompt: String,
        systemInstruction: String,
    ): Flow<String> =
        flow {
            val generativeModel =
                GenerativeModel(
                    modelName = "gemini-flash-latest",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    systemInstruction =
                        content {
                            text(systemInstruction)
                        },
                )
            val response = generativeModel.generateContent(prompt)
            emit(response.text ?: "")
        }
}
