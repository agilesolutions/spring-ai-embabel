package com.agilesolutions.trader.model;

import java.math.BigDecimal;
import java.time.Instant;

public record LatestCloseDto(
        String symbol,
        BigDecimal closePrice,
        Instant timestamp
) { }