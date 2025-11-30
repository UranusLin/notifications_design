package com.example.notification.service;

import com.example.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final NotificationStatusService statusService;
    private static final String TOPIC = "notifications";

    public String enqueueNotification(NotificationRequest request) {
        String notificationId = UUID.randomUUID().toString();
        log.info("Enqueuing notification: {}", notificationId);
        
        statusService.createInitialStatus(notificationId, request.getChannels());
        
        kafkaTemplate.send(TOPIC, notificationId, request);
        return notificationId;
    }
}
