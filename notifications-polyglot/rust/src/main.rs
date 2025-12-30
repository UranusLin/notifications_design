mod api;
mod service;
mod worker;
mod response;
mod adapter;
mod models;
mod status_service;

use std::sync::Arc;
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};
use sqlx::postgres::PgPoolOptions;

#[tokio::main]
async fn main() -> anyhow::Result<()> {
    let file_appender = tracing_appender::rolling::daily("logs", "application.log");
    let (non_blocking, _guard) = tracing_appender::non_blocking(file_appender);

    tracing_subscriber::registry()
        .with(tracing_subscriber::EnvFilter::new(
            std::env::var("RUST_LOG").unwrap_or_else(|_| "info".into()),
        ))
        .with(tracing_subscriber::fmt::layer())
        .with(tracing_subscriber::fmt::layer().with_writer(non_blocking))
        .init();

    let database_url = std::env::var("DATABASE_URL")
        .unwrap_or_else(|_| "postgres://postgres:postgres@localhost:5432/notification_db".to_string());
    
    let pool = PgPoolOptions::new()
        .max_connections(5)
        .connect(&database_url)
        .await?;

    // Run migrations
    sqlx::query(
        r#"
        CREATE TABLE IF NOT EXISTS notification_status (
            notification_id VARCHAR(255) PRIMARY KEY,
            status VARCHAR(50) NOT NULL,
            channel_statuses JSONB NOT NULL,
            created_at TIMESTAMP WITH TIME ZONE NOT NULL,
            updated_at TIMESTAMP WITH TIME ZONE NOT NULL
        )
        "#,
    )
    .execute(&pool)
    .await?;

    tracing::debug!("Migrations completed");

    let kafka_brokers = std::env::var("KAFKA_BROKERS").unwrap_or_else(|_| "localhost:9092".to_string());

    let notification_service = Arc::new(service::NotificationService::new(&kafka_brokers).await?);
    let status_service = Arc::new(status_service::NotificationStatusService::new(pool));

    let worker_status_service = status_service.clone();
    let worker_brokers = kafka_brokers.clone();
    tokio::spawn(async move {
        if let Err(e) = worker::run_worker(&worker_brokers, "notification-workers-rust", worker_status_service).await {
            tracing::error!("Worker failure: {:?}", e);
        }
    });

    let app_state = api::AppState {
        notification_service,
        status_service,
    };
    let app = api::create_router(app_state);
    let listener = tokio::net::TcpListener::bind("0.0.0.0:8084").await?;
    tracing::info!("Server listening on {}", listener.local_addr()?);
    axum::serve(listener, app).await?;

    Ok(())
}
