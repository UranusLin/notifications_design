package com.example.notification.adapter;

public interface ChannelAdapter {
    boolean supports(String channel);
    void send(String recipientId, String message);
}
