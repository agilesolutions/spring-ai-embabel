package com.agilesolutions.trader.controller;

import com.agilesolutions.trader.domain.PortfolioRefreshRequest;
import com.agilesolutions.trader.domain.PortfolioReport;
import com.agilesolutions.trader.service.PortfolioAgentService;
import com.embabel.agent.core.AgentPlatform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AssetController {

    private final AgentPlatform agentPlatform;
    private final PortfolioAgentService portfolioAgentService;

    /**
     * Get performance markdown report for all outstanding portfolio assets.
     *
     * @param request Portfolio request containing the portfolio name
     * @return ResponseEntity with entity information or appropriate error status
     */
    @PostMapping("/refresh")
    public PortfolioReport refreshPortfolio(
            @RequestBody PortfolioRefreshRequest request) {

        return portfolioAgentService.generateReport(request);
    }
}