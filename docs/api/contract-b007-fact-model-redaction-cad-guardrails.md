# B-007 Fact Model with Redaction and CAD Guardrails — API Contract

This contract defines the backend API surface required to support the B-007 decision:
append-only facts, superseding chains, evidence fields, registry-backed predicates,
pre-extraction redaction, and CAD guardrails.

## Common Conventions

### Content Type
- All request and response bodies use `application/json`.

### Authentication / Resolver Identity
- Not finalized by the decision. Until a product-wide auth scheme is selected, the API
  MUST accept an opaque resolver identity in the request header:
  - `X-Resolver-Id: <string>`
- The backend MUST treat this as the recorded resolver identity for the operation.
- If the header is missing, return `401 Unauthorized`.

### Error Envelope
All non-2xx responses MUST use the following shape:

```json
{
  "error": {
    "code": "string",
    "message": "string",
    "details": {}
  }
}
```

- `error.code`: stable machine-readable code.
- `error.message`: human-readable explanation.
- `error.details`: object for field-level or policy-specific context; may be empty.

### Recorded Timestamp
The `recorded_at` field MUST be an RFC 3339 timestamp with timezone offset, for example:
`2026-10-03T14:25:43Z` or `2026-10-03T14:25:43.123Z`.

The backend MUST preserve the submitted `recorded_at` value exactly as provided if valid.

---

## 1) Ingest Redacted Transcript for Fact Extraction

### POST `/v1/facts/ingest`

Creates an ingestion record using a redacted transcript only. Raw transcripts MUST NOT be stored.
Raw transcript content may exist only in memory during the redaction step.

#### Request Body
```json
{
  "source_ref": "string",
  "recorded_at": "2026-10-03T14:25:43Z",
  "redacted_transcript": "string",
  "resolver_id": "string",
  "metadata": {
    "any": "object"
  }
}
```

#### Request Field Rules
- `source_ref` required, non-empty string; opaque source identifier.
- `recorded_at` required; RFC 3339 timestamp with timezone.
- `redacted_transcript` required; MUST already be redacted.
- `resolver_id` required; identity of the actor/resolver.
- `metadata` optional arbitrary object.
- Raw transcript fields are not accepted.
- The server MUST NOT persist any raw transcript value.

#### Success Response `201 Created`
```json
{
  "ingestion_id": "ing_01JABCDEF1234567890",
  "source_ref": "string",
  "recorded_at": "2026-10-03T14:25:43Z",
  "redacted_transcript": "string",
  "redaction_status": "redacted",
  "created_at": "2026-10-03T14:25:44Z"
}
```

#### Error Responses
- `400 Bad Request` — malformed JSON or invalid timestamp.
- `401 Unauthorized` — missing resolver identity.
- `422 Unprocessable Entity` — invalid redaction state or unsupported payload.

Example `422` for attempted raw transcript submission:
```json
{
  "error": {
    "code": "raw_transcript_rejected",
    "message": "Raw transcripts are not accepted for storage. Submit redacted_transcript only.",
    "details": {
      "allowed_fields": ["source_ref", "recorded_at", "redacted_transcript", "resolver_id", "metadata"]
    }
  }
}
```

---

## 2) Extract Facts from a Redacted Transcript

### POST `/v1/facts/extract`

Runs extraction against a transcript. The server MUST run redaction before every extraction call and fail closed.
If redaction cannot be completed successfully, extraction MUST NOT proceed.

#### Request Body
```json
{
  "source_ref": "string",
  "recorded_at": "2026-10-03T14:25:43Z",
  "transcript": "string",
  "resolver_id": "string",
  "metadata": {
    "any": "object"
  }
}
```

#### Request Field Rules
- `source_ref` required.
- `recorded_at` required; RFC 3339 timestamp with timezone.
- `transcript` required; may be raw on input, but MUST exist only in memory until redaction completes.
- `resolver_id` required.
- `metadata` optional.

#### Behavior
1. The backend redacts `transcript` in memory.
2. If redaction succeeds, extraction runs on the redacted transcript.
3. Only the redacted transcript may be persisted.
4. Raw transcript MUST NOT be stored in any database, log, or event payload.

