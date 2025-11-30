package service

import (
	"encoding/json"
	"log"

	"github.com/IBM/sarama"
	"github.com/google/uuid"
)

type NotificationRequest struct {
	Channels     []string               `json:"channels"`
	RecipientIDs []string               `json:"recipientIds"`
	Message      string                 `json:"message"`
	Metadata     map[string]interface{} `json:"metadata"`
}

type NotificationService struct {
	producer      sarama.SyncProducer
	topic         string
	statusService *NotificationStatusService
}

func NewNotificationService(brokers []string, statusService *NotificationStatusService) (*NotificationService, error) {
	config := sarama.NewConfig()
	config.Producer.Return.Successes = true
	config.Producer.RequiredAcks = sarama.WaitForAll

	producer, err := sarama.NewSyncProducer(brokers, config)
	if err != nil {
		return nil, err
	}

	return &NotificationService{
		producer:      producer,
		topic:         "notifications",
		statusService: statusService,
	}, nil
}

func (s *NotificationService) EnqueueNotification(req NotificationRequest) (string, error) {
	id := uuid.New().String()
	log.Printf("Enqueuing notification: %s", id)

	// Create initial status
	if err := s.statusService.CreateInitialStatus(id, req.Channels); err != nil {
		log.Printf("Failed to create initial status for %s: %v", id, err)
		// Continue anyway, or return error depending on requirements.
		// For now, we log and continue to ensure notification is sent.
	}

	bytes, err := json.Marshal(req)
	if err != nil {
		return "", err
	}

	msg := &sarama.ProducerMessage{
		Topic: s.topic,
		Key:   sarama.StringEncoder(id),
		Value: sarama.ByteEncoder(bytes),
	}

	_, _, err = s.producer.SendMessage(msg)
	if err != nil {
		return "", err
	}

	return id, nil
}

func (s *NotificationService) Close() {
	s.producer.Close()
}
