# B-003 Append-Only Fact Model with Controlled Predicates

Adopt an append-only fact model as the source of truth. Each change creates a new row with `supersedes_id` pointing to the prior fact, plus `actor` and `recorded_at` for traceability. Store either `evidence_text` or `evidence_ref` per row. Predicates must be validated against a registered vocabulary; unknown predicates are rejected at write time.

## Fit and boundaries
This aligns with the existing boundary-driven layout in `connector/src/connector/core_boundary.py` and keeps B-003 as the canonical write model for downstream B-004 conflict handling and B-006 status progression. AI may only emit candidate facts/statuses; only authenticated users can resolve or promote status.

## Tradeoffs
Append-only chains improve auditability and reproducibility, but require chain traversal and careful validation of supersession links. This is preferable to mutable records, which would weaken traceability and complicate conflict logic.