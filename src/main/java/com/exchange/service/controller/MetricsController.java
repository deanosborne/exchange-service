package com.exchange.service.controller;

import com.exchange.service.model.ApiMetrics;
import com.exchange.service.model.MetricsResponse;
import com.exchange.service.service.MetricsService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Returns application metrics including API performance.
     */
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        log.debug("Retrieving application metrics");

        final long totalRequests = metricsService.getTotalRequests();

        final ApiMetrics freeExchangeMetrics = buildApiMetrics("free_exchange", "Free currency rates API");
        final ApiMetrics frankfurterMetrics = buildApiMetrics("frankfurter", "Frankfurter API");

        final MetricsResponse response = MetricsResponse.builder()
                .totalRequests(totalRequests)
                .apiMetrics(Arrays.asList(freeExchangeMetrics, frankfurterMetrics))
                .build();

        return ResponseEntity.ok(response);
    }

    private ApiMetrics buildApiMetrics(final String apiKey, final String displayName) {
        final long requests = metricsService.getRequests(apiKey);
        final long responses = metricsService.getResponses(apiKey);

        return ApiMetrics.builder()
                .datasource(displayName)
                .totalRequests(requests)
                .totalResponses(responses)
                .totalErrors(metricsService.getErrors(apiKey))
                .averageResponseTime(metricsService.getAvgTime(apiKey))
                .lastResponseTime(metricsService.getLastTime(apiKey))
                .successRate(requests > 0 ? (double) responses / requests * 100 : 0.0)
                .build();
    }

}
