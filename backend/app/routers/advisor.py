from fastapi import APIRouter
from app.schemas.advisor_schema import AdvisorQueryRequest, AdvisorQueryResponse
import os

router = APIRouter()

@router.post("/query", response_model=AdvisorQueryResponse)
def get_advisor_guidance(req: AdvisorQueryRequest):
    # Rule-based offline / fallback answers
    q = req.query.lower()
    if "mudra" in q or "मुद्रा" in q or "loan" in q:
        advice = (
            "🏦 **प्रधानमंत्री मुद्रा योजना (PMMY) सलाह:**\n\n"
            "1. **शिशु ऋण:** ₹50,000 तक (नया स्टॉक/टूल्स, कोई गारंटी नहीं)।\n"
            "2. **किशोर ऋण:** ₹50,000 से ₹5 लाख तक।\n"
            "3. **तरुण ऋण:** ₹5 लाख से ₹10 लाख तक।\n\n"
            "💡 अपने SahayakAI डिजिटल खाते का 'बैंक-रेडी प्रमाणपत्र' बैंक में प्रस्तुत करें।"
        )
        schemes = ["PM Mudra Yojana", "PMEGP"]
    else:
        advice = (
            f"🌿 **SahayakAI ग्रामीण सलाहकार:** आपके व्यवसाय ({req.user_profile.business_type}) "
            f"के लिए दैनिक जमा (₹{req.ledger_summary.total_inflow:,.0f}) और बकाया उधार का अनुपात सुरक्षित सीमा में रखें।"
        )
        schemes = ["PM Mudra Yojana", "PM SVANidhi"]

    return AdvisorQueryResponse(
        advice_text=advice,
        is_offline_tier=True,
        recommended_schemes=schemes,
        audio_tts_text=advice.replace("*", "")
    )
