from __future__ import annotations

from datetime import datetime, timezone
from typing import Annotated
from uuid import UUID, uuid4

from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field

api_router = APIRouter()


class HealthResponse(BaseModel):
    status: str = Field(default="ok")
    service: str = Field(default="radnav-web")


class StatusResponse(BaseModel):
    api_version: str = Field(default="v1")
    service: str = Field(default="radnav-web")
    timestamp: datetime


class TaskCreate(BaseModel):
    title: str = Field(min_length=1, max_length=200)
    description: str | None = Field(default=None, max_length=2000)


class TaskRead(TaskCreate):
    id: UUID
    state: str = Field(default="queued")


_TASKS: list[TaskRead] = []


@api_router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse()


@api_router.get("/status", response_model=StatusResponse)
def status_view() -> StatusResponse:
    return StatusResponse(timestamp=datetime.now(timezone.utc))


@api_router.get("/tasks", response_model=list[TaskRead])
def list_tasks() -> list[TaskRead]:
    return _TASKS


@api_router.post("/tasks", response_model=TaskRead, status_code=status.HTTP_201_CREATED)
def create_task(payload: TaskCreate) -> TaskRead:
    task = TaskRead(id=uuid4(), **payload.model_dump())
    _TASKS.append(task)
    return task


@api_router.get("/tasks/{task_id}", response_model=TaskRead)
def get_task(task_id: UUID) -> TaskRead:
    for task in _TASKS:
        if task.id == task_id:
            return task
    raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Task not found")
