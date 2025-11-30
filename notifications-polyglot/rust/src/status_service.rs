use crate::models::{NotificationMetrics, NotificationStatus};
use sqlx::{PgPool, Row};
use std::collections::HashMap;

#[derive(Clone)]
pub struct NotificationStatusService {
    pool: PgPool,
}

impl NotificationStatusService {
    pub fn new(pool: PgPool) -> Self {
        Self { pool }
    }

    pub async fn create_initial_status(
        &self,
        notification_id: &str,
        channels: &[String],
    ) -> anyhow::Result<()> {
        let mut channel_statuses = HashMap::new();
        for channel in channels {
            channel_statuses.insert(channel.clone(), "PENDING".to_string());
        }

        sqlx::query(
            r#"
            INSERT INTO notification_status (notification_id, status, channel_statuses, created_at, updated_at)
            VALUES ($1, $2, $3, NOW(), NOW())
            "#,
        )
        .bind(notification_id)
        .bind("ENQUEUED")
        .bind(sqlx::types::Json(&channel_statuses))
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    pub async fn get_status(&self, notification_id: &str) -> anyhow::Result<Option<NotificationStatus>> {
        let status = sqlx::query_as::<_, NotificationStatus>(
            r#"
            SELECT notification_id, status, channel_statuses, created_at, updated_at
            FROM notification_status
            WHERE notification_id = $1
            "#,
        )
        .bind(notification_id)
        .fetch_optional(&self.pool)
        .await?;

        Ok(status)
    }

    pub async fn update_channel_status(
        &self,
        notification_id: &str,
        channel: &str,
        new_status: &str,
    ) -> anyhow::Result<()> {
        // Get current status
        let current = self.get_status(notification_id).await?;
        if current.is_none() {
            return Ok(());
        }

        let current = current.unwrap();
        let mut channel_statuses = current.channel_statuses.0;
        channel_statuses.insert(channel.to_string(), new_status.to_string());

        // Aggregate overall status
        let all_completed = channel_statuses.values().all(|s| s == "COMPLETED");
        let any_failed = channel_statuses.values().any(|s| s == "FAILED");

        let overall_status = if all_completed {
            "COMPLETED"
        } else if any_failed {
            "PARTIAL_FAILURE"
        } else {
            "PROCESSING"
        };

        sqlx::query(
            r#"
            UPDATE notification_status
            SET status = $1, channel_statuses = $2, updated_at = NOW()
            WHERE notification_id = $3
            "#,
        )
        .bind(overall_status)
        .bind(sqlx::types::Json(&channel_statuses))
        .bind(notification_id)
        .execute(&self.pool)
        .await?;

        Ok(())
    }

    pub async fn get_metrics(&self) -> anyhow::Result<NotificationMetrics> {
        let rows = sqlx::query(
            r#"
            SELECT status, COUNT(*) as count
            FROM notification_status
            GROUP BY status
            "#,
        )
        .fetch_all(&self.pool)
        .await?;

        let mut by_status = HashMap::new();
        let mut total = 0i64;
        let mut success = 0i64;
        let mut failed = 0i64;

        for row in rows {
            let status: String = row.get("status");
            let count: i64 = row.get("count");
            
            by_status.insert(status.clone(), count);
            total += count;
            
            if status == "COMPLETED" {
                success += count;
            } else if status == "FAILED" {
                failed += count;
            }
        }

        Ok(NotificationMetrics {
            total_sent: total,
            total_success: success,
            total_failed: failed,
            by_status,
        })
    }
}
