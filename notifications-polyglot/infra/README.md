# Infrastructure Services

本目錄包含 **Polyglot Notification System** 所需的所有基礎設施服務，使用 [Docker Compose](https://docs.docker.com/compose/) 進行編排。

## 服務列表

### 1. PostgreSQL (`postgres`)
- **用途**: 持久化儲存通知狀態、用戶資料和指標數據。
- **版本**: 16-alpine
- **端口**: 5432
- **預設帳密**: `user` / `password`
- **資料庫**: `notifications`
- **數據卷**: `postgres_data`
- **更多資訊**: [PostgreSQL Official](https://www.postgresql.org/)

### 2. Kafka (`kafka` & `zookeeper`)
- **用途**: 訊息隊列，用於解耦 API 層和 Worker 層，實現高吞吐量的異步通知發送。
- **版本**: Confluent Platform 7.5.0
- **端口**: 
  - Kafka: 9092 (外部訪問), 29092 (容器間通訊)
  - Zookeeper: 2181
- **Topic**: `notification-requests`
- **更多資訊**: [Apache Kafka](https://kafka.apache.org/)

### 3. Redis (`redis`)
- **用途**: (可選) 用於快取、速率限制 (Rate Limiting) 和去重 (Deduplication)。
- **版本**: 7-alpine
- **端口**: 6379
- **數據卷**: `redis_data`
- **更多資訊**: [Redis](https://redis.io/)

## 快速開始

### 啟動所有服務
```bash
docker compose up -d
```

### 查看服務狀態
```bash
docker compose ps
```

### 查看日誌
```bash
docker compose logs -f
```

### 停止服務
```bash
docker compose down
```

### 清除數據 (重置)
```bash
docker compose down -v
```

## 連接資訊

| 服務 | Host | Port | Username | Password | Database |
|------|------|------|----------|----------|----------|
| Postgres | localhost | 5432 | user | password | notifications |
| Kafka | localhost | 9092 | - | - | - |
| Redis | localhost | 6379 | - | - | - |

## 常見問題

### Kafka 連接失敗
如果應用程式無法連接 Kafka，請確保：
1. 容器已完全啟動 (`docker compose ps` 顯示 healthy)。
2. 應用程式配置的 `KAFKA_BROKERS` 指向 `localhost:9092` (如果在宿主機運行) 或 `kafka:29092` (如果在 Docker 網路內運行)。

### 資料庫連接被拒絕
請檢查端口 5432 是否被其他本地 Postgres 實例佔用。如果是，請修改 `docker-compose.yml` 中的端口映射，例如 `"5433:5432"`，並相應更新應用程式配置。
