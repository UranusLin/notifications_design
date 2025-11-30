package com.example.notification.controller;

import com.example.notification.common.ApiResponse;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/notify")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, String>>> sendNotification(@RequestBody NotificationRequest request) {
        String id = notificationService.enqueueNotification(request);
        return ResponseEntity.accepted()
                .body(ApiResponse.success(Map.of("notification_id", id, "status", "enqueued")));
    }
}
