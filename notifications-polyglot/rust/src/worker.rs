use rdkafka::config::ClientConfig;
use rdkafka::consumer::{Consumer, StreamConsumer};
use rdkafka::Message;
use crate::api::NotificationRequest;
use crate::adapter::ChannelFactory;
use crate::status_service::NotificationStatusService;
use std::sync::Arc;
use tracing::{info, warn, error};

pub async fn run_worker(
    brokers: &str,
    group_id: &str,
    status_service: Arc<NotificationStatusService>,
) -> anyhow::Result<()> {
    let factory = ChannelFactory::new();
    
    let consumer: StreamConsumer = ClientConfig::new()
        .set("group.id", group_id)
        .set("bootstrap.servers", brokers)
        .set("enable.partition.eof", "false")
        .set("session.timeout.ms", "6000")
        .set("enable.auto.commit", "true")
        .create()?;

    consumer.subscribe(&["notifications"])?;

    info!("Worker started, listening on topic 'notifications'");

    loop {
        match consumer.recv().await {
            Err(e) => warn!("Kafka error: {}", e),
            Ok(m) => {
                let payload = match m.payload_view::<str>() {
                    None => "",
                    Some(Ok(s)) => s,
                    Some(Err(e)) => {
                        warn!("Error while deserializing message payload: {:?}", e);
                        ""
                    }
                };
                
                let key = match m.key_view::<str>() {
                    None => "",
                    Some(Ok(s)) => s,
                    Some(Err(_)) => "",
                };

                info!("Processing notification [{}]: payload size {}", key, payload.len());

                if let Ok(req) = serde_json::from_str::<NotificationRequest>(payload) {
                    info!("Processing notification [{}]: sending to channels {:?}", key, req.channels);
                    
                    for channel in &req.channels {
                        // Update status to PROCESSING
                        if let Err(e) = status_service.update_channel_status(key, channel, "PROCESSING").await {
                            error!("Failed to update status to PROCESSING: {}", e);
                        }
                        
                        if let Some(adapter) = factory.get_adapter(channel) {
                            let mut success = true;
                            for recipient_id in &req.recipient_ids {
                                if let Err(e) = adapter.send(recipient_id, &req.message).await {
                                    error!("Failed to send to {} via {}: {}", recipient_id, channel, e);
                                    success = false;
                                }
                            }
                            
                            // Update status based on result
                            let channel_status = if success { "COMPLETED" } else { "FAILED" };
                            if let Err(e) = status_service.update_channel_status(key, channel, channel_status).await {
                                error!("Failed to update channel status: {}", e);
                            }
                        } else {
                            warn!("No adapter found for channel: {}", channel);
                            if let Err(e) = status_service.update_channel_status(key, channel, "FAILED").await {
                                error!("Failed to update status to FAILED: {}", e);
                            }
                        }
                    }
                    
                    info!("Notification [{}] processed successfully", key);
                } else {
                    error!("Failed to parse notification payload");
                }
            }
        };
    }
}
