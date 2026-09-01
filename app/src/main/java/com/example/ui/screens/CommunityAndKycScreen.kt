package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel

/**
 * Community Section — full-screen module with its own internal 4-tab bottom nav.
 * Main app's Home top bar & bottom nav are hidden while this section is active.
 */
@Composable
fun CommunityAndKycScreen(
    viewModel: SahayakViewModel,
    onBack: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Slate50,
        topBar = {
            // Internal top bar: back arrow | title | notification
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
                Text(
                    text = if (selectedTab == 0) "Community" else if (selectedTab == 1) "Chats" else if (selectedTab == 2) "Advisor" else "Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Slate900,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { }) {
                    Icon(Icons.Outlined.NotificationsNone, contentDescription = "Notifications", tint = Slate600)
                }
            }
            HorizontalDivider(color = Slate200)
        },
        bottomBar = {
            // Internal bottom nav: Community | Chats | Advisor | Profile
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 8.dp
            ) {
                listOf(
                    Triple(0, if (selectedTab == 0) Icons.Filled.Groups else Icons.Outlined.Groups, if (isHindi) "समुदाय" else "Community"),
                    Triple(1, if (selectedTab == 1) Icons.Filled.Forum else Icons.Outlined.Forum, if (isHindi) "चैट" else "Chats"),
                    Triple(2, if (selectedTab == 2) Icons.Filled.SmartToy else Icons.Outlined.SmartToy, if (isHindi) "सलाहकार" else "Advisor"),
                    Triple(3, if (selectedTab == 3) Icons.Filled.Person else Icons.Outlined.Person, if (isHindi) "प्रोफ़ाइल" else "Profile")
                ).forEach { (index, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = {
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Emerald900,
                            selectedTextColor = Emerald900,
                            indicatorColor = Emerald100,
                            unselectedIconColor = Slate700,
                            unselectedTextColor = Slate700
                        ),
                        modifier = Modifier.testTag("community_tab_$index")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> CommunityFeedTab(viewModel = viewModel, isHindi = isHindi)
                1 -> ChatsScreen(viewModel = viewModel)
                2 -> AdvisorScreen(viewModel = viewModel)
                3 -> ProfileScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CommunityFeedTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val posts by viewModel.allPosts.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showNewPostDialog by remember { mutableStateOf(false) }

    val filters = listOf("All", "Success", "Question", "Update", "Tip")
    val filterLabels = if (isHindi) listOf("सभी", "सफलता", "प्रश्न", "अपडेट", "टिप") else filters

    val filteredPosts = if (selectedFilter == "All") posts else posts.filter {
        it.postType.name == selectedFilter.uppercase()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Filter chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Slate50)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters.indices.toList()) { index ->
                val isSelected = selectedFilter == filters[index]
                val bgColor by animateColorAsState(if (isSelected) Emerald800 else Slate100, label = "chip_bg")
                val textColor by animateColorAsState(if (isSelected) PureWhite else Slate700, label = "chip_text")
                Surface(
                    onClick = { selectedFilter = filters[index] },
                    shape = RoundedCornerShape(20.dp),
                    color = bgColor
                ) {
                    Text(
                        text = filterLabels[index],
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
            }
        }

        // Feed
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(filteredPosts, key = { it.id }) { post ->
                CommunityPostCard(
                    post = post,
                    onLike = { viewModel.togglePostLike(post) },
                    onPlayVoice = { viewModel.speakText(post.content, if (isHindi) "hi" else "en") }
                )
            }

            if (filteredPosts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Article, contentDescription = null, tint = Slate400, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(if (isHindi) "इस श्रेणी में कोई पोस्ट नहीं" else "No posts in this category", color = Slate500, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    // Floating Action Button
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showNewPostDialog = true },
            containerColor = Emerald800,
            contentColor = PureWhite,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(end = 16.dp, bottom = 88.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create Post", modifier = Modifier.size(28.dp))
        }
    }

    // New Post Dialog
    if (showNewPostDialog) {
        var postContent by remember { mutableStateOf("") }
        var postTag by remember { mutableStateOf("#BusinessTip") }
        var selectedType by remember { mutableStateOf(PostType.BUSINESS_TIP) }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showNewPostDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = PureWhite
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(if (isHindi) "नया पोस्ट बनाएं" else "Create Post", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Slate900)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        placeholder = { Text(if (isHindi) "अपना अनुभव साझा करें..." else "Share your experience...") },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = postTag,
                        onValueChange = { postTag = it },
                        label = { Text("Tag") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showNewPostDialog = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (postContent.isNotBlank()) {
                                    viewModel.addCommunityPost(postContent, postTag, selectedType)
                                    showNewPostDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald800)
                        ) { Text("Post") }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    onLike: () -> Unit,
    onPlayVoice: () -> Unit
) {
    val accentColor = when (post.postType) {
        PostType.SUCCESS -> Color(0xFF1B5E20)
        PostType.QUESTION -> Color(0xFF884200)
        PostType.SCHEME_UPDATE -> Color(0xFF005DB7)
        PostType.BUSINESS_TIP -> Emerald800
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(accentColor.copy(alpha = 0.8f))
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(accentColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(post.authorName.take(1), color = accentColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Column {
                            Text(post.authorName, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Slate900)
                            Text(post.authorRole, fontSize = 12.sp, color = Slate500)
                        }
                    }
                    Surface(
                        color = accentColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = post.postType.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = post.content,
                    fontSize = 14.sp,
                    color = Slate800,
                    lineHeight = 20.sp
                )

                if (post.imageUrl != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AsyncImage(
                        model = post.imageUrl,
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (post.voiceNoteSeconds != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPlayVoice() },
                        color = Emerald50
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Emerald800, modifier = Modifier.size(18.dp))
                            Text("Voice Note (${post.voiceNoteSeconds}s) • Tap to listen", fontSize = 12.sp, color = Emerald900, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate100)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(onClick = onLike, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (post.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (post.isLikedByUser) UdhaarRed else Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text("${post.likesCount}", fontSize = 13.sp, color = Slate600, modifier = Modifier.align(Alignment.CenterVertically))

                        Spacer(modifier = Modifier.width(16.dp))

                        Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.commentsCount}", fontSize = 13.sp, color = Slate600)
                    }

                    IconButton(onClick = { }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Share, contentDescription = "Share", tint = Slate400, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}