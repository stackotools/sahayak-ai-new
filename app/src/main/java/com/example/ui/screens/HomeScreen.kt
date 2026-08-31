package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: SahayakViewModel,
    onNavigateToKhata: () -> Unit,
    onNavigateToAdvisor: () -> Unit,
    onNavigateToSchemes: () -> Unit,
    onNavigateToReport: () -> Unit,
    onOpenOcrScan: () -> Unit,
    onOpenAddEntry: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val ledgerEntries by viewModel.allLedgerEntries.collectAsState()
    val healthScore by viewModel.financialHealthScore.collectAsState()
    val mandiPrices by viewModel.mandiPrices.collectAsState()
    val reminders by viewModel.allReminders.collectAsState()

    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    val totalCredit = ledgerEntries.filter { it.type == LedgerType.CREDIT }.sumOf { it.amount }
    val totalDebit = ledgerEntries.filter { it.type == LedgerType.DEBIT }.sumOf { it.amount }
    val pendingUdhaar = ledgerEntries.filter { it.type == LedgerType.DEBIT && it.category == LedgerCategory.CUSTOMER_UDHAAR }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
    ) {
        // 1. Welcome & Business Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald800)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(userProfile.businessType.icon, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) userProfile.businessType.titleHi else userProfile.businessType.title,
                                color = Amber300,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userProfile.name,
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = userProfile.location,
                            color = Emerald200,
                            fontSize = 12.sp
                        )
                    }

                    // Quick AI Mic shortcut
                    FloatingActionButton(
                        onClick = onNavigateToAdvisor,
                        containerColor = Amber500,
                        contentColor = Slate900,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_voice_advisor_fab")
                    ) {
                        Icon(Icons.Filled.Mic, contentDescription = "Voice Advisor")
                    }
                }
            }
        }

        // 2. Financial Health Score Hero Card (The Core Differentiator)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToReport() }
                    .testTag("health_score_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.WorkspacePremium, contentDescription = null, tint = Amber600)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "वित्तीय स्वास्थ्य स्कोर (बैंक-रेडी)" else "Financial Health Score (Bank-Ready)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Slate900
                            )
                        }

                        Surface(
                            color = Emerald100,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (isHindi) healthScore.gradeHi else healthScore.grade,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald900
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${healthScore.totalScore}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                color = Emerald800
                            )
                            Text(
                                text = " / 900",
                                fontSize = 14.sp,
                                color = Slate500,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isHindi) "मुद्रा लोन अनुमान" else "Estimated Loan Fit",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                            Text(
                                text = "₹${String.format(Locale.ROOT, "%,.0f", healthScore.eligibleLoanAmountMax)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { (healthScore.totalScore - 300) / 600f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Emerald600,
                        trackColor = Slate200
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "5 ऑडिट कारकों पर आधारित" else "Based on 5 explainable factors",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                        Text(
                            text = if (isHindi) "प्रमाणपत्र देखें ›" else "View Certificate ›",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700
                        )
                    }
                }
            }
        }

        // 3. Digital Khata Cash Snapshot
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isHindi) "डिजिटल खाता सारांश" else "Digital Khata Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (isHindi) "खाता खोलें ›" else "Full Khata ›",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald700,
                            modifier = Modifier.clickable { onNavigateToKhata() }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Inflow Card (Jama)
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = JamaGreenBg
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = JamaGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "कुल जमा" else "Total Inflow", fontSize = 11.sp, color = JamaGreen, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${String.format(Locale.ROOT, "%,.0f", totalCredit)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = JamaGreen
                                )
                            }
                        }

                        // Outflow / Udhaar Card
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            color = UdhaarRedBg
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = UdhaarRed, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (isHindi) "बाकी उधार" else "Pending Udhaar", fontSize = 11.sp, color = UdhaarRed, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${String.format(Locale.ROOT, "%,.0f", pendingUdhaar)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = UdhaarRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Khata Quick Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onOpenOcrScan,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_khata_ocr_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Amber600)
                        ) {
                            Icon(Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "खाता स्कैन OCR" else "Scan Khata", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenAddEntry,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("add_khata_entry_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isHindi) "+ प्रविष्टि" else "+ Add Entry", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. Mandi Price Live Ticker
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Emerald700, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHindi) "मंडी भाव व व्यापार सलाह (Agmarknet)" else "Live Mandi Prices & Market Trends",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(mandiPrices) { item ->
                        Card(
                            modifier = Modifier.width(180.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isHindi) item.nameHi else item.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Slate900
                                    )
                                    Surface(
                                        color = if (item.trend == "UP") JamaGreenBg else if (item.trend == "DOWN") UdhaarRedBg else Slate100,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (item.trend == "UP") "▲ +${item.priceChangePercent}%" else if (item.trend == "DOWN") "▼ ${item.priceChangePercent}%" else "— Flat",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.trend == "UP") JamaGreen else if (item.trend == "DOWN") UdhaarRed else Slate600
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "₹${item.pricePerUnit} / ${item.unit}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Emerald900
                                )

                                Text(
                                    text = item.marketLocation,
                                    fontSize = 10.sp,
                                    color = Slate500
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (isHindi) item.advisoryNoteHi else item.advisoryNote,
                                    fontSize = 10.sp,
                                    color = Slate700,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Urgent Reminders & Scheme Deadlines
        if (reminders.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber50),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber300)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = Amber800, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isHindi) "महत्वपूर्ण देय तिथियां व रिमाइंडर" else "Upcoming Deadlines & Reminders",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Amber900
                                )
                            }
                            Text(
                                text = if (isHindi) "सभी देखें ›" else "View All ›",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber800,
                                modifier = Modifier.clickable { onNavigateToSchemes() }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        reminders.take(2).forEach { reminder ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = reminder.isCompleted,
                                    onCheckedChange = { viewModel.toggleReminder(reminder) },
                                    colors = CheckboxDefaults.colors(checkedColor = Emerald700)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = reminder.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (reminder.isCompleted) Slate400 else Slate900
                                    )
                                    Text(
                                        text = "Due: ${reminder.dueDate}${reminder.amount?.let { " • ₹${String.format(Locale.ROOT, "%,.0f", it)}" } ?: ""}",
                                        fontSize = 11.sp,
                                        color = Slate600
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
