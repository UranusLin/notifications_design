package com.example.notification.worker

import com.example.notification.adapter.ChannelFactory
import com.example.notification.dto.NotificationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.notification.service.NotificationStatusService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class NotificationWorker(
    private val channelFactory: ChannelFactory,
    private val statusService: NotificationStatusService
) {

    private val logger = LoggerFactory.getLogger(NotificationWorker::class.java)

    @KafkaListener(topics = ["notifications"], groupId = "notification-workers")
    fun listen(record: ConsumerRecord<String, NotificationRequest>) {
        val notificationId = record.key()
        val request = record.value()

        logger.info("Processing notification [{}]: sending to channels {}", notificationId, request.channels)

        for (channel in request.channels) {
            try {
                statusService.updateChannelStatus(notificationId, channel, "PROCESSING")
                val adapter = channelFactory.getAdapter(channel)
                for (recipientId in request.recipientIds) {
                    adapter.send(recipientId, request.message)
                }
                statusService.updateChannelStatus(notificationId, channel, "COMPLETED")
            } catch (e: Exception) {
                logger.error("Failed to process channel {} for notification [{}]", channel, notificationId, e)
                statusService.updateChannelStatus(notificationId, channel, "FAILED")
            }
        }

        logger.info("Notification [{}] processed successfully", notificationId)
    }
}
