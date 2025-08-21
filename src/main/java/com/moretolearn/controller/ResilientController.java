package com.moretolearn.controller;

import java.util.Random;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;

@RestController
@RequestMapping("/api")
public class ResilientController {

    private final Random random = new Random();

    @Retry(name = "unstableService", fallbackMethod = "retryFallback")
    @GetMapping("/retry")
    public String retryExample() {
        if (random.nextBoolean()) {
            System.out.println("Simulating failure...");
            throw new RuntimeException("Random failure!");
        }
        return "Success after retry!";
    }

    // fallback method: must match return type + parameters
    public String retryFallback(Throwable ex) {
        return "⚡ Service failed after retries. Please try again later.";
    }

    @CircuitBreaker(name = "unstableService", fallbackMethod = "circuitFallback")
    @GetMapping("/circuit")
    public String circuitExample() {
        if (random.nextBoolean()) {
            throw new RuntimeException("Circuit breaker failure!");
        }
        return "Circuit breaker success!";
    }
    
    // Fallback method
    public String circuitFallback(Throwable ex) {
        return "Circuit breaker fallback triggered!";
    }

    @RateLimiter(name = "limitedService", fallbackMethod = "rateLimiterFallback")
    @GetMapping("/rate")
    public String rateLimiterExample() {
        return "This endpoint is rate limited!";
    }

    // Fallback method
    public String rateLimiterFallback(RequestNotPermitted ex) {
        return "Too many requests! Please try again later.";
    }
    
    @Bulkhead(name = "limitedConcurrency", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "bulkheadFallback")
    @GetMapping("/bulkhead")
    public String bulkheadExample() throws InterruptedException {
        Thread.sleep(2000); // simulate work
        return "Processed with concurrency limit!";
    }

    // fallback if bulkhead is full
    public String bulkheadFallback(BulkheadFullException ex) {
        return "Too many concurrent requests! Try again later.";
    }
}
