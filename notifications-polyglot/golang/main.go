package main

import (
	"log"

	"github.com/example/notification-service-go/api"
	"github.com/example/notification-service-go/service"
	"github.com/example/notification-service-go/worker"
	"github.com/gin-gonic/gin"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func main() {
	// Initialize Database
	dsn := "host=localhost user=postgres password=postgres dbname=notification_db port=5432 sslmode=disable"
	db, err := gorm.Open(postgres.Open(dsn), &gorm.Config{})
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}

	// Migrate the schema
	db.AutoMigrate(&service.NotificationStatus{})

	// Initialize Services
	statusSvc := service.NewNotificationStatusService(db)
	svc, err := service.NewNotificationService([]string{"localhost:9092"}, statusSvc)
	if err != nil {
		log.Fatalf("Failed to create service: %v", err)
	}
	defer svc.Close()

	// Start Worker (Consumer) in a goroutine
	w := worker.NewNotificationWorker([]string{"localhost:9092"}, "notification-workers-go", statusSvc)
	go w.Start()

	// Initialize API Server
	r := gin.Default()
	api.RegisterRoutes(r, svc, statusSvc)

	log.Println("Server starting on :8082")
	if err := r.Run(":8082"); err != nil {
		log.Fatalf("Server failed: %v", err)
	}
}
