package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.targaryen.cafeteria.core_designsystem.theme.BloodRed
import com.targaryen.cafeteria.core_designsystem.theme.DragonScale
import com.targaryen.cafeteria.core_designsystem.theme.Obsidian
import com.targaryen.cafeteria.core_designsystem.theme.SilverHair
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenTheme
import com.targaryen.cafeteria.core_designsystem.theme.TargaryenWhite
import com.targaryen.cafeteria.core_designsystem.theme.ValyrianGold
import com.targaryen.cafeteria.feature_catalog.BuildConfig
import com.targaryen.cafeteria.feature_catalog.catalog.domain.repository.CatalogRepository
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.AddProductRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CartAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.CatalogAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.FavoritesAppFunctions
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.RemoveProductRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.SearchCatalogRequest
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.ToggleFavoriteRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.koin.compose.koinInject
import com.targaryen.cafeteria.feature_catalog.R as CatalogR

private const val SIMULATOR_HEIGHT_FRACTION = 0.85f

private const val GEMINI_SIMULATOR_PROMPT =
    "Você é um interpretador de comandos por voz para a Cafeteria Targaryen. Sua única tarefa é " +
        "receber a frase em linguagem natural do usuário e traduzir em um comando JSON estruturado " +
        "representando uma ação do aplicativo. Não inclua markdown, crases ou texto explicativo. " +
        "Retorne APENAS o JSON válido.\n" +
        "Funções disponíveis:\n" +
        "1. Adicionar ao carrinho:\n" +
        "   { \"function\": \"addProductToCart\", \"productName\": \"...\", \"quantity\": ... }\n" +
        "2. Resumo do carrinho:\n" +
        "   { \"function\": \"getCartSummary\" }\n" +
        "3. Limpar carrinho:\n" +
        "   { \"function\": \"clearCart\" }\n" +
        "4. Pesquisar cardápio:\n" +
        "   { \"function\": \"searchCatalog\", \"query\": \"...\" }\n" +
        "5. Status do último pedido:\n" +
        "   { \"function\": \"getLastOrderStatus\" }\n" +
        "6. Perguntas informais/outras dúvidas:\n" +
        "   { \"function\": \"askRhaenyra\", \"query\": \"...\" }\n" +
        "7. Remover ou diminuir produto do carrinho:\n" +
        "   { \"function\": \"removeProductFromCart\", \"productName\": \"...\", \"quantity\": ... }\n" +
        "8. Ver banquetes favoritos:\n" +
        "   { \"function\": \"getFavoriteProducts\" }\n" +
        "9. Favoritar ou desfavoritar produto:\n" +
        "   { \"function\": \"toggleFavoriteProduct\", \"productName\": \"...\" }"

internal data class VoiceShortcut(
    val labelResId: Int,
    val command: String,
)

private data class SimulationDependencies(
    val catalogRepository: CatalogRepository,
    val firestore: FirebaseFirestore,
    val firebaseAuth: FirebaseAuth,
    val androidContext: android.content.Context,
)

internal data class VoiceExecutionResult(
    val message: String,
    val isError: Boolean = false,
)

internal data class VoiceCommandUiState(
    val commandText: String = "",
    val isLoading: Boolean = false,
    val executionResult: VoiceExecutionResult? = null,
)

