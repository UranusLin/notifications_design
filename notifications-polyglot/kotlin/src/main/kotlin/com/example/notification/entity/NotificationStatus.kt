package com.example.notification.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "notification_status")
data class NotificationStatus(
    @Id
    val notificationId: String,

    var status: String, // ENQUEUED, PROCESSING, COMPLETED, FAILED

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "notification_channel_status", joinColumns = [JoinColumn(name = "notification_id")])
    @MapKeyColumn(name = "channel")
    @Column(name = "status")
    val channelStatuses: MutableMap<String, String> = mutableMapOf(),

    @CreationTimestamp
    var createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    var updatedAt: LocalDateTime? = null
) {
    // No-arg constructor for JPA
    constructor() : this("", "", mutableMapOf())
}
