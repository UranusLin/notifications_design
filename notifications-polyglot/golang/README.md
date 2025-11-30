# Go Notification Service (Gin + GORM)

## 核心特色
*   **Goroutines**: 高效的併發處理。
*   **Adapter Pattern**: 使用 Go Interface 實現的策略模式。
*   **GORM + PostgreSQL**: 使用 GORM 持久化通知狀態。

## 架構設計
*   **API**: Gin 框架處理 HTTP 請求。
  - `SendNotification` - POST /notifications
  - `GetStatus` - GET /status/:id
  - `GetMetrics` - GET /metrics
  - `HandleWebhook` - POST /webhook/callback
*   **Worker**: 使用 `sarama` Consumer Group 消費 Kafka。
*   **Adapter**: `adapter` package 定義了 `ChannelAdapter` interface 與 `ChannelFactory`。

## API 端點

### POST /notifications
發送通知請求
```bash
curl -X POST http://localhost:8083/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipient_ids": ["user1"],
    "message": "Hello"
  }'
```

### GET /status/:id
查詢通知狀態
```bash
curl http://localhost:8083/status/{notification-id}
```

### GET /metrics
獲取統計數據
```bash
curl http://localhost:8083/metrics
```

## 擴展新渠道
1. 在 `adapter/adapter.go` 中定義新的 Struct (e.g., `VoiceAdapter`)。
2. 實作 `Supports` 和 `Send` 方法。
3. 在 `NewChannelFactory` 中註冊該 Adapter。

## 運行
```bash
go run main.go
```

## 測試
```bash
# 所有測試
go test ./...

# 整合測試
go test -v -run TestNotificationIntegration
```
