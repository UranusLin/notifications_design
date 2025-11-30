package com.example.notification;

import com.example.notification.controller.StatusController;
import com.example.notification.dto.NotificationMetrics;
import com.example.notification.entity.NotificationStatus;
import com.example.notification.service.NotificationStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationStatusService statusService;

    @Test
    void shouldGetNotificationStatus() throws Exception {
        NotificationStatus mockStatus = new NotificationStatus();
        mockStatus.setNotificationId("test-id");
        mockStatus.setStatus("COMPLETED");
        Map<String, String> channelStatuses = new HashMap<>();
        channelStatuses.put("email", "COMPLETED");
        channelStatuses.put("sms", "COMPLETED");
        mockStatus.setChannelStatuses(channelStatuses);
        mockStatus.setCreatedAt(LocalDateTime.now());
        mockStatus.setUpdatedAt(LocalDateTime.now());

        when(statusService.getStatus("test-id")).thenReturn(Optional.of(mockStatus));

        mockMvc.perform(get("/status/test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.notificationId").value("test-id"))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.channelStatuses.email").value("COMPLETED"));
    }

    @Test
    void shouldReturnNotFoundForNonexistentStatus() throws Exception {
        when(statusService.getStatus("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/status/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetMetrics() throws Exception {
        Map<String, Long> byStatus = new HashMap<>();
        byStatus.put("COMPLETED", 80L);
        byStatus.put("FAILED", 20L);

        NotificationMetrics mockMetrics = new NotificationMetrics(100L, 80L, 20L, byStatus);

        when(statusService.getMetrics()).thenReturn(mockMetrics);

        mockMvc.perform(get("/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalSent").value(100))
                .andExpect(jsonPath("$.data.totalSuccess").value(80))
                .andExpect(jsonPath("$.data.totalFailed").value(20))
                .andExpect(jsonPath("$.data.byStatus.COMPLETED").value(80));
    }
}
