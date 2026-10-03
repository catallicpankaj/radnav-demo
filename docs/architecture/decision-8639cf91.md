# B-007: Fact model with redaction and CAD guardrails

Adopt the existing B-003 append-only fact model in `docs/api/contract-b003-facts-and-statuses.md` and the fact-store patterns already exercised under `integration-tests/.../AppendOnlyFactStoreIntegrationTest.java`. Store only redacted transcripts; raw text may exist transiently in memory during redaction and must never be persisted. Keep `evidence_text`, `evidence_ref`, `supersedes_id`, and registry-backed predicates; reject unknown predicates at ingest.

Add a pre-extraction redaction gate that fail-closes before every extraction call, plus CAD guardrails that reject `subject_type=cad_result` and `clinical_output`/CAD predicates with `422` and a clear message. Emit the non-hideable “not a clinical tool” banner in relevant web screens (`web/src/web/app/*`).

Tradeoff: tighter safety and no raw-audit replay; synthetic golden set covers tuning.