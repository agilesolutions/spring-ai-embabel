package com.agilesolutions.trader.domain;

import java.math.BigDecimal;

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