package com.example.notification.dto

data class NotificationMetrics(
    val totalSent: Long,
    val totalSuccess: Long,
    val totalFailed: Long,
    val byStatus: Map<String, Long>
)
