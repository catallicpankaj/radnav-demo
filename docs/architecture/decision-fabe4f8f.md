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

## Decision: Append-only fact model with controlled predicates
Adopt an append-only fact model for B-003 with chain-based supersession, evidence capture, and controlled predicate registration. Use authenticated users for resolver identity in conflict handling, and allow only forward status promotion with AI limited to candidate creation.

- Append-only facts; no updates or deletes
- New fact rows must link to prior rows via `supersedes_id`
- Include `actor` and `recorded_at` on each row
- Include `evidence_text` or `evidence_ref` on each row
- Predicates must be registered in a controlled vocabulary
- Unknown predicates must be rejected
- B-004 conflict handling depends on B-003
- B-006 status progression depends on B-003
- AI output can only create candidate status

## Open Questions
- Exact `recorded_at` schema details
- Auth decision for resolver identity
- Accessibility labels and colours specifics

## Discussion Thread
- Raised by: Pankaj Sharma
- Resolved by: 1/2 votes
- Pod: TestNewFeatures
- Mission: Test the new features from this demo account

(no discussion messages)

## Implementation Notes for B-003
Implement the append-only fact storage flow with `supersedes_id` chaining, immutable rows, `actor`/`recorded_at` fields, and rejection of unknown predicates.