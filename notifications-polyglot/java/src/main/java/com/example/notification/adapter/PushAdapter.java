package com.example.notification.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PushAdapter implements ChannelAdapter {

    @Override
    public boolean supports(String channel) {
        return "push".equalsIgnoreCase(channel);
    }

    @Override
    public void send(String recipientId, String message) {
        log.info("[Push] Sending to {}: {}", recipientId, message);
        try {
            Thread.sleep(100); // Simulate latency
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
