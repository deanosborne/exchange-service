package com.exchange.service.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiMetrics {

    private String datasource;
    private long totalRequests;
    private long totalResponses;
    private long totalErrors;
    private double averageResponseTime;
    private long lastResponseTime;
    private double successRate;

}

