package com.exchange.service.service;

import com.exchange.service.error.ExchangeRateException;
import com.exchange.service.model.ExchangeRateResponse;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private static final int API_TIMEOUT_MS = 800;
    private final RestTemplate restTemplate;
    private final MetricsService metricsService;

    /**
     * Gets exchange rates by averaging results from freeExchange and Frankfurter APIs.
     * Results are cached by base currency and target symbols.
     *
     * @param base Base currency code
     * @param symbols Comma-separated target currencies
     * @return Exchange rates response with averaged values
     * @throws ExchangeRateException if both APIs fail or service is unavailable
     */
    @Cacheable(value = "exchangeRates",
            key = "T(com.exchange.service.util.CacheKeyUtil).generateKey(#base, #symbols)")
    public ExchangeRateResponse getExchangeRates(final String base, final String symbols) {
        metricsService.incrementTotalRequests();
        log.debug("Cache miss for base={}, symbols={}", base, symbols);

        final CompletableFuture<Map<String, Double>> freeExchangeFuture = executeApiCall(
                () -> fetchRates(base, symbols, "free_exchange", this::fetchFreeExchangeRates)
        );

        final CompletableFuture<Map<String, Double>> frankfurterFuture = executeApiCall(
                () -> fetchRates(base, symbols, "frankfurter", this::fetchFrankfurterRates)
        );

        try {
            CompletableFuture.allOf(freeExchangeFuture, frankfurterFuture).join();

            final Map<String, Double> freeExchangeRates = freeExchangeFuture.get();
            final Map<String, Double> frankfurterRates = frankfurterFuture.get();

            if (freeExchangeRates.isEmpty() && frankfurterRates.isEmpty()) {
                log.error("Both exchange rate providers failed to return data");
                throw new ExchangeRateException("Unable to fetch exchange rates from any provider");
            }

            return ExchangeRateResponse.builder()
                    .base(base)
                    .rates(calculateAverageRates(freeExchangeRates, frankfurterRates))
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (final Exception e) {
            log.error("Failed to fetch rates: {}", e.getMessage());
            throw e instanceof ExchangeRateException
                    ? (ExchangeRateException) e
                    : new ExchangeRateException("Exchange rate service unavailable", e);
        }
    }

    private CompletableFuture<Map<String, Double>> executeApiCall(final Supplier<Map<String, Double>> apiCall) {
        return CompletableFuture
                .supplyAsync(apiCall)
                .completeOnTimeout(Collections.emptyMap(), API_TIMEOUT_MS, TimeUnit.MILLISECONDS);
    }

    private Map<String, Double> fetchRates(final String base, final String symbols, final String apiName,
            final BiFunction<String, String, Map<String, Double>> fetcher) {
        final long start = System.currentTimeMillis();
        metricsService.incrementRequests(apiName);

        try {
            final Map<String, Double> rates = fetcher.apply(base, symbols);
            if (!rates.isEmpty()) {
                metricsService.incrementResponses(apiName);
            }
            return rates;
        } catch (final Exception e) {
            metricsService.incrementErrors(apiName);
            log.warn("{} API failure: {}", apiName, e.getMessage());
            return Collections.emptyMap();
        } finally {
            metricsService.recordTime(apiName, System.currentTimeMillis() - start);
        }
    }

    private Map<String, Double> fetchFreeExchangeRates(final String base, final String symbols) {
        final String url = String.format(
                "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/%s.json",
                base.toLowerCase()
        );

        final ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Collections.emptyMap();
        }

        @SuppressWarnings("unchecked")
        final Map<String, Double> allRates = (Map<String, Double>) response.getBody().get(base.toLowerCase());
        if (allRates == null) {
            log.warn("freeExchange returned no rates for base {}", base);
            return Collections.emptyMap();
        }

        return Arrays.stream(symbols.split(","))
                .filter(symbol -> allRates.containsKey(symbol.toLowerCase()))
                .collect(Collectors.toMap(
                        String::toUpperCase,
                        symbol -> allRates.get(symbol.toLowerCase())
                ));
    }

    private Map<String, Double> fetchFrankfurterRates(final String base, final String symbols) {
        final String url = String.format(
                "https://api.frankfurter.dev/v1/latest?base=%s&symbols=%s",
                base, symbols
        );

        final ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                }
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return Collections.emptyMap();
        }

        @SuppressWarnings("unchecked")
        final Map<String, Double> rates = (Map<String, Double>) response.getBody().get("rates");

        return rates != null ? rates : Collections.emptyMap();
    }

    private Map<String, Double> calculateAverageRates(final Map<String, Double> rates1, final Map<String, Double> rates2) {
        final Set<String> allSymbols = new HashSet<>();
        allSymbols.addAll(rates1.keySet());
        allSymbols.addAll(rates2.keySet());

        return allSymbols.stream().collect(Collectors.toMap(
                s -> s,
                s -> {
                    final Double val1 = rates1.get(s);
                    final Double val2 = rates2.get(s);
                    return val1 != null && val2 != null
                            ? (val1 + val2) / 2
                            : val1 != null ? val1 : val2;
                }
        ));
    }

}