internal val VOICE_SHORTCUTS =
    listOf(
        VoiceShortcut(CatalogR.string.voice_command_chip_view_cart, "Ver resumo do meu carrinho"),
        VoiceShortcut(CatalogR.string.voice_command_chip_favorites, "Ver meus banquetes favoritos"),
        VoiceShortcut(CatalogR.string.voice_command_chip_last_order, "Ver o status do meu último pedido"),
        VoiceShortcut(CatalogR.string.voice_command_chip_add_coffee, "Adicionar 1 Café Expresso Valíria ao carrinho"),
        VoiceShortcut(CatalogR.string.voice_command_chip_remove_coffee, "Remover 1 Café Expresso Valíria do carrinho"),
        VoiceShortcut(CatalogR.string.voice_command_chip_search_coffee, "Buscar cafés no cardápio"),
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeminiSimulatorBottomSheet(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val androidContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val catalogRepository = koinInject<CatalogRepository>()
    val firestore = koinInject<FirebaseFirestore>()
    val firebaseAuth = koinInject<FirebaseAuth>()

    var commandText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var executionResult by remember { mutableStateOf<VoiceExecutionResult?>(null) }

    val dependencies =
        remember(catalogRepository, firestore, firebaseAuth, androidContext) {
            SimulationDependencies(
                catalogRepository = catalogRepository,
                firestore = firestore,
                firebaseAuth = firebaseAuth,
                androidContext = androidContext,
            )
        }

    fun runCommand(targetCommand: String) {
        if (targetCommand.isNotBlank() && !isLoading) {
            commandText = targetCommand
            isLoading = true
            coroutineScope.launch {
                executionResult =
                    executeVoiceCommand(
                        command = targetCommand,
                        dependencies = dependencies,
                    )
                isLoading = false
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DragonScale,
        dragHandle = null,
        modifier = modifier.fillMaxHeight(SIMULATOR_HEIGHT_FRACTION),
    ) {
        VoiceCommandSheetContent(
            state =
                VoiceCommandUiState(
                    commandText = commandText,
                    isLoading = isLoading,
                    executionResult = executionResult,
                ),
            onCommandTextChange = { newText -> commandText = newText },
            onShortcutClick = { selectedCommand -> runCommand(selectedCommand) },
            onExecuteClick = { runCommand(commandText) },
            onDismissRequest = onDismissRequest,
        )
    }
}

@Composable
internal fun VoiceCommandSheetContent(
    state: VoiceCommandUiState,
    onCommandTextChange: (String) -> Unit,
    onShortcutClick: (String) -> Unit,
    onExecuteClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(DragonScale)
                .padding(TargaryenTheme.dimens.spaceMedium),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = ValyrianGold,
                    modifier = Modifier.size(28.dp),
                )
                Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                Column {
                    Text(
                        text = stringResource(CatalogR.string.voice_command_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = ValyrianGold,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(CatalogR.string.voice_command_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverHair,
                    )
                }
            }
            IconButton(onClick = onDismissRequest) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(CatalogR.string.voice_command_close_desc),
                    tint = TargaryenWhite,
                )
            }
        }
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

        VoiceCommandSuggestionsRow(
            shortcuts = VOICE_SHORTCUTS,
            enabled = !state.isLoading,
            onShortcutClick = onShortcutClick,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

        VoiceCommandInputField(
            commandText = state.commandText,
            onCommandTextChange = onCommandTextChange,
            isLoading = state.isLoading,
            onExecuteClick = onExecuteClick,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

        VoiceCommandResultCard(
            result = state.executionResult,
            isLoading = state.isLoading,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))

        Text(
            text = stringResource(CatalogR.string.voice_command_footer_note),
            style = MaterialTheme.typography.bodySmall,
            color = SilverHair,
        )
        Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
    }
}

@Composable
private fun VoiceCommandSuggestionsRow(
    shortcuts: List<VoiceShortcut>,
    enabled: Boolean,
    onShortcutClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        shortcuts.forEach { shortcut ->
            SuggestionChip(
                onClick = { onShortcutClick(shortcut.command) },
                enabled = enabled,
                label = {
                    Text(
                        text = stringResource(shortcut.labelResId),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (enabled) ValyrianGold else SilverHair.copy(alpha = 0.5f),
                    )
                },
                colors =
                    SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Obsidian,
                    ),
                border =
                    BorderStroke(
                        width = 1.dp,
                        color = if (enabled) ValyrianGold.copy(alpha = 0.4f) else SilverHair.copy(alpha = 0.2f),
                    ),
            )
        }
    }
}

