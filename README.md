# E-Commerce Cart Checkout + Mock Payment System

Java 25 / Spring Boot 3.5.14 — clean architecture implementation of a cart checkout system with safe payment handling.

## Prerequisites

- Java 25 LTS
- Maven 3.9+

## Setup

```bash
mvn clean install
```

## Run

```bash
mvn spring-boot:run
```

Server starts at `http://localhost:8080`.

## Swagger / API Docs

Interactive API documentation is available at:

| Resource | URL |
|----------|-----|
| **Swagger UI** | http://localhost:8080/swagger-ui.html |
| **OpenAPI JSON** | http://localhost:8080/v3/api-docs |


## Architecture

**Clean Architecture** with three layers:

```
domain/       → Pure business logic (models, state machine, repository interfaces, gateway ports)
application/  → Use cases (services, DTOs, exception handling)
infrastructure/ → Adapters (JPA persistence, REST controllers, mock payment provider)
```

Dependency direction: `infrastructure → application → domain`

## Key Design Decisions

### 1. Order State Machine

```
CREATED ──────→ PENDING_PAYMENT ──────→ PAID
  │                   │
  │                   ├────────────→ PAYMENT_FAILED ──→ PENDING_PAYMENT (retry)
  │                   │                    │
  ├────────────→ CANCELLED                ├────────→ CANCELLED
```

- Transitions enforced inside `Order.transitionTo()` via `OrderState.canTransitionTo()`
- `InvalidStateTransitionException` thrown for invalid transitions
- Terminal states (PAID, CANCELLED) are immutable — no outgoing transitions

### 2. Idempotency

The system protects against duplicates at **two levels** — payment creation and webhook delivery.

#### Payment Start Idempotency (Client-Provided Key)

```
Client → POST /orders/{id}/payment/start {"idempotencyKey":"abc-123"}
Server → findByIdempotencyKey("abc-123")
       ├─ FOUND → return cached response (safe retry after timeout)
       └─ NOT FOUND
            ├─ pessimistic lock order
            ├─ validate state (CREATED or PAYMENT_FAILED)
            ├─ check no PENDING payment exists
            ├─ create Payment with key "abc-123"
            ├─ transition order → PENDING_PAYMENT
            └─ return PaymentStartResponse {..., idempotencyKey:"abc-123"}
```

Same `idempotencyKey` = same Payment returned. No duplicate payment, no double charge.
The key is **required** (`@NotBlank`) — server does not generate fallback keys.

#### Webhook Idempotency (Two-Layer)

```
Provider → POST /payments/webhook {paymentId, event, idempotencyKey}
           Layer 1: idempotencyKey matches Payment.getIdempotencyKey()?
                    → NO → reject (stale webhook from previous payment attempt)
                    → YES or null → proceed to layer 2
           Layer 2: is payment already CONFIRMED/FAILED and event matches?
                    → YES → return cached response (idempotent, no state change)
                    → NO → process event (transition payment + order)
```

This handles:
- Same webhook arriving twice (provider retries) → **no-op**
- Stale webhook from previous payment attempt → **rejected** (key mismatch)
- Race condition between CONFIRMED and FAILED → **first wins** (status check)

### 3. Concurrency Protection

Three layers protect against concurrent access:

| Layer | Scope | Mechanism |
|-------|-------|-----------|
| **Pessimistic lock** | Order row (`findByIdWithLock`) | `SELECT FOR UPDATE` on order — serializes payment attempts per order |
| **Optimistic locking** | `@Version` on all entities | Prevents lost updates on cart, order, and payment |
| **DB unique constraint** | `(order_id, status)` on payments | Second INSERT with same (order, PENDING) fails at DB level |

### 4. Product Catalog (Server-Side Pricing)

The server looks up product prices from a seeded catalog — clients cannot manipulate prices.

- `CartItemRequest` accepts only `productId` + `quantity` (no `price`)
- Server validates product exists and is available
- Price is authoritative (from `Product` entity)

**Sample products** seeded on startup:

| ID | Name | Price | Available |
|----|------|-------|-----------|
| `prod-001` | Wireless Mouse | $29.99 | ✅ |
| `prod-002` | Mechanical Keyboard | $89.99 | ✅ |
| `prod-003` | USB-C Cable | $9.99 | ✅ |
| `prod-004` | Monitor Stand | $49.99 | ❌ (for testing) |

### 5. Cart Locking

- Cart is locked during checkout (`locked = true`)
- Locked carts reject item additions
- Prevents duplicate checkout attempts via the locked flag

### 6. Mock Payment Provider

- Implements `PaymentGateway` interface (domain port)
- **Real HTTP call**: mock endpoint uses `RestTemplate` to POST to `/payments/webhook` — exercises the full HTTP stack (validation, serialization, error handling)
- Same host/port as the running server (configurable via `app.webhook.base-url`)

## API Reference

