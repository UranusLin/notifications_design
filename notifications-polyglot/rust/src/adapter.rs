use async_trait::async_trait;
use std::collections::HashMap;
use std::sync::Arc;
use tokio::time::{sleep, Duration};
use tracing::info;

#[async_trait]
pub trait ChannelAdapter: Send + Sync {
    #[allow(dead_code)]
    fn supports(&self, channel: &str) -> bool;
    async fn send(&self, recipient_id: &str, message: &str) -> Result<(), String>;
}

pub struct EmailAdapter;

#[async_trait]
impl ChannelAdapter for EmailAdapter {
    fn supports(&self, channel: &str) -> bool {
        channel == "email"
    }

    async fn send(&self, recipient_id: &str, message: &str) -> Result<(), String> {
        info!("[Email] Sending to {}: {}", recipient_id, message);
        sleep(Duration::from_millis(200)).await;
        Ok(())
    }
}

pub struct SmsAdapter;

#[async_trait]
impl ChannelAdapter for SmsAdapter {
    fn supports(&self, channel: &str) -> bool {
        channel == "sms"
    }

    async fn send(&self, recipient_id: &str, message: &str) -> Result<(), String> {
        info!("[SMS] Sending to {}: {}", recipient_id, message);
        sleep(Duration::from_millis(500)).await;
        Ok(())
    }
}

pub struct PushAdapter;

#[async_trait]
impl ChannelAdapter for PushAdapter {
    fn supports(&self, channel: &str) -> bool {
        channel == "push"
    }

    async fn send(&self, recipient_id: &str, message: &str) -> Result<(), String> {
        info!("[Push] Sending to {}: {}", recipient_id, message);
        sleep(Duration::from_millis(100)).await;
        Ok(())
    }
}

pub struct ChannelFactory {
    adapters: HashMap<String, Arc<dyn ChannelAdapter>>,
}

impl ChannelFactory {
    pub fn new() -> Self {
        let mut adapters: HashMap<String, Arc<dyn ChannelAdapter>> = HashMap::new();
        
        // Register adapters
        let email = Arc::new(EmailAdapter);
        let sms = Arc::new(SmsAdapter);
        let push = Arc::new(PushAdapter);

        adapters.insert("email".to_string(), email);
        adapters.insert("sms".to_string(), sms);
        adapters.insert("push".to_string(), push);

        Self { adapters }
    }

    pub fn get_adapter(&self, channel: &str) -> Option<Arc<dyn ChannelAdapter>> {
        self.adapters.get(channel).cloned()
    }
}
