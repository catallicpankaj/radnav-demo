<!-- API contract for AIWorkhive decision 1c5cdb8f-589c-4427-abf1-b2b5ba51178d: Newrun-PayPal Payment Microservice. Reconciled with the implementation; edit via the decision, not by hand. -->

**Summary**  
The *Newrun‑PayPal Payment Microservice* exposes a small, well‑defined HTTP API that lets callers (e.g., the e‑commerce front‑end or other internal services) create a PayPal payment, execute it after the payer approves it, query its status, and receive PayPal webhook notifications. All endpoints are **NEW**. The contract is expressed as endpoint + method + request body + response body (including HTTP status codes) so front‑end and back‑end teams can implement against the same shape.

---

## API Contract

| # | Method | Path | Description | Request Body | Success Response (200/201) | Error Response (4xx/5xx) |
|---|--------|------|-------------|--------------|----------------------------|--------------------------|
| 1 | **POST** | `/payments/paypal/create` | Create a new PayPal payment (order) and obtain an approval URL that the client redirects the buyer to. | `CreatePaymentRequest` | `CreatePaymentResponse` (201) | `ErrorResponse` (400, 500) |
| 2 | **POST** | `/payments/paypal/execute` | Execute a previously‑created payment after the payer approves it on PayPal. | `ExecutePaymentRequest` | `ExecutePaymentResponse` (200) | `ErrorResponse` (400, 404, 500) |
| 3 | **GET** | `/payments/paypal/status/{paymentId}` | Retrieve current status of a PayPal payment. | – (path param) | `PaymentStatusResponse` (200) | `ErrorResponse` (404, 500) |
| 4 | **POST** | `/payments/paypal/webhook` | Receive PayPal webhook events (e.g., `PAYMENT.SALE.COMPLETED`). This endpoint is called by PayPal; it must validate the webhook signature. | Raw PayPal webhook JSON (any) | `WebhookAckResponse` (200) | `ErrorResponse` (400, 500) |
| 5 | **POST** | `/payments/paypal/credentials` | Store or update PayPal client credentials (client‑id, secret). Intended for admin use only. | `CredentialsRequest` | `CredentialsResponse` (200) | `ErrorResponse` (400, 401, 500) |

All endpoints require an **Authorization** header with a bearer JWT (not modelled in the JSON schemas).  

---

### 1. Create Payment

**Request** – `CreatePaymentRequest`

```json
{
  "intent": "sale",                     // "sale" | "authorize" | "order"
  "payer": {
    "payment_method": "paypal"
  },
  "transactions": [
    {
      "amount": {
        "total": "49.99",
        "currency": "USD"
      },
      "description": "Order #12345 – Widgets",
      "item_list": {
        "items": [
          {
            "name": "Widget A",
            "sku": "WIDGET-A",
            "price": "19.99",
            "currency": "USD",
            "quantity": 1
          },
          {
            "name": "Widget B",
            "sku": "WIDGET-B",
            "price": "30.00",
            "currency": "USD",
            "quantity": 1
          }
        ]
      }
    }
  ],
  "redirect_urls": {
    "return_url": "https://example.com/paypal/return",
    "cancel_url": "https://example.com/paypal/cancel"
  }
}
```

**Success Response** – `CreatePaymentResponse` (HTTP 201)

```json
{
  "payment_id": "PAY-5XL12345AB6789012",
  "approval_url": "https://www.sandbox.paypal.com/cgi-bin/webscr?cmd=_express-checkout&token=EC-5XL12345AB6789012"
}
```

**Error Response** – `ErrorResponse`

```json
{
  "error": "invalid_request",
  "message": "The amount total does not match sum of items.",
  "details": null
}
```

---

### 2. Execute Payment

**Request** – `ExecutePaymentRequest`

```json
{
  "payment_id": "PAY-5XL12345AB6789012",
  "payer_id": "7E7JH12345KLM"
}
```

**Success Response** – `ExecutePaymentResponse` (HTTP 200)

