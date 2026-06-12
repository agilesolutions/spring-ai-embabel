package com.agilesolutions.trader.config;

import com.agilesolutions.trader.tools.MarketDataTools;
import com.agilesolutions.trader.tools.PortfolioTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider tools(MarketDataTools marketDataTools,
                                      PortfolioTools portfolioTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(marketDataTools, portfolioTools)
                .build();
    }

}