package com.targaryen.cafeteria.feature_catalog.chat.presentation.view

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.R
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatMessage
import com.targaryen.cafeteria.feature_catalog.chat.domain.model.ChatSender
import com.targaryen.cafeteria.feature_catalog.chat.presentation.intent.ChatIntent
import com.targaryen.cafeteria.feature_catalog.chat.presentation.state.ChatUiState
import com.targaryen.cafeteria.feature_catalog.chat.presentation.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreenContent(
        uiState = uiState,
        onIntent = { intent -> viewModel.onIntent(intent) },
        modifier = modifier,
    )
}

@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    onIntent: (ChatIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val welcomeMessage =
        ChatMessage(
            text = stringResource(R.string.chat_welcome_message),
            sender = ChatSender.AI,
        )
    val displayMessages =
        remember(uiState.messages) {
            uiState.messages.ifEmpty {
                listOf(welcomeMessage)
            }
        }

    LaunchedEffect(displayMessages.size, uiState.isLoading) {
        if (displayMessages.isNotEmpty()) {
            lazyListState.animateScrollToItem(displayMessages.size - 1)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Obsidian)
                .imePadding(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(DragonScale)
                    .padding(
                        horizontal = TargaryenTheme.dimens.spaceMedium,
                        vertical = TargaryenTheme.dimens.spaceSmall,
                    ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = ValyrianGold,
                    modifier = Modifier.size(TargaryenTheme.dimens.chatAvatarSize),
                )
                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                Column {
                    Text(
                        text = stringResource(R.string.chat_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = ValyrianGold,
                    )
                    Text(
                        text = stringResource(R.string.chat_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverHair.copy(alpha = 0.8f),
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onIntent(ChatIntent.NewSession) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nova Conversa",
                        tint = ValyrianGold,
                    )
                }
                IconButton(onClick = { onIntent(ChatIntent.ToggleHistory(true)) }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Histórico de Chat",
                        tint = ValyrianGold,
                    )
                }
                IconButton(
                    onClick = { onIntent(ChatIntent.ClearChat) },
                    enabled = uiState.currentSessionId != null,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.chat_clear_desc),
                        tint = if (uiState.currentSessionId != null) BloodRed else SilverHair.copy(alpha = 0.5f),
                    )
                }
            }
        }

        LazyColumn(
            state = lazyListState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentPadding = PaddingValues(TargaryenTheme.dimens.spaceMedium),
            verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceNormal),
        ) {
            items(
                items = displayMessages,
                key = { message -> message.id },
            ) { message ->
                ChatBubble(message = message)
            }

            if (uiState.isLoading) {
                item(key = "thinking_indicator") {
                    ThinkingIndicator()
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(DragonScale)
                    .padding(TargaryenTheme.dimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { text -> onIntent(ChatIntent.UpdateInputText(text)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder),
                        color = SilverHair.copy(alpha = 0.5f),
                    )
                },
                modifier = Modifier.weight(1f),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ValyrianGold,
                        unfocusedBorderColor = SilverHair.copy(alpha = 0.3f),
                        focusedContainerColor = Obsidian,
                        unfocusedContainerColor = Obsidian,
                        focusedTextColor = TargaryenWhite,
                        unfocusedTextColor = TargaryenWhite,
                    ),
                shape = RoundedCornerShape(TargaryenTheme.dimens.chatInputRadius),
                maxLines = 3,
                keyboardOptions =
                    KeyboardOptions(
                        imeAction = ImeAction.Send,
                    ),
                keyboardActions =
                    KeyboardActions(
                        onSend = {
                            if (uiState.inputText.isNotBlank()) {
                                onIntent(ChatIntent.SendMessage(uiState.inputText))
                            }
                        },
                    ),
            )

            Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))

            IconButton(
                onClick = { onIntent(ChatIntent.SendMessage(uiState.inputText)) },
                enabled = uiState.inputText.isNotBlank() && !uiState.isLoading,
                modifier =
                    Modifier
                        .background(
                            color = if (uiState.inputText.isNotBlank()) ValyrianGold else SilverHair.copy(alpha = 0.2f),
                            shape = CircleShape,
                        ).size(TargaryenTheme.dimens.chatSendButtonSize),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_send_desc),
                    tint = Obsidian,
                )
            }
        }

        if (uiState.showHistory) {
            ChatHistoryBottomSheet(
                uiState = uiState,
                onIntent = onIntent,
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == ChatSender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment,
    ) {
        val radiusLarge = TargaryenTheme.dimens.chatBubbleRadius
        val radiusNone = TargaryenTheme.dimens.none
        val bubbleShape =
            if (isUser) {
                RoundedCornerShape(radiusLarge, radiusLarge, radiusNone, radiusLarge)
            } else {
                RoundedCornerShape(radiusLarge, radiusLarge, radiusLarge, radiusNone)
            }

        val bubbleBackground =
            if (isUser) {
                DragonScale
            } else {
                Obsidian
            }

        val borderModifier =
            if (isUser) {
                Modifier.border(TargaryenTheme.dimens.borderSmall, BloodRed.copy(alpha = 0.5f), bubbleShape)
            } else {
                Modifier.border(TargaryenTheme.dimens.borderSmall, ValyrianGold.copy(alpha = 0.6f), bubbleShape)
            }

        Box(
            modifier =
                Modifier
                    .widthIn(max = TargaryenTheme.dimens.chatBubbleMaxWidth)
                    .clip(bubbleShape)
                    .background(bubbleBackground)
                    .then(borderModifier)
                    .padding(
                        horizontal = TargaryenTheme.dimens.spaceNormal,
                        vertical = TargaryenTheme.dimens.spaceSmall,
                    ),
        ) {
            Text(
                text = parseMarkdownToAnnotatedString(message.text),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) TargaryenWhite else ValyrianGold,
            )
        }
    }
}

