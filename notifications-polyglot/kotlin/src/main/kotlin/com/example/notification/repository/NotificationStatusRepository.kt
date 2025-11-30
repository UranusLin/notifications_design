package com.example.notification.repository

import com.example.notification.entity.NotificationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NotificationStatusRepository : JpaRepository<NotificationStatus, String> {
    
    @Query("SELECT s.status, COUNT(s) FROM NotificationStatus s GROUP BY s.status")
    fun countByStatus(): List<Array<Any>>
}
