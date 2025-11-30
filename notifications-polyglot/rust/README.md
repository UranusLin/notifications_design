# Rust Notification Service (Axum + Tokio + SQLx)

## 核心特色
*   **Performance**: 極致的運算效能與記憶體安全。
*   **Adapter Pattern**: 使用 Trait Objects (`dyn ChannelAdapter`) 實現動態分派。
*   **[SQLx](https://github.com/launchbadge/sqlx) + [PostgreSQL](https://www.postgresql.org/)**: 使用 SQLx 異步持久化通知狀態。

## 🛡️ 最佳實踐 (Best Practices)
*   **Structured Logging**: 使用 [Tracing](https://github.com/tokio-rs/tracing) 生態系統。
*   **Log Rotation**: 使用 `tracing-appender` 實現 Daily Rolling 日誌。
*   **Non-blocking Logging**: 使用非阻塞 Writer 確保日誌記錄不影響主線程效能。

## 架構設計
*   **API**: Axum Web Framework。
  - `send_notification` - POST /notify
  - `get_status` - GET /status/:id
  - `get_metrics` - GET /metrics
  - `handle_webhook` - POST /webhook/callback
*   **Worker**: Tokio 異步任務消費 Kafka。
*   **Adapter**: `src/adapter.rs` 定義了 `ChannelAdapter` Trait 與 `ChannelFactory`。

## API 端點

### POST /notify
發送通知請求
```bash
curl -X POST http://localhost:8084/notify \
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
curl http://localhost:8084/status/{notification-id}
```

### GET /metrics
獲取統計數據
```bash
curl http://localhost:8084/metrics
```

## 擴展新渠道
1. 在 `src/adapter.rs` 中定義新的 Struct (e.g., `VoiceAdapter`)。
2. 為該 Struct 實作 `ChannelAdapter` Trait。
3. 在 `ChannelFactory::new()` 中將其實例化並插入 HashMap。

## 運行
```bash
cargo run
```

## 測試
```bash
# 所有測試
cargo test

# 整合測試
cargo test --test integration_test
```
