# Kotlin Notification Service (Spring Boot 3 + Coroutines)

## Core Features
*   **[Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)**: Uses Kotlin Coroutines (`suspend` functions) to handle asynchronous logic.
*   **Adapter Pattern**: Adapter pattern combined with Coroutines.
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
*   **Worker**: `NotificationWorker` uses `CoroutineScope` to process multi-channel sending in parallel.
*   **Adapter**: `ChannelAdapter` interface defines `suspend fun send(...)`, ensuring the sending process does not block threads.

## API Endpoints

### POST /notify
Send notification request
```bash
curl -X POST http://localhost:8081/notify \
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
curl http://localhost:8081/status/{notification-id}
```

### GET /metrics
Get statistics
```bash
curl http://localhost:8081/metrics
```

## Extending New Channels
1. Implement the `ChannelAdapter` interface (e.g., `class VoiceAdapter : ChannelAdapter`).
2. Add `@Component`.
3. Implement the `suspend fun send` method.

## Run
```bash
./gradlew bootRun
```

## Test
```bash
# Unit Tests
./gradlew test

# Integration Tests
./gradlew test --tests "*IntegrationTest"
```
