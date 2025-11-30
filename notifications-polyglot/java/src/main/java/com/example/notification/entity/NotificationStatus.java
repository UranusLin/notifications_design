package com.example.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notification_status")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationStatus {

    @Id
    private String notificationId;

    private String status; // ENQUEUED, PROCESSING, COMPLETED, FAILED

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_channel_status", joinColumns = @JoinColumn(name = "notification_id"))
    @MapKeyColumn(name = "channel")
    @Column(name = "status")
    private Map<String, String> channelStatuses;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
