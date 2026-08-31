package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.*
import com.example.ui.viewmodel.ProfitSplitMember
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun CalculatorsScreen(
    viewModel: SahayakViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = if (isHindi) listOf("विकास सिमुलेटर", "SHG मुनाफा बंटवारा", "ब्रेक-ईवेन गणना") else listOf("Growth Sim", "SHG Profit Split", "Break-Even")

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

        when (selectedTab) {
            0 -> GrowthSimulatorTab(viewModel = viewModel, isHindi = isHindi)
            1 -> ShgProfitSplitTab(viewModel = viewModel, isHindi = isHindi)
            2 -> BreakEvenTab(viewModel = viewModel, isHindi = isHindi)
        }
    }
}

@Composable
fun GrowthSimulatorTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val simResult by viewModel.growthSimResult.collectAsState()

    var investmentText by remember { mutableStateOf("15000") }
    var extraSalesText by remember { mutableStateOf("450") }
    var marginText by remember { mutableStateOf("25") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isHindi) "व्यापार विस्तार व ऋण आरओआई सिमुलेटर" else "Business Expansion & Loan ROI Simulator",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Slate900
            )
            Text(
                text = if (isHindi) "नए स्टॉक/मशीन में निवेश करने पर संभावित अतिरिक्त मुनाफा और वसूली समय जानें" else "Simulate new stock/tool investment return & payback period",
                fontSize = 11.sp,
                color = Slate600
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = investmentText,
                        onValueChange = {
                            investmentText = it
                            val inv = it.toDoubleOrNull() ?: 0.0
                            val sales = extraSalesText.toDoubleOrNull() ?: 0.0
                            val margin = marginText.toDoubleOrNull() ?: 20.0
                            viewModel.runGrowthSimulation(inv, sales, margin)
                        },
                        label = { Text(if (isHindi) "नया निवेश (₹ इन्वेस्टमन्ट)" else "Planned Investment (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = extraSalesText,
                        onValueChange = {
                            extraSalesText = it
                            val inv = investmentText.toDoubleOrNull() ?: 0.0
                            val sales = it.toDoubleOrNull() ?: 0.0
                            val margin = marginText.toDoubleOrNull() ?: 20.0
                            viewModel.runGrowthSimulation(inv, sales, margin)
                        },
                        label = { Text(if (isHindi) "अपेक्षित अतिरिक्त दैनिक बिक्री (₹)" else "Expected Extra Daily Sales (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = marginText,
                        onValueChange = {
                            marginText = it
                            val inv = investmentText.toDoubleOrNull() ?: 0.0
                            val sales = extraSalesText.toDoubleOrNull() ?: 0.0
                            val margin = it.toDoubleOrNull() ?: 20.0
                            viewModel.runGrowthSimulation(inv, sales, margin)
                        },
                        label = { Text(if (isHindi) "मुनाफा मार्जिन (%)" else "Net Margin (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = { Text("%", fontWeight = FontWeight.Bold, color = Slate700) }
                    )
                }
            }
        }

        simResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Emerald800)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) "सिमुलेशन परिणाम" else "Simulation Results",
                                color = Amber300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Surface(
                                color = Amber500,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${String.format(Locale.ROOT, "%.0f", res.roiPercent)}% ROI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Slate900,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald900
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isHindi) "मासिक अतिरिक्त मुनाफा" else "Monthly Profit Added", fontSize = 10.sp, color = Emerald200)
                                    Text("₹${String.format(Locale.ROOT, "%,.0f", res.monthlyNetProfitAdded)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Emerald900
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(if (isHindi) "निवेश वसूली समय" else "Payback Period", fontSize = 10.sp, color = Emerald200)
                                    Text("${String.format(Locale.ROOT, "%.1f", res.paybackMonths)} Months", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Amber300)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isHindi) res.adviceNotesHi else res.adviceNotes,
                            fontSize = 12.sp,
                            color = PureWhite,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShgProfitSplitTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val profitSplitResult by viewModel.profitSplitResult.collectAsState()

    var revenueText by remember { mutableStateOf("48000") }
    var expensesText by remember { mutableStateOf("26000") }
    var reservePercentText by remember { mutableStateOf("10") }

    val members = remember {
        mutableStateListOf(
            ProfitSplitMember("1", "Geeta Devi", "Lead Artisan / Cutter", 40.0),
            ProfitSplitMember("2", "Sunita Sharma", "Stitching & Finishing", 35.0),
            ProfitSplitMember("3", "Pooja Verma", "Sales & Packaging", 25.0)
        )
    }

    LaunchedEffect(Unit) {
        viewModel.runProfitSplit(48000.0, 26000.0, 10.0, members)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isHindi) "महिला स्वयं सहायता समूह (SHG) पारदर्शी लाभ बंटवारा" else "SHG & Joint Enterprise Transparent Profit Split",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900
            )
            Text(
                text = if (isHindi) "ग्रुप रिजर्व फंड अलग करके सदस्यों में मेहनत के अनुसार पारदर्शी वितरण" else "Distribute earnings transparently after setting aside SHG reserve fund",
                fontSize = 11.sp,
                color = Slate600
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = revenueText,
                            onValueChange = {
                                revenueText = it
                                val rev = it.toDoubleOrNull() ?: 0.0
                                val exp = expensesText.toDoubleOrNull() ?: 0.0
                                val res = reservePercentText.toDoubleOrNull() ?: 10.0
                                viewModel.runProfitSplit(rev, exp, res, members)
                            },
                            label = { Text(if (isHindi) "कुल बिक्री (₹)" else "Gross Revenue (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = expensesText,
                            onValueChange = {
                                expensesText = it
                                val rev = revenueText.toDoubleOrNull() ?: 0.0
                                val exp = it.toDoubleOrNull() ?: 0.0
                                val res = reservePercentText.toDoubleOrNull() ?: 10.0
                                viewModel.runProfitSplit(rev, exp, res, members)
                            },
                            label = { Text(if (isHindi) "कुल लागत (₹)" else "Total Cost (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = reservePercentText,
                        onValueChange = {
                            reservePercentText = it
                            val rev = revenueText.toDoubleOrNull() ?: 0.0
                            val exp = expensesText.toDoubleOrNull() ?: 0.0
                            val res = it.toDoubleOrNull() ?: 10.0
                            viewModel.runProfitSplit(rev, exp, res, members)
                        },
                        label = { Text(if (isHindi) "समूह आपातकालीन बचत फंड (%)" else "SHG Emergency Reserve (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        profitSplitResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald300)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if (isHindi) "वितरण योग्य शुद्ध लाभ" else "Distributable Profit", fontSize = 11.sp, color = Slate500)
                                Text("₹${String.format(Locale.ROOT, "%,.0f", res.netDistributableProfit)}", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Emerald800)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(if (isHindi) "समूह बचत फंड (10%)" else "Group Reserve Fund", fontSize = 11.sp, color = Slate500)
                                Text("₹${String.format(Locale.ROOT, "%,.0f", res.reserveFundAmount)}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Amber700)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Slate200)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(if (isHindi) "सदस्यों का व्यक्तिगत हिस्सा:" else "Individual Member Payouts:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate800)

                        Spacer(modifier = Modifier.height(6.dp))

                        res.memberPayouts.forEach { member ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(member.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Slate900)
                                    Text("${member.role} • ${member.contributionPercent.toInt()}% Share", fontSize = 10.sp, color = Slate500)
                                }
                                Text(
                                    text = "₹${String.format(Locale.ROOT, "%,.0f", member.payoutAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = JamaGreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BreakEvenTab(
    viewModel: SahayakViewModel,
    isHindi: Boolean
) {
    val breakEvenResult by viewModel.breakEvenResult.collectAsState()

    var fixedCostText by remember { mutableStateOf("4500") }
    var priceText by remember { mutableStateOf("50") }
    var varCostText by remember { mutableStateOf("32") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text(
                text = if (isHindi) "ब्रेक-ईवेन बिक्री कैलकुलेटर (दैनिक लक्ष्य)" else "Break-Even Sales Target Calculator",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Slate900
            )
            Text(
                text = if (isHindi) "दुकान का किराया व बिजली खर्च निकालने हेतु प्रतिदिन कितनी बिक्री आवश्यक है" else "Calculate minimum daily units needed to cover fixed overheads",
                fontSize = 11.sp,
                color = Slate600
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = fixedCostText,
                        onValueChange = {
                            fixedCostText = it
                            val f = it.toDoubleOrNull() ?: 0.0
                            val p = priceText.toDoubleOrNull() ?: 0.0
                            val v = varCostText.toDoubleOrNull() ?: 0.0
                            viewModel.runBreakEven(f, p, v)
                        },
                        label = { Text(if (isHindi) "मासिक स्थायी खर्च (किराया, बिजली, लोन EMI)" else "Monthly Fixed Costs (Rent, Electricity, EMI)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = {
                                priceText = it
                                val f = fixedCostText.toDoubleOrNull() ?: 0.0
                                val p = it.toDoubleOrNull() ?: 0.0
                                val v = varCostText.toDoubleOrNull() ?: 0.0
                                viewModel.runBreakEven(f, p, v)
                            },
                            label = { Text(if (isHindi) "प्रति पीस विक्रय मूल्य" else "Selling Price / Unit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                        )

                        OutlinedTextField(
                            value = varCostText,
                            onValueChange = {
                                varCostText = it
                                val f = fixedCostText.toDoubleOrNull() ?: 0.0
                                val p = priceText.toDoubleOrNull() ?: 0.0
                                val v = it.toDoubleOrNull() ?: 0.0
                                viewModel.runBreakEven(f, p, v)
                            },
                            label = { Text(if (isHindi) "प्रति पीस लागत" else "Variable Cost / Unit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                        )
                    }
                }
            }
        }

        breakEvenResult?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isHindi) "दैनिक न्यूनतम बिक्री लक्ष्य" else "Daily Minimum Sales Target",
                                color = Amber300,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${res.unitsNeededDaily} Units / Day",
                                color = PureWhite,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(if (isHindi) "मासिक ब्रेक-ईवेन राजस्व:" else "Monthly Break-Even Sales:", color = Slate300, fontSize = 12.sp)
                            Text("₹${String.format(Locale.ROOT, "%,.0f", res.salesRevenueNeededMonthly)}", color = Amber300, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isHindi) res.adviceSummaryHi else res.adviceSummary,
                            fontSize = 11.sp,
                            color = Slate300,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
