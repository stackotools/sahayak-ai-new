package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.data.model.ChatConversation
import com.example.data.model.ChatThreadMessage
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel

@Composable
fun ChatsScreen(
    viewModel: SahayakViewModel
) {
    val conversations by viewModel.allConversations.collectAsState()
    val selectedChat by viewModel.selectedChatConversation.collectAsState()

    if (selectedChat != null) {
        ChatThreadScreen(
            viewModel = viewModel,
            conversation = selectedChat!!,
            onBack = { viewModel.closeChatConversation() }
        )
    } else {
        ChatListScreen(
            conversations = conversations,
            onOpenChat = { viewModel.openChatConversation(it) }
        )
    }
}

@Composable
fun ChatListScreen(
    conversations: List<ChatConversation>,
    onOpenChat: (ChatConversation) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val filters = listOf("All", "Unread", "Groups")

    val filtered = conversations.filter { conv ->
        val matchesQuery = searchQuery.isBlank() ||
            conv.name.contains(searchQuery, ignoreCase = true) ||
            conv.lastMessage.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Unread" -> conv.unreadCount > 0
            "Groups" -> conv.isGroup
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search conversations...", fontSize = 14.sp, color = Slate500) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Slate400) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = PureWhite,
                unfocusedContainerColor = PureWhite
            )
        )

        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    onClick = { selectedFilter = filter },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Emerald800 else Slate100
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PureWhite else Slate700,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (filtered.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Forum, contentDescription = null, tint = Slate300, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No chats found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate700)
                    Text("Start a conversation with fellow entrepreneurs!", fontSize = 13.sp, color = Slate500)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(filtered, key = { it.id }) { conv ->
                    ChatListItem(conversation = conv, onClick = { onOpenChat(conv) })
                }
            }
        }
    }
}

@Composable
fun ChatListItem(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    val hasUnread = conversation.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (conversation.isGroup) Emerald100 else Color(0xFF005DB7).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = conversation.initial,
                color = if (conversation.isGroup) Emerald800 else Color(0xFF005DB7),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // Name + last message
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.name,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = Slate900,
                    maxLines = 1
                )
                Text(
                    text = conversation.time,
                    fontSize = 11.sp,
                    color = if (hasUnread) Emerald800 else Slate400
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = conversation.lastMessage,
                    fontSize = 13.sp,
                    color = if (hasUnread) Slate700 else Slate500,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (hasUnread) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Emerald800),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${conversation.unreadCount}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatThreadScreen(
    viewModel: SahayakViewModel,
    conversation: ChatConversation,
    onBack: () -> Unit
) {
    val messages by viewModel.threadMessages.collectAsState()
    val threadMessages = messages.filter { it.conversationId == conversation.id }
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }

    LaunchedEffect(threadMessages.size) {
        if (threadMessages.isNotEmpty()) {
            listState.animateScrollToItem(threadMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PureWhite)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate800)
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (conversation.isGroup) Emerald100 else Color(0xFF005DB7).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(conversation.initial, color = if (conversation.isGroup) Emerald800 else Color(0xFF005DB7), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(conversation.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Slate900)
                Text(if (conversation.isGroup) "Group • Online" else "Online", fontSize = 11.sp, color = Emerald700)
            }
        }

        HorizontalDivider(color = Slate200)

        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(threadMessages, key = { it.id }) { msg ->
                ThreadMessageBubble(message = msg)
            }
        }

        // Input bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = PureWhite,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a message...", fontSize = 14.sp, color = Slate500) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_thread_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = PureWhite
                    )
                )
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendThreadMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) Emerald800 else Slate300)
                        .testTag("chat_thread_send")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = PureWhite)
                }
            }
        }
    }
}

@Composable
fun ThreadMessageBubble(
    message: ChatThreadMessage
) {
    val isMine = message.isMine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMine) 16.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 16.dp
                ),
                color = if (isMine) Emerald800 else PureWhite,
                tonalElevation = if (isMine) 0.dp else 1.dp,
                border = if (!isMine) androidx.compose.foundation.BorderStroke(1.dp, Slate200) else null
            ) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = if (isMine) PureWhite else Slate900,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
            Text(
                text = message.time,
                fontSize = 10.sp,
                color = Slate400,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}