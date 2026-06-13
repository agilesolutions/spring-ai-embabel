package com.agilesolutions.trader.controller;

import com.agilesolutions.trader.domain.PortfolioRefreshRequest;
import com.agilesolutions.trader.domain.PortfolioReport;
import com.agilesolutions.trader.service.PortfolioAgentService;
import com.embabel.agent.core.AgentPlatform;
import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for invoking AI Trader agentic logic (Public Gateway)
 * 
 * Provides external API endpoint for kicking .
 * Acts as a gateway forwarding requests to Agentic logic through TradingAgent.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Portfolio Assets reporting",
        description = "Provides endpoints for generating performance reports for portfolio assets.")
public class AssetController {

    private final AgentPlatform agentPlatform;
    private final PortfolioAgentService portfolioAgentService;

    /**
     * Generates a performance report for a given portfolio name by invoking the agentic logic through the TradingAgent.
     *
     * @param request Portfolio request containing the portfolio name
     * @return ResponseEntity with entity information or appropriate error status
     */
    @PostMapping("/refresh")
    @Operation(
            summary     = "Refresh portfolio report",
            description = "Refreshes the portfolio report for a given portfolio name and returns the performance markdown report."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully refreshed portfolio report"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public PortfolioReport refreshPortfolio(
            @RequestBody PortfolioRefreshRequest request) {

        return portfolioAgentService.generateReport(request);
    }
}