package com.exchange.service.error;

public class ExchangeRateException extends RuntimeException {

    /**
     * Creates a new exception with message.
     *
     * @param message the detail message
     */
    public ExchangeRateException(final String message) {
        super(message);
    }

    /**
     * Creates a new exception with message and cause.
     *
     * @param message the detail message
     * @param cause the root cause
     */
    public ExchangeRateException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
