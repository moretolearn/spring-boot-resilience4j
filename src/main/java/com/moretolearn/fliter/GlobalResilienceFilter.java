package com.moretolearn.fliter;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.Retry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.Callable;

@Component
public class GlobalResilienceFilter extends OncePerRequestFilter {

	private final Retry retry;
	private final CircuitBreaker circuitBreaker;
	private final RateLimiter rateLimiter;
	private final Bulkhead bulkhead;

	public GlobalResilienceFilter(Retry retry, CircuitBreaker circuitBreaker, RateLimiter rateLimiter,
			Bulkhead bulkhead) {
		this.retry = retry;
		this.circuitBreaker = circuitBreaker;
		this.rateLimiter = rateLimiter;
		this.bulkhead = bulkhead;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		Callable<Void> task = () -> {
			filterChain.doFilter(request, response);
			return null;
		};

		try {
//			Decorators.ofCallable(task).withRetry(retry).withCircuitBreaker(circuitBreaker).withRateLimiter(rateLimiter)
//					.withBulkhead(bulkhead).call();
			Decorators.DecorateCallable<Void> decorated = Decorators.ofCallable(task)
			        .withRetry(retry)
			        .withCircuitBreaker(circuitBreaker)
			        .withRateLimiter(rateLimiter)
			        .withBulkhead(bulkhead);

			decorated.decorate().call();
		} catch (Exception e) {
//			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
//			response.getWriter().write("Global resilience fallback: " + e.getMessage());
			 // map exception -> fallback
            var fallback = resolveFallback(e.getCause() != null ? e.getCause() : e);
            response.setStatus(fallback.getStatusCode().value());
            response.getWriter().write(fallback.getBody());
		}
	}
	
	public ResponseEntity<String> retryFallback(Throwable ex) {
        return ResponseEntity.status(503).body("Retry fallback: " + ex.getMessage());
    }

    public ResponseEntity<String> circuitBreakerFallback(Throwable ex) {
        return ResponseEntity.status(503).body("CircuitBreaker fallback: Service temporarily unavailable");
    }

    public ResponseEntity<String> rateLimiterFallback(Throwable ex) {
        return ResponseEntity.status(429).body("RateLimiter fallback: Too many requests, try later");
    }

    public ResponseEntity<String> bulkheadFallback(Throwable ex) {
        return ResponseEntity.status(503).body("Bulkhead fallback: Too many concurrent requests");
    }

    public ResponseEntity<String> genericFallback(Throwable ex) {
        return ResponseEntity.status(500).body("Generic fallback: " + ex.getMessage());
    }

    public ResponseEntity<String> resolveFallback(Throwable ex) {
        if (ex instanceof CallNotPermittedException) {
            return circuitBreakerFallback(ex);
        } else if (ex instanceof RequestNotPermitted) {
            return rateLimiterFallback(ex);
        } else if (ex instanceof BulkheadFullException) {
            return bulkheadFallback(ex);
        } else {
            return retryFallback(ex); // default first line of defense
        }
    }
}
