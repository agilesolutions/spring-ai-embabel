package com.agilesolutions.trader.model;

import java.math.BigDecimal;

public record AssetPerformance(
        String symbol,
        String name,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal currentPrice,
        BigDecimal marketValue,
        BigDecimal gainLoss
) {
}