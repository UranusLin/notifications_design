# Go Notification Service (Gin + GORM)

## Core Features
*   **[Goroutines](https://go.dev/tour/concurrency/1)**: Efficient concurrency handling.
*   **Adapter Pattern**: Strategy pattern implemented using Go Interfaces.
*   **[GORM](https://gorm.io/) + [PostgreSQL](https://www.postgresql.org/)**: Uses GORM to persist notification status.

## 🛡️ Best Practices
*   **Structured Logging**: Uses [Zap](https://github.com/uber-go/zap) for high-performance structured logging.
*   **Log Rotation**: Uses [Lumberjack](https://github.com/natefinch/lumberjack) for log rotation (Daily Rolling, 30 days retention).
*   **Middleware**: Custom Gin Middleware to log request details (Latency, Status, Method).
*   **Error Separation**: Error logs are written independently to `logs/error.log`.

## Architecture Design
*   **API**: Gin framework handles HTTP requests.
  - `SendNotification` - POST /notifications
  - `GetStatus` - GET /status/:id
  - `GetMetrics` - GET /metrics
  - `HandleWebhook` - POST /webhook/callback
*   **Worker**: Uses `sarama` Consumer Group to consume Kafka.
*   **Adapter**: `adapter` package defines `ChannelAdapter` interface and `ChannelFactory`.

## API Endpoints

### POST /notifications
Send notification request
```bash
curl -X POST http://localhost:8083/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipient_ids": ["user1"],
    "message": "Hello"
  }'
```

### GET /status/:id
Query notification status
```bash
curl http://localhost:8083/status/{notification-id}
```

### GET /metrics
Get statistics
```bash
curl http://localhost:8083/metrics
```

## Extending New Channels
1. Define a new Struct in `adapter/adapter.go` (e.g., `VoiceAdapter`).
2. Implement `Supports` and `Send` methods.
3. Register the Adapter in `NewChannelFactory`.

## Run
```bash
go run main.go
```

## Test
```bash
# All Tests
go test ./...

# Integration Tests
go test -v -run TestNotificationIntegration
```
