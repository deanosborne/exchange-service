package com.exchange.service.controller;

import com.exchange.service.model.ExchangeRateResponse;
import com.exchange.service.service.ExchangeRateService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    /**
     * Get exchange rates for a base currency against specified symbols.
     */
    @GetMapping("/exchange-rates")
    public ResponseEntity<ExchangeRateResponse> getExchangeRates(
            @RequestParam final String base,
            @RequestParam final String symbols
    ) {
        log.debug("Exchange rate request: base={}, symbols={}", base, symbols);
        final ExchangeRateResponse response = exchangeRateService.getExchangeRates(base, symbols);
        return ResponseEntity.ok(response);
    }

}
