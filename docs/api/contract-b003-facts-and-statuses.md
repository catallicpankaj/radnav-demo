# B-003 API Contract: Append-only Facts and Candidate Statuses

## Overview

This contract defines the backend API for the B-003 append-only fact model with controlled predicates. It supports creating facts, superseding prior facts via chain-based references, and creating candidate statuses only.

### Core rules

- Facts are append-only. No update or delete endpoints are provided.
- A new fact may reference a prior fact using `supersedes_id`.
- Each fact must include `actor` and `recorded_at`.
- Each fact must include either `evidence_text` or `evidence_ref`.
- `predicate` must be one of the registered vocabulary entries.
- Unknown predicates are rejected with `400`.
- Status creation is limited to `candidate` only.
- Authenticated users provide the resolver/actor identity for server-side attribution.

---

## Common JSON shapes

### Fact object

```json
{
  "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
  "predicate": "customer.email_verified",
  "value": "true",
  "actor": {
    "type": "user",
    "id": "usr_123",
    "display_name": "Pankaj Sharma"
  },
  "recorded_at": "2026-10-03T12:34:56Z",
  "evidence_text": "User confirmed email via verification link.",
  "evidence_ref": null,
  "supersedes_id": null,
  "status": "candidate",
  "created_at": "2026-10-03T12:34:56Z"
}
```

### Candidate status object

```json
{
  "status_id": "status_01J9Z3R0A2E4M8D1P7Q9K5T6V3",
  "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
  "status": "candidate",
  "actor": {
    "type": "user",
    "id": "usr_123",
    "display_name": "Pankaj Sharma"
  },
  "recorded_at": "2026-10-03T12:34:56Z",
  "created_at": "2026-10-03T12:34:56Z"
}
```

### Error object

```json
{
  "error": {
    "code": "UNKNOWN_PREDICATE",
    "message": "predicate is not registered",
    "details": {
      "predicate": "customer.unknown_field"
    }
  }
}
```

---

## Endpoint: Register predicate

Registers a predicate in the controlled vocabulary.

### `POST /v1/predicates`

#### Request body

```json
{
  "predicate": "customer.email_verified",
  "description": "Whether the customer email has been verified",
  "value_type": "boolean",
  "enabled": true
}
```

#### Request fields

- `predicate` string, required, unique identifier for the predicate.
- `description` string, optional, human-readable explanation.
- `value_type` string, required, one of:
  - `string`
  - `number`
  - `boolean`
  - `json`
  - `date`
  - `datetime`
- `enabled` boolean, optional, defaults to `true`.

#### Response `201`

```json
{
  "predicate": "customer.email_verified",
  "description": "Whether the customer email has been verified",
  "value_type": "boolean",
  "enabled": true,
  "registered_at": "2026-10-03T12:34:56Z"
}
```

#### Errors

- `409 CONFLICT` if the predicate is already registered.
- `400 BAD REQUEST` if the schema is invalid.

---

## Endpoint: List registered predicates

### `GET /v1/predicates`

#### Response `200`

```json
{
  "items": [
    {
      "predicate": "customer.email_verified",
      "description": "Whether the customer email has been verified",
      "value_type": "boolean",
      "enabled": true,
      "registered_at": "2026-10-03T12:34:56Z"
    }
  ]
}
```

---

## Endpoint: Create fact

Creates an append-only fact row.

### `POST /v1/facts`

#### Authentication

Required. The authenticated user is used as the resolver/actor identity unless an administrative system integration supplies a server-trusted actor mapping.

#### Request body

```json
{
  "predicate": "customer.email_verified",
  "value": true,
  "recorded_at": "2026-10-03T12:34:56Z",
  "evidence_text": "User confirmed email via verification link.",
  "evidence_ref": null,
  "supersedes_id": null
}
```

#### Request fields

