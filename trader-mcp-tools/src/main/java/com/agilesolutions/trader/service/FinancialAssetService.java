package com.agilesolutions.trader.service;

import com.agilesolutions.trader.model.QuoteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FinancialAssetService {

    private final RestClient restClient;
    private final String apiKey;

    public FinancialAssetService(
            RestClient restClient,
            @Value("${twelvedata.api-key}") String apiKey) {

        this.restClient = restClient;
        this.apiKey = apiKey;
    }

    public QuoteResponse quote(String symbol) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(QuoteResponse.class);
    }
}