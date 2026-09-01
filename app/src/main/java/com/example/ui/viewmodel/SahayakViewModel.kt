package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.SahayakRepository
import com.example.data.service.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LedgerFilter {
    ALL,
    JAMA_CREDIT,
    UDHAAR_DEBIT,
    PENDING_OCR
}

data class GrowthSimResult(
    val investmentAmount: Double,
    val monthlyRevenueAdded: Double,
    val monthlyNetProfitAdded: Double,
    val annualProfitIncrease: Double,
    val roiPercent: Double,
    val paybackMonths: Double,
    val mudraCategoryFit: String,
    val adviceNotes: String,
    val adviceNotesHi: String
)

data class ProfitSplitMember(
    val id: String,
    val name: String,
    val role: String,
    val contributionPercent: Double,
    val payoutAmount: Double = 0.0
)

data class ProfitSplitResult(
    val grossRevenue: Double,
    val totalExpenses: Double,
    val netDistributableProfit: Double,
    val reserveFundAmount: Double,
    val reservePercent: Double,
    val memberPayouts: List<ProfitSplitMember>
)

data class BreakEvenResult(
    val fixedCostsMonthly: Double,
    val unitSellingPrice: Double,
    val unitVariableCost: Double,
    val contributionMarginPerUnit: Double,
    val contributionMarginRatio: Double,
    val unitsNeededMonthly: Int,
    val unitsNeededDaily: Int,
    val salesRevenueNeededMonthly: Double,
    val adviceSummary: String,
    val adviceSummaryHi: String
)

class SahayakViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val repository = SahayakRepository(database)
    private val geminiService = GeminiAdvisorService()
    private val scoringService = FinancialScoringService()
    private val ocrParser = OcrKhataParser()
    private val ttsManager = TtsManager(application)
    private val kycProvider = repository.kycProvider

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val allLedgerEntries: StateFlow<List<LedgerEntry>> = repository.allLedgerEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchemes: StateFlow<List<Scheme>> = repository.allSchemes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReminders: StateFlow<List<BusinessReminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPosts: StateFlow<List<CommunityPost>> = repository.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mandiPrices: StateFlow<List<MandiCommodity>> = repository.mandiPrices

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDigitalDocuments: StateFlow<List<DigitalDocument>> = repository.allDigitalDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allConversations: StateFlow<List<ChatConversation>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val threadMessages: StateFlow<List<ChatThreadMessage>> = repository.threadMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Machine Learning Forecasting & Demand Models
    val allCommodityForecasts: List<com.example.data.ml.CommodityMlForecast> =
        com.example.data.ml.MlForecastingEngine.getAllCommodityForecasts()

    private val _selectedForecastCommodity = MutableStateFlow(allCommodityForecasts.first())
    val selectedForecastCommodity: StateFlow<com.example.data.ml.CommodityMlForecast> = _selectedForecastCommodity

    val mlPredictionSummary: com.example.data.ml.MlPredictionSummary =
        com.example.data.ml.MlForecastingEngine.getMlPredictionSummary()

    private val _selectedLedgerFilter = MutableStateFlow(LedgerFilter.ALL)
    val selectedLedgerFilter: StateFlow<LedgerFilter> = _selectedLedgerFilter

    private val _selectedSchemeCategory = MutableStateFlow<SchemeCategory?>(null)
    val selectedSchemeCategory: StateFlow<SchemeCategory?> = _selectedSchemeCategory

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking

    val isTtsSpeaking: StateFlow<Boolean> = ttsManager.isSpeaking

    // OCR Pending review items (Human-in-the-loop)
    private val _ocrReviewItems = MutableStateFlow<List<OcrParsedItem>?>(null)
    val ocrReviewItems: StateFlow<List<OcrParsedItem>?> = _ocrReviewItems

    // Calculators state
    private val _growthSimResult = MutableStateFlow<GrowthSimResult?>(null)
    val growthSimResult: StateFlow<GrowthSimResult?> = _growthSimResult

    private val _profitSplitResult = MutableStateFlow<ProfitSplitResult?>(null)
    val profitSplitResult: StateFlow<ProfitSplitResult?> = _profitSplitResult

    private val _breakEvenResult = MutableStateFlow<BreakEvenResult?>(null)
    val breakEvenResult: StateFlow<BreakEvenResult?> = _breakEvenResult

    // KYC Mock States
    private val _panDetails = MutableStateFlow<PanDetails?>(null)
    val panDetails: StateFlow<PanDetails?> = _panDetails

    private val _aadhaarDetails = MutableStateFlow<AadhaarDetails?>(null)
    val aadhaarDetails: StateFlow<AadhaarDetails?> = _aadhaarDetails

    private val _bankAccount = MutableStateFlow<BankAccount?>(null)
    val bankAccount: StateFlow<BankAccount?> = _bankAccount

    private val _cibilReport = MutableStateFlow<CibilReport?>(null)
    val cibilReport: StateFlow<CibilReport?> = _cibilReport

    private val _selectedChatConversation = MutableStateFlow<ChatConversation?>(null)
    val selectedChatConversation: StateFlow<ChatConversation?> = _selectedChatConversation

    // Reactive Financial Health Score & Bank Report
    val financialHealthScore: StateFlow<FinancialHealthScore> = combine(userProfile, allLedgerEntries) { profile, ledger ->
        scoringService.calculateScore(profile, ledger)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        scoringService.calculateScore(UserProfile(), emptyList())
    )

    val bankReportSummary: StateFlow<BankReportSummary> = combine(userProfile, financialHealthScore, allLedgerEntries) { profile, score, ledger ->
        scoringService.generateBankReportSummary(profile, score, ledger)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        scoringService.generateBankReportSummary(UserProfile(), scoringService.calculateScore(UserProfile(), emptyList()), emptyList())
    )

    init {
        // Run initial default calculations so screens have immediate rich values
        runGrowthSimulation(15000.0, 450.0, 25.0)
        runBreakEven(4500.0, 50.0, 32.0)
        loadInitialMockKyc()
    }

    private fun loadInitialMockKyc() {
        viewModelScope.launch(Dispatchers.IO) {
            _panDetails.value = kycProvider.verifyPan("ABCPS1234F")
            _aadhaarDetails.value = kycProvider.verifyAadhaar("987654324892", "123456")
            _bankAccount.value = kycProvider.linkBank("ACC-9048102")
            _cibilReport.value = kycProvider.fetchCibil("usr_demo_01")
        }
    }

    fun setLedgerFilter(filter: LedgerFilter) {
        _selectedLedgerFilter.value = filter
    }

    fun setSchemeCategory(category: SchemeCategory?) {
        _selectedSchemeCategory.value = category
    }

    fun setLanguage(language: AppLanguage) {
        val current = userProfile.value
        val updated = current.copy(preferredLanguage = language)
        repository.updateUserProfile(updated)
    }

    fun updateProfile(name: String, businessType: BusinessType, location: String, monthlyTurnover: Double, shgName: String) {
        val current = userProfile.value
        val updated = current.copy(
            name = name,
            businessType = businessType,
            location = location,
            monthlyTurnover = monthlyTurnover,
            shgName = shgName
        )
        repository.updateUserProfile(updated)
    }

    // --- CHAT & VOICE ADVISORY ---

    fun sendChatMessage(query: String, autoSpeak: Boolean = true) {
        if (query.isBlank()) return
        viewModelScope.launch {
            // 1. Save user query
            val userMsg = ChatMessage(text = query, isUser = true)
            repository.saveChatMessage(userMsg)

            _isAiThinking.value = true

            try {
                val profile = userProfile.value
                val ledger = allLedgerEntries.value
                val mandi = mandiPrices.value

                val (advice, isOffline) = geminiService.getAdvice(
                    userQuery = query,
                    userProfile = profile,
                    ledgerEntries = ledger,
                    mandiPrices = mandi
                )

                val aiMsg = ChatMessage(
                    text = advice,
                    isUser = false,
                    isOfflineTier = isOffline
                )
                repository.saveChatMessage(aiMsg)

                if (autoSpeak) {
                    speakText(advice, if (profile.preferredLanguage == AppLanguage.HINDI) "hi" else "en")
                }
            } catch (e: Exception) {
                val errorMsg = "माफ़ कीजिए, उत्तर प्राप्त करने में समस्या हुई। कृपया पुनः प्रयास करें।"
                repository.saveChatMessage(ChatMessage(text = errorMsg, isUser = false, isOfflineTier = true))
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun speakText(text: String, langCode: String = "hi") {
        ttsManager.speak(text, langCode)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- KHATA & OCR DIGITIZATION ---

    fun startOcrScan(rawText: String) {
        val parsed = ocrParser.parseKhataText(rawText)
        _ocrReviewItems.value = parsed
    }

    fun clearOcrReview() {
        _ocrReviewItems.value = null
    }

    fun confirmOcrEntries(confirmedItems: List<OcrParsedItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val today = sdf.format(Date())

            val entries = confirmedItems.map { item ->
                LedgerEntry(
                    date = today,
                    partyName = item.partyName,
                    description = item.description,
                    amount = item.amount,
                    type = item.type,
                    category = item.category,
                    source = LedgerSource.OCR,
                    isConfirmed = true
                )
            }
            repository.addLedgerEntries(entries)
            _ocrReviewItems.value = null
        }
    }

    fun addManualLedgerEntry(
        partyName: String,
        description: String,
        amount: Double,
        type: LedgerType,
        category: LedgerCategory,
        phone: String? = null,
        source: LedgerSource = LedgerSource.MANUAL
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val today = sdf.format(Date())

            val entry = LedgerEntry(
                date = today,
                partyName = partyName,
                description = description,
                amount = amount,
                type = type,
                category = category,
                source = source,
                isConfirmed = true,
                customerPhone = phone
            )
            repository.addLedgerEntry(entry)
        }
    }

    fun deleteLedgerEntry(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLedgerEntry(id)
        }
    }

    fun generateWhatsAppReminderText(entry: LedgerEntry, lang: AppLanguage = AppLanguage.HINDI): String {
        return if (lang == AppLanguage.HINDI) {
            "नमस्ते ${entry.partyName} जी! 🙏\nआपकी दुकान '${userProfile.value.name}' का कुल बकाया ₹${String.format(Locale.ROOT, "%.0f", entry.amount)} बाकी है (${entry.description})। कृपया सुविधानुसार भुगतान कर दें। धन्यवाद! \n- ${userProfile.value.name}"
        } else {
            "Hello ${entry.partyName}! 🙏\nThis is a polite reminder from '${userProfile.value.name}' regarding outstanding balance of ₹${String.format(Locale.ROOT, "%.0f", entry.amount)} for ${entry.description}. Kindly settle at your earliest convenience. Thank you!"
        }
    }

    fun getSampleKhataPresets() = ocrParser.getSampleKhataPresets()

    // --- ML COMMODITY FORECASTING ---

    fun selectForecastCommodity(commodity: com.example.data.ml.CommodityMlForecast) {
        _selectedForecastCommodity.value = commodity
    }

    // --- AUTHENTICATION & LOGIN ---

    fun loginWithOtp(phone: String, otp: String, role: BusinessType, name: String): Boolean {
        // Valid demo OTP: 749201 or any 6-digit number in demo mode
        val isValid = otp.trim().length == 6
        if (isValid) {
            val current = userProfile.value
            val updated = current.copy(
                phone = if (phone.startsWith("+91")) phone else "+91 $phone",
                businessType = role,
                name = if (name.isNotBlank()) name else current.name,
                isLoggedIn = true,
                authOtp = otp
            )
            repository.updateUserProfile(updated)
            return true
        }
        return false
    }

    fun logout() {
        val current = userProfile.value
        val updated = current.copy(isLoggedIn = false)
        repository.updateUserProfile(updated)
    }

    // --- DIGITAL DOCUMENT LOCKER ---

    fun addScannedDigitalDocument(
        title: String,
        category: DocumentCategory,
        totalAmount: Double,
        extractedText: String,
        parsedEntryCount: Int,
        notes: String = ""
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val today = sdf.format(Date())

            val doc = DigitalDocument(
                title = title,
                category = category,
                dateAdded = today,
                totalAmount = totalAmount,
                extractedText = extractedText,
                parsedEntryCount = parsedEntryCount,
                isSyncedToKhata = true,
                notes = notes
            )
            repository.addDigitalDocument(doc)
        }
    }

    fun deleteDigitalDocument(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDigitalDocument(id)
        }
    }

    fun processCameraScanAndAddToLocker(
        photoTitle: String,
        category: DocumentCategory,
        rawOcrText: String
    ) {
        val parsed = ocrParser.parseKhataText(rawOcrText)
        val totalAmount = parsed.sumOf { it.amount }
        addScannedDigitalDocument(
            title = photoTitle,
            category = category,
            totalAmount = totalAmount,
            extractedText = rawOcrText,
            parsedEntryCount = parsed.size,
            notes = "Scanned via camera & preserved in Digital Document Vault"
        )
        // Also load into OCR review for Khata ledger confirmation
        _ocrReviewItems.value = parsed
    }

    // --- REMINDERS ---

    fun addReminder(title: String, dueDate: String, type: ReminderType, amount: Double?, note: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val reminder = BusinessReminder(
                title = title,
                dueDate = dueDate,
                type = type,
                amount = amount,
                isCompleted = false,
                note = note
            )
            repository.addReminder(reminder)
        }
    }

    fun toggleReminder(reminder: BusinessReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleReminderComplete(reminder.id, !reminder.isCompleted)
        }
    }

    fun deleteReminder(reminder: BusinessReminder) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteReminder(reminder)
        }
    }

    // --- COMMUNITY ---

    fun togglePostLike(post: CommunityPost) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePostLike(post.id, post.isLikedByUser, post.likesCount)
        }
    }

    fun addCommunityPost(content: String, tag: String, voiceSeconds: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = userProfile.value
            val newPost = CommunityPost(
                authorName = profile.name,
                authorRole = "${profile.businessType.title}, ${profile.location.substringBefore(",")}",
                content = content,
                tag = if (tag.startsWith("#")) tag else "#$tag",
                likesCount = 1,
                commentsCount = 0,
                isLikedByUser = true,
                voiceNoteSeconds = voiceSeconds,
                createdAtFormatted = "Just now"
            )
            repository.addCommunityPost(newPost)
        }
    }

    fun addCommunityPost(content: String, tag: String, type: PostType, voiceSeconds: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val profile = userProfile.value
            val newPost = CommunityPost(
                authorName = profile.name,
                authorRole = "${profile.businessType.title}, ${profile.location.substringBefore(",")}",
                content = content,
                tag = if (tag.startsWith("#")) tag else "#$tag",
                postType = type,
                likesCount = 1,
                commentsCount = 0,
                isLikedByUser = true,
                voiceNoteSeconds = voiceSeconds,
                createdAtFormatted = "Just now"
            )
            repository.addCommunityPost(newPost)
        }
    }

    // --- CHATS ---

    fun openChatConversation(conversation: ChatConversation) {
        _selectedChatConversation.value = conversation
    }

    fun closeChatConversation() {
        _selectedChatConversation.value = null
    }

    fun sendThreadMessage(text: String) {
        val conversation = _selectedChatConversation.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val msg = ChatThreadMessage(
                conversationId = conversation.id,
                text = text,
                isMine = true,
                time = "Now"
            )
            repository.sendThreadMessage(msg)
        }
    }

    // --- FINANCIAL CALCULATORS ---

    fun runGrowthSimulation(investment: Double, extraSalesDaily: Double, marginPercent: Double) {
        val extraMonthlyRevenue = extraSalesDaily * 30.0
        val extraMonthlyProfit = extraMonthlyRevenue * (marginPercent / 100.0)
        val annualProfitIncrease = extraMonthlyProfit * 12.0
        val roi = if (investment > 0) (annualProfitIncrease / investment) * 100.0 else 0.0
        val paybackMonths = if (extraMonthlyProfit > 0) investment / extraMonthlyProfit else 0.0

        val mudraCategory = when {
            investment <= 50000 -> "PM Mudra 'Shishu' (Up to ₹50,000 - Zero Collateral)"
            investment <= 500000 -> "PM Mudra 'Kishor' (₹50K to ₹5 Lakh)"
            else -> "PM Mudra 'Tarun' / PMEGP"
        }

        val adviceEn = "Investing ₹${String.format(Locale.ROOT, "%,.0f", investment)} pays back in ${String.format(Locale.ROOT, "%.1f", paybackMonths)} months with an annual return of ${String.format(Locale.ROOT, "%.0f", roi)}%. Highly recommended under $mudraCategory."
        val adviceHi = "₹${String.format(Locale.ROOT, "%,.0f", investment)} का निवेश ${String.format(Locale.ROOT, "%.1f", paybackMonths)} महीने में वापस वसूल हो जाएगा और वार्षिक रिटर्न ${String.format(Locale.ROOT, "%.0f", roi)}% रहेगा। $mudraCategory के तहत आवेदन करें।"

        _growthSimResult.value = GrowthSimResult(
            investmentAmount = investment,
            monthlyRevenueAdded = extraMonthlyRevenue,
            monthlyNetProfitAdded = extraMonthlyProfit,
            annualProfitIncrease = annualProfitIncrease,
            roiPercent = roi,
            paybackMonths = paybackMonths,
            mudraCategoryFit = mudraCategory,
            adviceNotes = adviceEn,
            adviceNotesHi = adviceHi
        )
    }

    fun runProfitSplit(
        grossRevenue: Double,
        expenses: Double,
        reservePercent: Double,
        members: List<ProfitSplitMember>
    ) {
        val netProfit = (grossRevenue - expenses).coerceAtLeast(0.0)
        val reserveAmount = netProfit * (reservePercent / 100.0)
        val distributable = (netProfit - reserveAmount).coerceAtLeast(0.0)

        val updatedMembers = members.map { member ->
            val share = distributable * (member.contributionPercent / 100.0)
            member.copy(payoutAmount = share)
        }

        _profitSplitResult.value = ProfitSplitResult(
            grossRevenue = grossRevenue,
            totalExpenses = expenses,
            netDistributableProfit = distributable,
            reserveFundAmount = reserveAmount,
            reservePercent = reservePercent,
            memberPayouts = updatedMembers
        )
    }

    fun runBreakEven(fixedMonthlyCost: Double, unitPrice: Double, variableCostPerUnit: Double) {
        val marginPerUnit = (unitPrice - variableCostPerUnit).coerceAtLeast(0.01)
        val marginRatio = (marginPerUnit / unitPrice)
        val unitsMonthly = if (marginPerUnit > 0) (fixedMonthlyCost / marginPerUnit).toInt() + 1 else 0
        val unitsDaily = (unitsMonthly / 30).coerceAtLeast(1)
        val revenueNeeded = unitsMonthly * unitPrice

        val adviceEn = "You need to sell at least $unitsDaily units per day (₹${String.format(Locale.ROOT, "%,.0f", revenueNeeded)}/month) to cover rent, utilities and break even."
        val adviceHi = "किराया व बिजली लागत निकालने के लिए आपको प्रतिदिन कम से कम $unitsDaily यूनिट (मासिक ₹${String.format(Locale.ROOT, "%,.0f", revenueNeeded)}) बेचना आवश्यक है।"

        _breakEvenResult.value = BreakEvenResult(
            fixedCostsMonthly = fixedMonthlyCost,
            unitSellingPrice = unitPrice,
            unitVariableCost = variableCostPerUnit,
            contributionMarginPerUnit = marginPerUnit,
            contributionMarginRatio = marginRatio,
            unitsNeededMonthly = unitsMonthly,
            unitsNeededDaily = unitsDaily,
            salesRevenueNeededMonthly = revenueNeeded,
            adviceSummary = adviceEn,
            adviceSummaryHi = adviceHi
        )
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
