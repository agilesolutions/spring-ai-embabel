package com.agilesolutions.trader.service;

import com.agilesolutions.trader.entity.PortfolioAssetEntity;
import com.agilesolutions.trader.model.PortfolioAssetDto;
import com.agilesolutions.trader.repository.PortfolioAssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioService {

    private final PortfolioAssetRepository repository;

    public PortfolioService(
            PortfolioAssetRepository repository) {

        this.repository = repository;
    }

    public List<PortfolioAssetDto> findAllAssets() {

        return repository.findAll()
                .stream()
                .map(asset ->
                        PortfolioAssetDto.builder()
                                .symbol(asset.getSymbol())
                                .name(asset.getName())
                                .quantity(asset.getQuantity())
                                .purchasePrice(asset.getPurchasePrice())
                                .build())
                .toList();
    }

    public PortfolioAssetDto findAssetById(Long assetId) {

        return repository.findById(assetId)
                .map(asset ->
                        PortfolioAssetDto.builder()
                                .symbol(asset.getSymbol())
                                .name(asset.getName())
                                .quantity(asset.getQuantity())
                                .purchasePrice(asset.getPurchasePrice())
                                .build())
                .orElseThrow();

    }

    public PortfolioAssetEntity saveAsset(PortfolioAssetDto asset) {


        return repository.save(PortfolioAssetEntity.builder()
                .id(asset.id())
                .name(asset.name())
                .quantity(asset.quantity())
                .symbol(asset.symbol())
                .purchasePrice(asset.lastClosePrice())
                .build());

    }
}