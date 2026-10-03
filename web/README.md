# Web Application

This package contains the RadNav web application layer for Sprint 2.

## Purpose

- Integrate with the versioned FastAPI API contract.
- Present a deployable application entrypoint.
- Keep web-specific code separate from connector and core packages.

## Contract expectations

The web layer is expected to consume the v1 API namespace defined in `docs/api/contract-v1.md`:

- `GET /api/v1/health`
- `GET /api/v1/status`
- `GET /api/v1/tasks`
- `POST /api/v1/tasks`
- `GET /api/v1/tasks/{task_id}`

## Local run

The package is structured so an ASGI server can mount `web.app.main:app`.
