package com.agilesolutions.model;

// Output of the action/REST API
public record StockPrice(String symbol, Double price, String currency) {}