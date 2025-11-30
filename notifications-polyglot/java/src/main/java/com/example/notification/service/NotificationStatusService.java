package com.example.notification.service;

import com.example.notification.dto.NotificationMetrics;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.repository.NotificationStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationStatusService {

    private final NotificationStatusRepository repository;

    @Transactional
    public void createInitialStatus(String notificationId, List<String> channels) {
        Map<String, String> channelStatuses = new HashMap<>();
        for (String channel : channels) {
            channelStatuses.put(channel, "PENDING");
        }
        
        NotificationStatus status = new NotificationStatus();
        status.setNotificationId(notificationId);
        status.setStatus("ENQUEUED");
        status.setChannelStatuses(channelStatuses);
        
        repository.save(status);
    }

    public Optional<NotificationStatus> getStatus(String notificationId) {
        return repository.findById(notificationId);
    }

    public NotificationMetrics getMetrics() {
        List<Object[]> stats = repository.countByStatus();
        
        long total = 0;
        long success = 0;
        long failed = 0;
        Map<String, Long> byStatus = new HashMap<>();

        for (Object[] row : stats) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            
            byStatus.put(status, count);
            total += count;
            
            if ("COMPLETED".equalsIgnoreCase(status)) {
                success += count;
            } else if ("FAILED".equalsIgnoreCase(status)) {
                failed += count;
            }
        }

        return new NotificationMetrics(total, success, failed, byStatus);
    }

    @Transactional
    public void updateChannelStatus(String notificationId, String channel, String newStatus) {
        System.out.println("Updating status for " + notificationId + " channel " + channel + " to " + newStatus);
        repository.findById(notificationId).ifPresent(status -> {
            status.getChannelStatuses().put(channel, newStatus);
            
            // Simple aggregation logic for overall status
            boolean allCompleted = status.getChannelStatuses().values().stream()
                    .allMatch(s -> "COMPLETED".equalsIgnoreCase(s));
            boolean anyFailed = status.getChannelStatuses().values().stream()
                    .anyMatch(s -> "FAILED".equalsIgnoreCase(s));
            
            if (allCompleted) {
                status.setStatus("COMPLETED");
            } else if (anyFailed) {
                status.setStatus("PARTIAL_FAILURE");
            } else {
                status.setStatus("PROCESSING");
            }
            System.out.println("New overall status for " + notificationId + ": " + status.getStatus());
            repository.save(status);
        });
    }
}
