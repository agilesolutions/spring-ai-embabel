package com.agilesolutions.trader.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
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