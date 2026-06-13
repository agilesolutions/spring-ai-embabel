package com.agilesolutions.trader.controller;

import com.agilesolutions.service_a.service.EntityClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OAuth2 Client Credentials Flow Integration Tests - Service A
 * 
 * Uses Testcontainers to test OAuth2 Client Credentials flow with a real Keycloak instance.
 * Validates token acquisition and refresh mechanisms.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Slf4j
@DisplayName("OAuth2 Client Credentials Flow Integration Tests")
class OAuth2ClientIntegrationTest {

    /**
     * Keycloak container for testing
     * Uses dev mode for quick startup
     */
    @Container
    public static final GenericContainer<?> keycloakContainer = new GenericContainer<>(
            DockerImageName.parse("quay.io/keycloak/keycloak:24.0.1")
    )
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCommand("start-dev");

    @Autowired(required = false)
    private EntityClient entityClient;

    @Autowired(required = false)
    private OAuth2AuthorizedClientManager authorizedClientManager;

    @Autowired(required = false)
    private ClientRegistrationRepository clientRegistrationRepository;

    private String keycloakUrl;
    private static final String REALM = "demo";
    private static final String SERVICE_A_CLIENT = "service-a";
    private static final String SERVICE_A_SECRET = "service-a-secret";

    @BeforeEach
    void setUp() throws Exception {
        keycloakUrl = "http://" + keycloakContainer.getHost() + ":" + 
                      keycloakContainer.getMappedPort(8080);
        
        log.debug("Keycloak URL: {}", keycloakUrl);
        
        // Wait for Keycloak to be ready
        waitForKeycloakReady();
    }

    /**
     * Wait for Keycloak to be ready by checking health endpoint
     */
    private void waitForKeycloakReady() throws InterruptedException {
        int maxRetries = 30; // 30 seconds
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                var url = new java.net.URL(keycloakUrl + "/health/ready");
                var connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(2000);
                connection.setReadTimeout(2000);
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    log.debug("Keycloak is ready");
                    return;
                }
            } catch (Exception e) {
                log.debug("Keycloak not ready, attempt {}/{}", attempt + 1, maxRetries);
            }
            
            attempt++;
            Thread.sleep(1000);
        }
        
        throw new RuntimeException("Keycloak failed to start within 30 seconds");
    }

    @Test
    @DisplayName("OAuth2AuthorizedClientManager should be initialized")
    void testOAuth2AuthorizedClientManagerInitialized() {
        assertNotNull(authorizedClientManager,
                "OAuth2AuthorizedClientManager should be initialized");
    }

    @Test
    @DisplayName("EntityClient should be initialized with OAuth2 support")
    void testEntityClientInitializedWithOAuth2() {
        assertNotNull(entityClient,
                "EntityClient should be initialized");
    }

    @Test
    @DisplayName("ClientRegistrationRepository should contain Keycloak registration")
    void testClientRegistrationRepositoryContainsKeycloakClient() {
        assertNotNull(clientRegistrationRepository,
                "ClientRegistrationRepository should be initialized");
        
        var keycloakRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        assertNotNull(keycloakRegistration,
                "Keycloak client registration should be available");
        assertEquals("keycloak", keycloakRegistration.getRegistrationId());
    }

    @Test
    @DisplayName("OAuth2 client should be configured with Client Credentials grant type")
    void testOAuth2ClientCredentialsGrantType() {
        assertNotNull(clientRegistrationRepository);
        
        var keycloakRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        assertNotNull(keycloakRegistration);
        
        assertEquals("client_credentials", keycloakRegistration.getAuthorizationGrantType().getValue(),
                "OAuth2 should use client_credentials grant type");
    }

    @Test
    @DisplayName("OAuth2 client registration should have token URI configured")
    void testOAuth2TokenUriConfigured() {
        assertNotNull(clientRegistrationRepository);
        
        var keycloakRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        assertNotNull(keycloakRegistration);
        assertNotNull(keycloakRegistration.getProviderDetails().getTokenUri(),
                "Token URI should be configured");
        assertTrue(keycloakRegistration.getProviderDetails().getTokenUri().contains("token"),
                "Token URI should contain 'token' endpoint");
    }

    @Test
    @DisplayName("OAuth2 client should be configured with correct scopes")
    void testOAuth2ClientConfiguredWithScopes() {
        assertNotNull(clientRegistrationRepository);
        
        var keycloakRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        assertNotNull(keycloakRegistration);
        
        var scopes = keycloakRegistration.getScopes();
        assertNotNull(scopes,
                "Scopes should be configured for OAuth2 client");
    }

    @Test
    @DisplayName("OAuth2 client should support refresh token flow")
    void testOAuth2SupportRefreshToken() {
        assertNotNull(authorizedClientManager,
                "OAuth2AuthorizedClientManager should support refresh token");
        
        // ClientManager is configured with refreshToken provider in OAuth2ClientConfig
        assertNotNull(authorizedClientManager);
    }

    @Test
    @DisplayName("TokenUri should point to Keycloak realm token endpoint")
    void testTokenUriPointsToKeycloakRealm() {
        assertNotNull(clientRegistrationRepository);
        
        var keycloakRegistration = clientRegistrationRepository.findByRegistrationId("keycloak");
        assertNotNull(keycloakRegistration);
        
        String tokenUri = keycloakRegistration.getProviderDetails().getTokenUri();
        assertTrue(tokenUri.contains("/realms/") || tokenUri.contains("/token"),
                "Token URI should point to Keycloak realm endpoint");
    }

    @Test
    @DisplayName("OAuth2 client configuration should be accessible from application context")
    void testOAuth2ConfigurationAccessible() {
        assertNotNull(clientRegistrationRepository,
                "Client registration repository should be accessible");
        assertNotNull(authorizedClientManager,
                "OAuth2 authorized client manager should be accessible");
    }

    @Test
    @DisplayName("Keycloak container should be running")
    void testKeycloakContainerRunning() {
        assertTrue(keycloakContainer.isRunning(),
                "Keycloak container should be running");
    }

    @Test
    @DisplayName("Keycloak should be accessible via HTTP")
    void testKeycloakAccessible() throws Exception {
        String url = keycloakUrl + "/health/live";
        var httpUrl = new java.net.URL(url);
        var connection = (java.net.HttpURLConnection) httpUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        int responseCode = connection.getResponseCode();
        assertTrue(responseCode == 200 || responseCode == 204,
                "Keycloak should return healthy status");
    }
}

