package com.agilesolutions.trader.model;

public record QuoteResponse(
        String symbol,
        String name,
        String exchange,
        String currency,
        String close,
        String datetime
) {
}