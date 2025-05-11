# Exchange Rate Service

A Spring Boot application that provides currency exchange rates by aggregating data from multiple external APIs.

## Approach & Design Rationale

When designing this exchange rate service, I attempted a TDD approach, first defining the core requirements and behaviors through tests,
then implementing components to satisfy those tests.

### Core Design

#### 1. Resilience & Fallback Mechanisms

The application needs to handle potential failures gracefully. I designed the service to:

- Make parallel calls to multiple exchange rate providers
- Fall back to data from a single provider if one fails
- Implement appropriate timeouts to prevent blocking on slow responses
- Return consistent error messages

This approach reflects the reality that external APIs may experience downtime or delays, and the service should continue functioning
whenever possible.

#### 2. Performance Optimization

Performance was a key consideration, implemented through:

- Caching Strategy: Using Caffeine cache with a 1-hour expiration to balance data freshness
- Connection Pooling: Configuring reusable HTTP connections to reduce connection establishment overhead
- Parallel Execution: Using CompletableFuture for concurrent API calls

I specifically chose Caffeine for caching because it offers high performance, memory efficiency, and built-in statistics collection.

#### 3. Clear Separation of Concerns

The application follows a clean separation of responsibilities:

- Controllers: Handle HTTP requests/responses only
- Services: Contain core business logic
- Configuration: Centralize application setup
- Models: Define clear data structures
- Utilities: Provide reusable functions

#### 4. Metrics

The metrics collection provides:

- Tracking fort request counts, response times and error rates
- Separate metrics for each API provider to identify potential issues
- Success rate calculations

## Technical Implementation Details

### Caching

The caching strategy involves:

- Normalizing of cache keys by uppercase conversion and symbol sorting
- Setting appropriate TTL values to balance data freshness with performance
- Uses builder pattern through Caffeine for cache configuration

### Error Handling

The application implements a global exception handler that:

- Centralizes error response formatting
- Provides appropriate HTTP status codes based on error type
- Logging for debugging

### HTTP Client Configuration

The HTTP client configuration:

- Uses connection pooling
- Implements configurable timeouts
- Disables buffer request body for streaming efficiency

### Unit Tests

Unit tests focus on isolated components:

- **Utility Classes**: Testing helper functions like cache key generation
- **Service Logic**: Testing core business logic with mocked dependencies
- **Models**: Validating data structures and transformations

## Future Improvements

There are several improvements that would be made given more time:

1. **Test Coverage Expansion**: Add integration tests to verify actual API interactions
2. **Enhanced Error Recovery**: Implement retry mechanisms
3. **Dynamic Provider Selection**: Implement logic to favor more reliable providers based on past performance
4. **Response Time Optimization**: Add response compression and consider better caching
5. **Monitoring Enhancements**: Implement health checks and alerting based on error rates or response times
6. **Documentation**: Add OpenAPI documentation for easier API consumption
7. **Cache Management**: Add endpoints to manually invalidate cache entries when needed
8. **Currency Validation**: Add validation for currency codes against a standardized list
9. **Configurable Parameters**: Make more parameters configurable via properties
10. **Reporting**: Metrics and health can be stored to provide a easily consumable dashboard or documentation

## Running the Application

### Prerequisites

- Java 17

### Build and Run

```bash
# Build the application
./gradlew build

# Run the application
./gradlew bootRun
```

The service will be available at http://localhost:8080 and APIs can be hit using Postman/preferred API consumer.

## API Documentation

### Get Exchange Rates

```
GET /api/exchange-rates?base={BASE_CURRENCY}&symbols={SYMBOLS}
```

**Parameters:**

- `base`: Base currency code (e.g., USD, EUR)
- `symbols`: Comma-separated list of target currency codes (e.g., EUR,GBP,JPY)

**Sample Response:**

```json
{
  "base": "USD",
  "rates": {
    "EUR": 0.85,
    "GBP": 0.75,
    "JPY": 110.2
  },
  "timestamp": "2025-05-10T12:34:56"
}
```

### Get Metrics

```
GET /api/metrics
```

**Sample Response:**

```json
{
  "totalRequests": 120,
  "apiMetrics": [
    {
      "datasource": "Free currency rates API",
      "totalRequests": 85,
      "totalResponses": 82,
      "totalErrors": 3,
      "averageResponseTime": 145.7,
      "lastResponseTime": 130,
      "successRate": 96.5
    },
    {
      "datasource": "Frankfurter API",
      "totalRequests": 85,
      "totalResponses": 85,
      "totalErrors": 0,
      "averageResponseTime": 112.3,
      "lastResponseTime": 95,
      "successRate": 100.0
    }
  ]
}
```
