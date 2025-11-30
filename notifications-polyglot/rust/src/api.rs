use axum::{
    extract::{Path, State},
    http::StatusCode,
    routing::{get, post},
    Json, Router,
};
use serde::{Deserialize, Serialize};
use std::sync::Arc;
use crate::service::NotificationService;
use crate::status_service::NotificationStatusService;
use crate::models::{NotificationStatus, NotificationMetrics};
use crate::response::ApiResponse;

#[derive(Deserialize, Serialize, Clone)]
pub struct NotificationRequest {
    pub channels: Vec<String>,
    pub recipient_ids: Vec<String>,
    pub message: String,
    #[serde(default)]
    pub metadata: serde_json::Map<String, serde_json::Value>,
}

#[derive(Serialize)]
pub struct EnqueueResponse {
    pub notification_id: String,
    pub status: String,
}

#[derive(Deserialize)]
pub struct WebhookPayload {
    pub notification_id: String,
    pub channel: String,
    pub status: String,
}

#[derive(Clone)]
pub struct AppState {
    pub notification_service: Arc<NotificationService>,
    pub status_service: Arc<NotificationStatusService>,
}

pub fn create_router(state: AppState) -> Router {
    Router::new()
        .route("/notify", post(send_notification))
        .route("/status/:id", get(get_status))
        .route("/metrics", get(get_metrics))
        .route("/webhook/callback", post(handle_webhook))
        .with_state(state)
}

async fn send_notification(
    State(state): State<AppState>,
    Json(payload): Json<NotificationRequest>,
) -> Result<(StatusCode, Json<ApiResponse<EnqueueResponse>>), StatusCode> {
    match state.notification_service.enqueue_notification(payload.clone()).await {
        Ok(id) => {
            // Create initial status
            if let Err(e) = state.status_service.create_initial_status(&id, &payload.channels).await {
                tracing::error!("Failed to create initial status: {}", e);
            }
            
            Ok((
                StatusCode::ACCEPTED,
                Json(ApiResponse::success(EnqueueResponse {
                    notification_id: id,
                    status: "enqueued".to_string(),
                })),
            ))
        }
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

async fn get_status(
    State(state): State<AppState>,
    Path(id): Path<String>,
) -> Result<Json<NotificationStatus>, StatusCode> {
    match state.status_service.get_status(&id).await {
        Ok(Some(status)) => Ok(Json(status)),
        Ok(None) => Err(StatusCode::NOT_FOUND),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

async fn get_metrics(
    State(state): State<AppState>,
) -> Result<Json<NotificationMetrics>, StatusCode> {
    match state.status_service.get_metrics().await {
        Ok(metrics) => Ok(Json(metrics)),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}

#[derive(Serialize)]
struct WebhookResponse {
    status: String,
}

async fn handle_webhook(
    State(state): State<AppState>,
    Json(payload): Json<WebhookPayload>,
) -> Result<Json<WebhookResponse>, StatusCode> {
    match state.status_service.update_channel_status(
        &payload.notification_id,
        &payload.channel,
        &payload.status,
    ).await {
        Ok(_) => Ok(Json(WebhookResponse {
            status: "updated".to_string(),
        })),
        Err(_) => Err(StatusCode::INTERNAL_SERVER_ERROR),
    }
}
