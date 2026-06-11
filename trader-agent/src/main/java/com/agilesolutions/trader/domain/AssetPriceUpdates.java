package com.agilesolutions.trader.domain;

import java.util.List;

public record AssetPriceUpdates(
        List<AssetDeviation> deviations
) {
}