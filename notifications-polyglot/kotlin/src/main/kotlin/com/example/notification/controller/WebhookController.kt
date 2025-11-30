package com.example.notification.controller

import com.example.notification.common.ApiResponse
import com.example.notification.service.NotificationStatusService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val statusService: NotificationStatusService
) {

    private val logger = LoggerFactory.getLogger(WebhookController::class.java)

    @PostMapping("/callback")
    fun handleCallback(@RequestBody payload: Map<String, Any>): ResponseEntity<ApiResponse<String>> {
        logger.info("Received webhook callback: {}", payload)
        
        val notificationId = payload["notification_id"] as? String
        val channel = payload["channel"] as? String
        val status = payload["status"] as? String
        
        if (notificationId != null && channel != null && status != null) {
            statusService.updateChannelStatus(notificationId, channel, status)
            return ResponseEntity.ok(ApiResponse.success("Callback processed"))
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid payload"))
        }
    }
}
