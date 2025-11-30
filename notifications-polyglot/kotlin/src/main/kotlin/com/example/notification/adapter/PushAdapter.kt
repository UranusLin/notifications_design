package com.example.notification.adapter

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PushAdapter : ChannelAdapter {
    private val logger = LoggerFactory.getLogger(PushAdapter::class.java)

    override fun supports(channel: String): Boolean {
        return "push".equals(channel, ignoreCase = true)
    }

    override fun send(recipientId: String, message: String) {
        logger.info("[Push] Sending to {}: {}", recipientId, message)
        Thread.sleep(100) // Simulate latency
    }
}
