package com.example.notification.worker;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.adapter.ChannelFactory;
import com.example.notification.adapter.ChannelAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationWorker {

    private final ChannelFactory channelFactory;
    private final com.example.notification.service.NotificationStatusService statusService;

    public NotificationWorker(ChannelFactory channelFactory, com.example.notification.service.NotificationStatusService statusService) {
        this.channelFactory = channelFactory;
        this.statusService = statusService;
    }

    @KafkaListener(topics = "notifications", groupId = "notification-workers")
    public void listen(ConsumerRecord<String, NotificationRequest> record) {
        String notificationId = record.key();
        NotificationRequest request = record.value();
        
        log.info("Processing notification [{}]: sending to channels {}", notificationId, request.getChannels());
        
        for (String channel : request.getChannels()) {
            try {
                statusService.updateChannelStatus(notificationId, channel, "PROCESSING");
                ChannelAdapter adapter = channelFactory.getAdapter(channel);
                for (String recipientId : request.getRecipientIds()) {
                    adapter.send(recipientId, request.getMessage());
                }
                statusService.updateChannelStatus(notificationId, channel, "COMPLETED");
            } catch (Exception e) {
                log.error("Failed to process channel {} for notification [{}]", channel, notificationId, e);
                statusService.updateChannelStatus(notificationId, channel, "FAILED");
            }
        }
        
        log.info("Notification [{}] processed successfully", notificationId);
    }
}
