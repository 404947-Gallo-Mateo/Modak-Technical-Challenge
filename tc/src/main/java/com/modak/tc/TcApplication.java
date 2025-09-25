package com.modak.tc;

import com.modak.tc.config.RateLimitConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RateLimitConfig.class)
public class TcApplication {
	public static void main(String[] args) {
		SpringApplication.run(TcApplication.class, args);
	}
}
