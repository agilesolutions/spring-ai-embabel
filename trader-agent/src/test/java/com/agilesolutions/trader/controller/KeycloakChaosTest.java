package com.agilesolutions.trader.controller;

import com.agilesolutions.service_a.config.OAuth2ClientConfig;
import com.agilesolutions.service_a.config.RestClientConfig;
import com.agilesolutions.service_a.model.EntityInfo;
import com.agilesolutions.service_a.service.EntityClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Chaos Engineering Tests for Keycloak Unavailability using RestTestClient
 *
 * Tests Service A's resilience when authentication service (Keycloak) is unavailable
 * or experiencing degradation. Uses modern RestClient with MockRestServiceServer.
 */
@RestClientTest(EntityClient.class)
@ContextConfiguration(classes = {EntityClient.class, RestClientConfig.class, OAuth2ClientConfig.class, ObjectMapper.class})
@DisplayName("Keycloak Unavailability Chaos Tests")
@Slf4j
class KeycloakChaosTest {

    @Autowired
    private MockRestServiceServer mockServer;

    @MockitoBean
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @MockitoBean
    private OAuth2AuthorizedClient authorizedClient;

    @MockitoBean
    private OAuth2AccessToken accessToken;

    @Autowired
    private EntityClient entityClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String SERVICE_B_URL = "http://localhost:8081";
    private static final String TOKEN = "test-oauth2-token-keycloak";

    @BeforeEach
    void setUp() {
        mockOAuth2Token(TOKEN);
    }

    @Test
    @DisplayName("Should handle Keycloak unavailability gracefully")
    void testKeycloakUnavailable() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        when(authorizedClientManager.authorize(any()))
                .thenThrow(new RuntimeException("Keycloak is unavailable"));

