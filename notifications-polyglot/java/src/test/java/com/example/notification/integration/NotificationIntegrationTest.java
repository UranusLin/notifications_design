package com.example.notification.integration;

import com.example.notification.dto.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0")
    );

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine")
    );

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldEnqueueNotificationSuccessfully() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setChannels(List.of("email", "sms"));
        request.setRecipientIds(List.of("user123", "user456"));
        request.setMessage("Integration test message");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/notify",
                request,
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(response.getBody()).isNotNull();
        
        Map<String, Object> body = response.getBody();
        assertThat(body.get("success")).isEqualTo(true);
        
        Map<String, String> data = (Map<String, String>) body.get("data");
        assertThat(data).isNotNull();
        assertThat(data.get("notification_id")).isNotNull();
        assertThat(data.get("status")).isEqualTo("enqueued");
    }

    @Test
    void shouldProcessNotificationThroughWorker() {
        // Given
        NotificationRequest request = new NotificationRequest();
        request.setChannels(List.of("push"));
        request.setRecipientIds(List.of("device-token-123"));
        request.setMessage("Worker integration test");

        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "/notify",
                request,
                Map.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        
        // Wait for worker to process (check logs)
        await().atMost(5, TimeUnit.SECONDS)
                .pollInterval(500, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    Map<String, Object> body = response.getBody();
                    Map<String, String> data = (Map<String, String>) body.get("data");
                    String notificationId = data.get("notification_id");
                    assertThat(notificationId).isNotNull();

                    // Check status endpoint
                    ResponseEntity<Map> statusResponse = restTemplate.getForEntity(
                            "/status/" + notificationId,
                            Map.class
                    );
                    assertThat(statusResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    Map<String, Object> statusBody = statusResponse.getBody();
                    Map<String, Object> statusData = (Map<String, Object>) statusBody.get("data");
                    assertThat(statusData.get("status"))
                        .as("Status should be COMPLETED but was " + statusData.get("status"))
                        .isEqualTo("COMPLETED");
                    
                    // Check metrics endpoint
                    ResponseEntity<Map> metricsResponse = restTemplate.getForEntity(
                            "/metrics",
                            Map.class
                    );
                    assertThat(metricsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
                    Map<String, Object> metricsBody = metricsResponse.getBody();
                    Map<String, Object> metricsData = (Map<String, Object>) metricsBody.get("data");
                    assertThat(((Number) metricsData.get("totalSent")).longValue()).isGreaterThan(0);
                });
    }
}