#### Success Response `200 OK`
```json
{
  "extraction_id": "ext_01JABCDEF1234567890",
  "source_ref": "string",
  "recorded_at": "2026-10-03T14:25:43Z",
  "redacted_transcript": "string",
  "facts": [
    {
      "fact_id": "fact_01JABCDEF1234567890",
      "subject_type": "person",
      "subject_id": "subj_123",
      "predicate": "registry_predicate_name",
      "object_type": "string",
      "object_value": "string",
      "evidence_text": "string",
      "evidence_ref": "string",
      "supersedes_id": null,
      "recorded_at": "2026-10-03T14:25:43Z"
    }
  ]
}
```

#### Failure on Redaction `422 Unprocessable Entity`
If redaction fails, return `422` and do not perform extraction.

```json
{
  "error": {
    "code": "redaction_failed",
    "message": "Transcript redaction failed; extraction was blocked.",
    "details": {
      "policy": "fail_closed"
    }
  }
}
```

---

## 3) Append-Only Fact Write

### POST `/v1/facts`

Writes an append-only fact row.
Facts are immutable once written. Corrections must be represented by a new fact row whose `supersedes_id` points to the prior row.

#### Request Body
```json
{
  "subject_type": "person",
  "subject_id": "subj_123",
  "predicate": "registry_predicate_name",
  "object_type": "string",
  "object_value": "string",
  "evidence_text": "string",
  "evidence_ref": "string",
  "supersedes_id": null,
  "recorded_at": "2026-10-03T14:25:43Z",
  "resolver_id": "string"
}
```

#### Request Field Rules
- `subject_type` required.
- `subject_id` required.
- `predicate` required and MUST exist in the predicate registry.
- `object_type` required.
- `object_value` required.
- `evidence_text` required.
- `evidence_ref` required.
- `supersedes_id` optional; use `null` for a new root fact.
- `recorded_at` required; RFC 3339 timestamp with timezone.
- `resolver_id` required.

#### Predicate Registry Validation
- The backend MUST reject any predicate not present in the registry.
- The backend MUST reject any predicate flagged as `clinical_output` or CAD output.

#### Success Response `201 Created`
```json
{
  "fact_id": "fact_01JABCDEF1234567890",
  "subject_type": "person",
  "subject_id": "subj_123",
  "predicate": "registry_predicate_name",
  "object_type": "string",
  "object_value": "string",
  "evidence_text": "string",
  "evidence_ref": "string",
  "supersedes_id": null,
  "recorded_at": "2026-10-03T14:25:43Z",
  "created_at": "2026-10-03T14:25:44Z",
  "status": "active"
}
```

#### Error Responses

##### Unknown Predicate `422 Unprocessable Entity`
```json
{
  "error": {
    "code": "predicate_not_registered",
    "message": "Predicate is not present in the registry.",
    "details": {
      "predicate": "unregistered_predicate_name"
    }
  }
}
```

##### CAD / Clinical Output Predicate `422 Unprocessable Entity`
```json
{
  "error": {
    "code": "cad_violation",
    "message": "CAD-related facts and outputs are blocked by policy.",
    "details": {
      "predicate": "registry_predicate_name",
      "reason": "predicate_marked_clinical_output"
    }
  }
}
```

##### Invalid Supersession `422 Unprocessable Entity`
```json
{
  "error": {
    "code": "invalid_supersedes_id",
    "message": "supersedes_id must reference an existing fact row or be null.",
    "details": {
      "supersedes_id": "fact_01JINVALID"
    }
  }
}
```

---

## 4) Reject CAD-Tagged Ingest Payloads

### POST `/v1/facts/validate`

Validates a fact-like payload before persistence. This endpoint is used by clients that need a preflight rejection reason.
It MUST reject any payload with `subject_type=cad_result`.

#### Request Body
```json
{
  "subject_type": "cad_result",
  "subject_id": "string",
  "predicate": "string",
  "object_type": "string",
  "object_value": "string",
  "evidence_text": "string",
  "evidence_ref": "string",
  "supersedes_id": null,
  "recorded_at": "2026-10-03T14:25:43Z",
  "resolver_id": "string"
}
```

