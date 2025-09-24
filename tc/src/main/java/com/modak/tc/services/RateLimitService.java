package com.modak.tc.services;

import com.modak.tc.models.enums.NotificationType;

import java.util.Map;

public interface RateLimitService {

    boolean isAllowed(NotificationType type, String userId);
    Map<NotificationType, Double> getCurrentUsage(String userId);
    double getRemainingQuota(NotificationType type, String userId);
    int getRetryAfterSeconds(NotificationType type, String userId);
}
