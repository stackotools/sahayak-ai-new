from abc import ABC, abstractmethod
from typing import Dict, Any

class KycProvider(ABC):
    @abstractmethod
    def verify_pan(self, pan_number: str) -> Dict[str, Any]:
        pass

    @abstractmethod
    def verify_aadhaar(self, aadhaar_number: str, otp: str) -> Dict[str, Any]:
        pass

    @abstractmethod
    def link_bank(self, account_id: str) -> Dict[str, Any]:
        pass

    @abstractmethod
    def fetch_cibil(self, user_id: str) -> Dict[str, Any]:
        pass

class MockKycProvider(KycProvider):
    def verify_pan(self, pan_number: str) -> Dict[str, Any]:
        clean_pan = pan_number.upper() if pan_number else "ABCPS1234F"
        return {
            "pan_number": clean_pan,
            "full_name": "RAMESH KUMAR SHARMA",
            "date_of_birth": "15/08/1988",
            "status": "VALID_ACTIVE",
            "is_demo_data": True
        }

    def verify_aadhaar(self, aadhaar_number: str, otp: str) -> Dict[str, Any]:
        last4 = aadhaar_number[-4:] if len(aadhaar_number) >= 4 else "4892"
        return {
            "masked_aadhaar": f"XXXX-XXXX-{last4}",
            "name": "Ramesh Kumar Sharma",
            "address": "Village Shivpur, District Varanasi, Uttar Pradesh - 221003",
            "is_verified": len(otp) == 6,
            "is_demo_data": True
        }

    def link_bank(self, account_id: str) -> Dict[str, Any]:
        return {
            "account_id": account_id or "ACC-9048102",
            "bank_name": "Bank of Baroda (Rural Shivpur Branch)",
            "account_number_masked": "XXXX-XXXX-4819",
            "ifsc_code": "BARB0VASHIV",
            "is_aa_linked": True,
            "avg_monthly_balance": 14850.0,
            "is_demo_data": True
        }

    def fetch_cibil(self, user_id: str) -> Dict[str, Any]:
        return {
            "score": 742,
            "total_accounts": 3,
            "active_loans": 1,
            "on_time_repayment_percent": 96.5,
            "risk_category": "Low Risk (Good Standing)",
            "is_demo_data": True
        }
