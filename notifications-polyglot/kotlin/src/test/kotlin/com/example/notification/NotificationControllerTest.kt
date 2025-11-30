package com.example.notification

import com.example.notification.controller.NotificationController
import com.example.notification.dto.NotificationRequest
import com.example.notification.service.NotificationService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NotificationController::class)
class NotificationControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var notificationService: NotificationService

    @Test
    fun shouldEnqueueNotification() = runBlocking {
        `when`(notificationService.enqueueNotification(any())).thenReturn("test-id")

        val request = NotificationRequest(
            channels = listOf("email"),
            recipientIds = listOf("user1"),
            message = "Hello"
        )
        val json = jacksonObjectMapper().writeValueAsString(request)

        mockMvc.perform(post("/notify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isAccepted)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notification_id").value("test-id"))
    }
}
