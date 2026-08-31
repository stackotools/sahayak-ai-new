from fastapi import APIRouter
from app.services.kyc_provider import MockKycProvider
from pydantic import BaseModel

router = APIRouter()
kyc_provider = MockKycProvider()

class PanRequest(BaseModel):
    pan_number: str = "ABCPS1234F"

class AadhaarRequest(BaseModel):
    aadhaar_number: str = "987654324892"
    otp: str = "123456"

class BankLinkRequest(BaseModel):
    account_id: str = "ACC-9048102"

@router.post("/verify-pan")
def verify_pan(req: PanRequest):
    return kyc_provider.verify_pan(req.pan_number)

@router.post("/verify-aadhaar")
def verify_aadhaar(req: AadhaarRequest):
    return kyc_provider.verify_aadhaar(req.aadhaar_number, req.otp)

@router.post("/link-bank")
def link_bank(req: BankLinkRequest):
    return kyc_provider.link_bank(req.account_id)

@router.get("/cibil/{user_id}")
def fetch_cibil(user_id: str):
    return kyc_provider.fetch_cibil(user_id)