### Cart Endpoints

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/carts` | Create a new empty cart | — | `CartResponse` (201) |
| `GET` | `/carts/{cartId}` | Get cart details | — | `CartResponse` (200) / 404 |
| `POST` | `/carts/{cartId}/items` | Add item to cart | `CartItemRequest` | `CartResponse` (200) / 400 / 404 |
| `POST` | `/carts/{cartId}/checkout` | Checkout → create order | — | `OrderResponse` (201) / 400 / 404 / 409 |

**Example: Create cart**
```bash
curl -X POST http://localhost:8080/carts
```

**Example: Add item**
```bash
curl -X POST http://localhost:8080/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"prod-001","quantity":2}'
```

**Example: Checkout**
```bash
curl -X POST http://localhost:8080/carts/{cartId}/checkout
```

### Order / Payment Endpoints

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/orders/{orderId}/payment/start` | Initiate payment (client must provide idempotency key) | `PaymentStartRequest` | `PaymentStartResponse` (200) / 404 / 409 |

**Example: Start payment**
```bash
curl -X POST http://localhost:8080/orders/{orderId}/payment/start \
  -H "Content-Type: application/json" \
  -d '{"idempotencyKey":"pay-retry-abc-123"}'
```

**Idempotency:** Sending the same `idempotencyKey` again returns the cached response — no duplicate payment created. Safe for retries after network timeouts.

### Payment Webhook Endpoints

| Method | Path | Description | Request | Response |
|--------|------|-------------|---------|----------|
| `POST` | `/payments/webhook` | Receive payment result from provider | `WebhookRequest` | `PaymentResponse` (200) / 400 / 404 |

**Webhook payload:**
```json
{
  "paymentId": "p-12345",
  "event": "CONFIRMED",
  "idempotencyKey": "idem-abc123"
}
```

**Idempotency:** Two-layer check — (1) idempotency key must match the payment's stored key (rejects stale webhooks from previous attempts); (2) payment status must not already match the event. Duplicate webhooks are safe no-ops.

### Mock Payment Provider Endpoints

For testing — simulates payment provider callbacks.

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/mock-payment/{paymentId}/confirm` | Simulate CONFIRMED webhook | `PaymentResponse` (200) |
| `POST` | `/mock-payment/{paymentId}/fail` | Simulate FAILED webhook | `PaymentResponse` (200) |
| `GET` | `/mock-payment/{paymentId}` | Check payment status | `Map<String,String>` |

**Example: Confirm payment (mock)**
```bash
curl -X POST http://localhost:8080/mock-payment/{paymentId}/confirm
```

### Complete Example Flow

```bash
# 1. Create cart
CART_ID=$(curl -X POST http://localhost:8080/carts | jq -r .id)

# 2. Add items
curl -X POST http://localhost:8080/carts/$CART_ID/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"prod-001","quantity":2}'

# 3. Checkout → creates order
ORDER=$(curl -X POST http://localhost:8080/carts/$CART_ID/checkout)
ORDER_ID=$(echo $ORDER | jq -r .id)

# 4. Start payment (client provides idempotency key)
IDEM_KEY="pay-attempt-$(uuidgen)"
PAYMENT=$(curl -X POST http://localhost:8080/orders/$ORDER_ID/payment/start \
  -H "Content-Type: application/json" \
  -d "{\"idempotencyKey\":\"${IDEM_KEY}\"}")
PAYMENT_ID=$(echo $PAYMENT | jq -r .paymentId)

# 5. Safe retry — same idempotency key returns cached response (no double charge)
curl -X POST http://localhost:8080/orders/$ORDER_ID/payment/start \
  -H "Content-Type: application/json" \
  -d "{\"idempotencyKey\":\"${IDEM_KEY}\"}"

# 6. Confirm payment (mock)
curl -X POST http://localhost:8080/mock-payment/$PAYMENT_ID/confirm
```

## Testing

### Unit Tests
- `OrderStateTransitionTest` — validates all state transitions (valid + invalid)
- `CartServiceTest` — cart creation, item addition, checkout, locking
- `PaymentServiceTest` — payment start, duplicate prevention, webhook handling, idempotency

### Integration Tests
- `CheckoutFlowIntegrationTest` — complete happy path (cart → paid)
- `WebhookDuplicateIntegrationTest` — duplicate webhook idempotency + payment failure retry

Run: `mvn test`

## Trade-offs & Future Improvements

- **H2 in-memory DB** chosen for simplicity; replace with PostgreSQL + Testcontainers for production
- **Synchronous mock webhook** used for simplicity; real async event processing would use a message queue
- **No authentication** — add Spring Security for production
- **Refunds/partial payments** — state machine already designed with extensibility in mind; add new states without breaking existing invariants
- **Redis caching for idempotency keys** — `findByIdempotencyKey()` queries the DB on every `startPayment`. Adding Redis (String key → Payment ID, with TTL matching the idempotency retention window) would reduce DB load and speed up retry responses. The DB remains the source of truth; Redis acts as a fast L1 cache.
