## Newrun‑PayPal Payment Microservice – Architecture Reference

**Scope**  
A standalone `pay-pal` microservice responsible for all PayPal‑specific payment flows: order creation, capture, refunds, and webhook event handling.

**Placement**  
`services/pay-pal/` (sibling to existing `services/payment-gateway/`). Register in the service‑mesh (`service‑registry.yaml`) and expose the HTTP API via the API‑gateway under `/pay-pal/*`.

**Key Components**  

| Component | Role |
|-----------|------|
| **API Layer** (`controller/`) | REST endpoints (`/create`, `/capture`, `/refund`) that translate internal DTOs to PayPal SDK calls. |
| **PayPal SDK Adapter** (`adapter/paypal/`) | Wraps the chosen PayPal SDK (currently mocked). Isolates third‑party changes. |
| **Auth Service** (`auth/`) | Handles OAuth token acquisition/refresh using credentials stored in Vault; exposes a token cache to the adapter. |
| **Webhook Processor** (`webhook/`) | Receives PayPal notifications, validates signatures, and publishes domain events (`PaymentCompleted`, `Refunded`) onto the event bus (Kafka/NATS). |
| **Configuration** (`config/`) | Reads `PAYPAL_CLIENT_ID`, `PAYPAL_SECRET`, `WEBHOOK_ID` from Vault at start‑up. |
| **Observability** (`metrics/`, `tracing/`) | Exposes Prometheus metrics and OpenTelemetry traces for all PayPal interactions. |

**Integration Points**  

- **Event Bus**: Emits `Payment*` events consumed by Order and Notification services.  
- **API‑Gateway**: Routes external calls; enforces JWT auth and rate limiting.  
- **Service Mesh**: Mutual TLS for inter‑service calls.

**Trade‑offs**  

| Pro | Con |
|-----|-----|
| Clear isolation of PayPal logic → easy scaling, independent release cadence. | Additional dev‑ops overhead (deployment, monitoring). |
| Limited blast radius for PayPal SDK changes. | Potential duplication of generic payment utilities; keep shared logic in `libs/payment-common`. |

**Implementation Guidance**  

- Reuse `libs/payment-common/*` for DTOs and error handling.  
- Store secrets in HashiCorp Vault; inject via sidecar or environment variables.  
- Write integration tests against PayPal sandbox (mocked now, replace with SDK stubs later).  
- Add CI pipeline step `pay-pal:test` and `pay-pal:lint`.  

**Next Steps**  

1. Choose concrete PayPal SDK (Java/Node) and replace mocks.  
2. Implement OAuth flow in `auth/`.  
3. Register webhook URL in PayPal sandbox and configure verification.  

---  

*This document aligns the new service with the existing microservice architecture, ensures low coupling, and flags operational considerations for downstream engineers.*