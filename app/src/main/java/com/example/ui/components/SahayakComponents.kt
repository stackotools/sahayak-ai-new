package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SahayakTopBar(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isSpeaking: Boolean,
    onStopSpeaking: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenKyc: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Emerald800),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "₹",
                        color = Amber300,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Sahayak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Emerald900
                        )
                        Text(
                            text = "AI",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Amber700
                        )
                    }
                    Text(
                        text = if (currentLanguage == AppLanguage.HINDI) "ग्रामीण वित्तीय साथी" else "Rural Financial Advisor",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }
            }
        },
        actions = {
            // TTS Speaking animation & stop button
            AnimatedVisibility(visible = isSpeaking) {
                IconButton(
                    onClick = onStopSpeaking,
                    modifier = Modifier
                        .testTag("stop_tts_button")
                        .clip(CircleShape)
                        .background(Amber100)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Stop voice reading",
                        tint = Amber800
                    )
                }
            }

            // Mock KYC Demo Tag
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onOpenKyc() }
                    .testTag("kyc_badge_button"),
                color = Amber100,
                contentColor = Amber800
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Demo KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Language Switcher Toggle
            FilledTonalButton(
                onClick = {
                    val nextLang = if (currentLanguage == AppLanguage.HINDI) AppLanguage.ENGLISH else AppLanguage.HINDI
                    onLanguageChange(nextLang)
                },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(34.dp)
                    .testTag("language_toggle_button")
            ) {
                Text(
                    text = if (currentLanguage == AppLanguage.HINDI) "EN 🌐" else "हिन्दी 🇮🇳",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onOpenProfile,
                modifier = Modifier.testTag("profile_icon_button")
            ) {
                Icon(Icons.Outlined.AccountCircle, contentDescription = "User Profile", tint = Slate700)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = PureWhite,
            scrolledContainerColor = PureWhite
        )
    )
}

/**
 * Side-by-Side Review Screen & Human-in-the-Loop Verification
 * Mandate: OCR-extracted ledger entries must always be user-confirmed & labeled before official saving!
 */
