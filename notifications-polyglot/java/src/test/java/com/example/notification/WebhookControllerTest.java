package com.example.notification;

import com.example.notification.controller.WebhookController;
import com.example.notification.service.NotificationStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationStatusService statusService;

    @Test
    void shouldHandleWebhookCallback() throws Exception {
        Map<String, Object> payload = Map.of(
            "notification_id", "test-id",
            "channel", "email",
            "status", "COMPLETED"
        );
        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/webhook/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statusService).updateChannelStatus(
                eq("test-id"),
                eq("email"),
                eq("COMPLETED")
        );
    }

    @Test
    void shouldHandleDifferentChannels() throws Exception {
        Map<String, Object> payload = Map.of(
            "notification_id", "test-id-2",
            "channel", "sms",
            "status", "FAILED"
        );
        String json = objectMapper.writeValueAsString(payload);

        mockMvc.perform(post("/webhook/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(statusService).updateChannelStatus(
                eq("test-id-2"),
                eq("sms"),
                eq("FAILED")
        );
    }
}
