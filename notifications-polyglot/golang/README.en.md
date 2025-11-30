# Go Notification Service (Gin + Sarama)

## Core Features
*   **Goroutines**: Efficient concurrent processing.
*   **Adapter Pattern**: Strategy pattern implemented using Go interfaces.

## Architecture Design
*   **API**: Gin framework handles HTTP requests.
*   **Worker**: Uses `sarama` Consumer Group to consume Kafka messages.
*   **Adapter**: `adapter` package defines `ChannelAdapter` interface and `ChannelFactory`.

## Adding New Channels
1. Define a new Struct in `adapter/adapter.go` (e.g., `VoiceAdapter`).
2. Implement `Supports` and `Send` methods.
3. Register the adapter in `NewChannelFactory`.

## Running
```bash
go run main.go
```
