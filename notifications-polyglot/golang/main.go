package main

import (
	"os"

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
	logger.Init()
	defer logger.Log.Sync()

	dsn := os.Getenv("DATABASE_URL")
	if dsn == "" {
		dsn = "host=localhost user=postgres password=postgres dbname=notification_db port=5432 sslmode=disable"
	}
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		logger.Log.Fatal("Failed to connect to database", zap.Error(err))
	}

	db.AutoMigrate(&service.NotificationStatus{})

	kafkaBrokers := []string{os.Getenv("KAFKA_BROKERS")}
	if kafkaBrokers[0] == "" {
		kafkaBrokers = []string{"localhost:9092"}
	}

	statusSvc := service.NewNotificationStatusService(db)
	svc, err := service.NewNotificationService(kafkaBrokers, statusSvc)
	if err != nil {
		logger.Log.Fatal("Failed to create service", zap.Error(err))
	}
	defer svc.Close()

	w := worker.NewNotificationWorker(kafkaBrokers, "notification-workers-go", statusSvc)
	go w.Start()

	r := gin.New()
	r.Use(gin.Recovery())
	r.Use(middleware.Logger())

	api.RegisterRoutes(r, svc, statusSvc)

	logger.Log.Info("Server starting on :8082")
	if err := r.Run(":8082"); err != nil {
		logger.Log.Fatal("Server failed", zap.Error(err))
	}
}
