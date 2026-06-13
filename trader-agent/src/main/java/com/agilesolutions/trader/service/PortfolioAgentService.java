package com.agilesolutions.trader.service;

import com.agilesolutions.trader.domain.PortfolioRefreshRequest;
import com.agilesolutions.trader.domain.PortfolioReport;
import com.embabel.agent.api.invocation.AgentInvocation;
import com.embabel.agent.core.AgentPlatform;
import org.springframework.stereotype.Service;

@Service
public class PortfolioAgentService {

    private final AgentPlatform agentPlatform;

    public PortfolioAgentService(
            AgentPlatform agentPlatform) {

        this.agentPlatform = agentPlatform;
    }

    public PortfolioReport generateReport(
            PortfolioRefreshRequest request) {

        return AgentInvocation
                .create(
                        agentPlatform,
                        PortfolioReport.class)
                .invoke(request);
    }
}