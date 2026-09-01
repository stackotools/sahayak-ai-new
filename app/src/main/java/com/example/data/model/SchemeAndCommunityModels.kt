package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SchemeCategory(val label: String, val labelHi: String) {
    CREDIT_LOAN("Subsidized Loans", "रियायती ऋण"),
    WOMEN_EMPOWERMENT("Women & SHG", "महिला व स्वयं सहायता समूह"),
    MINORITY_SC_ST("Ministry of Social Justice", "सामाजिक न्याय व अधिकारिता"),
    STREET_VENDORS("Street Vendors & Micro", "रेहड़ी-पटरी व सूक्ष्म"),
    AGRICULTURE("Agri & Allied", "कृषि व संबद्ध"),
    SKILL_EQUIPMENT("Machinery & Toolkits", "मशीनरी व टूलकिट")
}

@Entity(tableName = "schemes")
data class Scheme(
    @PrimaryKey val id: String,
    val name: String,
    val nameHi: String,
    val ministry: String,
    val description: String,
    val descriptionHi: String,
    val subsidyPercent: Int, // e.g. 25% or 35%
    val maxLoanAmount: Double, // e.g. 500000.0 (5 Lakh)
    val eligibilityCriteria: String,
    val eligibilityCriteriaHi: String,
    val documentsRequired: String,
    val category: SchemeCategory,
    val deadline: String? = null, // e.g. "Ongoing" or "2026-10-31"
    val officialUrl: String = "https://www.myscheme.gov.in",
    val applicationUrl: String = "https://www.myscheme.gov.in"
)

enum class ReminderType(val label: String, val labelHi: String) {
    SCHEME_DEADLINE("Scheme Deadline", "योजना की अंतिम तिथि"),
    EMI_REPAYMENT("Loan / Bank EMI", "बैंक किस्त भुगतान"),
    UDHAAR_COLLECTION("Customer Udhaar Followup", "उधार वसूली तकादा"),
    MANDI_TAX("Mandi / License Renewal", "मंडी व लाइसेंस नवीनीकरण"),
    SHG_MEETING("SHG Group Meeting", "समूह बैठक व बचत")
}

@Entity(tableName = "reminders")
data class BusinessReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dueDate: String, // YYYY-MM-DD
    val type: ReminderType,
    val amount: Double? = null,
    val isCompleted: Boolean = false,
    val note: String = ""
)

enum class PostType(val label: String, val labelHi: String) {
    SUCCESS("Success", "सफलता"),
    QUESTION("Question", "प्रश्न"),
    SCHEME_UPDATE("Update", "अपडेट"),
    BUSINESS_TIP("Tip", "टिप")
}

@Entity(tableName = "community_posts")
data class CommunityPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorRole: String,
    val content: String,
    val tag: String,
    val postType: PostType = PostType.BUSINESS_TIP,
    val imageUrl: String? = null,
    val likesCount: Int = 12,
    val commentsCount: Int = 3,
    val isLikedByUser: Boolean = false,
    val voiceNoteSeconds: Int? = null,
    val createdAtFormatted: String = "2 hours ago"
)

@Entity(tableName = "chat_conversations")
data class ChatConversation(
    @PrimaryKey val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val isGroup: Boolean = false,
    val initial: String = "?"
)

@Entity(tableName = "chat_thread_messages")
data class ChatThreadMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: String,
    val text: String,
    val isMine: Boolean,
    val time: String
)

data class MandiCommodity(
    val id: String,
    val name: String,
    val nameHi: String,
    val marketLocation: String,
    val pricePerUnit: Double, // in ₹ per Quintal or Kg
    val unit: String = "Quintal",
    val priceChangePercent: Double, // e.g. +4.2 or -1.8
    val trend: String = "UP", // UP, DOWN, STABLE
    val advisoryNote: String,
    val advisoryNoteHi: String
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isOfflineTier: Boolean = false,
    val suggestedActions: String? = null // Comma-separated quick actions
)
