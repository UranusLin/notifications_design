package com.example.notification.controller

import com.example.notification.common.ApiResponse
import com.example.notification.dto.NotificationMetrics
import com.example.notification.entity.NotificationStatus
import com.example.notification.service.NotificationStatusService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusController(
    private val statusService: NotificationStatusService
) {

    @GetMapping("/status/{id}")
    fun getStatus(@PathVariable id: String): ResponseEntity<ApiResponse<NotificationStatus>> {
        return statusService.getStatus(id)
            .map { status -> ResponseEntity.ok(ApiResponse.success(status)) }
            .orElse(ResponseEntity.notFound().build())
    }

    @GetMapping("/metrics")
    fun getMetrics(): ResponseEntity<ApiResponse<NotificationMetrics>> {
        return ResponseEntity.ok(ApiResponse.success(statusService.getMetrics()))
    }
}
