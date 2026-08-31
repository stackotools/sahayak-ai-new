from fastapi import APIRouter
from typing import List, Dict, Any

router = APIRouter()

SCHEMES_DB = [
    {
        "id": "pm_mudra",
        "name": "PM Mudra Yojana (PMMY)",
        "ministry": "Ministry of Finance",
        "subsidy_percent": 0,
        "max_loan_amount": 1000000.0,
        "category": "CREDIT_LOAN",
        "description": "Collateral-free micro loans up to ₹10 Lakh for small business setup, shop expansion, and equipment."
    },
    {
        "id": "pmegp_scheme",
        "name": "PMEGP (Prime Minister Employment Generation)",
        "ministry": "Ministry of MSME & KVIC",
        "subsidy_percent": 35,
        "max_loan_amount": 5000000.0,
        "category": "MINORITY_SC_ST",
        "description": "Credit-linked subsidy programme offering up to 35% government subsidy for micro enterprises in rural areas."
    },
    {
        "id": "pm_svanidhi",
        "name": "PM SVANidhi (Street Vendor AtmaNirbhar)",
        "ministry": "Ministry of Housing & Urban Affairs",
        "subsidy_percent": 7,
        "max_loan_amount": 50000.0,
        "category": "STREET_VENDORS",
        "description": "Micro working capital collateral-free credit starting at ₹10,000 graduating to ₹20,000 and ₹50,000 on timely digital repayment."
    }
]

@router.get("/", response_model=List[Dict[str, Any]])
def list_schemes():
    return SCHEMES_DB