```json
{
  "payment_id": "PAY-5XL12345AB6789012",
  "state": "approved",
  "transactions": [
    {
      "amount": {
        "total": "49.99",
        "currency": "USD"
      },
      "related_resources": [
        {
          "sale": {
            "id": "SALE-7J8K12345L9M",
            "state": "completed",
            "create_time": "2026-10-05T12:34:56Z",
            "update_time": "2026-10-05T12:35:10Z"
          }
        }
      ]
    }
  ]
}
```

**Error Response** – `ErrorResponse`

```json
{
  "error": "payment_not_found",
  "message": "Payment ID does not exist or has already been executed."
}
```

---

### 3. Get Payment Status

**Path Parameter**  
`paymentId` – the PayPal payment identifier (`PAY-…`).

**Success Response** – `PaymentStatusResponse` (HTTP 200)

```json
{
  "payment_id": "PAY-5XL12345AB6789012",
  "state": "created" | "approved" | "failed" | "canceled",
  "intent": "sale",
  "amount": {
    "total": "49.99",
    "currency": "USD"
  },
  "created_at": "2026-10-05T12:20:00Z",
  "updated_at": "2026-10-05T12:35:10Z"
}
```

**Error Response** – `ErrorResponse`

```json
{
  "error": "not_found",
  "message": "No payment found with ID PAY-xxxx"
}
```

---

### 4. Webhook Receiver

**Request** – Raw JSON sent by PayPal (example below). The service must verify the `PAYPAL-TRANSMISSION-ID`, `PAYPAL-TRANSMISSION-TIME`, `PAYPAL-CERT-URL`, `PAYPAL-AUTH-ALGO`, and `PAYPAL-TRANSMISSION-SIG` headers as per PayPal docs – not part of the payload contract.

**Example Payload** (one of many possible events)

```json
{
  "id": "WH-2WR12345AB6789CDE0",
  "event_version": "1.0",
  "create_time": "2026-10-05T12:36:00Z",
  "resource_type": "sale",
  "event_type": "PAYMENT.SALE.COMPLETED",
  "summary": "Payment completed for $49.99 USD",
  "resource": {
    "id": "SALE-7J8K12345L9M",
    "state": "completed",
    "amount": {
      "total": "49.99",
      "currency": "USD"
    },
    "payment_id": "PAY-5XL12345AB6789012",
    "payer": {
      "payer_info": {
        "email": "buyer@example.com",
        "payer_id": "7E7JH12345KLM"
      }
    }
  },
  "links": [...]
}
```

**Success Response** – `WebhookAckResponse` (HTTP 200)

```json
{
  "status": "received"
}
```

**Error Response** – `ErrorResponse`

```json
{
  "error": "invalid_signature",
  "message": "Webhook signature verification failed."
}
```

---

### 5. Store PayPal Credentials (Admin)

**Request** – `CredentialsRequest`

```json
{
  "client_id": "AQ6Axxxxxxxxxxxxxxxxxxxxx",
  "client_secret": "EJvxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
}
```

**Success Response** – `CredentialsResponse` (HTTP 200)

```json
{
  "message": "PayPal credentials updated successfully."
}
```

**Error Response** – `ErrorResponse`

```json
{
  "error": "unauthorized",
  "message": "Missing or invalid admin token."
}
```

---

## Common Types

**ErrorResponse**

```json
{
  "error": "<error_code>",
  "message": "<human readable description>",
  "details": "<optional free‑form detail>"
}
```

All timestamps are ISO‑8601 UTC strings. Monetary amounts are strings to preserve precision.

---

### Versioning & Extensibility

- Base path: `/payments/paypal` (all endpoints under this namespace).  
- Future versioning can be introduced as `/v1/payments/paypal/...` without breaking existing contracts.  

--- 

**All endpoints listed above are NEW.** Implementors should wire these routes to the PayPal SDK/mock layer, enforce JWT authentication, validate payloads against the schemas, and persist minimal state (payment ID, status, timestamps) in a secure datastore. Webhook handling must verify signatures before persisting or acting on events.
