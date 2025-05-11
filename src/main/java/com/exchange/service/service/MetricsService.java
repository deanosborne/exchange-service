package com.exchange.service.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private static final String FREE = "free_exchange";
    private static final String FRANK = "frankfurter";

    private static final String API_REQUESTS_TOTAL = "api.requests.total";

    private final MeterRegistry registry;

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    private final Map<String, ApiStats> apiStats = Map.of(
            FREE, new ApiStats(),
            FRANK, new ApiStats()
    );

    /**
     * Initializes all counters and timer.
     */
    @PostConstruct
    public void init() {
        registerCounter(API_REQUESTS_TOTAL, "Total requests");

        for (final String api : apiStats.keySet()) {
            registerCounter("api.requests", "API requests", "source", api);
            registerCounter("api.responses", "API responses", "source", api);
            registerCounter("api.errors", "API errors", "source", api);
            registerTimer("api.time", "API response time", "source", api);
        }
    }

    private void registerCounter(final String datasource, final String desc) {
        counters.put(datasource, Counter.builder(datasource).description(desc).register(registry));
    }

    private void registerCounter(final String datasource, final String desc, final String tag, final String value) {
        counters.put(metricKey(datasource, value),
                Counter.builder(datasource).tag(tag, value).description(desc).register(registry));
    }

    private void registerTimer(final String datasource, final String desc, final String tag, final String value) {
        timers.put(metricKey(datasource, value),
                Timer.builder(datasource).tag(tag, value).description(desc).register(registry));
    }

    private String metricKey(final String datasource, final String value) {
        return datasource + "." + value;
    }

    /**
     * Increments the total request counter.
     */
    public void incrementTotalRequests() {
        inc(API_REQUESTS_TOTAL);
    }

    private void inc(final String datasource) {
        final Counter counter = counters.get(datasource);
        if (counter != null) {
            counter.increment();
        }
    }

    /**
     * Increments the request counter for the given API.
     */
    public void incrementRequests(final String api) {
        inc(metricKey("api.requests", api));
    }

    /**
     * Increments the response counter for the given API.
     */
    public void incrementResponses(final String api) {
        inc(metricKey("api.responses", api));
    }

    /**
     * Increments the error counter for the given API.
     */
    public void incrementErrors(final String api) {
        inc(metricKey("api.errors", api));
    }

    /**
     * Records the response time and tracks average time for the API.
     */
    public void recordTime(final String api, final long millis) {
        final Timer timer = timers.get(metricKey("api.time", api));
        if (timer != null) {
            timer.record(millis, TimeUnit.MILLISECONDS);
        }
        final ApiStats stats = apiStats.get(api);
        if (stats != null) {
            stats.record(millis);
        }
    }

    /**
     * Gets the total request count.
     */
    public long getTotalRequests() {
        return getCount(API_REQUESTS_TOTAL);
    }

    private long getCount(final String datasource) {
        final Counter counter = counters.get(datasource);
        return counter != null ? (long) counter.count() : 0;
    }

    /**
     * Gets the request count for the API.
     */
    public long getRequests(final String api) {
        return getCount(metricKey("api.requests", api));
    }

    /**
     * Gets the response count for the API.
     */
    public long getResponses(final String api) {
        return getCount(metricKey("api.responses", api));
    }

    /**
     * Gets the error count for the API.
     */
    public long getErrors(final String api) {
        return getCount(metricKey("api.errors", api));
    }

    /**
     * Gets the average response time for the API.
     */
    public double getAvgTime(final String api) {
        final ApiStats stats = apiStats.get(api);
        return stats != null ? stats.avgTime() : 0;
    }

    /**
     * Tracks the last response time in ms.
     */
    public long getLastTime(final String api) {
        final ApiStats stats = apiStats.get(api);
        return stats != null ? stats.lastResponseTime() : 0;
    }

    /**
     * Tracks call count and total time to compute average response time.
     */
    private static class ApiStats {

        private final AtomicLong totalTime = new AtomicLong();
        private final AtomicLong callCount = new AtomicLong();
        private final AtomicLong lastTime = new AtomicLong();

        public void record(final long millis) {
            totalTime.addAndGet(millis);
            callCount.incrementAndGet();
            lastTime.set(millis);
        }

        public double avgTime() {
            final long calls = callCount.get();
            return calls > 0 ? (double) totalTime.get() / calls : 0;
        }

        public long lastResponseTime() {
            return lastTime.get();
        }

    }

}
