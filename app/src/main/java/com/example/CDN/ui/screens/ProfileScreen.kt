package com.example.CDN.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.R
import com.example.CDN.db.UserEntity
import com.example.CDN.ui.components.CyberButton
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel

@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?
) {
    var editBioText by remember { mutableStateOf("") }
    var editPinText by remember { mutableStateOf("") }
    var isEditingBio by remember { mutableStateOf(false) }
    
    val adminUsers by viewModel.adminUsersList.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            editBioText = currentUser.bio
            editPinText = currentUser.tacticalPin
            if (currentUser.role == "ADMIN") {
                viewModel.fetchAdminUserList()
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            // Profile Graphic Avatar & Sync metrics
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "ACCOUNT OPERATIVO",
                terminalBadge = if (currentUser?.firebaseSynced == true) "FIREBASE_ONLINE" else "LOCAL_SECURE"
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .border(2.dp, CyberRed, RoundedCornerShape(20.dp))
                            .background(CyberBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.clan_logo),
                            contentDescription = "Avatar Clan Logo",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "@${currentUser?.username ?: "anonimo"}",
                        color = CyberWhite,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = currentUser?.email ?: "nessun_aggancio_email@clan.cyber",
                        color = CyberWhite.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // IG Metrics: Followers and followings updated!
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentUser?.followersCount?.toString() ?: "0",
                                color = CyberWhite,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "SEGUACI CLAN",
                                color = CyberWhite.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentUser?.followingCount?.toString() ?: "0",
                                color = CyberWhite,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "CONNESSI SEGUITI",
                                color = CyberWhite.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "NODO-S3",
                                color = CyberYellow,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "STABILITÀ",
                                color = CyberWhite.copy(alpha = 0.4f),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            // Bio Section with interactive Console Editor
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "BIO STRUTTURALI & PIN",
                terminalBadge = "BIO_TERMINAL"
            ) {
                if (isEditingBio) {
                    OutlinedTextField(
                        value = editBioText,
                        onValueChange = { editBioText = it },
                        modifier = Modifier.fillMaxWidth().testTag("bio_input_field"),
                        label = { Text("BIO OPERATIVA", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = CyberWhite.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editPinText,
                        onValueChange = { editPinText = it.filter { c -> c.isDigit() }.take(6) },
                        modifier = Modifier.fillMaxWidth().testTag("pin_input_field"),
                        label = { Text("NUOVO PIN TATTICO (6 CIFRE)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = CyberYellow) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberYellow)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CyberButton(
                            text = "Salva",
                            onClick = {
                                if (editPinText.length == 6) {
                                    viewModel.updateUserProfile(editBioText, editPinText)
                                    isEditingBio = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            testTag = "save_bio_btn"
                        )
                        CyberButton(
                            text = "Annulla",
                            onClick = { isEditingBio = false },
                            modifier = Modifier.weight(1f),
                            isSecondary = true
                        )
                    }
                } else {
                    Text(
                        text = currentUser?.bio ?: "Nessun dato registrato nella bio.",
                        color = CyberWhite.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CyberButton(
                        text = "Aggiorna Profilo e PIN",
                        onClick = { isEditingBio = true },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "edit_bio_btn"
                    )
                }
            }
        }

        if (currentUser?.role == "ADMIN") {
            item {
                CyberCard(
                    borderColor = CyberYellow,
                    titleSpec = "PANNELLO ADMIN / CLAN ROOT",
                    terminalBadge = "ROOT_ACCESS"
                ) {
                    Text(
                        text = "GESTIONE AGENTI OPERATIVI",
                        color = CyberWhite,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        adminUsers.forEach { user ->
                            Row(
                                modifier = Modifier.fillMaxWidth().border(1.dp, CyberGray, RoundedCornerShape(4.dp)).padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "@${user.username}", color = CyberRed, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = "RUOLO: ${user.role}", color = CyberWhite.copy(alpha = 0.7f), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (user.role != "ADMIN") {
                                        Text(
                                            text = "[PROMUOVI]",
                                            color = CyberYellow,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.clickable { viewModel.adminChangeUserRole(user.username, "ADMIN") }
                                        )
                                    }
                                    if (user.role != "SILENTE") {
                                        Text(
                                            text = "[SILENZIA]",
                                            color = CyberWhite.copy(alpha = 0.5f),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.clickable { viewModel.adminChangeUserRole(user.username, "SILENTE") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            // Firebase Live node sync toggling
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "SINCRONIZZATORE LIVE CLOUD FIREBASE",
                terminalBadge = "NODE_AUTH_SYNC"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Text(
                            text = if (currentUser?.firebaseSynced == true) "SYNC LIVE FIREBASE ACCESO" else "SYNC LIVE OFFLINE PROTECTED",
                            color = CyberWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Invia ed aggancia le credenziali ad un nodo Firebase.",
                            color = CyberWhite.copy(alpha = 0.4f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Switch(
                        checked = currentUser?.firebaseSynced == true,
                        onCheckedChange = { viewModel.toggleFirebaseSync() },
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberRed, checkedTrackColor = CyberRed.copy(alpha = 0.4f))
                    )
                }
            }
        }

        item {
            CyberButton(
                text = "DISCONNETTI TERMINALE",
                onClick = { viewModel.performLogout() },
                modifier = Modifier.fillMaxWidth(),
                isSecondary = true,
                testTag = "logout_btn"
            )
        }
    }
}
