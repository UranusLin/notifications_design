package com.example.notification.adapter

import org.springframework.stereotype.Component

@Component
class ChannelFactory(private val adapters: List<ChannelAdapter>) {

    fun getAdapter(channel: String): ChannelAdapter {
        return adapters.firstOrNull { it.supports(channel) }
            ?: throw IllegalArgumentException("Unsupported channel: $channel")
    }
}
