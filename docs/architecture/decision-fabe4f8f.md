# Architecture Decision: Python/FastAPI Monorepo with Enforced Package Boundaries

## Approach
Adopt a Python 3.12 monorepo with `core/`, `connector/`, `web/`, `deploy/`, and `docs/`. Use FastAPI + SQLAlchemy 2 + Alembic for the backend, Postgres in production, SQLite only for local dev/tests. Keep the connector independently buildable/shippable with its own Dockerfile and version tag.

## Boundary Rules
- `core`: domain models, schema packages, services, DB access, migrations.
- `connector`: only depends on exported `core` interfaces/contracts; no imports from `core` internals.
- `web`: consumes the API contract, not ORM or connector internals.
- Enforce via CI import checks and package export lists.

## Tradeoffs
This avoids duplicated backend stacks and keeps Sprint 2 deployable. The main cost is stricter interface discipline, but it prevents tight coupling and preserves connector independence.

## Implementation Notes
Start with a minimal skeleton in each package plus shared contract/schema modules; keep `README.md`, `docs/`, and `deploy/` aligned with the exported boundaries.