package com.exchange.service.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utility class for creating standardized cache keys.
 */
public final class CacheKeyUtil {

    private CacheKeyUtil() {
    }

    /**
     * Generates a cache key for exchange rate lookups.
     *
     * @param base Base currency code
     * @param symbols Comma-separated currency symbols
     * @return cache key
     */
    public static String generateKey(final String base, final String symbols) {
        if (base == null || symbols == null) {
            throw new IllegalArgumentException("Base and symbols cannot be null");
        }

        final String normalizedSymbols = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .map(String::toUpperCase)
                .sorted()
                .collect(Collectors.joining(","));

        return base.toUpperCase() + ":" + normalizedSymbols;
    }

}
