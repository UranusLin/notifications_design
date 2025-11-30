package api

import (
	"github.com/example/notification-service-go/service"
	"github.com/gin-gonic/gin"
)

func RegisterRoutes(r *gin.Engine, notificationService *service.NotificationService, statusService *service.NotificationStatusService) {
	h := NewHandler(notificationService, statusService)

	r.POST("/notifications", h.SendNotification)
	r.GET("/status/:id", h.GetStatus)
	r.GET("/metrics", h.GetMetrics)
	r.POST("/webhook/callback", h.HandleWebhook)
}
