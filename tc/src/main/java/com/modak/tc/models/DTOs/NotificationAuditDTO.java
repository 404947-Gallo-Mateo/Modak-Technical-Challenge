package com.modak.tc.models.DTOs;

import com.modak.tc.models.enums.NotificationType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationAuditDTO {
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String userId;
    private String message;
    private LocalDateTime createdAt;
    private String status;
}
