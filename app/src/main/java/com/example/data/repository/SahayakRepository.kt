package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.service.KycProvider
import com.example.data.service.MockKycProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SahayakRepository(
    private val database: AppDatabase,
    val kycProvider: KycProvider = MockKycProvider()
) {
    private val ledgerDao = database.ledgerDao()
    private val schemeDao = database.schemeDao()
    private val reminderDao = database.reminderDao()
    private val communityDao = database.communityDao()
    private val chatDao = database.chatMessageDao()
    private val documentDao = database.documentDao()
    private val conversationDao = database.conversationDao()
    private val threadMessageDao = database.threadMessageDao()

    private val _userProfile = MutableStateFlow(UserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile

    private val _mandiPrices = MutableStateFlow(getInitialMandiPrices())
    val mandiPrices: StateFlow<List<MandiCommodity>> = _mandiPrices

    val allLedgerEntries: Flow<List<LedgerEntry>> = ledgerDao.getAllEntries()
    val allSchemes: Flow<List<Scheme>> = schemeDao.getAllSchemes()
    val allReminders: Flow<List<BusinessReminder>> = reminderDao.getAllReminders()
    val allPosts: Flow<List<CommunityPost>> = communityDao.getAllPosts()
    val chatHistory: Flow<List<ChatMessage>> = chatDao.getAllMessages()
    val allDigitalDocuments: Flow<List<DigitalDocument>> = documentDao.getAllDocuments()
    val allConversations: Flow<List<ChatConversation>> = conversationDao.getAllConversations()
    val threadMessages: Flow<List<ChatThreadMessage>> = threadMessageDao.getAllMessages()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
    }

    suspend fun addLedgerEntry(entry: LedgerEntry): Long {
        return ledgerDao.insertEntry(entry)
    }

    suspend fun addLedgerEntries(entries: List<LedgerEntry>) {
        ledgerDao.insertEntries(entries)
    }

    suspend fun updateLedgerEntry(entry: LedgerEntry) {
        ledgerDao.updateEntry(entry)
    }

    suspend fun deleteLedgerEntry(id: Long) {
        ledgerDao.deleteById(id)
    }

    suspend fun addReminder(reminder: BusinessReminder): Long {
        return reminderDao.insertReminder(reminder)
    }

    suspend fun toggleReminderComplete(id: Long, isCompleted: Boolean) {
        reminderDao.setCompleted(id, isCompleted)
    }

    suspend fun deleteReminder(reminder: BusinessReminder) {
        reminderDao.deleteReminder(reminder)
    }

    suspend fun addCommunityPost(post: CommunityPost): Long {
        return communityDao.insertPost(post)
    }

    suspend fun togglePostLike(id: Long, currentlyLiked: Boolean, currentCount: Int) {
        val newLiked = !currentlyLiked
        val newCount = if (newLiked) currentCount + 1 else (currentCount - 1).coerceAtLeast(0)
        communityDao.updateLike(id, newLiked, newCount)
    }

    suspend fun saveChatMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    suspend fun sendThreadMessage(message: ChatThreadMessage) {
        threadMessageDao.insertMessages(listOf(message))
    }

    suspend fun clearChat() {
        chatDao.clearHistory()
    }

    suspend fun addDigitalDocument(doc: DigitalDocument): Long {
        return documentDao.insertDocument(doc)
    }

    suspend fun deleteDigitalDocument(id: Long) {
        documentDao.deleteById(id)
    }

    suspend fun saveScannedKhataSession(
        label: String,
        description: String,
        category: DocumentCategory,
        imageUri: String?,
        source: String,
        items: List<OcrParsedItem>
    ): Long {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val today = sdf.format(Date())

        val totalCredit = items.filter { it.type == LedgerType.CREDIT }.sumOf { it.amount }
        val totalDebit = items.filter { it.type == LedgerType.DEBIT }.sumOf { it.amount }
        val totalAmount = totalCredit + totalDebit

        val doc = DigitalDocument(
            title = if (label.isNotBlank()) label else "Khata Scan #${(100..999).random()}",
            label = label.ifBlank { "Khata Scan" },
            description = description,
            category = category,
            dateAdded = today,
            totalAmount = totalAmount,
            totalCredit = totalCredit,
            totalDebit = totalDebit,
            imageUri = imageUri,
            extractedText = items.joinToString("\n") { "${it.date} ${it.partyName} - ₹${it.amount} (${it.type.name})" },
            parsedEntryCount = items.size,
            source = source,
            status = "confirmed",
            isSyncedToKhata = true,
            notes = "Confirmed and saved into Khata ledger"
        )

        val khataId = documentDao.insertDocument(doc)

        val entries = items.map { item ->
            LedgerEntry(
                khataId = khataId,
                date = item.date.ifBlank { today },
                partyName = item.partyName,
                description = item.description,
                amount = item.amount,
                type = item.type,
                category = item.category,
                source = LedgerSource.OCR,
                isConfirmed = true,
                rawText = item.rawText,
                confidence = item.confidence,
                editedByUser = item.editedByUser
            )
        }
        ledgerDao.insertEntries(entries)

        return khataId
    }

    fun getEntriesForKhata(khataId: Long): Flow<List<LedgerEntry>> {
        return ledgerDao.getEntriesForKhata(khataId)
    }

    private suspend fun seedInitialDataIfEmpty() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
        val today = sdf.format(Date())

        val initialDocs = listOf(
            DigitalDocument(
                title = "Kashi Wholesale Agency Oil & Soap Bill #408",
                category = DocumentCategory.SUPPLIER_BILL,
                dateAdded = today,
                totalAmount = 2600.0,
                extractedText = "Kashi Wholesale Agency\nMustard Oil 15L - Rs 2040\nWashing Soap 2 Cartons - Rs 560\nTotal: Rs 2600",
                parsedEntryCount = 2,
                isSyncedToKhata = true,
                notes = "Auto-scanned & synced into Khata ledger"
            ),
            DigitalDocument(
                title = "Suresh Verma Ration Parcha",
                category = DocumentCategory.CUSTOMER_CHIT,
                dateAdded = today,
                totalAmount = 1450.0,
                extractedText = "सुरेश वर्मा - 1450 रु\nआटा 10kg - 380\nसरसों तेल 2L - 280\nअरहर दाल 5kg - 790",
                parsedEntryCount = 3,
                isSyncedToKhata = true,
                notes = "OCR parsed handwritten chit"
            ),
            DigitalDocument(
                title = "Varanasi Mandi Samiti Sales Challan",
                category = DocumentCategory.MANDI_RECEIPT,
                dateAdded = "2026-08-28",
                totalAmount = 4800.0,
                extractedText = "कृषि उपज मंडी समिति वाराणसी\nविक्रय पर्ची: गेहूं 2 क्विंटल @ 2400 = 4800 रु\nमंडी शुल्क: शून्य",
                parsedEntryCount = 1,
                isSyncedToKhata = true,
                notes = "Mandi verified sales slip"
            )
        )
        documentDao.insertDocuments(initialDocs)

        // Initial sample ledger items (realistic rural Kirana & sales transactions)
        val initialLedger = listOf(
            LedgerEntry(
                date = today,
                partyName = "Suresh Verma (Panchayat Sewak)",
                description = "Monthly ration supplies (Atta, Oil & Dal)",
                amount = 1450.0,
                type = LedgerType.DEBIT,
                category = LedgerCategory.CUSTOMER_UDHAAR,
                source = LedgerSource.OCR,
                isConfirmed = true,
                customerPhone = "+91 94150 11223"
            ),
            LedgerEntry(
                date = today,
                partyName = "Daily Cash Counter",
                description = "Day retail cash sales & UPI counter collection",
                amount = 3280.0,
                type = LedgerType.CREDIT,
                category = LedgerCategory.SALES,
                source = LedgerSource.MANUAL,
                isConfirmed = true
            ),
            LedgerEntry(
                date = today,
                partyName = "Kashi Wholesale Agency",
                description = "Mustard oil tins and soap cartons purchase",
                amount = 2600.0,
                type = LedgerType.DEBIT,
                category = LedgerCategory.INVENTORY_BUY,
                source = LedgerSource.MANUAL,
                isConfirmed = true
            ),
            LedgerEntry(
                date = "2026-08-29",
                partyName = "Geeta Devi (Mahila SHG)",
                description = "Tailoring thread packets and spices sale",
                amount = 680.0,
                type = LedgerType.CREDIT,
                category = LedgerCategory.SALES,
                source = LedgerSource.VOICE,
                isConfirmed = true
            ),
            LedgerEntry(
                date = "2026-08-28",
                partyName = "Shivaji Traders",
                description = "Flour mill grinding & electricity utility payment",
                amount = 850.0,
                type = LedgerType.DEBIT,
                category = LedgerCategory.RENT_UTILITIES,
                source = LedgerSource.MANUAL,
                isConfirmed = true
            ),
            LedgerEntry(
                date = "2026-08-27",
                partyName = "Mataji SHG Bachat",
                description = "Weekly group savings deposit",
                amount = 500.0,
                type = LedgerType.DEBIT,
                category = LedgerCategory.SHG_SAVINGS,
                source = LedgerSource.MANUAL,
                isConfirmed = true
            )
        )
        ledgerDao.insertEntries(initialLedger)

        // Initial Government Schemes (PMEGP, Mudra, PM SVANidhi, Stand-Up India, NRLM)
        val initialSchemes = listOf(
            Scheme(
                id = "pm_mudra",
                name = "PM Mudra Yojana (PMMY)",
                nameHi = "प्रधानमंत्री मुद्रा योजना",
                ministry = "Ministry of Finance",
                description = "Collateral-free micro loans up to ₹10 Lakh for small business setup, shop expansion, and equipment.",
                descriptionHi = "दुकान, सिलाई, डेयरी व लघु उद्यम हेतु ₹10 लाख तक का बिना गारंटी बैंक ऋण (शिशु, किशोर व तरुण श्रेणियां)।",
                subsidyPercent = 0, // Interest subvention
                maxLoanAmount = 1000000.0,
                eligibilityCriteria = "All Indian citizens running or starting micro business (Kirana, food stall, artisan, services). No collateral required.",
                eligibilityCriteriaHi = "कोई भी भारतीय नागरिक जो लघु व्यवसाय चला रहा हो या शुरू करना चाहता हो। संपत्ति गारंटी की आवश्यकता नहीं।",
                documentsRequired = "Aadhaar Card, PAN Card, Business Address Proof, SahayakAI Khata Bank Report.",
                category = SchemeCategory.CREDIT_LOAN,
                deadline = "Ongoing Year-Round"
            ),
            Scheme(
                id = "pmegp_scheme",
                name = "PMEGP (Prime Minister Employment Generation)",
                nameHi = "प्रधानमंत्री रोजगार सृजन कार्यक्रम (PMEGP)",
                ministry = "Ministry of MSME & KVIC",
                description = "Credit-linked subsidy programme offering up to 35% government subsidy for micro enterprises in rural areas.",
                descriptionHi = "ग्रामीण क्षेत्रों में नया उद्यम लगाने पर सरकार से 35% तक की नकद सब्सिडी (मैन्युफैक्चरिंग ₹50 लाख, सर्विस ₹20 लाख)।",
                subsidyPercent = 35,
                maxLoanAmount = 5000000.0,
                eligibilityCriteria = "Age 18+, 8th pass for projects above ₹10L in manufacturing / ₹5L in service. Higher subsidy for SC/ST/Women/OBC.",
                eligibilityCriteriaHi = "आयु 18+ वर्ष। महिलाओं, SC/ST, OBC व ग्रामीण उद्यमियों को विशेष 35% सब्सिडी छूट।",
                documentsRequired = "Project Profile Report, Caste/Category Certificate, Aadhaar, Bank Details, Education proof.",
                category = SchemeCategory.MINORITY_SC_ST,
                deadline = "Ongoing FY 2026-27"
            ),
            Scheme(
                id = "pm_svanidhi",
                name = "PM SVANidhi (Street Vendor AtmaNirbhar)",
                nameHi = "पीएम स्वनिधि योजना",
                ministry = "Ministry of Housing and Urban Affairs",
                description = "Micro working capital collateral-free credit starting at ₹10,000 graduating to ₹20,000 and ₹50,000 on timely digital repayment.",
                descriptionHi = "रेहड़ी-पटरी, ठेला, फल-सब्जी व फुटपाथ विक्रेताओं के लिए ₹10,000 से ₹50,000 तक आसान ब्याज सब्सिडी लोन।",
                subsidyPercent = 7, // 7% interest subsidy directly deposited
                maxLoanAmount = 50000.0,
                eligibilityCriteria = "Street vendors, cart operators, weekly haat sellers with vending certificate or recommendation letter.",
                eligibilityCriteriaHi = "शहरी व अर्ध-शहरी रेहड़ी-पटरी व हाट बाजार विक्रेता।",
                documentsRequired = "Aadhaar Card, Mobile linked Bank Account, Vending ID / LOR.",
                category = SchemeCategory.STREET_VENDORS,
                deadline = "Active in all districts"
            ),
            Scheme(
                id = "stand_up_india",
                name = "Stand-Up India Scheme",
                nameHi = "स्टैंड-अप इंडिया योजना",
                ministry = "Ministry of Social Justice & Finance",
                description = "Bank loans between ₹10 Lakh and ₹1 Crore to at least one SC/ST and one Woman borrower per bank branch for greenfield enterprises.",
                descriptionHi = "अनुसूचित जाति (SC), जनजाति (ST) और महिला उद्यमियों को नया उद्यम लगाने हेतु ₹10 लाख से ₹1 करोड़ तक बैंक लोन।",
                subsidyPercent = 25,
                maxLoanAmount = 10000000.0,
                eligibilityCriteria = "SC/ST and/or Woman entrepreneurs above 18 years for manufacturing, services, agri-allied or trading sectors.",
                eligibilityCriteriaHi = "SC/ST वर्ग या महिला उद्यमी, न्यूनतम 51% हिस्सेदारी।",
                documentsRequired = "Identity proof, SC/ST Certificate, Project report, Balance sheet / Khata history.",
                category = SchemeCategory.MINORITY_SC_ST,
                deadline = "Active till 2027"
            ),
            Scheme(
                id = "nrlm_shg",
                name = "Deendayal Antyodaya - NRLM / Aajeevika",
                nameHi = "दीनदयाल अंत्योदय योजना - राष्ट्रीय ग्रामीण आजीविका मिशन",
                ministry = "Ministry of Rural Development",
                description = "Low-interest revolving fund and Community Investment Fund for Women Self-Help Groups (SHGs) to scale micro enterprises.",
                descriptionHi = "महिला स्वयं सहायता समूहों को कम ब्याज पर रिवाल्विंग फंड और माइक्रो-एंटरप्राइज आजीविका सहायता।",
                subsidyPercent = 30,
                maxLoanAmount = 600000.0,
                eligibilityCriteria = "Registered Rural SHG with regular Panchasutra (weekly meetings, savings, internal lending, timely repayment, book-keeping).",
                eligibilityCriteriaHi = "पंचसूत्र का पालन करने वाले ग्रामीण महिला स्वयं सहायता समूह।",
                documentsRequired = "SHG Resolution, Bank Passbook, Member List, SahayakAI Group Register.",
                category = SchemeCategory.WOMEN_EMPOWERMENT,
                deadline = "Continuous Community Program"
            )
        )
        schemeDao.insertSchemes(initialSchemes)

        // Initial Reminders
        val initialReminders = listOf(
            BusinessReminder(
                title = "PM Mudra Kishor EMI Repayment",
                dueDate = "2026-09-05",
                type = ReminderType.EMI_REPAYMENT,
                amount = 2850.0,
                isCompleted = false,
                note = "Auto-debit from Bank of Baroda account. Keep balance ready."
            ),
            BusinessReminder(
                title = "Suresh Verma Udhaar Payment Follow-up",
                dueDate = "2026-09-02",
                type = ReminderType.UDHAAR_COLLECTION,
                amount = 1450.0,
                isCompleted = false,
                note = "Send friendly WhatsApp reminder from Sahayak Khata tab."
            ),
            BusinessReminder(
                title = "Mahila SHG Monthly Meeting & Savings",
                dueDate = "2026-09-08",
                type = ReminderType.SHG_MEETING,
                amount = 500.0,
                isCompleted = false,
                note = "Submit digitized Khata report to Panchayat Gram Sevak."
            )
        )
        for (r in initialReminders) {
            reminderDao.insertReminder(r)
        }

        // Initial Community Posts (realistic rural entrepreneur stories)
        val initialPosts = listOf(
            CommunityPost(
                authorName = "Sunita Devi",
                authorRole = "Kirana Owner • 2h ago",
                content = "Just upgraded my shop with a new digital weighing scale using the PM Svanidhi loan! The process was much smoother than I expected. Happy to guide anyone applying. ✨",
                tag = "#SvanidhiSuccess",
                postType = PostType.SUCCESS,
                imageUrl = "https://images.unsplash.com/photo-1587653915935-5623d0c949da?w=600",
                likesCount = 248,
                commentsCount = 32,
                voiceNoteSeconds = 42,
                createdAtFormatted = "2h ago"
            ),
            CommunityPost(
                authorName = "Rajesh Patel",
                authorRole = "Hardware Store • 4h ago",
                content = "I'm thinking of starting to accept UPI payments for wholesale orders above ₹50,000. Has anyone faced issues with transaction limits or delays with standard merchant accounts?",
                tag = "#UPIQuestion",
                postType = PostType.QUESTION,
                likesCount = 45,
                commentsCount = 18,
                voiceNoteSeconds = null,
                createdAtFormatted = "4h ago"
            ),
            CommunityPost(
                authorName = "Amit Sharma",
                authorRole = "Sahayak Advisor • 1d ago",
                content = "The State Government has just announced a 20% subsidy on modern loom upgrades. Deadline to apply is next month. Check the 'Schemes' tab to see if you are eligible!",
                tag = "#SchemeUpdate",
                postType = PostType.SCHEME_UPDATE,
                likesCount = 512,
                commentsCount = 89,
                voiceNoteSeconds = 28,
                createdAtFormatted = "1d ago"
            ),
            CommunityPost(
                authorName = "Mohan Lal Gupta",
                authorRole = "Kirana Store, Jaunpur • Yesterday",
                content = "साथियों, थोक मंडी से दाल-तेल सीधे Haat के दिन खरीदने से 10% लागत बचती है। SahayakAI ब्रेक-ईवेन कैलकुलेटर से मैंने अपना दैनिक खर्च ₹300 कम कर लिया है।",
                tag = "#KiranaProfit",
                postType = PostType.BUSINESS_TIP,
                likesCount = 24,
                commentsCount = 4,
                voiceNoteSeconds = null,
                createdAtFormatted = "Yesterday"
            ),
            CommunityPost(
                authorName = "Geeta Sharma",
                authorRole = "Dairy Farmer, Varanasi • 3d ago",
                content = "मंडी में दूध के दाम स्थिर हैं लेकिन चारे की कीमत बढ़ रही है। मैंने AI सलाहकार से पूछकर साइलेज चारा बनाना शुरू किया जिससे 15% बचत हो रही है।",
                tag = "#DairyTips",
                postType = PostType.BUSINESS_TIP,
                likesCount = 19,
                commentsCount = 2,
                voiceNoteSeconds = 28,
                createdAtFormatted = "3d ago"
            ),
            CommunityPost(
                authorName = "Vikram Singh",
                authorRole = "Street Vendor, Mirzapur • 5d ago",
                content = "झुग्गी-झोपड़ी विक्रेताओं के लिए PM SVANidhi का पहला ₹10,000 लोन मिला। ब्याज सब्सिडी सीधे खाते में आती है। किसी से कमीशन मत दो, सीधे बैंक जाओ!",
                tag = "#SVANidhi",
                postType = PostType.SUCCESS,
                likesCount = 77,
                commentsCount = 9,
                voiceNoteSeconds = null,
                createdAtFormatted = "5d ago"
            )
        )
        communityDao.insertPosts(initialPosts)

        // Initial Chat Conversations (realistic rural peer-to-peer chats)
        val initialConversations = listOf(
            ChatConversation(
                id = "rajesh_kumar",
                name = "Rajesh Kumar",
                lastMessage = "Bhai, loan ka process kya hai?",
                time = "10:42 AM",
                unreadCount = 2,
                isGroup = false,
                initial = "R"
            ),
            ChatConversation(
                id = "sunita_devi",
                name = "Sunita Devi (Maha Laxmi Store)",
                lastMessage = "Haan, payment receive ho gaya.",
                time = "Yesterday",
                unreadCount = 0,
                isGroup = false,
                initial = "S"
            ),
            ChatConversation(
                id = "agri_tech_network",
                name = "Agri-Tech Network",
                lastMessage = "Amit: Naya beej kahan se liya?",
                time = "Tuesday",
                unreadCount = 0,
                isGroup = true,
                initial = "A"
            ),
            ChatConversation(
                id = "vikram_auto",
                name = "Vikram Auto Repairs",
                lastMessage = "Khata entry check kar lena ek baar.",
                time = "Monday",
                unreadCount = 1,
                isGroup = false,
                initial = "V"
            ),
            ChatConversation(
                id = "shg_meeting",
                name = "Mahila SHG - Varanasi",
                lastMessage = "Sunita: Kal meeting 5 baje hai sab log.",
                time = "Sunday",
                unreadCount = 5,
                isGroup = true,
                initial = "M"
            )
        )
        conversationDao.insertConversations(initialConversations)

        // Initial Thread Messages for each conversation
        val initialThreads = listOf(
            ChatThreadMessage(conversationId = "rajesh_kumar", text = "Bhai, loan ka process kya hai?", isMine = false, time = "10:38 AM"),
            ChatThreadMessage(conversationId = "rajesh_kumar", text = "Kis loan ki baat kar rahe ho, Mudra ya SVANidhi?", isMine = true, time = "10:40 AM"),
            ChatThreadMessage(conversationId = "rajesh_kumar", text = "Mudra wala. Bank se directly apply karna hota hai?", isMine = false, time = "10:42 AM"),
            ChatThreadMessage(conversationId = "rajesh_kumar", text = "Haan, apna Khata report aur Aadhaar leke kisi bhi bank jao. SahayakAI me Report tab me mil jayega.", isMine = true, time = "10:45 AM"),
            ChatThreadMessage(conversationId = "sunita_devi", text = "Haan, payment receive ho gaya. Thank you!", isMine = false, time = "Yesterday"),
            ChatThreadMessage(conversationId = "sunita_devi", text = "Aapke 2 cartons kal aur bhej rahe hain. Bill bhi sath me lagega.", isMine = true, time = "Yesterday"),
            ChatThreadMessage(conversationId = "agri_tech_network", text = "Amit: Naya beej kahan se liya?", isMine = false, time = "Tuesday"),
            ChatThreadMessage(conversationId = "agri_tech_network", text = "Kisan Bazaar se liya tha, 20% sasta pada.", isMine = true, time = "Tuesday"),
            ChatThreadMessage(conversationId = "vikram_auto", text = "Khata entry check kar lena ek baar.", isMine = false, time = "Monday"),
            ChatThreadMessage(conversationId = "vikram_auto", text = "Kal ka kaam ho gaya tha, main abhi entry daal deta hoon.", isMine = true, time = "Monday"),
            ChatThreadMessage(conversationId = "shg_meeting", text = "Sunita: Kal meeting 5 baje hai sab log.", isMine = false, time = "Sunday"),
            ChatThreadMessage(conversationId = "shg_meeting", text = "Theek hai, main bachat register le aaungi.", isMine = true, time = "Sunday"),
            ChatThreadMessage(conversationId = "shg_meeting", text = "Main naya SahayakAI group account bhi bana ke dikhaungi.", isMine = false, time = "Sunday")
        )
        threadMessageDao.insertMessages(initialThreads)
    }

    private fun getInitialMandiPrices(): List<MandiCommodity> {
        return listOf(
            MandiCommodity(
                id = "cmd_wheat",
                name = "Wheat (गेहूं)",
                nameHi = "गेहूं (Sharbati/Dara)",
                marketLocation = "Varanasi Mandi, UP",
                pricePerUnit = 2480.0,
                unit = "Quintal",
                priceChangePercent = +3.2,
                trend = "UP",
                advisoryNote = "Demand rising ahead of festive season. Recommended holding stock for 1-2 weeks.",
                advisoryNoteHi = "त्योहारी मांग से दाम बढ़ रहे हैं। 1-2 हफ्ते स्टॉक रखने पर बेहतर भाव मिल सकता है।"
            ),
            MandiCommodity(
                id = "cmd_mustard",
                name = "Mustard (सरसों)",
                nameHi = "सरसों / राई",
                marketLocation = "Agra Mandi, UP",
                pricePerUnit = 5650.0,
                unit = "Quintal",
                priceChangePercent = +1.5,
                trend = "UP",
                advisoryNote = "High oil mill crushing demand.",
                advisoryNoteHi = "तेल मिलों की मजबूत खरीद जारी है।"
            ),
            MandiCommodity(
                id = "cmd_tomato",
                name = "Tomato (टमाटर)",
                nameHi = "देशी टमाटर",
                marketLocation = "Mirzapur Mandi, UP",
                pricePerUnit = 24.0,
                unit = "Kg",
                priceChangePercent = -4.5,
                trend = "DOWN",
                advisoryNote = "Local fresh harvest arrival increasing. Sell within 24 hours to avoid spoilage.",
                advisoryNoteHi = "नई आवक बढ़ने से भाव में नरमी। खराब होने से बचाने के लिए जल्द बिक्री करें।"
            ),
            MandiCommodity(
                id = "cmd_potato",
                name = "Potato (आलू)",
                nameHi = "चिप्सोना व लाल आलू",
                marketLocation = "Kanpur Mandi, UP",
                pricePerUnit = 18.5,
                unit = "Kg",
                priceChangePercent = 0.0,
                trend = "STABLE",
                advisoryNote = "Cold storage release steady. Prices expected to remain range-bound.",
                advisoryNoteHi = "कोल्ड स्टोरेज से आपूर्ति स्थिर है, भाव सामान्य रहेंगे।"
            ),
            MandiCommodity(
                id = "cmd_milk",
                name = "Cow Milk (गाय का दूध)",
                nameHi = "ताज़ा गाय का दूध",
                marketLocation = "Local Cooperative",
                pricePerUnit = 48.0,
                unit = "Litre",
                priceChangePercent = +2.1,
                trend = "UP",
                advisoryNote = "Cooperative bonus active for SNF >= 8.5.",
                advisoryNoteHi = "फैट व SNF गुणवत्ता पर ₹2/लीटर अतिरिक्त प्रोत्साहन।"
            )
        )
    }
}
