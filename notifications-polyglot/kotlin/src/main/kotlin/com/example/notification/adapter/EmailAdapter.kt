package com.example.notification.adapter

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EmailAdapter : ChannelAdapter {
    private val logger = LoggerFactory.getLogger(EmailAdapter::class.java)

    override fun supports(channel: String): Boolean {
        return "email".equals(channel, ignoreCase = true)
    }

    override fun send(recipientId: String, message: String) {
        logger.info("[Email] Sending to {}: {}", recipientId, message)
        Thread.sleep(200) // Simulate latency
    }
}
