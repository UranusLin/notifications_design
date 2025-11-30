# TypeScript Notification Service (NestJS + Fastify + TypeORM)

## 核心特色
*   **NestJS**: 模組化架構，易於維護。
*   **Fastify**: 高效能 HTTP 底層。
*   **Adapter Pattern**: 使用 NestJS Providers 實作的適配器模式。
*   **TypeORM + PostgreSQL**: 使用 TypeORM 持久化通知狀態。

## 架構設計
*   **Module**: `ChannelModule` 封裝了所有渠道邏輯。
*   **Controllers**:
  - `NotificationController` - POST /notifications
  - `StatusController` - GET /status/:id, GET /metrics
  - `WebhookController` - POST /webhook/callback
*   **Factory**: `ChannelFactory` 負責提供正確的 Adapter 實例。
*   **Worker**: `NotificationWorker` 透過 Factory 調用 `send()`。

## API 端點

### POST /notifications
發送通知請求
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
查詢通知狀態
```bash
curl http://localhost:3000/status/{notification-id}
```

### GET /metrics
獲取統計數據
```bash
curl http://localhost:3000/metrics
```

## 擴展新渠道
1. 在 `src/channel/adapters/` 建立新 Adapter (e.g., `VoiceAdapter`)。
2. 實作 `ChannelAdapter` 介面。
3. 在 `src/channel/channel.module.ts` 的 `providers` 中註冊該 Adapter。
4. 更新 `ChannelFactory` 以包含新 Adapter。

## 運行
```bash
npm install
npm run start
```

## 測試
```bash
# 單元測試
npm test

# E2E 測試
npm run test:e2e
```
