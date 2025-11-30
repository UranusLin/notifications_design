package com.example.notification.controller

import com.example.notification.common.ApiResponse
import com.example.notification.dto.NotificationRequest
import com.example.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/notify")
class NotificationController(
    private val notificationService: NotificationService
) {

    @PostMapping
    suspend fun sendNotification(@RequestBody request: NotificationRequest): ResponseEntity<ApiResponse<Map<String, String>>> {
        val id = notificationService.enqueueNotification(request)
        return ResponseEntity.accepted()
            .body(ApiResponse.success(mapOf("notification_id" to id, "status" to "enqueued")))
    }
}
