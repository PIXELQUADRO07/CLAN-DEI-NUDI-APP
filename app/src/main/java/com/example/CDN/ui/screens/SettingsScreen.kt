package com.example.CDN.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.db.BackupEntity
import com.example.CDN.db.UserEntity
import com.example.CDN.ui.components.CyberButton
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?,
    backupsList: List<BackupEntity>
) {
    val oledMode by viewModel.isOledDarkMode.collectAsState()
    val pushSettings by viewModel.pushSettings.collectAsState()
    val gitRelease by viewModel.githubRelease.collectAsState()
    val isCheckingForUpdates by viewModel.checkingForUpdates.collectAsState()

    val repoOwner by viewModel.repoOwner.collectAsState()
    val repoName by viewModel.repoName.collectAsState()
    val serviceStatus by viewModel.serviceStatus.collectAsState()
    val serviceEnabled by viewModel.serviceEnabled.collectAsState()

    var backupNameInput by remember { mutableStateOf("") }
    var selfDestructUserVerify by remember { mutableStateOf("") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Theme Customization settings
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = viewModel.translate("theme_title", "TEMATIZZAZIONE INTERFACCIA TATTICA"),
                terminalBadge = "CUSTOM_COLOR_THEMES"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "STRUTTURA RISPARMIO ENERGETICO OLED",
                            color = CyberWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (oledMode) "DARK OLED: #000000 (PIXEL OFF)" else "LIGHT CYBER SATELLITE ACTIVE",
                            color = CyberWhite.copy(alpha = 0.5f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }

                    CyberButton(
                        text = if (oledMode) "CYBER LIGHT" else "OLED BLACK",
                        onClick = { viewModel.toggleOledTheme() },
                        modifier = Modifier.width(130.dp),
                        testTag = "toggle_oled_theme_btn"
                    )
                }
            }
        }

        // Multilingual Translation Customization
        item {
            val appLanguage by viewModel.appLanguage.collectAsState()
            
            CyberCard(
                borderColor = CyberYellow,
                titleSpec = if (appLanguage == "IT") "CONFIGURAZIONE MULTILINGUA" else if (appLanguage == "ES") "CONFIGURACIÓN MULTILINGÜE" else "MULTILINGUAL CONFIGURATION",
                terminalBadge = "LOCALE_PATCH"
            ) {
                Text(
                    text = if (appLanguage == "IT") "SELEZIONA LINGUA OPERATIVA TERMINALE:" else if (appLanguage == "ES") "SELECCIONE IDIOMA OPERATIVO DE LA TERMINAL:" else "SELECT TERMINAL OPERATIONAL LANGUAGE:",
                    color = CyberWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val languages = listOf(
                        Triple("IT", "Italiano (IT)", "🇮🇹"),
                        Triple("EN", "English (EN)", "🇬🇧"),
                        Triple("ES", "Español (ES)", "🇪🇸")
                    )
                    
                    languages.forEach { (code, name, flag) ->
                        val isSelected = appLanguage == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyberRed.copy(alpha = 0.2f) else CyberMediumGray)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyberRed else CyberGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setLanguage(code) }
                                .padding(vertical = 10.dp)
                                .testTag("lang_btn_$code"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = flag, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = code,
                                    color = CyberWhite,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom FCM Push Notification settings
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = viewModel.translate("fcm_title", "NOTIFICHE DI CLOUD MESSAGING PERSONALIZZATE"),
                terminalBadge = "FCM_ALERT_CONFIG"
            ) {
                listOf("MESSAGGI", "SONDAGGI", "ALLERTA", "SISTEMA").forEach { category ->
                    val isEnabled = pushSettings[category] ?: true
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NOTIFICHE IMPULSI $category",
                            color = CyberWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )

                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { viewModel.togglePushSetting(category) },
                            colors = SwitchDefaults.colors(checkedThumbColor = CyberRed, checkedTrackColor = CyberRed.copy(alpha = 0.4f)),
                            modifier = Modifier.testTag("push_toggle_$category")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                // Push simulator triggers inside settings! Very ease of use!
                Text(
                    text = "SIMULA FLUSSI DI NOTIFICA GCM PUSH",
                    color = CyberYellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CyberButton(
                        text = "Msg Push",
                        onClick = {
                            viewModel.triggerSimulatedNotification(
                                "Attacco d'Incursione",
                                "Allarme lanciato sul server ovest! Nuovo impulso dati.",
                                "ALLERTA"
                            )
                        },
                        color = CyberRed,
                        modifier = Modifier.weight(1f)
                    )
                    CyberButton(
                        text = "Sondaggio Push",
                        onClick = {
                            viewModel.triggerSimulatedNotification(
                                "Voto Lanciato",
                                "Disponibile sondaggio decisivo sul reticolo del clan.",
                                "SONDAGGIO"
                            )
                        },
                        color = CyberYellow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Github release updates controls
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = viewModel.translate("github_patches_title", "CONTROLLO RELEASES GITHUB"),
                terminalBadge = "GIT_PATCHER"
            ) {
                // Section 1: GitHub Connection Settings
                Text(
                    text = viewModel.translate("github_joined_repo", "REPOSITORY ABBINATO ATTIVAMENTE:"),
                    color = CyberYellow,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "github.com/$repoOwner/$repoName",
                    color = CyberWhite.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Periodic Scanning Service Sync toggles
                val statusColor = when (serviceStatus) {
                    "CHECKING" -> CyberYellow
                    "UPDATE_FOUND" -> CyberRed
                    "UP_TO_DATE" -> Color(0xFF00FF66)
                    "ERROR" -> CyberRed
                    else -> CyberWhite.copy(alpha = 0.6f)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = viewModel.translate("bg_scanner", "SERVIZIO SCANSIONE SFONDO"),
                            color = CyberWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewModel.translate("daemon_status", "STATO DAEMON"),
                                color = CyberWhite.copy(alpha = 0.4f),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "[$serviceStatus]",
                                color = statusColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Switch(
                        checked = serviceEnabled,
                        onCheckedChange = { viewModel.toggleGitHubService(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberRed, checkedTrackColor = CyberRed.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("service_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Manual trigger check and updates installation
                Text(
                    text = "${viewModel.translate("active_release", "RELEASES ATTIVA INTERFACCIA:")}\n${gitRelease?.version ?: "CLAN-V2.0.4-RED-BLACK"}",
                    color = CyberWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (isCheckingForUpdates) {
                    CircularProgressIndicator(color = CyberRed, modifier = Modifier.size(24.dp).align(Alignment.CenterHorizontally))
                } else {
                    CyberButton(
                        text = viewModel.translate("check_github_latest", "Controlla Nuova Release Github"),
                        onClick = { viewModel.checkForClusterUpdates() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "check_github_updates_btn"
                    )

                    gitRelease?.let { release ->
                        if (release.canUpdate) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = CyberYellow.copy(alpha = 0.15f),
                                modifier = Modifier.fillMaxWidth().border(1.dp, CyberYellow, RoundedCornerShape(4.dp)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "TROVATO AGGIORNAMENTO GITHUB: ${release.version}",
                                        color = CyberYellow,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "NOTE DI RILASCIO:\n" + release.changelog.joinToString("\n") { "- $it" },
                                        color = CyberWhite.copy(alpha = 0.9f),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    CyberButton(
                                        text = viewModel.translate("download_github_update", "Scarica & Installa Patch"),
                                        onClick = {
                                            viewModel.performSimulatedPatchUpdate {
                                                Toast.makeText(context, "Firmware autorigenerato!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        color = CyberYellow,
                                        testTag = "apply_github_update_btn"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Local Encrypted Backup generator system
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "BACKUP CRITTOGRAFATI SU CLOUD LOCALE",
                terminalBadge = "SECURE_RESTORE_POINT"
            ) {
                OutlinedTextField(
                    value = backupNameInput,
                    onValueChange = { backupNameInput = it },
                    modifier = Modifier.fillMaxWidth().testTag("backup_name_input"),
                    label = { Text("Nome file backup locale...", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CyberButton(
                    text = "Genera Caveau Backup Cifrato",
                    onClick = {
                        if (backupNameInput.isNotBlank()) {
                            viewModel.executeSecureSystemBackup(backupNameInput)
                            backupNameInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "run_backup_btn"
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "LOGS ARCHIVI LOCALI PROTETTI",
                    color = CyberWhite,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                if (backupsList.isEmpty()) {
                    Text(
                        text = "Nessun archivio salvato.",
                        color = CyberWhite.copy(alpha = 0.4f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                } else {
                    backupsList.forEach { backup ->
                        Surface(
                            color = CyberGray.copy(alpha = 0.3f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).border(1.dp, CyberGray, RoundedCornerShape(4.dp)),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.5f)) {
                                    Text(
                                        text = backup.backupName,
                                        color = CyberWhite,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "HASH: ${backup.id}",
                                        color = CyberWhite.copy(alpha = 0.4f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 8.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.removeBackupById(backup.id) },
                                    modifier = Modifier.size(24.dp).testTag("delete_backup_${backup.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Rimuovi backup",
                                        tint = CyberRed.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Account / Data secure self destruction Deletion wizard
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "ELIMINAZIONE ACCOUNT E DATI SENSIBILI",
                terminalBadge = "SELF_DESTRUCTION"
            ) {
                Text(
                    text = "ATTENZIONE • AZIONE IRREVERSIBILE\nINSERISCI IL TUO USERNAME PER CONFERMARE LA CANCELLAZIONE COMPLETA.",
                    color = CyberRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = selfDestructUserVerify,
                    onValueChange = { selfDestructUserVerify = it },
                    modifier = Modifier.fillMaxWidth().testTag("delete_verify_user_field"),
                    label = { Text("Nome Utente Attuale per Verifica", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                CyberButton(
                    text = "Esegui Wiping & Autodistruzione",
                    onClick = {
                        if (selfDestructUserVerify == currentUser?.username) {
                            viewModel.executeSecureWipeAndSelfDestruct(selfDestructUserVerify) {
                                Toast.makeText(context, "SISTEMA SANITIZZATO CON ECCELLENZA • ACCOUNT RIMOSSO!", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Username di verifica errato!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "self_destruct_btn"
                )
            }
        }
    }
}
