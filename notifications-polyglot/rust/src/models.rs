use chrono::{DateTime, Utc};
use serde::{Deserialize, Serialize};
use sqlx::FromRow;
use std::collections::HashMap;

#[derive(Debug, Clone, Serialize, Deserialize, FromRow)]
pub struct NotificationStatus {
    pub notification_id: String,
    pub status: String,
    pub channel_statuses: sqlx::types::Json<HashMap<String, String>>,
    pub created_at: DateTime<Utc>,
    pub updated_at: DateTime<Utc>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct NotificationMetrics {
    pub total_sent: i64,
    pub total_success: i64,
    pub total_failed: i64,
    pub by_status: HashMap<String, i64>,
}
