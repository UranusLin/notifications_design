use rdkafka::config::ClientConfig;
use rdkafka::producer::{FutureProducer, FutureRecord};
use rdkafka::util::Timeout;
use std::time::Duration;
use uuid::Uuid;
use crate::api::NotificationRequest;

pub struct NotificationService {
    producer: FutureProducer,
    topic: String,
}

impl NotificationService {
    pub async fn new(brokers: &str) -> anyhow::Result<Self> {
        let producer: FutureProducer = ClientConfig::new()
            .set("bootstrap.servers", brokers)
            .set("message.timeout.ms", "5000")
            .create()?;

        Ok(Self {
            producer,
            topic: "notifications".to_string(),
        })
    }

    pub async fn enqueue_notification(&self, request: NotificationRequest) -> anyhow::Result<String> {
        let id = Uuid::new_v4().to_string();
        tracing::info!("Enqueuing notification: {}", id);

        let payload = serde_json::to_string(&request)?;
        let record = FutureRecord::to(&self.topic)
            .key(&id)
            .payload(&payload);

        self.producer
            .send(record, Timeout::After(Duration::from_secs(0)))
            .await
            .map_err(|(e, _)| e)?;

        Ok(id)
    }
}
