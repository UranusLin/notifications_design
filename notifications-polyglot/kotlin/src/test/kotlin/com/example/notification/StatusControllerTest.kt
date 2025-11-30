package com.example.notification

import com.example.notification.controller.StatusController
import com.example.notification.dto.NotificationMetrics
import com.example.notification.entity.NotificationStatus
import com.example.notification.service.NotificationStatusService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import java.util.Optional

@WebMvcTest(StatusController::class)
class StatusControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var statusService: NotificationStatusService

    @Test
    fun shouldGetNotificationStatus() {
        val mockStatus = NotificationStatus(
            notificationId = "test-id",
            status = "COMPLETED",
            channelStatuses = mutableMapOf("email" to "COMPLETED", "sms" to "COMPLETED"),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        `when`(statusService.getStatus("test-id")).thenReturn(Optional.of(mockStatus))

        mockMvc.perform(get("/status/test-id"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.notificationId").value("test-id"))
            .andExpect(jsonPath("$.data.status").value("COMPLETED"))
            .andExpect(jsonPath("$.data.channelStatuses.email").value("COMPLETED"))
    }

    @Test
    fun shouldReturnNotFoundForNonexistentStatus() {
        `when`(statusService.getStatus("nonexistent")).thenReturn(Optional.empty())

        mockMvc.perform(get("/status/nonexistent"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun shouldGetMetrics() {
        val byStatus = mapOf("COMPLETED" to 80L, "FAILED" to 20L)
        val mockMetrics = NotificationMetrics(
            totalSent = 100L,
            totalSuccess = 80L,
            totalFailed = 20L,
            byStatus = byStatus
        )

        `when`(statusService.getMetrics()).thenReturn(mockMetrics)

        mockMvc.perform(get("/metrics"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalSent").value(100))
            .andExpect(jsonPath("$.data.totalSuccess").value(80))
            .andExpect(jsonPath("$.data.totalFailed").value(20))
            .andExpect(jsonPath("$.data.byStatus.COMPLETED").value(80))
    }
}
