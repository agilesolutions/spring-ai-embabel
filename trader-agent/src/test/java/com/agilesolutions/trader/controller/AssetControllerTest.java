package com.agilesolutions.trader.controller;

import com.agilesolutions.trader.domain.PortfolioReport;
import com.agilesolutions.trader.exception.GlobalExceptionHandler;
import com.agilesolutions.trader.service.PortfolioAgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(AssetController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AssetController.class, GlobalExceptionHandler.class, PortfolioAgentService.class})
class AssetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioAgentService service;

    private PortfolioReport report;

    @BeforeEach
    void setUp() {
        report = new PortfolioReport("test");
    }

    @Test
    void refreshPortfolio() throws Exception {

        // given
        when(service.generateReport(any())).thenReturn(report);

        // when & then
        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/refresh")
                        .contentType("application/json")
                        .content("{\"portfolioName\":\"test\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.portfolioName").value("test"));


    }
}