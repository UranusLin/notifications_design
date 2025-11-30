package service

import (
	"strings"
	"time"

	"gorm.io/gorm"
)

type NotificationStatusService struct {
	db *gorm.DB
}

func NewNotificationStatusService(db *gorm.DB) *NotificationStatusService {
	return &NotificationStatusService{db: db}
}

func (s *NotificationStatusService) CreateInitialStatus(notificationID string, channels []string) error {
	channelStatuses := make(map[string]string)
	for _, channel := range channels {
		channelStatuses[channel] = "PENDING"
	}

	status := &NotificationStatus{
		NotificationID:  notificationID,
		Status:          "ENQUEUED",
		ChannelStatuses: channelStatuses,
		CreatedAt:       time.Now(),
		UpdatedAt:       time.Now(),
	}

	return s.db.Create(status).Error
}

func (s *NotificationStatusService) GetStatus(notificationID string) (*NotificationStatus, error) {
	var status NotificationStatus
	err := s.db.First(&status, "notification_id = ?", notificationID).Error
	if err != nil {
		return nil, err
	}
	return &status, nil
}

func (s *NotificationStatusService) UpdateChannelStatus(notificationID, channel, newStatus string) error {
	var status NotificationStatus
	if err := s.db.First(&status, "notification_id = ?", notificationID).Error; err != nil {
		return err
	}

	status.ChannelStatuses[channel] = newStatus

	// Aggregate overall status
	allCompleted := true
	anyFailed := false

	for _, s := range status.ChannelStatuses {
		if !strings.EqualFold(s, "COMPLETED") {
			allCompleted = false
		}
		if strings.EqualFold(s, "FAILED") {
			anyFailed = true
		}
	}

	if allCompleted {
		status.Status = "COMPLETED"
	} else if anyFailed {
		status.Status = "PARTIAL_FAILURE"
	} else {
		status.Status = "PROCESSING"
	}

	status.UpdatedAt = time.Now()
	return s.db.Save(&status).Error
}

func (s *NotificationStatusService) GetMetrics() (*NotificationMetrics, error) {
	var results []struct {
		Status string
		Count  int64
	}

	err := s.db.Model(&NotificationStatus{}).
		Select("status, count(*) as count").
		Group("status").
		Scan(&results).Error

	if err != nil {
		return nil, err
	}

	counts := make(map[string]int64)
	var total, success, failed int64

	for _, r := range results {
		counts[r.Status] = r.Count
		total += r.Count
		if strings.EqualFold(r.Status, "COMPLETED") {
			success += r.Count
		} else if strings.EqualFold(r.Status, "FAILED") {
			failed += r.Count
		}
	}

	return &NotificationMetrics{
		TotalSent:    total,
		TotalSuccess: success,
		TotalFailed:  failed,
		ByStatus:     counts,
	}, nil
}
