package com.exchange.service.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CacheKeyUtilTest {

    @Test
    void test_generatesKeyWithSortedUppercaseSymbols() {
        final String key = CacheKeyUtil.generateKey("usd", "eur,gbp,jpy");
        assertEquals("USD:EUR,GBP,JPY", key);
    }

    @Test
    void test_trimsWhitespaceAndSortsSymbols() {
        final String key = CacheKeyUtil.generateKey("usd", " jpy , eur , gbp ");
        assertEquals("USD:EUR,GBP,JPY", key);
    }

    @Test
    void test_throwsExceptionIfBaseIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                CacheKeyUtil.generateKey(null, "EUR,GBP"));
    }

    @Test
    void test_throwsExceptionIfSymbolsIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                CacheKeyUtil.generateKey("USD", null));
    }

}
