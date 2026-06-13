package com.agilesolutions.trader.service;

import com.agilesolutions.trader.model.PortfolioAssetDto;
import com.agilesolutions.trader.repository.PortfolioAssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioAssetRepository repository;

    @InjectMocks
    private PortfolioService service;

    private PortfolioAssetDto appleAsset;

    @BeforeEach
    void setUp() {
        appleAsset = PortfolioAssetDto.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .quantity(10)
                .lastClosePrice(new BigDecimal(150.00))
                .build();
    }

    @Test
    void findAllAssets() {
    }

    @Test
    void findAssetById() {
    }

    @Test
    void saveAsset() {
    }
}