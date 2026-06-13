package com.agilesolutions.trader.repository;

import com.agilesolutions.trader.entity.PortfolioAssetEntity;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
class PortfolioAssetRepositoryTest {

    @Autowired
    private PortfolioAssetRepository repository;

    @BeforeEach
    void  setUp() {

        repository.deleteAll();

        repository.save(PortfolioAssetEntity.builder()
                .id(1L)
                .symbol("AAPL")
                .name("Apple Inc.")
                .quantity(10)
                .purchasePrice(new BigDecimal(150.00))
                .currency("USD")
                .build());

    }

    @Test
    void testFindAll() {
        List<PortfolioAssetEntity> list = repository.findAll();
        Assertions.assertNotNull(list);

    }

    @Test
    void testFindById() {
        Optional<PortfolioAssetEntity> findById = repository.findById(1L);
        Assertions.assertTrue(findById.isPresent());

    }

    @Test
    void testSavingNewAsset() {

        PortfolioAssetEntity asset = PortfolioAssetEntity.builder()
                .symbol("MSFT")
                .name("Microsoft Corporation")
                .quantity(20)
                .purchasePrice(new BigDecimal(300.00))
                .currency("USD")
                .build();

        PortfolioAssetEntity saved = repository.save(asset);
        Assertions.assertNotNull(saved);
        Assertions.assertEquals("MSFT", saved.getSymbol());

    }


}