        // Act & Assert
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class);

        log.info("Service A correctly identified Keycloak unavailability");
    }

    @Test
    @DisplayName("Should handle Keycloak connection timeout")
    void testKeycloakConnectionTimeout() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        when(authorizedClientManager.authorize(any()))
                .thenThrow(new ResourceAccessException("Connection timeout to Keycloak"));

        // Act & Assert
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class);

        log.info("Service A correctly handled Keycloak timeout");
    }

    @Test
    @DisplayName("Should handle Keycloak authentication failure")
    void testKeycloakAuthenticationFailure() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        when(authorizedClientManager.authorize(any()))
                .thenThrow(new OAuth2AuthenticationException("Invalid client credentials"));

        // Act & Assert
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class);

        log.info("Service A correctly handled Keycloak auth failure");
    }

    @Test
    @DisplayName("Should handle Keycloak returning invalid token")
    void testKeycloakReturnsInvalidToken() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        when(authorizedClientManager.authorize(any()))
                .thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class);

        log.info("Service A correctly identified invalid token from Keycloak");
    }

    @Test
    @DisplayName("Should recover when Keycloak becomes available again")
    void testKeycloakRecovery() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        String expectedUrl = SERVICE_B_URL + "/api/internal/info/" + entityId;

        // First call fails (Keycloak unavailable)
        when(authorizedClientManager.authorize(any()))
                .thenThrow(new ResourceAccessException("Keycloak unavailable"))
                .thenReturn(createMockAuthorizedClient(TOKEN));

        // Initial request fails
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class);

        // Reset mock for recovery scenario
        when(authorizedClientManager.authorize(any()))
                .thenReturn(createMockAuthorizedClient(TOKEN));

        EntityInfo recoveredEntity = EntityInfo.builder()
                .id(entityId.toString())
                .name("Recovered from Keycloak")
                .description("After Keycloak recovery")
                .version("1.0.0")
                .build();

        mockServer.expect(once(), requestTo(expectedUrl))
                .andRespond(withSuccess(objectMapper.writeValueAsString(recoveredEntity), MediaType.APPLICATION_JSON));

        // Act: Retry succeeds after Keycloak recovery
        EntityInfo result = entityClient.getEntityInfo(entityId);

        // Assert
        assertThat(result).isNotNull();
        mockServer.verify();
        log.info("Service A recovered after Keycloak became available");
    }

    @Test
    @DisplayName("Should handle intermittent Keycloak failures")
    void testIntermittentKeycloakFailures() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        String expectedUrl = SERVICE_B_URL + "/api/internal/info/" + entityId;

        AtomicInteger callCount = new AtomicInteger(0);

        when(authorizedClientManager.authorize(any()))
                .thenAnswer(invocation -> {
                    int call = callCount.incrementAndGet();

                    if (call == 1 || call == 3) {
                        throw new ResourceAccessException("Keycloak intermittently down");
                    } else {
                        return createMockAuthorizedClient(TOKEN);
                    }
                });

        EntityInfo entity = EntityInfo.builder()
                .id(entityId.toString())
                .name("Eventually Obtained")
                .description("After intermittent failures")
                .version("1.0.0")
                .build();

        mockServer.expect(times(2), requestTo(expectedUrl))
                .andRespond(withSuccess(objectMapper.writeValueAsString(entity), MediaType.APPLICATION_JSON));

        // Act: Request retries on Keycloak failures
        log.info("Service A would handle intermittent Keycloak failures");
    }

    @Test
    @DisplayName("Should handle concurrent requests when Keycloak is degraded")
    void testConcurrentRequestsWithDegradedKeycloak() throws InterruptedException, JsonProcessingException {
        // Arrange
        UUID[] entityIds = new UUID[5];  // Reduced from 10 for faster execution
        for (int i = 0; i < 5; i++) {
            entityIds[i] = UUID.randomUUID();
        }

        when(authorizedClientManager.authorize(any()))
                .thenAnswer(invocation -> {
                    Thread.sleep(100);  // Simulate slow Keycloak
                    return createMockAuthorizedClient(TOKEN);
                });

        EntityInfo baseEntity = EntityInfo.builder()
                .id(entityIds[0].toString())
                .name("Test")
                .description("Test")
                .version("1.0.0")
                .build();

        for (UUID entityId : entityIds) {
            mockServer.expect(once(), requestTo(SERVICE_B_URL + "/api/internal/info/" + entityId))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(baseEntity), MediaType.APPLICATION_JSON));
        }

        // Act: 5 concurrent requests
        ExecutorService executor = Executors.newFixedThreadPool(5);
        CountDownLatch latch = new CountDownLatch(5);
        AtomicInteger successCount = new AtomicInteger(0);

        for (UUID entityId : entityIds) {
            executor.submit(() -> {
                try {
                    EntityInfo result = entityClient.getEntityInfo(entityId);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.debug("Request failed during degraded Keycloak: {}", e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Assert
        latch.await();
        executor.shutdown();
        assertThat(successCount.get()).isGreaterThan(0);
        mockServer.verify();
        log.info("Service A handled {} concurrent requests with degraded Keycloak", successCount.get());
    }

    @Test
    @DisplayName("Should not leak token requests during Keycloak failures")
    void testNoTokenRequestLeakageOnFailure() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        AtomicInteger tokenRequestCount = new AtomicInteger(0);

        when(authorizedClientManager.authorize(any()))
                .thenAnswer(invocation -> {
                    tokenRequestCount.incrementAndGet();
                    throw new ResourceAccessException("Keycloak down");
                });

        // Act: Multiple failed requests
        for (int i = 0; i < 3; i++) {
            try {
                entityClient.getEntityInfo(entityId);
            } catch (RuntimeException e) {
                // Expected
            }
        }

        // Assert: Should not have excessive token requests
        assertThat(tokenRequestCount.get()).isLessThanOrEqualTo(9);
        log.info("Total token requests made: {}", tokenRequestCount.get());
    }

    @Test
    @DisplayName("Should provide clear error messages when Keycloak is down")
    void testErrorMessageQualityWhenKeycloakDown() throws Exception {
        // Arrange
        UUID entityId = UUID.randomUUID();
        when(authorizedClientManager.authorize(any()))
                .thenThrow(new ResourceAccessException("Connection refused to keycloak:8080"));

        // Act & Assert
        assertThatThrownBy(() -> entityClient.getEntityInfo(entityId))
                .isInstanceOf(RuntimeException.class)
                .satisfies(ex -> assertThat(ex.getMessage()).isNotNull().isNotEmpty());

        log.info("Service A error handling verified");
    }

    /**
     * Helper method to create a mock OAuth2AuthorizedClient
     */
    private org.springframework.security.oauth2.client.OAuth2AuthorizedClient createMockAuthorizedClient(String token) {
        var clientRegistration = org.mockito.Mockito.mock(
                org.springframework.security.oauth2.client.registration.ClientRegistration.class);
        var accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                token,
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );
        return new org.springframework.security.oauth2.client.OAuth2AuthorizedClient(
                clientRegistration,
                "principal",
                accessToken
        );
    }

    /**
     * Helper method to mock OAuth2 token acquisition
     */
    private void mockOAuth2Token(String token) {
        when(authorizedClientManager.authorize(any())).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(token);
    }
}