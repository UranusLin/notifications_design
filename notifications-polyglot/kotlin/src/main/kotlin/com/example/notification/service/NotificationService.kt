package com.example.notification.service

import com.example.notification.dto.NotificationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class NotificationService(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val statusService: NotificationStatusService // Added statusService
) {
    private val logger = LoggerFactory.getLogger(NotificationService::class.java) // Changed logger initialization
    private val topic = "notifications"

    suspend fun enqueueNotification(request: NotificationRequest): String { // Removed withContext(Dispatchers.IO)
        val notificationId = UUID.randomUUID().toString()
        logger.info("Enqueuing notification: {}", notificationId)
        
        // In a real app, we would save to DB here first with status PENDING
        statusService.createInitialStatus(notificationId, request.channels) // Added call to createInitialStatus
        
        kafkaTemplate.send(topic, notificationId, request) // Removed .get()
        return notificationId
    }
}
