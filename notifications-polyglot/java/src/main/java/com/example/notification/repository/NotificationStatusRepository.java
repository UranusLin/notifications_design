package com.example.notification.repository;

import com.example.notification.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface NotificationStatusRepository extends JpaRepository<NotificationStatus, String> {
    
    @Query("SELECT s.status, COUNT(s) FROM NotificationStatus s GROUP BY s.status")
    List<Object[]> countByStatus();
}
