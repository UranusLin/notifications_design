# Kotlin Notification Service (Spring Boot 3 + Coroutines)

## Core Features
*   **Coroutines**: Uses Kotlin Coroutines (`suspend` functions) for async logic handling.
*   **Adapter Pattern**: Adapter pattern combined with Coroutines.

## Architecture Design
*   **Controller**: `NotificationController` (suspend) receives requests.
*   **Worker**: `NotificationWorker` uses `CoroutineScope` for parallel multi-channel processing.
*   **Adapter**: `ChannelAdapter` interface defines `suspend fun send(...)`, ensuring non-blocking send operations.

## Adding New Channels
1. Implement `ChannelAdapter` interface (e.g., `class VoiceAdapter : ChannelAdapter`).
2. Add `@Component` annotation.
3. Implement `suspend fun send` method.

## Running
```bash
./gradlew bootRun
```
