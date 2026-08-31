package com.example.data.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.model.LedgerEntry
import com.example.data.model.LedgerType
import com.example.data.model.MandiCommodity
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiAdvisorService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    // Offline Tier: Local Knowledge Base (Hindi + English)
    private val offlineKnowledgeBase = listOf(
        OfflineAnswer(
            keywords = listOf("mudra", "मुद्रा", "loan", "लोन", "कर्ज", "shishu", "kishor", "tarun"),
            answerHi = "🏦 **प्रधानमंत्री मुद्रा योजना (PMMY) जानकारी:**\n\n1. **शिशु लोन:** ₹50,000 तक (नया या छोटा काम शुरू करने हेतु, कोई गारंटी नहीं)।\n2. **किशोर लोन:** ₹50,000 से ₹5 लाख तक (दुकान या स्टॉक बढ़ाने हेतु)।\n3. **तरुण लोन:** ₹5 लाख से ₹10 लाख तक।\n\n💡 *आवेदन कैसे करें:* अपने नजदीकी सरकारी या ग्रामीण बैंक में आधार, पैन कार्ड, दुकान का प्रमाण और अपना SahayakAI 'बैंक-रेडी रिपोर्ट' लेकर जाएं।",
            answerEn = "🏦 **PM Mudra Yojana (PMMY) Details:**\n\n1. **Shishu Loan:** Up to ₹50,000 (For small tools/working capital, zero collateral).\n2. **Kishor Loan:** ₹50,000 to ₹5,00,000 (For expanding shop/inventory).\n3. **Tarun Loan:** ₹5 Lakh to ₹10 Lakh.\n\n💡 *How to apply:* Visit your nearest rural/commercial bank with Aadhaar, PAN, shop proof, and your SahayakAI 'Bank-Ready Report'."
        ),
        OfflineAnswer(
            keywords = listOf("svanidhi", "स्वनिधि", "street vendor", "ठेला", "पटरी", "रेहड़ी", "10000", "20000", "50000"),
            answerHi = "🛒 **पीएम स्वनिधि योजना (PM SVANidhi):**\n\n- रेहड़ी, ठेला, फल-सब्जी विक्रेता और चाय-नाश्ता दुकानों के लिए ₹10,000 का पहला ऋण (समय पर भरने पर ₹20,000 व ₹50,000 का अगला ऋण)।\n- 7% ब्याज सब्सिडी सीधे खाते में मिलती है और डिजिटल भुगतान पर ₹1,200 वार्षिक कैशबैक।\n- किसी संपत्ति को गिरवी रखने की आवश्यकता नहीं है।",
            answerEn = "🛒 **PM SVANidhi Scheme:**\n\n- Micro working capital loan of ₹10,000 for street vendors & micro food stalls (graduates to ₹20,000 & ₹50,000 upon timely repayment).\n- 7% interest subsidy directly credited + ₹1,200 annual cashback on digital transactions."
        ),
        OfflineAnswer(
            keywords = listOf("pmegp", "पीएमईजीपी", "subsidy", "सब्सिडी", "खादी", "kvic", "मैन्युफैक्चरिंग"),
            answerHi = "🏭 **PMEGP योजना (प्रधानमंत्री रोजगार सृजन कार्यक्रम):**\n\n- विनिर्माण (Manufacturing) हेतु ₹50 लाख तक और सेवा क्षेत्र (Services) हेतु ₹20 लाख तक का ऋण।\n- ग्रामीण क्षेत्र में सामान्य वर्ग को 25% और SC/ST/OBC/महिला/दिव्यांग को **35% तक सरकारी सब्सिडी** मिलती है।\n- 18 वर्ष से अधिक आयु और 8वीं पास योग्यता।",
            answerEn = "🏭 **PMEGP Scheme:**\n\n- Up to ₹50 Lakh for manufacturing & ₹20 Lakh for service micro-units.\n- Government subsidy up to **35% in rural areas** for SC/ST/OBC/Women entrepreneurs (25% for general).\n- Apply via kviconline.gov.in portal."
        ),
        OfflineAnswer(
            keywords = listOf("udhaar", "उधार", "recovery", "वसूली", "khata", "खाता", "बकाया"),
            answerHi = "📋 **उधार वसूली व खाता प्रबंधन टिप्स:**\n\n1. कभी भी कुल मासिक बिक्री का 20% से अधिक उधार न बांटें।\n2. ग्राहक को प्यार से याद दिलाने हेतु SahayakAI खाता सेक्शन से **WhatsApp/SMS तकादा संदेश** भेजें।\n3. नए ग्राहकों को पहले छोटी रकम का उधार दें, समय पर लौटाने पर ही सीमा बढ़ाएं।",
            answerEn = "📋 **Udhaar Recovery & Cashflow Tips:**\n\n1. Keep total outstanding customer credit below 20% of monthly sales.\n2. Use the SahayakAI WhatsApp reminder feature to send polite polite payment links/messages.\n3. Offer a small 2% cash discount on immediate upfront payment."
        ),
        OfflineAnswer(
            keywords = listOf("profit", "मुनाफा", "margin", "मार्जिन", "बिक्री", "बढ़ाएं", "growth"),
            answerHi = "📈 **ग्रामीण दुकान/व्यापार का मुनाफा बढ़ाने के 4 नियम:**\n\n1. **फास्ट-मूविंग सामान:** तेल, साबुन, बिस्किट, रीचार्ज पर टर्नओवर तेज रखें।\n2. **सीधा थोक मंडी से खरीद:** बिचौलियों को हटाकर 8-12% लागत बचाएं।\n3. **कॉम्बो ऑफर:** त्योहारों और साप्ताहिक हाट में बंडल पैक बनाएं।\n4. **डिजिटल पेमेंट:** QR कोड लगाएं ताकि छुट्टे पैसे की समस्या न हो।",
            answerEn = "📈 **4 Rules to Boost Rural Micro-Business Profits:**\n\n1. Maintain fast turnover on essential household staples.\n2. Direct procurement from wholesale mandi to save 8-12% middleman margin.\n3. Run combo packages during local weekly Haat bazaar days.\n4. Accept UPI to capture all customer wallet sizes without change issues."
        )
    )

    suspend fun getAdvice(
        userQuery: String,
        userProfile: UserProfile,
        ledgerEntries: List<LedgerEntry>,
        mandiPrices: List<MandiCommodity>,
        forceOffline: Boolean = false
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        val lang = userProfile.preferredLanguage

        // 1. Check Offline Tier first if forced or if no API Key provided
        if (forceOffline || apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val offlineMatch = matchOfflineKnowledge(userQuery, lang)
            if (offlineMatch != null) {
                return@withContext Pair(offlineMatch, true)
            }
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                val fallbackMsg = if (lang == AppLanguage.HINDI) {
                    "🌿 **सहायक एआई ग्रामीण सलाहकार (ऑफ़लाइन मोड):**\n\nआपके प्रश्न *\"$userQuery\"* के लिए सलाह:\n- अपनी दुकान का दैनिक खाता नियमित दर्ज करें ताकि बैंक लोन हेतु आपका वित्तीय स्कोर बढ़े।\n- सरकारी मुद्रा (Mudra) व पीएम स्वनिधि योजना का लाभ उठाने के लिए 'योजनाएं' टैब देखें।\n- अधिक सटीक लाइव सलाह हेतु इंटरनेट कनेक्ट करें।"
                } else {
                    "🌿 **SahayakAI Rural Advisor (Offline Mode):**\n\nAdvice for *\"$userQuery\"*:\n- Maintain your daily Khata entries consistently to strengthen your Bank-Ready Financial Health Score.\n- Check the 'Schemes' section for PM Mudra & SVANidhi subsidized loans matching your ${userProfile.businessType.title}.\n- Connect to internet for deep personalized AI reasoning."
                }
                return@withContext Pair(fallbackMsg, true)
            }
        }

        // 2. Online Tier: Gemini API reasoning over injected real context
        try {
            val prompt = buildInjectedContextPrompt(userQuery, userProfile, ledgerEntries, mandiPrices)
            val responseText = callGeminiRest(prompt, apiKey)
            return@withContext Pair(responseText, false)
        } catch (e: Exception) {
            Log.e("GeminiAdvisorService", "Gemini API call failed, falling back to offline", e)
            val offlineFallback = matchOfflineKnowledge(userQuery, lang) ?: if (lang == AppLanguage.HINDI) {
                "🌿 **सहायक एआई (ऑफ़लाइन बैकअप):** नेटवर्क व्यस्त है। आपके व्यापार (${userProfile.businessType.titleHi}) के लिए मुख्य सुझाव है कि दैनिक नकद आय और उधार का अनुपात 80:20 रखें और पीएम मुद्रा योजना के तहत रियायती पूंजी हेतु आवेदन करें।"
            } else {
                "🌿 **SahayakAI (Offline Fallback):** Network is temporarily busy. For your business (${userProfile.businessType.title}), keep cash-to-credit ratio at 80:20 and explore Mudra Shishu for subsidized working capital."
            }
            return@withContext Pair(offlineFallback, true)
        }
    }

    private fun matchOfflineKnowledge(query: String, lang: AppLanguage): String? {
        val lowerQuery = query.lowercase()
        val matched = offlineKnowledgeBase.firstOrNull { item ->
            item.keywords.any { kw -> lowerQuery.contains(kw) }
        }
        return matched?.let { if (lang == AppLanguage.HINDI) it.answerHi else it.answerEn }
    }

    private fun buildInjectedContextPrompt(
        query: String,
        userProfile: UserProfile,
        ledger: List<LedgerEntry>,
        mandiPrices: List<MandiCommodity>
    ): String {
        val totalCredit = ledger.filter { it.type == LedgerType.CREDIT }.sumOf { it.amount }
        val totalDebit = ledger.filter { it.type == LedgerType.DEBIT }.sumOf { it.amount }
        val netSavings = totalCredit - totalDebit
        val pendingUdhaar = ledger.filter { it.type == LedgerType.DEBIT && it.category == com.example.data.model.LedgerCategory.CUSTOMER_UDHAAR }.sumOf { it.amount }

        val mandiSummary = mandiPrices.take(4).joinToString(", ") { "${it.name}: ₹${it.pricePerUnit}/${it.unit} (${it.trend})" }
        val mlContext = com.example.data.ml.MlForecastingEngine.generateGeminiPromptContext()

        return """
You are 'SahayakAI', an empathetic, highly knowledgeable, and practical rural business and financial advisor for Indian micro-entrepreneurs (street vendors, kirana store owners, artisans, dairy farmers, and SHG members).
Your mission is to help credit-invisible entrepreneurs build viable businesses, digitize their informal records, reduce bad debts, optimize wholesale inventory using machine learning price forecasts, and access government subsidies/bank credit (PM Mudra, PM SVANidhi, PMEGP, NRLM, Stand-Up India).

--- CURRENT USER CONTEXT (INJECTED REAL DATA) ---
Entrepreneur Name: ${userProfile.name}
Business Type: ${userProfile.businessType.title} (${userProfile.businessType.titleHi})
Location: ${userProfile.location}
Estimated Monthly Turnover: ₹${userProfile.monthlyTurnover}
Self-Help Group (SHG) / Community: ${userProfile.shgName}
Preferred Language: ${userProfile.preferredLanguage.name}

--- DIGITIZED LEDGER / KHATA SUMMARY ---
Total Recorded Inflow (Jama): ₹$totalCredit
Total Recorded Outflow (Kharch/Udhaar): ₹$totalDebit
Estimated Net Cash Balance: ₹$netSavings
Pending Customer Udhaar (Receivables): ₹$pendingUdhaar

--- RECENT MANDI / MARKET BENCHMARKS ---
$mandiSummary

$mlContext

--- USER QUERY ---
"$query"

--- RESPONSE GUIDELINES ---
1. Provide practical, high-impact, easy-to-understand advice tailored directly to their business (${userProfile.businessType.title}) in ${userProfile.location}.
2. Use the Machine Learning Demand & Price Forecasts above to advise them specifically on when to buy, how much to stock, and how to avoid price surges or seasonal margin losses.
3. If they ask about loans or funding, mention specific Indian government schemes (Mudra, SVANidhi, PMEGP) with clear eligibility and application steps.
4. If they ask about profit, pricing, or debt collection, give concrete numerical rules of thumb (e.g. 15% margin, 20% max udhaar).
5. Tone: Respectful, encouraging, clear, and culturally grounded. Use bullet points and bold highlights for easy visual scanning and text-to-speech audio clarity.
6. If the preferred language is HINDI, respond in polite, clear Hindi with easy business vocabulary. If ENGLISH or HINGLISH, respond in accessible English/Hinglish.
        """.trimIndent()
    }

    private fun callGeminiRest(prompt: String, apiKey: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val jsonRequest = JSONObject().apply {
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject().apply {
                put("text", prompt)
            }
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.6)
                put("topP", 0.95)
                put("topK", 40)
            }
            put("generationConfig", genConfig)
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            throw Exception("Gemini API error ${response.code}: $responseBodyString")
        }

        val root = JSONObject(responseBodyString)
        val candidates = root.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            if (parts != null && parts.length() > 0) {
                return parts.getJSONObject(0).optString("text", "No response text received.")
            }
        }
        return "No response received from advisor."
    }

    private data class OfflineAnswer(
        val keywords: List<String>,
        val answerHi: String,
        val answerEn: String
    )
}
