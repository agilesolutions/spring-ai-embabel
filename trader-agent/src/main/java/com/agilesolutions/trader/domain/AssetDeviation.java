package com.agilesolutions.trader.domain;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AssetDeviation(
        Long assetId,
        String symbol,
        String name,
        BigDecimal previousClose,
        BigDecimal currentClose,
        BigDecimal absoluteChange,
        BigDecimal percentageChange
) {
}