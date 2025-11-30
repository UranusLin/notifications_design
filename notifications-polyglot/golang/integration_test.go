package main

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/example/notification-service-go/api"
	"github.com/example/notification-service-go/service"
	"github.com/example/notification-service-go/worker"
	"github.com/gin-gonic/gin"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/modules/kafka"
	"github.com/testcontainers/testcontainers-go/modules/postgres"
	"github.com/testcontainers/testcontainers-go/wait"
	postgresDriver "gorm.io/driver/postgres"
	"gorm.io/gorm"
)

func TestNotificationIntegration(t *testing.T) {
	if testing.Short() {
		t.Skip("Skipping integration test in short mode")
	}

	ctx := context.Background()

	// Start Kafka container
	kafkaContainer, err := kafka.Run(ctx,
		"confluentinc/confluent-local:7.5.0",
		kafka.WithClusterID("test-cluster"),
	)
	if err != nil {
		t.Fatalf("Failed to start Kafka container: %v", err)
	}
	defer func() {
		if err := testcontainers.TerminateContainer(kafkaContainer); err != nil {
			t.Fatalf("Failed to terminate Kafka container: %v", err)
		}
	}()

	// Get Kafka bootstrap servers
	brokers, err := kafkaContainer.Brokers(ctx)
	if err != nil {
		t.Fatalf("Failed to get Kafka brokers: %v", err)
	}

	// Start Postgres container
	postgresContainer, err := postgres.Run(ctx,
		"postgres:15-alpine",
		postgres.WithDatabase("testdb"),
		postgres.WithUsername("testuser"),
		postgres.WithPassword("testpass"),
		testcontainers.WithWaitStrategy(
			wait.ForLog("database system is ready to accept connections").
				WithOccurrence(2).
				WithStartupTimeout(5*time.Second)),
	)
	if err != nil {
		t.Fatalf("Failed to start Postgres container: %v", err)
	}
	defer func() {
		if err := testcontainers.TerminateContainer(postgresContainer); err != nil {
			t.Fatalf("Failed to terminate Postgres container: %v", err)
		}
	}()

	// Get Postgres connection string
	dsn, err := postgresContainer.ConnectionString(ctx, "sslmode=disable")
	if err != nil {
		t.Fatalf("Failed to get Postgres connection string: %v", err)
	}

	// Initialize Database
	db, err := gorm.Open(postgresDriver.Open(dsn), &gorm.Config{})
	if err != nil {
		t.Fatalf("Failed to connect to database: %v", err)
	}
	db.AutoMigrate(&service.NotificationStatus{})

	// Initialize Services
	statusSvc := service.NewNotificationStatusService(db)
	notifService, err := service.NewNotificationService(brokers, statusSvc)
	if err != nil {
		t.Fatalf("Failed to create notification service: %v", err)
	}
	defer notifService.Close()

	// Start Worker
	worker := worker.NewNotificationWorker(brokers, "test-group", statusSvc)
	go worker.Start()

	// Setup Gin router
	gin.SetMode(gin.TestMode)
	router := gin.Default()
	api.RegisterRoutes(router, notifService, statusSvc)

	// Test notification enqueue
	var notificationID string
	t.Run("should enqueue notification successfully", func(t *testing.T) {
		reqBody := service.NotificationRequest{
			Channels:     []string{"email", "sms"},
			RecipientIDs: []string{"user123"},
			Message:      "Go integration test",
		}
		jsonBody, _ := json.Marshal(reqBody)

		req := httptest.NewRequest(http.MethodPost, "/notifications", bytes.NewBuffer(jsonBody))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()

		router.ServeHTTP(w, req)

		if w.Code != http.StatusAccepted {
			t.Errorf("Expected status %d, got %d", http.StatusAccepted, w.Code)
		}

		var response map[string]interface{}
		if err := json.Unmarshal(w.Body.Bytes(), &response); err != nil {
			t.Fatalf("Failed to unmarshal response: %v", err)
		}

		if response["id"] == nil {
			t.Error("Expected id in response")
		}
		notificationID = response["id"].(string)
	})

	// Wait for worker to process
	time.Sleep(5 * time.Second)

	// Test status endpoint
	t.Run("should get notification status", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodGet, "/status/"+notificationID, nil)
		w := httptest.NewRecorder()

		router.ServeHTTP(w, req)

		if w.Code != http.StatusOK {
			t.Errorf("Expected status %d, got %d", http.StatusOK, w.Code)
		}

		var status service.NotificationStatus
		if err := json.Unmarshal(w.Body.Bytes(), &status); err != nil {
			t.Fatalf("Failed to unmarshal status: %v", err)
		}

		if status.NotificationID != notificationID {
			t.Errorf("Expected ID %s, got %s", notificationID, status.NotificationID)
		}
		// Since we don't have real channel adapters in test, it might be FAILED or PROCESSING
		// But we just want to verify the endpoint works
		t.Logf("Notification Status: %s", status.Status)
	})

	// Test metrics endpoint
	t.Run("should get metrics", func(t *testing.T) {
		req := httptest.NewRequest(http.MethodGet, "/metrics", nil)
		w := httptest.NewRecorder()

		router.ServeHTTP(w, req)

		if w.Code != http.StatusOK {
			t.Errorf("Expected status %d, got %d", http.StatusOK, w.Code)
		}

		var metrics service.NotificationMetrics
		if err := json.Unmarshal(w.Body.Bytes(), &metrics); err != nil {
			t.Fatalf("Failed to unmarshal metrics: %v", err)
		}

		if metrics.TotalSent == 0 {
			t.Error("Expected TotalSent > 0")
		}
	})
}
