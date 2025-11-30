package api

import (
	"net/http"

	"github.com/example/notification-service-go/logger"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

type ApiResponse struct {
	Success bool        `json:"success"`
	Message string      `json:"message"`
	Data    interface{} `json:"data,omitempty"`
}

func Success(c *gin.Context, data interface{}) {
	c.JSON(http.StatusAccepted, ApiResponse{
		Success: true,
		Message: "Success",
		Data:    data,
	})
}

func Error(c *gin.Context, code int, message string) {
	c.JSON(code, ApiResponse{
		Success: false,
		Message: message,
	})
}

func GlobalErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		if len(c.Errors) > 0 {
			err := c.Errors.Last()
			logger.Log.Error("Request failed", zap.Error(err.Err))
			Error(c, http.StatusInternalServerError, err.Error())
		}
	}
}
