package com.agilesolutions.trader.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_asset")
@Data
@Builder
public class PortfolioAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private String name;

    private Integer quantity;

    private BigDecimal purchasePrice;

    private String currency;

    // getters/setters
}