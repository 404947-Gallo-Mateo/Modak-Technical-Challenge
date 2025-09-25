package com.modak.tc.config;

import com.modak.tc.models.RateLimitRule;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rate.limit")
@Data
public class RateLimitConfig {

    private Map<String, RateLimitRule> rules = new HashMap<>();

    public RateLimitRule getRuleForType(String type) {
        RateLimitRule rule = rules.get(type.toLowerCase());

        return new RateLimitRule(rule.getMaxRequests(), rule.getDuration(), rule.getUnit());
    }
}
