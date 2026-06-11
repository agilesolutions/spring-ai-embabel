package com.agilesolutions.trader.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfiguration {

    @Bean
    RestClient twelveDataClient() {

        return RestClient.builder()
                .baseUrl("https://api.twelvedata.com")
                .build();
    }
}