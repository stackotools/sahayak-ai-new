package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.CommunityPost
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun CommunityAndKycScreen(
    viewModel: SahayakViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = if (isHindi) listOf("समुदाय व अनुभव साझा", "KYC व बैंक खाता सैंडबॉक्स") else listOf("Peer Community", "KYC & Bank Sandbox")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = PureWhite,
            contentColor = Emerald800
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        if (selectedTab == 0) {
            CommunityFeedTab(viewModel = viewModel, isHindi = isHindi)
        } else {
            KycSandboxTab(viewModel = viewModel, isHindi = isHindi)
        }
    }
}

@Composable
fun CommunityFeedTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val posts by viewModel.allPosts.collectAsState()
    var showNewPostDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewPostDialog = true },
                containerColor = Emerald700,
                contentColor = PureWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "Share Story")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald200)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Groups, contentDescription = null, tint = Emerald800)
                        Column {
                            Text(
                                text = if (isHindi) "ग्रामीण उद्यमी समुदाय" else "Rural Entrepreneurs Community",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Emerald900
                            )
                            Text(
                                text = if (isHindi) "साथी दुकानदारों और महिला समूहों से सीखें और सुझाव साझा करें" else "Learn from fellow micro-entrepreneurs & SHG leaders",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            }

            items(posts, key = { it.id }) { post ->
                CommunityPostCard(
                    post = post,
                    isHindi = isHindi,
                    onLike = { viewModel.togglePostLike(post) },
                    onPlayVoice = {
                        viewModel.speakText(post.content, if (isHindi) "hi" else "en")
                    }
                )
            }
        }
    }

    if (showNewPostDialog) {
        var postContent by remember { mutableStateOf("") }
        var postTag by remember { mutableStateOf("#KiranaProfit") }

        androidx.compose.ui.window.Dialog(onDismissRequest = { showNewPostDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                color = PureWhite
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(if (isHindi) "अपना अनुभव या टिप साझा करें" else "Share Your Business Tip / Experience", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = postContent,
                        onValueChange = { postContent = it },
                        placeholder = { Text(if (isHindi) "आपने दुकान या योजना में क्या नया किया..." else "Share how you boosted profit or used Mudra loan...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = postTag,
                        onValueChange = { postTag = it },
                        label = { Text("Topic Tag (e.g. #MudraSuccess, #DairyTips)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showNewPostDialog = false }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (postContent.isNotBlank()) {
                                    viewModel.addCommunityPost(postContent, postTag)
                                    showNewPostDialog = false
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Text("Post")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    isHindi: Boolean,
    onLike: () -> Unit,
    onPlayVoice: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Emerald800),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(post.authorName.take(1), color = Amber300, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column {
                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                        Text(post.authorRole, fontSize = 10.sp, color = Slate500)
                    }
                }

                Surface(color = Amber100, shape = RoundedCornerShape(6.dp)) {
                    Text(post.tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Amber900, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = post.content,
                fontSize = 12.sp,
                color = Slate800,
                lineHeight = 17.sp
            )

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
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Emerald800)
                        Text("Voice Note (${post.voiceNoteSeconds}s) • Tap to listen", fontSize = 11.sp, color = Emerald900, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Slate100)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (post.isLikedByUser) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLikedByUser) UdhaarRed else Slate400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text("${post.likesCount}", fontSize = 11.sp, color = Slate600)

                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Slate400, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.commentsCount}", fontSize = 11.sp, color = Slate600)
                }

                Text(post.createdAtFormatted, fontSize = 10.sp, color = Slate400)
            }
        }
    }
}

/**
 * Swappable Mock KYC Sandbox (Mandate: Swappable Interface + Clear Demo Labels)
 */
@Composable
fun KycSandboxTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val panDetails by viewModel.panDetails.collectAsState()
    val aadhaarDetails by viewModel.aadhaarDetails.collectAsState()
    val bankAccount by viewModel.bankAccount.collectAsState()
    val cibilReport by viewModel.cibilReport.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Amber100),
                border = androidx.compose.foundation.BorderStroke(1.dp, Amber400)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Amber800)
                    Column {
                        Text(
                            text = "Swappable KycProvider Sandbox (MockKycProvider)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Amber900
                        )
                        Text(
                            text = "All verification endpoints return structured realistic data flagged as 'Demo Data' for SIH 2026 jury evaluation.",
                            fontSize = 10.sp,
                            color = Amber800
                        )
                    }
                }
            }
        }

        // PAN Verification Card
        panDetails?.let { pan ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("1. PAN Verification (NDML / NSDL)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            DemoBadge()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("PAN: ${pan.panNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)
                        Text("Full Name: ${pan.fullName}", fontSize = 12.sp, color = Slate700)
                        Text("Status: ${pan.status} • DOB: ${pan.dateOfBirth}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }

        // Aadhaar Verification Card
        aadhaarDetails?.let { aadhaar ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("2. Aadhaar e-KYC (UIDAI Sandbox)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            DemoBadge()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Masked Aadhaar: ${aadhaar.maskedAadhaar}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)
                        Text("Name: ${aadhaar.name}", fontSize = 12.sp, color = Slate700)
                        Text("Address: ${aadhaar.address}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }

        // Bank Account Link Card
        bankAccount?.let { bank ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("3. Account Aggregator (AA) Bank Link", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            DemoBadge()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(bank.bankName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Emerald800)
                        Text("Account: ${bank.accountNumberMasked} (IFSC: ${bank.ifscCode})", fontSize = 12.sp, color = Slate700)
                        Text("Avg Monthly Balance: ₹${String.format(Locale.ROOT, "%,.0f", bank.avgMonthlyBalance)}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }

        // CIBIL Credit Bureau Card
        cibilReport?.let { cibil ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("4. Credit Bureau (CIBIL / Experian)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                            DemoBadge()
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Score: ${cibil.score}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Emerald800)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(cibil.riskCategory, fontSize = 12.sp, color = Slate600, fontWeight = FontWeight.SemiBold)
                        }
                        Text("On-Time Repayment: ${cibil.onTimeRepaymentPercent}% • Active Loans: ${cibil.activeLoans}", fontSize = 11.sp, color = Slate500)
                    }
                }
            }
        }
    }
}

@Composable
fun DemoBadge() {
    Surface(
        color = Amber200,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = "Demo Data",
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = Amber900,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