@Composable
fun OcrConfirmationDialog(
    parsedItems: List<OcrParsedItem>,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (List<OcrParsedItem>) -> Unit,
    onConfirmWithDetails: ((label: String, description: String, category: DocumentCategory, items: List<OcrParsedItem>) -> Unit)? = null
) {
    var editableItems by remember { mutableStateOf(parsedItems) }
    var khataLabel by remember { mutableStateOf("March 2026 — Grocery Shop") }
    var khataNotes by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.SUPPLIER_BILL) }

    val lowConfidenceCount = editableItems.count { it.isLowConfidence || it.confidence < 0.80f }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Emerald100),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.DocumentScanner, contentDescription = null, tint = Emerald800, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "खाता स्कैन समीक्षा (Side-by-Side Review)" else "Side-by-Side Khata Review",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Slate900
                        )
                        Text(
                            text = if (isHindi) "मूल पर्ची प्रविष्टियां जांचें व लेबल लगाएं" else "Verify extracted rows and add Khata label",
                            fontSize = 11.sp,
                            color = Slate500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Low Confidence Warning Alert
                if (lowConfidenceCount > 0) {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Filled.Warning, contentDescription = null, tint = UdhaarRed, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isHindi) "⚠️ $lowConfidenceCount प्रविष्टि में OCR विश्वास कम है। कृपया पीली पट्टी वाली रकम जांचें।"
                                else "⚠️ $lowConfidenceCount item(s) flagged for review due to low OCR confidence.",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = UdhaarRed
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Khata Label & Notes Fields
                OutlinedTextField(
                    value = khataLabel,
                    onValueChange = { khataLabel = it },
                    label = { Text(if (isHindi) "खाता का नाम/लेबल (Khata Label)" else "Khata Label (e.g. March Grocery)", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Slate900, unfocusedTextColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = khataNotes,
                    onValueChange = { khataNotes = it },
                    label = { Text(if (isHindi) "अतिरिक्त विवरण/नोट (optional)" else "Description / Notes", fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Slate900, unfocusedTextColor = Slate900),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isHindi) "डिजिटल तालिका (${editableItems.size} प्रविष्टियां):" else "Extracted Table (${editableItems.size} rows):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    TextButton(
                        onClick = {
                            editableItems = editableItems + OcrParsedItem(
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date()),
                                partyName = "New Party",
                                description = "Manual entry item",
                                amount = 100.0,
                                type = LedgerType.CREDIT,
                                category = LedgerCategory.SALES,
                                confidence = 1.0f,
                                isLowConfidence = false,
                                editedByUser = true
                            )
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(if (isHindi) "पंक्ति जोड़ें" else "Add Row", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(editableItems) { index, item ->
                        OcrItemEditCard(
                            item = item,
                            isHindi = isHindi,
                            onUpdate = { updated ->
                                editableItems = editableItems.toMutableList().also { it[index] = updated.copy(editedByUser = true) }
                            },
                            onDelete = {
                                editableItems = editableItems.toMutableList().also { it.removeAt(index) }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ocr_cancel_button")
                    ) {
                        Text(if (isHindi) "रद्द करें" else "Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            if (onConfirmWithDetails != null) {
                                onConfirmWithDetails(khataLabel, khataNotes, selectedCategory, editableItems)
                            } else {
                                onConfirm(editableItems)
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("ocr_confirm_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = PureWhite)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "सहेजें व पुष्टि करें" else "Confirm & Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OcrItemEditCard(
    item: OcrParsedItem,
    isHindi: Boolean,
    onUpdate: (OcrParsedItem) -> Unit,
    onDelete: () -> Unit
) {
    var partyName by remember { mutableStateOf(item.partyName) }
    var amountStr by remember { mutableStateOf(item.amount.toString()) }
    var isCredit by remember { mutableStateOf(item.type == LedgerType.CREDIT) }
    val isFlagged = item.isLowConfidence || item.confidence < 0.80f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = if (isFlagged) Amber50 else Slate50),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isFlagged) Amber600 else Slate200)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isFlagged) {
                        Surface(
                            color = Amber700,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "⚠️ Review OCR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    // Type selector: Jama (Credit) vs Udhaar (Debit)
                    FilterChip(
                        selected = isCredit,
                        onClick = {
                            isCredit = true
                            onUpdate(item.copy(type = LedgerType.CREDIT, category = LedgerCategory.SALES))
                        },
                        label = { Text(if (isHindi) "जमा (+)" else "Credit (+)", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JamaGreenBg,
                            selectedLabelColor = JamaGreen
                        )
                    )

                    FilterChip(
                        selected = !isCredit,
                        onClick = {
                            isCredit = false
                            onUpdate(item.copy(type = LedgerType.DEBIT, category = LedgerCategory.CUSTOMER_UDHAAR))
                        },
                        label = { Text(if (isHindi) "उधार (-)" else "Debit (-)", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = UdhaarRedBg,
                            selectedLabelColor = UdhaarRed
                        )
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete item", tint = UdhaarRed, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = partyName,
                    onValueChange = {
                        partyName = it
                        onUpdate(item.copy(partyName = it))
                    },
                    label = { Text(if (isHindi) "विवरण / ग्राहक" else "Party / Desc", fontSize = 10.sp) },
                    modifier = Modifier.weight(1.3f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = {
                        amountStr = it
                        val num = it.toDoubleOrNull() ?: item.amount
                        onUpdate(item.copy(amount = num))
                    },
                    label = { Text("₹ Amount", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(0.9f),
                    singleLine = true
                )
            }
        }
    }
}

/**
 * Add / Voice Ledger Entry Modal
 */
@Composable
fun AddLedgerEntryDialog(
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSave: (partyName: String, desc: String, amount: Double, type: LedgerType, category: LedgerCategory, phone: String?) -> Unit
) {
    var partyName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(LedgerType.CREDIT) }
    var selectedCategory by remember { mutableStateOf(LedgerCategory.SALES) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Text(
                    text = if (isHindi) "नया खाता लेन-देन दर्ज करें" else "Add Khata Transaction",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Slate900
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Type Toggle (Jama vs Udhaar)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = LedgerType.CREDIT
                                selectedCategory = LedgerCategory.SALES
                            }
                            .border(
                                width = if (selectedType == LedgerType.CREDIT) 2.dp else 1.dp,
                                color = if (selectedType == LedgerType.CREDIT) JamaGreen else Slate300,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (selectedType == LedgerType.CREDIT) JamaGreenBg else Slate50
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = JamaGreen)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "जमा (आय)" else "Jama (In)",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == LedgerType.CREDIT) JamaGreen else Slate700
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedType = LedgerType.DEBIT
                                selectedCategory = LedgerCategory.CUSTOMER_UDHAAR
                            }
                            .border(
                                width = if (selectedType == LedgerType.DEBIT) 2.dp else 1.dp,
                                color = if (selectedType == LedgerType.DEBIT) UdhaarRed else Slate300,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        color = if (selectedType == LedgerType.DEBIT) UdhaarRedBg else Slate50
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = UdhaarRed)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHindi) "उधार / खर्च" else "Udhaar / Out",
                                fontWeight = FontWeight.Bold,
                                color = if (selectedType == LedgerType.DEBIT) UdhaarRed else Slate700
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(if (isHindi) "रकम (₹ Amount)" else "Amount (₹)", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entry_amount_input"),
                    singleLine = true,
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, color = Slate700) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text(if (isHindi) "पार्टी / ग्राहक का नाम" else "Party / Customer Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("entry_party_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(if (isHindi) "विवरण (सामान/नोट)" else "Description / Items") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedType == LedgerType.DEBIT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneText,
                        onValueChange = { phoneText = it },
                        label = { Text(if (isHindi) "ग्राहक मोबाइल नंबर (WhatsApp तकादा हेतु)" else "Customer Phone (for WhatsApp reminder)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "रद्द करें" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (amount > 0 && partyName.isNotBlank()) {
                                onSave(
                                    partyName,
                                    description.ifBlank { if (selectedType == LedgerType.CREDIT) "Sales Income" else "Customer Udhaar" },
                                    amount,
                                    selectedType,
                                    selectedCategory,
                                    phoneText.ifBlank { null }
                                )
                            }
                        },
                        enabled = amountText.isNotBlank() && partyName.isNotBlank(),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("entry_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald700)
                    ) {
                        Text(if (isHindi) "दर्ज करें" else "Save Entry", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * WhatsApp Udhaar Reminder Dialog with Share Intent
 */
@Composable
fun WhatsAppReminderDialog(
    reminderText: String,
    customerName: String,
    amount: Double,
    isHindi: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = PureWhite,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF25D366).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, tint = Color(0xFF128C7E))
                    }
                    Column {
                        Text(
                            text = if (isHindi) "WhatsApp तकादा संदेश" else "WhatsApp Payment Reminder",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Slate900
                        )
                        Text(
                            text = "$customerName • ₹${String.format(Locale.ROOT, "%,.0f", amount)}",
                            fontSize = 12.sp,
                            color = UdhaarRed,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                ) {
                    Text(
                        text = reminderText,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        color = Slate800,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isHindi) "बंद करें" else "Close")
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, reminderText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Send Payment Reminder")
                            context.startActivity(shareIntent)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("share_whatsapp_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isHindi) "WhatsApp पर भेजें" else "Share Message", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
