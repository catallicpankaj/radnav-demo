from __future__ import annotations

from datetime import datetime, timezone
from typing import Annotated, Any
from uuid import UUID, uuid4

from fastapi import APIRouter, HTTPException, Query, status
from pydantic import BaseModel, Field, model_validator

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


class TaskListResponse(BaseModel):
    items: list[TaskRead]
    total: int
    limit: int
    offset: int


class SessionQuestionBlock(BaseModel):
    id: str
    role: str
    prompt: str
    kind: str = Field(default="question")


class SessionNextBlockResponse(BaseModel):
    session_id: UUID
    next_block: SessionQuestionBlock | None


class ArtifactFact(BaseModel):
    key: str
    value: Any
    status: str
    confidence: float = Field(ge=0.0, le=1.0)
    source: str


class ArtifactResponse(BaseModel):
    name: str
    facts: list[ArtifactFact]


class ConnectorIngestMetadata(BaseModel):
    version: str = Field(min_length=1)
    site_id: str = Field(min_length=1)
    token: str = Field(min_length=1)

    @model_validator(mode="after")
    def validate_signed_token(self) -> "ConnectorIngestMetadata":
        if not self.token.startswith(f"{self.site_id}:"):
            raise ValueError("Invalid site token")
        return self


class ConnectorIngestRequest(BaseModel):
    metadata: ConnectorIngestMetadata
    messages: list[Any] | None = None

    @model_validator(mode="after")
    def reject_message_bodies(self) -> "ConnectorIngestRequest":
        if self.messages:
            raise ValueError("Message bodies are not allowed")
        return self


class ConnectorIngestResponse(BaseModel):
    accepted: bool = Field(default=True)
    version: str
    site_id: str


_TASKS: list[TaskRead] = []
_SESSIONS: dict[UUID, SessionQuestionBlock | None] = {}
_ARTIFACTS: dict[str, ArtifactResponse] = {}


@api_router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse()


@api_router.get("/status", response_model=StatusResponse)
def status_view() -> StatusResponse:
    return StatusResponse(timestamp=datetime.now(timezone.utc))


@api_router.get("/tasks", response_model=TaskListResponse)
def list_tasks(
    limit: Annotated[int, Query(ge=1, le=100)] = 20,
    offset: Annotated[int, Query(ge=0)] = 0,
    state: Annotated[str | None, Query()] = None,
) -> TaskListResponse:
    tasks = _TASKS
    if state is not None:
        tasks = [task for task in tasks if task.state == state]
    total = len(tasks)
    items = tasks[offset : offset + limit]
    return TaskListResponse(items=items, total=total, limit=limit, offset=offset)


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


@api_router.get("/sessions/{session_id}/next-block", response_model=SessionNextBlockResponse)
def get_next_block(session_id: UUID) -> SessionNextBlockResponse:
    next_block = _SESSIONS.get(session_id)
    if session_id not in _SESSIONS:
        next_block = SessionQuestionBlock(id="default", role="default", prompt="Continue?")
        _SESSIONS[session_id] = next_block
    return SessionNextBlockResponse(session_id=session_id, next_block=next_block)


@api_router.get("/artifacts/{name}", response_model=ArtifactResponse)
def get_artifact(name: str) -> ArtifactResponse:
    artifact = _ARTIFACTS.get(name)
    if artifact is not None:
        return artifact
    return ArtifactResponse(
        name=name,
        facts=[
            ArtifactFact(
                key="status",
                value="unknown",
                status="unknown",
                confidence=0.0,
                source="server",
            )
        ],
    )


@api_router.post("/connector/ingest", response_model=ConnectorIngestResponse)
def connector_ingest(payload: ConnectorIngestRequest) -> ConnectorIngestResponse:
    return ConnectorIngestResponse(
        version=payload.metadata.version,
        site_id=payload.metadata.site_id,
    )