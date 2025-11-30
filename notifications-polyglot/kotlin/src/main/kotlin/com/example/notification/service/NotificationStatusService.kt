package com.example.notification.service

import com.example.notification.dto.NotificationMetrics
import com.example.notification.entity.NotificationStatus
import com.example.notification.repository.NotificationStatusRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class NotificationStatusService(
    private val repository: NotificationStatusRepository
) {

    @Transactional
    fun createInitialStatus(notificationId: String, channels: List<String>) {
        val channelStatuses = channels.associateWith { "PENDING" }.toMutableMap()
        
        val status = NotificationStatus(
            notificationId = notificationId,
            status = "ENQUEUED",
            channelStatuses = channelStatuses
        )
        
        repository.save(status)
    }

    fun getStatus(notificationId: String): Optional<NotificationStatus> {
        return repository.findById(notificationId)
    }

    fun getMetrics(): NotificationMetrics {
        val stats = repository.countByStatus()
        
        var total: Long = 0
        var success: Long = 0
        var failed: Long = 0
        val byStatus = mutableMapOf<String, Long>()

        for (row in stats) {
            val status = row[0] as String
            val count = (row[1] as Number).toLong()
            
            byStatus[status] = count
            total += count
            
            if ("COMPLETED".equals(status, ignoreCase = true)) {
                success += count
            } else if ("FAILED".equals(status, ignoreCase = true)) {
                failed += count
            }
        }

        return NotificationMetrics(total, success, failed, byStatus)
    }

    @Transactional
    fun updateChannelStatus(notificationId: String, channel: String, newStatus: String) {
        repository.findById(notificationId).ifPresent { status ->
            status.channelStatuses[channel] = newStatus
            
            val allCompleted = status.channelStatuses.values.all { "COMPLETED".equals(it, ignoreCase = true) }
            val anyFailed = status.channelStatuses.values.any { "FAILED".equals(it, ignoreCase = true) }
            
            if (allCompleted) {
                status.status = "COMPLETED"
            } else if (anyFailed) {
                status.status = "PARTIAL_FAILURE"
            } else {
                status.status = "PROCESSING"
            }
            
            repository.save(status)
        }
    }
}
