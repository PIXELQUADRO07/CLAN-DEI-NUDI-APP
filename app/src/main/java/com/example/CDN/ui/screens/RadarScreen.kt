package com.example.CDN.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.db.UserEntity
import com.example.CDN.ui.components.CyberButton
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel

@Composable
fun RadarScreen(
    viewModel: MainViewModel,
    currentUser: UserEntity?
) {
    val members = viewModel.radarMembers

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard(
                borderColor = CyberRed,
                titleSpec = "MAPPA RADAR NODI OPERATIVI",
                terminalBadge = "GPS_TRACKER_ACTIVE"
            ) {
                Text(
                    text = "SCANSIONE RETICOLO LOCALE IN CORSO...\nMEMBRI RILEVATI: ${members.size}",
                    color = CyberWhite.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        items(members) { member ->
            val distressActive = member.distressLevel > 70
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (distressActive) CyberYellow else CyberGray,
                        RoundedCornerShape(12.dp)
                    ),
                color = if (distressActive) CyberYellow.copy(alpha = 0.05f) else CyberDarkGray,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1.5f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (member.activeStatus == "OPERATIVO") Color.Green else CyberYellow,
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = member.username.uppercase(),
                                color = CyberWhite,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "STATUS: ${member.activeStatus}",
                            color = if (member.activeStatus == "INCURSIONE") CyberRed else CyberWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = member.bio,
                                color = CyberWhite.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "DISTANZA: ${member.distanceMeters} mt",
                                    color = if (distressActive) CyberYellow else CyberNeonRed,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "PULSO: ${member.distressLevel}%",
                                    color = CyberWhite.copy(alpha = 0.5f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    CyberButton(
                        text = "Cifra Chat",
                        onClick = {
                            viewModel.changeActiveChatReceiver(member.username)
                            viewModel.navigateTo("messages")
                        },
                        modifier = Modifier.weight(0.8f).height(40.dp),
                        color = if (distressActive) CyberYellow else CyberRed,
                        testTag = "chat_with_${member.username}_btn"
                    )
                }
            }
        }
    }
}
