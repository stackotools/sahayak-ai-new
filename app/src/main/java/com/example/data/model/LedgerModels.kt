package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    HINDI("hi", "Hindi", "हिन्दी"),
    ENGLISH("en", "English", "English"),
    HINGLISH("hi-en", "Hinglish", "Hinglish")
}

enum class BusinessType(val title: String, val titleHi: String, val icon: String) {
    KIRANA("Kirana / Grocery", "किराना दुकान", "🛒"),
    STREET_VENDOR("Street Vendor / Cart", "रेहड़ी-पटरी विक्रेता", "🍢"),
    DAIRY_FARMING("Dairy / Livestock", "डेयरी व पशुपालन", "🐄"),
    AGRICULTURE("Small Farmer / Mandi", "लघु किसान / मंडी", "🌾"),
    TAILORING("Tailoring / Garments", "सिलाई व परिधान", "🧵"),
    HANDICRAFTS("Artisan / Handicrafts", "हस्तशिल्प / कारीगर", "🏺"),
    FOOD_STALL("Tea Stall / Eatery", "चाय व ढाबा", "☕"),
    OTHER("Other Micro Enterprise", "अन्य सूक्ष्म उद्यम", "💼")
}

data class UserProfile(
    val id: String = "usr_demo_01",
    val name: String = "Ramesh Kumar Sharma",
    val phone: String = "+91 98765 43210",
    val businessType: BusinessType = BusinessType.KIRANA,
    val location: String = "Varanasi Rural, Uttar Pradesh",
    val monthlyTurnover: Double = 45000.0,
    val preferredLanguage: AppLanguage = AppLanguage.HINDI,
    val isKycMockLinked: Boolean = true,
    val shgName: String = "Shri Radha Mahila SHG / Gram Samiti",
    val isLoggedIn: Boolean = true,
    val authOtp: String = "749201"
)

enum class DocumentCategory(val label: String, val labelHi: String, val icon: String) {
    SUPPLIER_BILL("Supplier / Stock Invoice", "थोक पर्ची व बिल", "🧾"),
    CUSTOMER_CHIT("Customer Udhaar Chit", "ग्राहक पर्ची", "📝"),
    MANDI_RECEIPT("Mandi Sale Receipt", "मंडी पर्ची / रसीद", "🌾"),
    LOAN_DOC("Loan / KYC Document", "बैंक व ऋण दस्तावेज", "🏦"),
    EXPENSE_SLIP("Utility / Expense Slip", "किराया व खर्च पर्ची", "⚡")
}

@Entity(tableName = "digital_documents")
data class DigitalDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val label: String = "",
    val description: String = "",
    val category: DocumentCategory = DocumentCategory.SUPPLIER_BILL,
    val dateAdded: String,
    val totalAmount: Double,
    val totalCredit: Double = 0.0,
    val totalDebit: Double = 0.0,
    val imageUri: String? = null,
    val extractedText: String = "",
    val parsedEntryCount: Int = 1,
    val source: String = "camera", // "camera" | "gallery" | "import"
    val status: String = "confirmed", // "processing" | "needs_review" | "confirmed"
    val isSyncedToKhata: Boolean = true,
    val notes: String = ""
)

enum class LedgerType {
    CREDIT, // Jama / Received (Income)
    DEBIT   // Udhaar / Given (Expense/Pending Collection)
}

enum class LedgerSource {
    OCR,      // Scanned from handwritten Khata
    MANUAL,   // Manually entered
    VOICE,    // Voice dictated
    AA_MOCK   // Bank Account Aggregator
}

enum class LedgerCategory(val label: String, val labelHi: String) {
    SALES("Sales Income", "दुकान बिक्री"),
    CUSTOMER_UDHAAR("Customer Udhaar", "ग्राहक उधार"),
    INVENTORY_BUY("Stock / Goods Purchase", "सामान / स्टॉक खरीद"),
    RENT_UTILITIES("Rent & Electricity", "किराया व बिजली"),
    LABOR_WAGES("Wages & Daily Pay", "मजदूरी / दिहाड़ी"),
    SHG_SAVINGS("SHG / Chit Deposit", "समूह बचत / बीसी"),
    LOAN_EMI("Loan / Interest EMI", "कर्ज किस्त"),
    OTHER("Other Expense", "अन्य खर्च")
}

@Entity(tableName = "ledger_entries")
data class LedgerEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val khataId: Long? = null, // Links to DigitalDocument id
    val date: String, // YYYY-MM-DD
    val partyName: String, // Customer or Supplier Name
    val description: String,
    val amount: Double,
    val type: LedgerType,
    val category: LedgerCategory = LedgerCategory.SALES,
    val source: LedgerSource = LedgerSource.MANUAL,
    val isConfirmed: Boolean = true, // Human-in-the-loop review flag
    val customerPhone: String? = null,
    val rawText: String = "",
    val confidence: Float = 0.95f,
    val editedByUser: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class OcrParsedItem(
    val date: String = "",
    val partyName: String,
    val description: String,
    val amount: Double,
    val type: LedgerType,
    val category: LedgerCategory,
    val confidence: Float = 0.92f,
    val rawText: String = "",
    val isLowConfidence: Boolean = false,
    val editedByUser: Boolean = false
)