@Composable
fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "ThinkingPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "ThinkingAlpha",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = TargaryenTheme.dimens.spaceSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(TargaryenTheme.dimens.chatThinkingRadius))
                    .background(DragonScale)
                    .border(
                        TargaryenTheme.dimens.borderSmall,
                        ValyrianGold.copy(alpha = 0.3f),
                        RoundedCornerShape(TargaryenTheme.dimens.chatThinkingRadius),
                    ).padding(
                        horizontal = TargaryenTheme.dimens.spaceNormal,
                        vertical = TargaryenTheme.dimens.spaceSmall,
                    ).alpha(alpha),
        ) {
            Text(
                text = stringResource(R.string.chat_thinking_indicator),
                style = MaterialTheme.typography.bodySmall,
                color = ValyrianGold,
            )
        }
    }
}

private fun parseMarkdownToAnnotatedString(text: String): AnnotatedString =
    buildAnnotatedString {
        val parts = text.split("**")
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryBottomSheet(
    uiState: ChatUiState,
    onIntent: (ChatIntent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onIntent(ChatIntent.ToggleHistory(false)) },
        containerColor = Obsidian,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceMedium),
        ) {
            Text(
                text = "Histórico de Conversas",
                style = MaterialTheme.typography.titleLarge,
                color = ValyrianGold,
                modifier = Modifier.padding(bottom = TargaryenTheme.dimens.spaceMedium),
            )

            if (uiState.sessions.isEmpty()) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = TargaryenTheme.dimens.spaceLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Nenhum chat anterior encontrado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverHair.copy(alpha = 0.6f),
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(TargaryenTheme.dimens.spaceSmall),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        items = uiState.sessions,
                        key = { session -> session.id },
                    ) { session ->
                        val isSelected = session.id == uiState.currentSessionId
                        val formatter =
                            DateTimeFormatter
                                .ofPattern("dd/MM/yyyy HH:mm")
                                .withZone(ZoneId.systemDefault())
                        val formattedDate = formatter.format(Instant.ofEpochMilli(session.lastUpdated))

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(TargaryenTheme.dimens.chatInputRadius))
                                    .background(if (isSelected) DragonScale else Obsidian)
                                    .border(
                                        width = TargaryenTheme.dimens.borderSmall,
                                        color = if (isSelected) ValyrianGold else SilverHair.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(TargaryenTheme.dimens.chatInputRadius),
                                    ).padding(TargaryenTheme.dimens.spaceNormal),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .clickable {
                                            onIntent(ChatIntent.SelectSession(session.id))
                                            onIntent(ChatIntent.ToggleHistory(false))
                                        },
                            ) {
                                Text(
                                    text = session.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected) ValyrianGold else TargaryenWhite,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                )
                                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraSmall))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SilverHair.copy(alpha = 0.6f),
                                )
                            }
                            IconButton(onClick = { onIntent(ChatIntent.DeleteSession(session.id)) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir chat",
                                    tint = BloodRed,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceLarge))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    TargaryenTheme {
        ChatScreenContent(
            uiState =
                ChatUiState(
                    messages =
                        listOf(
                            ChatMessage(text = "Olá, Rainha Rhaenyra!", sender = ChatSender.USER),
                            ChatMessage(text = "Saudações, nobre cliente imperial!", sender = ChatSender.AI),
                        ),
                ),
            onIntent = {},
        )
    }
}
