package com.targaryen.cafeteria.feature_catalog.catalog.presentation.view.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
import com.targaryen.cafeteria.feature_catalog.chat.appfunctions.SearchCatalogRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.koin.compose.koinInject

private const val SIMULATOR_HEIGHT_FRACTION = 0.85f

private const val GEMINI_SIMULATOR_PROMPT =
    "Você é um simulador de Google Assistant para a Cafeteria Targaryen. Sua única tarefa é " +
        "receber o texto em linguagem natural do usuário e traduzir em um comando JSON estruturado " +
        "que represente uma chamada de AppFunction local do app. Não inclua markdown, formatação " +
        "de código ou texto explicativo. Retorne APENAS o JSON válido.\n" +
        "As funções disponíveis são:\n" +
        "1. Para adicionar produto ao carrinho:\n" +
        "   { \"function\": \"addProductToCart\", \"productName\": \"...\", \"quantity\": ... }\n" +
        "2. Para ver resumo do carrinho:\n" +
        "   { \"function\": \"getCartSummary\" }\n" +
        "3. Para limpar o carrinho:\n" +
        "   { \"function\": \"clearCart\" }\n" +
        "4. Para pesquisar no cardápio:\n" +
        "   { \"function\": \"searchCatalog\", \"query\": \"...\" }\n" +
        "5. Para ver status do último pedido:\n" +
        "   { \"function\": \"getLastOrderStatus\" }\n" +
        "6. Para perguntas informais gerais (se não bater com nenhuma das anteriores):\n" +
        "   { \"function\": \"askRhaenyra\", \"query\": \"...\" }"

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

    var commandText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var simulationLog by remember { mutableStateOf("Aguardando comando de voz ou texto...") }
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = DragonScale,
        dragHandle = null,
        modifier = modifier.fillMaxHeight(SIMULATOR_HEIGHT_FRACTION),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(TargaryenTheme.dimens.spaceMedium),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ValyrianGold,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.width(TargaryenTheme.dimens.spaceSmall))
                    Text(
                        text = "Targaryen Gemini Sandbox",
                        style = MaterialTheme.typography.titleMedium,
                        color = ValyrianGold,
                        fontWeight = FontWeight.Bold,
                    )
                }
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar",
                        tint = TargaryenWhite,
                    )
                }
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            // Campo de entrada do comando
            OutlinedTextField(
                value = commandText,
                onValueChange = { commandText = it },
                label = { Text("Fale ou digite o comando (ex: Adicionar 1 Blood Blend)", color = SilverHair) },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TargaryenWhite,
                        unfocusedTextColor = TargaryenWhite,
                        focusedBorderColor = ValyrianGold,
                        unfocusedBorderColor = SilverHair.copy(alpha = 0.5f),
                        focusedLabelColor = ValyrianGold,
                    ),
                trailingIcon = {
                    Row {
                        IconButton(
                            onClick = {
                                Toast
                                    .makeText(
                                        androidContext,
                                        "Use o microfone do teclado do seu celular",
                                        Toast.LENGTH_LONG,
                                    ).show()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Falar",
                                tint = ValyrianGold,
                            )
                        }
                        IconButton(
                            onClick = {
                                if (commandText.isNotBlank()) {
                                    isLoading = true
                                    simulationLog = "Enviando comando para interpretação do Gemini..."
                                    coroutineScope.launch {
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
                                            val response = model.generateContent(commandText)
                                            val responseText = response.text ?: "{}"
                                            simulationLog =
                                                "1. [Gemini Local OS Parser]\nJSON recebido:\n$responseText\n\n" +
                                                "2. [Invocando AppFunction correspondente...]\n"

                                            val json = JSONObject(responseText)
                                            val funcName = json.optString("function")

                                            val appCtx =
                                                object : androidx.appfunctions.AppFunctionContext {
                                                    override val context: android.content.Context = androidContext
                                                }

                                            val executionResult =
                                                executeSimulationAction(
                                                    funcName = funcName,
                                                    json = json,
                                                    catalogRepository = catalogRepository,
                                                    appCtx = appCtx,
                                                )
                                            simulationLog += executionResult
                                        } catch (
                                            e: com.google.ai.client.generativeai.type.GoogleGenerativeAIException,
                                        ) {
                                            simulationLog += "Falha na IA: ${e.localizedMessage}"
                                        } catch (e: org.json.JSONException) {
                                            simulationLog += "Falha de Sintaxe JSON: ${e.localizedMessage}"
                                        } catch (e: com.google.firebase.FirebaseException) {
                                            simulationLog += "Falha no Firebase: ${e.localizedMessage}"
                                        } catch (e: java.io.IOException) {
                                            simulationLog += "Erro de Conexão: ${e.localizedMessage}"
                                        } catch (e: IllegalStateException) {
                                            simulationLog += "Erro de Estado: ${e.localizedMessage}"
                                        } catch (e: IllegalArgumentException) {
                                            simulationLog += "Erro de Argumento: ${e.localizedMessage}"
                                        } catch (e: android.database.sqlite.SQLiteException) {
                                            simulationLog += "Erro de Banco Local: ${e.localizedMessage}"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
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
                                    contentDescription = "Simular",
                                    tint =
                                        if (commandText.isNotBlank()) {
                                            ValyrianGold
                                        } else {
                                            SilverHair.copy(
                                                alpha = 0.5f,
                                            )
                                        },
                                )
                            }
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            // Painel de Logs
            Text(
                text = "Console de Execução da Simulação:",
                style = MaterialTheme.typography.labelMedium,
                color = ValyrianGold,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceExtraSmall))

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Obsidian, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, SilverHair.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(TargaryenTheme.dimens.spaceMedium)
                        .verticalScroll(scrollState),
            ) {
                Text(
                    text = simulationLog,
                    style = MaterialTheme.typography.bodySmall,
                    color = TargaryenWhite.copy(alpha = 0.9f),
                    fontFamily = FontFamily.Monospace,
                )
            }

            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))

            // Instrução de uso
            Text(
                text =
                    "🛡️ Como funciona: O Gemini na nuvem atua como o " +
                        "processador semântico do sistema operacional do seu " +
                        "celular, traduzindo o comando de voz/texto em JSON. " +
                        "O aplicativo intercepta o JSON e executa a AppFunction " +
                        "Kotlin real localmente.",
                style = MaterialTheme.typography.bodySmall,
                color = SilverHair,
            )
            Spacer(modifier = Modifier.height(TargaryenTheme.dimens.spaceMedium))
        }
    }
}

