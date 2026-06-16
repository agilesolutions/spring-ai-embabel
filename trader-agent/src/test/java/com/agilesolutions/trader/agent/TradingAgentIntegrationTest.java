package com.agilesolutions.trader.agent;


import com.agilesolutions.trader.model.PortfolioAssetDto;
import com.agilesolutions.trader.model.PortfolioAssets;
import com.agilesolutions.trader.service.PortfolioMarkdownGenerator;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.integration.EmbabelMockitoIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Ensure:
 * 1. Agents are picked up by the agent platform
 * 2. Dataflow is correct within agent
 * 3. Failure scenarios are handled correctly
 * 4. Overall workflow behaves as expected
 */
class TradingAgentIntegrationTest extends EmbabelMockitoIntegrationTest {

    // Spring Boot 4 uses @MockitoBean instead of the deprecated @MockBean
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void shouldExecuteCompleteWorkflow() {
        // Given
        var input = new UserInput("Please provide a portfolio for AAPL and GOOGL assets");

        var assets = new PortfolioAssets(List.of(
                PortfolioAssetDto.builder()
                        .id(1L)
                        .symbol("AAPL")
                        .name("Apple Inc.")
                        .quantity(10)
                        .purchasePrice(new java.math.BigDecimal("150.00"))
                        .lastClosePrice(new java.math.BigDecimal("155.00"))
                        .build(),
                PortfolioAssetDto.builder()
                        .id(2L)
                        .symbol("GOOGL")
                        .name("Alphabet Inc.")
                        .quantity(5)
                        .purchasePrice(new java.math.BigDecimal("2800.00"))
                        .lastClosePrice(new java.math.BigDecimal("2850.00"))
                        .build()
        ));
        // When
        // Trigger the agent execution, e.g., by calling a method or simulating an event
        whenCreateObject(s -> s.contains("AAPL") || s.contains("GOOGL"), PortfolioAssets.class).thenReturn(assets);

        // Then
        // Verify that the agent performed the expected actions and produced the correct results
        // You can use assertions to check the outputs and interactions with mocked dependencies
        var invocation = AgentInvocation.create(agentPlatform, PortfolioAssets.class);

        var result = invocation.invoke(input);

        assertNotNull(result);
        assertEquals(2, result.assets() .size());

    }


}