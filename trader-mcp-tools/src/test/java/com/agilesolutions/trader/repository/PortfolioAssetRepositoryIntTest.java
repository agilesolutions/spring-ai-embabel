package com.agilesolutions.trader.repository;

import com.agilesolutions.trader.entity.PortfolioAssetEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
class PortfolioAssetRepositoryIntTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

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