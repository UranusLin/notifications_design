# Java Notification Service (Spring Boot 3 + Virtual Threads)

## Core Features
*   **[Virtual Threads](https://openjdk.org/jeps/444)**: Uses Java 21 Virtual Threads to handle high concurrency I/O.
*   **Adapter Pattern**: Implements an extensible channel adapter design.
*   **[JPA](https://spring.io/projects/spring-data-jpa) + [PostgreSQL](https://www.postgresql.org/)**: Uses JPA to persist notification status.

## 🛡️ Best Practices
*   **Spring AOP**: Uses AspectJ (`LoggingAspect`) to automatically log request parameters and response times for all Controllers.
*   **Advanced Logging**: 
    *   Uses Logback for log management.
    *   **Daily Rolling**: Logs are automatically rolled daily, keeping 30 days of history.
    *   **Error Separation**: Error logs (`ERROR` level) are written independently to `logs/error.log` for easy troubleshooting.

## Architecture Design
*   **Controller**: 
  - `NotificationController` - Receives notification requests (POST /notify)
  - `StatusController` - Queries status and metrics (GET /status/:id, GET /metrics)
  - `WebhookController` - Handles webhook callbacks (POST /webhook/callback)
*   **Service**: 
  - `NotificationService` - Writes to Kafka
  - `NotificationStatusService` - Manages notification status and metrics
*   **Worker**: `NotificationWorker` consumes Kafka messages and invokes the corresponding `ChannelAdapter` via `ChannelFactory`.

## API Endpoints

### POST /notify
Send notification request
```bash
curl -X POST http://localhost:8080/notify \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipientIds": ["user1"],
    "message": "Hello"
  }'
```

### GET /status/{id}
Query notification status
```bash
curl http://localhost:8080/status/{notification-id}
```

### GET /metrics
Get statistics
```bash
curl http://localhost:8080/metrics
```

### POST /webhook/callback
Receive external receipts
```bash
curl -X POST http://localhost:8080/webhook/callback \
  -H "Content-Type: application/json" \
  -d '{
    "notification_id": "xxx",
    "channel": "email",
    "status": "COMPLETED"
  }'
```

## Extending New Channels
1. Create a new class under the `com.example.notification.adapter` package (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` interface.
3. Add the `@Component` annotation, and Spring will automatically inject it into the Factory.

## Run
```bash
./gradlew bootRun
```

## Test
```bash
# Unit Tests
./gradlew test

# Integration Tests (including Testcontainers)
./gradlew test --tests "*IntegrationTest"
```
