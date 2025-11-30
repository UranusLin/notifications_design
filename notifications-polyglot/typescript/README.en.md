# TypeScript Notification Service (NestJS + Fastify + TypeORM)

## Core Features
*   **[NestJS](https://nestjs.com/)**: Modular architecture, easy to maintain.
*   **[Fastify](https://fastify.dev/)**: High-performance HTTP underlayer.
*   **Adapter Pattern**: Adapter pattern implemented using NestJS Providers.
*   **[TypeORM](https://typeorm.io/) + [PostgreSQL](https://www.postgresql.org/)**: Uses TypeORM to persist notification status.

## 🛡️ Best Practices
*   **Advanced Logging**: Uses [Winston](https://github.com/winstonjs/winston) + `winston-daily-rotate-file`.
    *   **Daily Rolling**: Logs are automatically rolled daily, keeping 14 days of history.
    *   **Error Separation**: Error logs are written independently to `logs/error-%DATE%.log`.
*   **Global Exception Filter**: Centralized exception handling to return standardized error responses.

## Architecture Design
*   **Module**: `ChannelModule` encapsulates all channel logic.
*   **Controllers**:
  - `NotificationController` - POST /notifications
  - `StatusController` - GET /status/:id, GET /metrics
  - `WebhookController` - POST /webhook/callback
*   **Factory**: `ChannelFactory` is responsible for providing the correct Adapter instance.
*   **Worker**: `NotificationWorker` invokes `send()` via the Factory.

## API Endpoints

### POST /notifications
Send notification request
```bash
curl -X POST http://localhost:3000/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipientIds": ["user1"],
    "message": "Hello"
  }'
```

### GET /status/:id
Query notification status
```bash
curl http://localhost:3000/status/{notification-id}
```

### GET /metrics
Get statistics
```bash
curl http://localhost:3000/metrics
```

## Extending New Channels
1. Create a new Adapter in `src/channel/adapters/` (e.g., `VoiceAdapter`).
2. Implement the `ChannelAdapter` interface.
3. Register the Adapter in `providers` of `src/channel/channel.module.ts`.
4. Update `ChannelFactory` to include the new Adapter.

## Run
```bash
npm install
npm run start
```

## Test
```bash
# Unit Tests
npm test

# E2E Tests
npm run test:e2e
```
