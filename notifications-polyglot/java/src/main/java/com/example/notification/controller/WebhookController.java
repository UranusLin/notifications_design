package com.example.notification.controller;

import com.example.notification.common.ApiResponse;
import com.example.notification.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final NotificationStatusService statusService;

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<String>> handleCallback(@RequestBody Map<String, Object> payload) {
        log.info("Received webhook callback: {}", payload);
        
        // Mock implementation: Expecting payload to contain notification_id, channel, and status
        // In production, we would verify signatures here
        
        String notificationId = (String) payload.get("notification_id");
        String channel = (String) payload.get("channel");
        String status = (String) payload.get("status");
        
        if (notificationId != null && channel != null && status != null) {
            statusService.updateChannelStatus(notificationId, channel, status);
            return ResponseEntity.ok(ApiResponse.success("Callback processed"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid payload"));
        }
    }
}
