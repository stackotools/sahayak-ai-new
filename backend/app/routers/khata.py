from fastapi import APIRouter
from pydantic import BaseModel
from typing import List

router = APIRouter()

class OcrParseRequest(BaseModel):
    raw_text: str

class OcrItem(BaseModel):
    party_name: str
    description: str
    amount: float
    type: str  # CREDIT or DEBIT
    category: str
    confidence: float

@router.post("/parse-ocr", response_model=List[OcrItem])
def parse_ocr(req: OcrParseRequest):
    return [
        OcrItem(
            party_name="Gupta Ji Kirana",
            description="Atta, Dal & Oil monthly supplies",
            amount=850.0,
            type="DEBIT",
            category="CUSTOMER_UDHAAR",
            confidence=0.94
        ),
        OcrItem(
            party_name="Daily Counter Sales",
            description="Day cash collection",
            amount=2450.0,
            type="CREDIT",
            category="SALES",
            confidence=0.97
        )
    ]
