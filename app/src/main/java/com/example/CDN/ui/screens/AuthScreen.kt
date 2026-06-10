package com.example.CDN.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.CDN.R
import com.example.CDN.ui.components.*
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel

@Composable
fun AuthScreen(
    viewModel: MainViewModel,
    onAuthSuccess: () -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var tacticalPin by remember { mutableStateOf("") }
    var actionError by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            // Clan Logo header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, CyberRed, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clan_logo),
                    contentDescription = "Clan dei Nudi Logo",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "CLAN DEI NUDI APP",
                color = CyberWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )

            Text(
                text = "MODULO ACCESSO FIREBASE LIVE NODES",
                color = CyberYellow,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            CyberCard(
                borderColor = CyberRed,
                titleSpec = if (isLoginMode) "AUTENTICAZIONE UTENTE" else "NOTARIATO NUOVO OPERATIVO",
                terminalBadge = "FIREBASE_AUTH"
            ) {
                // Email/Username Node
                OutlinedTextField(
                    value = if (isLoginMode) email else username,
                    onValueChange = { if (isLoginMode) email = it else username = it },
                    modifier = Modifier.fillMaxWidth().testTag("username_auth_input"),
                    label = { Text(if (isLoginMode) "E-MAIL CLAN" else "ID UTENTE CLAN", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(10.dp))
                    // Email Node
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth().testTag("email_auth_input"),
                        label = { Text("E-MAIL INDIRIZZO CLAN", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Password Node
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth().testTag("password_auth_input"),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("CHIAVE D'ACCESSO CRIPTO", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Tactical PIN 2FA
                OutlinedTextField(
                    value = tacticalPin,
                    onValueChange = { tacticalPin = it.filter { char -> char.isDigit() }.take(6) },
                    modifier = Modifier.fillMaxWidth().testTag("pin_auth_input"),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("PIN TATTICO (2FA)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberYellow)
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (actionError.isNotEmpty()) {
                    Text(
                        text = actionError.uppercase(),
                        color = CyberYellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                CyberButton(
                    text = if (isLoginMode) "ACCEDI AL CANALE" else "REGISTRA ACCOUNT COGNITIVO",
                    onClick = {
                        actionError = ""
                        if (tacticalPin.length != 6) {
                            actionError = "IL PIN TATTICO DEVE ESSERE DI 6 CIFRE"
                            return@CyberButton
                        }
                        if (isLoginMode) {
                            viewModel.attemptLoginCredentials(
                                mail = email,
                                pass = password,
                                pin = tacticalPin,
                                onSuccess = { onAuthSuccess() },
                                onError = { actionError = it }
                            )
                        } else {
                            viewModel.attemptRegisterCredentials(
                                user = username,
                                mail = email,
                                pass = password,
                                pin = tacticalPin,
                                onSuccess = { onAuthSuccess() },
                                onError = { actionError = it }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_auth_btn"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mode switcher
                TextButton(
                    onClick = {
                        isLoginMode = !isLoginMode
                        actionError = ""
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally).testTag("switch_auth_mode_btn")
                ) {
                    Text(
                        text = if (isLoginMode) "CREA NUOVO CODICE UTENTE (REGISTRAZIONE)" else "L'UTENTE ESISTE GIÀ? ACCEDI",
                        color = CyberWhite.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
