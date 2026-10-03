# RadNav API Contract v1

This document defines the initial backend API contract for the Sprint 2 deployable RadNav output.

## Stability and versioning

- Base path: `/api/v1`
- All responses are JSON unless explicitly stated otherwise.
- Timestamps use ISO 8601 UTC strings, e.g. `2026-10-03T12:34:56Z`.
- IDs are opaque strings.

## Exported package boundaries

### `core` exported interfaces

The `core` package exports the domain and persistence surfaces that other packages may consume:

- `core.schemas`
  - request/response DTOs used by web and connector
  - Pydantic models only
- `core.models`
  - SQLAlchemy ORM models
- `core.services`
  - application service interfaces and implementations
- `core.repositories`
  - repository interfaces and DB-facing implementations
- `core.db`
  - SQLAlchemy engine/session factory helpers
- `core.errors`
  - shared exception types mapped to API responses

### `connector` exported interfaces

The `connector` package is standalone and may only consume the public `core` interfaces listed above.

Connector export surface:

- `connector.client`
  - connector client entrypoints
- `connector.schemas`
  - connector-specific request/response models
- `connector.version`
  - build/version metadata

### Import boundary rule

- `connector` must not import from internal modules under `core` that are not explicitly exported above.
- CI should fail if `connector` imports `core.models` internals directly outside the public contract.

## API endpoints

### 1) Health check

#### `GET /api/v1/health`

Used by web, deploy probes, and CI smoke checks.

##### Response 200

```json
{
  "status": "ok",
  "service": "radnav-backend",
  "version": "1.0.0",
  "environment": "development",
  "timestamp": "2026-10-03T12:34:56Z"
}
```

##### Fields

- `status`: string, always `ok` when healthy
- `service`: string, stable service name
- `version`: string, backend build version
- `environment`: string, deployment environment name
- `timestamp`: string, current server time in UTC

##### Errors

- `503 Service Unavailable` when the app is not ready to serve traffic.

---

### 2) Readiness check

#### `GET /api/v1/ready`

Used by deploy and orchestrators to determine readiness.

##### Response 200

```json
{
  "ready": true,
  "checks": {
    "database": "ok"
  }
}
```

##### Fields

- `ready`: boolean
- `checks.database`: string, `ok` when the database connection is healthy

##### Errors

- `503 Service Unavailable` when dependencies are unavailable.

---

### 3) Task list

#### `GET /api/v1/tasks`

Initial web-facing resource list endpoint.

##### Query parameters

- `limit` optional integer, default `20`, min `1`, max `100`
- `offset` optional integer, default `0`, min `0`
- `status` optional string filter, one of `open`, `in_progress`, `done`, `archived`

##### Response 200

```json
{
  "items": [
    {
      "id": "tsk_01JABCDEF1234567890",
      "title": "Review ingestion pipeline",
      "status": "open",
      "priority": "medium",
      "created_at": "2026-10-03T12:00:00Z",
      "updated_at": "2026-10-03T12:10:00Z"
    }
  ],
  "limit": 20,
  "offset": 0,
  "total": 1
}
```

##### Item fields

- `id`: string, opaque task identifier
- `title`: string, human-readable summary
- `status`: string, one of `open`, `in_progress`, `done`, `archived`
- `priority`: string, one of `low`, `medium`, `high`, `critical`
- `created_at`: string, UTC timestamp
- `updated_at`: string, UTC timestamp

##### Pagination fields

- `items`: array of task objects
- `limit`: integer, echoed request limit
- `offset`: integer, echoed request offset
- `total`: integer, total matching records

---

### 4) Task detail

#### `GET /api/v1/tasks/{task_id}`

##### Path parameters

- `task_id`: string, opaque task identifier

##### Response 200

```json
{
  "id": "tsk_01JABCDEF1234567890",
  "title": "Review ingestion pipeline",
  "description": "Validate the ingestion flow before release.",
  "status": "open",
  "priority": "medium",
  "created_at": "2026-10-03T12:00:00Z",
  "updated_at": "2026-10-03T12:10:00Z"
}
```

##### Errors

- `404 Not Found` when the task does not exist.

---

### 5) Create task

#### `POST /api/v1/tasks`

Used by web to create the initial resource.

##### Request body

```json
{
  "title": "Review ingestion pipeline",
  "description": "Validate the ingestion flow before release.",
  "priority": "medium"
}
```

##### Request fields

- `title`: string, required, min length `1`, max length `200`
- `description`: string, optional, max length `5000`
- `priority`: string, optional, default `medium`, one of `low`, `medium`, `high`, `critical`

##### Response 201

```json
{
  "id": "tsk_01JABCDEF1234567890",
  "title": "Review ingestion pipeline",
  "description": "Validate the ingestion flow before release.",
  "status": "open",
  "priority": "medium",
  "created_at": "2026-10-03T12:00:00Z",
  "updated_at": "2026-10-03T12:00:00Z"
}
```

##### Errors

- `400 Bad Request` for validation failures.
- `409 Conflict` when a duplicate or conflicting resource is detected.

---

### 6) Update task status

#### `PATCH /api/v1/tasks/{task_id}`

Used by web to update task status without replacing the full resource.

##### Request body

```json
{
  "status": "in_progress"
}
```

##### Request fields

- `status`: string, required, one of `open`, `in_progress`, `done`, `archived`

##### Response 200

```json
{
  "id": "tsk_01JABCDEF1234567890",
  "title": "Review ingestion pipeline",
  "description": "Validate the ingestion flow before release.",
  "status": "in_progress",
  "priority": "medium",
  "created_at": "2026-10-03T12:00:00Z",
  "updated_at": "2026-10-03T12:20:00Z"
}
```

##### Errors

- `400 Bad Request` for validation failures.
- `404 Not Found` when the task does not exist.

## Shared schema package contract

The following schema objects are the public cross-package DTOs that web and connector may import from `core.schemas`.

### `core.schemas.health`

- `HealthResponse`
- `ReadyResponse`

### `core.schemas.task`

- `TaskCreateRequest`
- `TaskUpdateRequest`
- `TaskResponse`
- `TaskListResponse`
- `TaskStatus`
- `TaskPriority`

### `core.schemas.common`

- `ErrorResponse`
- `PaginationMeta`

## Error response shape

All non-2xx API errors should use the same JSON shape.

```json
{
  "error": {
    "code": "not_found",
    "message": "Task not found",
    "details": {
      "task_id": "tsk_01JABCDEF1234567890"
    }
  }
}
```

### Error fields

- `error.code`: machine-readable string
- `error.message`: human-readable message
- `error.details`: optional object with validation or context data

### Standard error codes

- `validation_error`
- `not_found`
- `conflict`
- `unauthorized`
- `forbidden`
- `internal_error`
- `service_unavailable`

## Web integration notes

- The web app should call only the `/api/v1` endpoints defined above.
- Web should treat `GET /api/v1/health` as a liveness probe and `GET /api/v1/ready` as a readiness probe.
- Web should use `TaskListResponse` and `TaskResponse` as the canonical data shapes for UI state.
- Web should not duplicate schema definitions; it should import shared DTOs from `core.schemas`.

## Initial Sprint 2 skeleton scope

The initial deployable skeleton must include:

- FastAPI app with `/api/v1/health` and `/api/v1/ready`
- Task endpoints listed above, backed by SQLAlchemy 2 models
- Alembic migration scaffolding for the task table
- SQLite dev/test configuration and PostgreSQL production config hooks
- standalone connector package with its own version metadata and Dockerfile
- CI import-boundary checks for connector-to-core restrictions