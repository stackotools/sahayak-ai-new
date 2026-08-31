package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import kotlinx.coroutines.launch

@Composable
fun AdvisorScreen(
    viewModel: SahayakViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isSpeaking by viewModel.isTtsSpeaking.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val promptSuggestions = remember(isHindi) {
        if (isHindi) {
            listOf(
                "🏦 पीएम मुद्रा शिशु लोन ₹50,000 कैसे मिलेगा?",
                "📈 किराना दुकान का मुनाफा 15% से 25% कैसे बढ़ाएं?",
                "🍅 मंडी में टमाटर का भाव गिर रहा है, क्या रणनीति अपनाएं?",
                "📋 उधार वसूली तेजी से करने का सबसे आसान तरीका क्या है?",
                "🌾 डेयरी व पशुपालन के लिए नाबार्ड सब्सिडी योजना बताएं"
            )
        } else {
            listOf(
                "🏦 How to apply for PM Mudra Shishu loan (₹50,000)?",
                "📈 How to boost retail profit margin by 10%?",
                "🍅 Wholesale mandi prices are fluctuating, what to do?",
                "📋 Best polite strategies to recover pending customer udhaar?",
                "🌾 Government subsidies for setting up micro food/dairy enterprise"
            )
        }
    }

    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // 1. Context Injection Banner (Demonstrating personal AI pipeline)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Emerald50,
            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Emerald800, modifier = Modifier.size(16.dp))
                Column {
                    Text(
                        text = if (isHindi) "व्यक्तिगत ग्रामीण AI सलाहकार" else "Personalized Rural Business AI Advisor",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald900
                    )
                    Text(
                        text = "${userProfile.businessType.title} • ${userProfile.location.substringBefore(",")} • Khata Tunover: ₹${userProfile.monthlyTurnover.toInt()}",
                        fontSize = 10.sp,
                        color = Slate600
                    )
                }
            }
        }

        // 2. Chat Messages Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Welcome Assistant Message if empty
            if (chatMessages.isEmpty()) {
                item {
                    AssistantWelcomeCard(isHindi = isHindi, userProfile = userProfile)
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                ChatMessageBubble(
                    message = message,
                    isHindi = isHindi,
                    onSpeak = {
                        viewModel.speakText(message.text, if (isHindi) "hi" else "en")
                    }
                )
            }

            if (isAiThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Emerald700
                        )
                        Text(
                            text = if (isHindi) "सहायक एआई सलाह तैयार कर रहा है..." else "SahayakAI is analyzing your profile & ledger...",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }
            }
        }

        // 3. Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(promptSuggestions) { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Slate100,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            inputText = prompt
                            viewModel.sendChatMessage(prompt, autoSpeak = true)
                            inputText = ""
                        }
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = Slate800,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 4. Input Bar with TTS & Voice input
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PureWhite,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (isHindi) "व्यापार या योजना का सवाल पूछें..." else "Ask business or scheme query...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("advisor_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = PureWhite
                    )
                )

                // Voice Mic trigger
                IconButton(
                    onClick = {
                        val sampleVoicePrompt = if (isHindi) "प्रधानमंत्री मुद्रा लोन में कितनी सब्सिडी और गारंटी मिलती है?" else "How do I expand my retail inventory using Mudra loan?"
                        inputText = sampleVoicePrompt
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Amber100)
                        .testTag("advisor_mic_button")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = "Voice Input", tint = Amber800)
                }

                // Send Button
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textToSend = inputText
                            inputText = ""
                            viewModel.sendChatMessage(textToSend, autoSpeak = true)
                        }
                    },
                    enabled = inputText.isNotBlank() && !isAiThinking,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Emerald700 else Slate300)
                        .testTag("advisor_send_button")
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
fun AssistantWelcomeCard(
    isHindi: Boolean,
    userProfile: com.example.data.model.UserProfile
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Emerald800),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Psychology, contentDescription = null, tint = Amber300, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "नमस्ते ${userProfile.name} जी! 🙏" else "Welcome ${userProfile.name}! 🙏",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Emerald900
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isHindi)
                    "मैं आपका 'सहायक एआई' सलाहकार हूँ। मैं आपकी दुकान (${userProfile.businessType.titleHi}) के बहीखाते, सरकारी योजनाओं और स्थानीय मंडी भावों का विश्लेषण करके सही व्यावसायिक सलाह देता हूँ।\n\n🔊 किसी भी उत्तर को सुनने के लिए संदेश के ऊपर दिए गए 'स्पीकर' आइकन को दबाएं।"
                else
                    "I am your 'SahayakAI' rural advisor. I analyze your digitized Khata records, government subsidy schemes, and local mandi prices to give actionable business growth guidance.\n\n🔊 Tap the speaker icon on any message to listen via Voice Narration (TTS).",
                fontSize = 12.sp,
                color = Slate700,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isHindi: Boolean,
    onSpeak: () -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Emerald700),
                contentAlignment = Alignment.Center
            ) {
                Text("₹", color = Amber300, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) Emerald800 else PureWhite,
            tonalElevation = 2.dp,
            border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, Slate200) else null
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "SahayakAI",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                            if (message.isOfflineTier) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    color = Amber100,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "Offline KB",
                                        fontSize = 8.sp,
                                        color = Amber800,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        // Voice Speaker Icon for Low Literacy Users
                        IconButton(
                            onClick = onSpeak,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = Emerald700,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) PureWhite else Slate900,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
