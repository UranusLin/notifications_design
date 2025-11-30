# Rust Notification Service (Axum + Tokio + SQLx)

## Core Features
*   **Performance**: Extreme computational performance and memory safety.
*   **Adapter Pattern**: Implements dynamic dispatch using Trait Objects (`dyn ChannelAdapter`).
*   **[SQLx](https://github.com/launchbadge/sqlx) + [PostgreSQL](https://www.postgresql.org/)**: Uses SQLx for asynchronous persistence of notification status.

## 🛡️ Best Practices
*   **Structured Logging**: Uses the [Tracing](https://github.com/tokio-rs/tracing) ecosystem.
*   **Log Rotation**: Uses `tracing-appender` to implement Daily Rolling logs.
*   **Non-blocking Logging**: Uses non-blocking Writer to ensure logging does not affect main thread performance.

## Architecture Design
*   **API**: Axum Web Framework.
  - `send_notification` - POST /notify
  - `get_status` - GET /status/:id
  - `get_metrics` - GET /metrics
  - `handle_webhook` - POST /webhook/callback
*   **Worker**: Tokio asynchronous task consumes Kafka.
*   **Adapter**: `src/adapter.rs` defines `ChannelAdapter` Trait and `ChannelFactory`.

## API Endpoints

### POST /notify
Send notification request
```bash
curl -X POST http://localhost:8084/notify \
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
curl http://localhost:8084/status/{notification-id}
```

### GET /metrics
Get statistics
```bash
curl http://localhost:8084/metrics
```

## Extending New Channels
1. Define a new Struct in `src/adapter.rs` (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` Trait for the Struct.
3. Instantiate and insert it into the HashMap in `ChannelFactory::new()`.

## Run
```bash
cargo run
```

## Test
```bash
# All Tests
cargo test

# Integration Tests
cargo test --test integration_test
```
