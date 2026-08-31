package com.example.data.model

data class ScoreFactor(
    val title: String,
    val titleHi: String,
    val weightPercent: Int,
    val scoreOutOf100: Int,
    val status: String,
    val explanation: String,
    val explanationHi: String
)

data class FinancialHealthScore(
    val totalScore: Int, // 300 - 900
    val maxScore: Int = 900,
    val grade: String, // "A+ Bank Ready", "A Loan Eligible", "B Moderate", "C High Risk"
    val gradeHi: String,
    val summary: String,
    val summaryHi: String,
    val eligibleLoanAmountMax: Double,
    val factors: List<ScoreFactor>,
    val verifiedMonthlyInflow: Double,
    val verifiedMonthlyOutflow: Double,
    val netMonthlyProfit: Double,
    val udhaarRecoveryRate: Double,
    val cashRunwayDays: Int
)

data class BankReportSummary(
    val reportId: String = "SHK-${System.currentTimeMillis() % 1000000}",
    val applicantName: String,
    val businessType: String,
    val location: String,
    val totalVerifiedTurnoverAnnual: Double,
    val netProfitMarginPercent: Double,
    val creditScore: Int,
    val activeCreditTransactions: Int,
    val avgMonthlyTransactions: Int,
    val loanRecommendation: String,
    val generatedDate: String,
    val qrVerificationCode: String = "https://sahayak.gov.in/verify/demo-7389"
)
