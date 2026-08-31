package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.data.ml.CommodityMlForecast
import com.example.data.ml.PriceTrendDirection
import com.example.data.model.AppLanguage
import com.example.data.model.LedgerCategory
import com.example.data.model.LedgerType
import com.example.ui.theme.*
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

@Composable
fun MlForecastingScreen(
    viewModel: SahayakViewModel,
    onNavigateToAdvisorWithQuery: (String) -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isHi = userProfile.preferredLanguage == AppLanguage.HINDI
    val allForecasts = viewModel.allCommodityForecasts
    val selectedCommodity by viewModel.selectedForecastCommodity.collectAsState()
    val summary = viewModel.mlPredictionSummary

    var showKhataConfirmationSnackbar by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Emerald900),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PureWhite.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🤖", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isHi) "एआई मांग व मंडी भाव भविष्यवाणी" else "AI Demand & Price Forecasting",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PureWhite
                                )
                            )
                            Text(
                                text = "Multi-Factor ML Time-Series Model (94% Accuracy)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Emerald200
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PureWhite.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isHi)
                                "त्योहारी मांग से पहले थोक खरीद करने पर ₹${String.format(Locale.ROOT, "%,.0f", summary.totalProjectedSavings)} तक की बचत संभव!"
                            else
                                "Potential ₹${String.format(Locale.ROOT, "%,.0f", summary.totalProjectedSavings)} savings via proactive bulk Mandi procurement!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = PureWhite,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }

        // Horizontal Commodity Selector
        Text(
            text = if (isHi) "वस्तु / जिंस चुनें (Select Commodity):" else "Select Commodity for ML Forecast:",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allForecasts) { item ->
                val isSelected = item.id == selectedCommodity.id
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectForecastCommodity(item) }
                        .testTag("forecast_chip_${item.id}"),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Emerald800 else PureWhite,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Emerald700 else Slate300
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.trendDirection.icon,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = if (isHi) item.nameHi.substringBefore("(") else item.nameEn.substringBefore("("),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) PureWhite else Slate900
                                )
                            )
                            Text(
                                text = "₹${item.currentSpotPrice.toInt()}/${item.unit}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Emerald200 else Slate700,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Main ML Forecast Deep Dive Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ml_forecast_deep_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header with name and trend badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHi) selectedCommodity.nameHi else selectedCommodity.nameEn,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = selectedCommodity.category,
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }

                    // Trend Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (selectedCommodity.trendDirection) {
                            PriceTrendDirection.BULLISH_SURGE -> UdhaarRedBg
                            PriceTrendDirection.BEARISH_DROP -> JamaGreenBg
                            PriceTrendDirection.STABLE -> Amber100
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (selectedCommodity.trendDirection) {
                                PriceTrendDirection.BULLISH_SURGE -> UdhaarRed
                                PriceTrendDirection.BEARISH_DROP -> JamaGreen
                                PriceTrendDirection.STABLE -> Amber700
                            }
                        )
                    ) {
                        Text(
                            text = if (isHi) selectedCommodity.trendDirection.labelHi else selectedCommodity.trendDirection.label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = when (selectedCommodity.trendDirection) {
                                    PriceTrendDirection.BULLISH_SURGE -> UdhaarRed
                                    PriceTrendDirection.BEARISH_DROP -> JamaGreen
                                    PriceTrendDirection.STABLE -> Amber900
                                }
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Forecast Matrix (Current vs 7d vs 15d vs 30d)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current Spot Price
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isHi) "वर्तमान भाव" else "Spot Price",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "₹${String.format(Locale.ROOT, "%.1f", selectedCommodity.currentSpotPrice)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "प्रति ${selectedCommodity.unit}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate600, fontSize = 10.sp)
                            )
                        }
                    }

                    // 7-Day Forecast
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate300)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isHi) "7 दिन बाद" else "7-Day ML",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "₹${String.format(Locale.ROOT, "%.1f", selectedCommodity.forecast7Days)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "${if (selectedCommodity.forecast7Days >= selectedCommodity.currentSpotPrice) "+" else ""}${String.format(Locale.ROOT, "%.1f", ((selectedCommodity.forecast7Days - selectedCommodity.currentSpotPrice)/selectedCommodity.currentSpotPrice)*100)}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selectedCommodity.forecast7Days >= selectedCommodity.currentSpotPrice) UdhaarRed else JamaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    // 30-Day Forecast
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCommodity.priceChangePercent30d > 0) Amber50 else JamaGreenBg,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedCommodity.priceChangePercent30d > 0) Amber600 else JamaGreen
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isHi) "30 दिन बाद" else "30-Day ML",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selectedCommodity.priceChangePercent30d > 0) Amber900 else JamaGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "₹${String.format(Locale.ROOT, "%.1f", selectedCommodity.forecast30Days)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCommodity.priceChangePercent30d > 0) Amber900 else JamaGreen
                                )
                            )
                            Text(
                                text = "${if (selectedCommodity.priceChangePercent30d >= 0) "+" else ""}${String.format(Locale.ROOT, "%.1f", selectedCommodity.priceChangePercent30d)}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (selectedCommodity.priceChangePercent30d >= 0) UdhaarRed else JamaGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recommended Strategic Action
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Emerald50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Emerald300),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("🎯", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isHi) "एआई खरीद रणनीति (Actionable ML Strategy):" else "Recommended Procurement Strategy:",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald900
                                )
                            )
                            Text(
                                text = if (isHi) selectedCommodity.procurementActionHi else selectedCommodity.procurementAction,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate900,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Seasonal Insight
                Text(
                    text = "🌾 ${if (isHi) "मौसमी व बाजार कारण:" else "Market Driver:"} ${selectedCommodity.seasonalFactorDescription}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 12-Month Historical Price Visualizer
                Text(
                    text = if (isHi) "12-महीने का ऐतिहासिक भाव चार्ट (Mandi Price History):" else "12-Month Historical Mandi Price Trend:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val maxHistoricalPrice = selectedCommodity.historical12Months.maxOfOrNull { it.price } ?: 100.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Slate50, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    selectedCommodity.historical12Months.forEach { pt ->
                        val heightFraction = ((pt.price / maxHistoricalPrice) * 0.75f).toFloat().coerceIn(0.15f, 1f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${pt.price.toInt()}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = Slate700)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .width(16.dp)
                                    .fillMaxHeight(heightFraction)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(if (pt.monthName == "Aug") Emerald700 else Slate400)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = pt.monthName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = if (pt.monthName == "Aug") FontWeight.Bold else FontWeight.Normal,
                                    color = if (pt.monthName == "Aug") Emerald900 else Slate700
                                )
                            )
                        }
                    }
                }
            }
        }

        // Inventory Demand Prediction & Safety Buffer Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (isHi) "📦 मांग भविष्यवाणी व स्टॉक सिफारिश" else "📦 Demand Forecast & Reorder Plan",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = if (isHi) "दुकान/व्यापार की बिक्री के आधार पर आवश्यक स्टॉक" else "EOQ based inventory safety optimization",
                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHi) "30-दिन अनुमानित मांग" else "30-Day Demand",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "${selectedCommodity.predictedDemand30dUnits} ${selectedCommodity.unit}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHi) "अनुशंसित ऑर्डर मात्रा" else "Reorder Quantity",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "${selectedCommodity.recommendedReorderQty} ${selectedCommodity.unit}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald800
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHi) "सुरक्षा स्टॉक बफर" else "Safety Buffer Days",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "${selectedCommodity.safetyStockDays} ${if (isHi) "दिन" else "Days"}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isHi) "आवश्यक कार्यशील पूंजी" else "Required Capital",
                                style = MaterialTheme.typography.labelSmall.copy(color = Slate700)
                            )
                            Text(
                                text = "₹${String.format(Locale.ROOT, "%,.0f", selectedCommodity.estimatedWorkingCapital)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Ask Gemini Button
                    Button(
                        onClick = {
                            val query = if (isHi) {
                                "मंडी में ${selectedCommodity.nameHi} का भाव अभी ₹${selectedCommodity.currentSpotPrice} है और 30 दिन में ₹${selectedCommodity.forecast30Days} होने का अनुमान है। मुझे कब और कितना माल थोक में खरीदना चाहिए?"
                            } else {
                                "Mandi forecast shows ${selectedCommodity.nameEn} price moving from ₹${selectedCommodity.currentSpotPrice} to ₹${selectedCommodity.forecast30Days}. What inventory procurement strategy should I execute?"
                            }
                            onNavigateToAdvisorWithQuery(query)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("ask_gemini_ml_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald800,
                            contentColor = PureWhite
                        )
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHi) "एआई से सलाह लें" else "Ask Gemini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Add to Khata Button
                    OutlinedButton(
                        onClick = {
                            viewModel.addManualLedgerEntry(
                                partyName = "${selectedCommodity.nameEn} Wholesale Stock",
                                description = "Procurement of ${selectedCommodity.recommendedReorderQty} ${selectedCommodity.unit} based on ML Demand Forecast",
                                amount = selectedCommodity.estimatedWorkingCapital,
                                type = LedgerType.DEBIT,
                                category = LedgerCategory.INVENTORY_BUY
                            )
                            showKhataConfirmationSnackbar = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("add_ml_khata_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Slate900
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate400)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Emerald800, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isHi) "खाता में जोड़ें" else "Save to Khata",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Slate900
                        )
                    }
                }

                if (showKhataConfirmationSnackbar) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isHi) "✅ स्टॉक खरीद का विवरण खाता में सफलतापूर्वक दर्ज कर दिया गया!" else "✅ Stock purchase successfully recorded in your Khata ledger!",
                        color = JamaGreen,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
