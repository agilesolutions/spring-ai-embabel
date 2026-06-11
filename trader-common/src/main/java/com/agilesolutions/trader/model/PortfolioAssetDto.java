package com.agilesolutions.trader.model;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
public record PortfolioAssetDto(
        Long id,
        String symbol,
        String name,
        Integer quantity,
        BigDecimal purchasePrice,
        BigDecimal lastClosePrice
) {}