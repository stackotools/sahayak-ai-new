package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.ui.components.AddLedgerEntryDialog
import com.example.ui.components.WhatsAppReminderDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.LedgerFilter
import com.example.ui.viewmodel.SahayakViewModel
import java.util.Locale

enum class KhataViewMode {
    LEDGER_ENTRIES,
    DOCUMENT_LOCKER
}

@Composable
fun KhataScreen(
    viewModel: SahayakViewModel,
    onOpenOcrScan: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val ledgerEntries by viewModel.allLedgerEntries.collectAsState()
    val digitalDocs by viewModel.allDigitalDocuments.collectAsState()
    val selectedFilter by viewModel.selectedLedgerFilter.collectAsState()
    val isHindi = userProfile.preferredLanguage == AppLanguage.HINDI

    var activeViewMode by remember { mutableStateOf(KhataViewMode.LEDGER_ENTRIES) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedWhatsAppReminderEntry by remember { mutableStateOf<LedgerEntry?>(null) }
    var showCameraScanSuccessDialog by remember { mutableStateOf(false) }
    var lastScannedDocTitle by remember { mutableStateOf("") }
    var selectedDocDetail by remember { mutableStateOf<DigitalDocument?>(null) }

    // Camera image capture launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        val docTitle = "March 2026 — Camera Khata Scan"
        lastScannedDocTitle = docTitle
        val simulatedExtractedText = """
            2026-08-31 Ramesh Kumar ration aata dal Rs 1450 udhaar
            2026-08-31 Daily counter cash sales ₹3280 jama
            2026-08-31 Mustard oil tins wholesale ₹2600 kharch
        """.trimIndent()
        viewModel.startOcrScan(simulatedExtractedText, imageUri = "camera_captured_photo.jpg", source = "camera")
    }

    val filteredEntries = when (selectedFilter) {
        LedgerFilter.ALL -> ledgerEntries
        LedgerFilter.JAMA_CREDIT -> ledgerEntries.filter { it.type == LedgerType.CREDIT }
        LedgerFilter.UDHAAR_DEBIT -> ledgerEntries.filter { it.type == LedgerType.DEBIT }
        LedgerFilter.PENDING_OCR -> ledgerEntries.filter { !it.isConfirmed }
    }

    val totalCredit = ledgerEntries.filter { it.type == LedgerType.CREDIT }.sumOf { it.amount }
    val totalDebit = ledgerEntries.filter { it.type == LedgerType.DEBIT }.sumOf { it.amount }
    val netBalance = totalCredit - totalDebit

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Emerald800,
                contentColor = PureWhite,
                shape = CircleShape,
                modifier = Modifier.testTag("fab_add_khata_entry")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // 1. Digital Cash Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PureWhite),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isHindi) "कुल शुद्ध रोकड़ (नेट बैलेंस)" else "Net Cash In Hand Balance",
                            fontSize = 12.sp,
                            color = Slate700,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${String.format(Locale.ROOT, "%,.0f", netBalance)}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = if (netBalance >= 0) Emerald900 else UdhaarRed
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = JamaGreenBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = null, tint = JamaGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(if (isHindi) "कुल जमा" else "Total Jama", fontSize = 11.sp, color = Slate900, fontWeight = FontWeight.SemiBold)
                                        Text("₹${String.format(Locale.ROOT, "%,.0f", totalCredit)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = JamaGreen)
                                    }
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = UdhaarRedBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = null, tint = UdhaarRed, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(if (isHindi) "कुल उधार/खर्च" else "Total Udhaar", fontSize = 11.sp, color = Slate900, fontWeight = FontWeight.SemiBold)
                                        Text("₹${String.format(Locale.ROOT, "%,.0f", totalDebit)}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = UdhaarRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Camera Scan to Khata & Document Vault Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Amber100),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber600)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Amber700),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = PureWhite, modifier = Modifier.size(22.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "हस्तलिखित खाता स्कैन व डिजिटल लॉकर" else "Scan Handwritten Khata & Save to Vault",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Amber900
                                )
                                Text(
                                    text = if (isHindi) "कागजी पर्ची का फोटो लें - OCR निष्कर्षण, साइड-बाय-साइड समीक्षा व लेबल सुरक्षित रहेगा" else "Snap photo: Layout-preserving OCR with side-by-side review & custom Khata labels",
                                    fontSize = 11.sp,
                                    color = Slate900
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { cameraLauncher.launch(null) },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .testTag("camera_scan_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = PureWhite)
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isHindi) "कैमरा खोलें" else "Camera Snap", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onOpenOcrScan,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("ocr_text_paste_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Slate400)
                            ) {
                                Icon(Icons.Filled.TextFields, contentDescription = null, tint = Slate800, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isHindi) "टेक्स्ट/CSV स्कैन" else "OCR / CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
            }

            // 3. Section Switcher Tabs: [Ledger Entries] vs [Scanned Khatas & Vault]
            item {
                TabRow(
                    selectedTabIndex = activeViewMode.ordinal,
                    containerColor = PureWhite,
                    contentColor = Emerald800,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeViewMode == KhataViewMode.LEDGER_ENTRIES,
                        onClick = { activeViewMode = KhataViewMode.LEDGER_ENTRIES },
                        text = {
                            Text(
                                text = if (isHindi) "📋 बहीखाता (${ledgerEntries.size})" else "📋 Ledger (${ledgerEntries.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (activeViewMode == KhataViewMode.LEDGER_ENTRIES) Emerald900 else Slate700
                            )
                        }
                    )
                    Tab(
                        selected = activeViewMode == KhataViewMode.DOCUMENT_LOCKER,
                        onClick = { activeViewMode = KhataViewMode.DOCUMENT_LOCKER },
                        text = {
                            Text(
                                text = if (isHindi) "🗄️ स्कैन खाता लॉकर (${digitalDocs.size})" else "🗄️ Scanned Khatas (${digitalDocs.size})",
                                fontWeight = FontWeight.Bold,
                                color = if (activeViewMode == KhataViewMode.DOCUMENT_LOCKER) Emerald900 else Slate700
                            )
                        }
                    )
                }
            }

            if (activeViewMode == KhataViewMode.LEDGER_ENTRIES) {
                // Filter Chips
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedFilter == LedgerFilter.ALL,
                                onClick = { viewModel.setLedgerFilter(LedgerFilter.ALL) },
                                label = { Text(if (isHindi) "सभी लेन-देन (${ledgerEntries.size})" else "All Entries (${ledgerEntries.size})", fontSize = 12.sp, color = if (selectedFilter == LedgerFilter.ALL) PureWhite else Slate900) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald800,
                                    selectedLabelColor = PureWhite,
                                    containerColor = Slate100,
                                    labelColor = Slate900
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == LedgerFilter.JAMA_CREDIT,
                                onClick = { viewModel.setLedgerFilter(LedgerFilter.JAMA_CREDIT) },
                                label = { Text(if (isHindi) "जमा (आय)" else "Jama (Credit)", fontSize = 12.sp, color = if (selectedFilter == LedgerFilter.JAMA_CREDIT) PureWhite else Slate900) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JamaGreen,
                                    selectedLabelColor = PureWhite,
                                    containerColor = Slate100,
                                    labelColor = Slate900
                                )
                            )
                        }
                        item {
                            FilterChip(
                                selected = selectedFilter == LedgerFilter.UDHAAR_DEBIT,
                                onClick = { viewModel.setLedgerFilter(LedgerFilter.UDHAAR_DEBIT) },
                                label = { Text(if (isHindi) "उधार (बकाया)" else "Udhaar (Debit)", fontSize = 12.sp, color = if (selectedFilter == LedgerFilter.UDHAAR_DEBIT) PureWhite else Slate900) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = UdhaarRed,
                                    selectedLabelColor = PureWhite,
                                    containerColor = Slate100,
                                    labelColor = Slate900
                                )
                            )
                        }
                    }
                }

                // Ledger Transaction List
                if (filteredEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.ReceiptLong, contentDescription = null, tint = Slate400, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isHindi) "कोई लेन-देन नहीं मिला" else "No transactions found",
                                    fontSize = 14.sp,
                                    color = Slate900,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) "नया लेन-देन जोड़ने के लिए '+' बटन दबाएं" else "Tap '+' button to record daily sales or customer credit",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                } else {
                    items(filteredEntries, key = { it.id }) { entry ->
                        val commodityForecast = viewModel.getCommodityForecastForDescription(entry.description)
                        LedgerEntryCard(
                            entry = entry,
                            commodityForecast = commodityForecast,
                            isHindi = isHindi,
                            onSendWhatsApp = { selectedWhatsAppReminderEntry = entry },
                            onDelete = { viewModel.deleteLedgerEntry(entry.id) }
                        )
                    }
                }
            } else {
                // DIGITAL DOCUMENT / SCANNED KHATA VAULT VIEW
                item {
                    Text(
                        text = if (isHindi) "स्कैन किए गए खाता दस्तावेज़ व डिजिटल पर्चियां:" else "Scanned Khatas & Preserved Digital Ledgers:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                }

                if (digitalDocs.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Slate400, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isHindi) "लॉकर में कोई स्कैन खाता नहीं है" else "No scanned Khatas saved yet",
                                    fontSize = 14.sp,
                                    color = Slate900,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isHindi) "कैमरा से पर्ची स्कैन करें और यहाँ सहेजें" else "Snap photo of bills & receipts to store digitally",
                                    fontSize = 12.sp,
                                    color = Slate700
                                )
                            }
                        }
                    }
                } else {
                    items(digitalDocs, key = { it.id }) { doc ->
                        DigitalDocCard(
                            doc = doc,
                            isHindi = isHindi,
                            onViewDetail = { selectedDocDetail = doc },
                            onDelete = { viewModel.deleteDigitalDocument(doc.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Entry Modal
    if (showAddDialog) {
        AddLedgerEntryDialog(
            isHindi = isHindi,
            onDismiss = { showAddDialog = false },
            onSave = { party, desc, amount, type, category, phone ->
                viewModel.addManualLedgerEntry(party, desc, amount, type, category, phone)
                showAddDialog = false
            }
        )
    }

    // WhatsApp Reminder Share Dialog
    selectedWhatsAppReminderEntry?.let { entry ->
        WhatsAppReminderDialog(
            reminderText = viewModel.generateWhatsAppReminderText(entry, userProfile.preferredLanguage),
            customerName = entry.partyName,
            amount = entry.amount,
            isHindi = isHindi,
            onDismiss = { selectedWhatsAppReminderEntry = null }
        )
    }

    // Document Detail / Scanned Khata View Dialog
    selectedDocDetail?.let { doc ->
        val linkedEntries by viewModel.getEntriesForKhata(doc.id).collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { selectedDocDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = doc.category.icon, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (doc.label.isNotBlank()) doc.label else doc.title,
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (doc.description.isNotBlank()) {
                            Text(text = doc.description, fontSize = 11.sp, color = Slate700)
                        }
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📅 ${doc.dateAdded}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate800, fontWeight = FontWeight.SemiBold)
                        )
                        Surface(
                            color = Amber100,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Source: ${doc.source.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Mini P&L
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Slate100,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isHindi) "कुल जमा" else "Jama (+)", fontSize = 10.sp, color = JamaGreen, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.ROOT, "%,.0f", doc.totalCredit)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = JamaGreen)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isHindi) "कुल उधार" else "Udhaar (-)", fontSize = 10.sp, color = UdhaarRed, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.ROOT, "%,.0f", doc.totalDebit)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = UdhaarRed)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isHindi) "नेट योग" else "Total Amount", fontSize = 10.sp, color = Slate900, fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.ROOT, "%,.0f", doc.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Slate900)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Slate200)

                    Text(
                        text = if (isHindi) "डिजिटाइज़्ड प्रविष्टियां (${linkedEntries.size}):" else "Digitized Entries (${linkedEntries.size}):",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Slate900)
                    )

                    if (linkedEntries.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Slate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = doc.extractedText.ifBlank { "Original OCR Text Preserved." },
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate900),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(linkedEntries) { e ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (e.type == LedgerType.CREDIT) JamaGreenBg else UdhaarRedBg)
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = e.partyName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                        Text(text = "${e.date} • ${e.description}", fontSize = 10.sp, color = Slate700)
                                    }
                                    Text(
                                        text = "${if (e.type == LedgerType.CREDIT) "+" else "-"} ₹${String.format(Locale.ROOT, "%,.0f", e.amount)}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (e.type == LedgerType.CREDIT) JamaGreen else UdhaarRed
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedDocDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald800, contentColor = PureWhite)
                ) {
                    Text("बंद करें (Close)", color = PureWhite)
                }
            }
        )
    }
}

