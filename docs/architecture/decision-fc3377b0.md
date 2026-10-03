# RadNav Sprint 2: Monorepo and Package Boundaries

## Decision Summary
Adopt a Python 3.12 monorepo with `core/`, `connector/`, `web/`, `deploy/`, and `docs/`, using FastAPI + SQLAlchemy 2 + Alembic, Postgres in production, and SQLite only for local dev/tests.

## Architectural Fit
This is a sound fit for Sprint 2 because it keeps a single source of truth for shared domain logic while allowing the connector to remain independently buildable and versioned. The monorepo reduces duplication and simplifies CI/CD alignment without forcing runtime coupling.

## Package Boundaries
- `core/`: pure domain models, service interfaces, validation, and shared schema contracts.
- `connector/`: integration/adapters and any shippable connector entrypoints; may depend on `core`, but not `web`.
- `web/`: FastAPI app, HTTP routes, request/response translation, and API composition; may depend on `core`, not connector internals.
- `deploy/`: infra and release artifacts.
- `docs/`: architecture and contract docs.

Export only stable schema objects and service interfaces from `core`; avoid exporting persistence or transport internals.

## Boundary Enforcement
Use CI import-lint to block:
- `connector` importing `web`
- `web` importing connector internals
- any module importing private `core` internals
- cross-package relative imports outside declared public APIs

## Tradeoffs
This adds some upfront structure, but avoids later refactors and keeps connector shippability intact. The main constraint is disciplined public API curation in `core`.

## Implementation Notes
Arjun should create the initial skeleton with package markers, public API modules, Alembic wiring, and CI boundary checks first.