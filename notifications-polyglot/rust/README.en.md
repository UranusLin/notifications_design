# Rust Notification Service (Axum + Tokio)

## Core Features
*   **Performance**: Extreme computational performance and memory safety.
*   **Adapter Pattern**: Dynamic dispatch using Trait Objects (`dyn ChannelAdapter`).

## Architecture Design
*   **API**: Axum Web Framework.
*   **Worker**: Tokio async tasks consume Kafka messages.
*   **Adapter**: `src/adapter.rs` defines `ChannelAdapter` Trait and `ChannelFactory`.

## Adding New Channels
1. Define a new Struct in `src/adapter.rs` (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` Trait for the Struct.
3. Instantiate and insert it into the HashMap in `ChannelFactory::new()`.

## Running
```bash
cargo run
```