@Composable
private fun VoiceCommandInputField(
    commandText: String,
    onCommandTextChange: (String) -> Unit,
    isLoading: Boolean,
    onExecuteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val androidContext = LocalContext.current
    val micToastMessage = stringResource(CatalogR.string.voice_command_mic_toast)
    OutlinedTextField(
        value = commandText,
        onValueChange = onCommandTextChange,
        label = {
            Text(
                stringResource(CatalogR.string.voice_command_label),
                color = SilverHair,
            )
        },
        placeholder = {
            Text(
                stringResource(CatalogR.string.voice_command_placeholder),
                color = SilverHair.copy(alpha = 0.5f),
            )
        },
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = TargaryenWhite,
                unfocusedTextColor = TargaryenWhite,
                focusedBorderColor = ValyrianGold,
                unfocusedBorderColor = SilverHair.copy(alpha = 0.5f),
                focusedLabelColor = ValyrianGold,
            ),
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        Toast
                            .makeText(
                                androidContext,
                                micToastMessage,
                                Toast.LENGTH_LONG,
                            ).show()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(CatalogR.string.voice_command_mic_desc),
                        tint = ValyrianGold,
                    )
                }
                IconButton(
                    onClick = onExecuteClick,
                    enabled = !isLoading && commandText.isNotBlank(),
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = ValyrianGold,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(CatalogR.string.voice_command_send_desc),
                            tint =
                                if (commandText.isNotBlank()) {
                                    ValyrianGold
                                } else {
                                    SilverHair.copy(alpha = 0.4f)
                                },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun VoiceCommandResultCard(
    result: VoiceExecutionResult?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val borderColor =
        when {
            result?.isError == true -> BloodRed.copy(alpha = 0.6f)
            result != null -> ValyrianGold.copy(alpha = 0.4f)
            else -> SilverHair.copy(alpha = 0.2f)
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(Obsidian, shape = RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, borderColor), shape = RoundedCornerShape(12.dp))
                .padding(TargaryenTheme.dimens.spaceMedium),
    ) {
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TargaryenTheme.dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(
                        color = ValyrianGold,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp,
                    )
                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                    Text(
                        text = stringResource(CatalogR.string.voice_command_status_processing),
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverHair,
                    )
                }
            }
            result == null -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(TargaryenTheme.dimens.spaceMedium),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = SilverHair.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp),
                    )
                    Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                    Text(
                        text = stringResource(CatalogR.string.voice_command_status_idle),
                        style = MaterialTheme.typography.bodySmall,
                        color = SilverHair,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            else -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(
                        imageVector = if (result.isError) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (result.isError) BloodRed else ValyrianGold,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceExtraSmall))
                    Text(
                        text = stringResource(CatalogR.string.voice_command_result_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (result.isError) BloodRed else ValyrianGold,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceSmall))
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TargaryenWhite,
                    modifier = Modifier.verticalScroll(scrollState),
                )
            }
        }
    }
}

private suspend fun executeVoiceCommand(
    command: String,
    dependencies: SimulationDependencies,
): VoiceExecutionResult =
    try {
        val model =
            GenerativeModel(
                modelName = "gemini-2.5-flash",
                apiKey = BuildConfig.GEMINI_API_KEY,
                systemInstruction =
                    content {
                        text(GEMINI_SIMULATOR_PROMPT)
                    },
            )
        val response = model.generateContent(command)
        val responseText = response.text ?: "{}"
        val json = JSONObject(responseText)
        val funcName = json.optString("function")

        val appCtx =
            object : androidx.appfunctions.AppFunctionContext {
                override val context: android.content.Context = dependencies.androidContext
            }

        val actionResult =
            executeSimulationAction(
                funcName = funcName,
                json = json,
                dependencies = dependencies,
                appCtx = appCtx,
            )
        VoiceExecutionResult(
            message = actionResult,
            isError = actionResult.startsWith("Erro:") || actionResult.startsWith("Falha"),
        )
    } catch (e: com.google.ai.client.generativeai.type.GoogleGenerativeAIException) {
        VoiceExecutionResult(message = "Falha na IA: ${e.localizedMessage}", isError = true)
    } catch (e: org.json.JSONException) {
        VoiceExecutionResult(message = "Falha ao interpretar comando: ${e.localizedMessage}", isError = true)
    } catch (e: com.google.firebase.FirebaseException) {
        VoiceExecutionResult(message = "Falha no Firebase: ${e.localizedMessage}", isError = true)
    } catch (e: java.io.IOException) {
        VoiceExecutionResult(message = "Erro de Conexão: ${e.localizedMessage}", isError = true)
    } catch (e: IllegalStateException) {
        VoiceExecutionResult(message = "Erro de Estado: ${e.localizedMessage}", isError = true)
    } catch (e: IllegalArgumentException) {
        VoiceExecutionResult(message = "Erro de Argumento: ${e.localizedMessage}", isError = true)
    } catch (e: android.database.sqlite.SQLiteException) {
        VoiceExecutionResult(message = "Erro no Banco Local: ${e.localizedMessage}", isError = true)
    }

