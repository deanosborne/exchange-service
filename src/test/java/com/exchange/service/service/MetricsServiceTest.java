package com.exchange.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class MetricsServiceTest {

    private final String FREE = "free_exchange";
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        metricsService = new MetricsService(new SimpleMeterRegistry());
        metricsService.init();
    }

    @Test
    void test_increment_and_get_total_requests() {
        metricsService.incrementTotalRequests();
        metricsService.incrementTotalRequests();
        assertEquals(2, metricsService.getTotalRequests());
    }

    @Test
    void test_increment_and_get_api_metrics() {
        metricsService.incrementRequests(FREE);
        metricsService.incrementResponses(FREE);
        metricsService.incrementErrors(FREE);

        assertEquals(1, metricsService.getRequests(FREE));
        assertEquals(1, metricsService.getResponses(FREE));
        assertEquals(1, metricsService.getErrors(FREE));
    }

    @Test
    void test_record_and_get_avg_time() {
        metricsService.recordTime(FREE, 100);
        metricsService.recordTime(FREE, 300);
        assertEquals(200.0, metricsService.getAvgTime(FREE), 0.01);
    }

    @Test
    void test_get_last_response_time_and_success_rate() {
        metricsService.recordTime("free_exchange", 100);
        metricsService.incrementRequests("free_exchange");
        metricsService.incrementResponses("free_exchange");

        assertEquals(100, metricsService.getLastTime("free_exchange"));
        assertEquals(1, metricsService.getRequests("free_exchange"));
        assertEquals(1, metricsService.getResponses("free_exchange"));
    }

}
