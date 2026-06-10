package com.example.CDN.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.CDN.R
import com.example.CDN.db.BackupEntity
import com.example.CDN.db.MessageEntity
import com.example.CDN.db.PostEntity
import com.example.CDN.db.UserEntity
import com.example.CDN.security.SecurityUtils
import com.example.CDN.ui.components.*
import com.example.CDN.ui.theme.*
import com.example.CDN.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

// --- FEED / INSTAGRAM STREAM SCREEN ---
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

// --- SURVEYS / SONDAGGI SCREEN (DEDICATED PANEL) ---
@Composable
fun SurveysScreen(
    viewModel: MainViewModel,
    posts: List<PostEntity>
) {
    val surveyPosts = posts.filter { it.isPoll }
    var surveyQuestion by remember { mutableStateOf("") }
    var optionText1 by remember { mutableStateOf("") }
    var optionText2 by remember { mutableStateOf("") }
    var createMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            CyberCard(
                borderColor = CyberYellow,
                titleSpec = "CREA NUOVO SONDAGGIO DI CLAN",
                terminalBadge = "SURVEY_CREATOR_MODULE"
            ) {
                OutlinedTextField(
                    value = surveyQuestion,
                    onValueChange = { surveyQuestion = it },
                    modifier = Modifier.fillMaxWidth().testTag("survey_question_input"),
                    label = { Text("Domanda sondaggio decisa...", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = optionText1,
                    onValueChange = { optionText1 = it },
                    modifier = Modifier.fillMaxWidth().testTag("survey_option1"),
                    label = { Text("Opzione Risposta A", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = optionText2,
                    onValueChange = { optionText2 = it },
                    modifier = Modifier.fillMaxWidth().testTag("survey_option2"),
                    label = { Text("Opzione Risposta B", fontFamily = FontFamily.Monospace, color = CyberWhite.copy(alpha = 0.5f)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberYellow, unfocusedBorderColor = CyberGray, focusedTextColor = CyberWhite)
                )

                Spacer(modifier = Modifier.height(14.dp))

                CyberButton(
                    text = "Pubblica Sondaggio Criptato",
                    onClick = {
                        if (surveyQuestion.isNotBlank() && optionText1.isNotBlank() && optionText2.isNotBlank()) {
                            viewModel.createNewPost(
                                text = "Lanciato sondaggio del clan: ${surveyQuestion.take(30)}...",
                                isPoll = true,
                                pollQuestion = surveyQuestion,
                                pollOptionsList = listOf(optionText1, optionText2)
                            )
                            surveyQuestion = ""
                            optionText1 = ""
                            optionText2 = ""
                            createMessage = "Sondaggio registrato con successo!"
                        } else {
                            createMessage = "Compilare tutti i nodi obbligatori!"
                        }
                    },
                    color = CyberYellow,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_survey_btn"
                )

                if (createMessage.isNotEmpty()) {
                    Text(
                        text = createMessage.uppercase(),
                        color = CyberYellow,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "INDICI E SONDAGGI ATTIVI NEL RETICOLO",
                color = CyberWhite,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (surveyPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NESSUN SONDAGGIO DA MOSTRARE ONLINE",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = CyberWhite.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            items(surveyPosts) { post ->
                CyberCard(
                    borderColor = CyberYellow,
                    titleSpec = "SONDAGGIO DA: @${post.author}",
                    terminalBadge = "NUCLEO_SURVEY"
                ) {
                    Text(
                        text = post.pollQuestion.uppercase(),
                        color = CyberWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 12.dp)
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
                            }.padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "[  ] $option",
                                    color = CyberWhite.copy(alpha = 0.9f),
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
                            Spacer(modifier = Modifier.height(4.dp))
                            // Glowing dynamic progress bar on OLED Black
                            Box(
                                modifier = Modifier.fillMaxWidth().height(6.dp).background(CyberGray, RoundedCornerShape(3.dp))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth(optVotes.toFloat() / totalVotes.toFloat()).fillMaxHeight().background(CyberYellow, RoundedCornerShape(3.dp))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "VOTAZIONI TOTALI: $totalVotes CHIAVI",
                            color = CyberWhite.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "TOCCA UNA STRINGA PER VOTARE",
                            color = CyberYellow.copy(alpha = 0.6f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// --- ENCRYPTED MESSAGES DIRECT PANEL ---
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
                        val decrypted = com.example.CDN.security.SecurityUtils.decryptAES(message.encryptedBody, cryptoKeyInput)

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

// --- RADAR GPS MAPS SCREEN ---
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
                titleSpec = "MASTHEAD TELEMETRIA RADAR",
                terminalBadge = "RADAR_NODE_ACTIVE"
            ) {
                CyberRadarMap(
                    membersList = members,
                    onMemberSelected = { selectedUsername ->
                        viewModel.changeActiveChatReceiver(selectedUsername)
                        viewModel.navigateTo("messages")
                    }
                )
            }
        }

        item {
            Text(
                text = "UTENTI INDIVIDUATI NELLE VICINANZE (DENTRO CLUSTER)",
                color = CyberWhite,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        items(members) { member ->
            val distressActive = member.distressLevel > 50

            CyberCard(
                borderColor = if (distressActive) CyberYellow else CyberRed,
                titleSpec = "@${member.username}",
                terminalBadge = member.activeStatus
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

// --- PROFILE SECTION ---
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

// --- SETTINGS, CHECKS, SYSTEM BACKUPS AND ACC DELETION PANEL ---
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
    var selfDestructPassVerify by remember { mutableStateOf("") }

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
