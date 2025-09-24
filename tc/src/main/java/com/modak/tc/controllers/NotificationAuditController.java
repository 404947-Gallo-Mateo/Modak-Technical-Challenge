package com.modak.tc.controllers;

import com.modak.tc.models.DTOs.NotificationAuditDTO;
import com.modak.tc.models.enums.NotificationType;
import com.modak.tc.services.NotificationAuditService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit/notifications")
@AllArgsConstructor
public class NotificationAuditController {

    private final NotificationAuditService auditService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationAuditDTO>> getByUserId(
            @PathVariable String userId) {
        return ResponseEntity.ok(auditService.getAllByUserId(userId));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<NotificationAuditDTO>> getByType(
            @PathVariable NotificationType type) {
        return ResponseEntity.ok(auditService.getAllByType(type));
    }

    @GetMapping("/user/{userId}/type/{type}")
    public ResponseEntity<List<NotificationAuditDTO>> getByUserAndType(
            @PathVariable String userId,
            @PathVariable NotificationType type) {
        return ResponseEntity.ok(auditService.getAllByTypeAndUserId(type, userId));
    }

    @GetMapping("/user/{userId}/after")
    public ResponseEntity<List<NotificationAuditDTO>> getByUserAfterDate(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime after) {
        return ResponseEntity.ok(auditService.getAllByUserIdAndCreatedAtAfter(userId, after));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<NotificationAuditDTO>> getByStatus(
            @PathVariable String status) {
        return ResponseEntity.ok(auditService.getAllByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<NotificationAuditDTO>> searchNotifications(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) throws BadRequestException {

        if (userId != null && type != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(auditService.getAllNotificationsByUserTypeAndDateRange(userId, type, startDate, endDate));
        } else if (userId != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(auditService.getAllNotificationsByUserAndDateRange(userId, startDate, endDate));
        } else if (type != null && startDate != null && endDate != null) {
            return ResponseEntity.ok(auditService.getAllNotificationsByTypeAndDateRange(type, startDate, endDate));
        } else {
            throw new IllegalArgumentException("Invalid search parameters");
        }
    }
}
