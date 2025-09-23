package com.modak.tc.models.enums;

import com.modak.tc.config.RateLimitConfig;
import com.modak.tc.config.RateLimitRule;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum NotificationType {
    STATUS("status"),
    NEWS("news"),
    MARKETING("marketing"),
    URGENT("urgent"),
    QUARTER_REPORT("quarter-report"),
    WEEKLY_REPORT("weekly-report");

    private final String configKey;

    public RateLimitRule getRule(RateLimitConfig config) {
        return config.getRuleForType(this.configKey);
    }
}
