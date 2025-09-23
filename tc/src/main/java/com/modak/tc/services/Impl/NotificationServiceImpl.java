package com.modak.tc.services.Impl;

import com.modak.tc.exceptions.RateLimitExceededException;
import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.records.NotificationRequest;
import com.modak.tc.models.NotificationResponse;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.repositories.NotificationAuditRepository;
import com.modak.tc.services.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final RateLimitServiceImpl rateLimitService;
    private final MockGateway gateway;
    private final NotificationAuditRepository repository;

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {

        if (!rateLimitService.isAllowed(request.type(), request.userId())) {
            int retryInSeconds = rateLimitService.getRetryAfterSeconds(request.type(), request.userId());
            String message = "for type: " + request.type() + " and user: " + request.userId() + " / retry after " + retryInSeconds + " seconds.";
            throw new RateLimitExceededException(message, request.userId(), request.type().toString(), retryInSeconds);
        }

        try {
            // send notification through mock gateway
            gateway.send(request.userId(), request.message());

            NotificationAuditEntity notificationAudit = new NotificationAuditEntity();
                   notificationAudit.setType(request.type());
                   notificationAudit.setUserId(request.userId());
                   notificationAudit.setMessage(request.message());
                   notificationAudit.setCreatedAt(LocalDateTime.now());

            NotificationAuditEntity savedNotification = repository.save(notificationAudit);

            double remainingQuota = rateLimitService.getRemainingQuota(request.type(), request.userId());

            return NotificationResponse.success(
                    "Notification sent successfully",
                    LocalDateTime.now(),
                    savedNotification.getId().toString(),
                    remainingQuota
            );

        } catch (Exception e) {
            NotificationAuditEntity failedNotificationAudit = new NotificationAuditEntity();
            failedNotificationAudit.setType(request.type());
            failedNotificationAudit.setUserId(request.userId());
            failedNotificationAudit.setMessage(request.message());
            failedNotificationAudit.setCreatedAt(LocalDateTime.now());

            failedNotificationAudit.setStatus("FAILED");
            repository.save(failedNotificationAudit);

            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Override
    public Map<NotificationType, Double> getRateLimitStatus(String userId) {
        return rateLimitService.getCurrentUsage(userId);
    }
}
