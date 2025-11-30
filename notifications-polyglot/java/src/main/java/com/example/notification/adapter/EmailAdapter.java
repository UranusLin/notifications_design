package com.example.notification.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailAdapter implements ChannelAdapter {

    @Override
    public boolean supports(String channel) {
        return "email".equalsIgnoreCase(channel);
    }

    @Override
    public void send(String recipientId, String message) {
        log.info("[Email] Sending to {}: {}", recipientId, message);
        try {
            Thread.sleep(200); // Simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
