use notification_service_rust::adapter::{ChannelFactory, EmailAdapter, SmsAdapter, PushAdapter, ChannelAdapter};

#[test]
fn test_channel_factory_new() {
    let factory = ChannelFactory::new();
    // Factory should have 3 adapters registered
    assert_eq!(factory.get_adapter("email").is_some(), true);
    assert_eq!(factory.get_adapter("sms").is_some(), true);
    assert_eq!(factory.get_adapter("push").is_some(), true);
}

#[test]
fn test_get_adapter_email() {
    let factory = ChannelFactory::new();
    let adapter = factory.get_adapter("email");
    assert!(adapter.is_some(), "Should return email adapter");
}

#[test]
fn test_get_adapter_sms() {
    let factory = ChannelFactory::new();
    let adapter = factory.get_adapter("sms");
    assert!(adapter.is_some(), "Should return sms adapter");
}

#[test]
fn test_get_adapter_push() {
    let factory = ChannelFactory::new();
    let adapter = factory.get_adapter("push");
    assert!(adapter.is_some(), "Should return push adapter");
}

#[test]
fn test_get_adapter_unsupported() {
    let factory = ChannelFactory::new();
    let adapter = factory.get_adapter("voice");
    assert!(adapter.is_none(), "Should return None for unsupported channel");
}

#[tokio::test]
async fn test_email_adapter_send() {
    let adapter = EmailAdapter;
    let result = adapter.send("user@example.com", "Test message").await;
    assert!(result.is_ok(), "Send should succeed");
}

#[tokio::test]
async fn test_sms_adapter_send() {
    let adapter = SmsAdapter;
    let result = adapter.send("+1234567890", "Test message").await;
    assert!(result.is_ok(), "Send should succeed");
}

#[tokio::test]
async fn test_push_adapter_send() {
    let adapter = PushAdapter;
    let result = adapter.send("device-token-123", "Test message").await;
    assert!(result.is_ok(), "Send should succeed");
}
