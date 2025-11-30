package adapter

import (
	"testing"
)

func TestNewChannelFactory(t *testing.T) {
	factory := NewChannelFactory()
	if factory == nil {
		t.Fatal("Factory should not be nil")
	}
	if len(factory.adapters) != 3 {
		t.Fatalf("Expected 3 adapters, got %d", len(factory.adapters))
	}
}

func TestGetAdapter_Email(t *testing.T) {
	factory := NewChannelFactory()
	adapter, err := factory.GetAdapter("email")
	if err != nil {
		t.Fatalf("Failed to get email adapter: %v", err)
	}
	if !adapter.Supports("email") {
		t.Error("Email adapter should support 'email' channel")
	}
}

func TestGetAdapter_SMS(t *testing.T) {
	factory := NewChannelFactory()
	adapter, err := factory.GetAdapter("sms")
	if err != nil {
		t.Fatalf("Failed to get sms adapter: %v", err)
	}
	if !adapter.Supports("sms") {
		t.Error("SMS adapter should support 'sms' channel")
	}
}

func TestGetAdapter_Push(t *testing.T) {
	factory := NewChannelFactory()
	adapter, err := factory.GetAdapter("push")
	if err != nil {
		t.Fatalf("Failed to get push adapter: %v", err)
	}
	if !adapter.Supports("push") {
		t.Error("Push adapter should support 'push' channel")
	}
}

func TestGetAdapter_Unsupported(t *testing.T) {
	factory := NewChannelFactory()
	_, err := factory.GetAdapter("voice")
	if err == nil {
		t.Error("Expected error for unsupported channel")
	}
}

func TestEmailAdapter_Send(t *testing.T) {
	adapter := &EmailAdapter{}
	err := adapter.Send("user@example.com", "Test message")
	if err != nil {
		t.Errorf("Send should not return error: %v", err)
	}
}

func TestSmsAdapter_Send(t *testing.T) {
	adapter := &SmsAdapter{}
	err := adapter.Send("+1234567890", "Test message")
	if err != nil {
		t.Errorf("Send should not return error: %v", err)
	}
}

func TestPushAdapter_Send(t *testing.T) {
	adapter := &PushAdapter{}
	err := adapter.Send("device-token-123", "Test message")
	if err != nil {
		t.Errorf("Send should not return error: %v", err)
	}
}
