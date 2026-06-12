package com.agilesolutions.trader.service;

import com.agilesolutions.trader.domain.AssetPerformance;
import com.agilesolutions.trader.domain.AssetPerformances;
import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

@Component
public class PortfolioMarkdownGenerator {

    @LlmTool(description = "Generate a markdown portfolio report based on actual performances")
    public String generate(
            AssetPerformances performances) {

        StringBuilder md = new StringBuilder();

        md.append("# Portfolio Overview\n\n");

        md.append("| Symbol | Name | Qty | Buy | Current | Value | P/L |\n");
        md.append("|--------|------|-----|-----|---------|-------|-----|\n");

        for (AssetPerformance asset : performances.performances()) {

            md.append("| ")
                    .append(asset.symbol()).append(" | ")
                    .append(asset.name()).append(" | ")
                    .append(asset.quantity()).append(" | ")
                    .append(asset.purchasePrice()).append(" | ")
                    .append(asset.currentPrice()).append(" | ")
                    .append(asset.marketValue()).append(" | ")
                    .append(asset.gainLoss()).append(" |\n");
        }

        return md.toString();
    }
}