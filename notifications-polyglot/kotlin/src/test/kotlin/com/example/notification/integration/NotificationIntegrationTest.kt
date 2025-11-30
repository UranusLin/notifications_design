package com.example.notification.integration

import com.example.notification.dto.NotificationRequest
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class NotificationIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val kafka = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))

        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:16-alpine"))

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Test
    fun `should enqueue notification successfully`() = runTest {
        // Given
        val request = NotificationRequest(
            channels = listOf("email", "sms"),
            recipientIds = listOf("user123", "user456"),
            message = "Kotlin integration test"
        )

        // When
        val response = restTemplate.postForEntity(
            "/notify",
            request,
            Map::class.java
        )

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
        assertThat(response.body).isNotNull
        
        val body = response.body as Map<String, Any>
        assertThat(body["success"]).isEqualTo(true)
        
        val data = body["data"] as Map<String, String>
        assertThat(data["notification_id"]).isNotNull()
        assertThat(data["status"]).isEqualTo("enqueued")
    }

    @Test
    fun `should process notification through worker with coroutines`() = runTest {
        // Given
        val request = NotificationRequest(
            channels = listOf("push"),
            recipientIds = listOf("device-token-kotlin"),
            message = "Coroutines worker test"
        )

        // When
        val response = restTemplate.postForEntity(
            "/notify",
            request,
            Map::class.java
        )

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
        
        // Wait for worker to process (check logs)
        await.atMost(5, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                val body = response.body as Map<String, Any>
                val data = body["data"] as Map<String, String>
                val notificationId = data["notification_id"]
                assertThat(notificationId).isNotNull()

                // Check status endpoint
                val statusResponse = restTemplate.getForEntity(
                    "/status/$notificationId",
                    Map::class.java
                )
                assertThat(statusResponse.statusCode).isEqualTo(HttpStatus.OK)
                val statusBody = statusResponse.body as Map<String, Any>
                val statusData = statusBody["data"] as Map<String, Any>
                assertThat(statusData["status"]).isEqualTo("COMPLETED")
                
                // Check metrics endpoint
                val metricsResponse = restTemplate.getForEntity(
                    "/metrics",
                    Map::class.java
                )
                assertThat(metricsResponse.statusCode).isEqualTo(HttpStatus.OK)
                val metricsBody = metricsResponse.body as Map<String, Any>
                val metricsData = metricsBody["data"] as Map<String, Any>
                assertThat((metricsData["totalSent"] as Number).toLong()).isGreaterThan(0)
            }
    }
}
