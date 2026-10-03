# RadNav API Contract v1

This document defines the initial stable HTTP API contract between the web application and the RadNav backend. It is intentionally small so the web and backend can integrate while the monorepo scaffold is still being built.

## Conventions

- Base path: `/api/v1`
- Content-Type: `application/json`
- Timestamps: RFC 3339 / ISO 8601 strings in UTC, for example `2026-10-03T12:34:56Z`
- IDs: opaque strings
- All responses use JSON
- Error shape is consistent across endpoints

## Shared response shapes

### ErrorResponse

```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": {}
  }
}
```

Fields:
- `error.code`: stable machine-readable error code
- `error.message`: human-readable summary
- `error.details`: optional object with endpoint-specific context; may be `{}`

### HealthResponse

```json
{
  "status": "ok",
  "service": "radnav-backend",
  "timestamp": "2026-10-03T12:34:56Z"
}
```

### VersionResponse

```json
{
  "service": "radnav-backend",
  "version": "0.1.0",
  "git_sha": "abc1234",
  "build_timestamp": "2026-10-03T12:34:56Z"
}
```

### ConnectorVersionResponse

```json
{
  "connector": {
    "name": "radnav-connector",
    "version": "0.1.0",
    "git_sha": "abc1234",
    "build_timestamp": "2026-10-03T12:34:56Z"
  }
}
```

## Endpoints

### GET `/api/v1/health`

Purpose: lightweight liveness check for web and deployment smoke checks.

Response: `200 OK`

```json
{
  "status": "ok",
  "service": "radnav-backend",
  "timestamp": "2026-10-03T12:34:56Z"
}
```

Error responses:
- `500 Internal Server Error` with `ErrorResponse` if backend cannot respond normally.

---

### GET `/api/v1/version`

Purpose: return backend build/version metadata for web footer/debug display and deployment verification.

Response: `200 OK`

```json
{
  "service": "radnav-backend",
  "version": "0.1.0",
  "git_sha": "abc1234",
  "build_timestamp": "2026-10-03T12:34:56Z"
}
```

Notes:
- `git_sha` may be an empty string if unavailable in local dev.
- `build_timestamp` may be `null` only if the build system cannot provide it; preferred value is an RFC 3339 string.

Error responses:
- `500 Internal Server Error` with `ErrorResponse`.

---

### GET `/api/v1/connector/version`

Purpose: expose the independently versioned connector package metadata so the web app can verify which connector build is deployed.

Response: `200 OK`

```json
{
  "connector": {
    "name": "radnav-connector",
    "version": "0.1.0",
    "git_sha": "abc1234",
    "build_timestamp": "2026-10-03T12:34:56Z"
  }
}
```

Error responses:
- `500 Internal Server Error` with `ErrorResponse`.

## Web integration rules

The web application may rely on the following contract guarantees:

1. `/api/v1/health` exists and returns `status: "ok"` for healthy instances.
2. `/api/v1/version` exists and returns backend version metadata.
3. `/api/v1/connector/version` exists and returns connector version metadata independently from backend version metadata.
4. The response field names in this document are stable for Sprint 2 integration.
5. New endpoints may be added under `/api/v1`, but these three endpoints should remain backward compatible.

## Backend implementation notes

- FastAPI should mount these routes under `/api/v1`.
- SQLAlchemy/Alembic schema work does not affect this contract.
- SQLite and PostgreSQL selection is an internal deployment concern and does not change these response shapes.
