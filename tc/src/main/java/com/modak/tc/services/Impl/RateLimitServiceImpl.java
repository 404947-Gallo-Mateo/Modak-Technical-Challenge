package com.modak.tc.services.Impl;

import com.modak.tc.config.RateLimitConfig;
import com.modak.tc.models.RateLimitRule;
import com.modak.tc.exceptions.RuleNotFoundException;
import com.modak.tc.models.TokenBucket;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.services.RateLimitService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class RateLimitServiceImpl implements RateLimitService {
    private final RateLimitConfig rateLimitConfig;
    private final Map<String, Map<NotificationType, TokenBucket>> userBuckets = new ConcurrentHashMap<>();

    public boolean isAllowed(NotificationType type, String userId) {
        RateLimitRule rule = type.getRule(rateLimitConfig);

        if (rule == null) {
            String message = " for type: " + type.toString() + " / user: " + userId;
            throw new RuleNotFoundException(message, userId, type.toString());
        }

        TokenBucket bucket = getUserBucket(userId, type, rule);
        return bucket.tryConsume();
    }

    private TokenBucket getUserBucket(String userId, NotificationType type, RateLimitRule rule) {
        return userBuckets
                .computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(type, k -> new TokenBucket(rule.getMaxRequests(), rule.getTokensPerSecond()));
    }

    public Map<NotificationType, Double> getCurrentUsage(String userId) {
        Map<NotificationType, Double> usage = new HashMap<>();
        Map<NotificationType, TokenBucket> buckets = userBuckets.get(userId);

        if (buckets != null) {
            buckets.forEach((type, bucket) -> {
                usage.put(type, bucket.getAvailableTokens());
            });
        }

        return usage;
    }

    public double getRemainingQuota(NotificationType type, String userId) {
        Map<NotificationType, TokenBucket> buckets = userBuckets.get(userId);
        if (buckets != null && buckets.containsKey(type)) {
            return buckets.get(type).getAvailableTokens();
        }

        RateLimitRule rule = type.getRule(rateLimitConfig);
        return rule != null ? rule.getMaxRequests() : -1.0;
    }

    public int getRetryAfterSeconds(NotificationType type, String userId) {
        Map<NotificationType, TokenBucket> buckets = userBuckets.get(userId);
        if (buckets != null && buckets.containsKey(type)) {
            return (int) Math.round(buckets.get(type).getTimeToNextToken());
        }
        return 60;
    }
}
