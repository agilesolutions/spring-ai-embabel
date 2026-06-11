package com.agilesolutions.trader.model;

// Output of the action/REST API
public record StockPrice(String symbol, Double price, String currency) {}