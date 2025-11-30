package api

import (
	"net/http"

	"github.com/example/notification-service-go/service"
	"github.com/gin-gonic/gin"
)

type Handler struct {
	notificationService *service.NotificationService
	statusService       *service.NotificationStatusService
}

func NewHandler(notificationService *service.NotificationService, statusService *service.NotificationStatusService) *Handler {
	return &Handler{
		notificationService: notificationService,
		statusService:       statusService,
	}
}

func (h *Handler) SendNotification(c *gin.Context) {
	var req service.NotificationRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	id, err := h.notificationService.EnqueueNotification(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusAccepted, gin.H{"id": id, "status": "accepted"})
}

func (h *Handler) GetStatus(c *gin.Context) {
	id := c.Param("id")
	status, err := h.statusService.GetStatus(id)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Notification not found"})
		return
	}
	c.JSON(http.StatusOK, status)
}

func (h *Handler) GetMetrics(c *gin.Context) {
	metrics, err := h.statusService.GetMetrics()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, metrics)
}

func (h *Handler) HandleWebhook(c *gin.Context) {
	var payload struct {
		NotificationID string `json:"notification_id"`
		Channel        string `json:"channel"`
		Status         string `json:"status"`
	}

	if err := c.ShouldBindJSON(&payload); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	if err := h.statusService.UpdateChannelStatus(payload.NotificationID, payload.Channel, payload.Status); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"status": "updated"})
}
