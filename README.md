# Order Notification Service

Spring Boot service for accepting high-volume order status requests, storing every request as
an audit entry, and producing mocked email notifications asynchronously through Kafka.

The project is organized as a modular monolith with domain-oriented packages. Package internals
stay package-private where possible. Other modules communicate through public facades and DTOs.

## Architecture

```mermaid
flowchart LR
    Client["E-commerce platforms"] --> Api["Order intake API"]
    Api --> OrdersTopic["Kafka: orders.received.v1"]

    OrdersTopic --> Audit["Order audit module"]
    Audit --> AuditDb[("PostgreSQL: order_request_audit")]
    Audit --> Outbox[("PostgreSQL: notification_outbox")]

    Outbox --> OutboxTask["Notification outbox publisher"]
    OutboxTask --> NotificationsTopic["Kafka: notifications.requested.v1"]

    NotificationsTopic --> Notification["Notification module"]
    Notification --> MockEmail["Mock email sender"]
    Notification --> NotificationDb[("PostgreSQL: notification_log")]
```

Main modules:

- `orderintake` accepts and validates HTTP requests, then publishes order events.
- `orderaudit` consumes order events and stores immutable audit data.
- `notificationoutbox` stores pending notification requests and publishes them in batches.
- `notification` consumes notification requests, simulates email sending, and stores notification logs.
- `messaging` contains Kafka message contracts shared between modules.
- `shared` contains cross-module infrastructure DTOs/configuration.

Notification outbox status flow:

```text
PENDING -> PROCESSING -> PUBLISHED
                  |
                  -> PENDING retry
                  -> FAILED after max attempts
```

Outbox entries are claimed with PostgreSQL row locking before publishing. This allows multiple
application instances to process different outbox entries without publishing the same notification
request twice. Stale `PROCESSING` entries are reclaimed after a configurable timeout, so a crashed
instance does not leave notifications blocked forever.

## Requirements

- Java 17
- Docker and Docker Compose

## Run Locally With Docker Compose

Start the full stack:

```bash
docker compose up --build
```

Services:

- API: `http://localhost:8080`
- Kafka UI: `http://localhost:8081`
- PostgreSQL: `localhost:5432`
- Kafka: `localhost:9092`

Stop the stack:

```bash
docker compose down
```

Remove persisted PostgreSQL data:

```bash
docker compose down -v
```

## Test The Flow

Create an order status request:

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "shipmentNumber": "PL123456789",
    "recipientEmail": "recipient@example.com",
    "recipientCountryCode": "PL",
    "senderCountryCode": "DE",
    "statusCode": 42
  }'
```

The response contains a `requestId`:

```json
{
  "requestId": "00000000-0000-0000-0000-000000000000",
  "acceptedAt": "2026-05-22T18:00:00Z"
}
```

Check stored audit data:

```bash
curl http://localhost:8080/api/v1/order-requests/{requestId}
```

Check stored notification log after a short moment:

```bash
curl http://localhost:8080/api/v1/notifications/{requestId}
```

Health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Kafka consumers use exponential retry and publish exhausted records to dead-letter topics:

- `orders.received.v1.DLT`
- `notifications.requested.v1.DLT`

## Processing Controls

The application exposes separate controls for intake and notification processing:

| Area | Variable | Default |
| --- | --- | --- |
| Kafka consumer retry initial interval | `KAFKA_CONSUMER_RETRY_INITIAL_INTERVAL` | `1s` |
| Kafka consumer retry max interval | `KAFKA_CONSUMER_RETRY_MAX_INTERVAL` | `10s` |
| Kafka consumer retry max elapsed time | `KAFKA_CONSUMER_RETRY_MAX_ELAPSED_TIME` | `1m` |
| Order audit consumer concurrency | `ORDER_AUDIT_CONCURRENCY` | `1` |
| Order audit max poll records | `ORDER_AUDIT_MAX_POLL_RECORDS` | `100` |
| Order audit dead-letter topic | `ORDER_AUDIT_DLT_TOPIC` | `orders.received.v1.DLT` |
| Notification consumer concurrency | `NOTIFICATION_CONCURRENCY` | `1` |
| Notification max poll records | `NOTIFICATION_MAX_POLL_RECORDS` | `50` |
| Notification dead-letter topic | `NOTIFICATION_DLT_TOPIC` | `notifications.requested.v1.DLT` |
| Outbox publishing interval | `NOTIFICATION_OUTBOX_PUBLISH_INTERVAL` | `2s` |
| Outbox batch size | `NOTIFICATION_OUTBOX_BATCH_SIZE` | `50` |
| Outbox retry delay | `NOTIFICATION_OUTBOX_RETRY_DELAY` | `10s` |
| Outbox processing timeout | `NOTIFICATION_OUTBOX_PROCESSING_TIMEOUT` | `1m` |
| Outbox max attempts | `NOTIFICATION_OUTBOX_MAX_ATTEMPTS` | `3` |

## Run Tests

```bash
./gradlew test
```

## Deployment

Public deployment:

```text
https://order-notification-service.onrender.com/
```

External health check:

```bash
curl https://order-notification-service.onrender.com/actuator/health
```

External order request example:

```bash
curl -i -X POST https://order-notification-service.onrender.com/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "shipmentNumber": "PL123456789",
    "recipientEmail": "recipient@example.com",
    "recipientCountryCode": "PL",
    "senderCountryCode": "DE",
    "statusCode": 42
  }'
```
