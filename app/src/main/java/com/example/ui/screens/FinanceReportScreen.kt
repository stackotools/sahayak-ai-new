package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ScoreFactor
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun FinanceReportScreen(
    viewModel: SahayakViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val healthScore by viewModel.financialHealthScore.collectAsState()
    val bankReport by viewModel.bankReportSummary.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Bank Proof Certificate Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Emerald700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Emerald800),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Verified, contentDescription = null, tint = Amber300, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isHindi) "बैंक-रेडी वित्तीय साख प्रमाणपत्र" else "Bank-Ready Financial Proof",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Emerald900
                                )
                                Text(
                                    text = "SahayakAI Certified • ID: ${bankReport.reportId}",
                                    fontSize = 10.sp,
                                    color = Slate500
                                )
                            }
                        }

                        Surface(
                            color = Amber100,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Amber900,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Slate200)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Applicant Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (isHindi) "उद्यमी का नाम" else "Applicant", fontSize = 11.sp, color = Slate500)
                            Text(userProfile.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                            Text(userProfile.location, fontSize = 11.sp, color = Slate600)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(if (isHindi) "व्यापार प्रकार" else "Enterprise Type", fontSize = 11.sp, color = Slate500)
                            Text(userProfile.businessType.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                            Text(userProfile.shgName, fontSize = 11.sp, color = Slate600)
                        }
                    }
                }
            }
        }

        // 2. Score Meter & Grade
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Emerald900)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isHindi) "डिजिटल खाता आधारित वित्तीय साख स्कोर" else "Khata-Based Financial Health Score",
                        color = Emerald200,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${healthScore.totalScore}",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = Amber300
                        )
                        Text(
                            text = " / 900",
                            fontSize = 18.sp,
                            color = Emerald200,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Surface(
                        color = Amber500,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = if (isHindi) healthScore.gradeHi else healthScore.grade,
                            color = Slate900,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (isHindi) healthScore.summaryHi else healthScore.summary,
                        color = PureWhite,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 3. Verified Financial Metrics
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isHindi) "सत्यापित वित्तीय आंकड़े (बैंक ऑडिट हेतु)" else "Verified Financial Metrics (For Bank Audit)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBlock(
                            label = if (isHindi) "वार्षिक टर्नओवर" else "Annual Turnover",
                            value = "₹${String.format(Locale.ROOT, "%,.0f", bankReport.totalVerifiedTurnoverAnnual)}",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBlock(
                            label = if (isHindi) "मासिक शुद्ध बचत" else "Monthly Profit",
                            value = "₹${String.format(Locale.ROOT, "%,.0f", healthScore.netMonthlyProfit)}",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricBlock(
                            label = if (isHindi) "उधार वसूली दर" else "Udhaar Recovery",
                            value = "${String.format(Locale.ROOT, "%.0f", healthScore.udhaarRecoveryRate)}%",
                            modifier = Modifier.weight(1f)
                        )
                        MetricBlock(
                            label = if (isHindi) "रोकड़ सुरक्षा दिन" else "Cash Runway",
                            value = "${healthScore.cashRunwayDays} Days",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 4. Explainable Score Breakdown Factors
        item {
            Text(
                text = if (isHindi) "स्कोर विश्लेषण (5 पारदर्शी कारक)" else "Explainable Score Factors (5 Pillars)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        items(healthScore.factors) { factor ->
            ScoreFactorCard(factor = factor, isHindi = isHindi)
        }

        // 5. Share & Download Bank Proof Report Button
        item {
            Button(
                onClick = {
                    val shareText = """
📜 **SAHAYAKAI BANK-READY FINANCIAL PROOF CERTIFICATE**
Applicant: ${userProfile.name}
Business: ${userProfile.businessType.title} (${userProfile.location})
SHG Linkage: ${userProfile.shgName}

🏆 Financial Health Score: ${healthScore.totalScore}/900 (${healthScore.grade})
💰 Verified Annual Turnover: ₹${String.format(Locale.ROOT, "%,.0f", bankReport.totalVerifiedTurnoverAnnual)}
📊 Net Operating Margin: ${String.format(Locale.ROOT, "%.1f", bankReport.netProfitMarginPercent)}%
✅ Udhaar Recovery Index: ${String.format(Locale.ROOT, "%.0f", healthScore.udhaarRecoveryRate)}%
🏦 Recommended Scheme: PM Mudra / Stand-Up India (Up to ₹${String.format(Locale.ROOT, "%,.0f", healthScore.eligibleLoanAmountMax)})

Generated via SahayakAI (SIH 2026 Smart India Hackathon Prototype)
                    """.trimIndent()

                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    val chooser = Intent.createChooser(intent, "Share Bank Certificate")
                    context.startActivity(chooser)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("share_bank_certificate_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isHindi) "बैंक प्रमाणपत्र साझा / डाउनलोड करें" else "Share Bank Proof Certificate",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MetricBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Slate50,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(label, fontSize = 10.sp, color = Slate500)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
        }
    }
}

@Composable
fun ScoreFactorCard(
    factor: ScoreFactor,
    isHindi: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) factor.titleHi else factor.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Slate900
                )
                Surface(
                    color = if (factor.scoreOutOf100 >= 80) JamaGreenBg else Amber100,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "${factor.scoreOutOf100}/100 • ${factor.status}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (factor.scoreOutOf100 >= 80) JamaGreen else Amber800,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { factor.scoreOutOf100 / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (factor.scoreOutOf100 >= 80) Emerald600 else Amber500,
                trackColor = Slate200
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isHindi) factor.explanationHi else factor.explanation,
                fontSize = 11.sp,
                color = Slate600,
                lineHeight = 15.sp
            )
        }
    }
}
