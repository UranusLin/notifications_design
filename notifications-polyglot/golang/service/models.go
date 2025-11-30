package service

import "time"

type NotificationStatus struct {
	NotificationID  string            `json:"notification_id" gorm:"primaryKey"`
	Status          string            `json:"status"` // ENQUEUED, PROCESSING, COMPLETED, FAILED, PARTIAL_FAILURE
	ChannelStatuses map[string]string `json:"channel_statuses" gorm:"serializer:json"`
	CreatedAt       time.Time         `json:"created_at"`
	UpdatedAt       time.Time         `json:"updated_at"`
}

type NotificationMetrics struct {
	TotalSent    int64            `json:"total_sent"`
	TotalSuccess int64            `json:"total_success"`
	TotalFailed  int64            `json:"total_failed"`
	ByStatus     map[string]int64 `json:"by_status"`
}
