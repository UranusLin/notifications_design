# Java Notification Service (Spring Boot 3 + Virtual Threads)

## 核心特色
*   **[Virtual Threads](https://openjdk.org/jeps/444)**: 使用 Java 21 虛擬線程處理高併發 I/O。
*   **Adapter Pattern**: 實作了可擴展的渠道適配器設計。
*   **[JPA](https://spring.io/projects/spring-data-jpa) + [PostgreSQL](https://www.postgresql.org/)**: 使用 JPA 持久化通知狀態。

## 🛡️ 最佳實踐 (Best Practices)
*   **Spring AOP**: 使用 AspectJ (`LoggingAspect`) 自動記錄所有 Controller 的請求參數與響應時間。
*   **Advanced Logging**: 
    *   使用 Logback 進行日誌管理。
    *   **Daily Rolling**: 日誌每天自動滾動，保留 30 天歷史。
    *   **Error Separation**: 錯誤日誌 (`ERROR` level) 獨立寫入 `logs/error.log`，方便排查。

## 架構設計
*   **Controller**: 
  - `NotificationController` - 接收通知請求 (POST /notify)
  - `StatusController` - 查詢狀態和指標 (GET /status/:id, GET /metrics)
  - `WebhookController` - 處理 webhook 回調 (POST /webhook/callback)
*   **Service**: 
  - `NotificationService` - 寫入 Kafka
  - `NotificationStatusService` - 管理通知狀態和指標
*   **Worker**: `NotificationWorker` 消費 Kafka 訊息，並透過 `ChannelFactory` 調用對應的 `ChannelAdapter`。

## API 端點

### POST /notify
發送通知請求
```bash
curl -X POST http://localhost:8080/notify \
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
curl http://localhost:8080/status/{notification-id}
```

### GET /metrics
獲取統計數據
```bash
curl http://localhost:8080/metrics
```

### POST /webhook/callback
接收外部回執
```bash
curl -X POST http://localhost:8080/webhook/callback \
  -H "Content-Type: application/json" \
  -d '{
    "notification_id": "xxx",
    "channel": "email",
    "status": "COMPLETED"
  }'
```

## 擴展新渠道
1. 在 `com.example.notification.adapter` 套件下建立新類別 (e.g., `VoiceAdapter`)。
2. 實作 `ChannelAdapter` 介面。
3. 加上 `@Component` 註解，Spring 會自動將其注入到 Factory 中。

## 運行
```bash
./gradlew bootRun
```

## 測試
```bash
# 單元測試
./gradlew test

# 整合測試（包含 Testcontainers）
./gradlew test --tests "*IntegrationTest"
```
