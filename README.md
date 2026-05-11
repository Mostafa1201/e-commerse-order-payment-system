# Checkout Service — Cart + Mock Payment System

## Quick start

```bash
# Build and run
./mvnw spring-boot:run

# Run tests
./mvnw test

# H2 console (inspect DB while running)
# URL: http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:checkoutdb
# User: sa  Password: (empty)
```

---

## Architecture overview

Single Spring Boot service with three internal domains:

```
com.exequt.checkout
├── cart        → Cart aggregate, items, checkout lock
├── order       → Order aggregate, state machine
├── payment     → PaymentAttempt, idempotency, webhook handling
├── mock        → Simulated payment provider
├── config      → RestTemplate
└── exception   → Custom exceptions + GlobalExceptionHandler
```

---

## State machine

```
CREATED ──startPayment()──► PENDING_PAYMENT
                                │
                    ┌───────────┴───────────┐
              CONFIRMED               FAILED webhook
                  │                        │
                PAID              PAYMENT_FAILED
                                           │
                                    startPayment()
                                    (retry allowed)
```

Transitions are enforced inside the `Order` aggregate. Calling a transition
method from an invalid state throws `IllegalStateTransitionException` (HTTP 409).

---

## API reference

### Cart

| Method | Endpoint                   | Description                             |
|--------|----------------------------|-----------------------------------------|
| POST   | `/carts`                   | Create empty cart                       |
| GET    | `/carts/{cartId}`          | Get cart with items and total           |
| POST   | `/carts/{cartId}/items`    | Add item `{productId, quantity, price}` |
| POST   | `/carts/{cartId}/checkout` | Checkout → creates Order                |

### Order

| Method | Endpoint            | Description                  |
|--------|---------------------|------------------------------|
| GET    | `/orders/{orderId}` | Get order details and status |

### Payment

| Method | Endpoint                          | Description                           |
|--------|-----------------------------------|---------------------------------------|
| POST   | `/orders/{orderId}/payment/start` | Start payment for order               |
| POST   | `/payments/webhook`               | Provider webhook (CONFIRMED / FAILED) |

### Mock provider (test only)

| Method | Endpoint                 | Description                    |
|--------|--------------------------|--------------------------------|
| POST   | `/mock-provider/trigger` | Manually fire a payment result |

---

## Testing the full flow

### Happy path

```bash
# 1. Create cart
curl -X POST http://localhost:8080/carts
# → { "id": "cart-uuid", "status": "OPEN", ... }

# 2. Add item
curl -X POST http://localhost:8080/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"prod-1","quantity":2,"price":49.99}'

# 3. Checkout
curl -X POST http://localhost:8080/carts/{cartId}/checkout
# → { "id": "order-uuid", "status": "CREATED", "totalAmount": 99.98 }

# 4. Start payment
curl -X POST http://localhost:8080/orders/{orderId}/payment/start
# → { "externalPaymentId": "ext-...", "orderStatus": "PENDING_PAYMENT" }

# 5. Trigger CONFIRMED via mock provider
curl -X POST http://localhost:8080/mock-provider/trigger \
  -H "Content-Type: application/json" \
  -d '{"externalPaymentId":"ext-...","result":"CONFIRMED"}'

# 6. Check order is PAID
curl http://localhost:8080/orders/{orderId}
# → { "status": "PAID" }
```

### Duplicate webhook test

```bash
# Fire the same trigger twice — second call must be a no-op
curl -X POST http://localhost:8080/mock-provider/trigger \
  -d '{"externalPaymentId":"ext-...","result":"CONFIRMED"}'
curl -X POST http://localhost:8080/mock-provider/trigger \
  -d '{"externalPaymentId":"ext-...","result":"CONFIRMED"}'
# Order stays PAID, no error, no corrupted state
```

---

## Key decisions & trade-offs

### State machine in the domain object, not in a service

The `Order` class owns its own transition methods. Services cannot put `Order`
into an invalid state — the object protects itself. This is the "rich domain model"
pattern.

### Idempotency via PaymentAttempt + DB unique constraint

`PaymentAttempt.externalPaymentId` has a `UNIQUE` constraint. Duplicate webhooks
are detected in application code (isAlreadyProcessed()) and as a last resort
blocked at the DB level. Both layers of protection.

### Optimistic locking (@Version) over pessimistic locking

Contention on a single order is rare. Optimistic locking (compare-and-swap on
the version column) is much cheaper than `SELECT FOR UPDATE`. The rare concurrent
collision is handled by catching `ObjectOptimisticLockingFailureException` in
`PaymentService.handleWebhook()`.

---