@Composable
fun DigitalDocCard(
    doc: DigitalDocument,
    isHindi: Boolean,
    onViewDetail: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Emerald100),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = doc.category.icon, fontSize = 20.sp)
                }

                Column {
                    Text(
                        text = if (doc.label.isNotBlank()) doc.label else doc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Slate900
                    )
                    Text(
                        text = if (doc.description.isNotBlank()) doc.description else (if (isHindi) doc.category.labelHi else doc.category.label),
                        fontSize = 11.sp,
                        color = Slate700
                    )
                    Text(
                        text = "📅 ${doc.dateAdded} • ${doc.parsedEntryCount} ${if (isHindi) "प्रविष्टियां" else "items"} • ${doc.source}",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.ROOT, "%,.0f", doc.totalAmount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = Slate900
                )
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun LedgerEntryCard(
    entry: LedgerEntry,
    commodityForecast: com.example.data.ml.CommodityMlForecast? = null,
    isHindi: Boolean,
    onSendWhatsApp: () -> Unit,
    onDelete: () -> Unit
) {
    val isCredit = entry.type == LedgerType.CREDIT

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (isCredit) JamaGreenBg else UdhaarRedBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCredit) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                            contentDescription = null,
                            tint = if (isCredit) JamaGreen else UdhaarRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = entry.partyName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Slate900
                        )
                        Text(
                            text = entry.description,
                            fontSize = 12.sp,
                            color = Slate700
                        )

                        // Agmarknet Commodity Forecast Link Badge
                        if (commodityForecast != null) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Surface(
                                color = Emerald100,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "${commodityForecast.trendDirection.icon} ${commodityForecast.nameEn}: ${commodityForecast.procurementActionHi}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald900
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = entry.date,
                                fontSize = 10.sp,
                                color = Slate500
                            )
                            Surface(
                                color = when (entry.source) {
                                    LedgerSource.OCR -> Amber100
                                    LedgerSource.VOICE -> Indigo100
                                    else -> Slate100
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = entry.source.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    color = Slate900
                                )
                            }
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isCredit) "+" else "-"} ₹${String.format(Locale.ROOT, "%,.0f", entry.amount)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isCredit) JamaGreen else UdhaarRed
                    )
                    Text(
                        text = if (isCredit) (if (isHindi) "जमा" else "Credit") else (if (isHindi) "उधार" else "Debit"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCredit) JamaGreen else UdhaarRed
                    )
                }
            }

            // If it's a customer udhaar entry, show the WhatsApp reminder trigger button
            if (!isCredit) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Slate100)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = onSendWhatsApp,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFDCFCE7), contentColor = Color(0xFF15803D)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "तकादा संदेश (WhatsApp)" else "Send WhatsApp Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Slate400, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
