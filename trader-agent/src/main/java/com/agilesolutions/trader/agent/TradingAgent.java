package com.agilesolutions.trader.agent;

import com.agilesolutions.trader.domain.PortfolioReport;
import com.agilesolutions.trader.model.PortfolioAssets;
import com.agilesolutions.trader.model.PriceUpdateResult;
import com.agilesolutions.trader.tools.PortfolioTools;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import lombok.RequiredArgsConstructor;

@Agent(description = "Portfolio Manager")
@RequiredArgsConstructor
public class TradingAgent {

    private final PortfolioTools portfolioTools;

    @Action(
            description = "Load all persisted portfolio assets from PostgreSQL"
    )
    public PortfolioAssets loadPortfolioAssets() {

        return new PortfolioAssets(
                portfolioTools.getPortfolioAssets()
        );
    }

    @Action(
            description = "Retrieve latest close prices for all portfolio assets"
    )
    public PriceUpdateResult refreshMarketPrices(
            PortfolioAssets portfolioAssets) {

        return null;
    }

    @Action
    public PortfolioReport generateReport(
            PriceUpdateResult updates,
            OperationContext context) {

        return context.ai()
                .withDefaultLlm()
                .createObject(
                        "Generate a markdown portfolio report",
                        PortfolioReport.class);
    }




}
