package com.agilesolutions.trader.model;

import java.util.List;

public record PortfolioAssets(
        List<PortfolioAssetDto> assets
) {
}
