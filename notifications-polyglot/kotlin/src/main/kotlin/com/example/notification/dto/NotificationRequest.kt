package com.example.notification.dto

data class NotificationRequest(
    val channels: List<String>,
    val recipientIds: List<String>,
    val message: String,
    val metadata: Map<String, Any> = emptyMap()
)
