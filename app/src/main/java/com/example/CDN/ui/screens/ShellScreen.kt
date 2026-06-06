package com.example.CDN.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()
    var commandInput by remember { mutableStateOf("") }
    var history by remember {
        mutableStateOf(
            listOf(
                "=== SISTEMA OPERATIVO SECURE CLAN SHELL v2.4 ===",
                "Digita 'help' per la lista dei comandi abilitati.",
                "Dispositivo pronto per ricevere istruzioni."
            )
        )
    }

    val lazyListState = rememberLazyListState()

    // Dynamic strings via translations
    val titleLabel = viewModel.translate("shell_title", "TERMINALE SHELL STRUTTURATO")
    val badgeLabel = viewModel.translate("shell_badge", "SECURE_ROOT_SHELL")
    val placeholderLabel = viewModel.translate("shell_placeholder", "Digita un comando... (es. help)")
    val hintLabel = viewModel.translate("shell_hint", "Terminale di controllo locale ad accesso privilegiato.")

    // Auto scroll bottom when history grows
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            lazyListState.animateScrollToItem(history.size - 1)
        }
    }

    fun executeRealShellCommand(cmd: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            var errLine: String?
            while (errReader.readLine().also { errLine = it } != null) {
                output.append("[ERR] ").append(errLine).append("\n")
            }

            process.waitFor()
            val finalOut = output.toString().trim()
            if (finalOut.isEmpty()) "Comand eseguito correttamente (Nessun output)." else finalOut
        } catch (e: Exception) {
            "Errore UNIX: ${e.localizedMessage}"
        }
    }

    fun onRunCommand(cmdString: String) {
        if (cmdString.trim().isEmpty()) return
        val currentCmd = cmdString.trim()
        history = history + "$ $currentCmd"
        commandInput = ""

        coroutineScope.launch(Dispatchers.IO) {
            val result = when (currentCmd.lowercase()) {
                "clear" -> {
                    history = emptyList()
                    ""
                }
                "help", "menu" -> {
                    """
                    === COMANDI ABILITATI ===
                    - ls         : Elenca i file della directory locale
                    - pwd        : Stampa il percorso attuale
                    - date       : Mostra ora di sistema del dispositivo
                    - uptime     : Tempo di attività calcolato del kernel
                    - whoami     : Recupera utente sandbox corrente
                    - systeminfo : Info sul chip hardware reale
                    - df         : Spazio residuo sul disco interno
                    - ping       : Esegue un test di ping basilare (es. ping -c 1 google.com)
                    
                    --- DIALOGO CLAN ---
                    - app-status : Mostra lo stato dei node offline e Firebase
                    - app-update : Forza la ricerca di release su GitHub
                    - clear      : Pulisce la cronologia del terminale
                    """.trimIndent()
                }
                "app-status" -> {
                    "--- STATO STRUMENTO CLAN ---\n" +
                    "Local Room DB : ClanDatabase (OPERATIVO)\n" +
                    "Firebase Sync: ${if (viewModel.firebaseSynced.value) "ONLINE (CONNESSO)" else "OFFLINE"}\n" +
                    "Proprietario: ${viewModel.repoOwner.value}\n" +
                    "Repository  : ${viewModel.repoName.value}\n" +
                    "Stato Daemon: ${viewModel.serviceStatus.value}\n"
                }
                "app-update" -> {
                    viewModel.checkForClusterUpdates()
                    "Trasmesso segnale di scansione periodica al daemon GitHub..."
                }
                "systeminfo", "sysinfo" -> {
                    "--- INFORMAZIONI HARDWARE DISPOSITIVO ---\n" +
                    "PRODUTTORE: ${android.os.Build.MANUFACTURER}\n" +
                    "MODELLO   : ${android.os.Build.MODEL}\n" +
                    "BOARD     : ${android.os.Build.BOARD}\n" +
                    "HARDWARE  : ${android.os.Build.HARDWARE}\n" +
                    "OS VERSION: Android ${android.os.Build.VERSION.RELEASE} (SDK ${android.os.Build.VERSION.SDK_INT})\n"
                }
                else -> {
                    executeRealShellCommand(currentCmd)
                }
            }

            if (result.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    history = history + result
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        CyberCard(
            titleSpec = titleLabel,
            terminalBadge = badgeLabel,
            borderColor = CyberRed,
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = hintLabel,
                    color = CyberMutedText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Shell output trace log
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(CyberSlateBg.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .border(1.dp, CyberCrimsonBorder, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    items(history) { line ->
                        val textColor = when {
                            line.startsWith("$ ") -> CyberNeonRed
                            line.startsWith("[ERR]") -> CyberRed
                            line.startsWith("===") || line.startsWith("---") -> CyberYellow
                            else -> Color(0xFF00FF66) // Classic Neon green terminal look
                        }
                        Text(
                            text = line,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Quick Action Buttons
                Text(
                    text = "COMANDI PREFISSATI RAPIDI:",
                    color = CyberYellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickCmds = listOf("help", "ls", "pwd", "whoami", "app-status", "systeminfo", "clear")
                    quickCmds.take(4).forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberMediumGray)
                                .border(1.dp, CyberGray, RoundedCornerShape(6.dp))
                                .clickable { onRunCommand(cmd) }
                                .padding(vertical = 6.dp)
                                .testTag("quick_cmd_$cmd"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cmd,
                                color = CyberWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val quickCmdsMore = listOf("app-status", "systeminfo", "clear")
                    quickCmdsMore.forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberMediumGray)
                                .border(1.dp, CyberGray, RoundedCornerShape(6.dp))
                                .clickable { onRunCommand(cmd) }
                                .padding(vertical = 6.dp)
                                .testTag("quick_cmd_$cmd"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cmd,
                                color = CyberWhite,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Input layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Shell cursor",
                        tint = CyberNeonRed,
                        modifier = Modifier.size(20.dp)
                    )

                    OutlinedTextField(
                        value = commandInput,
                        onValueChange = { commandInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("terminal_input_field"),
                        placeholder = {
                            Text(
                                text = placeholderLabel,
                                color = CyberMutedText.copy(alpha = 0.5f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF00FF66),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CyberSlateBg,
                            unfocusedContainerColor = CyberSlateBg,
                            focusedBorderColor = CyberNeonRed,
                            unfocusedBorderColor = CyberGray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (commandInput.isNotEmpty()) {
                                    onRunCommand(commandInput)
                                }
                            }
                        )
                    )
                }
            }
        }
    }
}
