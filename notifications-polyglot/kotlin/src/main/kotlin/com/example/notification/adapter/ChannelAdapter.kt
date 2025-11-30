package com.example.notification.adapter

interface ChannelAdapter {
    fun supports(channel: String): Boolean
    fun send(recipientId: String, message: String)
}
