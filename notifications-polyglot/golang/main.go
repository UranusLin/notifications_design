package main

import (
	"github.com/example/notification-service-go/api"
	"github.com/example/notification-service-go/pkg/logger"
	"github.com/example/notification-service-go/pkg/middleware"
	"github.com/example/notification-service-go/service"
	"github.com/example/notification-service-go/worker"
	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func main() {
	// Initialize Logger
	logger.Init()
	defer logger.Log.Sync()

	// Initialize Database
	dsn := "host=localhost user=postgres password=postgres dbname=notification_db port=5432 sslmode=disable"
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		logger.Log.Fatal("Failed to connect to database", zap.Error(err))
	}

	// Migrate the schema
	db.AutoMigrate(&service.NotificationStatus{})

	// Initialize Services
	statusSvc := service.NewNotificationStatusService(db)
	svc, err := service.NewNotificationService([]string{"localhost:9092"}, statusSvc)
	if err != nil {
		logger.Log.Fatal("Failed to create service", zap.Error(err))
	}
	defer svc.Close()

	// Start Worker (Consumer) in a goroutine
	w := worker.NewNotificationWorker([]string{"localhost:9092"}, "notification-workers-go", statusSvc)
	go w.Start()

	// Initialize API Server
	r := gin.New() // Use New() instead of Default() to skip default logger
	r.Use(gin.Recovery())
	r.Use(middleware.Logger())

	api.RegisterRoutes(r, svc, statusSvc)

	logger.Log.Info("Server starting on :8082")
	if err := r.Run(":8082"); err != nil {
		logger.Log.Fatal("Server failed", zap.Error(err))
	}
}
