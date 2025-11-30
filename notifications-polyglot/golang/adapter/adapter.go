package adapter

import (
	"fmt"
	"log"
	"time"
)

type ChannelAdapter interface {
	Supports(channel string) bool
	Send(recipientID string, message string) error
}

type EmailAdapter struct{}

func (a *EmailAdapter) Supports(channel string) bool {
	return channel == "email"
}

func (a *EmailAdapter) Send(recipientID string, message string) error {
	log.Printf("[Email] Sending to %s: %s", recipientID, message)
	time.Sleep(200 * time.Millisecond) // Simulate latency
	return nil
}

type SmsAdapter struct{}

func (a *SmsAdapter) Supports(channel string) bool {
	return channel == "sms"
}

func (a *SmsAdapter) Send(recipientID string, message string) error {
	log.Printf("[SMS] Sending to %s: %s", recipientID, message)
	time.Sleep(500 * time.Millisecond) // Simulate latency
	return nil
}

type PushAdapter struct{}

func (a *PushAdapter) Supports(channel string) bool {
	return channel == "push"
}

func (a *PushAdapter) Send(recipientID string, message string) error {
	log.Printf("[Push] Sending to %s: %s", recipientID, message)
	time.Sleep(100 * time.Millisecond) // Simulate latency
	return nil
}

type ChannelFactory struct {
	adapters []ChannelAdapter
}

func NewChannelFactory() *ChannelFactory {
	return &ChannelFactory{
		adapters: []ChannelAdapter{
			&EmailAdapter{},
			&SmsAdapter{},
			&PushAdapter{},
		},
	}
}

func (f *ChannelFactory) GetAdapter(channel string) (ChannelAdapter, error) {
	for _, adapter := range f.adapters {
		if adapter.Supports(channel) {
			return adapter, nil
		}
	}
	return nil, fmt.Errorf("unsupported channel: %s", channel)
}
