package com.exchange.service.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MetricsResponse {

    private long totalRequests;
    private List<ApiMetrics> apiMetrics;

}
