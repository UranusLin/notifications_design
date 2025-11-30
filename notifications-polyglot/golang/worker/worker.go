package worker

import (
	"context"
	"encoding/json"
	"log"
	"time"

	"github.com/IBM/sarama"
	"github.com/example/notification-service-go/adapter"
	"github.com/example/notification-service-go/service"
)

type NotificationWorker struct {
	brokers       []string
	group         string
	topic         string
	statusService *service.NotificationStatusService
}

func NewNotificationWorker(brokers []string, group string, statusService *service.NotificationStatusService) *NotificationWorker {
	return &NotificationWorker{
		brokers:       brokers,
		group:         group,
		topic:         "notifications",
		statusService: statusService,
	}
}

func (w *NotificationWorker) Start() {
	config := sarama.NewConfig()
	config.Consumer.Group.Rebalance.Strategy = sarama.NewBalanceStrategyRoundRobin()
	config.Consumer.Offsets.Initial = sarama.OffsetOldest

	consumer, err := sarama.NewConsumerGroup(w.brokers, w.group, config)
	if err != nil {
		log.Fatalf("Error creating consumer group client: %v", err)
	}

	handler := &ConsumerHandler{
		statusService: w.statusService,
	}

	for {
		if err := consumer.Consume(context.Background(), []string{w.topic}, handler); err != nil {
			log.Printf("Error from consumer: %v", err)
			time.Sleep(2 * time.Second)
		}
	}
}

type ConsumerHandler struct {
	statusService *service.NotificationStatusService
}

func (ConsumerHandler) Setup(_ sarama.ConsumerGroupSession) error   { return nil }
func (ConsumerHandler) Cleanup(_ sarama.ConsumerGroupSession) error { return nil }
func (h ConsumerHandler) ConsumeClaim(sess sarama.ConsumerGroupSession, claim sarama.ConsumerGroupClaim) error {
	factory := adapter.NewChannelFactory()

	for msg := range claim.Messages() {
		var req service.NotificationRequest
		if err := json.Unmarshal(msg.Value, &req); err != nil {
			log.Printf("Failed to unmarshal message: %v", err)
			sess.MarkMessage(msg, "")
			continue
		}

		id := string(msg.Key)
		log.Printf("Processing notification [%s]: sending to channels %v", id, req.Channels)

		for _, channel := range req.Channels {
			// Update status to PROCESSING
			if err := h.statusService.UpdateChannelStatus(id, channel, "PROCESSING"); err != nil {
				log.Printf("Failed to update status to PROCESSING for %s channel %s: %v", id, channel, err)
			}

			adp, err := factory.GetAdapter(channel)
			if err != nil {
				log.Printf("Error getting adapter for channel %s: %v", channel, err)
				h.statusService.UpdateChannelStatus(id, channel, "FAILED")
				continue
			}

			success := true
			for _, recipientID := range req.RecipientIDs {
				if err := adp.Send(recipientID, req.Message); err != nil {
					log.Printf("Error sending to %s via %s: %v", recipientID, channel, err)
					success = false
				}
			}

			if success {
				h.statusService.UpdateChannelStatus(id, channel, "COMPLETED")
			} else {
				h.statusService.UpdateChannelStatus(id, channel, "FAILED")
			}
		}

		log.Printf("Notification [%s] processed successfully", id)

		sess.MarkMessage(msg, "")
	}
	return nil
}
