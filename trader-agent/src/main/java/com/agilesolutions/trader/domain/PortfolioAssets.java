package com.agilesolutions.trader.domain;

import com.agilesolutions.trader.model.PortfolioAssetDto;

import java.util.List;

public record PortfolioAssets(
        List<PortfolioAssetDto> assets
) {
}