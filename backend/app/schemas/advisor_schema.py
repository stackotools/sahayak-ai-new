from pydantic import BaseModel
from typing import List, Optional

class UserProfileContext(BaseModel):
    name: str
    business_type: str
    location: str
    monthly_turnover: float
    shg_name: Optional[str] = None
    language: str = "hi"

class LedgerSummaryContext(BaseModel):
    total_inflow: float
    total_outflow: float
    net_savings: float
    pending_udhaar: float
    transaction_count: int

class AdvisorQueryRequest(BaseModel):
    query: str
    user_profile: UserProfileContext
    ledger_summary: LedgerSummaryContext
    force_offline: bool = False

class AdvisorQueryResponse(BaseModel):
    advice_text: str
    is_offline_tier: bool
    recommended_schemes: List[str] = []
    audio_tts_text: str
