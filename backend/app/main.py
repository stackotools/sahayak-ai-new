from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers import advisor, khata, schemes, kyc

app = FastAPI(
    title="SahayakAI Backend API",
    description="Rural Business & Financial Advisory Backend (SIH 2026 Problem Statement SIH26091)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(advisor.router, prefix="/api/v1/advisor", tags=["AI Advisor"])
app.include_router(khata.router, prefix="/api/v1/khata", tags=["Khata & OCR"])
app.include_router(schemes.router, prefix="/api/v1/schemes", tags=["Government Schemes"])
app.include_router(kyc.router, prefix="/api/v1/kyc", tags=["KYC & Bank Sandbox"])

@app.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "SahayakAI Backend",
        "sih_team": "SIH26091 - Smart India Hackathon 2026",
        "gemini_pipeline": "Active"
    }
