package com.exchange.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.exchange.service.model.ExchangeRateResponse;
import com.exchange.service.service.ExchangeRateService;
import com.exchange.service.service.MetricsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;

@WebMvcTest(ExchangeRateController.class)
class ExchangeRateControllerTest {

    private final ExchangeRateResponse mockResponse = ExchangeRateResponse.builder()
            .base("USD")
            .rates(Map.of("EUR", 0.85))
            .timestamp(LocalDateTime.now())
            .build();

    @Autowired
    private ExchangeRateController controller;
    @MockBean
    private ExchangeRateService exchangeRateService;
    @MockBean
    private CacheManager cacheManager;
    @MockBean
    private MetricsService metricsService;

    @Test
    void test_getExchangeRates() {
        final String base = "USD";
        final String symbols = "EUR,GBP";
        final String cacheKey = "USD-EUR,GBP";

        mockResponse.setBase(base);

        final Cache mockCache = mock(Cache.class);
        when(cacheManager.getCache("exchangeRates")).thenReturn(mockCache);
        when(mockCache.get(cacheKey)).thenReturn(null);
        when(exchangeRateService.getExchangeRates(base, symbols)).thenReturn(mockResponse);

        final ResponseEntity<ExchangeRateResponse> response = controller.getExchangeRates(base, symbols);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertSame(mockResponse, response.getBody());
    }

}