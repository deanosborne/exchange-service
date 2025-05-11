package com.exchange.service.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * Global exception handler for centralizing error responses across the application.
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exchange rate service exceptions.
     *
     * @param ex The exception
     * @return Error response with SERVICE_UNAVAILABLE status
     */
    @ExceptionHandler(ExchangeRateException.class)
    public ResponseEntity<Map<String, Object>> handleExchangeRateException(final ExchangeRateException ex) {
        log.error("Exchange rate service error: {}", ex.getMessage());
        return buildErrorResponse(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Builds standard error response.
     *
     * @param message Error message
     * @param status HTTP status code
     * @return Formatted error response
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            final String message,
            final HttpStatus status) {
        final Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", LocalDateTime.now());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("message", message);
        return new ResponseEntity<>(errorBody, status);
    }

    /**
     * Handles missing request parameters.
     *
     * @param ex The exception
     * @return Error response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParams(final MissingServletRequestParameterException ex) {
        log.error("Missing required parameter: {}", ex.getMessage());
        return buildErrorResponse("Missing required parameter: " + ex.getParameterName(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles HTTP client errors.
     *
     * @param ex The exception
     * @return Error response with BAD_GATEWAY status
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Map<String, Object>> handleHttpClientError(final HttpClientErrorException ex) {
        log.error("API client error: {}", ex.getMessage());
        return buildErrorResponse("External API error: " + ex.getStatusCode(), HttpStatus.BAD_GATEWAY);
    }

    /**
     * Handles resource access exceptions.
     *
     * @param ex The exception
     * @return Error response with SERVICE_UNAVAILABLE status
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<Map<String, Object>> handleResourceAccessException(final ResourceAccessException ex) {
        log.error("Resource access error: {}", ex.getMessage());
        return buildErrorResponse("External service unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Fallback handler for all other exceptions.
     *
     * @param ex The exception
     * @return Error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(final Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
