package com.agilesolutions.trader.model;

import java.math.BigDecimal;

public record PriceUpdateResult(
        Long assetId,
        BigDecimal previousPrice,
        BigDecimal newPrice,
        boolean changed
) { }