package com.example.CDN.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.R
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.RadarMember
import com.example.CDN.viewmodel.SimulatedNotification
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

// --- CUSTOM CYBER BUTTON ---
@Composable
fun CyberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CyberRed,
    isSecondary: Boolean = false,
    icon: @Composable (() -> Unit)? = null,
    testTag: String = ""
) {
    val shape = RoundedCornerShape(12.dp)
    val finalTag = testTag.ifEmpty { text.lowercase().replace(" ", "_") + "_btn" }

    Surface(
        onClick = onClick,
        modifier = modifier
            .testTag(finalTag)
            .height(50.dp)
            .clip(shape)
            .border(
                1.dp,
                if (isSecondary) CyberMutedRed.copy(alpha = 0.5f) else Color.Transparent,
                shape
            ),
        color = if (isSecondary) Color.Transparent else color,
        contentColor = if (isSecondary) CyberNeonRed else Color.Black
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text.uppercase(),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
        }
    }
}

// --- CYBERPUNK DECORATIVE CARD ---
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    titleSpec: String = "",
    terminalBadge: String = "SYS_SECURE",
    borderColor: Color = CyberRed,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = CyberCrimsonBorder,
                shape = shape
            )
            .background(
                color = CyberMutedRed.copy(alpha = 0.08f),
                shape = shape
            )
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (titleSpec.isNotEmpty() || terminalBadge.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (titleSpec.isNotEmpty()) {
                        Text(
                            text = titleSpec.uppercase(),
                            color = CyberWhite,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    if (terminalBadge.isNotEmpty()) {
                        Surface(
                            color = borderColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "[$terminalBadge]",
                                color = CyberNeonRed,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                HorizontalDivider(
                    color = CyberCrimsonBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            content()
        }
    }
}

// --- DYNAMIC STARTUP LOADING ANIMATION SCREEN ---
@Composable
fun GlitchLoader(
    onFinished: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var currentLogLine by remember { mutableStateOf("INIZIALIZZAZIONE RETICOLO CRITTOGRAFATO...") }

    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scannerLine"
    )

    LaunchedEffect(Unit) {
        val logLines = listOf(
            "ACCESSO KERNEL CLAN_DEI_NUDI_APP V2...",
            "DECRITTAZIONE ARCHIVIO LOCALE SQLite...",
            "CONNESSIONE AI LIVE NODES FIREBASE COMPLETATA...",
            "SINCRONIZZAZIONE DATI DISPOSITIVI DI NOTIFICA...",
            "IMPULSI SCAN RADAR MEMBRI NELLE VICINANZE: ATTIVI.",
            "SISTEMA PRONTO • ACCESSO SOCIALE SICURO AUTORIZZATO."
        )
        // Increment progress step-by-step to simulate true hacker loading feel
        for (i in 1..100) {
            kotlinx.coroutines.delay(25)
            progress = i / 100f
            if (i == 20) currentLogLine = logLines[0]
            if (i == 40) currentLogLine = logLines[1]
            if (i == 60) currentLogLine = logLines[2]
            if (i == 75) currentLogLine = logLines[3]
            if (i == 90) currentLogLine = logLines[4]
            if (i == 98) currentLogLine = logLines[5]
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        // Hacker Hologram scanner effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            val yPos = scanY * size.height
            drawLine(
                color = CyberRed.copy(alpha = 0.35f),
                start = Offset(0f, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 4f
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Clan Logo in Loading Animation
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, CyberRed, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clan_logo),
                    contentDescription = "Clan Logo Animation",
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CLAN DEI NUDI APP",
                style = MaterialTheme.typography.headlineLarge,
                color = CyberWhite,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = "TATTICO • CRITTOGRAFATO • DECENTRATO",
                style = MaterialTheme.typography.labelLarge,
                color = CyberRed,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 36.dp)
            )

            // Dynamic progress numerical loader
            Text(
                text = "CARICAMENTO FIRMWARE: ${(progress * 100).toInt()}%",
                color = CyberYellow,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Linear Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .border(1.dp, CyberRed, RoundedCornerShape(3.dp))
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(CyberRed, RoundedCornerShape(3.dp))
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic security logs stream
            Text(
                text = currentLogLine.uppercase(),
                color = CyberWhite.copy(alpha = 0.65f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                modifier = Modifier.height(30.dp)
            )
        }
    }
}

// --- INTERACTIVE RADAR MEMBERS MAP ---
@Composable
fun CyberRadarMap(
    membersList: List<RadarMember>,
    onMemberSelected: (String) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_transition")
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "SCANSIONE IMPULSI GPS VICINANZE",
            color = CyberYellow,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Radar Sphere
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(CyberBlack, shape = RoundedCornerShape(140.dp))
                .border(1.dp, CyberRed.copy(alpha = 0.7f), shape = RoundedCornerShape(140.dp))
                .clip(shape = RoundedCornerShape(140.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width / 2

                // Concentric Range rings
                drawCircle(color = CyberRed.copy(alpha = 0.15f), radius = maxRadius * 0.35f, center = center, style = Stroke(width = 1f))
                drawCircle(color = CyberRed.copy(alpha = 0.15f), radius = maxRadius * 0.70f, center = center, style = Stroke(width = 1f))
                drawCircle(color = CyberRed.copy(alpha = 0.25f), radius = maxRadius, center = center, style = Stroke(width = 1.5f))

                // Compass Crosshairs lines
                drawLine(color = CyberRed.copy(alpha = 0.2f), start = Offset(0f, center.y), end = Offset(size.width, center.y))
                drawLine(color = CyberRed.copy(alpha = 0.2f), start = Offset(center.x, 0f), end = Offset(center.x, size.height))

                // Sonar Laser line sweeps
                rotate(degrees = radarRotation, pivot = center) {
                    val brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            CyberRed.copy(alpha = 0.25f),
                            CyberRed.copy(alpha = 0.75f)
                        ),
                        center = center
                    )
                    drawCircle(brush = brush, radius = maxRadius, center = center)
                    drawLine(
                        color = CyberRed,
                        start = center,
                        end = Offset(center.x, 0f),
                        strokeWidth = 2f
                    )
                }
            }

            // Overlay targets on the sonar
            membersList.forEach { member ->
                // Calculate coordinate placements based on bearing & distance (scaled)
                val angleRad = Math.toRadians((member.bearingDegrees - 90f).toDouble())
                // Max radar display representing 4000 meters
                val distanceRatio = (member.distanceMeters.toFloat() / 4000f).coerceAtMost(0.95f)
                val pixelDistance = 140.dp * distanceRatio

                Box(
                    modifier = Modifier
                        .offset(
                            x = pixelDistance * cos(angleRad).toFloat(),
                            y = pixelDistance * sin(angleRad).toFloat()
                        )
                        .size(34.dp)
                        .background(CyberBlack, RoundedCornerShape(17.dp))
                        .border(
                            1.dp,
                            if (member.distressLevel > 50) CyberYellow else CyberRed,
                            RoundedCornerShape(17.dp)
                        )
                        .clickable { onMemberSelected(member.username) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (member.distressLevel > 50) Icons.Default.Warning else Icons.Default.Person,
                        contentDescription = member.username,
                        tint = if (member.distressLevel > 50) CyberYellow else CyberNeonRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Primary Logged user center marker
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(CyberWhite, RoundedCornerShape(7.dp))
                    .border(2.dp, CyberRed, RoundedCornerShape(7.dp))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radar target legends
        Text(
            text = "RILEVATE ${membersList.size} CHIAVI GPS ATTIIVE • SELEZIONA PER CONTATTO",
            color = CyberWhite.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp
        )
    }
}

// --- SIMULATED GOOGLE CLOUD MESSAGING PUSH ALERTS ---
@Composable
fun SimulatedPushNotificationOverlay(
    notification: SimulatedNotification,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .border(1.dp, CyberMutedRed, RoundedCornerShape(16.dp))
            .clickable { onDismiss() },
        colors = CardDefaults.cardColors(containerColor = CyberDarkGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Push FCM Indicator",
                        tint = CyberYellow,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NOTIFICA PUSH DI CLAN",
                        color = CyberYellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = CyberRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = notification.type,
                        color = CyberRed,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = notification.title.uppercase(),
                color = CyberWhite,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = notification.body,
                color = CyberWhite.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "TOCCA PER CONGEDARE",
                    color = CyberWhite.copy(alpha = 0.4f),
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- BIOMETRIC SECURITY ACCESS LOCK OVERLAY ---
@Composable
fun BiometricLockOverlay(
    isScanning: Boolean,
    errorMessage: String,
    onTriggerScan: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_line")
    val scannerGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack.copy(alpha = 0.96f))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, CyberRed, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.clan_logo),
                    contentDescription = "Clan Logo Lock",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SCHERMO DI BLOCCO BIOMETRICO",
                color = CyberWhite,
                fontSize = 15.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Text(
                text = "SISTEMA INTEGRATO DI PROTEZIONE CLAN DEI NUDI APP",
                color = CyberWhite.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Scanner circular pad
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .border(2.dp, if (isScanning) CyberNeonRed else CyberRed, RoundedCornerShape(65.dp))
                    .background(CyberDarkGray, RoundedCornerShape(65.dp))
                    .clickable { onTriggerScan() },
                contentAlignment = Alignment.Center
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(110.dp),
                        color = CyberYellow,
                        strokeWidth = 3.dp
                    )
                    Text(
                        text = "LETTURA\nRETINA...",
                        color = CyberYellow,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Trigger Scanner pad key",
                        tint = CyberRed.copy(alpha = scannerGlow),
                        modifier = Modifier.size(74.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "INSERIRE IMPRONTA PER AUTENTICARE",
                color = CyberWhite,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp,
                fontWeight = FontWeight.Bold
            )

            if (errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = CyberYellow,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}
