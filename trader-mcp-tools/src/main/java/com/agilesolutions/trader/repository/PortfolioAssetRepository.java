package com.agilesolutions.trader.repository;

import com.agilesolutions.trader.entity.PortfolioAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortfolioAssetRepository
        extends JpaRepository<PortfolioAssetEntity, Long> {
}