from __future__ import annotations

from fastapi import FastAPI

from web.app.routes import api_router

app = FastAPI(title="RadNav API", version="v1")
app.include_router(api_router, prefix="/api/v1")
