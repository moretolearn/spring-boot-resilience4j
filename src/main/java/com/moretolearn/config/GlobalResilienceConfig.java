package com.moretolearn.config;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GlobalResilienceConfig {

    @Bean
    public Retry retry(RetryRegistry registry) {
        return registry.retry("globalRetry");
    }

    @Bean
    public CircuitBreaker circuitBreaker(CircuitBreakerRegistry registry) {
        return registry.circuitBreaker("globalCircuitBreaker");
    }

    @Bean
    public RateLimiter rateLimiter(RateLimiterRegistry registry) {
        return registry.rateLimiter("globalRateLimiter");
    }

    @Bean
    public Bulkhead bulkhead(BulkheadRegistry registry) {
        return registry.bulkhead("globalBulkhead");
    }
}
