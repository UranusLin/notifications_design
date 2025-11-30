# Java Notification Service (Spring Boot 3 + Virtual Threads)

## Core Features
*   **Virtual Threads**: Uses Java 21 virtual threads for high-concurrency I/O handling.
*   **Adapter Pattern**: Implements extensible channel adapter design.

## Architecture Design
*   **Controller**: `NotificationController` receives REST requests.
*   **Service**: `NotificationService` writes to Kafka.
*   **Worker**: `NotificationWorker` consumes Kafka messages and invokes corresponding `ChannelAdapter` via `ChannelFactory`.

## Adding New Channels
1. Create a new class in `com.example.notification.adapter` package (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` interface.
3. Add `@Component` annotation; Spring will automatically inject it into the Factory.

## Running
```bash
./gradlew bootRun
```
