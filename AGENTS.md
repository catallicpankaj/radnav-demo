# Engineering Conventions for this Repository

## Service Layout
- Each micro‑service lives in its own top‑level directory named **kebab‑case** (e.g., `newrun-paypal`).
- Inside a service:
  - `src/main/java/<base‑package>/` – Java source.
  - `src/main/resources/` – configuration files (`application.yml`).
  - `infra/` – minimal Kubernetes manifests (`k8s-deployment.yaml`, `k8s-service.yaml`).

## Package Structure (per service)
```
com.example.<service-name>
├─ adapter/            # Third‑party SDK wrappers (mocked here)
├─ config/             # @ConfigurationProperties classes
├─ controller/         # REST controllers – only request mapping & validation
├─ dto/                # Request/response POJOs, validation annotations
├─ exception/          # Global exception handling returning ErrorResponse
├─ model/              # JPA entities
├─ repository/         # Spring Data repositories
├─ service/            # Business logic interfaces & implementations
└─ New<CapitalizedService>Application.java  # SpringBoot entry point
```

## Dependency Injection
- Constructor injection everywhere; no `@Autowired` on fields.

## Configuration
- `application.yml` is the sole source of configuration.
- Sensitive values are read via environment variables or Kubernetes secrets and bound to a `@ConfigurationProperties` bean (`PayPalProperties`).

## Validation & Error Handling
- DTOs use Jakarta Bean Validation (`@NotBlank`, `@NotNull`, etc.).
- A `@RestControllerAdvice` (`GlobalExceptionHandler`) translates exceptions to the unified `ErrorResponse` JSON shape defined in the API contract.

## Persistence
- For simplicity we use an in‑memory H2 database with JPA (`spring-boot-starter-data-jpa`).
- Entities are minimal, only what the contract requires.

## Testing & Observability (not included in scaffold but expected)
- Unit tests should be placed under `src/test/java` mirroring the main package layout.
- Future services should add OpenAPI docs via SpringDoc (dependency already declared).

## Kubernetes Manifests
- One‑replica `Deployment` exposing the port defined in `application.yml`.
- `Service` of type `ClusterIP` exposing the same port as `80` (targetPort `8080`).
- Secrets for PayPal credentials are referenced but **not** committed.

---
*All new services added to this repository must follow the conventions described above.*