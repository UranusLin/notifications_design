package com.example.notification.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SmsAdapter implements ChannelAdapter {

    @Override
    public boolean supports(String channel) {
        return "sms".equalsIgnoreCase(channel);
    }

    @Override
    public void send(String recipientId, String message) {
        log.info("[SMS] Sending to {}: {}", recipientId, message);
        try {
            Thread.sleep(500); // Simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