#### Success Response `200 OK`
```json
{
  "valid": true
}
```

#### CAD Subject Rejection `422 Unprocessable Entity`
```json
{
  "error": {
    "code": "cad_violation",
    "message": "CAD-related facts and outputs are blocked by policy.",
    "details": {
      "subject_type": "cad_result",
      "reason": "subject_type_blocked"
    }
  }
}
```

#### Predicate CAD Rejection `422 Unprocessable Entity`
If the predicate is flagged as CAD output or clinical_output, return the same `cad_violation` code with the predicate detail.

```json
{
  "error": {
    "code": "cad_violation",
    "message": "CAD-related facts and outputs are blocked by policy.",
    "details": {
      "predicate": "registry_predicate_name",
      "reason": "predicate_marked_cad_output"
    }
  }
}
```

---

## 5) Fact Retrieval for UI / Audit

### GET `/v1/facts/{fact_id}`

Retrieves a single fact row, including supersession metadata.

#### Success Response `200 OK`
```json
{
  "fact_id": "fact_01JABCDEF1234567890",
  "subject_type": "person",
  "subject_id": "subj_123",
  "predicate": "registry_predicate_name",
  "object_type": "string",
  "object_value": "string",
  "evidence_text": "string",
  "evidence_ref": "string",
  "supersedes_id": null,
  "recorded_at": "2026-10-03T14:25:43Z",
  "created_at": "2026-10-03T14:25:44Z",
  "status": "active"
}
```

#### Error Responses
- `404 Not Found` if the fact does not exist.
- `401 Unauthorized` if resolver identity is missing on protected deployments.

---

## 6) Predicate Registry

### GET `/v1/predicates`

Returns the registry of supported predicates and flags used for validation.

#### Success Response `200 OK`
```json
{
  "predicates": [
    {
      "name": "registry_predicate_name",
      "description": "string",
      "clinical_output": false,
      "cad_output": false,
      "active": true
    }
  ]
}
```

#### Registry Rules
- Clients MUST use this endpoint or an equivalent registry source to discover allowed predicates.
- Any predicate with `clinical_output=true` or `cad_output=true` MUST be rejected on write.

---

## 7) UI Banner Contract for CAD-Blocked Screens

Any UI screen that can display fact ingestion, extraction, validation, or review results MUST render a permanent non-hideable banner when CAD guardrails are active.

### Banner Data Contract
```json
{
  "banner": {
    "type": "not_a_clinical_tool",
    "visible": true,
    "dismissible": false,
    "label": "Not a clinical tool"
  }
}
```

### UI Requirements
- The banner MUST not be dismissible.
- The banner MUST be present on relevant screens by default.
- Accessibility label, text color, and color palette specifics are not finalized by this decision and MUST be supplied by the product accessibility spec.

---

## 8) Shared Validation Matrix

| Condition | HTTP | error.code | message |
|---|---:|---|---|
| Missing resolver identity | 401 | `unauthorized` | Resolver identity is required. |
| Invalid JSON / timestamp | 400 | `bad_request` | Request is malformed. |
| Unknown predicate | 422 | `predicate_not_registered` | Predicate is not present in the registry. |
| Predicate marked clinical_output / CAD output | 422 | `cad_violation` | CAD-related facts and outputs are blocked by policy. |
| `subject_type=cad_result` | 422 | `cad_violation` | CAD-related facts and outputs are blocked by policy. |
| Redaction failure | 422 | `redaction_failed` | Transcript redaction failed; extraction was blocked. |
| Invalid supersedes reference | 422 | `invalid_supersedes_id` | supersedes_id must reference an existing fact row or be null. |

---

## 9) Implementation Notes

- Facts are append-only and must never be updated in place.
- A corrected fact MUST be written as a new row with `supersedes_id` referencing the prior row.
- Evidence fields are mandatory for fact writes.
- Raw transcripts MUST never be persisted.
- Redaction MUST always run before extraction and MUST fail closed.
- CAD-related subjects and outputs MUST be rejected with `422` and the `cad_violation` code.
