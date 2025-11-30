package com.example.notification.controller;

import com.example.notification.common.ApiResponse;
import com.example.notification.dto.NotificationMetrics;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.service.NotificationStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StatusController {

    private final NotificationStatusService statusService;

    @GetMapping("/status/{id}")
    public ResponseEntity<ApiResponse<NotificationStatus>> getStatus(@PathVariable String id) {
        return statusService.getStatus(id)
                .map(status -> ResponseEntity.ok(ApiResponse.success(status)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/metrics")
    public ResponseEntity<ApiResponse<NotificationMetrics>> getMetrics() {
        return ResponseEntity.ok(ApiResponse.success(statusService.getMetrics()));
    }
}
