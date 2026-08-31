package com.example.data.service

data class PanDetails(
    val panNumber: String,
    val fullName: String,
    val dateOfBirth: String,
    val status: String,
    val isDemoData: Boolean = true
)

data class AadhaarDetails(
    val maskedAadhaar: String,
    val name: String,
    val address: String,
    val isVerified: Boolean,
    val isDemoData: Boolean = true
)

data class BankAccount(
    val accountId: String,
    val bankName: String,
    val accountNumberMasked: String,
    val ifscCode: String,
    val isAaLinked: Boolean,
    val avgMonthlyBalance: Double,
    val isDemoData: Boolean = true
)

data class CibilReport(
    val score: Int,
    val totalAccounts: Int,
    val activeLoans: Int,
    val onTimeRepaymentPercent: Double,
    val riskCategory: String,
    val isDemoData: Boolean = true
)

interface KycProvider {
    suspend fun verifyPan(panNumber: String): PanDetails
    suspend fun verifyAadhaar(aadhaarNumber: String, otp: String): AadhaarDetails
    suspend fun linkBank(accountId: String): BankAccount
    suspend fun fetchCibil(userId: String): CibilReport
}

class MockKycProvider : KycProvider {
    override suspend fun verifyPan(panNumber: String): PanDetails {
        val cleanPan = if (panNumber.isNotBlank()) panNumber.uppercase() else "ABCPS1234F"
        return PanDetails(
            panNumber = cleanPan,
            fullName = "RAMESH KUMAR SHARMA",
            dateOfBirth = "15/08/1988",
            status = "VALID_ACTIVE",
            isDemoData = true
        )
    }

    override suspend fun verifyAadhaar(aadhaarNumber: String, otp: String): AadhaarDetails {
        val last4 = if (aadhaarNumber.length >= 4) aadhaarNumber.takeLast(4) else "4892"
        return AadhaarDetails(
            maskedAadhaar = "XXXX-XXXX-$last4",
            name = "Ramesh Kumar Sharma",
            address = "Village Shivpur, District Varanasi, Uttar Pradesh - 221003",
            isVerified = otp.isNotBlank() && otp.length == 6,
            isDemoData = true
        )
    }

    override suspend fun linkBank(accountId: String): BankAccount {
        return BankAccount(
            accountId = accountId.ifBlank { "ACC-9048102" },
            bankName = "Bank of Baroda (Rural Shivpur Branch)",
            accountNumberMasked = "XXXX-XXXX-4819",
            ifscCode = "BARB0VASHIV",
            isAaLinked = true,
            avgMonthlyBalance = 14850.0,
            isDemoData = true
        )
    }

    override suspend fun fetchCibil(userId: String): CibilReport {
        return CibilReport(
            score = 742,
            totalAccounts = 3,
            activeLoans = 1, // e.g. PM SVANidhi or KCC loan
            onTimeRepaymentPercent = 96.5,
            riskCategory = "Low Risk (Good Standing)",
            isDemoData = true
        )
    }
}
