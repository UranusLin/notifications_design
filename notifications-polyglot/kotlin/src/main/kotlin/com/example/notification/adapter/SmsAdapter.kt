package com.example.notification.adapter

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SmsAdapter : ChannelAdapter {
    private val logger = LoggerFactory.getLogger(SmsAdapter::class.java)

    override fun supports(channel: String): Boolean {
        return "sms".equals(channel, ignoreCase = true)
    }

    override fun send(recipientId: String, message: String) {
        logger.info("[SMS] Sending to {}: {}", recipientId, message)
        Thread.sleep(500) // Simulate latency
    }
}
