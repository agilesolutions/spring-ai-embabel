package com.agilesolutions.trader.agent;


import com.agilesolutions.trader.domain.*;
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

    // Required to initialize Oauth security context for agent execution, but we mock JwtDecoder to bypass actual token validation
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

        whenCreateObject(s -> s.contains("latest quotes for all assets"), AssetPriceUpdates.class).thenReturn(
                new AssetPriceUpdates(List.of(
                        AssetDeviation.builder()
                                .symbol("AAPL")
                                .previousClose(new java.math.BigDecimal("155.00"))
                                .currentClose(new java.math.BigDecimal("162.00"))
                                .absoluteChange(new java.math.BigDecimal("157.00"))
                                .percentageChange(new java.math.BigDecimal("4.52"))
                                .build(),
                        AssetDeviation.builder()
                                .symbol("GOOGL")
                                .previousClose(new java.math.BigDecimal("2850.00"))
                                .currentClose(new java.math.BigDecimal("2900.00"))
                                .absoluteChange(new java.math.BigDecimal("50.00"))
                                .percentageChange(new java.math.BigDecimal("1.75"))
                                .build())));

        whenCreateObject(s -> s.contains("Adjust purchase prices on behalf of latest quotes"), AssetPerformances.class).thenReturn(
                new AssetPerformances(List.of(
                        AssetPerformance.builder()
                                .symbol("AAPL")
                                .quantity(10)
                                .purchasePrice(new java.math.BigDecimal("150.00"))
                                .currentPrice(new java.math.BigDecimal("162.00"))
                                .marketValue(new java.math.BigDecimal("1620.00"))
                                .gainLoss(new java.math.BigDecimal("120.00"))
                                .build(),
                        AssetPerformance.builder()
                                .symbol("GOOGL")
                                .quantity(5)
                                .purchasePrice(new java.math.BigDecimal("2800.00"))
                                .currentPrice(new java.math.BigDecimal("2900.00"))
                                .marketValue(new java.math.BigDecimal("14500.00"))
                                .gainLoss(new java.math.BigDecimal("500.00"))
                                .build()
                )));

         whenCreateObject(s -> s.contains("Generate markdown report"), PortfolioReport.class).thenReturn(
                new PortfolioReport("## Portfolio Report\n\n- AAPL: +4.52%\n- GOOGL: +1.75%")
        );

        // Then
        // Verify that the agent performed the expected actions and produced the correct results
        // You can use assertions to check the outputs and interactions with mocked dependencies
        var invocation = AgentInvocation.create(agentPlatform, PortfolioReport.class);

        var result = invocation.invoke(input);

        assertNotNull(result);
        assertEquals(2, result.markdown().split("-").length - 1); // Check that both assets are included in the report

    }


}