- `predicate` string, required. Must match a registered predicate.
- `value` string|number|boolean|object|array|null, required. Must conform to the predicate’s registered `value_type`.
- `recorded_at` string, required, RFC 3339 timestamp with timezone.
- `evidence_text` string|null, optional.
- `evidence_ref` string|null, optional.
- `supersedes_id` string|null, optional. If present, must reference an existing fact.

#### Validation rules

- At least one of `evidence_text` or `evidence_ref` must be provided.
- `recorded_at` must include timezone information.
- `supersedes_id`, when provided, must reference an existing fact row.
- The new row must not modify or overwrite prior rows.
- Unknown predicates are rejected.
- If the predicate is disabled, reject the write with `400`.

#### Response `201`

```json
{
  "fact": {
    "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
    "predicate": "customer.email_verified",
    "value": true,
    "actor": {
      "type": "user",
      "id": "usr_123",
      "display_name": "Pankaj Sharma"
    },
    "recorded_at": "2026-10-03T12:34:56Z",
    "evidence_text": "User confirmed email via verification link.",
    "evidence_ref": null,
    "supersedes_id": null,
    "status": "candidate",
    "created_at": "2026-10-03T12:34:56Z"
  }
}
```

#### Error responses

##### `400 BAD REQUEST`

Examples:

```json
{
  "error": {
    "code": "INVALID_FACT_REQUEST",
    "message": "either evidence_text or evidence_ref is required"
  }
}
```

```json
{
  "error": {
    "code": "INVALID_FACT_REQUEST",
    "message": "recorded_at must be an RFC 3339 timestamp with timezone"
  }
}
```

##### `400 BAD REQUEST` unknown predicate

```json
{
  "error": {
    "code": "UNKNOWN_PREDICATE",
    "message": "predicate is not registered",
    "details": {
      "predicate": "customer.unknown_field"
    }
  }
}
```

##### `404 NOT FOUND`

Returned when `supersedes_id` references a fact that does not exist.

```json
{
  "error": {
    "code": "FACT_NOT_FOUND",
    "message": "supersedes_id does not reference an existing fact"
  }
}
```

##### `409 CONFLICT`

Returned when append constraints fail due to concurrent chain rules.

```json
{
  "error": {
    "code": "FACT_CHAIN_CONFLICT",
    "message": "supersedes_id is not the current tip of the chain"
  }
}
```

---

## Endpoint: Get fact

### `GET /v1/facts/{fact_id}`

Returns a single fact row.

#### Response `200`

```json
{
  "fact": {
    "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
    "predicate": "customer.email_verified",
    "value": true,
    "actor": {
      "type": "user",
      "id": "usr_123",
      "display_name": "Pankaj Sharma"
    },
    "recorded_at": "2026-10-03T12:34:56Z",
    "evidence_text": "User confirmed email via verification link.",
    "evidence_ref": null,
    "supersedes_id": null,
    "status": "candidate",
    "created_at": "2026-10-03T12:34:56Z"
  }
}
```

#### Error `404`

```json
{
  "error": {
    "code": "FACT_NOT_FOUND",
    "message": "fact not found"
  }
}
```

---

## Endpoint: List facts

### `GET /v1/facts`

Optional filters:

- `predicate`
- `status`
- `supersedes_id`
- `actor_id`

#### Response `200`

```json
{
  "items": [
    {
      "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
      "predicate": "customer.email_verified",
      "value": true,
      "actor": {
        "type": "user",
        "id": "usr_123",
        "display_name": "Pankaj Sharma"
      },
      "recorded_at": "2026-10-03T12:34:56Z",
      "evidence_text": "User confirmed email via verification link.",
      "evidence_ref": null,
      "supersedes_id": null,
      "status": "candidate",
      "created_at": "2026-10-03T12:34:56Z"
    }
  ],
  "next_cursor": null
}
```

---

## Endpoint: Create candidate status

Creates a candidate status record for B-006 dependent workflows. AI may only create candidate status.

### `POST /v1/facts/{fact_id}/statuses`

#### Authentication

Required.

#### Request body

