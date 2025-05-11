package com.exchange.service.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.exchange.service.model.ApiMetrics;
import com.exchange.service.model.MetricsResponse;
import com.exchange.service.service.MetricsService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class MetricsControllerTest {

    @Mock
    private MetricsService metricsService;
    @InjectMocks
    private MetricsController metricsController;

    @Test
    void test_getMetrics() {
        when(metricsService.getTotalRequests()).thenReturn(100L);

        final String FREE = "free_exchange";
        when(metricsService.getRequests(FREE)).thenReturn(50L);
        when(metricsService.getResponses(FREE)).thenReturn(45L);
        when(metricsService.getErrors(FREE)).thenReturn(5L);
        when(metricsService.getAvgTime(FREE)).thenReturn(150.5);

        final String FRANK = "frankfurter";
        when(metricsService.getRequests(FRANK)).thenReturn(50L);
        when(metricsService.getResponses(FRANK)).thenReturn(48L);
        when(metricsService.getErrors(FRANK)).thenReturn(2L);
        when(metricsService.getAvgTime(FRANK)).thenReturn(120.0);

        // Act
        final ResponseEntity<MetricsResponse> response = metricsController.getMetrics();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());

        final MetricsResponse body = response.getBody();
        assert body != null;
        assertEquals(100L, body.getTotalRequests());

        assertEquals(2, body.getApiMetrics().size());

        final ApiMetrics freeMetrics = body.getApiMetrics().get(0);
        assertEquals("freeExchangeApi", freeMetrics.getDatasource());
        assertEquals(50L, freeMetrics.getTotalRequests());
        assertEquals(45L, freeMetrics.getTotalResponses());
        assertEquals(5L, freeMetrics.getTotalErrors());
        assertEquals(150.5, freeMetrics.getAverageResponseTime());
    }

}
