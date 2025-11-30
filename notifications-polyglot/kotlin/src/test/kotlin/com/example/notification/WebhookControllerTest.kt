package com.example.notification

import com.example.notification.controller.WebhookController
import com.example.notification.service.NotificationStatusService
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.ArgumentMatchers.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(WebhookController::class)
class WebhookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var statusService: NotificationStatusService

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun shouldHandleWebhookCallback() {
        val payload = mapOf(
            "notification_id" to "test-id",
            "channel" to "email",
            "status" to "COMPLETED"
        )
        val json = objectMapper.writeValueAsString(payload)

        mockMvc.perform(
            post("/webhook/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(statusService).updateChannelStatus(
            "test-id",
            "email",
            "COMPLETED"
        )
    }

    @Test
    fun shouldHandleDifferentChannels() {
        val payload = mapOf(
            "notification_id" to "test-id-2",
            "channel" to "sms",
            "status" to "FAILED"
        )
        val json = objectMapper.writeValueAsString(payload)

        mockMvc.perform(
            post("/webhook/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))

        verify(statusService).updateChannelStatus(
            "test-id-2",
            "sms",
            "FAILED"
        )
    }
}
