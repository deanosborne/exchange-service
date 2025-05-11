package com.exchange.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.exchange.service.error.ExchangeRateException;
import com.exchange.service.model.ExchangeRateResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ExchangeRateServiceTest {

    private RestTemplate restTemplate;
    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        final MetricsService metricsService = mock(MetricsService.class);
        exchangeRateService = new ExchangeRateService(restTemplate, metricsService);
    }

    @Test
    void test_returns_average_rate_from_both_apis() {
        mockFreeExchangeResponse("usd", Map.of("eur", 1.1));
        mockFrankfurterResponse(Map.of("EUR", 1.3));

        final ExchangeRateResponse response = exchangeRateService.getExchangeRates("USD", "EUR");

        assertNotNull(response);
        assertEquals("USD", response.getBase());
        assertEquals(1.2, response.getRates().get("EUR"), 0.001);
    }

    private void mockFreeExchangeResponse(final String base, final Map<String, Double> rates) {
        final Map<String, Object> response = new HashMap<>();
        response.put(base, rates);
        when(restTemplate.exchange(
                contains("currency-api"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));
    }

    private void mockFrankfurterResponse(final Map<String, Double> rates) {
        final Map<String, Object> response = new HashMap<>();
        response.put("rates", rates);
        when(restTemplate.exchange(
                contains("frankfurter"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));
    }

    @Test
    void test_returns_single_provider_if_other_fails() {
        mockFreeExchangeResponse("usd", Map.of("eur", 1.1));
        mockFailedFrankfurterResponse();

        final ExchangeRateResponse response = exchangeRateService.getExchangeRates("USD", "EUR");

        assertEquals(1.1, response.getRates().get("EUR"));
    }

    private void mockFailedFrankfurterResponse() {
        when(restTemplate.exchange(
                contains("frankfurter"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Test
    void test_throws_exception_if_both_providers_fail() {
        mockEmptyFreeExchangeResponse();
        mockFailedFrankfurterResponse();

        assertThrows(ExchangeRateException.class, () ->
                exchangeRateService.getExchangeRates("USD", "EUR"));
    }

    private void mockEmptyFreeExchangeResponse() {
        when(restTemplate.exchange(
                contains("currency-api"),
                eq(org.springframework.http.HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)))
                .thenReturn(new ResponseEntity<>(Map.of(), HttpStatus.OK));
    }

}
