package com.example.CDN.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.db.MessageEntity
import com.example.CDN.db.UserEntity
import com.example.CDN.security.SecurityUtils
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel

@Composable
fun EncryptedChatScreen(
    viewModel: MainViewModel,
    messages: List<MessageEntity>,
    currentUser: UserEntity?,
    activeReceiver: String
) {
    var rawText by remember { mutableStateOf("") }
    var cryptoKeyInput by remember { mutableStateOf("CLAN_CRYPT_99") } // Default Key
    var viewDecryptedMsg by remember { mutableStateOf(true) }
    var isGhostMsg by remember { mutableStateOf(false) }

    // Use current radar list as chat recipients
    val members = viewModel.radarMembers.map { it.username }

    Column(
        modifier = Modifier.fillMaxSize().padding(14.dp)
    ) {
        // Horizontal Chat recipients list
        if (members.isNotEmpty()) {
            Text(
                text = "CANALI OPERATIVI VICINI",
                color = CyberWhite.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                members.forEach { member ->
                    val isActive = activeReceiver == member
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.changeActiveChatReceiver(member) }
                            .border(
                                1.dp,
                                if (isActive) CyberRed else CyberGray,
                                RoundedCornerShape(8.dp)
                            ),
                        color = if (isActive) CyberRed.copy(alpha = 0.15f) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "@$member",
                            color = if (isActive) CyberWhite else CyberWhite.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Crypto Cryptographic Key configurations panel
        CyberCard(
            borderColor = CyberRed,
            titleSpec = "CENTRALINA DIFESA CRITTOGRAFICA",
            terminalBadge = "E2E_CONFIG"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = cryptoKeyInput,
                    onValueChange = { cryptoKeyInput = it },
                    modifier = Modifier.weight(1.5f).height(50.dp).testTag("crypto_key_field"),
                    label = { Text("CHIAVE DINAMICA DES", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = CyberYellow) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "DECODIFICA",
                        color = CyberWhite,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = viewDecryptedMsg,
                        onCheckedChange = { viewDecryptedMsg = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberRed, checkedTrackColor = CyberRed.copy(alpha = 0.4f))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat stream layout
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth().border(1.dp, CyberRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp)).background(CyberDarkGray).padding(10.dp)
        ) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "NESSUN MESSAGGIO CIFRATO SCAMBIATO COM @$activeReceiver\nIMPOSTA CHIAVE COMUNE ED INVIA PRIMO IMPULSO DATI",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CyberWhite.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { message ->
                        val isMe = message.sender == currentUser?.username
                        val decrypted = SecurityUtils.decryptAES(message.encryptedBody, cryptoKeyInput)

                        val displayColor = if (isMe) CyberRed else CyberGray
                        val alignSide = if (isMe) Alignment.End else Alignment.Start

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = alignSide
                        ) {
                            val bubbleShape = if (isMe) {
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
                            } else {
                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
                            }
                            Surface(
                                color = displayColor.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .fillMaxWidth(0.85f)
                                    .border(1.dp, displayColor, bubbleShape),
                                shape = bubbleShape
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    // Header specs
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = (if (isMe) "TU [XOR]" else "@${message.sender} [XOR]") + if (message.isGhost) " [GHOST]" else "",
                                            color = if (message.isGhost) CyberRed else if (isMe) CyberRed else CyberYellow,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "[CODE_ID: ${message.idCode}]",
                                            color = CyberWhite.copy(alpha = 0.35f),
                                            fontSize = 8.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Body Toggle decryption
                                    if (viewDecryptedMsg) {
                                        Text(
                                            text = decrypted,
                                            color = CyberWhite,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    } else {
                                        // RAW PAYLOAD WITH HEX DECRYPT WINDOW
                                        Column {
                                            Text(
                                                text = "BASE64: ${message.encryptedBody}",
                                                color = CyberYellow,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.background(CyberBlack).padding(4.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "HEX DUMP DETECTOR:\n${SecurityUtils.stringToHexDump(message.encryptedBody)}",
                                                color = CyberWhite.copy(alpha = 0.5f),
                                                fontSize = 9.sp,
                                                fontFamily = FontFamily.Monospace,
                                                lineHeight = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message input console
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isGhostMsg,
                    onCheckedChange = { isGhostMsg = it },
                    colors = CheckboxDefaults.colors(checkedColor = CyberYellow, uncheckedColor = CyberGray)
                )
                Text(
                    text = "PROTOCOLLO GHOST",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = CyberYellow,
                    modifier = Modifier.clickable { isGhostMsg = !isGhostMsg }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    modifier = Modifier.weight(1.5f).testTag("chat_input_field"),
                    placeholder = { Text("Messaggio sicuro...", fontFamily = FontFamily.Monospace, fontSize = 12.sp, color = CyberWhite.copy(alpha = 0.4f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                IconButton(
                    onClick = {
                        if (rawText.isNotBlank()) {
                            viewModel.sendEncryptedMessage(rawText, cryptoKeyInput, isGhostMsg)
                            rawText = ""
                        }
                    },
                    modifier = Modifier
                        .size(50.dp)
                        .background(CyberRed, RoundedCornerShape(10.dp))
                        .testTag("send_msg_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Encrypted Message Toggle",
                        tint = CyberWhite
                    )
                }
            }
        }
    }
}
