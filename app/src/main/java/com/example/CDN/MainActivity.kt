package com.example.CDN

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.CDN.ui.components.*
import com.example.CDN.ui.screens.*
import com.example.CDN.ui.theme.MyApplicationTheme
import com.example.CDN.viewmodel.MainViewModel

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val viewModel: MainViewModel = viewModel()
      val isOledMode by viewModel.isOledDarkMode.collectAsState()

      MyApplicationTheme(darkTheme = isOledMode) {
        val currentScreen by viewModel.currentScreen.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()
        val postsFeed by viewModel.postsFeed.collectAsState()
        val currentChatMessages by viewModel.currentChatMessages.collectAsState()
        val backupsList by viewModel.databaseBackups.collectAsState()
        val activeChatReceiver by viewModel.activeChatReceiver.collectAsState()
        val appLanguage by viewModel.appLanguage.collectAsState()

        val isAppLocked by viewModel.isAppLocked.collectAsState()
        val isScanningBiometrics by viewModel.isAuthenticatingBiometrics.collectAsState()
        val biometricError by viewModel.biometricAuthError.collectAsState()

        val activeNotification by viewModel.activeNotification.collectAsState()

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          when (currentScreen) {
            "splash" -> {
              GlitchLoader {
                viewModel.navigateTo("login")
              }
            }

            "login" -> {
              AuthScreen(viewModel = viewModel) {
                viewModel.navigateTo("lock")
              }
            }

            "lock" -> {
              BiometricLockOverlay(
                isScanning = isScanningBiometrics,
                errorMessage = biometricError,
                onTriggerScan = {
                    showBiometricPrompt(
                        onSuccess = {
                            viewModel.navigateTo("feed")
                        },
                        onError = { error ->
                             viewModel.triggerSimulatedNotification("ERRORE BIOMETRICO", error, "ALLERTA")
                        }
                    )
                }
              )
            }

            else -> {
              Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                  Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                      .fillMaxWidth()
                      .statusBarsPadding()
                      .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    tonalElevation = 8.dp
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                          modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(6.dp))
                        ) {
                          Image(
                            painter = painterResource(id = R.drawable.clan_logo),
                            contentDescription = "Clan Logo Mini",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                          )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                          modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (currentUser?.firebaseSynced == true) Color.Green else Color.Red)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = viewModel.translate("app_title", "CLAN DEI NUDI APP"),
                          fontSize = 13.sp,
                          fontFamily = FontFamily.Monospace,
                          fontWeight = FontWeight.Black,
                          letterSpacing = 1.5.sp,
                          color = MaterialTheme.colorScheme.onBackground
                        )
                      }

                      Surface(
                        color = if (currentScreen == "profile") MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier
                          .border(
                            1.dp,
                            if (currentScreen == "profile") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            RoundedCornerShape(8.dp)
                          )
                          .clickable { viewModel.navigateTo("profile") }
                          .testTag("top_profile_btn"),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Row(
                          modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile navigation shortcut",
                            tint = if (currentScreen == "profile") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(14.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "@${currentUser?.username ?: "profilo"}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                        }
                      }
                    }
                  }
                },
                bottomBar = {
                  NavigationBar(
                    modifier = Modifier
                      .fillMaxWidth()
                      .navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp
                  ) {
                    val items = listOf(
                      Triple("feed", Icons.Default.Home, viewModel.translate("tab_feed", "Bacheca")),
                      Triple("map", Icons.Default.Radar, viewModel.translate("tab_radar", "Radar GPS")),
                      Triple("surveys", Icons.Default.Poll, viewModel.translate("tab_surveys", "Sondaggi")),
                      Triple("messages", Icons.Default.Chat, viewModel.translate("tab_chat", "Chat Criptate")),
                      Triple("shell", Icons.Default.Terminal, viewModel.translate("tab_shell", "Shell UNIX")),
                      Triple("settings", Icons.Default.Settings, viewModel.translate("tab_settings", "Config"))
                    )

                    items.forEach { (route, icon, label) ->
                      val isSelected = currentScreen == route
                      NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.navigateTo(route) },
                        icon = {
                          Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                          )
                        },
                        label = {
                          Text(
                            text = label,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                          )
                        },
                        colors = NavigationBarItemDefaults.colors(
                          indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.testTag("tab_$route")
                      )
                    }
                  }
                }
              ) { paddingValues ->
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                ) {
                  when (currentScreen) {
                    "feed" -> FeedScreen(
                      viewModel = viewModel,
                      posts = postsFeed,
                      currentUser = currentUser
                    )

                    "profile" -> ProfileScreen(
                      viewModel = viewModel,
                      currentUser = currentUser
                    )

                    "surveys" -> SurveysScreen(
                      viewModel = viewModel,
                      posts = postsFeed
                    )

                    "messages" -> EncryptedChatScreen(
                      viewModel = viewModel,
                      messages = currentChatMessages,
                      currentUser = currentUser,
                      activeReceiver = activeChatReceiver
                    )

                    "map" -> RadarScreen(
                      viewModel = viewModel,
                      currentUser = currentUser
                    )

                    "shell" -> ShellScreen(
                      viewModel = viewModel
                    )

                    "settings" -> SettingsScreen(
                      viewModel = viewModel,
                      currentUser = currentUser,
                      backupsList = backupsList
                    )
                  }
                }
              }
            }
          }

          activeNotification?.let { notification ->
            Box(
              modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
            ) {
              SimulatedPushNotificationOverlay(
                notification = notification,
                onDismiss = { viewModel.dismissActiveNotification() }
              )
            }
          }
        }
      }
    }
  }

  private fun showBiometricPrompt(onSuccess: () -> Unit, onError: (String) -> Unit) {
    val executor = ContextCompat.getMainExecutor(this)
    val biometricPrompt = BiometricPrompt(this, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Autenticazione fallita")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Accesso Terminale Clan")
        .setSubtitle("Utilizza la tua impronta per autenticare")
        .setNegativeButtonText("Annulla")
        .build()

    biometricPrompt.authenticate(promptInfo)
  }
}
