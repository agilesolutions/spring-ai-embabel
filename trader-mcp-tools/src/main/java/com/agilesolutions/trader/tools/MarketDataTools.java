package com.agilesolutions.trader.tools;

import com.agilesolutions.trader.model.QuoteResponse;
import com.agilesolutions.trader.service.FinancialAssetService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class MarketDataTools {

    private final FinancialAssetService service;

    public MarketDataTools(FinancialAssetService service) {
        this.service = service;
    }

    @Tool(
            name = "getLatestQuote",
            description = """
                    Retrieve the latest close price
                    for a stock, ETF, index or fund.
                    """
    )
    public QuoteResponse getLatestQuote(String symbol) {

        return service.quote(symbol);
    }
}