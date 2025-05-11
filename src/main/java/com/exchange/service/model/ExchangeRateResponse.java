package com.exchange.service.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExchangeRateResponse {

    private String base;
    private Map<String, Double> rates;
    private LocalDateTime timestamp;

}
