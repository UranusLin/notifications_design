# Kotlin Notification Service (Spring Boot 3 + Coroutines)

## 核心特色
*   **Coroutines**: 使用 Kotlin Coroutines (`suspend` functions) 處理非同步邏輯。
*   **Adapter Pattern**: 結合 Coroutines 的適配器模式。
*   **JPA + PostgreSQL**: 使用 JPA 持久化通知狀態。

## 架構設計
*   **Controller**: 
  - `NotificationController` - 接收通知請求 (POST /notify)
  - `StatusController` - 查詢狀態和指標 (GET /status/:id, GET /metrics)
  - `WebhookController` - 處理 webhook 回調 (POST /webhook/callback)
*   **Worker**: `NotificationWorker` 使用 `CoroutineScope` 並行處理多渠道發送。
*   **Adapter**: `ChannelAdapter` 介面定義了 `suspend fun send(...)`，確保發送過程不阻塞線程。

## API 端點

### POST /notify
發送通知請求
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
查詢通知狀態
```bash
curl http://localhost:8081/status/{notification-id}
```

### GET /metrics
獲取統計數據
```bash
curl http://localhost:8081/metrics
```

## 擴展新渠道
1. 實作 `ChannelAdapter` 介面 (e.g., `class VoiceAdapter : ChannelAdapter`)。
2. 加上 `@Component`。
3. 實作 `suspend fun send` 方法。

## 運行
```bash
./gradlew bootRun
```

## 測試
```bash
# 單元測試
./gradlew test

# 整合測試
./gradlew test --tests "*IntegrationTest"
```
