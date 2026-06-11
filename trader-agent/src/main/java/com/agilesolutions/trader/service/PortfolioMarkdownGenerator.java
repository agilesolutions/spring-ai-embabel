package com.agilesolutions.trader.service;

import com.agilesolutions.trader.model.AssetPerformance;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioMarkdownGenerator {

    public String generate(
            List<AssetPerformance> assets) {

        StringBuilder md = new StringBuilder();

        md.append("# Portfolio Overview\n\n");

        md.append("| Symbol | Name | Qty | Buy | Current | Value | P/L |\n");
        md.append("|--------|------|-----|-----|---------|-------|-----|\n");

        for (AssetPerformance asset : assets) {

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