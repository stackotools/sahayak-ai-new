package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun ProfileScreen(
    viewModel: SahayakViewModel,
    onBack: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val healthScore by viewModel.financialHealthScore.collectAsState()
    val posts by viewModel.allPosts.collectAsState()
    val myPosts = posts.filter { it.authorName == userProfile.name }
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
    ) {
        // Profile Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Emerald100),
                contentAlignment = Alignment.Center
            ) {
                Text(userProfile.name.take(1), fontWeight = FontWeight.Bold, fontSize = 36.sp, color = Emerald800)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(userProfile.name, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Slate900)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Emerald100)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(userProfile.businessType.icon, fontSize = 14.sp)
                        Text(
                            if (isHindi) userProfile.businessType.titleHi else userProfile.businessType.title,
                            fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Emerald900
                        )
                    }
                }
            }
        }

        // Financial Health Score Gauge
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Emerald800)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Financial Health", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Amber300)
                    IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Amber300, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                // Semi-circle gauge
                Box(
                    modifier = Modifier.size(200.dp, 100.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val sweepAngle = 180f
                        val startAngle = 180f
                        val progress = ((healthScore.totalScore - 300).toFloat() / 600f).coerceIn(0f, 1f)
                        val strokeWidth = 20f
                        drawArc(
                            color = Color.White.copy(alpha = 0.2f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                        )
                        drawArc(
                            color = Color(0xFF88D982),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle * progress,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            if (isHindi) healthScore.gradeHi else healthScore.grade,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PureWhite
                        )
                        Text(
                            "Score: ${healthScore.totalScore}",
                            fontSize = 13.sp, color = Amber300, fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Business Details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileDetailRow(icon = Icons.Outlined.LocationOn, label = "Location", value = userProfile.location)
                Spacer(modifier = Modifier.height(12.dp))
                ProfileDetailRow(icon = Icons.Outlined.Description, label = "Bio", value = "Specializing in ${userProfile.businessType.title}. ${if (userProfile.shgName.isNotBlank()) "Active member of ${userProfile.shgName}." else ""}")
                Spacer(modifier = Modifier.height(12.dp))
                ProfileDetailRow(icon = Icons.Outlined.TrendingUp, label = "Monthly Turnover", value = "₹${String.format(Locale.ROOT, "%,.0f", userProfile.monthlyTurnover)}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // My Posts
        if (myPosts.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Posts", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                Text("View All", fontSize = 13.sp, color = Emerald800, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(myPosts.take(2)) { post ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald100)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text(post.tag, color = Emerald900, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preferences
        Text("Preferences", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Outlined.Language, title = "Language", value = if (isHindi) "Hindi" else "English")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Slate100)
                ProfileMenuItem(icon = Icons.Outlined.NotificationsActive, title = "Notifications", value = "On")
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Slate100)
                ProfileMenuItem(icon = Icons.Outlined.Lock, title = "Privacy & Security", value = "")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Logout
        Button(
            onClick = { viewModel.logout() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UdhaarRedBg, contentColor = UdhaarRed)
        ) {
            Icon(Icons.Outlined.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Out", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Emerald50),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Emerald800, modifier = Modifier.size(20.dp))
        }
        Column {
            Text(label, fontSize = 12.sp, color = Slate500)
            Text(value, fontSize = 14.sp, color = Slate900, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuItem(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = Slate500, modifier = Modifier.size(22.dp))
            Text(title, fontSize = 15.sp, color = Slate800)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (value.isNotBlank()) {
                Text(value, fontSize = 13.sp, color = Slate500)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp))
        }
    }
}