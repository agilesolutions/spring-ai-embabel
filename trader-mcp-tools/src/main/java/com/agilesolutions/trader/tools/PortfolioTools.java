package com.agilesolutions.trader.tools;

import com.agilesolutions.trader.model.PortfolioAssetDto;
import com.agilesolutions.trader.model.PriceUpdateResult;
import com.agilesolutions.trader.service.PortfolioService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class PortfolioTools {

    private final PortfolioService service;

    public PortfolioTools(PortfolioService service) {
        this.service = service;
    }

    @Tool(
            name = "getPortfolioAssets",
            description = "Returns all financial assets from portfolio")
    public List<PortfolioAssetDto> getPortfolioAssets() {

        return service.findAllAssets();
    }

    @Tool(
            name = "updateClosePrice",
            description = "Persist latest close price")
    public PriceUpdateResult updateClosePrice(
            Long assetId,
            BigDecimal latestClose) {

        var asset = service.findAssetById(assetId);

        var previous = asset.lastClosePrice();

        boolean changed =
                previous == null ||
                        previous.compareTo(latestClose) != 0;

        if (changed) {

            service.saveAsset(PortfolioAssetDto.builder()
                    .id(asset.id())
                    .name(asset.name())
                    .symbol(asset.symbol())
                    .purchasePrice(asset.purchasePrice())
                    .lastClosePrice(latestClose)
                    .build());
        }

        return new PriceUpdateResult(
                assetId,
                previous,
                latestClose,
                changed);
    }
}