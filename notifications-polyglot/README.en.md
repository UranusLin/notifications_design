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

This directory contains all source code implementations of the **Polyglot Notification System**.

## 📂 Directory Structure

*   `infra/`: Infrastructure (Docker Compose, Kafka, Postgres, Redis).
*   `java/`: Java (Spring Boot 3 + Virtual Threads) implementation.
*   `kotlin/`: Kotlin (Spring Boot 3 + Coroutines) implementation.
*   `golang/`: Go (Gin + Goroutines) implementation.
*   `typescript/`: TypeScript (NestJS + Fastify) implementation.
*   `rust/`: Rust (Axum + Tokio) implementation.

## 🏗️ System Architecture & Design Decisions

This system is designed based on the following key requirements:

### 1. Polyglot Implementation
To demonstrate the strengths of different languages in a microservices architecture, we implemented five versions of the service:
- **Java ([Spring Boot](https://spring.io/projects/spring-boot))**: Enterprise standard, leveraging [Virtual Threads](https://openjdk.org/jeps/444) for improved I/O performance.
- **Kotlin ([Spring Boot](https://spring.io/projects/spring-boot))**: Modern JVM language, using [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) to simplify asynchronous code.
- **Go ([Gin](https://gin-gonic.com/))**: High performance, low resource consumption, suitable for high concurrency scenarios.
- **Rust ([Axum](https://github.com/tokio-rs/axum))**: Extreme performance and memory safety, ideal for compute-intensive or latency-sensitive components.
- **TypeScript ([NestJS](https://nestjs.com/))**: Rapid development, vast ecosystem, suitable for full-stack teams.

### 2. Core Components
- **API Layer**: Receives requests, performs initial validation, responds quickly (Accepted 202), and pushes tasks to Kafka.
- **Message Queue ([Kafka](https://kafka.apache.org/))**: Buffers traffic bursts, ensuring system stability under high load, and guarantees at-least-once delivery.
- **Worker Layer**: Consumes Kafka messages and invokes external adapters to send notifications. Can be scaled independently to meet high throughput demands.
- **Storage ([PostgreSQL](https://www.postgresql.org/))**: Persists notification status, supporting transactional updates.

### 3. Design Patterns
- **Adapter Pattern**: Defines a unified `ChannelAdapter` interface, allowing new channels (e.g., Voice, In-app) to be added without modifying core logic, adhering to the **Open/Closed Principle**.
- **Asynchronous Processing**: All time-consuming operations (sending emails, SMS) are executed asynchronously in the background to ensure low API latency.

### 4. Observability
- **Status Tracking**: Each notification has a unique ID to track its complete lifecycle from `ENQUEUED` -> `PROCESSING` -> `COMPLETED`/`FAILED`.
- **Metrics**: Provides a `/metrics` endpoint to monitor total sent count and success rate.

## 🎯 Core Features

All language implementations include the following features:

### API Endpoints

1. **POST /notify** (or /notifications) - Send Notification
   - Receives notification request (channels, recipients, message)
   - Enqueues to Kafka
   - Returns Notification ID

2. **GET /status/:id** - Query Notification Status
   - Queries overall delivery status
   - Queries delivery status per channel
   - Statuses: ENQUEUED, PROCESSING, COMPLETED, FAILED, PARTIAL_FAILURE

3. **GET /metrics** - Get Statistics
   - Total sent count
   - Success/Failure count
   - Statistics by status

4. **POST /webhook/callback** - Webhook Callback
   - Receives delivery receipts from external providers (SendGrid, Twilio)
   - Updates notification status

### Technical Features

- ✅ **Asynchronous Processing**: Uses Kafka for async notification delivery
- ✅ **Multi-channel Support**: Email, SMS, Push notifications
- ✅ **Status Tracking**: Complete notification lifecycle tracking
- ✅ **Adapter Pattern**: Easy to extend with new notification channels
- ✅ **Data Persistence**: PostgreSQL for storing notification state
- ✅ **Comprehensive Testing**: Unit Tests + Integration Tests (using Testcontainers)

## 📖 Documentation Navigation

*   **Complete Architecture Design & Requirements**: See the root [README.md](../README.en.md).
*   **Language-Specific Details**: Enter each language directory to view its individual `README.md`.

## 🚀 Quick Start

```bash
# 1. Start infrastructure
cd infra
docker compose up -d

# 2. Run any service (Go example)
cd ../golang
make run

# 3. Test API
curl -X POST http://localhost:8083/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "channels": ["email", "sms"],
    "recipientIds": ["user1"],
    "message": "Hello World"
  }'
```

## 🧪 Running Tests

Each language implementation includes a complete test suite:

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