private suspend fun executeSimulationAction(
    funcName: String,
    json: JSONObject,
    dependencies: SimulationDependencies,
    appCtx: androidx.appfunctions.AppFunctionContext,
): String {
    val cartFunctions = CartAppFunctions(dependencies.catalogRepository)
    val catalogFunctions = CatalogAppFunctions(dependencies.catalogRepository)
    val favoritesFunctions = FavoritesAppFunctions(dependencies.catalogRepository)

    return when (funcName) {
        "addProductToCart" -> {
            val req = AddProductRequest(json.optString("productName"), json.optInt("quantity", 1))
            cartFunctions.addProductToCart(appCtx, req).message
        }
        "removeProductFromCart" -> {
            val req = RemoveProductRequest(json.optString("productName"), json.optInt("quantity", 1))
            cartFunctions.removeProductFromCart(appCtx, req).message
        }
        "getCartSummary" -> {
            val res = cartFunctions.getCartSummary(appCtx)
            "${res.message}\nTotal: ${res.totalPrice} Dragões de Ouro (Itens: ${res.items.size})"
        }
        "clearCart" -> cartFunctions.clearCart(appCtx).message
        "getFavoriteProducts" -> favoritesFunctions.getFavoriteProducts(appCtx).message
        "toggleFavoriteProduct" -> {
            val req = ToggleFavoriteRequest(json.optString("productName"))
            favoritesFunctions.toggleFavoriteProduct(appCtx, req).message
        }
        "searchCatalog" -> {
            val res = catalogFunctions.searchCatalog(appCtx, SearchCatalogRequest(json.optString("query")))
            res.message
        }
        "getLastOrderStatus" -> getLastOrderStatusSimulation(dependencies.firestore, dependencies.firebaseAuth)
        "askRhaenyra" -> {
            val query = json.optString("query")
            "Consulta: \"$query\"\n\nRhaenyra Targaryen: Acesse o Chat para conversar diretamente comigo."
        }
        else -> "Erro: Ação '$funcName' não é suportada por esta versão do assistente."
    }
}

private suspend fun getLastOrderStatusSimulation(
    firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth,
): String {
    val userId = firebaseAuth.currentUser?.uid ?: return "Erro: Nenhum usuário imperial autenticado no app."

    return try {
        val snapshot =
            firestore
                .collection("orders")
                .whereEqualTo("userId", userId)
                .get()
                .await()

        val latestDoc =
            snapshot.documents.maxByOrNull { doc ->
                doc.getLong("dateMillis") ?: 0L
            }

        if (latestDoc == null) {
            "Nenhum pedido recente foi encontrado nos pergaminhos reais."
        } else {
            val status = latestDoc.getString("status") ?: "Pendente"
            val summary =
                latestDoc.getString("itemsSummary")
                    ?: latestDoc.getString("summary")
                    ?: "Itens imperiais"
            val total = latestDoc.getDouble("totalPrice") ?: 0.0
            "Último Pedido Real:\nStatus: $status\nDetalhes: $summary\nTotal: $total Dragões de Ouro"
        }
    } catch (e: com.google.firebase.FirebaseException) {
        "Falha ao consultar último pedido: ${e.localizedMessage}"
    } catch (e: java.io.IOException) {
        "Falha de conexão com o banco na nuvem: ${e.localizedMessage}"
    }
}

@Preview(name = "Voice Command Sheet", showBackground = true)
@Composable
fun VoiceCommandSheetContentPreview() {
    TargaryenTheme {
        VoiceCommandSheetContent(
            state =
                VoiceCommandUiState(
                    commandText = "Adicionar 1 Café Expresso Valíria",
                    isLoading = false,
                    executionResult =
                        VoiceExecutionResult(
                            message = "1x Café Expresso Valíria adicionado ao carrinho com sucesso.",
                            isError = false,
                        ),
                ),
            onCommandTextChange = {},
            onShortcutClick = {},
            onExecuteClick = {},
            onDismissRequest = {},
        )
    }
}
