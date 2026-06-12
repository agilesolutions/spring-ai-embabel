package com.agilesolutions.trader.agent;

import com.agilesolutions.trader.domain.AssetPerformances;
import com.agilesolutions.trader.domain.AssetPriceUpdates;
import com.agilesolutions.trader.domain.PortfolioReport;
import com.agilesolutions.trader.model.PortfolioAssets;
import com.agilesolutions.trader.service.PortfolioMarkdownGenerator;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import lombok.RequiredArgsConstructor;

@Agent(description = "Portfolio Manager")
@RequiredArgsConstructor
public class TradingAgent {

    private final PortfolioMarkdownGenerator markdownGenerator;

    @Action(
            description = "Load all persisted portfolio assets from PostgreSQL"
    )
    public PortfolioAssets loadPortfolioAssets(OperationContext context) {

        return context.ai()
                .withDefaultLlm()
                .createObject(
                        """
                        Use MCP tool getPortfolioAssets
                        to return all financial assets from portfolio
                        """,
                        PortfolioAssets.class);

    }

    @Action(
            description =
                    "Retrieve latest market quote and compare with portfolio asset quotes"
    )
    public AssetPriceUpdates getActualQuotes(
            PortfolioAssets assets,
            OperationContext context) {

        return context.ai()
                .withDefaultLlm()
                .createObject(
                        """
                        Get latest quotes for all assets using MCP tool getLatestQuote
                        for symbol of each asset and calculate and return latest market as marketValue based on close price 
                         and calculate gainLoss attribute
                        """,
                        AssetPriceUpdates.class);
    }

    @Action(
            description = "Adjust purchase prices on behalf of latest quotes and update each portfolio asset accordingly"
    )
    public AssetPerformances updatePortfolioAssets(
            AssetPriceUpdates priceUpdates,
            OperationContext context) {

        return context.ai()
                .withDefaultLlm()
                .withId("")
                .createObject(
                        """
                        Update portfolio quotes accordingly using MCP tool updateClosePrice
                        for symbol of each asset and calculate and return the performance of each asset as AssetPerformance with marketValue and gainLoss attributes
                        """,
                        AssetPerformances.class);
    }

    @Action(description = "Generate a markdown portfolio report based on actual performances")
    public PortfolioReport generateReport(
            AssetPerformances performances,
            OperationContext context) {

        return context.ai()
                .withDefaultLlm()
                .withToolObject(markdownGenerator)
                .withId("report-generator")
                .createObject(
                        "Generate a markdown portfolio report based on performances",
                        PortfolioReport.class);
    }

}
