package com.moretolearn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry // enables @Retryable
@EnableAspectJAutoProxy

public class SpringBootResilience4jRetryApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootResilience4jRetryApplication.class, args);
	}

}
