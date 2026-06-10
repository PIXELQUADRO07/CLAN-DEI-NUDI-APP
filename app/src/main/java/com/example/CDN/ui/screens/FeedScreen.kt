package com.example.CDN.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CDN.db.PostEntity
import com.example.CDN.db.UserEntity
import com.example.CDN.ui.components.CyberButton
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FeedScreen(
    viewModel: MainViewModel,
    posts: List<PostEntity>,
    currentUser: UserEntity?
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var isFormOpen by remember { mutableStateOf(false) }
    var newPostText by remember { mutableStateOf("") }

    // State for simple Poll option addition inside creative post form
    var isPollPost by remember { mutableStateOf(false) }
    var pollQuestionText by remember { mutableStateOf("") }
    var pollOption1 by remember { mutableStateOf("") }
    var pollOption2 by remember { mutableStateOf("") }
    
    // Ghost protocol state
    var isGhostPost by remember { mutableStateOf(false) }

    val activeChannel by viewModel.activeChannel.collectAsState()
    val channels = listOf("generale", "operazioni", "intelligence", "sicurezza")

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = channels.indexOf(activeChannel).coerceAtLeast(0),
            containerColor = CyberBlack,
            contentColor = CyberRed,
            edgePadding = 8.dp,
            indicator = { tabPositions ->
                if (channels.indexOf(activeChannel) >= 0) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[channels.indexOf(activeChannel)]),
                        color = CyberRed
                    )
                }
            }
        ) {
            channels.forEach { channel ->
                val isSelected = activeChannel == channel
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.changeActiveChannel(channel) },
                    text = { 
                        Text(
                            text = "#${channel.uppercase()}", 
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) CyberRed else CyberWhite.copy(alpha = 0.5f)
                        ) 
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                // Creative Collapsible Editor Form for new post creation
                CyberCard(
                    borderColor = CyberRed,
                    titleSpec = "SCRIVI SUL RETICOLO",
                    terminalBadge = "NODE_FEED_WRITE"
                ) {
                    if (!isFormOpen) {
                        CyberButton(
                            text = "Apri Console Terminale",
                            onClick = { isFormOpen = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        OutlinedTextField(
                            value = newPostText,
                            onValueChange = { newPostText = it },
                            modifier = Modifier.fillMaxWidth().testTag("post_input_field"),
                            label = { Text("Messaggio da diffondere...", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberRed,
                                unfocusedBorderColor = CyberGray,
                                focusedTextColor = CyberWhite,
                                unfocusedTextColor = CyberWhite
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isPollPost,
                                onCheckedChange = { isPollPost = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberRed, uncheckedColor = CyberGray)
                            )
                            Text(
                                text = "ALLEGA SONDAGGIO DI CLAN",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberWhite,
                                modifier = Modifier.clickable { isPollPost = !isPollPost }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isGhostPost,
                                onCheckedChange = { isGhostPost = it },
                                colors = CheckboxDefaults.colors(checkedColor = CyberYellow, uncheckedColor = CyberGray)
                            )
                            Text(
                                text = "PROTOCOLLO GHOST (SCADE IN 1 ORA)",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = CyberYellow,
                                modifier = Modifier.clickable { isGhostPost = !isGhostPost }
                            )
                        }

                        if (isPollPost) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = pollQuestionText,
                                onValueChange = { pollQuestionText = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Domanda Sondaggio", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = pollOption1,
                                    onValueChange = { pollOption1 = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Opz 1", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray)
                                )
                                OutlinedTextField(
                                    value = pollOption2,
                                    onValueChange = { pollOption2 = it },
                                    modifier = Modifier.weight(1f),
                                    label = { Text("Opz 2", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CyberButton(
                                text = "Invia Dati",
                                onClick = {
                                    if (newPostText.isNotBlank()) {
                                        val options = if (isPollPost && pollOption1.isNotBlank() && pollOption2.isNotBlank()) {
                                            listOf(pollOption1, pollOption2)
                                        } else emptyList()

                                        viewModel.createNewPost(
                                            text = newPostText,
                                            isPoll = isPollPost && options.isNotEmpty(),
                                            pollQuestion = pollQuestionText,
                                            pollOptionsList = options,
                                            isGhost = isGhostPost
                                        )
                                        // Reset fields
                                        newPostText = ""
                                        isPollPost = false
                                        isGhostPost = false
                                        pollQuestionText = ""
                                        pollOption1 = ""
                                        pollOption2 = ""
                                        isFormOpen = false
                                    } else {
                                        Toast.makeText(context, "Il post non può essere vuoto!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                testTag = "submit_post_btn"
                            )

                            CyberButton(
                                text = "Annulla",
                                onClick = { isFormOpen = false },
                                modifier = Modifier.weight(1f),
                                isSecondary = true
                            )
                        }
                    }
                }
            }

            if (posts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "NESSUN DATO TRASMESSO SUL RETICOLO FEED",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = CyberWhite.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(posts) { post ->
                    val alreadyLiked = post.likedByUsers.split(",").contains(currentUser?.username ?: "")
                    val format = SimpleDateFormat("HH:mm:ss dd-MM-yyyy", Locale.getDefault())
                    val timestampStr = format.format(Date(post.timestamp))

                    CyberCard(
                        borderColor = if (post.isPoll) CyberYellow else if (post.isGhost) CyberRed else CyberRed,
                        titleSpec = "POST DA: @${post.author}" + if (post.isGhost) " [GHOST]" else "",
                        terminalBadge = if (post.isPoll) "SONDAGGIO_CLAN" else if (post.isGhost) "GHOST_DATA" else "FEED_DATA"
                    ) {
                        Text(
                            text = post.content,
                            color = CyberWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        // If contains active Poll options, render interactive poll
                        if (post.isPoll && post.pollQuestion.isNotEmpty() && post.pollOptions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Surface(
                                color = CyberYellow.copy(alpha = 0.05f),
                                modifier = Modifier.fillMaxWidth().border(1.dp, CyberYellow.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = post.pollQuestion.uppercase(),
                                        color = CyberYellow,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    val options = post.pollOptions.split("|")
                                    val votes = post.pollVotes.split(",").map { it.replace(" ", "").toIntOrNull() ?: 0 }
                                    val totalVotes = votes.sum().coerceAtLeast(1)

                                    options.forEachIndexed { index, option ->
                                        val optVotes = votes.getOrElse(index) { 0 }
                                        val percent = ((optVotes.toFloat() / totalVotes.toFloat()) * 100).toInt()

                                        Column(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                viewModel.submitPollVote(post.id, index)
                                            }.padding(vertical = 4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "> $option",
                                                    color = CyberWhite,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                                Text(
                                                    text = "$percent% ($optVotes)",
                                                    color = CyberYellow,
                                                    fontSize = 11.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            // Vote progress line
                                            Box(
                                                modifier = Modifier.fillMaxWidth().height(4.dp).background(CyberGray, RoundedCornerShape(2.dp))
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth(optVotes.toFloat() / totalVotes.toFloat()).fillMaxHeight().background(CyberYellow, RoundedCornerShape(2.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Post Metrics & Share interactions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TIME: $timestampStr",
                                color = CyberWhite.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Delete button if authored by current user
                                if (post.author == currentUser?.username) {
                                    IconButton(
                                        onClick = { viewModel.deletePostById(post.id) },
                                        modifier = Modifier.size(24.dp).testTag("delete_post_${post.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Rimuovi Post",
                                            tint = CyberRed.copy(alpha = 0.62f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Share / Copy Link trigger
                                IconButton(
                                    onClick = {
                                        val linkText = "CLAN_POST_SHARE://${post.id} • Dati: [${post.content.take(30)}...]"
                                        clipboard.setText(AnnotatedString(linkText))
                                        Toast.makeText(context, "Link del post copiato nel terminale di sistema!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp).testTag("share_post_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Condividi Post",
                                        tint = CyberWhite.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                // Likes Interactive Switch
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { viewModel.toggleLikePost(post.id) }.testTag("like_post_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = if (alreadyLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like Post Interaction",
                                        tint = CyberRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = post.likesCount.toString(),
                                        color = CyberWhite,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
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
