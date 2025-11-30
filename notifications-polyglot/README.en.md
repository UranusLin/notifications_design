# Polyglot Notification System Implementation

This directory contains all source code implementations of the **Polyglot Notification System**.

## 📂 Directory Structure

*   `infra/`: Infrastructure (Docker Compose, Kafka, Postgres, Redis).
*   `java/`: Java (Spring Boot 3 + Virtual Threads) implementation.
*   `kotlin/`: Kotlin (Spring Boot 3 + Coroutines) implementation.
*   `golang/`: Go (Gin + Goroutines) implementation.
*   `typescript/`: TypeScript (NestJS + Fastify) implementation.
*   `rust/`: Rust (Axum + Tokio) implementation.

## 📖 Documentation Navigation

*   **Complete Architecture Design & Requirements**: See the root [README.md](../README.md).
*   **Language-Specific Details**: Enter each language directory to view its individual `README.md`.

## 🚀 Quick Start

```bash
# 1. Start infrastructure
cd infra
docker-compose up -d

# 2. Run any service (Go example)
cd ../golang
make run
```
