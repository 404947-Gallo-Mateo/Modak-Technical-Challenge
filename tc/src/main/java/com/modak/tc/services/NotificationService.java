package com.modak.tc.services;

import com.modak.tc.models.records.NotificationRequest;
import com.modak.tc.models.NotificationResponse;
import com.modak.tc.models.enums.NotificationType;

import java.util.Map;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    Map<NotificationType, Double> getRateLimitStatus(String userId);
}