private suspend fun executeSimulationAction(
    funcName: String,
    json: JSONObject,
    catalogRepository: CatalogRepository,
    appCtx: androidx.appfunctions.AppFunctionContext,
): String {
    val cartFunctions = CartAppFunctions(catalogRepository)
    val catalogFunctions = CatalogAppFunctions(catalogRepository)

    return when (funcName) {
        "addProductToCart" -> {
            val productName = json.optString("productName")
            val quantity = json.optInt("quantity", 1)
            val res =
                cartFunctions.addProductToCart(
                    context = appCtx,
                    request = AddProductRequest(productName, quantity),
                )
            "Sucesso: ${res.message}"
        }
        "getCartSummary" -> {
            val res = cartFunctions.getCartSummary(appCtx)
            "Mensagem: ${res.message}\nTotal: ${res.totalPrice} " +
                "Dragões de Ouro\nItens: ${res.items.size}"
        }
        "clearCart" -> {
            val res = cartFunctions.clearCart(appCtx)
            "Sucesso: ${res.message}"
        }
        "searchCatalog" -> {
            val query = json.optString("query")
            val res =
                catalogFunctions.searchCatalog(
                    context = appCtx,
                    request = SearchCatalogRequest(query),
                )
            "Mensagem: ${res.message}\nEncontrados: " +
                "${res.products.size} itens"
        }
        "getLastOrderStatus" -> {
            getLastOrderStatusSimulation()
        }
        "askRhaenyra" -> {
            val query = json.optString("query")
            "Rota: askRhaenyra (Encaminhado para a assistente " +
                "conversacional)\nQuery: $query\n\n" +
                "[Rhaenyra]: Use a tela de chat do app para " +
                "conversar diretamente comigo sobre este tema."
        }
        else -> {
            "Erro: Função '$funcName' não é suportada ou " +
                "reconhecida por este Sandbox."
        }
    }
}

private suspend fun getLastOrderStatusSimulation(): String {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: return "Erro: Nenhum usuário imperial autenticado no app."

    return try {
        val snapshot =
            firestore
                .collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("dateMillis", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

        if (snapshot.isEmpty) {
            "Mensagem: Nenhum pedido encontrado para o seu perfil."
        } else {
            val doc = snapshot.documents.first()
            val status = doc.getString("status") ?: "Pendente"
            val summary = doc.getString("summary") ?: ""
            val total = doc.getDouble("totalPrice") ?: 0.0
            "Último Pedido Encontrado:\nStatus: $status\n" +
                "Detalhes: $summary\nTotal: $total Dragões de Ouro"
        }
    } catch (e: com.google.firebase.FirebaseException) {
        "Falha ao consultar último pedido: ${e.localizedMessage}"
    } catch (e: java.io.IOException) {
        "Falha de conexão com o banco na nuvem: ${e.localizedMessage}"
    }
}
