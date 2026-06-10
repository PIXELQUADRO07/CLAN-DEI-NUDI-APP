package com.example.CDN.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.CDN.db.PostEntity
import com.example.CDN.ui.components.CyberButton
import com.example.CDN.ui.components.CyberCard
import com.example.CDN.ui.theme.CyberGray
import com.example.CDN.ui.theme.CyberWhite
import com.example.CDN.ui.theme.CyberYellow
import com.example.CDN.viewmodel.MainViewModel

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