```json
{
  "status": "candidate",
  "recorded_at": "2026-10-03T12:34:56Z",
  "evidence_text": "AI suggests this fact should be reviewed.",
  "evidence_ref": null
}
```

#### Request fields

- `status` string, required, must be exactly `candidate`.
- `recorded_at` string, required, RFC 3339 timestamp with timezone.
- `evidence_text` string|null, optional.
- `evidence_ref` string|null, optional.

#### Validation rules

- Only `candidate` is permitted.
- At least one of `evidence_text` or `evidence_ref` must be provided.
- The `fact_id` must exist.
- If invoked by AI, the server must reject any non-`candidate` status with `403` or `400` depending on policy.
- Forward promotion is not performed here; it is handled by downstream workflow rules.

#### Response `201`

```json
{
  "status": {
    "status_id": "status_01J9Z3R0A2E4M8D1P7Q9K5T6V3",
    "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
    "status": "candidate",
    "actor": {
      "type": "user",
      "id": "usr_123",
      "display_name": "Pankaj Sharma"
    },
    "recorded_at": "2026-10-03T12:34:56Z",
    "evidence_text": "AI suggests this fact should be reviewed.",
    "evidence_ref": null,
    "created_at": "2026-10-03T12:34:56Z"
  }
}
```

#### Error `400`

```json
{
  "error": {
    "code": "INVALID_STATUS_REQUEST",
    "message": "only candidate status is permitted"
  }
}
```

#### Error `404`

```json
{
  "error": {
    "code": "FACT_NOT_FOUND",
    "message": "fact not found"
  }
}
```

---

## Endpoint: Get status history for a fact

### `GET /v1/facts/{fact_id}/statuses`

#### Response `200`

```json
{
  "items": [
    {
      "status_id": "status_01J9Z3R0A2E4M8D1P7Q9K5T6V3",
      "fact_id": "fact_01J9Z3Q7N6W8Y7K2D8X4W6A9Q1",
      "status": "candidate",
      "actor": {
        "type": "user",
        "id": "usr_123",
        "display_name": "Pankaj Sharma"
      },
      "recorded_at": "2026-10-03T12:34:56Z",
      "evidence_text": "AI suggests this fact should be reviewed.",
      "evidence_ref": null,
      "created_at": "2026-10-03T12:34:56Z"
    }
  ]
}
```

---

## Endpoint: Get predicate definition

### `GET /v1/predicates/{predicate}`

#### Response `200`

```json
{
  "predicate": "customer.email_verified",
  "description": "Whether the customer email has been verified",
  "value_type": "boolean",
  "enabled": true,
  "registered_at": "2026-10-03T12:34:56Z"
}
```

#### Error `404`

```json
{
  "error": {
    "code": "PREDICATE_NOT_FOUND",
    "message": "predicate is not registered"
  }
}
```

---

## Data validation details

### `recorded_at`

- Must be an RFC 3339 timestamp.
- Must include timezone offset or `Z`.
- The API treats the value as an instant in time.

### `actor`

Returned as a server-resolved object:

```json
{
  "type": "user",
  "id": "usr_123",
  "display_name": "Pankaj Sharma"
}
```

`type` may be one of:

- `user`
- `service`

### `evidence_text` and `evidence_ref`

At least one is required.

- `evidence_text`: free-form human-readable evidence.
- `evidence_ref`: stable external reference such as a document ID, URL, or attachment key.

### `supersedes_id`

When present, the new fact becomes the next row in the revision chain and must preserve the predicate lineage.

---

## Dependency notes

- B-004 conflict handling must consume this fact chain model and use authenticated user identity for resolver attribution.
- B-006 status progression must only advance from this append-only chain and must not allow AI to create any status other than `candidate`.

---

## Minimal implementation notes

- No `PATCH` or `DELETE` endpoints should be added for facts or statuses.
- The backend should never mutate an existing fact row.
- Any revision must be represented as a fresh row with `supersedes_id`.
