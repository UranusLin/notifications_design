# TypeScript Notification Service (NestJS + Fastify)

## Core Features
*   **NestJS**: Modular architecture for easy maintenance.
*   **Fastify**: High-performance HTTP layer.
*   **Adapter Pattern**: Adapter pattern implemented using NestJS Providers.

## Architecture Design
*   **Module**: `ChannelModule` encapsulates all channel logic.
*   **Factory**: `ChannelFactory` provides the correct Adapter instances.
*   **Worker**: `NotificationWorker` invokes `send()` via Factory.

## Adding New Channels
1. Create a new Adapter in `src/channel/adapters/` (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` interface.
3. Register the Adapter in `providers` of `src/channel/channel.module.ts`.
4. Update `ChannelFactory` to include the new Adapter.

## Running
```bash
npm install
npm run start
```
