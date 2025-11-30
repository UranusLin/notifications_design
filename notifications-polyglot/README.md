# Polyglot Notification System Implementation

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Go](https://img.shields.io/badge/Go-00ADD8?style=for-the-badge&logo=go&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Rust](https://img.shields.io/badge/Rust-000000?style=for-the-badge&logo=rust&logoColor=white)

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

[![CI](https://github.com/UranusLin/notifications_design/actions/workflows/ci.yml/badge.svg)](https://github.com/UranusLin/notifications_design/actions)
[![License](https://img.shields.io/github/license/UranusLin/notifications_design?style=flat-square)](https://github.com/UranusLin/notifications_design/blob/main/LICENSE)

本目錄包含了 **Polyglot Notification System** 的所有源代碼實作。

## 📂 目錄結構

*   `infra/`: 基礎設施 (Docker Compose, Kafka, Postgres, Redis)。
*   `java/`: Java (Spring Boot 3 + Virtual Threads) 實作。
*   `kotlin/`: Kotlin (Spring Boot 3 + Coroutines) 實作。
*   `golang/`: Go (Gin + Goroutines) 實作。
*   `typescript/`: TypeScript (NestJS + Fastify) 實作。
*   `rust/`: Rust (Axum + Tokio) 實作。

## 🏗️ 系統架構與設計決策

本系統根據以下關鍵需求進行設計：

### 1. 多語言實作 (Polyglot)
為了展示不同語言在微服務架構中的優勢，我們實作了五種版本的服務：
- **Java ([Spring Boot](https://spring.io/projects/spring-boot))**: 企業級標準，利用 [Virtual Threads](https://openjdk.org/jeps/444) 提升 I/O 效能。
- **Kotlin ([Spring Boot](https://spring.io/projects/spring-boot))**: 現代化 JVM 語言，利用 [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) 簡化非同步代碼。
- **Go ([Gin](https://gin-gonic.com/))**: 高效能、低資源消耗，適合高併發場景。
- **Rust ([Axum](https://github.com/tokio-rs/axum))**: 極致效能與記憶體安全，適合計算密集或對延遲極其敏感的組件。
- **TypeScript ([NestJS](https://nestjs.com/))**: 快速開發，龐大的生態系統，適合全端團隊。

### 2. 核心組件
- **API Layer**: 接收請求，進行初步驗證，快速回應 (Accepted 202)，並將任務推送到 Kafka。
- **Message Queue ([Kafka](https://kafka.apache.org/))**: 緩衝突發流量 (Bursts)，確保系統在高負載下的穩定性，並保證訊息的至少一次傳遞 (At-least-once delivery)。
- **Worker Layer**: 消費 Kafka 訊息，調用外部適配器發送通知。可獨立擴展以應對高吞吐量需求。
- **Storage ([PostgreSQL](https://www.postgresql.org/))**: 持久化通知狀態，支援事務性更新。

### 3. 設計模式
- **Adapter Pattern**: 定義統一的 `ChannelAdapter` 介面，使得新增渠道（如 Voice, In-app）無需修改核心邏輯，符合 **Open/Closed Principle**。
- **Asynchronous Processing**: 所有耗時操作（發送郵件、簡訊）均在背景異步執行，確保 API 低延遲。

### 4. 可觀察性 (Observability)
- **Status Tracking**: 每個通知都有唯一的 ID，可追蹤其從 `ENQUEUED` -> `PROCESSING` -> `COMPLETED`/`FAILED` 的完整生命週期。
- **Metrics**: 提供 `/metrics` 端點，監控發送總量與成功率。

## 🎯 核心功能

所有語言實作均包含以下功能：

### API 端點

1. **POST /notify** (或 /notifications) - 發送通知
   - 接收通知請求（渠道、接收者、訊息）
   - 排入 Kafka 發送隊列
   - 返回通知 ID

2. **GET /status/:id** - 查詢通知狀態
   - 查詢整體發送狀態
   - 查詢各渠道的發送狀態
   - 狀態：ENQUEUED, PROCESSING, COMPLETED, FAILED, PARTIAL_FAILURE

3. **GET /metrics** - 獲取統計數據
   - 總發送數
   - 成功數 / 失敗數
   - 按狀態分類的統計

4. **POST /webhook/callback** - Webhook 回調
   - 接收外部供應商（SendGrid, Twilio）的回執報告
   - 更新通知狀態

### 技術特性

- ✅ **非同步處理**：使用 Kafka 實現異步通知發送
- ✅ **多渠道支援**：Email, SMS, Push 通知
- ✅ **狀態追蹤**：完整的通知生命週期追蹤
- ✅ **適配器模式**：易於擴展新的通知渠道
- ✅ **資料持久化**：PostgreSQL 儲存通知狀態
- ✅ **完整測試**：單元測試 + 整合測試（使用 Testcontainers）

## 📖 文檔導航

*   **完整架構設計與需求**: 請參閱根目錄的 [README.md](../README.md)。
*   **各語言詳細說明**: 請進入上述各語言目錄查看其獨立的 `README.md`。

## 🚀 快速運行

```bash
# 1. 啟動基礎設施
cd infra
docker compose up -d

# 2. 運行任一服務 (以 Go 為例)
cd ../golang
make run

# 3. 測試 API
curl -X POST http://localhost:8083/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipientIds": ["user1"],
    "message": "Hello World"
  }'
```

## 🧪 運行測試

每個語言實作都包含完整的測試套件：

```bash
# Java
cd java && ./gradlew test

# Kotlin
cd kotlin && ./gradlew test

# Go
cd golang && go test ./...

# TypeScript
cd typescript && npm test

# Rust
cd rust && cargo test
```
