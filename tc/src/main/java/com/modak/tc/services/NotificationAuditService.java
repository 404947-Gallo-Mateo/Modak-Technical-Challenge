package com.modak.tc.services;

import com.modak.tc.models.DTOs.NotificationAuditDTO;
import com.modak.tc.models.entities.NotificationAuditEntity;
import com.modak.tc.models.enums.NotificationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationAuditService {
    List<NotificationAuditDTO> getAllByUserId(String userId);
    List<NotificationAuditDTO> getAllByType(NotificationType type);
    List<NotificationAuditDTO> getAllByTypeAndUserId(NotificationType type, String userId);
    List<NotificationAuditDTO> getAllByUserIdAndCreatedAtAfter(String userId, LocalDateTime after);
    List<NotificationAuditDTO> getAllByStatus(String status);

    List<NotificationAuditDTO> getAllNotificationsByUserTypeAndDateRange(String userId, NotificationType type, LocalDateTime startDate, LocalDateTime endDate);
    List<NotificationAuditDTO> getAllNotificationsByUserAndDateRange(String userId, LocalDateTime startDate, LocalDateTime endDate);
    List<NotificationAuditDTO> getAllNotificationsByTypeAndDateRange(NotificationType type, LocalDateTime startDate, LocalDateTime endDate);
}
